package com.vegeshop.dto.report;

import java.math.BigDecimal;

public interface LowStockProjection {
    Integer getProductId();
    String getProductName();
    BigDecimal getStockQuantity();
}
