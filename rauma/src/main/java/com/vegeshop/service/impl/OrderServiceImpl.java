package com.vegeshop.service.impl;

import com.vegeshop.dto.CheckoutRequestDTO;
import com.vegeshop.dto.CounterSaleRequestDTO;
import com.vegeshop.dto.OrderItemResponseDTO;
import com.vegeshop.dto.OrderResponseDTO;
import com.vegeshop.exception.InsufficientStockException;
import com.vegeshop.exception.InvalidStatusTransitionException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.*;
import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.model.enums.OrderType;
import com.vegeshop.model.enums.PaymentMethod;
import com.vegeshop.model.enums.Role;
import com.vegeshop.model.id.OrderItemId;
import com.vegeshop.repository.*;
import com.vegeshop.service.AccountService;
import com.vegeshop.service.OrderService;
import com.vegeshop.service.event.OrderStatusChangedEvent;
import com.vegeshop.service.strategy.IPaymentStrategy;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý toàn bộ nghiệp vụ Đơn hàng (Order Management & POS)
 * Bao gồm:
 * 1. Đặt hàng online (Checkout từ giỏ hàng, áp dụng Payment Strategy)
 * 2. Bán hàng trực tiếp tại quầy (POS: kiểm tra tiền khách đưa, xuất hóa đơn tức thì)
 * 3. Quản lý vòng đời trạng thái đơn hàng (ChoXacNhan -> DangGiao -> HoanThanh hoặc DaHuy)
 * 4. Kiểm tra tồn kho, lưu đơn giá cố định tại thời điểm bán
 * 5. Bắn sự kiện (Observer Pattern) khi trạng thái đơn thay đổi.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final AccountService accountService;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher publisher;
    private final Map<PaymentMethod, IPaymentStrategy> paymentStrategies = new EnumMap<>(PaymentMethod.class);

    /**
     * Khởi tạo Service và nạp động các Strategy thanh toán (COD, Chuyển khoản, Tiền mặt)
     */
    public OrderServiceImpl(SalesOrderRepository salesOrderRepository,
                            OrderItemRepository orderItemRepository,
                            ProductRepository productRepository,
                            CartRepository cartRepository,
                            CartItemRepository cartItemRepository,
                            CustomerRepository customerRepository,
                            AccountService accountService,
                            EntityManager entityManager,
                            ApplicationEventPublisher publisher,
                            List<IPaymentStrategy> strategies) {
        this.salesOrderRepository = salesOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.accountService = accountService;
        this.entityManager = entityManager;
        this.publisher = publisher;
        strategies.forEach(s -> paymentStrategies.put(s.method(), s));
    }

    /**
     * Chức năng: Đặt hàng online (Checkout) dành cho Khách hàng
     * Luồng xử lý nghiệp vụ:
     * 1. Xác định khách hàng hiện tại đang đăng nhập
     * 2. Lấy giỏ hàng và danh sách sản phẩm trong giỏ (nếu rỗng -> báo lỗi)
     * 3. Xác định địa chỉ giao hàng và khởi tạo đơn hàng ở trạng thái 'ChoXacNhan'
     * 4. Kích hoạt Strategy xử lý phương thức thanh toán tương ứng
     * 5. Lưu đơn hàng và các dòng chi tiết đơn hàng (kiểm tra tồn kho từng món)
     * 6. Làm trống giỏ hàng của khách
     * 7. Phát sự kiện OrderStatusChangedEvent để các service liên quan ghi nhận
     * @param request DTO chứa thông tin nhận hàng, phương thức thanh toán, ghi chú
     * @return DTO thông tin đơn hàng vừa tạo
     */
    @Override
    @Transactional
    public OrderResponseDTO checkout(CheckoutRequestDTO request) {
        Customer customer = accountService.currentCustomer();
        Cart cart = cartRepository.findByCustomer_CustomerId(customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng trống"));
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng không có sản phẩm");
        }
        String address = request.getShippingAddress();
        if (address == null || address.isBlank()) {
            address = customer.getAddress();
        }

        // Tạo đơn hàng online với trạng thái ban đầu là Chờ xác nhận
        SalesOrder order = SalesOrder.builder()
                .customer(customer)
                .orderType(OrderType.Online)
                .status(OrderStatus.ChoXacNhan)
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(address)
                .note(request.getNote())
                .build();

        // Xử lý thanh toán theo Strategy Pattern
        strategy(request.getPaymentMethod()).pay(order);
        order = salesOrderRepository.saveAndFlush(order);

        // Lưu chi tiết đơn hàng và kiểm tra tồn kho
        persistItems(order, cartItems.stream()
                .map(i -> new Line(i.getProduct().getProductId(), i.getQuantity()))
                .toList());

        // Làm trống giỏ hàng sau khi đặt thành công
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
        entityManager.refresh(order);

        // Bắn sự kiện thay đổi trạng thái đơn hàng (Observer Pattern)
        publisher.publishEvent(new OrderStatusChangedEvent(this, order, null, order.getStatus()));
        return toDto(order);
    }

    /**
     * Chức năng: Bán hàng trực tiếp tại quầy (POS) dành cho Nhân viên / Quản lý
     * Luồng xử lý nghiệp vụ:
     * 1. Kiểm tra tiền mặt khách đưa (nếu dùng Tiền Mặt, phải lớn hơn 1.000 VNĐ)
     * 2. Lấy thông tin nhân viên đang lập đơn và thông tin khách hàng (nếu có, hoặc khách vãng lai)
     * 3. Khởi tạo đơn hàng hình thức 'TaiQuay', trạng thái hoàn tất ngay 'HoanThanh'
     * 4. Lưu chi tiết sản phẩm bán ra, trừ số lượng tồn kho ngay lập tức
     * 5. Phát sự kiện hoàn tất đơn để tính doanh thu
     * @param request DTO chứa danh sách món, số lượng, tiền khách đưa, PTTT
     * @return DTO đơn hàng hoàn tất tại quầy kèm thông tin chi tiết
     */
    @Override
    @Transactional
    public OrderResponseDTO sellAtCounter(CounterSaleRequestDTO request) {
        // Kiểm tra validation tiền khách đưa tại quầy
        if (request.getPaymentMethod() == PaymentMethod.TienMat && request.getCashReceived() != null) {
            if (request.getCashReceived().compareTo(new java.math.BigDecimal("1000")) <= 0) {
                throw new IllegalArgumentException("Tiền khách đưa phải lớn hơn 1.000 VNĐ (> 1k VNĐ)!");
            }
        }

        Employee employee = accountService.currentEmployee();
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
        }

        // Tạo đơn hàng tại quầy (trạng thái HoanThanh ngay lập tức)
        SalesOrder order = SalesOrder.builder()
                .customer(customer)
                .employee(employee)
                .orderType(OrderType.TaiQuay)
                .status(OrderStatus.HoanThanh)
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .build();

        // Xử lý thanh toán theo Strategy Pattern
        strategy(request.getPaymentMethod()).pay(order);
        order = salesOrderRepository.saveAndFlush(order);

        // Lưu chi tiết sản phẩm và trừ tồn kho
        persistItems(order, request.getItems().stream()
                .map(i -> new Line(i.getProductId(), i.getQuantity()))
                .toList());

        entityManager.refresh(order);
        publisher.publishEvent(new OrderStatusChangedEvent(this, order, null, order.getStatus()));
        return toDto(order);
    }

    /**
     * Chức năng: Khách hàng xem danh sách các đơn hàng của chính mình (Lịch sử mua hàng)
     * @return Danh sách DTO đơn hàng sắp xếp theo mã đơn mới nhất trước
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> myOrders() {
        Customer customer = accountService.currentCustomer();
        return salesOrderRepository.findByCustomer_CustomerIdOrderByOrderIdDesc(customer.getCustomerId())
                .stream().map(this::toDto).toList();
    }

    /**
     * Chức năng: Xem thông tin chi tiết một đơn hàng theo mã ID
     * Kiểm tra phân quyền: Khách hàng chỉ được xem đơn của mình, Nhân viên/Quản lý được xem mọi đơn.
     * @param id Mã đơn hàng
     * @return DTO chi tiết đơn hàng
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Integer id) {
        SalesOrder order = get(id);
        Account account = accountService.currentAccount();
        if (account.getRole() == Role.KhachHang) {
            Customer c = accountService.currentCustomer();
            if (order.getCustomer() == null || !order.getCustomer().getCustomerId().equals(c.getCustomerId())) {
                throw new ResourceNotFoundException("Không tìm thấy đơn hàng");
            }
        }
        return toDto(order);
    }

    /**
     * Chức năng: Tra cứu, tìm kiếm và lọc danh sách đơn hàng dành cho Nhân viên & Quản lý
     * @param status Lọc theo trạng thái đơn (ChoXacNhan, DangGiao, HoanThanh, DaHuy)
     * @param type Lọc theo hình thức (Online hoặc TaiQuay)
     * @param from Thời gian bắt đầu
     * @param to Thời gian kết thúc
     * @return Danh sách DTO các đơn hàng thỏa điều kiện
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> search(OrderStatus status, OrderType type, LocalDateTime from, LocalDateTime to) {
        return salesOrderRepository.search(status, type, from, to).stream().map(this::toDto).toList();
    }

    /**
     * Chức năng: Lấy danh sách các đơn hàng bán tại quầy POS
     * @return Danh sách đơn hàng bán trực tiếp tại quầy
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> counterSales() {
        return salesOrderRepository.findByOrderTypeOrderByOrderIdDesc(OrderType.TaiQuay)
                .stream().map(this::toDto).toList();
    }

    /**
     * Chức năng: Nhân viên xác nhận đơn hàng online
     * Chuyển trạng thái từ ChoXacNhan -> DangGiao
     * @param id Mã đơn hàng
     */
    @Override
    @Transactional
    public OrderResponseDTO confirm(Integer id) {
        return transition(id, OrderStatus.DangGiao);
    }

    /**
     * Chức năng: Nhân viên hoàn tất giao hàng
     * Chuyển trạng thái từ DangGiao -> HoanThanh
     * @param id Mã đơn hàng
     */
    @Override
    @Transactional
    public OrderResponseDTO complete(Integer id) {
        return transition(id, OrderStatus.HoanThanh);
    }

    /**
     * Chức năng: Hủy đơn hàng
     * Quy tắc phân quyền & trạng thái:
     * - Khách hàng: Chỉ có quyền tự hủy đơn khi đơn vẫn ở trạng thái 'ChoXacNhan'
     * - Nhân viên/Quản lý: Có quyền hủy khi đơn ở trạng thái 'ChoXacNhan' hoặc 'DangGiao'
     * - Ghi nhận nhân viên thực hiện hủy (nếu do nhân viên thao tác)
     * - Phát sự kiện để khôi phục lại số lượng tồn kho của các sản phẩm trong đơn
     * @param id Mã đơn hàng cần hủy
     * @return DTO đơn hàng sau khi chuyển trạng thái DaHuy
     */
    @Override
    @Transactional
    public OrderResponseDTO cancel(Integer id) {
        SalesOrder order = get(id);
        Account account = accountService.currentAccount();
        OrderStatus from = order.getStatus();

        if (account.getRole() == Role.KhachHang) {
            Customer c = accountService.currentCustomer();
            if (order.getCustomer() == null || !order.getCustomer().getCustomerId().equals(c.getCustomerId())) {
                throw new ResourceNotFoundException("Không tìm thấy đơn hàng");
            }
            if (from != OrderStatus.ChoXacNhan) {
                throw new InvalidStatusTransitionException("Khách chỉ hủy được đơn đang chờ xác nhận");
            }
        } else if (from != OrderStatus.ChoXacNhan && from != OrderStatus.DangGiao) {
            throw new InvalidStatusTransitionException("Không thể hủy đơn ở trạng thái " + from);
        }

        order.setStatus(OrderStatus.DaHuy);
        if (account.getRole() != Role.KhachHang) {
            order.setEmployee(accountService.currentEmployee());
        }
        order = salesOrderRepository.save(order);

        // Bắn sự kiện hủy đơn (Observer lắng nghe để khôi phục tồn kho)
        publisher.publishEvent(new OrderStatusChangedEvent(this, order, from, OrderStatus.DaHuy));
        return toDto(order);
    }

    /**
     * Hàm phụ trợ kiểm tra tính hợp lệ của việc chuyển đổi trạng thái đơn hàng (State Pattern transition)
     */
    private OrderResponseDTO transition(Integer id, OrderStatus target) {
        SalesOrder order = get(id);
        OrderStatus from = order.getStatus();
        boolean ok = (from == OrderStatus.ChoXacNhan && target == OrderStatus.DangGiao)
                || (from == OrderStatus.DangGiao && target == OrderStatus.HoanThanh);
        if (!ok) {
            throw new InvalidStatusTransitionException("Không thể chuyển " + from + " sang " + target);
        }
        order.setStatus(target);
        order.setEmployee(accountService.currentEmployee());
        order = salesOrderRepository.save(order);
        publisher.publishEvent(new OrderStatusChangedEvent(this, order, from, target));
        return toDto(order);
    }

    /**
     * Hàm phụ trợ: Lưu chi tiết đơn hàng (OrderItem) và kiểm tra tồn kho
     * Đảm bảo đơn giá được cố định tại thời điểm bán
     */
    private void persistItems(SalesOrder order, List<Line> lines) {
        for (Line line : lines) {
            Product product = productRepository.findById(line.productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
            entityManager.refresh(product);
            if (product.getStockQuantity() == null || line.quantity.compareTo(product.getStockQuantity()) > 0) {
                throw new InsufficientStockException("Không đủ tồn kho cho " + product.getProductName());
            }
            OrderItem item = OrderItem.builder()
                    .id(new OrderItemId(order.getOrderId(), product.getProductId()))
                    .order(order)
                    .product(product)
                    .quantity(line.quantity)
                    .unitPrice(product.getPrice()) // Lưu giá bán tại thời điểm lập đơn
                    .build();
            orderItemRepository.saveAndFlush(item);
        }
    }

    /**
     * Hàm phụ trợ: Lấy Strategy thanh toán tương ứng từ Map
     */
    private IPaymentStrategy strategy(PaymentMethod method) {
        IPaymentStrategy s = paymentStrategies.get(method);
        if (s == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ");
        }
        return s;
    }

    /**
     * Hàm phụ trợ: Tìm đơn hàng theo ID hoặc ném lỗi nếu không tồn tại
     */
    private SalesOrder get(Integer id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

    /**
     * Hàm phụ trợ: Chuyển đổi Entity SalesOrder sang OrderResponseDTO kèm danh sách chi tiết món
     */
    private OrderResponseDTO toDto(SalesOrder o) {
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(o.getOrderId());
        return OrderResponseDTO.builder()
                .orderId(o.getOrderId())
                .customerId(o.getCustomer() != null ? o.getCustomer().getCustomerId() : null)
                .customerName(o.getCustomer() != null ? o.getCustomer().getFullName() : "Khách vãng lai")
                .employeeId(o.getEmployee() != null ? o.getEmployee().getEmployeeId() : null)
                .employeeName(o.getEmployee() != null ? o.getEmployee().getFullName() : null)
                .orderDate(o.getOrderDate())
                .orderType(o.getOrderType())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .shippingAddress(o.getShippingAddress())
                .note(o.getNote())
                .totalAmount(o.getTotalAmount())
                .items(items.stream().map(i -> OrderItemResponseDTO.builder()
                        .productId(i.getProduct().getProductId())
                        .productName(i.getProduct().getProductName())
                        .unit(i.getProduct().getUnit())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .lineTotal(i.getUnitPrice().multiply(i.getQuantity()))
                        .build()).toList())
                .build();
    }

    private record Line(Integer productId, BigDecimal quantity) {
    }
}

