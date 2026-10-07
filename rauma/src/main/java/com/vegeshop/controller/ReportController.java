package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.report.BestSellerProjection;
import com.vegeshop.dto.report.DailyRevenueProjection;
import com.vegeshop.dto.report.LowStockProjection;
import com.vegeshop.dto.report.MonthlyRevenueProjection;
import com.vegeshop.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/revenue-daily")
    public ResponseEntity<ApiResponse<List<DailyRevenueProjection>>> revenueDaily() {
        return ResponseEntity.ok(ApiResponse.success("OK", reportService.dailyRevenue()));
    }

    @GetMapping("/revenue-monthly")
    public ResponseEntity<ApiResponse<List<MonthlyRevenueProjection>>> revenueMonthly() {
        return ResponseEntity.ok(ApiResponse.success("OK", reportService.monthlyRevenue()));
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<ApiResponse<List<BestSellerProjection>>> bestSellers() {
        return ResponseEntity.ok(ApiResponse.success("OK", reportService.bestSellers()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockProjection>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.success("OK", reportService.lowStock()));
    }
}
