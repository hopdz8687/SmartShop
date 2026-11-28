package com.smartshop.modules.order.dto;

import com.smartshop.modules.order.entity.SellerOrder;
import com.smartshop.modules.order.entity.SellerOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerOrderResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String sellerOrderCode;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private SellerOrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;

    public static SellerOrderResponse of(
        SellerOrder sellerOrder,
        List<OrderItemResponse> items,
        PaymentResponse payment
    ) {
        return SellerOrderResponse.builder()
            .id(sellerOrder.getId())
            .storeId(sellerOrder.getStore().getId())
            .storeName(sellerOrder.getStore().getStoreName())
            .sellerOrderCode(sellerOrder.getSellerOrderCode())
            .subtotal(sellerOrder.getSubtotal())
            .shippingFee(sellerOrder.getShippingFee())
            .discountAmount(sellerOrder.getDiscountAmount())
            .totalAmount(sellerOrder.getTotalAmount())
            .status(sellerOrder.getStatus())
            .createdAt(sellerOrder.getCreatedAt())
            .items(items)
            .payment(payment)
            .build();
    }
}
