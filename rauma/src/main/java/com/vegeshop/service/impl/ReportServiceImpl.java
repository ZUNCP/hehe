package com.vegeshop.service.impl;

import com.vegeshop.dto.report.BestSellerProjection;
import com.vegeshop.dto.report.DailyRevenueProjection;
import com.vegeshop.dto.report.LowStockProjection;
import com.vegeshop.dto.report.MonthlyRevenueProjection;
import com.vegeshop.repository.ReportRepository;
import com.vegeshop.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các chức năng Báo cáo & Thống kê (Reporting & Analytics)
 * Dành cho Quản lý (Admin / Store Manager)
 * Truy vấn dữ liệu tổng hợp trực tiếp từ các SQL View trong MySQL:
 * - v_doanh_thu_ngay: Doanh thu và số lượng đơn hàng theo từng ngày
 * - v_doanh_thu_thang: Doanh thu tổng hợp theo từng tháng
 * - v_sp_ban_chay: Top sản phẩm bán chạy nhất theo số lượng và doanh thu
 * - v_ton_kho_thap: Cảnh báo danh sách sản phẩm sắp hết hàng trong kho (< 10)
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    /**
     * Khởi tạo ReportServiceImpl với ReportRepository
     */
    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Chức năng: Thống kê doanh thu theo từng ngày
     * Chỉ tính các đơn hàng có trạng thái 'HoanThanh'
     * @return Danh sách báo cáo doanh thu theo ngày
     */
    @Override
    @Transactional(readOnly = true)
    public List<DailyRevenueProjection> dailyRevenue() {
        return reportRepository.findDailyRevenue();
    }

    /**
     * Chức năng: Thống kê doanh thu theo từng tháng
     * Chỉ tính các đơn hàng có trạng thái 'HoanThanh'
     * @return Danh sách báo cáo doanh thu theo tháng
     */
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueProjection> monthlyRevenue() {
        return reportRepository.findMonthlyRevenue();
    }

    /**
     * Chức năng: Thống kê các sản phẩm bán chạy nhất
     * Sắp xếp theo tổng số lượng bán ra giảm dần
     * @return Danh sách top sản phẩm bán chạy
     */
    @Override
    @Transactional(readOnly = true)
    public List<BestSellerProjection> bestSellers() {
        return reportRepository.findBestSellers();
    }

    /**
     * Chức năng: Cảnh báo danh sách các sản phẩm có lượng tồn kho thấp
     * Lấy các sản phẩm có số lượng tồn kho <= ngưỡng quy định (ví dụ: < 10 kg/gói)
     * để quản lý kịp thời lên kế hoạch nhập hàng mới.
     * @return Danh sách sản phẩm tồn kho thấp
     */
    @Override
    @Transactional(readOnly = true)
    public List<LowStockProjection> lowStock() {
        return reportRepository.findLowStock();
    }
}

