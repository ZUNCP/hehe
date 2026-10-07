package com.vegeshop.service;

import com.vegeshop.dto.PageResponseDTO;
import com.vegeshop.dto.ProductRequestDTO;
import com.vegeshop.dto.ProductResponseDTO;

public interface ProductService {
    PageResponseDTO<ProductResponseDTO> search(String keyword, Integer categoryId, int page, int size, Boolean onlyActive);
    ProductResponseDTO findById(Integer id);
    ProductResponseDTO create(ProductRequestDTO request);
    ProductResponseDTO update(Integer id, ProductRequestDTO request);
    void deleteSoft(Integer id);
}
