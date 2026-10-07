package com.vegeshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/categories").hasRole("QuanLy")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**", "/api/v1/categories/**").hasRole("QuanLy")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**", "/api/v1/categories/**").hasRole("QuanLy")
                        .requestMatchers("/api/v1/cart/**").hasRole("KhachHang")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders").hasAnyRole("NhanVien", "QuanLy")
                        .requestMatchers("/api/v1/orders/*/confirm", "/api/v1/orders/*/complete").hasAnyRole("NhanVien", "QuanLy")
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders/checkout").hasRole("KhachHang")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders/my").hasRole("KhachHang")
                        .requestMatchers("/api/v1/sales/**").hasAnyRole("NhanVien", "QuanLy")
                        .requestMatchers("/api/v1/reports/**").hasRole("QuanLy")
                        .requestMatchers("/api/v1/employees/**").hasRole("QuanLy")
                        .requestMatchers("/api/v1/customers/**").hasRole("QuanLy")
                        .requestMatchers("/api/v1/suppliers/**").hasRole("QuanLy")
                        .requestMatchers("/api/v1/purchase-receipts/**").hasRole("QuanLy")
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts").hasRole("QuanLy")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/accounts/*/status").hasRole("QuanLy")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
