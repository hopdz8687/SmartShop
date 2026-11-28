package com.smartshop.modules.order.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.order.dto.OrderItemResponse;
import com.smartshop.modules.order.dto.OrderResponse;
import com.smartshop.modules.order.dto.PaymentResponse;
import com.smartshop.modules.order.dto.SellerOrderManageResponse;
import com.smartshop.modules.order.dto.SellerOrderResponse;
import com.smartshop.modules.order.entity.Order;
import com.smartshop.modules.order.entity.OrderItem;
import com.smartshop.modules.order.entity.OrderStatus;
import com.smartshop.modules.order.entity.Payment;
import com.smartshop.modules.order.entity.SellerOrder;
import com.smartshop.modules.order.entity.SellerOrderStatus;
import com.smartshop.modules.order.repository.OrderItemRepository;
import com.smartshop.modules.order.repository.OrderRepository;
import com.smartshop.modules.order.repository.PaymentRepository;
import com.smartshop.modules.order.repository.SellerOrderRepository;
import com.smartshop.modules.store.entity.Store;
import com.smartshop.modules.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final StoreRepository storeRepository;

    public OrderService(
        OrderRepository orderRepository,
        SellerOrderRepository sellerOrderRepository,
        OrderItemRepository orderItemRepository,
        PaymentRepository paymentRepository,
        StoreRepository storeRepository
    ) {
        this.orderRepository = orderRepository;
        this.sellerOrderRepository = sellerOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Order> orders = ordersPage.getContent();
        if (orders.isEmpty()) {
            return ordersPage.map(o -> OrderResponse.of(o, List.of()));
        }

        Map<Long, List<SellerOrderResponse>> sellerOrdersByOrderId = loadSellerOrdersByOrderIds(
            orders.stream().map(Order::getId).toList()
        );

        return ordersPage.map(order -> OrderResponse.of(
            order,
            sellerOrdersByOrderId.getOrDefault(order.getId(), List.of())
        ));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to view this order"
            );
        }

        Map<Long, List<SellerOrderResponse>> sellerOrdersByOrderId = loadSellerOrdersByOrderIds(List.of(orderId));
        return OrderResponse.of(order, sellerOrdersByOrderId.getOrDefault(orderId, List.of()));
    }

    private Map<Long, List<SellerOrderResponse>> loadSellerOrdersByOrderIds(List<Long> orderIds) {
        List<SellerOrder> sellerOrders = sellerOrderRepository.findWithStoreByOrderIdInOrderByIdAsc(orderIds);
        if (sellerOrders.isEmpty()) {
            return Map.of();
        }

        List<Long> sellerOrderIds = sellerOrders.stream().map(SellerOrder::getId).toList();

        Map<Long, List<OrderItemResponse>> itemsBySellerOrderId = new HashMap<>();
        for (OrderItem item : orderItemRepository.findBySellerOrderIdInOrderByIdAsc(sellerOrderIds)) {
            itemsBySellerOrderId
                .computeIfAbsent(item.getSellerOrder().getId(), k -> new ArrayList<>())
                .add(OrderItemResponse.from(item));
        }

        Map<Long, PaymentResponse> paymentBySellerOrderId = new HashMap<>();
        for (Payment payment : paymentRepository.findBySellerOrderIdIn(sellerOrderIds)) {
            paymentBySellerOrderId.put(payment.getSellerOrder().getId(), PaymentResponse.from(payment));
        }

        Map<Long, List<SellerOrderResponse>> result = new HashMap<>();
        for (SellerOrder so : sellerOrders) {
            SellerOrderResponse response = SellerOrderResponse.of(
                so,
                itemsBySellerOrderId.getOrDefault(so.getId(), List.of()),
                paymentBySellerOrderId.get(so.getId())
            );
            result.computeIfAbsent(so.getOrder().getId(), k -> new ArrayList<>()).add(response);
        }
        return result;
    }

    // ─── Seller Order Management ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SellerOrderManageResponse> getSellerOrders(Long sellerUserId, Pageable pageable) {
        Store store = resolveSellerStore(sellerUserId);
        Page<SellerOrder> page = sellerOrderRepository.findByStoreIdOrderByCreatedAtDesc(
            store.getId(), pageable
        );
        List<SellerOrder> sellerOrders = page.getContent();
        if (sellerOrders.isEmpty()) {
            return page.map(so -> SellerOrderManageResponse.of(so, List.of(), null));
        }

        List<Long> ids = sellerOrders.stream().map(SellerOrder::getId).toList();
        Map<Long, List<OrderItemResponse>> itemsMap = loadItemsMap(ids);
        Map<Long, PaymentResponse> paymentsMap = loadPaymentsMap(ids);

        return page.map(so -> SellerOrderManageResponse.of(
            so,
            itemsMap.getOrDefault(so.getId(), List.of()),
            paymentsMap.get(so.getId())
        ));
    }

    @Transactional(readOnly = true)
    public SellerOrderManageResponse getSellerOrderDetail(Long sellerUserId, Long sellerOrderId) {
        Store store = resolveSellerStore(sellerUserId);
        SellerOrder so = findOwnedSellerOrder(sellerOrderId, store.getId());
        return buildSellerOrderManageResponse(so);
    }

    @Transactional
    public SellerOrderManageResponse confirmSellerOrder(Long sellerUserId, Long sellerOrderId) {
        return transitionSellerOrderStatus(
            sellerUserId,
            sellerOrderId,
            SellerOrderStatus.PENDING,
            SellerOrderStatus.CONFIRMED
        );
    }

    @Transactional
    public SellerOrderManageResponse startProcessingSellerOrder(Long sellerUserId, Long sellerOrderId) {
        return transitionSellerOrderStatus(
            sellerUserId,
            sellerOrderId,
            SellerOrderStatus.CONFIRMED,
            SellerOrderStatus.PROCESSING
        );
    }

    private SellerOrderManageResponse transitionSellerOrderStatus(
        Long sellerUserId,
        Long sellerOrderId,
        SellerOrderStatus expectedStatus,
        SellerOrderStatus targetStatus
    ) {
        Store store = resolveSellerStore(sellerUserId);
        SellerOrder existing = findOwnedSellerOrder(sellerOrderId, store.getId());
        Long parentOrderId = existing.getOrder().getId();

        int updated = sellerOrderRepository.transitionStatusIfExpected(
            sellerOrderId,
            store.getId(),
            expectedStatus,
            targetStatus,
            LocalDateTime.now()
        );
        if (updated == 0) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INVALID_ORDER_STATE",
                "Order must be in " + expectedStatus + " status to transition to " + targetStatus
            );
        }

        recalculateParentOrderStatus(parentOrderId);

        SellerOrder refreshed = sellerOrderRepository.findWithOrderAndStoreById(sellerOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("SELLER_ORDER_NOT_FOUND", "Seller order not found"));
        return buildSellerOrderManageResponse(refreshed);
    }

    /**
     * Derives parent {@code orders.order_status} centrally from child {@code seller_orders}.
     */
    public void recalculateParentOrderStatus(Long orderId) {
        Order parent = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", "Order not found"));
        List<SellerOrder> children = sellerOrderRepository.findByOrderId(orderId);
        if (children.isEmpty()) {
            return;
        }

        List<SellerOrder> activeChildren = children.stream()
            .filter(so -> so.getStatus() != SellerOrderStatus.CANCELLED)
            .toList();

        OrderStatus derivedStatus;
        if (activeChildren.isEmpty()) {
            derivedStatus = OrderStatus.CANCELLED;
        } else if (activeChildren.stream().allMatch(so -> so.getStatus() == SellerOrderStatus.DELIVERED)) {
            derivedStatus = OrderStatus.DELIVERED;
        } else if (activeChildren.stream().allMatch(
            so -> so.getStatus() == SellerOrderStatus.SHIPPED || so.getStatus() == SellerOrderStatus.DELIVERED
        )) {
            derivedStatus = OrderStatus.SHIPPED;
        } else if (activeChildren.stream().anyMatch(
            so -> so.getStatus() == SellerOrderStatus.SHIPPED || so.getStatus() == SellerOrderStatus.DELIVERED
        )) {
            derivedStatus = OrderStatus.PARTIALLY_SHIPPED;
        } else if (activeChildren.stream().allMatch(so -> so.getStatus() == SellerOrderStatus.PENDING)) {
            derivedStatus = OrderStatus.PENDING;
        } else {
            derivedStatus = OrderStatus.PROCESSING;
        }

        if (parent.getOrderStatus() != derivedStatus) {
            parent.setOrderStatus(derivedStatus);
            orderRepository.saveAndFlush(parent);
        }
    }

    private Store resolveSellerStore(Long sellerUserId) {
        return storeRepository.findByUserId(sellerUserId)
            .filter(s -> s.getDeletedAt() == null)
            .orElseThrow(() -> new ResourceNotFoundException("STORE_NOT_FOUND", "Store not found for current seller"));
    }

    private SellerOrder findOwnedSellerOrder(Long sellerOrderId, Long storeId) {
        SellerOrder so = sellerOrderRepository.findWithOrderAndStoreById(sellerOrderId)
            .orElseThrow(() -> new ResourceNotFoundException("SELLER_ORDER_NOT_FOUND", "Seller order not found"));
        if (!so.getStore().getId().equals(storeId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this seller order"
            );
        }
        return so;
    }

    private SellerOrderManageResponse buildSellerOrderManageResponse(SellerOrder so) {
        List<Long> ids = List.of(so.getId());
        Map<Long, List<OrderItemResponse>> itemsMap = loadItemsMap(ids);
        Map<Long, PaymentResponse> paymentsMap = loadPaymentsMap(ids);
        return SellerOrderManageResponse.of(
            so,
            itemsMap.getOrDefault(so.getId(), List.of()),
            paymentsMap.get(so.getId())
        );
    }

    private Map<Long, List<OrderItemResponse>> loadItemsMap(List<Long> sellerOrderIds) {
        Map<Long, List<OrderItemResponse>> map = new HashMap<>();
        for (OrderItem item : orderItemRepository.findBySellerOrderIdInOrderByIdAsc(sellerOrderIds)) {
            map.computeIfAbsent(item.getSellerOrder().getId(), k -> new ArrayList<>())
                .add(OrderItemResponse.from(item));
        }
        return map;
    }

    private Map<Long, PaymentResponse> loadPaymentsMap(List<Long> sellerOrderIds) {
        Map<Long, PaymentResponse> map = new HashMap<>();
        for (Payment payment : paymentRepository.findBySellerOrderIdIn(sellerOrderIds)) {
            map.put(payment.getSellerOrder().getId(), PaymentResponse.from(payment));
        }
        return map;
    }
}
