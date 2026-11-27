package com.smartshop.modules.catalog.dto;

import com.smartshop.modules.catalog.entity.ProductVariant;
import com.smartshop.modules.catalog.entity.VariantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {

    private Long id;
    private Long productId;
    private String sku;
    private String variantName;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isDefault;
    private VariantStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductVariantResponse from(ProductVariant variant) {
        return ProductVariantResponse.builder()
            .id(variant.getId())
            .productId(variant.getProduct().getId())
            .sku(variant.getSku())
            .variantName(variant.getVariantName())
            .price(variant.getPrice())
            .stockQuantity(variant.getStockQuantity())
            .isDefault(variant.getIsDefault())
            .status(variant.getStatus())
            .createdAt(variant.getCreatedAt())
            .updatedAt(variant.getUpdatedAt())
            .build();
    }
}
