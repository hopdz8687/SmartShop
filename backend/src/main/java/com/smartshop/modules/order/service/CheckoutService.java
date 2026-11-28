package com.smartshop.modules.order.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.cart.entity.CartItem;
import com.smartshop.modules.cart.repository.CartItemRepository;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductImage;
import com.smartshop.modules.catalog.entity.ProductStatus;
import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import com.smartshop.modules.catalog.repository.ProductImageRepository;
import com.smartshop.modules.catalog.repository.ProductVariantRepository;
import com.smartshop.modules.order.dto.CheckoutRequest;
import com.smartshop.modules.order.dto.OrderItemResponse;
import com.smartshop.modules.order.dto.OrderResponse;
import com.smartshop.modules.order.dto.PaymentResponse;
import com.smartshop.modules.order.dto.SellerOrderResponse;
import com.smartshop.modules.order.entity.Order;
import com.smartshop.modules.order.entity.OrderItem;
import com.smartshop.modules.order.entity.OrderStatus;
import com.smartshop.modules.order.entity.Payment;
import com.smartshop.modules.order.entity.PaymentMethod;
import com.smartshop.modules.order.entity.PaymentStatus;
import com.smartshop.modules.order.entity.SellerOrder;
import com.smartshop.modules.order.entity.SellerOrderStatus;
import com.smartshop.modules.order.repository.OrderItemRepository;
import com.smartshop.modules.order.repository.OrderRepository;
import com.smartshop.modules.order.repository.PaymentRepository;
import com.smartshop.modules.order.repository.SellerOrderRepository;
import com.smartshop.modules.store.entity.StoreStatus;
import com.smartshop.modules.store.repository.StoreRepository;
import com.smartshop.modules.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CheckoutService {

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public CheckoutService(
        CartItemRepository cartItemRepository,
        ProductVariantRepository variantRepository,
        ProductImageRepository imageRepository,
        OrderRepository orderRepository,
        SellerOrderRepository sellerOrderRepository,
        OrderItemRepository orderItemRepository,
        PaymentRepository paymentRepository,
        UserRepository userRepository,
        StoreRepository storeRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.orderRepository = orderRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }

    private record CheckoutLineSnapshot(
        Long cartItemId,
        Long storeId,
        Long variantId,
        String productName,
        String productImage,
        String variantDescription,
        String sku,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
    ) {
    }

    /**
     * Multi-vendor COD checkout within a single database transaction:
     * validates ownership, re-reads catalog state, atomically decrements stock in ascending variantId order,
     * creates parent Order, per-Store SellerOrders, OrderItem snapshots, COD Payments,
     * and deletes only the selected CartItems.
     */
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        List<Long> uniqueIds = request.getCartItemIds().stream().distinct().toList();
        List<CartItem> items = cartItemRepository.findWithDetailsByIdIn(uniqueIds);

        if (items.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("CART_ITEM_NOT_FOUND", "One or more selected cart items were not found");
        }

        for (CartItem item : items) {
            if (!item.getCart().getUser().getId().equals(userId)) {
                throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "ACCESS_DENIED",
                    "You can only checkout items from your own cart"
                );
            }
        }

        List<Long> productIds = items.stream()
            .map(i -> i.getVariant().getProduct().getId())
            .distinct()
            .toList();
        Map<Long, String> primaryImages = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (ProductImage img : imageRepository.findByProductIdInAndIsPrimaryTrue(productIds)) {
                primaryImages.putIfAbsent(img.getProduct().getId(), img.getImageUrl());
            }
        }

        List<CheckoutLineSnapshot> snapshots = new ArrayList<>(items.size());
        for (CartItem item : items) {
            ProductVariant variant = item.getVariant();
            Product product = variant.getProduct();

            if (variant.getStatus() != VariantStatus.ACTIVE
                || product.getDeletedAt() != null
                || product.getStatus() != ProductStatus.ACTIVE
                || product.getStore().getStatus() != StoreStatus.ACTIVE
                || product.getCategory().getStatus() != CategoryStatus.ACTIVE) {
                throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "PRODUCT_NOT_AVAILABLE",
                    "Product is no longer available: " + product.getName()
                );
            }

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "INSUFFICIENT_STOCK",
                    "Insufficient stock for SKU: " + variant.getSku()
                );
            }

            BigDecimal lineSubtotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            snapshots.add(new CheckoutLineSnapshot(
                item.getId(),
                product.getStore().getId(),
                variant.getId(),
                product.getName(),
                primaryImages.get(product.getId()),
                variant.getVariantName(),
                variant.getSku(),
                variant.getPrice(),
                item.getQuantity(),
                lineSubtotal
            ));
        }

        // Atomic conditional stock decrement in ascending variantId order to reduce deadlock risk
        List<CheckoutLineSnapshot> sortedForStock = snapshots.stream()
            .sorted(Comparator.comparing(CheckoutLineSnapshot::variantId))
            .toList();
        for (CheckoutLineSnapshot line : sortedForStock) {
            int updated = variantRepository.decrementStockIfAvailable(line.variantId(), line.quantity());
            if (updated == 0) {
                throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "INSUFFICIENT_STOCK",
                    "Insufficient stock for SKU: " + line.sku()
                );
            }
        }

        // Group lines by store_id
        Map<Long, List<CheckoutLineSnapshot>> byStore = new LinkedHashMap<>();
        for (CheckoutLineSnapshot line : snapshots) {
            byStore.computeIfAbsent(line.storeId(), k -> new ArrayList<>()).add(line);
        }

        BigDecimal orderSubtotal = snapshots.stream()
            .map(CheckoutLineSnapshot::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
            .user(userRepository.getReferenceById(userId))
            .orderCode(generateOrderCode("ORD"))
            .recipientName(request.getRecipientName().trim())
            .recipientPhone(request.getRecipientPhone().trim())
            .shippingProvince(request.getShippingProvince().trim())
            .shippingDistrict(trimToNull(request.getShippingDistrict()))
            .shippingWard(request.getShippingWard().trim())
            .shippingAddress(request.getShippingAddress().trim())
            .customerNote(trimToNull(request.getCustomerNote()))
            .subtotal(orderSubtotal)
            .shippingFee(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(orderSubtotal)
            .orderStatus(OrderStatus.PENDING)
            .build();
        Order savedOrder = orderRepository.saveAndFlush(order);

        List<SellerOrderResponse> sellerOrderResponses = new ArrayList<>(byStore.size());
        for (Map.Entry<Long, List<CheckoutLineSnapshot>> entry : byStore.entrySet()) {
            Long storeId = entry.getKey();
            List<CheckoutLineSnapshot> storeLines = entry.getValue();

            BigDecimal storeSubtotal = storeLines.stream()
                .map(CheckoutLineSnapshot::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            SellerOrder sellerOrder = SellerOrder.builder()
                .order(savedOrder)
                .store(storeRepository.getReferenceById(storeId))
                .sellerOrderCode(generateOrderCode("SO"))
                .subtotal(storeSubtotal)
                .shippingFee(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(storeSubtotal)
                .status(SellerOrderStatus.PENDING)
                .build();
            SellerOrder savedSellerOrder = sellerOrderRepository.saveAndFlush(sellerOrder);

            List<OrderItemResponse> itemResponses = new ArrayList<>(storeLines.size());
            for (CheckoutLineSnapshot line : storeLines) {
                OrderItem orderItem = OrderItem.builder()
                    .sellerOrder(savedSellerOrder)
                    .variant(variantRepository.getReferenceById(line.variantId()))
                    .productName(line.productName())
                    .productImage(line.productImage())
                    .variantDescription(line.variantDescription())
                    .sku(line.sku())
                    .unitPrice(line.unitPrice())
                    .quantity(line.quantity())
                    .subtotal(line.subtotal())
                    .build();
                OrderItem savedItem = orderItemRepository.saveAndFlush(orderItem);
                itemResponses.add(OrderItemResponse.from(savedItem));
            }

            Payment payment = Payment.builder()
                .sellerOrder(savedSellerOrder)
                .paymentMethod(PaymentMethod.COD)
                .amount(savedSellerOrder.getTotalAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
            Payment savedPayment = paymentRepository.saveAndFlush(payment);

            sellerOrderResponses.add(SellerOrderResponse.of(
                savedSellerOrder,
                itemResponses,
                PaymentResponse.from(savedPayment)
            ));
        }

        // Delete only selected cart items after all order & stock updates succeed
        cartItemRepository.deleteByIdIn(uniqueIds);
        cartItemRepository.flush();

        return OrderResponse.of(savedOrder, sellerOrderResponses);
    }

    private String generateOrderCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
