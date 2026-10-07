# QUY ƯỚC DÙNG CHUNG KHI VIBE CODE

## Dự án: Hệ thống Web Bán Rau (MVC – Spring Boot – MySQL)

> **Bắt buộc đọc file này TRƯỚC khi bắt đầu prompt AI code bất kỳ phần nào.**
>
> Mục tiêu: cả nhóm dùng chung 1 kiến trúc, 1 cách đặt tên, 1 cấu trúc thư mục,
> để ghép code lại không bị xung đột, không phải sửa lại từ đầu.

---

## 1. Tóm tắt kiến trúc (bám sát đồ án)

- **Kiến trúc duy nhất:** MVC (Model – View – Controller)
- **Công nghệ:** Java 17 + Spring Boot 3.2 (Spring MVC, Spring Data JPA, Spring Security) + MySQL 8.0
- **Giao tiếp:** RESTful API (JSON), Controller dùng `@RestController`. View là giao diện web gọi API, không render view phía server.
- **Design Pattern bắt buộc áp dụng:**
  - **Singleton** → `DatabaseConnectionManager`, `AppConfigManager` (thực chất là Spring Bean mặc định singleton scope)
  - **Strategy** → `IPaymentStrategy` (Cash / BankTransfer / Cod) khi xử lý thanh toán đơn hàng, tương ứng 3 giá trị `PhuongThucTT`
  - **Observer** → `OrderStatusChangedEvent` + các `@EventListener` (StockRestoreListener, NotificationListener, LowStockWarningListener)
- **3 Actor** (theo use case diagram):

| Actor (trong code/tài liệu) | Giá trị `VaiTro` trong DB | Use case |
|---|---|---|
| Customer (Khách hàng) | `KhachHang` | Đăng ký, Đăng nhập, Tra cứu, Tài khoản, Giỏ hàng, Thanh toán |
| Employee (Nhân viên) | `NhanVien` | Đăng nhập, Tra cứu, Tài khoản, QL Đơn hàng, QL Bán hàng |
| Manager (Quản lý) | `QuanLy` | **Kế thừa toàn bộ quyền Employee**, thêm: QL Sản phẩm, QL Nhà cung cấp, QL Tài khoản, QL Khách hàng, QL Nhân viên, Báo cáo & Thống kê |

> Vì Manager kế thừa Employee, mọi endpoint của Employee đều cho phép cả Manager (`hasAnyRole('NhanVien','QuanLy')`).

---

## 2. Cấu trúc package BẮT BUỘC (không tự ý đổi tên package)

```
com.vegeshop
 ├── controller/      → @RestController, chỉ nhận request, gọi service, KHÔNG chứa business logic
 ├── service/         → xử lý nghiệp vụ, có interface + impl (VD: OrderService, OrderServiceImpl)
 │    ├── strategy/   → IPaymentStrategy, CashPaymentStrategy, BankTransferPaymentStrategy, CodPaymentStrategy
 │    └── event/      → OrderStatusChangedEvent, các Listener (Observer pattern)
 ├── repository/      → interface kế thừa JpaRepository<Entity, ID>
 ├── model/           → các @Entity, KHÔNG phụ thuộc package nào khác
 │    ├── enums/      → Role, Gender, OrderType, OrderStatus, PaymentMethod
 │    └── id/         → khóa chính phức hợp (@Embeddable): CartItemId, OrderItemId, PurchaseReceiptItemId
 ├── dto/             → Request/Response DTO, tách biệt hoàn toàn với Entity (không trả Entity thẳng ra API)
 │    └── report/     → DTO/projection cho 4 View báo cáo
 ├── config/          → SecurityConfig, JwtConfig, WebMvcConfig, AppConfigManager (Singleton)
 ├── exception/       → Custom Exception + GlobalExceptionHandler (@ControllerAdvice)
 └── util/            → class tiện ích dùng chung (JwtUtil, DateTimeUtil,...)
```

**Quy tắc:** mỗi Entity "chính" ở mục 3 đều phải có đủ 4 lớp: `Entity`, `Repository`, `Service` (+ `ServiceImpl`), `Controller`. Không gộp 2 entity vào 1 file.

