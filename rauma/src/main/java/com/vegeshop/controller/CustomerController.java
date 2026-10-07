package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.CustomerRequestDTO;
import com.vegeshop.dto.CustomerResponseDTO;
import com.vegeshop.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các chức năng Quản lý Khách hàng (Customer Management)
 * Dành cho vai trò Quản lý (Admin):
 * 1. Xem danh sách toàn bộ khách hàng
 * 2. Xem chi tiết thông tin một khách hàng
 * 3. Thêm mới khách hàng (tự động tạo giỏ hàng)
 * 4. Cập nhật thông tin hồ sơ khách hàng
 * 5. Xóa khách hàng (có kiểm tra ràng buộc đơn hàng đang mở)
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Khởi tạo CustomerController với CustomerService
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * API: Lấy danh sách toàn bộ khách hàng (Dành cho Quản lý)
     * Endpoint: GET /api/v1/customers
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success("OK", customerService.findAll()));
    }

    /**
     * API: Xem chi tiết thông tin một khách hàng theo ID (Dành cho Quản lý)
     * Endpoint: GET /api/v1/customers/{id}
     * @param id Mã khách hàng (MaKH)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> one(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("OK", customerService.findById(id)));
    }

    /**
     * API: Thêm mới một khách hàng (Dành cho Quản lý)
     * Endpoint: POST /api/v1/customers
     * Hệ thống sẽ tự động tạo giỏ hàng rỗng gắn với khách hàng mới
     * @param request DTO chứa: username, password, họ tên, SĐT, email, địa chỉ
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> create(@Valid @RequestBody CustomerRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm khách hàng thành công", customerService.create(request)));
    }

    /**
     * API: Cập nhật thông tin một khách hàng (Dành cho Quản lý)
     * Endpoint: PUT /api/v1/customers/{id}
     * @param id Mã khách hàng cần cập nhật
     * @param request DTO chứa thông tin mới (họ tên, SĐT, email, địa chỉ)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> update(@PathVariable Integer id,
                                                                   @Valid @RequestBody CustomerRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khách hàng thành công", customerService.update(id, request)));
    }

    /**
     * API: Xóa một khách hàng khỏi hệ thống (Dành cho Quản lý)
     * Endpoint: DELETE /api/v1/customers/{id}
     * Chú ý: Sẽ thất bại nếu khách hàng còn đơn hàng đang ở trạng thái ChoXacNhan hoặc DangGiao
     * @param id Mã khách hàng cần xóa
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công", null));
    }
}

