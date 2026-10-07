package com.vegeshop.service;

import com.vegeshop.dto.SupplierRequestDTO;
import com.vegeshop.dto.SupplierResponseDTO;

import java.util.List;

public interface SupplierService {
    List<SupplierResponseDTO> findAll();
    SupplierResponseDTO findById(Integer id);
    SupplierResponseDTO create(SupplierRequestDTO request);
    SupplierResponseDTO update(Integer id, SupplierRequestDTO request);
    void delete(Integer id);
}
