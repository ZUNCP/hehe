package com.vegeshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$", message = "Tên sản phẩm không được chứa ký tự đặc biệt")
    private String productName;
    @NotNull
    private Integer categoryId;
    private Integer supplierId;
    private String unit;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    private String description;
    private String image;
    private Boolean status;
}
