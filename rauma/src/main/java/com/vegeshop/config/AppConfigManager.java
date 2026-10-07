package com.vegeshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AppConfigManager {

    public static final int LOW_STOCK_THRESHOLD_DEFAULT = 10;

    @Value("${app.low-stock-threshold:10}")
    private int lowStockThreshold;

    @Value("${app.bank-transfer-info:}")
    private String bankTransferInfo;

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public BigDecimal getLowStockThresholdDecimal() {
        return BigDecimal.valueOf(lowStockThreshold);
    }

    public String getBankTransferInfo() {
        return bankTransferInfo;
    }
}
