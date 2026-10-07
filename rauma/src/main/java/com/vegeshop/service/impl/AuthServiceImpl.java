package com.vegeshop.service.impl;

import com.vegeshop.dto.LoginRequestDTO;
import com.vegeshop.dto.LoginResponseDTO;
import com.vegeshop.dto.RegisterRequestDTO;
import com.vegeshop.exception.DuplicateResourceException;
import com.vegeshop.model.Account;
import com.vegeshop.model.Cart;
import com.vegeshop.model.Customer;
import com.vegeshop.model.Employee;
import com.vegeshop.repository.AccountRepository;
import com.vegeshop.repository.CartRepository;
import com.vegeshop.repository.CustomerRepository;
import com.vegeshop.repository.EmployeeRepository;
import com.vegeshop.service.AuthService;
import com.vegeshop.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AccountRepository accountRepository,
                           CustomerRepository customerRepository,
                           EmployeeRepository employeeRepository,
                           CartRepository cartRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }
        Customer customer = Customer.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(true)
                .build();
        customer = customerRepository.save(customer);
        cartRepository.save(Cart.builder().customer(customer).build());
        return toLogin(Account.fromCustomer(customer), customer.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu"));
        if (!Boolean.TRUE.equals(account.getStatus())) {
            throw new BadCredentialsException("Tài khoản đã bị khóa");
        }
        if (!passwordMatches(request.getPassword(), account.getPassword(), request.getUsername())) {
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");
        }
        String fullName = account.getUsername();
        if (account.getCustomerId() != null) {
            fullName = customerRepository.findById(account.getCustomerId())
                    .map(Customer::getFullName).orElse(fullName);
        } else if (account.getEmployeeId() != null) {
            fullName = employeeRepository.findById(account.getEmployeeId())
                    .map(Employee::getFullName).orElse(fullName);
        }
        return toLogin(account, fullName);
    }

    private boolean passwordMatches(String raw, String stored, String username) {
        if (stored == null) {
            return false;
        }
        if (stored.equals(raw) || stored.equals("hash_" + username)) {
            return true;
        }
        if (stored.startsWith("$2")) {
            return passwordEncoder.matches(raw, stored);
        }
        return false;
    }

    private LoginResponseDTO toLogin(Account account, String fullName) {
        return LoginResponseDTO.builder()
                .token(jwtUtil.generateToken(account.getUsername(), account.getRole(), account.getAccountId()))
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .role(account.getRole())
                .fullName(fullName)
                .customerId(account.getCustomerId())
                .employeeId(account.getEmployeeId())
                .build();
    }
}
