package com.smartshop.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class CheckoutRequest {

    @NotEmpty(message = "At least one cart item must be selected")
    private List<Long> cartItemIds;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 150, message = "Recipient name must not exceed 150 characters")
    private String recipientName;

    @NotBlank(message = "Recipient phone is required")
    @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
    private String recipientPhone;

    @NotBlank(message = "Shipping province is required")
    @Size(max = 100, message = "Shipping province must not exceed 100 characters")
    private String shippingProvince;

    @Size(max = 100, message = "Shipping district must not exceed 100 characters")
    private String shippingDistrict;

    @NotBlank(message = "Shipping ward is required")
    @Size(max = 100, message = "Shipping ward must not exceed 100 characters")
    private String shippingWard;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;

    @Size(max = 1000, message = "Customer note must not exceed 1000 characters")
    private String customerNote;
}
