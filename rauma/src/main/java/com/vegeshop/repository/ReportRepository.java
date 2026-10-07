package com.vegeshop.repository;

import com.vegeshop.dto.report.BestSellerProjection;
import com.vegeshop.dto.report.DailyRevenueProjection;
import com.vegeshop.dto.report.LowStockProjection;
import com.vegeshop.dto.report.MonthlyRevenueProjection;
import com.vegeshop.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<SalesOrder, Integer> {

    @Query(value = "SELECT Ngay AS ngay, SoDon AS soDon, DoanhThu AS doanhThu FROM v_doanh_thu_ngay ORDER BY Ngay DESC", nativeQuery = true)
    List<DailyRevenueProjection> findDailyRevenue();

    @Query(value = "SELECT Nam AS nam, Thang AS thang, DoanhThu AS doanhThu FROM v_doanh_thu_thang ORDER BY Nam DESC, Thang DESC", nativeQuery = true)
    List<MonthlyRevenueProjection> findMonthlyRevenue();

    @Query(value = "SELECT MaSP AS productId, TenSP AS productName, TongBan AS tongBan, DoanhThu AS doanhThu FROM v_sp_ban_chay", nativeQuery = true)
    List<BestSellerProjection> findBestSellers();

    @Query(value = "SELECT MaSP AS productId, TenSP AS productName, SoLuongTon AS stockQuantity FROM v_ton_kho_thap", nativeQuery = true)
    List<LowStockProjection> findLowStock();
}
