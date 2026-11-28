package com.smartshop.modules.order.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.order.dto.CheckoutRequest;
import com.smartshop.modules.order.dto.OrderResponse;
import com.smartshop.modules.order.service.CheckoutService;
import com.smartshop.modules.order.service.OrderService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public OrderController(CheckoutService checkoutService, OrderService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
        @Valid @RequestBody CheckoutRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        OrderResponse order = checkoutService.checkout(currentUser.getUserId(), request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Order placed successfully", order));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
        @AuthenticationPrincipal CustomUserDetails currentUser,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<OrderResponse> orders = orderService.getMyOrders(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("Orders retrieved", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrderDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        OrderResponse order = orderService.getMyOrderDetail(currentUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved", order));
    }
}
