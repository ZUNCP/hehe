<?php
/**
 * TOÀN DIỆN HỆ THỐNG TEST SUITE - WEB BÁN RAU SẠCH (BÁCH HÓA XANH)
 * Kiểm thử chi tiết:
 * 1. Logic nghiệp vụ (Business Logic)
 * 2. Các chức năng (Features/Use Cases)
 * 3. Các điều kiện & Ràng buộc (Validation & Constraints)
 * 4. Hệ thống & Toàn vẹn dữ liệu (Triggers, Transactions, State Machine, CSDL)
 */

$baseUrl = 'http://127.0.0.1/rauma/rauma/rauma/view/api.php';
$pdo = new PDO('mysql:host=localhost;dbname=web_ban_rau;charset=utf8mb4', 'root', '', [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
]);

$totalTests = 0;
$passedTests = 0;
$failedTests = 0;
$testResults = [];

function request($method, $url, $data = null) {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    if ($data !== null) {
        $json = json_encode($data);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $json);
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Content-Type: application/json',
            'Content-Length: ' . strlen($json)
        ]);
    }
    $resp = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    $decoded = json_decode($resp, true);
    return ['code' => $code, 'raw' => $resp, 'json' => $decoded];
}

function assertTest($name, $condition, $details = '') {
    global $totalTests, $passedTests, $failedTests, $testResults;
    $totalTests++;
    if ($condition) {
        $passedTests++;
        $testResults[] = ['status' => 'PASS', 'name' => $name, 'details' => $details];
        echo "  [PASS] $name\n";
    } else {
        $failedTests++;
        $testResults[] = ['status' => 'FAIL', 'name' => $name, 'details' => $details];
        echo "  [FAIL] $name - $details\n";
    }
}

echo "====================================================================\n";
echo "    BẮT ĐẦU CHẠY KIỂM THỬ TOÀN DIỆN HỆ THỐNG WEB BÁN RAU SẠCH      \n";
echo "====================================================================\n\n";

// =====================================================================
// PHẦN 1: XÁC THỰC, TÀI KHOẢN VÀ ĐIỀU KIỆN NHẬP LIỆU (AUTH & ACCOUNTS)
// =====================================================================
echo "--- 1. KIỂM THỬ XÁC THỰC & ĐIỀU KIỆN RÀNG BUỘC TÀI KHOẢN ---\n";

// 1.1 Tên đăng nhập chứa khoảng trắng
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => 'user space',
    'password' => '123456',
    'fullName' => 'Nguyen Van Test'
]);
assertTest("Đăng ký: Tên đăng nhập chứa khoảng trắng bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'khoảng trắng'),
    $r['raw']
);

// 1.2 Tên đăng nhập chứa ký tự đặc biệt
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => 'user@#$!',
    'password' => '123456',
    'fullName' => 'Nguyen Van Test'
]);
assertTest("Đăng ký: Tên đăng nhập chứa ký tự đặc biệt bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'ký tự đặc biệt'),
    $r['raw']
);

// 1.3 Họ và tên chứa số
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => 'testuser' . time(),
    'password' => '123456',
    'fullName' => 'Nguyen Van 123'
]);
assertTest("Đăng ký: Họ tên chứa số bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'không được nhập số'),
    $r['raw']
);

// 1.4 Họ và tên chứa ký tự đặc biệt
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => 'testuser' . time(),
    'password' => '123456',
    'fullName' => 'Nguyen Van @!#'
]);
assertTest("Đăng ký: Họ tên chứa ký tự đặc biệt bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'ký tự đặc biệt'),
    $r['raw']
);

// 1.5 Số điện thoại sai định dạng (không phải 10 số bắt đầu bằng 0)
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => 'testuser' . time(),
    'password' => '123456',
    'fullName' => 'Nguyen Van Test',
    'phone' => '12345'
]);
assertTest("Đăng ký: SĐT không đúng 10 số bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], '10 chữ số'),
    $r['raw']
);

// 1.6 Email không đúng định dạng @gmail.com
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => 'testuser' . time(),
    'password' => '123456',
    'fullName' => 'Nguyen Van Test',
    'phone' => '0912345678',
    'email' => 'testuser@yahoo.com'
]);
assertTest("Đăng ký: Email không có đuôi @gmail.com bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'gmail.com'),
    $r['raw']
);

// 1.7 Đăng ký tài khoản hợp lệ
$testKhUsername = 'khtest' . rand(10000, 99999);
$testKhPass = 'P@ss1234';
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => $testKhUsername,
    'password' => $testKhPass,
    'fullName' => 'Trần Thị Kiểm Thử',
    'phone' => '0987654321',
    'email' => 'kiemthu' . time() . '@gmail.com',
    'address' => '123 Nguyễn Văn Cừ, Quận 5'
]);
assertTest("Đăng ký: Tài khoản hợp lệ được tạo thành công", 
    isset($r['json']['success']) && $r['json']['success'] === true,
    $r['raw']
);
$createdCustId = $r['json']['data']['customerId'] ?? null;

// 1.8 Kiểm tra hệ thống tự động khởi tạo giỏ hàng cho tài khoản mới
$cartCount = $pdo->query("SELECT COUNT(*) FROM GioHang WHERE MaKH = " . intval($createdCustId))->fetchColumn();
assertTest("Hệ thống: Tự động khởi tạo Giỏ hàng (GioHang) cho khách hàng mới", 
    $cartCount > 0,
    "MaKH: $createdCustId, Cart count: $cartCount"
);

// 1.9 Trùng tên đăng nhập
$r = request('POST', "$baseUrl?resource=auth&action=register", [
    'username' => $testKhUsername,
    'password' => 'anotherpass',
    'fullName' => 'Trần Thị B'
]);
assertTest("Đăng ký: Trùng tên đăng nhập bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'đã được sử dụng'),
    $r['raw']
);

