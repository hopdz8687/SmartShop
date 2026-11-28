package com.smartshop.modules.cart.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.cart.dto.AddCartItemRequest;
import com.smartshop.modules.cart.dto.CartResponse;
import com.smartshop.modules.cart.dto.UpdateCartItemRequest;
import com.smartshop.modules.cart.service.CartService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        CartResponse cart = cartService.getCart(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Cart retrieved", cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
        @Valid @RequestBody AddCartItemRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        CartResponse cart = cartService.addCartItem(currentUser.getUserId(), request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Cart item added", cart));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCartItemRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        CartResponse cart = cartService.updateCartItem(currentUser.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", cart));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        CartResponse cart = cartService.removeCartItem(currentUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Cart item removed", cart));
    }
}
