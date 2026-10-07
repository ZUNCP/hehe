package com.vegeshop.service.strategy;

import com.vegeshop.config.AppConfigManager;
import com.vegeshop.model.SalesOrder;
import com.vegeshop.model.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class BankTransferPaymentStrategy implements IPaymentStrategy {

    private final AppConfigManager appConfigManager;

    public BankTransferPaymentStrategy(AppConfigManager appConfigManager) {
        this.appConfigManager = appConfigManager;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.ChuyenKhoan;
    }

    @Override
    public void pay(SalesOrder order) {
        String info = appConfigManager.getBankTransferInfo();
        String extra = "CK: " + info;
        if (order.getNote() == null || order.getNote().isBlank()) {
            order.setNote(extra);
        } else if (!order.getNote().contains("CK:")) {
            order.setNote(order.getNote() + " | " + extra);
        }
    }
}
