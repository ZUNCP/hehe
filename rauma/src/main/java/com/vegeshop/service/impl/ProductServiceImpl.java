package com.vegeshop.service.impl;

import com.vegeshop.dto.PageResponseDTO;
import com.vegeshop.dto.ProductRequestDTO;
import com.vegeshop.dto.ProductResponseDTO;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Category;
import com.vegeshop.model.Product;
import com.vegeshop.model.Supplier;
import com.vegeshop.repository.CategoryRepository;
import com.vegeshop.repository.ProductRepository;
import com.vegeshop.repository.SupplierRepository;
import com.vegeshop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý toàn bộ các nghiệp vụ Sản phẩm (Product Management)
 * Bao gồm:
 * 1. Tìm kiếm và phân trang sản phẩm theo từ khóa, danh mục, trạng thái
 * 2. Xem chi tiết thông tin sản phẩm và số lượng tồn kho
 * 3. Thêm mới sản phẩm (gắn với Loại sản phẩm và Nhà cung cấp)
 * 4. Cập nhật thông tin, giá bán, hình ảnh sản phẩm
 * 5. Xóa mềm (Soft Delete - đổi trạng thái ngưng bán) để đảm bảo toàn vẹn dữ liệu đơn hàng cũ.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    /**
     * Khởi tạo ProductServiceImpl với các repository cần thiết
     */
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Chức năng: Tìm kiếm và lọc sản phẩm có phân trang (Pagination & Filter)
     * Hỗ trợ tìm theo tên (chứa từ khóa không phân biệt hoa thường) và lọc theo loại sản phẩm.
     * @param keyword Từ khóa tên sản phẩm cần tìm
     * @param categoryId Mã loại sản phẩm cần lọc (nếu có)
     * @param page Trang số mấy (bắt đầu từ 0)
     * @param size Số lượng sản phẩm trên một trang
     * @param onlyActive Nếu true thì chỉ lấy sản phẩm đang kinh doanh (Status = true)
     * @return DTO phân trang chứa danh sách sản phẩm và tổng số trang
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponseDTO> search(String keyword, Integer categoryId, int page, int size, Boolean onlyActive) {
        String kw = keyword == null ? "" : keyword.trim();
        Page<Product> result;
        var pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 12 : size);

        // 1. Phân loại truy vấn theo các điều kiện lọc kết hợp
        if (categoryId != null && !kw.isEmpty()) {
            result = productRepository.findByCategory_CategoryIdAndProductNameContainingIgnoreCase(categoryId, kw, pageable);
        } else if (categoryId != null) {
            result = productRepository.findByCategory_CategoryId(categoryId, pageable);
        } else if (!kw.isEmpty()) {
            result = productRepository.findByProductNameContainingIgnoreCase(kw, pageable);
        } else {
            result = productRepository.findAll(pageable);
        }

        // 2. Chuyển đổi Entity sang DTO và lọc theo trạng thái hoạt động nếu được yêu cầu
        var content = result.getContent().stream()
                .filter(p -> onlyActive == null || !onlyActive || Boolean.TRUE.equals(p.getStatus()))
                .map(this::toDto)
                .toList();

        return new PageResponseDTO<>(content, result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
    }

    /**
     * Chức năng: Xem thông tin chi tiết một sản phẩm theo ID
     * @param id Mã sản phẩm (MaSP)
     * @return DTO thông tin sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Integer id) {
        return toDto(get(id));
    }

    /**
     * Chức năng: Thêm mới một sản phẩm vào hệ thống (Dành cho Quản lý)
     * Luồng xử lý nghiệp vụ:
     * 1. Kiểm tra sự tồn tại của Loại sản phẩm (Category) và Nhà cung cấp (Supplier)
     * 2. Khởi tạo đối tượng Product với đơn vị tính mặc định là 'kg' nếu để trống
     * 3. Mặc định trạng thái kinh doanh là hoạt động (true)
     * 4. Lưu vào bảng SanPham
     * @param request DTO chứa dữ liệu tạo sản phẩm
     * @return DTO sản phẩm vừa được lưu thành công
     */
    @Override
    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        Product p = Product.builder()
                .productName(request.getProductName())
                .category(getCategory(request.getCategoryId()))
                .supplier(request.getSupplierId() == null ? null : getSupplier(request.getSupplierId()))
                .unit(request.getUnit() == null || request.getUnit().isBlank() ? "kg" : request.getUnit())
                .price(request.getPrice())
                .description(request.getDescription())
                .image(request.getImage())
                .status(request.getStatus() == null || request.getStatus())
                .build();
        return toDto(productRepository.save(p));
    }

    /**
     * Chức năng: Cập nhật thông tin sản phẩm (Dành cho Quản lý)
     * Cập nhật tên, loại SP, nhà cung cấp, giá bán, mô tả, ảnh và trạng thái kinh doanh
     * @param id Mã sản phẩm cần sửa
     * @param request DTO dữ liệu cập nhật
     * @return DTO sản phẩm sau khi cập nhật
     */
    @Override
    @Transactional
    public ProductResponseDTO update(Integer id, ProductRequestDTO request) {
        Product p = get(id);
        p.setProductName(request.getProductName());
        p.setCategory(getCategory(request.getCategoryId()));
        p.setSupplier(request.getSupplierId() == null ? null : getSupplier(request.getSupplierId()));
        if (request.getUnit() != null && !request.getUnit().isBlank()) {
            p.setUnit(request.getUnit());
        }
        p.setPrice(request.getPrice());
        p.setDescription(request.getDescription());
        p.setImage(request.getImage());
        if (request.getStatus() != null) {
            p.setStatus(request.getStatus());
        }
        return toDto(productRepository.save(p));
    }

    /**
     * Chức năng: Xóa mềm sản phẩm (Soft Delete / Ngừng bán)
     * Thay vì xóa cứng dòng bản ghi trong MySQL (dễ gây lỗi khóa ngoại FK với ChiTietDonHang),
     * hệ thống chuyển trường TrangThai thành false (0 - Ngừng kinh doanh).
     * @param id Mã sản phẩm cần ẩn/ngừng bán
     */
    @Override
    @Transactional
    public void deleteSoft(Integer id) {
        Product p = get(id);
        p.setStatus(false);
        productRepository.save(p);
    }

    /**
     * Hàm phụ trợ: Lấy Entity Product theo ID hoặc báo lỗi 404
     */
    private Product get(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    /**
     * Hàm phụ trợ: Lấy Entity Category theo ID hoặc báo lỗi 404
     */
    private Category getCategory(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại sản phẩm"));
    }

    /**
     * Hàm phụ trợ: Lấy Entity Supplier theo ID hoặc báo lỗi 404
     */
    private Supplier getSupplier(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
    }

    /**
     * Hàm phụ trợ: Chuyển đổi Entity Product sang ProductResponseDTO hiển thị cho Frontend
     */
    private ProductResponseDTO toDto(Product p) {
        return ProductResponseDTO.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .categoryId(p.getCategory().getCategoryId())
                .categoryName(p.getCategory().getCategoryName())
                .supplierId(p.getSupplier() != null ? p.getSupplier().getSupplierId() : null)
                .supplierName(p.getSupplier() != null ? p.getSupplier().getSupplierName() : null)
                .unit(p.getUnit())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .description(p.getDescription())
                .image(p.getImage())
                .status(p.getStatus())
                .build();
    }
}

