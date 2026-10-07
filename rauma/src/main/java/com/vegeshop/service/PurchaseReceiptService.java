package com.vegeshop.service;

import com.vegeshop.dto.PurchaseReceiptRequestDTO;
import com.vegeshop.dto.PurchaseReceiptResponseDTO;

import java.util.List;

public interface PurchaseReceiptService {
    List<PurchaseReceiptResponseDTO> findAll();
    PurchaseReceiptResponseDTO findById(Integer id);
    PurchaseReceiptResponseDTO create(PurchaseReceiptRequestDTO request);
}