// 1.10 Đăng nhập: Sai mật khẩu
$r = request('POST', "$baseUrl?resource=auth&action=login", [
    'username' => $testKhUsername,
    'password' => 'wrongpass'
]);
assertTest("Đăng nhập: Sai mật khẩu bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'Mật khẩu không chính xác'),
    $r['raw']
);

// 1.11 Đăng nhập: Đúng tài khoản mật khẩu
$r = request('POST', "$baseUrl?resource=auth&action=login", [
    'username' => $testKhUsername,
    'password' => $testKhPass
]);
assertTest("Đăng nhập: Thành công, trả về token, đúng vai trò KhachHang", 
    isset($r['json']['success']) && $r['json']['success'] === true && $r['json']['data']['role'] === 'KhachHang',
    $r['raw']
);

// 1.12 Đổi mật khẩu: Mật khẩu cũ sai
$r = request('POST', "$baseUrl?resource=accounts&action=password", [
    'username' => $testKhUsername,
    'oldPassword' => 'sai_pass',
    'newPassword' => 'newP@ssword678'
]);
assertTest("Đổi mật khẩu: Mật khẩu cũ sai bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'Mật khẩu hiện tại không đúng'),
    $r['raw']
);

// 1.13 Đổi mật khẩu: Mật khẩu mới < 6 ký tự
$r = request('POST', "$baseUrl?resource=accounts&action=password", [
    'username' => $testKhUsername,
    'oldPassword' => $testKhPass,
    'newPassword' => '123'
]);
assertTest("Đổi mật khẩu: Mật khẩu mới ngắn hơn 6 ký tự bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'ít nhất 6 ký tự'),
    $r['raw']
);

// 1.14 Đổi mật khẩu hợp lệ & xác nhận đăng nhập bằng mật khẩu mới
$testKhNewPass = 'newP@ssword678';
$r = request('POST', "$baseUrl?resource=accounts&action=password", [
    'username' => $testKhUsername,
    'oldPassword' => $testKhPass,
    'newPassword' => $testKhNewPass
]);
$rLoginNew = request('POST', "$baseUrl?resource=auth&action=login", [
    'username' => $testKhUsername,
    'password' => $testKhNewPass
]);
assertTest("Đổi mật khẩu: Thành công và đăng nhập được bằng mật khẩu mới", 
    isset($r['json']['success']) && $r['json']['success'] === true &&
    isset($rLoginNew['json']['success']) && $rLoginNew['json']['success'] === true,
    $rLoginNew['raw']
);

// 1.15 Khóa tài khoản và kiểm tra chặn đăng nhập
$accIdForCust = intval($createdCustId) + 100000;
$rLock = request('PUT', "$baseUrl?resource=accounts&id=$accIdForCust&action=status", [
    'status' => 0
]);
$rLoginLocked = request('POST', "$baseUrl?resource=auth&action=login", [
    'username' => $testKhUsername,
    'password' => $testKhNewPass
]);
assertTest("Hệ thống: Khóa tài khoản (TrangThai = 0) chặn đăng nhập thành công", 
    isset($rLoginLocked['json']['success']) && $rLoginLocked['json']['success'] === false && str_contains($rLoginLocked['json']['message'], 'đã bị khóa'),
    $rLoginLocked['raw']
);

// Mở khóa lại tài khoản
request('PUT', "$baseUrl?resource=accounts&id=$accIdForCust&action=status", ['status' => 1]);

echo "\n";

// =====================================================================
// PHẦN 2: QUẢN LÝ SẢN PHẨM & LOẠI SẢN PHẨM (PRODUCTS & CATEGORIES)
// =====================================================================
echo "--- 2. KIỂM THỬ SẢN PHẨM & LOẠI SẢN PHẨM (UC10, UC11, UC12) ---\n";

// 2.1 Thêm loại sản phẩm mới
$rCat = request('POST', "$baseUrl?resource=categories", [
    'categoryName' => 'Rau Củ Hữu Cơ Test ' . time(),
    'description' => 'Mô tả loại sản phẩm test'
]);
assertTest("Danh mục: Thêm loại sản phẩm thành công", 
    isset($rCat['json']['success']) && $rCat['json']['success'] === true,
    $rCat['raw']
);
$testCatId = $rCat['json']['data']['categoryId'] ?? 1;

// 2.2 Sản phẩm: Tên chứa ký tự đặc biệt (@#$!)
$r = request('POST', "$baseUrl?resource=products", [
    'productName' => 'Bắp Cải Súp Lơ #$@%',
    'categoryId' => $testCatId,
    'price' => 35000,
    'stockQuantity' => 20
]);
assertTest("Sản phẩm: Tên chứa ký tự đặc biệt bị từ chối (Backend 400)", 
    $r['code'] === 400 && isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'ký tự đặc biệt'),
    $r['raw']
);

// 2.3 Sản phẩm: Giá bán < 1.000 VNĐ
$r = request('POST', "$baseUrl?resource=products", [
    'productName' => 'Rau Muống Xanh Test',
    'categoryId' => $testCatId,
    'price' => 500,
    'stockQuantity' => 20
]);
assertTest("Sản phẩm: Giá bán < 1.000 VNĐ bị từ chối (Backend 400)", 
    $r['code'] === 400 && isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], '1.000'),
    $r['raw']
);

// 2.4 Sản phẩm: Giá bán chứa chữ cái / không phải số
$r = request('POST', "$baseUrl?resource=products", [
    'productName' => 'Rau Muống Xanh Test',
    'categoryId' => $testCatId,
    'price' => 'nam_muoi_nghin',
    'stockQuantity' => 20
]);
assertTest("Sản phẩm: Giá bán không phải số bị từ chối (Backend 400)", 
    $r['code'] === 400 && isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'phải là số'),
    $r['raw']
);

