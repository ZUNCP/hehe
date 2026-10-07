package com.vegeshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SupplierRequestDTO {
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$", message = "Tên nhà cung cấp không được chứa ký tự đặc biệt")
    private String supplierName;
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0")
    private String phone;
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message = "Email phải đúng định dạng @gmail.com")
    private String email;
    private String address;
}
