package com.vegeshop.service;

import com.vegeshop.dto.AccountProfileRequestDTO;
import com.vegeshop.dto.AccountResponseDTO;
import com.vegeshop.dto.ChangePasswordRequestDTO;
import com.vegeshop.model.Account;
import com.vegeshop.model.Customer;
import com.vegeshop.model.Employee;

import java.util.List;

public interface AccountService {
    Account currentAccount();
    Customer currentCustomer();
    Employee currentEmployee();
    AccountResponseDTO getMe();
    AccountResponseDTO updateMe(AccountProfileRequestDTO request);
    void changePassword(ChangePasswordRequestDTO request);
    List<AccountResponseDTO> findAll();
    AccountResponseDTO updateStatus(Integer id, boolean status);
    AccountResponseDTO createAccount(com.vegeshop.dto.AccountRequestDTO request);
    AccountResponseDTO updateAccount(Integer id, com.vegeshop.dto.AccountRequestDTO request);
}