// 2.5 Thêm sản phẩm hợp lệ
$rSp = request('POST', "$baseUrl?resource=products", [
    'productName' => 'Cà Chua Beef Đà Lạt ' . time(),
    'categoryId' => $testCatId,
    'unit' => 'kg',
    'price' => 45000,
    'stockQuantity' => 50,
    'description' => 'Cà chua chuẩn VietGAP tươi ngon',
    'image' => 'https://example.com/cachuabeef.jpg',
    'status' => 1
]);
assertTest("Sản phẩm: Thêm sản phẩm hợp lệ thành công", 
    isset($rSp['json']['success']) && $rSp['json']['success'] === true,
    $rSp['raw']
);
$testProductId = $rSp['json']['data']['productId'] ?? null;

// 2.6 Sửa thông tin sản phẩm (Cập nhật tên, giá bán)
$rSpEdit = request('PUT', "$baseUrl?resource=products&id=$testProductId", [
    'productName' => 'Cà Chua Beef Đà Lạt Đặc Biệt ' . time(),
    'categoryId' => $testCatId,
    'unit' => 'kg',
    'price' => 48000,
    'description' => 'Cập nhật giá mới',
    'status' => 1
]);
assertTest("Sản phẩm: Cập nhật thông tin và giá bán thành công", 
    isset($rSpEdit['json']['success']) && $rSpEdit['json']['success'] === true,
    $rSpEdit['raw']
);

// 2.7 Ẩn sản phẩm (Ngừng kinh doanh)
$rSpHide = request('PUT', "$baseUrl?resource=products&id=$testProductId", [
    'status' => 0
]);
$spStatusInDb = $pdo->query("SELECT TrangThai FROM SanPham WHERE MaSP = $testProductId")->fetchColumn();
assertTest("Sản phẩm: Đổi trạng thái Ẩn (TrangThai = 0) thành công", 
    intval($spStatusInDb) === 0,
    "Db status: $spStatusInDb"
);

// Bật lại hiển thị sản phẩm
request('PUT', "$baseUrl?resource=products&id=$testProductId", ['status' => 1]);

// 2.8 Kiểm tra bảo toàn dữ liệu: Loại sản phẩm đang có sản phẩm không được xóa cứng
$rDelCat = request('DELETE', "$baseUrl?resource=categories&id=$testCatId");
assertTest("Hệ thống: Không cho xóa loại sản phẩm đang chứa sản phẩm", 
    isset($rDelCat['json']['success']) && $rDelCat['json']['success'] === false && str_contains($rDelCat['json']['message'], 'đang chứa sản phẩm'),
    $rDelCat['raw']
);

echo "\n";

// =====================================================================
// PHẦN 3: QUẢN LÝ NHÀ CUNG CẤP & ĐIỀU KIỆN (SUPPLIERS - UC13, UC14, UC15)
// =====================================================================
echo "--- 3. KIỂM THỬ NHÀ CUNG CẤP & ĐIỀU KIỆN RÀNG BUỘC (UC13, UC14, UC15) ---\n";

// 3.1 NCC: Tên chứa ký tự đặc biệt
$r = request('POST', "$baseUrl?resource=suppliers", [
    'supplierName' => 'Nông Trại Xanh @#$!',
    'phone' => '0988776655',
    'email' => 'nongtrai@gmail.com',
    'address' => 'Đà Lạt'
]);
assertTest("NCC: Tên chứa ký tự đặc biệt bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'ký tự đặc biệt'),
    $r['raw']
);

// 3.2 NCC: Thiếu số điện thoại
$r = request('POST', "$baseUrl?resource=suppliers", [
    'supplierName' => 'Nông Trại Xanh Test ' . time(),
    'phone' => '',
    'email' => 'nongtrai@gmail.com',
    'address' => 'Đà Lạt'
]);
assertTest("NCC: Để trống SĐT bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'không được để trống'),
    $r['raw']
);

// 3.3 NCC: Email không phải @gmail.com
$r = request('POST', "$baseUrl?resource=suppliers", [
    'supplierName' => 'Nông Trại Xanh Test ' . time(),
    'phone' => '0988776655',
    'email' => 'nongtrai@coop.vn',
    'address' => 'Đà Lạt'
]);
assertTest("NCC: Email không có đuôi @gmail.com bị từ chối", 
    isset($r['json']['success']) && $r['json']['success'] === false && str_contains($r['json']['message'], 'gmail.com'),
    $r['raw']
);

// 3.4 Thêm NCC hợp lệ
$rSup = request('POST', "$baseUrl?resource=suppliers", [
    'supplierName' => 'Nông Trại Hữu Cơ Đà Lạt ' . time(),
    'phone' => '0988776655',
    'email' => 'nongtraidalat' . time() . '@gmail.com',
    'address' => 'Thành phố Đà Lạt, Tỉnh Lâm Đồng'
]);
assertTest("NCC: Thêm nhà cung cấp hợp lệ thành công", 
    isset($rSup['json']['success']) && $rSup['json']['success'] === true,
    $rSup['raw']
);
$testSupId = $rSup['json']['data']['supplierId'] ?? null;

// Gán sản phẩm với NCC này để test bảo toàn khóa ngoại
$pdo->exec("UPDATE SanPham SET MaNCC = $testSupId WHERE MaSP = $testProductId");

// 3.5 Không cho xóa NCC khi đang có sản phẩm liên kết
$rDelSup = request('DELETE', "$baseUrl?resource=suppliers&id=$testSupId");
assertTest("Hệ thống: Không cho xóa Nhà cung cấp đang có sản phẩm liên kết", 
    isset($rDelSup['json']['success']) && $rDelSup['json']['success'] === false && str_contains($rDelSup['json']['message'], 'đang có sản phẩm liên kết'),
    $rDelSup['raw']
);

echo "\n";

