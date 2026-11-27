package com.smartshop.modules.catalog.dto;

import com.smartshop.modules.catalog.entity.Product;
import com.smartshop.modules.catalog.entity.ProductStatus;
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
public class ProductSummaryResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String slug;
    private String brand;
    private ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String primaryImageUrl;
    private LocalDateTime createdAt;

    public static ProductSummaryResponse of(
        Product product,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String primaryImageUrl
    ) {
        return ProductSummaryResponse.builder()
            .id(product.getId())
            .storeId(product.getStore().getId())
            .storeName(product.getStore().getStoreName())
            .categoryId(product.getCategory().getId())
            .categoryName(product.getCategory().getName())
            .name(product.getName())
            .slug(product.getSlug())
            .brand(product.getBrand())
            .status(product.getStatus())
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .primaryImageUrl(primaryImageUrl)
            .createdAt(product.getCreatedAt())
            .build();
    }
}
