package com.smartshop.modules.cart.service;

import com.smartshop.common.exception.BusinessException;
import com.smartshop.common.exception.ResourceNotFoundException;
import com.smartshop.modules.cart.dto.AddCartItemRequest;
import com.smartshop.modules.cart.dto.CartItemResponse;
import com.smartshop.modules.cart.dto.CartResponse;
import com.smartshop.modules.cart.dto.UpdateCartItemRequest;
import com.smartshop.modules.cart.entity.Cart;
import com.smartshop.modules.cart.entity.CartItem;
import com.smartshop.modules.cart.repository.CartItemRepository;
import com.smartshop.modules.cart.repository.CartRepository;
import com.smartshop.modules.catalog.entity.CategoryStatus;
import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductImage;
import com.smartshop.modules.catalog.entity.ProductStatus;
import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import com.smartshop.modules.catalog.repository.ProductImageRepository;
import com.smartshop.modules.catalog.repository.ProductVariantRepository;
import com.smartshop.modules.store.entity.StoreStatus;
import com.smartshop.modules.user.entity.User;
import com.smartshop.modules.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final UserRepository userRepository;

    public CartService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ProductVariantRepository variantRepository,
        ProductImageRepository imageRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addCartItem(Long userId, AddCartItemRequest request) {
        ProductVariant variant = variantRepository.findWithFullProductById(request.getVariantId())
            .orElseThrow(() -> new ResourceNotFoundException("VARIANT_NOT_FOUND", "Product variant not found"));

        validateVariantPurchasable(variant);

        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
            .orElse(null);

        int targetQty = item != null ? item.getQuantity() + request.getQuantity() : request.getQuantity();
        if (targetQty > variant.getStockQuantity()) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INSUFFICIENT_STOCK",
                "Requested quantity exceeds available stock"
            );
        }

        if (item == null) {
            item = CartItem.builder()
                .cart(cart)
                .variant(variant)
                .quantity(targetQty)
                .build();
        } else {
            item.setQuantity(targetQty);
        }

        cartItemRepository.saveAndFlush(item);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem item = findOwnedCartItem(userId, cartItemId);
        ProductVariant variant = item.getVariant();

        validateVariantPurchasable(variant);
        if (request.getQuantity() > variant.getStockQuantity()) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "INSUFFICIENT_STOCK",
                "Requested quantity exceeds available stock"
            );
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.saveAndFlush(item);
        return buildCartResponse(item.getCart());
    }

    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        CartItem item = findOwnedCartItem(userId, cartItemId);
        Cart cart = item.getCart();
        cartItemRepository.delete(item);
        cartItemRepository.flush();
        return buildCartResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
                return cartRepository.saveAndFlush(Cart.builder().user(user).build());
            });
    }

    private CartItem findOwnedCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findWithDetailsById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("CART_ITEM_NOT_FOUND", "Cart item not found"));
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this cart item"
            );
        }
        return item;
    }

    private void validateVariantPurchasable(ProductVariant variant) {
        Product product = variant.getProduct();
        if (variant.getStatus() != VariantStatus.ACTIVE
            || product.getDeletedAt() != null
            || product.getStatus() != ProductStatus.ACTIVE
            || product.getStore().getStatus() != StoreStatus.ACTIVE
            || product.getCategory().getStatus() != CategoryStatus.ACTIVE) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "PRODUCT_NOT_AVAILABLE",
                "Product or variant is not currently available for purchase"
            );
        }
    }

    private boolean isAvailable(ProductVariant variant, int quantity) {
        Product product = variant.getProduct();
        return variant.getStatus() == VariantStatus.ACTIVE
            && product.getDeletedAt() == null
            && product.getStatus() == ProductStatus.ACTIVE
            && product.getStore().getStatus() == StoreStatus.ACTIVE
            && product.getCategory().getStatus() == CategoryStatus.ACTIVE
            && variant.getStockQuantity() >= quantity;
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findWithDetailsByCartIdOrderByCreatedAtDesc(cart.getId());
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

        BigDecimal cartSubtotal = BigDecimal.ZERO;
        int totalItems = 0;

        List<CartItemResponse> itemResponses = new java.util.ArrayList<>(items.size());
        for (CartItem item : items) {
            ProductVariant v = item.getVariant();
            Product p = v.getProduct();
            BigDecimal lineSubtotal = v.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            cartSubtotal = cartSubtotal.add(lineSubtotal);
            totalItems += item.getQuantity();

            itemResponses.add(CartItemResponse.builder()
                .id(item.getId())
                .variantId(v.getId())
                .productId(p.getId())
                .storeId(p.getStore().getId())
                .storeName(p.getStore().getStoreName())
                .productName(p.getName())
                .variantName(v.getVariantName())
                .sku(v.getSku())
                .unitPrice(v.getPrice())
                .quantity(item.getQuantity())
                .subtotal(lineSubtotal)
                .stockQuantity(v.getStockQuantity())
                .available(isAvailable(v, item.getQuantity()))
                .primaryImageUrl(primaryImages.get(p.getId()))
                .build());
        }

        return CartResponse.builder()
            .cartId(cart.getId())
            .totalItems(totalItems)
            .subtotal(cartSubtotal)
            .items(itemResponses)
            .build();
    }
}
