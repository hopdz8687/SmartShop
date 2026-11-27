package com.smartshop.modules.catalog.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.catalog.dto.ProductDetailResponse;
import com.smartshop.modules.catalog.dto.ProductSummaryResponse;
import com.smartshop.modules.catalog.dto.UpdateProductStatusRequest;
import com.smartshop.modules.catalog.entity.ProductStatus;
import com.smartshop.modules.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> listAll(
        @RequestParam(required = false) ProductStatus status,
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false) Long categoryId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryResponse> page = productService.getAdminProducts(
            status, storeId, categoryId, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved", page));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        ProductDetailResponse updated = productService.updateProductStatusByAdmin(
            id, request.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.ok("Product status updated", updated));
    }
}
