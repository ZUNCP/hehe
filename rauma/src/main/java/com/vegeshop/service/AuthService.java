package com.vegeshop.service;

import com.vegeshop.dto.LoginRequestDTO;
import com.vegeshop.dto.LoginResponseDTO;
import com.vegeshop.dto.RegisterRequestDTO;

public interface AuthService {
    LoginResponseDTO register(RegisterRequestDTO request);
    LoginResponseDTO login(LoginRequestDTO request);
}
