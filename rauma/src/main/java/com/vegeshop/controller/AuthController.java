package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.LoginRequestDTO;
import com.vegeshop.dto.LoginResponseDTO;
import com.vegeshop.dto.RegisterRequestDTO;
import com.vegeshop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các yêu cầu Xác thực & Phân quyền (Authentication & Authorization)
 * Cung cấp API công khai cho người dùng:
 * 1. Đăng ký tài khoản khách hàng mới
 * 2. Đăng nhập hệ thống (trả về JWT token và thông tin vai trò)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Khởi tạo AuthController với AuthService
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * API: Đăng ký tài khoản Khách hàng mới
     * Endpoint: POST /api/v1/auth/register
     * @param request DTO chứa thông tin: username, password, họ tên, SĐT, email, địa chỉ
     * @return Thông tin người dùng cùng JWT token sau khi đăng ký thành công
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công", authService.register(request)));
    }

    /**
     * API: Đăng nhập vào hệ thống
     * Endpoint: POST /api/v1/auth/login
     * Dùng chung cho cả 3 vai trò: Khách hàng, Nhân viên, Quản lý
     * @param request DTO chứa tên đăng nhập và mật khẩu
     * @return Token JWT và thông tin hồ sơ của người dùng
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authService.login(request)));
    }
}

