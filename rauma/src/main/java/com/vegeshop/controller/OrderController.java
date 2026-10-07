package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.CheckoutRequestDTO;
import com.vegeshop.dto.OrderResponseDTO;
import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.model.enums.OrderType;
import com.vegeshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller xử lý các chức năng Quản lý Đơn hàng Online (Order Management)
 * Bao gồm:
 * 1. Đặt hàng online từ giỏ hàng (Checkout) - dành cho Khách hàng
 * 2. Xem lịch sử đơn hàng cá nhân - dành cho Khách hàng
 * 3. Xem chi tiết một đơn hàng - Khách xem đơn mình, NV/QL xem mọi đơn
 * 4. Tìm kiếm và lọc đơn hàng theo trạng thái, hình thức, khoảng thời gian - NV/QL
 * 5. Xác nhận, giao hàng, hoàn tất và hủy đơn hàng - NV/QL
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Khởi tạo OrderController với OrderService
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * API: Đặt hàng Online (Checkout từ giỏ hàng) - dành cho Khách hàng
     * Endpoint: POST /api/v1/orders/checkout
     * Luồng: Lấy giỏ hàng hiện tại → Tạo đơn Online trạng thái ChoXacNhan → Xóa giỏ hàng
     * @param request DTO chứa địa chỉ giao hàng, phương thức thanh toán, ghi chú
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> checkout(@Valid @RequestBody CheckoutRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công", orderService.checkout(request)));
    }

    /**
     * API: Xem lịch sử đơn hàng của bản thân - dành cho Khách hàng
     * Endpoint: GET /api/v1/orders/my
     * @return Danh sách đơn hàng sắp xếp theo mã mới nhất trước
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> myOrders() {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.myOrders()));
    }

    /**
     * API: Xem chi tiết một đơn hàng theo ID
     * Endpoint: GET /api/v1/orders/{id}
     * Kiểm tra phân quyền: Khách hàng chỉ xem được đơn của mình
     * @param id Mã đơn hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.findById(id)));
    }

    /**
     * API: Tìm kiếm và lọc đơn hàng (Dành cho Nhân viên & Quản lý)
     * Endpoint: GET /api/v1/orders
     * @param status Lọc theo trạng thái: ChoXacNhan, DangGiao, HoanThanh, DaHuy
     * @param type Lọc theo hình thức: Online, TaiQuay
     * @param from Thời gian bắt đầu (ISO 8601)
     * @param to Thời gian kết thúc (ISO 8601)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success("OK", orderService.search(status, type, from, to)));
    }

    /**
     * API: Nhân viên xác nhận đơn hàng và bắt đầu giao
     * Endpoint: PUT /api/v1/orders/{id}/confirm
     * Chuyển trạng thái: ChoXacNhan → DangGiao
     * @param id Mã đơn hàng cần xác nhận
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> confirm(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Xác nhận đơn hàng thành công", orderService.confirm(id)));
    }

    /**
     * API: Nhân viên xác nhận giao hàng thành công
     * Endpoint: PUT /api/v1/orders/{id}/complete
     * Chuyển trạng thái: DangGiao → HoanThanh (ghi nhận doanh thu)
     * @param id Mã đơn hàng cần hoàn tất
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> complete(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Hoàn thành đơn hàng thành công", orderService.complete(id)));
    }

    /**
     * API: Hủy đơn hàng
     * Endpoint: PUT /api/v1/orders/{id}/cancel
     * Quy tắc: Khách chỉ hủy được đơn ChoXacNhan; NV/QL hủy được ChoXacNhan và DangGiao
     * @param id Mã đơn hàng cần hủy
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", orderService.cancel(id)));
    }
}

