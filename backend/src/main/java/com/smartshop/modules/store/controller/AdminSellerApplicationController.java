package com.smartshop.modules.store.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.store.dto.RejectSellerApplicationRequest;
import com.smartshop.modules.store.dto.SellerApplicationResponse;
import com.smartshop.modules.store.entity.SellerApplicationStatus;
import com.smartshop.modules.store.service.SellerApplicationService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/seller-applications")
public class AdminSellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    public AdminSellerApplicationController(SellerApplicationService sellerApplicationService) {
        this.sellerApplicationService = sellerApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SellerApplicationResponse>>> list(
        @RequestParam(required = false) SellerApplicationStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<SellerApplicationResponse> page = sellerApplicationService.getApplicationsForAdmin(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Seller applications retrieved", page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerApplicationResponse>> getById(@PathVariable Long id) {
        SellerApplicationResponse response = sellerApplicationService.getApplicationByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok("Seller application retrieved", response));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<SellerApplicationResponse>> approve(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerApplicationResponse response = sellerApplicationService.approveApplication(
            id, currentUser.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller application approved", response));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<SellerApplicationResponse>> reject(
        @PathVariable Long id,
        @Valid @RequestBody RejectSellerApplicationRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        SellerApplicationResponse response = sellerApplicationService.rejectApplication(
            id, currentUser.getUserId(), request
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller application rejected", response));
    }
}
