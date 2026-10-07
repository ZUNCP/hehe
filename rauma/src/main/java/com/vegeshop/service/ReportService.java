package com.vegeshop.service;

import com.vegeshop.dto.report.BestSellerProjection;
import com.vegeshop.dto.report.DailyRevenueProjection;
import com.vegeshop.dto.report.LowStockProjection;
import com.vegeshop.dto.report.MonthlyRevenueProjection;

import java.util.List;

public interface ReportService {
    List<DailyRevenueProjection> dailyRevenue();
    List<MonthlyRevenueProjection> monthlyRevenue();
    List<BestSellerProjection> bestSellers();
    List<LowStockProjection> lowStock();
}