// =====================================================================
// PHẦN 3B: QUẢN LÝ TÀI KHOẢN, KHÁCH HÀNG & NHÂN VIÊN (UC01 -> UC09)
// =====================================================================
echo "--- 3B. KIỂM THỬ QUẢN LÝ ADMIN: TÀI KHOẢN, KHÁCH HÀNG & NHÂN VIÊN (UC01-UC09) ---\n";

// UC01: Thêm tài khoản qua Admin
$testAdminAccUser = 'accadm' . rand(10000, 99999);
$rAccAdd = request('POST', "$baseUrl?resource=accounts", [
    'username' => $testAdminAccUser,
    'password' => 'Acc@123456',
    'role' => 'NhanVien',
    'status' => 1
]);
assertTest("Admin UC01: Thêm tài khoản nhân viên thành công", 
    isset($rAccAdd['json']['success']) && $rAccAdd['json']['success'] === true,
    $rAccAdd['raw']
);
$testAccId = $rAccAdd['json']['data']['accountId'] ?? null;

// UC02: Sửa tài khoản (đổi vai trò, trạng thái)
$rAccEdit = request('PUT', "$baseUrl?resource=accounts&id=$testAccId", [
    'role' => 'QuanLy',
    'status' => 1
]);
assertTest("Admin UC02: Sửa thông tin tài khoản thành công", 
    isset($rAccEdit['json']['success']) && $rAccEdit['json']['success'] === true,
    $rAccEdit['raw']
);

// UC03: Không cho xóa tài khoản admin chính
$rDelAdmin = request('DELETE', "$baseUrl?resource=accounts&id=1");
assertTest("Admin UC03: Chặn xóa tài khoản admin gốc/chính", 
    isset($rDelAdmin['json']['success']) && $rDelAdmin['json']['success'] === false && str_contains($rDelAdmin['json']['message'], 'admin chính'),
    $rDelAdmin['raw']
);

// UC03: Xóa tài khoản vừa tạo
$rDelAcc = request('DELETE', "$baseUrl?resource=accounts&id=$testAccId");
assertTest("Admin UC03: Xóa tài khoản hợp lệ thành công", 
    isset($rDelAcc['json']['success']) && $rDelAcc['json']['success'] === true,
    $rDelAcc['raw']
);

// UC04: Thêm khách hàng mới bởi Admin
$testAdmCustUser = 'admcust' . rand(10000, 99999);
$rAdmCustAdd = request('POST', "$baseUrl?resource=customers", [
    'username' => $testAdmCustUser,
    'fullName' => 'Phạm Văn Minh',
    'phone' => '0933221100',
    'email' => 'phamvanminh' . time() . '@gmail.com',
    'address' => '789 Đường 3 Tháng 2, Quận 10',
    'password' => 'Cust@123'
]);
assertTest("Admin UC04: Thêm khách hàng mới thành công", 
    isset($rAdmCustAdd['json']['success']) && $rAdmCustAdd['json']['success'] === true,
    $rAdmCustAdd['raw']
);
$admCustId = $rAdmCustAdd['json']['data']['customerId'] ?? null;

// UC05: Sửa thông tin khách hàng
$rAdmCustEdit = request('PUT', "$baseUrl?resource=customers&id=$admCustId", [
    'fullName' => 'Phạm Văn Minh Tiến',
    'phone' => '0933221199',
    'email' => 'phamvanminh' . time() . '@gmail.com',
    'address' => '789B Đường 3 Tháng 2, Quận 10'
]);
assertTest("Admin UC05: Sửa thông tin khách hàng thành công", 
    isset($rAdmCustEdit['json']['success']) && $rAdmCustEdit['json']['success'] === true,
    $rAdmCustEdit['raw']
);

// UC06: Chặn xóa khách hàng nếu đang có đơn hàng chưa hoàn tất
$pdo->exec("INSERT INTO DonHang (MaKH, NgayDat, HinhThuc, TrangThai, PhuongThucTT, DiaChiGiao, TongTien) VALUES ($admCustId, NOW(), 'Online', 'ChoXacNhan', 'COD', 'Quan 10', 0)");
$pendingOrdId = intval($pdo->lastInsertId());
$rDelCustBlocked = request('DELETE', "$baseUrl?resource=customers&id=$admCustId");
assertTest("Admin UC06: Chặn xóa khách hàng đang có đơn hàng chưa hoàn tất", 
    isset($rDelCustBlocked['json']['success']) && $rDelCustBlocked['json']['success'] === false && str_contains($rDelCustBlocked['json']['message'], 'chưa hoàn tất'),
    $rDelCustBlocked['raw']
);
// Xóa đơn chờ để xóa khách hàng hợp lệ
$pdo->exec("DELETE FROM DonHang WHERE MaDH = $pendingOrdId");
$rDelCustOk = request('DELETE', "$baseUrl?resource=customers&id=$admCustId");
assertTest("Admin UC06: Xóa khách hàng thành công khi không còn đơn dở dang", 
    isset($rDelCustOk['json']['success']) && $rDelCustOk['json']['success'] === true,
    $rDelCustOk['raw']
);

// UC07: Thêm nhân viên mới bởi Admin
$testAdmEmpUser = 'admemp' . rand(10000, 99999);
$rAdmEmpAdd = request('POST', "$baseUrl?resource=employees", [
    'username' => $testAdmEmpUser,
    'fullName' => 'Lê Hoàng Nam',
    'role' => 'NhanVien',
    'phone' => '0912889900',
    'gender' => 'Nam',
    'birthDate' => '1995-05-15',
    'hireDate' => '2023-01-10',
    'address' => '321 Điện Biên Phủ, Bình Thạnh',
    'password' => 'Emp@123'
]);
assertTest("Admin UC07: Thêm nhân viên mới thành công", 
    isset($rAdmEmpAdd['json']['success']) && $rAdmEmpAdd['json']['success'] === true,
    $rAdmEmpAdd['raw']
);
$admEmpId = $rAdmEmpAdd['json']['data']['employeeId'] ?? null;

