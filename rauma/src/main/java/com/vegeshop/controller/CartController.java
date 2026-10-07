package com.vegeshop.controller;

import com.vegeshop.dto.ApiResponse;
import com.vegeshop.dto.CartItemRequestDTO;
import com.vegeshop.dto.CartResponseDTO;
import com.vegeshop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các chức năng Giỏ hàng (Cart) dành cho Khách hàng
 * Bao gồm:
 * 1. Xem giỏ hàng hiện tại (kèm danh sách món và tổng tiền)
 * 2. Thêm món vào giỏ
 * 3. Điều chỉnh số lượng món trong giỏ
 * 4. Xóa món khỏi giỏ
 * 5. Làm trống giỏ hàng
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    /**
     * Khởi tạo CartController với CartService
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * API: Lấy thông tin giỏ hàng của người dùng hiện tại
     * Endpoint: GET /api/v1/cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart() {
        return ResponseEntity.ok(ApiResponse.success("OK", cartService.getMyCart()));
    }

    /**
     * API: Thêm một sản phẩm vào giỏ hàng
     * Endpoint: POST /api/v1/cart/items
     * @param request DTO chứa mã sản phẩm (productId) và số lượng (quantity)
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponseDTO>> addItem(@Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", cartService.addItem(request)));
    }

    /**
     * API: Cập nhật số lượng của một sản phẩm trong giỏ hàng
     * Endpoint: PUT /api/v1/cart/items/{productId}
     * @param productId Mã sản phẩm cần sửa
     * @param request DTO chứa số lượng mới
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> updateItem(@PathVariable Integer productId,
                                                                   @Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật giỏ hàng", cartService.updateItem(productId, request)));
    }

    /**
     * API: Xóa một sản phẩm ra khỏi giỏ hàng
     * Endpoint: DELETE /api/v1/cart/items/{productId}
     * @param productId Mã sản phẩm cần xóa
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponseDTO>> removeItem(@PathVariable Integer productId) {
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", cartService.removeItem(productId)));
    }

    /**
     * API: Xóa sạch toàn bộ sản phẩm trong giỏ hàng
     * Endpoint: DELETE /api/v1/cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clear();
        return ResponseEntity.ok(ApiResponse.success("Đã làm trống giỏ hàng", null));
    }
}

