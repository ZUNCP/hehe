package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.CounterSaleRequestDTO;
import com.vegeshop.dto.OrderResponseDTO;
import com.vegeshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    private final OrderService orderService;

    public SalesController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> sellAtCounter(@Valid @RequestBody CounterSaleRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Bán hàng tại quầy thành công", orderService.sellAtCounter(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> counterSales() {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.counterSales()));
    }
}
