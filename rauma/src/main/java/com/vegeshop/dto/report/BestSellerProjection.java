package com.vegeshop.dto.report;

import java.math.BigDecimal;

public interface BestSellerProjection {
    Integer getProductId();
    String getProductName();
    BigDecimal getTongBan();
    BigDecimal getDoanhThu();
}
