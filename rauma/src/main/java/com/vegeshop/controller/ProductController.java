package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.PageResponseDTO;
import com.vegeshop.dto.ProductRequestDTO;
import com.vegeshop.dto.ProductResponseDTO;
import com.vegeshop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các chức năng Quản lý Sản phẩm (Product Management)
 * Dùng chung cho tất cả các vai trò, phân quyền theo từng hành động:
 * 1. Tìm kiếm sản phẩm có phân trang (Khách hàng, Nhân viên, Quản lý)
 * 2. Xem chi tiết sản phẩm (Tất cả vai trò)
 * 3. Thêm sản phẩm mới (Dành cho Quản lý)
 * 4. Cập nhật sản phẩm: tên, giá, danh mục, NCC, ảnh (Dành cho Quản lý)
 * 5. Xóa mềm sản phẩm: chuyển trạng thái Ngừng bán (Dành cho Quản lý)
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    /**
     * Khởi tạo ProductController với ProductService
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * API: Tìm kiếm và lọc sản phẩm có phân trang
     * Endpoint: GET /api/v1/products
     * @param keyword Từ khóa tìm theo tên sản phẩm (không phân biệt hoa thường)
     * @param categoryId Lọc theo mã loại sản phẩm
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số sản phẩm mỗi trang (mặc định 12)
     * @param onlyActive true = chỉ lấy sản phẩm đang kinh doanh
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<ProductResponseDTO>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Boolean onlyActive) {
        PageResponseDTO<ProductResponseDTO> result = productService.search(keyword, categoryId, page, size, onlyActive);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }

    /**
     * API: Xem chi tiết thông tin một sản phẩm theo ID
     * Endpoint: GET /api/v1/products/{id}
     * @param id Mã sản phẩm (MaSP)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("OK", productService.findById(id)));
    }

    /**
     * API: Thêm mới sản phẩm vào danh mục (Dành cho Quản lý)
     * Endpoint: POST /api/v1/products
     * @param request DTO chứa: tên SP, loại SP, NCC, đơn vị, giá bán, mô tả, hình ảnh
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> create(@Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm thành công", productService.create(request)));
    }

    /**
     * API: Cập nhật thông tin sản phẩm (Dành cho Quản lý)
     * Endpoint: PUT /api/v1/products/{id}
     * @param id Mã sản phẩm cần cập nhật
     * @param request DTO chứa thông tin mới
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> update(@PathVariable Integer id,
                                                                 @Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", productService.update(id, request)));
    }

    /**
     * API: Ngừng bán sản phẩm - Xóa mềm (Dành cho Quản lý)
     * Endpoint: DELETE /api/v1/products/{id}
     * Thực tế: Chuyển TrangThai = false (không xóa cứng khỏi DB để bảo toàn lịch sử đơn hàng)
     * @param id Mã sản phẩm cần ẩn/ngừng bán
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        productService.deleteSoft(id);
        return ResponseEntity.ok(ApiResponse.success("Ngừng bán sản phẩm thành công", null));
    }
}

