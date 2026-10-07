package com.vegeshop.service.impl;

import com.vegeshop.dto.SupplierRequestDTO;
import com.vegeshop.dto.SupplierResponseDTO;
import com.vegeshop.exception.ConflictException;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Supplier;
import com.vegeshop.repository.ProductRepository;
import com.vegeshop.repository.SupplierRepository;
import com.vegeshop.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các chức năng Quản lý Nhà cung cấp (Supplier Management)
 * Bao gồm: Tra cứu danh sách, chi tiết, thêm mới NCC, cập nhật thông tin NCC,
 * và xóa NCC có kiểm tra toàn vẹn dữ liệu (không xóa NCC đang cung cấp sản phẩm).
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    /**
     * Khởi tạo SupplierServiceImpl với các repository cần thiết
     */
    public SupplierServiceImpl(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    /**
     * Chức năng: Lấy danh sách toàn bộ nhà cung cấp trong hệ thống
     * @return Danh sách DTO thông tin nhà cung cấp
     */
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponseDTO> findAll() {
        return supplierRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Chức năng: Tra cứu thông tin chi tiết một nhà cung cấp theo mã ID
     * @param id Mã nhà cung cấp (MaNCC)
     * @return DTO chi tiết nhà cung cấp
     */
    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO findById(Integer id) {
        return toDto(get(id));
    }

    /**
     * Chức năng: Thêm mới nhà cung cấp (Dành cho Quản lý)
     * Lưu thông tin: Tên nhà cung cấp, SĐT, Email, Địa chỉ
     * @param request DTO dữ liệu tạo mới NCC
     * @return DTO nhà cung cấp vừa lưu thành công
     */
    @Override
    @Transactional
    public SupplierResponseDTO create(SupplierRequestDTO request) {
        Supplier s = Supplier.builder()
                .supplierName(request.getSupplierName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();
        return toDto(supplierRepository.save(s));
    }

    /**
     * Chức năng: Cập nhật thông tin nhà cung cấp
     * @param id Mã nhà cung cấp cần sửa
     * @param request DTO dữ liệu cập nhật
     * @return DTO nhà cung cấp sau khi cập nhật
     */
    @Override
    @Transactional
    public SupplierResponseDTO update(Integer id, SupplierRequestDTO request) {
        Supplier s = get(id);
        s.setSupplierName(request.getSupplierName());
        s.setPhone(request.getPhone());
        s.setEmail(request.getEmail());
        s.setAddress(request.getAddress());
        return toDto(supplierRepository.save(s));
    }

    /**
     * Chức năng: Xóa nhà cung cấp khỏi hệ thống
     * Quy tắc toàn vẹn dữ liệu:
     * - Kiểm tra xem nhà cung cấp có đang liên kết với bất kỳ sản phẩm nào trong kho không
     * - Nếu còn sản phẩm -> Ngăn chặn xóa để tránh lỗi khóa ngoại FK và ném ngoại lệ ConflictException
     * @param id Mã nhà cung cấp cần xóa
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        Supplier s = get(id);
        if (productRepository.existsBySupplier_SupplierId(id)) {
            throw new ConflictException("Không thể xóa nhà cung cấp còn sản phẩm liên quan");
        }
        supplierRepository.delete(s);
    }

    /**
     * Hàm phụ trợ: Tìm Entity Supplier theo ID hoặc ném lỗi 404 nếu không tìm thấy
     */
    private Supplier get(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
    }

    /**
     * Hàm phụ trợ: Chuyển đổi Entity Supplier sang SupplierResponseDTO
     */
    private SupplierResponseDTO toDto(Supplier s) {
        return SupplierResponseDTO.builder()
                .supplierId(s.getSupplierId())
                .supplierName(s.getSupplierName())
                .phone(s.getPhone())
                .email(s.getEmail())
                .address(s.getAddress())
                .build();
    }
}

