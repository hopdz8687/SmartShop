package com.smartshop.modules.store.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.store.dto.StoreResponse;
import com.smartshop.modules.store.dto.UpdateStoreRequest;
import com.smartshop.modules.store.service.StoreService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/stores")
public class SellerStoreController {

    private final StoreService storeService;

    public SellerStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StoreResponse>> getMyStore(
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        StoreResponse response = storeService.getMyStore(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Store retrieved", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StoreResponse>> updateMyStore(
        @Valid @RequestBody UpdateStoreRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        StoreResponse response = storeService.updateMyStore(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Store updated", response));
    }
}
