package com.vegeshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemRequestDTO {
    @NotNull
    private Integer productId;
    @NotNull
    @Positive
    private BigDecimal quantity;
}
