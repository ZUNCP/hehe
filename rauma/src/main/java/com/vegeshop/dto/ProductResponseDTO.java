package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponseDTO {
    private Integer productId;
    private String productName;
    private Integer categoryId;
    private String categoryName;
    private Integer supplierId;
    private String supplierName;
    private String unit;
    private BigDecimal price;
    private BigDecimal stockQuantity;
    private String description;
    private String image;
    private Boolean status;
}
