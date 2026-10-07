package com.vegeshop.service.impl;

import com.vegeshop.dto.EmployeeRequestDTO;
import com.vegeshop.dto.EmployeeResponseDTO;
import com.vegeshop.exception.ConflictException;
import com.vegeshop.exception.DuplicateResourceException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Employee;
import com.vegeshop.model.enums.Role;
import com.vegeshop.repository.AccountRepository;
import com.vegeshop.repository.EmployeeRepository;
import com.vegeshop.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               AccountRepository accountRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> findAll() {
        return employeeRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO findById(Integer id) {
        return toDto(get(id));
    }

    @Override
    @Transactional
    public EmployeeResponseDTO create(EmployeeRequestDTO request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (accountRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }
        String rawPassword = (request.getPassword() == null || request.getPassword().isBlank())
                ? "123456" : request.getPassword();
        Employee employee = Employee.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(staffRole(request.getRole()))
                .fullName(request.getFullName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .phone(request.getPhone())
                .address(request.getAddress())
                .hireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now())
                .status(true)
                .build();
        return toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDTO update(Integer id, EmployeeRequestDTO request) {
        Employee e = get(id);
        e.setFullName(request.getFullName());
        e.setBirthDate(request.getBirthDate());
        e.setGender(request.getGender());
        e.setPhone(request.getPhone());
        e.setAddress(request.getAddress());
        if (request.getHireDate() != null) {
            e.setHireDate(request.getHireDate());
        }
        if (request.getRole() != null) {
            e.setRole(staffRole(request.getRole()));
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(e.getUsername())) {
            if (accountRepository.existsByUsername(request.getUsername().trim())) {
                throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
            }
            e.setUsername(request.getUsername().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            e.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toDto(employeeRepository.save(e));
    }

    @Override
    @Transactional
    public void delete(Integer id, Integer currentAccountId) {
        Employee e = get(id);
        Integer actorId = resolveActorEmployeeId(currentAccountId);
        if (actorId != null && actorId.equals(e.getEmployeeId())) {
            throw new ConflictException("Admin không thể tự xóa tài khoản của chính mình");
        }
        employeeRepository.delete(e);
    }

    private Integer resolveActorEmployeeId(Integer currentAccountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return employeeRepository.findByUsername(auth.getName())
                    .map(Employee::getEmployeeId)
                    .orElse(currentAccountId);
        }
        return currentAccountId;
    }

    private Role staffRole(Role role) {
        return role == Role.QuanLy ? Role.QuanLy : Role.NhanVien;
    }

    private Employee get(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));
    }

    private EmployeeResponseDTO toDto(Employee e) {
        String displayPassword = e.getPassword() != null && !e.getPassword().startsWith("$2")
                ? e.getPassword() : "******";
        return EmployeeResponseDTO.builder()
                .employeeId(e.getEmployeeId())
                .accountId(e.getEmployeeId())
                .username(e.getUsername())
                .password(displayPassword)
                .role(e.getRole())
                .accountStatus(e.getStatus())
                .fullName(e.getFullName())
                .birthDate(e.getBirthDate())
                .gender(e.getGender())
                .phone(e.getPhone())
                .address(e.getAddress())
                .hireDate(e.getHireDate())
                .build();
    }
}
