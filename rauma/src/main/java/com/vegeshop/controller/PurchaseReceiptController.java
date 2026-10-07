package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.PurchaseReceiptRequestDTO;
import com.vegeshop.dto.PurchaseReceiptResponseDTO;
import com.vegeshop.service.PurchaseReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các chức năng Nhập kho & Phiếu nhập hàng (Purchase Receipt Management)
 * Dành cho vai trò Quản lý (Admin):
 * 1. Xem lịch sử tất cả phiếu nhập kho (sắp xếp mới nhất trước)
 * 2. Xem chi tiết một phiếu nhập cụ thể
 * 3. Lập phiếu nhập mới từ nhà cung cấp
 *    → Hệ thống tự động cộng tồn kho qua trigger MySQL (trg_nhap_hang)
 */
@RestController
@RequestMapping("/api/v1/purchase-receipts")
public class PurchaseReceiptController {

    private final PurchaseReceiptService purchaseReceiptService;

    /**
     * Khởi tạo PurchaseReceiptController với PurchaseReceiptService
     */
    public PurchaseReceiptController(PurchaseReceiptService purchaseReceiptService) {
        this.purchaseReceiptService = purchaseReceiptService;
    }

    /**
     * API: Lấy danh sách toàn bộ phiếu nhập kho (Dành cho Quản lý)
     * Endpoint: GET /api/v1/purchase-receipts
     * @return Danh sách phiếu nhập, sắp xếp theo mã phiếu mới nhất trước
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseReceiptResponseDTO>>> all() {
        return ResponseEntity.ok(ApiResponse.success("OK", purchaseReceiptService.findAll()));
    }

    /**
     * API: Xem chi tiết một phiếu nhập kho theo ID (Dành cho Quản lý)
     * Endpoint: GET /api/v1/purchase-receipts/{id}
     * @param id Mã phiếu nhập (MaPN)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseReceiptResponseDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("OK", purchaseReceiptService.findById(id)));
    }

    /**
     * API: Lập phiếu nhập kho mới từ Nhà cung cấp (Dành cho Quản lý)
     * Endpoint: POST /api/v1/purchase-receipts
     * Luồng: Chọn NCC → Nhập danh sách SP + Số lượng + Giá nhập → Lưu PhieuNhap
     *        → Trigger MySQL tự động cộng SoLuongTon và tính TongTien
     * @param request DTO chứa mã nhà cung cấp và danh sách sản phẩm nhập
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseReceiptResponseDTO>> create(@Valid @RequestBody PurchaseReceiptRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Lập phiếu nhập hàng thành công", purchaseReceiptService.create(request)));
    }
}

