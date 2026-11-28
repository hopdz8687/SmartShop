package com.smartshop.modules.order.dto;

import com.smartshop.modules.order.entity.Payment;
import com.smartshop.modules.order.entity.PaymentMethod;
import com.smartshop.modules.order.entity.PaymentStatus;
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
public class PaymentResponse {

    private Long id;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .paymentMethod(payment.getPaymentMethod())
            .amount(payment.getAmount())
            .paymentStatus(payment.getPaymentStatus())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}
