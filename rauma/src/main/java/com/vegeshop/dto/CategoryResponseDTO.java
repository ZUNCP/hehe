package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDTO {
    private Integer categoryId;
    private String categoryName;
    private String description;
}
