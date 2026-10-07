package com.vegeshop.service.impl;

import com.vegeshop.dto.CustomerRequestDTO;
import com.vegeshop.dto.CustomerResponseDTO;
import com.vegeshop.exception.ConflictException;
import com.vegeshop.exception.DuplicateResourceException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Cart;
import com.vegeshop.model.Customer;
import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.repository.AccountRepository;
import com.vegeshop.repository.CartRepository;
import com.vegeshop.repository.CustomerRepository;
import com.vegeshop.repository.SalesOrderRepository;
import com.vegeshop.service.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các nghiệp vụ Quản lý Khách hàng (Customer Management)
 * Bao gồm: Tra cứu danh sách, chi tiết khách hàng, thêm mới khách hàng (tự động tạo giỏ hàng),
 * cập nhật hồ sơ thông tin và xóa khách hàng có kiểm tra ràng buộc đơn hàng.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CartRepository cartRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Khởi tạo Service với các dependency cần thiết (Dependency Injection qua Constructor)
     */
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AccountRepository accountRepository,
                               CartRepository cartRepository,
                               SalesOrderRepository salesOrderRepository,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cartRepository = cartRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Chức năng: Lấy danh sách toàn bộ khách hàng trong hệ thống
     * @return Danh sách DTO thông tin khách hàng
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> findAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Chức năng: Tra cứu thông tin chi tiết một khách hàng theo mã ID
     * @param id Mã khách hàng (MaKH)
     * @return DTO thông tin chi tiết khách hàng
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(Integer id) {
        return toDto(get(id));
    }

    /**
     * Chức năng: Thêm mới một khách hàng
     * Luồng xử lý nghiệp vụ:
     * 1. Kiểm tra tính hợp lệ của tên đăng nhập (không rỗng, không trùng trong hệ thống)
     * 2. Mã hóa mật khẩu (mặc định là '123456' nếu không nhập)
     * 3. Lưu thông tin khách hàng vào bảng KhachHang
     * 4. Tự động khởi tạo giỏ hàng (Cart) gắn liền với khách hàng mới
     * @param request DTO chứa dữ liệu thêm mới khách hàng
     * @return DTO khách hàng sau khi lưu thành công
     */
    @Override
    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO request) {
        // 1. Kiểm tra tên đăng nhập
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        if (accountRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại");
        }

        // 2. Thiết lập mật khẩu mặc định và mã hóa BCrypt
        String raw = (request.getPassword() == null || request.getPassword().isBlank()) ? "123456" : request.getPassword();
        Customer customer = Customer.builder()
                .username(username)
                .password(passwordEncoder.encode(raw))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(true)
                .build();

        // 3. Lưu khách hàng vào CSDL
        customer = customerRepository.save(customer);

        // 4. Tạo giỏ hàng rỗng cho khách hàng mới tạo
        cartRepository.save(Cart.builder().customer(customer).build());

        return toDto(customer);
    }

    /**
     * Chức năng: Cập nhật thông tin hồ sơ của khách hàng
     * Luồng xử lý nghiệp vụ:
     * 1. Tìm bản ghi khách hàng theo ID
     * 2. Cập nhật các trường: họ tên, số điện thoại, email, địa chỉ
     * 3. Nếu có truyền mật khẩu mới -> mã hóa BCrypt và cập nhật
     * @param id Mã khách hàng cần sửa
     * @param request DTO dữ liệu cập nhật
     * @return DTO khách hàng sau khi cập nhật
     */
    @Override
    @Transactional
    public CustomerResponseDTO update(Integer id, CustomerRequestDTO request) {
        Customer c = get(id);
        c.setFullName(request.getFullName());
        c.setPhone(request.getPhone());
        c.setEmail(request.getEmail());
        c.setAddress(request.getAddress());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            c.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toDto(customerRepository.save(c));
    }

    /**
     * Chức năng: Xóa khách hàng khỏi hệ thống
     * Quy tắc toàn vẹn dữ liệu:
     * 1. Kiểm tra xem khách hàng có đơn hàng nào đang trong quá trình xử lý (ChoXacNhan, DangGiao) hay không
     * 2. Nếu có đơn hàng chưa hoàn tất -> Chặn xóa và ném ngoại lệ ConflictException
     * 3. Nếu thỏa điều kiện -> Xóa giỏ hàng liên kết rồi mới xóa khách hàng
     * @param id Mã khách hàng cần xóa
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        Customer c = get(id);

        // Kiểm tra ràng buộc đơn hàng chưa hoàn tất
        long activeOrders = salesOrderRepository.findByCustomer_CustomerIdOrderByOrderIdDesc(id).stream()
                .filter(o -> o.getStatus() != OrderStatus.HoanThanh && o.getStatus() != OrderStatus.DaHuy)
                .count();
        if (activeOrders > 0) {
            throw new ConflictException("Không thể xóa khách hàng đang có đơn hàng chưa hoàn tất");
        }

        // Xóa giỏ hàng trước, sau đó xóa khách hàng
        cartRepository.findByCustomer_CustomerId(id).ifPresent(cartRepository::delete);
        customerRepository.delete(c);
    }

    /**
     * Hàm phụ trợ (Private Helper): Lấy đối tượng Entity Customer theo ID, ném lỗi 404 nếu không tìm thấy
     */
    private Customer get(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));
    }

    /**
     * Hàm phụ trợ (Private Helper): Chuyển đổi Entity Customer sang CustomerResponseDTO
     * Giúp ẩn mật khẩu đã băm BCrypt và định dạng mã định danh tài khoản phù hợp với Frontend
     */
    private CustomerResponseDTO toDto(Customer c) {
        String displayPassword = c.getPassword() != null && !c.getPassword().startsWith("$2")
                ? c.getPassword() : null;
        return CustomerResponseDTO.builder()
                .customerId(c.getCustomerId())
                .accountId(c.getCustomerId() == null ? null : com.vegeshop.model.Account.encode(
                        com.vegeshop.model.enums.Role.KhachHang, c.getCustomerId()))
                .username(c.getUsername())
                .password(displayPassword)
                .fullName(c.getFullName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .accountStatus(c.getStatus())
                .build();
    }
}

