package com.vegeshop.repository;

import com.vegeshop.model.Account;
import com.vegeshop.model.Customer;
import com.vegeshop.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    public AccountRepository(EmployeeRepository employeeRepository, CustomerRepository customerRepository) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<Account> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        Optional<Employee> employee = employeeRepository.findByUsername(username);
        if (employee.isPresent()) {
            return employee.map(Account::fromEmployee);
        }
        return customerRepository.findByUsername(username).map(Account::fromCustomer);
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return employeeRepository.existsByUsername(username) || customerRepository.existsByUsername(username);
    }

    public Optional<Account> findByAccountId(Integer accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        if (Account.isCustomerAccount(accountId)) {
            return customerRepository.findById(Account.nativeId(accountId)).map(Account::fromCustomer);
        }
        return employeeRepository.findById(accountId).map(Account::fromEmployee);
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        for (Employee employee : employeeRepository.findAll()) {
            accounts.add(Account.fromEmployee(employee));
        }
        for (Customer customer : customerRepository.findAll()) {
            if (customer.getUsername() != null && !customer.getUsername().isBlank()) {
                accounts.add(Account.fromCustomer(customer));
            }
        }
        return accounts;
    }
}
