package com.vegeshop.config;

import com.vegeshop.model.Customer;
import com.vegeshop.model.Employee;
import com.vegeshop.repository.CustomerRepository;
import com.vegeshop.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (Employee employee : employeeRepository.findAll()) {
            String raw = employee.getPassword();
            if (raw != null && !raw.startsWith("$2")) {
                employee.setPassword(passwordEncoder.encode(raw));
                employeeRepository.save(employee);
            }
        }
        for (Customer customer : customerRepository.findAll()) {
            String raw = customer.getPassword();
            if (raw != null && !raw.isBlank() && !raw.startsWith("$2")) {
                customer.setPassword(passwordEncoder.encode(raw));
                customerRepository.save(customer);
            }
        }
    }
}
