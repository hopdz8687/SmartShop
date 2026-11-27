package com.smartshop.modules.catalog.dto;

import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;

    public static ProductDetailResponse of(
        Product product,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images
    ) {
        return ProductDetailResponse.builder()
            .id(product.getId())
            .storeId(product.getStore().getId())
            .storeName(product.getStore().getStoreName())
            .categoryId(product.getCategory().getId())
            .categoryName(product.getCategory().getName())
            .name(product.getName())
            .slug(product.getSlug())
            .description(product.getDescription())
            .brand(product.getBrand())
            .status(product.getStatus())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .variants(variants)
            .images(images)
            .build();
    }
}
