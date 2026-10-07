package com.vegeshop.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponseDTO {
    private Integer customerId;
    private Integer accountId;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private Boolean accountStatus;
}
