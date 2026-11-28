package com.smartshop.modules.order.dto;

import com.smartshop.modules.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long variantId;
    private String productName;
    private String productImage;
    private String variantDescription;
    private String sku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .variantId(item.getVariant().getId())
            .productName(item.getProductName())
            .productImage(item.getProductImage())
            .variantDescription(item.getVariantDescription())
            .sku(item.getSku())
            .unitPrice(item.getUnitPrice())
            .quantity(item.getQuantity())
            .subtotal(item.getSubtotal())
            .build();
    }
}
