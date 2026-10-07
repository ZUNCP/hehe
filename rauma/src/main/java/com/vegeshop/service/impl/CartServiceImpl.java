package com.vegeshop.service.impl;

import com.vegeshop.dto.CartItemRequestDTO;
import com.vegeshop.dto.CartItemResponseDTO;
import com.vegeshop.dto.CartResponseDTO;
import com.vegeshop.exception.InsufficientStockException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Cart;
import com.vegeshop.model.CartItem;
import com.vegeshop.model.Customer;
import com.vegeshop.model.Product;
import com.vegeshop.model.id.CartItemId;
import com.vegeshop.repository.CartItemRepository;
import com.vegeshop.repository.CartRepository;
import com.vegeshop.repository.ProductRepository;
import com.vegeshop.service.AccountService;
import com.vegeshop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service xử lý các chức năng Giỏ hàng (Shopping Cart) dành cho Khách hàng
 * Bao gồm:
 * 1. Xem giỏ hàng hiện tại (kèm chi tiết các sản phẩm, tổng tiền)
 * 2. Thêm sản phẩm vào giỏ (cộng dồn số lượng, kiểm tra tồn kho & trạng thái bán)
 * 3. Cập nhật số lượng sản phẩm trong giỏ
 * 4. Xóa sản phẩm khỏi giỏ hàng
 * 5. Làm trống toàn bộ giỏ hàng (sau khi Checkout thành công).
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AccountService accountService;

    /**
     * Khởi tạo CartServiceImpl với các repository cần thiết
     */
    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           AccountService accountService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.accountService = accountService;
    }

    /**
     * Chức năng: Lấy thông tin giỏ hàng của khách hàng đang đăng nhập
     * @return DTO giỏ hàng bao gồm danh sách món và tổng số tiền
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart() {
        return toDto(myCart());
    }

    /**
     * Chức năng: Thêm một sản phẩm vào giỏ hàng
     * Luồng xử lý nghiệp vụ:
     * 1. Tìm sản phẩm theo ID và kiểm tra nếu sản phẩm đã bị ngừng bán (Status = false)
     * 2. Nếu món đã có trong giỏ -> Cộng dồn số lượng thêm mới vào số lượng cũ
     * 3. Kiểm tra số lượng yêu cầu có vượt quá số lượng tồn kho hay không
     * 4. Lưu hoặc cập nhật bản ghi ChiTietGioHang (CartItem)
     * @param request DTO chứa mã sản phẩm và số lượng cần thêm
     * @return DTO giỏ hàng cập nhật mới nhất
     */
    @Override
    @Transactional
    public CartResponseDTO addItem(CartItemRequestDTO request) {
        Cart cart = myCart();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // Kiểm tra trạng thái kinh doanh của sản phẩm
        if (!Boolean.TRUE.equals(product.getStatus())) {
            throw new IllegalArgumentException("Sản phẩm đã ngừng bán");
        }

        CartItemId id = new CartItemId(cart.getCartId(), product.getProductId());
        CartItem item = cartItemRepository.findById(id).orElse(null);
        BigDecimal qty = request.getQuantity();

        // Nếu sản phẩm đã có trong giỏ hàng thì cộng dồn số lượng
        if (item != null) {
            qty = item.getQuantity().add(request.getQuantity());
        }

        // Kiểm tra tồn kho khả dụng
        if (product.getStockQuantity() != null && qty.compareTo(product.getStockQuantity()) > 0) {
            throw new InsufficientStockException("Không đủ tồn kho cho " + product.getProductName());
        }

        if (item == null) {
            item = CartItem.builder()
                    .id(id)
                    .cart(cart)
                    .product(product)
                    .quantity(qty)
                    .build();
        } else {
            item.setQuantity(qty);
        }

        cartItemRepository.save(item);
        return toDto(myCart());
    }

    /**
     * Chức năng: Cập nhật số lượng của một sản phẩm trong giỏ hàng
     * @param productId Mã sản phẩm cần điều chỉnh số lượng
     * @param request DTO chứa số lượng mới
     * @return DTO giỏ hàng sau khi điều chỉnh
     */
    @Override
    @Transactional
    public CartResponseDTO updateItem(Integer productId, CartItemRequestDTO request) {
        Cart cart = myCart();
        CartItem item = cartItemRepository.findById(new CartItemId(cart.getCartId(), productId))
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong giỏ"));

        // Kiểm tra số lượng tồn kho khả dụng
        if (item.getProduct().getStockQuantity() != null
                && request.getQuantity().compareTo(item.getProduct().getStockQuantity()) > 0) {
            throw new InsufficientStockException("Không đủ tồn kho");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toDto(myCart());
    }

    /**
     * Chức năng: Xóa một sản phẩm ra khỏi giỏ hàng
     * @param productId Mã sản phẩm cần xóa
     * @return DTO giỏ hàng sau khi đã xóa sản phẩm
     */
    @Override
    @Transactional
    public CartResponseDTO removeItem(Integer productId) {
        Cart cart = myCart();
        cartItemRepository.deleteById(new CartItemId(cart.getCartId(), productId));
        return toDto(myCart());
    }

    /**
     * Chức năng: Làm sạch (xóa toàn bộ sản phẩm) trong giỏ hàng
     */
    @Override
    @Transactional
    public void clear() {
        cartItemRepository.deleteByCart_CartId(myCart().getCartId());
    }

    /**
     * Hàm phụ trợ: Lấy giỏ hàng của khách hàng hiện tại, nếu chưa có thì tự động khởi tạo mới
     */
    private Cart myCart() {
        Customer customer = accountService.currentCustomer();
        return cartRepository.findByCustomer_CustomerId(customer.getCustomerId())
                .orElseGet(() -> cartRepository.save(Cart.builder().customer(customer).build()));
    }

    /**
     * Hàm phụ trợ: Chuyển đổi Entity Cart sang CartResponseDTO
     * Tính toán tự động thành tiền từng dòng (đơn giá x số lượng) và tổng tiền của toàn bộ giỏ hàng
     */
    private CartResponseDTO toDto(Cart cart) {
        var items = cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                .map(i -> CartItemResponseDTO.builder()
                        .productId(i.getProduct().getProductId())
                        .productName(i.getProduct().getProductName())
                        .image(i.getProduct().getImage())
                        .unit(i.getProduct().getUnit())
                        .price(i.getProduct().getPrice())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getProduct().getPrice().multiply(i.getQuantity()))
                        .stockQuantity(i.getProduct().getStockQuantity())
                        .build())
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponseDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomer().getCustomerId())
                .items(items)
                .totalAmount(total)
                .build();
    }
}

