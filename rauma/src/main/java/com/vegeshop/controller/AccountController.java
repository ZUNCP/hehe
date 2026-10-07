package com.vegeshop.controller;

import com.vegeshop.dto.*;
import com.vegeshop.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các chức năng Quản lý Tài khoản (Account Management)
 * Bao gồm:
 * 1. Xem và cập nhật thông tin tài khoản cá nhân (/me, /me/password)
 * 2. Đổi mật khẩu
 * 3. Quản lý danh sách tài khoản, khóa/mở khóa tài khoản (Dành cho Quản lý)
 * 4. Thêm mới và chỉnh sửa tài khoản nhân viên / khách hàng
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    /**
     * Khởi tạo AccountController với AccountService
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * API: Lấy thông tin tài khoản hiện tại của người dùng đang đăng nhập
     * Endpoint: GET /api/v1/accounts/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponseDTO>> me() {
        return ResponseEntity.ok(ApiResponse.success("OK", accountService.getMe()));
    }

    /**
     * API: Cập nhật thông tin hồ sơ của tài khoản hiện tại
     * Endpoint: PUT /api/v1/accounts/me
     * @param request DTO chứa họ tên, SĐT, email, địa chỉ mới
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponseDTO>> updateMe(@Valid @RequestBody AccountProfileRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", accountService.updateMe(request)));
    }

    /**
     * API: Đổi mật khẩu tài khoản
     * Endpoint: PUT /api/v1/accounts/me/password
     * @param request DTO chứa: mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> password(@Valid @RequestBody ChangePasswordRequestDTO request) {
        accountService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }

    /**
     * API: Lấy danh sách toàn bộ tài khoản trong hệ thống (Dành cho Quản lý)
     * Endpoint: GET /api/v1/accounts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponseDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success("OK", accountService.findAll()));
    }

    /**
     * API: Khóa hoặc Mở khóa tài khoản người dùng
     * Endpoint: PUT /api/v1/accounts/{id}/status
     * @param id Mã tài khoản cần cập nhật
     * @param body Map chứa trạng thái mới { "status": true/false }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AccountResponseDTO>> status(@PathVariable Integer id,
                                                                  @RequestBody Map<String, Boolean> body) {
        boolean status = Boolean.TRUE.equals(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", accountService.updateStatus(id, status)));
    }

    /**
     * API: Quản lý tạo mới một tài khoản (Dành cho Quản lý)
     * Endpoint: POST /api/v1/accounts
     * @param request DTO thông tin tài khoản và vai trò
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponseDTO>> createAccount(@Valid @RequestBody AccountRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm tài khoản thành công", accountService.createAccount(request)));
    }

    /**
     * API: Quản lý cập nhật thông tin một tài khoản theo ID
     * Endpoint: PUT /api/v1/accounts/{id}
     * @param id Mã tài khoản
     * @param request DTO dữ liệu cập nhật
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponseDTO>> updateAccount(@PathVariable Integer id, @Valid @RequestBody AccountRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", accountService.updateAccount(id, request)));
    }
}

