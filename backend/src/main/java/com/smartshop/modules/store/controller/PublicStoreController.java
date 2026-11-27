package com.smartshop.modules.store.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.store.dto.PublicStoreResponse;
import com.smartshop.modules.store.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
public class PublicStoreController {

    private final StoreService storeService;

    public PublicStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicStoreResponse>> getStore(@PathVariable Long id) {
        PublicStoreResponse response = storeService.getPublicStore(id);
        return ResponseEntity.ok(ApiResponse.ok("Store retrieved", response));
    }
}
