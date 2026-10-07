package com.vegeshop.dto;

import com.vegeshop.model.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponseDTO {
    private Integer accountId;
    private String username;
    private Role role;
    private Boolean status;
    private LocalDateTime createdAt;
    private String fullName;
}
