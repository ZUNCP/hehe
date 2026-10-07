package com.vegeshop.service.impl;

import com.vegeshop.dto.PurchaseReceiptItemResponseDTO;
import com.vegeshop.dto.PurchaseReceiptRequestDTO;
import com.vegeshop.dto.PurchaseReceiptResponseDTO;
import com.vegeshop.exception.ResourceNotFoundException;
import com.vegeshop.model.Product;
import com.vegeshop.model.PurchaseReceipt;
import com.vegeshop.model.PurchaseReceiptItem;
import com.vegeshop.model.Supplier;
import com.vegeshop.model.id.PurchaseReceiptItemId;
import com.vegeshop.repository.ProductRepository;
import com.vegeshop.repository.PurchaseReceiptItemRepository;
import com.vegeshop.repository.PurchaseReceiptRepository;
import com.vegeshop.repository.SupplierRepository;
import com.vegeshop.service.AccountService;
import com.vegeshop.service.PurchaseReceiptService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service xử lý các chức năng Nhập kho & Phiếu nhập hàng (Purchase Receipt Management)
 * Bao gồm: Tra cứu danh sách phiếu nhập, xem chi tiết phiếu nhập,
 * và lập phiếu nhập mới từ nhà cung cấp (hệ thống tự động cộng tồn kho qua trigger MySQL trg_nhap_hang).
 */
@Service
public class PurchaseReceiptServiceImpl implements PurchaseReceiptService {

    private final PurchaseReceiptRepository receiptRepository;
    private final PurchaseReceiptItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final AccountService accountService;
    private final EntityManager entityManager;

    /**
     * Khởi tạo PurchaseReceiptServiceImpl với các repository cần thiết
     */
    public PurchaseReceiptServiceImpl(PurchaseReceiptRepository receiptRepository,
                                      PurchaseReceiptItemRepository itemRepository,
                                      SupplierRepository supplierRepository,
                                      ProductRepository productRepository,
                                      AccountService accountService,
                                      EntityManager entityManager) {
        this.receiptRepository = receiptRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.accountService = accountService;
        this.entityManager = entityManager;
    }

    /**
     * Chức năng: Lấy danh sách toàn bộ phiếu nhập kho
     * Sắp xếp theo mã phiếu mới nhất trước
     * @return Danh sách DTO phiếu nhập
     */
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseReceiptResponseDTO> findAll() {
        return receiptRepository.findAllByOrderByReceiptIdDesc().stream().map(this::toDto).toList();
    }

    /**
     * Chức năng: Xem thông tin chi tiết một phiếu nhập kho theo ID
     * @param id Mã phiếu nhập (MaPN)
     * @return DTO chi tiết phiếu nhập và danh sách sản phẩm nhập
     */
    @Override
    @Transactional(readOnly = true)
    public PurchaseReceiptResponseDTO findById(Integer id) {
        return toDto(get(id));
    }

    /**
     * Chức năng: Lập phiếu nhập kho mới từ nhà cung cấp (Dành cho Quản lý)
     * Luồng xử lý nghiệp vụ:
     * 1. Kiểm tra sự tồn tại của Nhà cung cấp
     * 2. Khởi tạo phiếu nhập gắn với nhân viên/quản lý đang thao tác
     * 3. Lưu từng dòng chi tiết phiếu nhập (sản phẩm, số lượng, giá nhập)
     * 4. CSDL kích hoạt trigger trg_nhap_hang để tự động cộng tồn kho và tính tổng tiền phiếu nhập
     * @param request DTO chứa mã nhà cung cấp và danh sách sản phẩm nhập
     * @return DTO phiếu nhập hoàn tất
     */
    @Override
    @Transactional
    public PurchaseReceiptResponseDTO create(PurchaseReceiptRequestDTO request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));

        // Tạo phiếu nhập gắn với nhân viên/quản lý hiện tại
        PurchaseReceipt receipt = PurchaseReceipt.builder()
                .supplier(supplier)
                .employee(accountService.currentEmployee())
                .build();
        receipt = receiptRepository.saveAndFlush(receipt);

        // Lưu từng dòng sản phẩm nhập kho
        for (PurchaseReceiptRequestDTO.Item line : request.getItems()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
            PurchaseReceiptItem item = PurchaseReceiptItem.builder()
                    .id(new PurchaseReceiptItemId(receipt.getReceiptId(), product.getProductId()))
                    .receipt(receipt)
                    .product(product)
                    .quantity(line.getQuantity())
                    .importPrice(line.getImportPrice())
                    .build();
            itemRepository.saveAndFlush(item);
        }

        // Refresh để lấy lại tổng tiền do Trigger CSDL vừa tính toán
        entityManager.refresh(receipt);
        return toDto(receipt);
    }

    /**
     * Hàm phụ trợ: Lấy Entity PurchaseReceipt theo ID hoặc ném lỗi 404
     */
    private PurchaseReceipt get(Integer id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập"));
    }

    /**
     * Hàm phụ trợ: Chuyển đổi Entity PurchaseReceipt sang PurchaseReceiptResponseDTO kèm chi tiết các món nhập
     */
    private PurchaseReceiptResponseDTO toDto(PurchaseReceipt r) {
        var items = itemRepository.findByReceipt_ReceiptId(r.getReceiptId());
        return PurchaseReceiptResponseDTO.builder()
                .receiptId(r.getReceiptId())
                .supplierId(r.getSupplier().getSupplierId())
                .supplierName(r.getSupplier().getSupplierName())
                .employeeId(r.getEmployee() != null ? r.getEmployee().getEmployeeId() : null)
                .employeeName(r.getEmployee() != null ? r.getEmployee().getFullName() : null)
                .receiptDate(r.getReceiptDate())
                .totalAmount(r.getTotalAmount())
                .items(items.stream().map(i -> PurchaseReceiptItemResponseDTO.builder()
                        .productId(i.getProduct().getProductId())
                        .productName(i.getProduct().getProductName())
                        .quantity(i.getQuantity())
                        .importPrice(i.getImportPrice())
                        .lineTotal(i.getImportPrice().multiply(i.getQuantity()))
                        .build()).toList())
                .build();
    }
}

