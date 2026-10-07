package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.EmployeeRequestDTO;
import com.vegeshop.dto.EmployeeResponseDTO;
import com.vegeshop.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các chức năng Quản lý Nhân viên (Employee Management)
 * Dành cho vai trò Quản lý (Admin):
 * 1. Xem danh sách toàn bộ nhân viên
 * 2. Xem chi tiết thông tin một nhân viên
 * 3. Thêm nhân viên mới (cấp tài khoản, phân vai trò NhanVien/QuanLy)
 * 4. Cập nhật thông tin hồ sơ nhân viên
 * 5. Xóa nhân viên (có kiểm tra không tự xóa tài khoản đang dùng)
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Khởi tạo EmployeeController với EmployeeService
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * API: Lấy danh sách toàn bộ nhân viên (Dành cho Quản lý)
     * Endpoint: GET /api/v1/employees
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success("OK", employeeService.findAll()));
    }

    /**
     * API: Xem chi tiết thông tin một nhân viên theo ID (Dành cho Quản lý)
     * Endpoint: GET /api/v1/employees/{id}
     * @param id Mã nhân viên (MaNV)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> one(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("OK", employeeService.findById(id)));
    }

    /**
     * API: Thêm mới một nhân viên (Dành cho Quản lý)
     * Endpoint: POST /api/v1/employees
     * @param request DTO chứa: username, password, họ tên, ngày sinh, giới tính, SĐT, địa chỉ, vai trò
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> create(@Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm nhân viên thành công", employeeService.create(request)));
    }

    /**
     * API: Cập nhật thông tin một nhân viên (Dành cho Quản lý)
     * Endpoint: PUT /api/v1/employees/{id}
     * @param id Mã nhân viên cần cập nhật
     * @param request DTO chứa thông tin mới
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> update(@PathVariable Integer id,
                                                                   @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công", employeeService.update(id, request)));
    }

    /**
     * API: Xóa một nhân viên khỏi hệ thống (Dành cho Quản lý)
     * Endpoint: DELETE /api/v1/employees/{id}
     * Chú ý: Kiểm tra không cho phép tự xóa tài khoản của chính Quản lý đang đăng nhập
     * @param id Mã nhân viên cần xóa
     * @param currentAccountId ID tài khoản đang thao tác (để kiểm tra tự xóa)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id,
                                                    @RequestParam(required = false) Integer currentAccountId) {
        employeeService.delete(id, currentAccountId);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
    }
}