**Ngoại lệ:** 3 entity chi tiết (`CartItem`, `OrderItem`, `PurchaseReceiptItem`) là quan hệ Composition với entity cha, nên chỉ cần `Entity` + `Repository`. Nghiệp vụ và API xử lý qua Service/Controller của entity cha.

---

## 3. Danh sách Entity chuẩn (12 thực thể — copy nguyên, không đổi tên field)

> **Database là nguồn chuẩn.** Tên bảng/cột giữ NGUYÊN như script SQL (tiếng Việt, PascalCase). Code Java dùng tên tiếng Anh, ánh xạ bằng `@Table(name = "...")` và `@Column(name = "...")`. Cột "Thuộc tính" ghi theo dạng `tênField (TÊN_CỘT)`.

| # | Entity (tên class) | Bảng | Thuộc tính (giữ đúng tên, camelCase) |
|---|---|---|---|
| 1 | `Account` | `TaiKhoan` | accountId (MaTK), username (TenDangNhap), password (MatKhau), role (VaiTro), status (TrangThai), createdAt (NgayTao) |
| 2 | `Customer` | `KhachHang` | customerId (MaKH), account (MaTK, nullable), fullName (HoTen), phone (SDT), email (Email), address (DiaChi) |
| 3 | `Employee` | `NhanVien` | employeeId (MaNV), account (MaTK), fullName (HoTen), birthDate (NgaySinh), gender (GioiTinh), phone (SDT), address (DiaChi), hireDate (NgayVaoLam) |
| 4 | `Supplier` | `NhaCungCap` | supplierId (MaNCC), supplierName (TenNCC), phone (SDT), email (Email), address (DiaChi) |
| 5 | `Category` | `LoaiSanPham` | categoryId (MaLoai), categoryName (TenLoai), description (MoTa) |
| 6 | `Product` | `SanPham` | productId (MaSP), productName (TenSP), category (MaLoai), supplier (MaNCC, nullable), unit (DonViTinh), price (GiaBan), stockQuantity (SoLuongTon), description (MoTa), image (HinhAnh), status (TrangThai) |
| 7 | `Cart` | `GioHang` | cartId (MaGH), customer (MaKH), createdAt (NgayTao) |
| 8 | `CartItem` | `ChiTietGioHang` | id: `CartItemId(cartId, productId)`, cart, product, quantity (SoLuong) |
| 9 | `SalesOrder` (không đặt là `Order` vì trùng từ khóa JPQL `ORDER`) | `DonHang` | orderId (MaDH), customer (MaKH, nullable), employee (MaNV, nullable), orderDate (NgayDat), orderType (HinhThuc), status (TrangThai), paymentMethod (PhuongThucTT), shippingAddress (DiaChiGiao), note (GhiChu), totalAmount (TongTien) |
| 10 | `OrderItem` | `ChiTietDonHang` | id: `OrderItemId(orderId, productId)`, order, product, quantity (SoLuong), unitPrice (DonGia) |
| 11 | `PurchaseReceipt` | `PhieuNhap` | receiptId (MaPN), supplier (MaNCC), employee (MaNV, nullable), receiptDate (NgayNhap), totalAmount (TongTien) |
| 12 | `PurchaseReceiptItem` | `ChiTietPhieuNhap` | id: `PurchaseReceiptItemId(receiptId, productId)`, receipt, product, quantity (SoLuong), importPrice (GiaNhap) |

**Kiểu dữ liệu bắt buộc:** mọi cột `DECIMAL` (giá, số lượng, tổng tiền) dùng `BigDecimal`, tuyệt đối không dùng `int`/`double`/`float` (rau bán theo kg nên số lượng có thể lẻ). Cột `TINYINT(1)` dùng `Boolean`.

**View báo cáo (không phải Entity):** `v_doanh_thu_ngay`, `v_doanh_thu_thang`, `v_sp_ban_chay`, `v_ton_kho_thap` chỉ đọc. Dùng interface projection + `@Query(nativeQuery = true)`, không tạo bảng, không tạo Entity ghi được.

**Quan hệ (giữ đúng như CSDL, không tự thêm/bớt quan hệ):**

