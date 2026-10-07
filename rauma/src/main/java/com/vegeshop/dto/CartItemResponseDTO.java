package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponseDTO {
    private Integer productId;
    private String productName;
    private String image;
    private String unit;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal lineTotal;
    private BigDecimal stockQuantity;
}
