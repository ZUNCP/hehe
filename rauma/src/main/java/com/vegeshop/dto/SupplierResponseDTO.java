package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponseDTO {
    private Integer supplierId;
    private String supplierName;
    private String phone;
    private String email;
    private String address;
}