// UC08: Sửa thông tin nhân viên
$rAdmEmpEdit = request('PUT', "$baseUrl?resource=employees&id=$admEmpId", [
    'fullName' => 'Lê Hoàng Nam Trưởng Ca',
    'role' => 'QuanLy',
    'phone' => '0912889911',
    'gender' => 'Nam',
    'address' => '321A Điện Biên Phủ, Bình Thạnh'
]);
assertTest("Admin UC08: Sửa thông tin và nâng chức vụ nhân viên thành công", 
    isset($rAdmEmpEdit['json']['success']) && $rAdmEmpEdit['json']['success'] === true,
    $rAdmEmpEdit['raw']
);

// UC09: Chặn admin tự xóa chính mình
$rDelSelfBlocked = request('DELETE', "$baseUrl?resource=employees&id=$admEmpId&currentAccountId=$admEmpId");
assertTest("Admin UC09: Chặn nhân viên/Admin tự xóa tài khoản của chính mình", 
    isset($rDelSelfBlocked['json']['success']) && $rDelSelfBlocked['json']['success'] === false && str_contains($rDelSelfBlocked['json']['message'], 'tự xóa tài khoản của chính mình'),
    $rDelSelfBlocked['raw']
);

// UC09: Xóa nhân viên bởi người khác
$rDelEmpOk = request('DELETE', "$baseUrl?resource=employees&id=$admEmpId&currentAccountId=1");
assertTest("Admin UC09: Xóa nhân viên thành công", 
    isset($rDelEmpOk['json']['success']) && $rDelEmpOk['json']['success'] === true,
    $rDelEmpOk['raw']
);

echo "\n";

// =====================================================================
// PHẦN 4: GIỎ HÀNG, CHECKOUT ONLINE, TRIGGER BÁN HÀNG & GIAO DỊCH
// =====================================================================
echo "--- 4. KIỂM THỬ GIỎ HÀNG, ĐẶT HÀNG ONLINE, TRIGGER TỰ ĐỘNG CẬP NHẬT KHO & TỔNG TIỀN ---\n";

// Lấy tồn kho hiện tại của $testProductId
$initStock = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());

// 4.1 Thêm vào giỏ vượt quá tồn kho hiện tại
$rCartOver = request('POST', "$baseUrl?resource=cart", [
    'customerId' => $createdCustId,
    'productId' => $testProductId,
    'quantity' => $initStock + 100
]);
assertTest("Giỏ hàng: Thêm số lượng vượt quá tồn kho bị từ chối", 
    isset($rCartOver['json']['success']) && $rCartOver['json']['success'] === false && str_contains($rCartOver['json']['message'], 'tồn kho không đủ'),
    $rCartOver['raw']
);

// 4.2 Thêm vào giỏ hợp lệ (số lượng: 5 kg)
$buyQty = 5.0;
$rCartOk = request('POST', "$baseUrl?resource=cart", [
    'customerId' => $createdCustId,
    'productId' => $testProductId,
    'quantity' => $buyQty
]);
assertTest("Giỏ hàng: Thêm sản phẩm hợp lệ thành công", 
    isset($rCartOk['json']['success']) && $rCartOk['json']['success'] === true,
    $rCartOk['raw']
);

