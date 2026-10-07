package com.vegeshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "KhachHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaKH")
    private Integer customerId;

    @Column(name = "TenDangNhap", unique = true, length = 50)
    private String username;

    @Column(name = "MatKhau")
    private String password;

    @Column(name = "HoTen", nullable = false, length = 100)
    private String fullName;

    @Column(name = "SDT", length = 15)
    private String phone;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "DiaChi", length = 255)
    private String address;

    @Column(name = "TrangThai", nullable = false)
    @Builder.Default
    private Boolean status = true;

    @Column(name = "NgayTao", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
