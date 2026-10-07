package com.vegeshop.model;

import com.vegeshop.model.enums.Gender;
import com.vegeshop.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NhanVien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNV")
    private Integer employeeId;

    @Column(name = "TenDangNhap", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "MatKhau", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "VaiTro", nullable = false)
    private Role role;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String fullName;

    @Column(name = "NgaySinh")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "GioiTinh")
    private Gender gender;

    @Column(name = "SDT", length = 15)
    private String phone;

    @Column(name = "DiaChi", length = 255)
    private String address;

    @Column(name = "NgayVaoLam")
    private LocalDate hireDate;

    @Column(name = "TrangThai", nullable = false)
    @Builder.Default
    private Boolean status = true;

    @Column(name = "NgayTao", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
