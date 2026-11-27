package com.smartshop.modules.catalog.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.catalog.dto.ProductDetailResponse;
import com.smartshop.modules.catalog.dto.ProductSummaryResponse;
import com.smartshop.modules.catalog.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
public class PublicProductController {

    private final ProductService productService;

    public PublicProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> list(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryResponse> page = productService.getPublicProducts(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved", page));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long storeId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false, defaultValue = "newest") String sort,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductSummaryResponse> page = productService.searchPublicProducts(
            keyword, categoryId, storeId, minPrice, maxPrice, sort, unsortedPageable
        );
        return ResponseEntity.ok(ApiResponse.ok("Products retrieved", page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getById(@PathVariable Long id) {
        ProductDetailResponse detail = productService.getPublicProductDetail(id);
        return ResponseEntity.ok(ApiResponse.ok("Product retrieved", detail));
    }
}
