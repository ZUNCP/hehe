package com.vegeshop.service;

import com.vegeshop.dto.CustomerRequestDTO;
import com.vegeshop.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {
    List<CustomerResponseDTO> findAll();
    CustomerResponseDTO findById(Integer id);
    CustomerResponseDTO create(CustomerRequestDTO request);
    CustomerResponseDTO update(Integer id, CustomerRequestDTO request);
    void delete(Integer id);
}
