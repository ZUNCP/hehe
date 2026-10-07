package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseReceiptResponseDTO {
    private Integer receiptId;
    private Integer supplierId;
    private String supplierName;
    private Integer employeeId;
    private String employeeName;
    private LocalDateTime receiptDate;
    private BigDecimal totalAmount;
    private List<PurchaseReceiptItemResponseDTO> items;
}
