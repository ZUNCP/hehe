package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponseDTO {
    private Integer cartId;
    private Integer customerId;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;
}