- Account 1—1 Customer (Customer.account có thể null: khách mua tại quầy)
- Account 1—1 Employee (bắt buộc)
- Customer 1—1 Cart
- Cart 1—n CartItem (Composition)
- Product 1—n CartItem
- Category 1—n Product
- Supplier 1—n Product (có thể null)
- Customer 1—n SalesOrder (có thể null: khách vãng lai)
- Employee 1—n SalesOrder (có thể null: người xử lý/bán)
- SalesOrder 1—n OrderItem (Composition)
- Product 1—n OrderItem
- Supplier 1—n PurchaseReceipt
- Employee 1—n PurchaseReceipt
- PurchaseReceipt 1—n PurchaseReceiptItem (Composition)
- Product 1—n PurchaseReceiptItem

**Giá trị chuẩn (enum) — dùng đúng chuỗi này, trùng 100% với `ENUM` trong DB, dùng `@Enumerated(EnumType.STRING)`:**

> Ngoại lệ có chủ đích: tên hằng enum giữ nguyên PascalCase của DB (không đổi sang UPPER_SNAKE_CASE) để khỏi phải viết converter.

- `Account.role`: `KhachHang`, `NhanVien`, `QuanLy`
- `Employee.gender`: `Nam`, `Nu`, `Khac`
- `SalesOrder.orderType`: `Online`, `TaiQuay`
- `SalesOrder.status`: `ChoXacNhan`, `DangGiao`, `HoanThanh`, `DaHuy`
- `SalesOrder.paymentMethod`: `TienMat`, `ChuyenKhoan`, `COD`
- `Account.status`, `Product.status`: `Boolean` (true = hoạt động / đang bán, false = khóa / ngừng bán)

**Luồng trạng thái đơn hàng (chỉ cho phép các chuyển đổi sau, còn lại ném `InvalidStatusTransitionException`):**

| Từ | Sang | Ai thực hiện |
|---|---|---|
| `ChoXacNhan` | `DangGiao` | Employee (xác nhận) |
| `DangGiao` | `HoanThanh` | Employee |
| `ChoXacNhan` | `DaHuy` | Customer (đơn của mình) hoặc Employee |
| `DangGiao` | `DaHuy` | Chỉ Employee |
| `HoanThanh`, `DaHuy` | — | Trạng thái cuối, không đổi nữa |

---

## 4. Quy tắc CSDL & nghiệp vụ QUAN TRỌNG (đọc kỹ, dễ lỗi nhất)

Database có **trigger, CHECK, ENUM, view** nên AI rất hay code sai nếu không được dặn trước.

1. **Không để Hibernate tạo/sửa bảng.** Đặt `spring.jpa.hibernate.ddl-auto=none` trong `application.properties`. Script SQL là nguồn chuẩn, chạy thủ công; nếu để Hibernate tự tạo sẽ mất trigger, view, CHECK, ENUM.
2. **Trigger `trg_ban_hang`** (sau INSERT `ChiTietDonHang`): DB **tự trừ** `SoLuongTon` và **tự cộng** `DonHang.TongTien`.
   → Code Java **KHÔNG** được tự trừ kho hay tự cộng tổng tiền (sẽ bị tính 2 lần).
   → `SalesOrder.totalAmount` và `Product.stockQuantity` khai báo `@Column(insertable = false, updatable = false)`. Sau khi lưu xong các `OrderItem`, phải `refresh` (hoặc query lại) để lấy giá trị mới từ DB.
   → Tồn kho chỉ thay đổi qua bán hàng hoặc phiếu nhập, **không cho sửa trực tiếp** qua API sản phẩm.
