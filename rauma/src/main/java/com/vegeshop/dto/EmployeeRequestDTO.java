package com.vegeshop.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vegeshop.model.enums.Gender;
import com.vegeshop.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeRequestDTO {
    @Pattern(regexp = "^([\\p{L}\\p{N}]+)?$", message = "Tên đăng nhập không được chứa ký tự đặc biệt (chỉ gồm chữ cái và số)")
    private String username;
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;
    @JsonAlias("hoTen")
    @NotBlank(message = "Họ tên không được trống")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ và tên không được chứa số hoặc ký tự đặc biệt")
    private String fullName;
    @JsonAlias("ngaySinh")
    private LocalDate birthDate;
    @JsonAlias("gioiTinh")
    private Gender gender;
    @JsonAlias("sdt")
    @Pattern(regexp = "^(0[0-9]{9})?$", message = "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0")
    private String phone;
    @JsonAlias("diaChi")
    private String address;
    @JsonAlias("ngayVaoLam")
    private LocalDate hireDate;
    private Role role;
}
