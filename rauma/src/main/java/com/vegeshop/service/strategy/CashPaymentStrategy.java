package com.vegeshop.service.strategy;

import com.vegeshop.model.SalesOrder;
import com.vegeshop.model.enums.OrderType;
import com.vegeshop.model.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class CashPaymentStrategy implements IPaymentStrategy {
    @Override
    public PaymentMethod method() {
        return PaymentMethod.TienMat;
    }

    @Override
    public void pay(SalesOrder order) {
        if (order.getOrderType() != OrderType.TaiQuay) {
            throw new IllegalArgumentException("Thanh toán tiền mặt chỉ dùng cho bán tại quầy");
        }
    }
}
