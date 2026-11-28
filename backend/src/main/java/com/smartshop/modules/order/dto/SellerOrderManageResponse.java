package com.smartshop.modules.order.dto;

import com.smartshop.modules.order.entity.Order;
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
public class SellerOrderManageResponse {

    private Long id;
    private Long orderId;
    private String orderCode;
    private Long storeId;
    private String storeName;
    private String sellerOrderCode;
    private SellerOrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String recipientName;
    private String recipientPhone;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingAddress;
    private String customerNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;

    public static SellerOrderManageResponse of(
        SellerOrder sellerOrder,
        List<OrderItemResponse> items,
        PaymentResponse payment
    ) {
        Order parent = sellerOrder.getOrder();
        return SellerOrderManageResponse.builder()
            .id(sellerOrder.getId())
            .orderId(parent.getId())
            .orderCode(parent.getOrderCode())
            .storeId(sellerOrder.getStore().getId())
            .storeName(sellerOrder.getStore().getStoreName())
            .sellerOrderCode(sellerOrder.getSellerOrderCode())
            .status(sellerOrder.getStatus())
            .subtotal(sellerOrder.getSubtotal())
            .shippingFee(sellerOrder.getShippingFee())
            .discountAmount(sellerOrder.getDiscountAmount())
            .totalAmount(sellerOrder.getTotalAmount())
            .recipientName(parent.getRecipientName())
            .recipientPhone(parent.getRecipientPhone())
            .shippingProvince(parent.getShippingProvince())
            .shippingDistrict(parent.getShippingDistrict())
            .shippingWard(parent.getShippingWard())
            .shippingAddress(parent.getShippingAddress())
            .customerNote(parent.getCustomerNote())
            .createdAt(sellerOrder.getCreatedAt())
            .updatedAt(sellerOrder.getUpdatedAt())
            .items(items)
            .payment(payment)
            .build();
    }
}
