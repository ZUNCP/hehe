package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.CategoryRequestDTO;
import com.vegeshop.dto.CategoryResponseDTO;
import com.vegeshop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các chức năng Quản lý Loại sản phẩm / Danh mục (Category Management)
 * Bao gồm:
 * 1. Lấy toàn bộ danh sách loại sản phẩm (Rau ăn lá, Củ, Quả, Nấm...)
 * 2. Thêm loại sản phẩm mới (Dành cho Quản lý)
 * 3. Cập nhật tên / mô tả loại sản phẩm
 * 4. Xóa loại sản phẩm (kiểm tra ràng buộc khóa ngoại trước khi xóa)
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Khởi tạo CategoryController với CategoryService
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * API: Lấy danh sách tất cả loại sản phẩm
     * Endpoint: GET /api/v1/categories
     * Dùng chung cho cả Khách hàng (hiển thị menu), Nhân viên, và Quản lý
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success("OK", categoryService.findAll()));
    }

    /**
     * API: Thêm một loại sản phẩm mới (Dành cho Quản lý)
     * Endpoint: POST /api/v1/categories
     * @param request DTO chứa tên loại và mô tả
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> create(@Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm loại sản phẩm thành công", categoryService.create(request)));
    }

    /**
     * API: Cập nhật thông tin loại sản phẩm (Dành cho Quản lý)
     * Endpoint: PUT /api/v1/categories/{id}
     * @param id Mã loại sản phẩm cần sửa
     * @param request DTO chứa thông tin mới
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> update(@PathVariable Integer id,
                                                                   @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật loại sản phẩm thành công", categoryService.update(id, request)));
    }

    /**
     * API: Xóa loại sản phẩm (Dành cho Quản lý)
     * Endpoint: DELETE /api/v1/categories/{id}
     * Chú ý: Sẽ thất bại nếu vẫn còn sản phẩm thuộc loại này (ràng buộc khóa ngoại FK)
     * @param id Mã loại sản phẩm cần xóa
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa loại sản phẩm", null));
    }
}

