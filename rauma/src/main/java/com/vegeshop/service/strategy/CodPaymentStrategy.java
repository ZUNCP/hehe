package com.vegeshop.service.strategy;

import com.vegeshop.model.SalesOrder;
import com.vegeshop.model.enums.OrderType;
import com.vegeshop.model.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CodPaymentStrategy implements IPaymentStrategy {
    @Override
    public PaymentMethod method() {
        return PaymentMethod.COD;
    }

    @Override
    public void pay(SalesOrder order) {
        if (order.getOrderType() != OrderType.Online) {
            throw new IllegalArgumentException("COD chỉ áp dụng cho đơn hàng Online");
        }
        if (order.getShippingAddress() == null || order.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("COD bắt buộc có địa chỉ giao hàng");
        }
    }
}