3. **Trigger `trg_nhap_hang`** (sau INSERT `ChiTietPhieuNhap`): DB tự cộng `SoLuongTon` và `PhieuNhap.TongTien`. Cùng quy tắc như trên, Java không tự cộng.
4. **Kiểm tra tồn kho TRƯỚC khi insert `OrderItem`:** nếu `quantity > stockQuantity` thì ném `InsufficientStockException` (HTTP 400). Nếu bỏ qua, CHECK `SoLuongTon >= 0` của DB sẽ làm lỗi SQL khó hiểu.
5. **Hủy đơn không tự hoàn kho** (trigger chỉ chạy khi INSERT). Khi đơn chuyển sang `DaHuy`, `StockRestoreListener` (Observer) phải cộng lại `stockQuantity` theo từng `OrderItem`. Chỉ hoàn kho đúng 1 lần (kiểm tra trạng thái cũ khác `DaHuy`).
6. **Giá lấy từ server:** `OrderItem.unitPrice` luôn lấy từ `Product.price` tại thời điểm bán. Không nhận giá từ client.
7. **Thanh toán online (checkout) phải nằm trong 1 `@Transactional`:** đọc giỏ hàng → kiểm tra tồn kho → tạo `SalesOrder` (`orderType = Online`, `status = ChoXacNhan`) → tạo `OrderItem` → xóa `CartItem` của giỏ. Lỗi ở bước nào thì rollback toàn bộ.
8. **Bán tại quầy:** `orderType = TaiQuay`, tạo thẳng `status = HoanThanh`, `employee` lấy từ người đang đăng nhập (không nhận từ client), `customer` có thể null.
9. **Không xóa cứng** `Product`, `Customer`, `Employee`, `Account`, `Supplier`, `Category` vì có khóa ngoại. Sản phẩm ngừng bán thì `status = false`; tài khoản thì khóa (`status = false`). Chỉ `CartItem` được xóa cứng. Xóa `Supplier`/`Category` còn dữ liệu liên quan thì ném `DuplicateResourceException`/`ConflictException` (HTTP 409).
10. **Đăng ký:** tạo `Account` (role `KhachHang`) + `Customer` + `Cart` rỗng trong 1 transaction. **Tạo nhân viên:** tạo `Account` (role `NhanVien`) + `Employee`. Mật khẩu hash BCrypt, **không bao giờ** trả `password` trong DTO. Tài khoản `status = false` thì từ chối đăng nhập.
11. **Phân quyền:** lấy user hiện tại từ JWT/SecurityContext. Customer chỉ xem/sửa được dữ liệu của chính mình (giỏ hàng, đơn hàng, thông tin cá nhân). Kiểm tra quyền sở hữu ở tầng Service.
12. **Strategy thanh toán** (`IPaymentStrategy`, chọn theo `paymentMethod`): mỗi strategy kiểm tra điều kiện của phương thức đó. Ví dụ `COD` bắt buộc `orderType = Online` và có `shippingAddress`; `TienMat` dùng cho `TaiQuay`; `ChuyenKhoan` ghi thông tin chuyển khoản vào `note`. Gom các strategy vào `Map<PaymentMethod, IPaymentStrategy>` để Service lấy ra dùng, không `if/else` dài trong Service.
13. **Observer:** `OrderService` đổi trạng thái xong thì publish `OrderStatusChangedEvent(order, oldStatus, newStatus)`. Dùng `@EventListener` đồng bộ, cùng transaction với Service:
    - `StockRestoreListener`: newStatus = `DaHuy` thì hoàn kho
    - `NotificationListener`: ghi log/thông báo cho khách khi đơn đổi trạng thái
    - `LowStockWarningListener`: sau khi bán, nếu `stockQuantity < 10` thì ghi cảnh báo (cùng ngưỡng với view `v_ton_kho_thap`)

---

## 5. Quy tắc đặt tên (Naming Convention)

| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Package | chữ thường, không viết hoa | `com.vegeshop.service` |
| Class / Entity / DTO | PascalCase | `OrderService`, `OrderRequestDTO` |
| Biến, method | camelCase | `getStockQuantity()` |
| Hằng số | UPPER_SNAKE_CASE | `LOW_STOCK_THRESHOLD` |
| Enum (ngoại lệ) | giữ nguyên chuỗi của DB | `OrderStatus.ChoXacNhan` |
| Bảng / cột CSDL | **giữ nguyên theo script SQL, không đổi** | `ChiTietDonHang`, `MaDH` |
| REST endpoint | kebab-case, số nhiều, có version | `/api/v1/products`, `/api/v1/purchase-receipts` |
| Request DTO | `<Entity>RequestDTO` | `ProductRequestDTO` |
| Response DTO | `<Entity>ResponseDTO` | `ProductResponseDTO` |
| Interface Service | `<Ten>Service` (interface) + `<Ten>ServiceImpl` (class) | `OrderService` / `OrderServiceImpl` |
| Branch git | `feature/<module>-<mo-ta-ngan>` | `feature/order-checkout` |
| Commit message | `<module>: <mô tả ngắn tiếng Việt hoặc Anh>` | `order: fix hoan kho khi huy don` |

