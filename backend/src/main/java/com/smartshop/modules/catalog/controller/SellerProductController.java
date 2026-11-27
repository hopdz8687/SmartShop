package com.smartshop.modules.catalog.controller;

import com.smartshop.common.response.ApiResponse;
import com.smartshop.modules.catalog.dto.CreateProductRequest;
import com.smartshop.modules.catalog.dto.ProductDetailResponse;
import com.smartshop.modules.catalog.dto.ProductImageRequest;
import com.smartshop.modules.catalog.dto.ProductImageResponse;
import com.smartshop.modules.catalog.dto.ProductSummaryResponse;
import com.smartshop.modules.catalog.dto.ProductVariantRequest;
import com.smartshop.modules.catalog.dto.ProductVariantResponse;
import com.smartshop.modules.catalog.dto.UpdateProductRequest;
import com.smartshop.modules.catalog.service.ProductService;
import com.smartshop.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/seller")
public class SellerProductController {

    private final ProductService productService;

    public SellerProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> listMyProducts(
        @AuthenticationPrincipal CustomUserDetails currentUser,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductSummaryResponse> page = productService.getSellerProducts(
            currentUser.getUserId(), pageable
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller products retrieved", page));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getMyProduct(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ProductDetailResponse detail = productService.getSellerProductDetail(
            currentUser.getUserId(), id
        );
        return ResponseEntity.ok(ApiResponse.ok("Seller product retrieved", detail));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
        @Valid @RequestBody CreateProductRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ProductDetailResponse created = productService.createProduct(
            currentUser.getUserId(), request
        );
        return ResponseEntity.status(201).body(ApiResponse.ok("Product created", created));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ProductDetailResponse updated = productService.updateProduct(
            currentUser.getUserId(), id, request
        );
        return ResponseEntity.ok(ApiResponse.ok("Product updated", updated));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        productService.deleteProduct(currentUser.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
    }

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
        @PathVariable Long productId,
        @Valid @RequestBody ProductVariantRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ProductVariantResponse created = productService.addVariant(
            currentUser.getUserId(), productId, request
        );
        return ResponseEntity.status(201).body(ApiResponse.ok("Variant created", created));
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
        @PathVariable Long variantId,
        @Valid @RequestBody ProductVariantRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ProductVariantResponse updated = productService.updateVariant(
            currentUser.getUserId(), variantId, request
        );
        return ResponseEntity.ok(ApiResponse.ok("Variant updated", updated));
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deactivateVariant(
        @PathVariable Long variantId,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        productService.deactivateVariant(currentUser.getUserId(), variantId);
        return ResponseEntity.ok(ApiResponse.ok("Variant deactivated", null));
    }

    @PostMapping("/products/{productId}/images")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
        @PathVariable Long productId,
        @Valid @RequestBody ProductImageRequest request,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        ProductImageResponse created = productService.addImage(
            currentUser.getUserId(), productId, request
        );
        return ResponseEntity.status(201).body(ApiResponse.ok("Product image added", created));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
        @PathVariable Long imageId,
        @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        productService.deleteImage(currentUser.getUserId(), imageId);
        return ResponseEntity.ok(ApiResponse.ok("Product image deleted", null));
    }
}
