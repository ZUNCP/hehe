package com.vegeshop.model;

import com.vegeshop.model.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tài khoản đăng nhập thống nhất (không còn bảng TaiKhoan).
 * Nhân viên lấy từ NhanVien, khách hàng lấy từ KhachHang.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    public static final int CUSTOMER_OFFSET = 100_000;

    private Integer accountId;
    private String username;
    private String password;
    private Role role;
    private Boolean status;
    private LocalDateTime createdAt;
    private Integer customerId;
    private Integer employeeId;

    public static int encode(Role role, Integer nativeId) {
        if (nativeId == null) {
            return 0;
        }
        return role == Role.KhachHang ? nativeId + CUSTOMER_OFFSET : nativeId;
    }

    public static int nativeId(Integer accountId) {
        if (accountId == null) {
            return 0;
        }
        return accountId >= CUSTOMER_OFFSET ? accountId - CUSTOMER_OFFSET : accountId;
    }

    public static boolean isCustomerAccount(Integer accountId) {
        return accountId != null && accountId >= CUSTOMER_OFFSET;
    }

    public static Account fromEmployee(Employee e) {
        return Account.builder()
                .accountId(encode(e.getRole(), e.getEmployeeId()))
                .username(e.getUsername())
                .password(e.getPassword())
                .role(e.getRole())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .employeeId(e.getEmployeeId())
                .build();
    }

    public static Account fromCustomer(Customer c) {
        return Account.builder()
                .accountId(encode(Role.KhachHang, c.getCustomerId()))
                .username(c.getUsername())
                .password(c.getPassword())
                .role(Role.KhachHang)
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .customerId(c.getCustomerId())
                .build();
    }
}
