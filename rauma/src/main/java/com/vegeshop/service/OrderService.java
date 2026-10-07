package com.vegeshop.service;

import com.vegeshop.dto.CheckoutRequestDTO;
import com.vegeshop.dto.CounterSaleRequestDTO;
import com.vegeshop.dto.OrderResponseDTO;
import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.model.enums.OrderType;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponseDTO checkout(CheckoutRequestDTO request);
    OrderResponseDTO sellAtCounter(CounterSaleRequestDTO request);
    List<OrderResponseDTO> myOrders();
    OrderResponseDTO findById(Integer id);
    List<OrderResponseDTO> search(OrderStatus status, OrderType type, LocalDateTime from, LocalDateTime to);
    List<OrderResponseDTO> counterSales();
    OrderResponseDTO confirm(Integer id);
    OrderResponseDTO complete(Integer id);
    OrderResponseDTO cancel(Integer id);
}
