package com.smartshop.modules.order.dto;

import com.smartshop.modules.order.entity.Order;
import com.smartshop.modules.order.entity.OrderStatus;
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
public class OrderResponse {

    private Long id;
    private String orderCode;
    private String recipientName;
    private String recipientPhone;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingAddress;
    private String customerNote;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<SellerOrderResponse> sellerOrders;

    public static OrderResponse of(Order order, List<SellerOrderResponse> sellerOrders) {
        return OrderResponse.builder()
            .id(order.getId())
            .orderCode(order.getOrderCode())
            .recipientName(order.getRecipientName())
            .recipientPhone(order.getRecipientPhone())
            .shippingProvince(order.getShippingProvince())
            .shippingDistrict(order.getShippingDistrict())
            .shippingWard(order.getShippingWard())
            .shippingAddress(order.getShippingAddress())
            .customerNote(order.getCustomerNote())
            .subtotal(order.getSubtotal())
            .shippingFee(order.getShippingFee())
            .discountAmount(order.getDiscountAmount())
            .totalAmount(order.getTotalAmount())
            .orderStatus(order.getOrderStatus())
            .createdAt(order.getCreatedAt())
            .sellerOrders(sellerOrders)
            .build();
    }
}
