package com.vegeshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Tên đăng nhập không được trống")
    private String username;
    @NotBlank(message = "Mật khẩu không được trống")
    private String password;
}
