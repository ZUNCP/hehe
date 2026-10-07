package com.vegeshop.dto;

import com.vegeshop.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private Integer accountId;
    private String username;
    private Role role;
    private String fullName;
    private Integer customerId;
    private Integer employeeId;
}
