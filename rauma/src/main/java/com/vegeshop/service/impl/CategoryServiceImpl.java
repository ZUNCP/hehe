package com.vegeshop.service.impl;

import com.vegeshop.dto.CategoryRequestDTO;
import com.vegeshop.dto.CategoryResponseDTO;
import com.vegeshop.exception.ConflictException;
import com.vegeshop.exception.DuplicateResourceException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Category;
import com.vegeshop.repository.CategoryRepository;
import com.vegeshop.repository.ProductRepository;
import com.vegeshop.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new DuplicateResourceException("Tên loại sản phẩm đã tồn tại");
        }
        Category c = Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();
        return toDto(categoryRepository.save(c));
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Integer id, CategoryRequestDTO request) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sản phẩm"));
        c.setCategoryName(request.getCategoryName());
        c.setDescription(request.getDescription());
        return toDto(categoryRepository.save(c));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sản phẩm"));
        if (productRepository.existsByCategory_CategoryId(id)) {
            throw new ConflictException("Không thể xóa loại sản phẩm còn hàng hóa liên quan");
        }
        categoryRepository.delete(c);
    }

    private CategoryResponseDTO toDto(Category c) {
        return CategoryResponseDTO.builder()
                .categoryId(c.getCategoryId())
                .categoryName(c.getCategoryName())
                .description(c.getDescription())
                .build();
    }
}
