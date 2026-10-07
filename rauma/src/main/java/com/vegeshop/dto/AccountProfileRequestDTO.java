package com.vegeshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountProfileRequestDTO {
    @NotBlank
    private String fullName;
    private String phone;
    private String email;
    private String address;
}
