package com.vegeshop.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueProjection {
    LocalDate getNgay();
    Long getSoDon();
    BigDecimal getDoanhThu();
}
