package com.vegeshop.service;

import com.vegeshop.dto.CategoryRequestDTO;
import com.vegeshop.dto.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> findAll();
    CategoryResponseDTO create(CategoryRequestDTO request);
    CategoryResponseDTO update(Integer id, CategoryRequestDTO request);
    void delete(Integer id);
}
