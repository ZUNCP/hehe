package com.vegeshop.service.impl;

import com.vegeshop.dto.AccountProfileRequestDTO;
import com.vegeshop.dto.AccountRequestDTO;
import com.vegeshop.dto.AccountResponseDTO;
import com.vegeshop.dto.ChangePasswordRequestDTO;
import com.vegeshop.exception.DuplicateResourceException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Account;
import com.vegeshop.model.Cart;
import com.vegeshop.model.Customer;
import com.vegeshop.model.Employee;
import com.vegeshop.model.enums.Role;
import com.vegeshop.repository.AccountRepository;
import com.vegeshop.repository.CartRepository;
import com.vegeshop.repository.CustomerRepository;
import com.vegeshop.repository.EmployeeRepository;
import com.vegeshop.service.AccountService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              EmployeeRepository employeeRepository,
                              CartRepository cartRepository,
                              PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Account currentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            throw new ResourceNotFoundException("Chưa đăng nhập");
        }
        return accountRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    @Override
    public Customer currentCustomer() {
        Account account = currentAccount();
        if (account.getCustomerId() == null) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng");
        }
        return customerRepository.findById(account.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng"));
    }

    @Override
    public Employee currentEmployee() {
        Account account = currentAccount();
        if (account.getEmployeeId() == null) {
            throw new ResourceNotFoundException("Không tìm thấy hồ sơ nhân viên");
        }
        return employeeRepository.findById(account.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhân viên"));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getMe() {
        return toDto(currentAccount());
    }

    @Override
    @Transactional
    public AccountResponseDTO updateMe(AccountProfileRequestDTO request) {
        Account account = currentAccount();
        if (account.getRole() == Role.KhachHang) {
            Customer c = currentCustomer();
            c.setFullName(request.getFullName());
            c.setPhone(request.getPhone());
            c.setEmail(request.getEmail());
            c.setAddress(request.getAddress());
            customerRepository.save(c);
        } else {
            Employee e = currentEmployee();
            e.setFullName(request.getFullName());
            e.setPhone(request.getPhone());
            e.setAddress(request.getAddress());
            employeeRepository.save(e);
        }
        return toDto(currentAccount());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        Account account = currentAccount();
        String stored = account.getPassword();
        boolean ok = stored != null && (stored.equals(request.getOldPassword())
                || (stored.startsWith("$2") && passwordEncoder.matches(request.getOldPassword(), stored)));
        if (!ok) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }
        applyPassword(account.getAccountId(), request.getNewPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findAll() {
        return accountRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public AccountResponseDTO updateStatus(Integer id, boolean status) {
        Account account = accountRepository.findByAccountId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (Account.isCustomerAccount(id)) {
            Customer c = customerRepository.findById(Account.nativeId(id))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
            c.setStatus(status);
            customerRepository.save(c);
        } else {
            Employee e = employeeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
            e.setStatus(status);
            employeeRepository.save(e);
        }
        account.setStatus(status);
        return toDto(account);
    }

    @Override
    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống khi thêm mới");
        }
        String hashed = passwordEncoder.encode(request.getPassword());
        if (request.getRole() == Role.KhachHang) {
            Customer customer = Customer.builder()
                    .username(request.getUsername())
                    .password(hashed)
                    .fullName(request.getUsername())
                    .status(request.getStatus() == null || request.getStatus())
                    .build();
            customer = customerRepository.save(customer);
            cartRepository.save(Cart.builder().customer(customer).build());
            return toDto(Account.fromCustomer(customer));
        }
        Role staffRole = request.getRole() == Role.QuanLy ? Role.QuanLy : Role.NhanVien;
        Employee employee = Employee.builder()
                .username(request.getUsername())
                .password(hashed)
                .role(staffRole)
                .fullName(request.getUsername())
                .hireDate(LocalDate.now())
                .status(request.getStatus() == null || request.getStatus())
                .build();
        employee = employeeRepository.save(employee);
        return toDto(Account.fromEmployee(employee));
    }

    @Override
    @Transactional
    public AccountResponseDTO updateAccount(Integer id, AccountRequestDTO request) {
        Account account = accountRepository.findByAccountId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (request.getUsername() != null && !request.getUsername().equals(account.getUsername())
                && accountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }
        if (Account.isCustomerAccount(id)) {
            Customer c = customerRepository.findById(Account.nativeId(id))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
            if (request.getUsername() != null) {
                c.setUsername(request.getUsername());
            }
            if (request.getStatus() != null) {
                c.setStatus(request.getStatus());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                c.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            return toDto(Account.fromCustomer(customerRepository.save(c)));
        }
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (request.getUsername() != null) {
            e.setUsername(request.getUsername());
        }
        if (request.getStatus() != null) {
            e.setStatus(request.getStatus());
        }
        if (request.getRole() == Role.QuanLy || request.getRole() == Role.NhanVien) {
            e.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            e.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toDto(Account.fromEmployee(employeeRepository.save(e)));
    }

    private void applyPassword(Integer accountId, String raw) {
        String hashed = passwordEncoder.encode(raw);
        if (Account.isCustomerAccount(accountId)) {
            Customer c = customerRepository.findById(Account.nativeId(accountId))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
            c.setPassword(hashed);
            customerRepository.save(c);
            return;
        }
        Employee e = employeeRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        e.setPassword(hashed);
        employeeRepository.save(e);
    }

    private AccountResponseDTO toDto(Account account) {
        String fullName = account.getUsername();
        if (account.getCustomerId() != null) {
            fullName = customerRepository.findById(account.getCustomerId())
                    .map(Customer::getFullName).orElse(fullName);
        } else if (account.getEmployeeId() != null) {
            fullName = employeeRepository.findById(account.getEmployeeId())
                    .map(Employee::getFullName).orElse(fullName);
        }
        return AccountResponseDTO.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .role(account.getRole())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .fullName(fullName)
                .build();
    }
}
