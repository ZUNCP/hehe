package com.vegeshop.dto;

import com.vegeshop.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccountRequestDTO {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Pattern(regexp = "^[\\p{L}\\p{N}]+$", message = "Tên đăng nhập không được chứa ký tự đặc biệt (chỉ gồm chữ cái và số)")
    private String username;
    
    private String password;
    
    @NotNull(message = "Vai trò không được để trống")
    private Role role;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Boolean status;
}