---

## 6. Chuẩn REST API (để không đặt trùng / lệch route)

Base path: `/api/v1`

Ký hiệu quyền: **Public** = không cần đăng nhập · **Customer** · **Employee** (gồm cả Manager) · **Manager**

| Module | Method | Endpoint | Ai dùng |
|---|---|---|---|
| Auth | POST | `/api/v1/auth/register` | Public |
| Auth | POST | `/api/v1/auth/login` | Public |
| Account | GET / PUT | `/api/v1/accounts/me` | Mọi user đã đăng nhập |
| Account | PUT | `/api/v1/accounts/me/password` | Mọi user đã đăng nhập |
| Account | GET | `/api/v1/accounts` | Manager |
| Account | PUT | `/api/v1/accounts/{id}/status` (khóa/mở) | Manager |
| Customer | GET | `/api/v1/customers`, `/api/v1/customers/{id}` | Manager |
| Customer | PUT | `/api/v1/customers/{id}` | Manager |
| Employee | GET / POST / PUT | `/api/v1/employees`, `/api/v1/employees/{id}` | Manager |
| Supplier | GET | `/api/v1/suppliers`, `/api/v1/suppliers/{id}` | Manager |
| Supplier | POST / PUT / DELETE | `/api/v1/suppliers/{id}` | Manager |
| Category | GET | `/api/v1/categories` | Public |
| Category | POST / PUT / DELETE | `/api/v1/categories/{id}` | Manager |
| Product | GET | `/api/v1/products?keyword=&categoryId=&page=&size=` (Tra cứu) | Public |
| Product | GET | `/api/v1/products/{id}` | Public |
| Product | POST / PUT / DELETE (xóa mềm) | `/api/v1/products/{id}` | Manager |
| Purchase | POST / GET | `/api/v1/purchase-receipts`, `/api/v1/purchase-receipts/{id}` | Manager |
| Cart | GET | `/api/v1/cart` | Customer |
| Cart | POST | `/api/v1/cart/items` | Customer |
| Cart | PUT / DELETE | `/api/v1/cart/items/{productId}` | Customer |
| Cart | DELETE | `/api/v1/cart` (xóa sạch giỏ) | Customer |
| Order | POST | `/api/v1/orders/checkout` (Thanh toán từ giỏ) | Customer |
| Order | GET | `/api/v1/orders/my` | Customer |
| Order | GET | `/api/v1/orders/{id}` | Customer (đơn của mình) / Employee |
| Order | GET | `/api/v1/orders?status=&type=&from=&to=` | Employee |
| Order | PUT | `/api/v1/orders/{id}/confirm` (→ `DangGiao`) | Employee |
| Order | PUT | `/api/v1/orders/{id}/complete` (→ `HoanThanh`) | Employee |
| Order | PUT | `/api/v1/orders/{id}/cancel` (→ `DaHuy`) | Customer / Employee |
| Sales | POST | `/api/v1/sales` (bán tại quầy) | Employee |
| Sales | GET | `/api/v1/sales` | Employee |
| Report | GET | `/api/v1/reports/revenue-daily` | Manager |
| Report | GET | `/api/v1/reports/revenue-monthly` | Manager |
| Report | GET | `/api/v1/reports/best-sellers` | Manager |
| Report | GET | `/api/v1/reports/low-stock` | Manager |

> Mapping view → API: `v_doanh_thu_ngay` → revenue-daily · `v_doanh_thu_thang` → revenue-monthly · `v_sp_ban_chay` → best-sellers · `v_ton_kho_thap` → low-stock.

**Response chuẩn** (bọc chung 1 wrapper, không trả thẳng Entity/Exception ra ngoài):

