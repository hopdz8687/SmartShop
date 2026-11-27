package com.smartshop.modules.catalog.dto;

import com.smartshop.modules.catalog.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    private Long id;
    private Long productId;
    private Long variantId;
    private String imageUrl;
    private String altText;
    private Integer displayOrder;
    private Boolean isPrimary;
    private LocalDateTime createdAt;

    public static ProductImageResponse from(ProductImage image) {
        return ProductImageResponse.builder()
            .id(image.getId())
            .productId(image.getProduct().getId())
            .variantId(image.getVariant() != null ? image.getVariant().getId() : null)
            .imageUrl(image.getImageUrl())
            .altText(image.getAltText())
            .displayOrder(image.getDisplayOrder())
            .isPrimary(image.getIsPrimary())
            .createdAt(image.getCreatedAt())
            .build();
    }
}
