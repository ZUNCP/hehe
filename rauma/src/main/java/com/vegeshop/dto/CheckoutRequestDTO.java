package com.vegeshop.dto;

import com.vegeshop.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequestDTO {
    @NotNull
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String note;
}
