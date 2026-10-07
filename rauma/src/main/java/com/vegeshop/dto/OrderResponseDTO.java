package com.vegeshop.dto;

import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.model.enums.OrderType;
import com.vegeshop.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private Integer orderId;
    private Integer customerId;
    private String customerName;
    private Integer employeeId;
    private String employeeName;
    private LocalDateTime orderDate;
    private OrderType orderType;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String note;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDTO> items;
}
