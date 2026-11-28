package com.smartshop.modules.order.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.order.dto.CheckoutRequest;
import com.smartshop.modules.order.dto.OrderResponse;
import com.smartshop.modules.order.service.CheckoutService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CheckoutService checkoutService;

    public OrderController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
        @Valid @RequestBody CheckoutRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        OrderResponse order = checkoutService.checkout(currentUser.getUserId(), request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Order placed successfully", order));
    }
}
