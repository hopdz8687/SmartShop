package com.smartshop.modules.catalog.dto;

import com.smartshop.modules.catalog.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 150, message = "Brand must not exceed 150 characters")
    private String brand;

    private ProductStatus status;

    @Valid
    @NotEmpty(message = "At least one product variant is required")
    private List<ProductVariantRequest> variants;

    @Valid
    private List<ProductImageRequest> images;
}
