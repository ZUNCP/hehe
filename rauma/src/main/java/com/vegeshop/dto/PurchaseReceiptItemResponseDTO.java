package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PurchaseReceiptItemResponseDTO {
    private Integer productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal importPrice;
    private BigDecimal lineTotal;
}
