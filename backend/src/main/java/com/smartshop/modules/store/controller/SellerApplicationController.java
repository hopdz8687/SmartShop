package com.smartshop.modules.store.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.store.dto.CreateSellerApplicationRequest;
import com.smartshop.modules.store.dto.SellerApplicationResponse;
import com.smartshop.modules.store.service.SellerApplicationService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller-applications")
public class SellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    public SellerApplicationController(SellerApplicationService sellerApplicationService) {
        this.sellerApplicationService = sellerApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<SellerApplicationResponse>> submit(
        @Valid @RequestBody CreateSellerApplicationRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerApplicationResponse response = sellerApplicationService.submitApplication(
            currentUser.getUserId(), request
        );
        return ResponseEntity.status(201).body(ApiResponse.ok("Seller application submitted", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<SellerApplicationResponse>>> getMyApplications(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<SellerApplicationResponse> response = sellerApplicationService.getMyApplications(
            currentUser.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller applications retrieved", response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<SellerApplicationResponse>> cancel(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerApplicationResponse response = sellerApplicationService.cancelApplication(
            id, currentUser.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller application cancelled", response));
    }
}
