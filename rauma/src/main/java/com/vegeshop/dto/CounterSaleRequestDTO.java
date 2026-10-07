package com.vegeshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.vegeshop.model.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CounterSaleRequestDTO {
    private Integer customerId;
    @NotNull
    private PaymentMethod paymentMethod;
    private String note;
    private BigDecimal cashReceived;
    @NotEmpty
    @Valid
    private List<CartItemRequestDTO> items;
}

