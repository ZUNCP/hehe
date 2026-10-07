package com.vegeshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseReceiptRequestDTO {
    @NotNull
    private Integer supplierId;
    @NotEmpty
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull
        private Integer productId;
        @NotNull
        @Positive
        private BigDecimal quantity;
        @NotNull
        @Positive
        private BigDecimal importPrice;
    }
}
