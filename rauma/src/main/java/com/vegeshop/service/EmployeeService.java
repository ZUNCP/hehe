package com.vegeshop.service;

import com.vegeshop.dto.EmployeeRequestDTO;
import com.vegeshop.dto.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponseDTO> findAll();
    EmployeeResponseDTO findById(Integer id);
    EmployeeResponseDTO create(EmployeeRequestDTO request);
    EmployeeResponseDTO update(Integer id, EmployeeRequestDTO request);
    void delete(Integer id, Integer currentAccountId);
}
