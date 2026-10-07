package com.vegeshop.service.strategy;

import com.vegeshop.model.SalesOrder;
import com.vegeshop.model.enums.PaymentMethod;

public interface IPaymentStrategy {
    PaymentMethod method();
    void pay(SalesOrder order);
}
