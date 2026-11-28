package com.smartshop.modules.order.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.order.dto.SellerOrderManageResponse;
import com.smartshop.modules.order.service.OrderService;
import com.smartshop.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;

    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SellerOrderManageResponse>>> listMyStoreOrders(
        @AuthenticationPrincipal CustomUserDetails currentUser,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<SellerOrderManageResponse> page = orderService.getSellerOrders(
            currentUser.getUserId(), pageable
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller orders retrieved", page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerOrderManageResponse>> getMyStoreOrderDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerOrderManageResponse detail = orderService.getSellerOrderDetail(
            currentUser.getUserId(), id
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller order retrieved", detail));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SellerOrderManageResponse>> confirmOrder(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerOrderManageResponse updated = orderService.confirmSellerOrder(
            currentUser.getUserId(), id
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller order confirmed", updated));
    }

    @PutMapping("/{id}/processing")
    public ResponseEntity<ApiResponse<SellerOrderManageResponse>> startProcessing(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerOrderManageResponse updated = orderService.startProcessingSellerOrder(
            currentUser.getUserId(), id
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller order is now processing", updated));
    }
}