```json
{
  "success": true,
  "message": "string",
  "data": {}
}
```

**Mã lỗi chuẩn** (do `GlobalExceptionHandler` xử lý):

| Exception | HTTP |
|---|---|
| `ResourceNotFoundException` | 404 |
| `InsufficientStockException`, validation lỗi | 400 |
| `InvalidStatusTransitionException`, `DuplicateResourceException` | 409 |
| Chưa đăng nhập / sai quyền | 401 / 403 |

---

## 7. CHIA VIỆC CHO 3 NGƯỜI (theo module dọc — tránh đụng code)

Chia theo chiều dọc (mỗi người 1 nhóm entity + toàn bộ controller/service/repo liên quan), **không chia theo tầng ngang** (vì sẽ phải đợi nhau).

| Người | Module phụ trách | Entity | Design pattern liên quan |
|---|---|---|---|
| **Người 1** | Auth + Tài khoản + Khách hàng + Nhân viên + Nhà cung cấp | Account, Customer, Employee, Supplier | Singleton (AppConfigManager, DatabaseConnectionManager) |
| **Người 2** | Loại SP + Sản phẩm (Tra cứu) + Nhập hàng + Giỏ hàng | Category, Product, PurchaseReceipt(+Item), Cart(+Item) | — |
| **Người 3** | Đơn hàng + Bán hàng + Thanh toán + Báo cáo | SalesOrder, OrderItem, 4 View báo cáo | Strategy (IPaymentStrategy), Observer (OrderStatusChangedEvent) |

**Bước 0 (làm trước, cả nhóm cùng thống nhất):** tạo đủ 12 Entity + enum + khóa phức hợp theo đúng mục 3 và push lên nhánh chung. Người 3 phụ thuộc `Product`, `Customer`, `Employee`, nên không ai được chờ nhau ở các Entity này.

**Dùng chung, không ai được tự sửa nếu chưa báo nhóm:**

- `com.vegeshop.config` (SecurityConfig, JwtConfig)
- `com.vegeshop.exception` (GlobalExceptionHandler)
- `com.vegeshop.util`
- `com.vegeshop.model` (Entity, enum, khóa phức hợp) sau Bước 0
- File `application.properties` và file script SQL

---

## 8. Checklist trước khi merge code (mỗi người tự check trước khi đẩy lên)

- [ ] Đặt tên package/class/API đúng bảng ở mục 3, 5, 6
- [ ] Không trả Entity trực tiếp ra Controller, phải qua DTO (và DTO không chứa `password`)
- [ ] Có validate input cơ bản (`@Valid`, `@NotNull`, `@Positive`...)
- [ ] Exception ném ra được `GlobalExceptionHandler` bắt, không để lộ stacktrace ra JSON response
- [ ] Không hardcode chuỗi trạng thái khác với enum ở mục 3
- [ ] Tiền và số lượng dùng `BigDecimal`
- [ ] **Không tự trừ/cộng tồn kho hay tổng tiền trong Java** (đã có trigger, xem mục 4)
- [ ] Đã kiểm tra tồn kho trước khi tạo `OrderItem`
- [ ] Đã test thử bằng Postman/curl ít nhất các case chính (thành công + lỗi + sai quyền)
- [ ] Không sửa file thuộc phần người khác phụ trách mà không báo trước

---

## 9. Cách dùng file này khi prompt AI

Khi vibe code, mỗi người copy đoạn tương ứng với module mình phụ trách + mục 2, 3, 4, 5 vào đầu prompt, ví dụ:

> "Tôi đang code module Đơn hàng cho hệ thống Web Bán Rau, kiến trúc MVC Spring Boot MySQL. Hãy tuân thủ đúng cấu trúc package, tên Entity, tên enum, quy tắc CSDL/trigger và chuẩn REST API sau: [dán mục 2, 3, 4, 5, 6]. Nhiệm vụ cụ thể: viết luồng checkout từ giỏ hàng thành đơn hàng Online..."

Làm vậy để AI không tự bịa tên field/API khác với những người còn lại, và không code trùng việc mà trigger trong DB đã làm.
