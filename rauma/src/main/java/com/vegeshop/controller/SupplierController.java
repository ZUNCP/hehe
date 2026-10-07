package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.SupplierRequestDTO;
import com.vegeshop.dto.SupplierResponseDTO;
import com.vegeshop.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponseDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success("OK", supplierService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> one(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("OK", supplierService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> create(@Valid @RequestBody SupplierRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm nhà cung cấp thành công", supplierService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> update(@PathVariable Integer id,
                                                                   @Valid @RequestBody SupplierRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhà cung cấp thành công", supplierService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa nhà cung cấp", null));
    }
}
