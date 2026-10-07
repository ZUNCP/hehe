package com.vegeshop.dto;

import com.vegeshop.model.enums.Gender;
import com.vegeshop.model.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponseDTO {
    private Integer employeeId;
    private Integer accountId;
    private String username;
    private String password;
    private Role role;
    private Boolean accountStatus;
    private String fullName;
    private LocalDate birthDate;
    private Gender gender;
    private String phone;
    private String address;
    private LocalDate hireDate;
}
