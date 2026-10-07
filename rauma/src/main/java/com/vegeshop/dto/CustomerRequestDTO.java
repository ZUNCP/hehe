package com.vegeshop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRequestDTO {
    @Pattern(regexp = "^([\\p{L}\\p{N}]+)?$", message = "Tên đăng nhập không được chứa ký tự đặc biệt (chỉ gồm chữ cái và số)")
    private String username;
    private String password;
    @NotBlank(message = "Họ tên không được trống")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ và tên không được chứa số hoặc ký tự đặc biệt")
    private String fullName;
    @Pattern(regexp = "^(0[0-9]{9})?$", message = "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0")
    private String phone;
    @Pattern(regexp = "^([a-zA-Z0-9._%+-]+@gmail\\.com)?$", message = "Email phải đúng định dạng @gmail.com")
    private String email;
    private String address;
}