// 4.3 Xem giỏ hàng & kiểm tra tính toán tổng tiền giỏ
$rCartGet = request('GET', "$baseUrl?resource=cart&customerId=$createdCustId");
$spPrice = floatval($pdo->query("SELECT GiaBan FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
$expectedCartTotal = $buyQty * $spPrice;
$actualCartTotal = floatval($rCartGet['json']['data']['totalAmount'] ?? 0);
assertTest("Giỏ hàng: Tổng tiền giỏ hàng tính đúng theo Giá bán * Số lượng", 
    abs($expectedCartTotal - $actualCartTotal) < 0.01,
    "Expected: $expectedCartTotal, Got: $actualCartTotal"
);

// 4.4 Checkout đặt hàng Online: Thiếu địa chỉ giao hàng
$rCheckoutNoAddr = request('POST', "$baseUrl?resource=orders&action=checkout", [
    'customerId' => $createdCustId,
    'shippingAddress' => '',
    'paymentMethod' => 'COD'
]);
assertTest("Đặt hàng Online: Thiếu địa chỉ giao hàng bị từ chối", 
    isset($rCheckoutNoAddr['json']['success']) && $rCheckoutNoAddr['json']['success'] === false && str_contains($rCheckoutNoAddr['json']['message'], 'địa chỉ giao hàng'),
    $rCheckoutNoAddr['raw']
);

// 4.5 Checkout đặt hàng Online hợp lệ
$rCheckout = request('POST', "$baseUrl?resource=orders&action=checkout", [
    'customerId' => $createdCustId,
    'shippingAddress' => '456 Lê Lợi, Phường Bến Thành, Quận 1',
    'paymentMethod' => 'COD',
    'note' => 'Giao hàng buổi chiều'
]);
assertTest("Đặt hàng Online: Đặt hàng thành công", 
    isset($rCheckout['json']['success']) && $rCheckout['json']['success'] === true,
    $rCheckout['raw']
);
$createdOrderId = $rCheckout['json']['data']['MaDH'] ?? ($rCheckout['json']['data']['orderId'] ?? null);

// 4.6 Kiểm tra Trigger trg_ban_hang: Tự động trừ tồn kho SanPham.SoLuongTon
$postOrderStock = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
assertTest("Database Trigger: 'trg_ban_hang' tự động trừ chính xác tồn kho (-$buyQty)", 
    abs($postOrderStock - ($initStock - $buyQty)) < 0.01,
    "Ban đầu: $initStock, Sau đặt: $postOrderStock, Mua: $buyQty"
);

// 4.7 Kiểm tra Trigger trg_ban_hang: Tự động tính DonHang.TongTien
$orderTotalInDb = floatval($pdo->query("SELECT TongTien FROM DonHang WHERE MaDH = $createdOrderId")->fetchColumn());
assertTest("Database Trigger: 'trg_ban_hang' tự động tính chính xác tổng tiền đơn hàng", 
    abs($orderTotalInDb - $expectedCartTotal) < 0.01,
    "Expected: $expectedCartTotal, Db: $orderTotalInDb"
);

// 4.8 Kiểm tra giỏ hàng được làm sạch sau khi checkout
$cartItemCount = intval($pdo->query("SELECT COUNT(*) FROM ChiTietGioHang WHERE MaGH IN (SELECT MaGH FROM GioHang WHERE MaKH = $createdCustId)")->fetchColumn());
assertTest("Hệ thống: Giỏ hàng được xóa sạch (ChiTietGioHang = 0) sau khi checkout thành công", 
    $cartItemCount === 0,
    "Cart item count: $cartItemCount"
);

echo "\n";

// =====================================================================
// PHẦN 5: LUỒNG TRẠNG THÁI ĐƠN HÀNG & CƠ CHẾ HOÀN KHO (ORDER STATE MACHINE)
// =====================================================================
echo "--- 5. KIỂM THỬ LUỒNG CHUYỂN TRẠNG THÁI & CƠ CHẾ HOÀN KHO KHI HỦY ĐƠN ---\n";

// Đơn hàng $createdOrderId đang ở ChoXacNhan
// 5.1 Chuyển trạng thái: ChoXacNhan -> DangGiao
$rConfirm = request('PUT', "$baseUrl?resource=orders&id=$createdOrderId&action=confirm");
$orderStatus1 = $pdo->query("SELECT TrangThai FROM DonHang WHERE MaDH = $createdOrderId")->fetchColumn();
assertTest("Luồng đơn hàng: Chuyển ChoXacNhan -> DangGiao thành công", 
    $orderStatus1 === 'DangGiao',
    "Status: $orderStatus1"
);

// 5.2 Hủy đơn hàng khi đang DangGiao -> Kiểm tra hoàn lại tồn kho SanPham (Quy tắc nghiệp vụ 5)
$stockBeforeCancel = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
$rCancel = request('PUT', "$baseUrl?resource=orders&id=$createdOrderId&action=cancel");
$orderStatusCancelled = $pdo->query("SELECT TrangThai FROM DonHang WHERE MaDH = $createdOrderId")->fetchColumn();
$stockAfterCancel = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());

assertTest("Luồng đơn hàng: Hủy đơn hàng chuyển sang trạng thái 'DaHuy'", 
    $orderStatusCancelled === 'DaHuy',
    "Status: $orderStatusCancelled"
);

assertTest("Nghiệp vụ Observer/StockRestore: Tự động hoàn lại tồn kho chính xác (+$buyQty)", 
    abs($stockAfterCancel - ($stockBeforeCancel + $buyQty)) < 0.01,
    "Trước hủy: $stockBeforeCancel, Sau hủy: $stockAfterCancel, Đã hoàn: " . ($stockAfterCancel - $stockBeforeCancel)
);

// 5.3 Không được phép hủy đơn hàng đã ở trạng thái DaHuy lần nữa
$rCancelAgain = request('PUT', "$baseUrl?resource=orders&id=$createdOrderId&action=cancel");
assertTest("Luồng đơn hàng: Chặn hủy lại đơn hàng đã DaHuy", 
    isset($rCancelAgain['json']['success']) && $rCancelAgain['json']['success'] === false,
    $rCancelAgain['raw']
);

// 5.4 Tạo một đơn hàng mới để test chuyển sang Hoàn thành (HoanThanh)
$pdo->beginTransaction();
$pdo->exec("INSERT INTO DonHang (MaKH, NgayDat, HinhThuc, TrangThai, PhuongThucTT, DiaChiGiao, TongTien) VALUES ($createdCustId, NOW(), 'Online', 'ChoXacNhan', 'COD', 'Quan 1', 0)");
$newOrdId = intval($pdo->lastInsertId());
$pdo->exec("INSERT INTO ChiTietDonHang (MaDH, MaSP, SoLuong, DonGia) VALUES ($newOrdId, $testProductId, 2.0, 48000)");
$pdo->commit();

// Xác nhận -> Đang giao
request('PUT', "$baseUrl?resource=orders&id=$newOrdId&action=confirm");
// Hoàn thành đơn hàng
$rComp = request('PUT', "$baseUrl?resource=orders&id=$newOrdId&action=complete");
$statusComp = $pdo->query("SELECT TrangThai FROM DonHang WHERE MaDH = $newOrdId")->fetchColumn();
assertTest("Luồng đơn hàng: Chuyển DangGiao -> HoanThanh thành công", 
    $statusComp === 'HoanThanh',
    "Status: $statusComp"
);

// 5.5 Đơn đã HoanThanh không được phép hủy (State machine constraint)
$rCancelComp = request('PUT', "$baseUrl?resource=orders&id=$newOrdId&action=cancel");
assertTest("Luồng đơn hàng: Chặn hủy đơn hàng đã hoàn tất (HoanThanh)", 
    isset($rCancelComp['json']['success']) && $rCancelComp['json']['success'] === false,
    $rCancelComp['raw']
);

echo "\n";

// =====================================================================
// PHẦN 6: BÁN HÀNG TẠI QUẦY (POS) & RÀNG BUỘC THANH TOÁN
// =====================================================================
echo "--- 6. KIỂM THỬ BÁN HÀNG TẠI QUẦY (POS) & RÀNG BUỘC THANH TOÁN ---\n";

$currentStockPos = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
$posQty = 3.0;
$posSubtotal = $posQty * 48000; // 144.000 VNĐ

// 6.1 POS: Tiền mặt đưa <= 1.000 VNĐ
$rPosLow = request('POST', "$baseUrl?resource=sales", [
    'employeeId' => 1,
    'paymentMethod' => 'TienMat',
    'cashReceived' => 1000,
    'items' => [['productId' => $testProductId, 'quantity' => $posQty]]
]);
assertTest("POS: Tiền khách đưa <= 1.000 VNĐ bị từ chối", 
    isset($rPosLow['json']['success']) && $rPosLow['json']['success'] === false && str_contains($rPosLow['json']['message'], '1.000 VNĐ'),
    $rPosLow['raw']
);

// 6.2 POS: Tiền khách đưa < tổng tiền phải trả (144.000 VNĐ)
$rPosNotEnough = request('POST', "$baseUrl?resource=sales", [
    'employeeId' => 1,
    'paymentMethod' => 'TienMat',
    'cashReceived' => 100000,
    'items' => [['productId' => $testProductId, 'quantity' => $posQty]]
]);
assertTest("POS: Tiền khách đưa không đủ so với tổng hóa đơn bị từ chối", 
    isset($rPosNotEnough['json']['success']) && $rPosNotEnough['json']['success'] === false && str_contains($rPosNotEnough['json']['message'], 'chưa đủ để thanh toán'),
    $rPosNotEnough['raw']
);

// 6.3 POS: Thanh toán thành công tiền mặt đủ hoặc dư (khách đưa 200.000đ)
$rPosOk = request('POST', "$baseUrl?resource=sales", [
    'employeeId' => 1,
    'paymentMethod' => 'TienMat',
    'cashReceived' => 200000,
    'items' => [['productId' => $testProductId, 'quantity' => $posQty]]
]);
assertTest("POS: Lập hóa đơn bán tại quầy thành công", 
    isset($rPosOk['json']['success']) && $rPosOk['json']['success'] === true,
    $rPosOk['raw']
);
$posOrderId = $rPosOk['json']['data']['orderId'] ?? null;

// Kiểm tra hóa đơn POS: HinhThuc = TaiQuay, TrangThai = HoanThanh
$posOrdRow = $pdo->query("SELECT HinhThuc, TrangThai, TongTien FROM DonHang WHERE MaDH = $posOrderId")->fetch();
assertTest("POS: Đơn hàng tự động lưu đúng HinhThuc = TaiQuay và TrangThai = HoanThanh", 
    $posOrdRow['HinhThuc'] === 'TaiQuay' && $posOrdRow['TrangThai'] === 'HoanThanh',
    "HinhThuc: {$posOrdRow['HinhThuc']}, TrangThai: {$posOrdRow['TrangThai']}"
);

// Kiểm tra trừ kho POS qua trigger trg_ban_hang
$stockAfterPos = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
assertTest("POS Trigger: Kho trừ chính xác sau khi bán tại quầy (-$posQty)", 
    abs($stockAfterPos - ($currentStockPos - $posQty)) < 0.01,
    "Trước POS: $currentStockPos, Sau POS: $stockAfterPos"
);

echo "\n";

// =====================================================================
// PHẦN 7: NHẬP HÀNG & TRIGGER CỘNG KHO (PURCHASE RECEIPTS)
// =====================================================================
echo "--- 7. KIỂM THỬ NHẬP HÀNG (PHIEUNHAP) & TRIGGER TỰ ĐỘNG CỘNG KHO ---\n";

$stockBeforeImport = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
$importQty = 25.0;
$importPrice = 30000.0;

// 7.1 Lập phiếu nhập hàng
$rImport = request('POST', "$baseUrl?resource=purchase-receipts", [
    'supplierId' => $testSupId,
    'employeeId' => 1,
    'items' => [
        ['productId' => $testProductId, 'quantity' => $importQty, 'importPrice' => $importPrice]
    ]
]);
assertTest("Nhập kho: Lập phiếu nhập hàng thành công", 
    isset($rImport['json']['success']) && $rImport['json']['success'] === true,
    $rImport['raw']
);
$receiptId = $rImport['json']['data']['receiptId'] ?? null;

// 7.2 Kiểm tra Trigger trg_nhap_hang: Tự động cộng tồn kho
$stockAfterImport = floatval($pdo->query("SELECT SoLuongTon FROM SanPham WHERE MaSP = $testProductId")->fetchColumn());
assertTest("Database Trigger: 'trg_nhap_hang' tự động cộng chính xác tồn kho (+$importQty)", 
    abs($stockAfterImport - ($stockBeforeImport + $importQty)) < 0.01,
    "Trước nhập: $stockBeforeImport, Sau nhập: $stockAfterImport"
);

// 7.3 Kiểm tra Trigger trg_nhap_hang: Tự động tính tổng tiền phiếu nhập
$receiptTotal = floatval($pdo->query("SELECT TongTien FROM PhieuNhap WHERE MaPN = $receiptId")->fetchColumn());
$expectedReceiptTotal = $importQty * $importPrice;
assertTest("Database Trigger: 'trg_nhap_hang' tự động tính chính xác tổng tiền phiếu nhập", 
    abs($receiptTotal - $expectedReceiptTotal) < 0.01,
    "Expected: $expectedReceiptTotal, Got: $receiptTotal"
);

// 7.4 Kiểm tra ràng buộc bảo toàn: Sản phẩm đã có trong phiếu nhập không được xóa cứng
$rDelSpImported = request('DELETE', "$baseUrl?resource=products&id=$testProductId");
assertTest("Hệ thống: Không cho xóa sản phẩm đã phát sinh phiếu nhập hoặc đơn hàng (bảo toàn toàn vẹn)", 
    $rDelSpImported['code'] === 400 && isset($rDelSpImported['json']['success']) && $rDelSpImported['json']['success'] === false && str_contains($rDelSpImported['json']['message'], 'Không thể xóa sản phẩm đã phát sinh'),
    $rDelSpImported['raw']
);

echo "\n";

// =====================================================================
// PHẦN 8: BÁO CÁO & THỐNG KÊ (REPORTS & 4 VIEWS)
// =====================================================================
echo "--- 8. KIỂM THỬ 4 VIEW BÁO CÁO & THỐNG KÊ (REPORTS) ---\n";

// 8.1 Báo cáo doanh thu theo ngày (v_doanh_thu_ngay)
$rRepDaily = request('GET', "$baseUrl?resource=reports&action=revenue-daily");
assertTest("Báo cáo: API revenue-daily trả về danh sách từ view v_doanh_thu_ngay", 
    isset($rRepDaily['json']['success']) && $rRepDaily['json']['success'] === true && is_array($rRepDaily['json']['data']),
    "Count: " . count($rRepDaily['json']['data'] ?? [])
);

// 8.2 Báo cáo doanh thu theo tháng (v_doanh_thu_thang)
$rRepMonthly = request('GET', "$baseUrl?resource=reports&action=revenue-monthly");
assertTest("Báo cáo: API revenue-monthly trả về danh sách từ view v_doanh_thu_thang", 
    isset($rRepMonthly['json']['success']) && $rRepMonthly['json']['success'] === true && is_array($rRepMonthly['json']['data']),
    "Count: " . count($rRepMonthly['json']['data'] ?? [])
);

// 8.3 Báo cáo sản phẩm bán chạy (v_sp_ban_chay)
$rRepBest = request('GET', "$baseUrl?resource=reports&action=best-sellers");
assertTest("Báo cáo: API best-sellers trả về danh sách từ view v_sp_ban_chay", 
    isset($rRepBest['json']['success']) && $rRepBest['json']['success'] === true && is_array($rRepBest['json']['data']),
    "Count: " . count($rRepBest['json']['data'] ?? [])
);

// 8.4 Kiểm tra View cảnh báo tồn kho thấp (v_ton_kho_thap) trực tiếp trên DB
$lowStockItems = $pdo->query("SELECT * FROM v_ton_kho_thap")->fetchAll();
assertTest("Báo cáo: View v_ton_kho_thap hoạt động chính xác (chỉ lấy SP có tồn kho <= 10)", 
    is_array($lowStockItems),
    "Items count: " . count($lowStockItems)
);

// 8.5 KPI Dashboard
$rKpi = request('GET', "$baseUrl?resource=reports");
assertTest("Báo cáo: KPI Dashboard tính toán tổng quan chính xác", 
    isset($rKpi['json']['data']['totalRevenue']) && isset($rKpi['json']['data']['totalOrders']),
    json_encode($rKpi['json']['data'] ?? [])
);

echo "\n";

// =====================================================================
// DỌN DẸP DỮ LIỆU TEST (CLEANUP)
// =====================================================================
echo "--- DỌN DẸP DỮ LIỆU TEST ---\n";
try {
    // Xóa chi tiết và phiếu nhập test
    if ($receiptId) {
        $pdo->exec("DELETE FROM ChiTietPhieuNhap WHERE MaPN = $receiptId");
        $pdo->exec("DELETE FROM PhieuNhap WHERE MaPN = $receiptId");
    }
    // Xóa chi tiết và đơn hàng test
    if ($createdOrderId) {
        $pdo->exec("DELETE FROM ChiTietDonHang WHERE MaDH = $createdOrderId");
        $pdo->exec("DELETE FROM DonHang WHERE MaDH = $createdOrderId");
    }
    if (isset($newOrdId)) {
        $pdo->exec("DELETE FROM ChiTietDonHang WHERE MaDH = $newOrdId");
        $pdo->exec("DELETE FROM DonHang WHERE MaDH = $newOrdId");
    }
    if ($posOrderId) {
        $pdo->exec("DELETE FROM ChiTietDonHang WHERE MaDH = $posOrderId");
        $pdo->exec("DELETE FROM DonHang WHERE MaDH = $posOrderId");
    }
    // Xóa giỏ hàng & tài khoản khách hàng test
    if ($createdCustId) {
        $pdo->exec("DELETE FROM ChiTietGioHang WHERE MaGH IN (SELECT MaGH FROM GioHang WHERE MaKH = $createdCustId)");
        $pdo->exec("DELETE FROM GioHang WHERE MaKH = $createdCustId");
        $pdo->exec("DELETE FROM KhachHang WHERE MaKH = $createdCustId");
    }
    // Xóa sản phẩm test
    if ($testProductId) {
        $pdo->exec("DELETE FROM SanPham WHERE MaSP = $testProductId");
    }
    // Xóa nhà cung cấp test
    if ($testSupId) {
        $pdo->exec("DELETE FROM NhaCungCap WHERE MaNCC = $testSupId");
    }
    // Xóa loại sản phẩm test
    if ($testCatId) {
        $pdo->exec("DELETE FROM LoaiSanPham WHERE MaLoai = $testCatId");
    }
    echo "  [DONE] Đã dọn dẹp sạch sẽ dữ liệu phát sinh trong quá trình kiểm thử!\n";
} catch (Exception $e) {
    echo "  [WARNING] Lỗi dọn dẹp: " . $e->getMessage() . "\n";
}

echo "\n====================================================================\n";
echo "                   TỔNG KẾT KẾT QUẢ KIỂM THỬ                        \n";
echo "====================================================================\n";
echo "TỔNG SỐ BÀI KIỂM THỬ : $totalTests\n";
echo "THÀNH CÔNG (PASSED)  : $passedTests\n";
echo "THẤT BẠI (FAILED)    : $failedTests\n";
$rate = round(($passedTests / max(1, $totalTests)) * 100, 2);
echo "TỶ LỆ ĐẠT (RATE)     : $rate%\n";
echo "====================================================================\n";
