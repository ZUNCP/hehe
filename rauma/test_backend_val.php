<?php
function postJson($url, $data) {
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    $resp = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    return ['code' => $code, 'body' => $resp];
}

$base = 'http://127.0.0.1/rauma/rauma/rauma/view/api.php';

echo "=== TEST VALIDATION SYSTEM ===\n\n";

// Test 1: Tên sản phẩm chứa ký tự đặc biệt
$t1 = postJson("$base?resource=products", [
    'productName' => 'Rau Cải Xanh @#$!',
    'categoryId' => 1,
    'unit' => 'kg',
    'price' => 25000,
    'stockQuantity' => 10
]);
echo "Test 1 - Tên sản phẩm chứa ký tự đặc biệt (@#$!):\nStatus: {$t1['code']}\nResponse: {$t1['body']}\n\n";

// Test 2: Tên sản phẩm hợp lệ (tiếng Việt có dấu, số, khoảng trắng)
$t2 = postJson("$base?resource=products", [
    'productName' => 'Rau Cải Xanh VietGAP 500g',
    'categoryId' => 1,
    'unit' => 'kg',
    'price' => 25000,
    'stockQuantity' => 10
]);
echo "Test 2 - Tên sản phẩm hợp lệ (Rau Cải Xanh VietGAP 500g):\nStatus: {$t2['code']}\nResponse: {$t2['body']}\n\n";

// Test 3: Tên đăng nhập chứa ký tự đặc biệt
$t3 = postJson("$base?resource=auth&action=register", [
    'username' => 'test@user!' . time(),
    'password' => '123456',
    'fullName' => 'Nguyễn Văn Test',
    'phone' => '0912345678',
    'email' => 'testuser@gmail.com'
]);
echo "Test 3 - Tên đăng nhập chứa ký tự đặc biệt (test@user!):\nStatus: {$t3['code']}\nResponse: {$t3['body']}\n\n";

// Test 4: Họ và tên chứa ký tự đặc biệt
$t4 = postJson("$base?resource=auth&action=register", [
    'username' => 'validuser' . time(),
    'password' => '123456',
    'fullName' => 'Nguyễn Văn #A',
    'phone' => '0912345678',
    'email' => 'testuser' . time() . '@gmail.com'
]);
echo "Test 4 - Họ và tên chứa ký tự đặc biệt (Nguyễn Văn #A):\nStatus: {$t4['code']}\nResponse: {$t4['body']}\n\n";

// Test 5: Thêm nhà cung cấp thiếu SĐT
$t5 = postJson("$base?resource=suppliers", [
    'supplierName' => 'Nông Trại Test ' . time(),
    'phone' => '',
    'email' => 'nongtrai@gmail.com',
    'address' => 'Đà Lạt'
]);
echo "Test 5 - Thêm NCC thiếu SĐT:\nStatus: {$t5['code']}\nResponse: {$t5['body']}\n\n";

// Test 6: Thêm nhà cung cấp thiếu Email
$t6 = postJson("$base?resource=suppliers", [
    'supplierName' => 'Nông Trại Test ' . time(),
    'phone' => '0987654321',
    'email' => '',
    'address' => 'Đà Lạt'
]);
echo "Test 6 - Thêm NCC thiếu Email:\nStatus: {$t6['code']}\nResponse: {$t6['body']}\n\n";

// Test 7: Thêm nhà cung cấp có đủ SĐT và Email hợp lệ
$t7 = postJson("$base?resource=suppliers", [
    'supplierName' => 'Nông Trại Xanh Hợp Chuẩn ' . time(),
    'phone' => '0987654321',
    'email' => 'nongtraixanh@gmail.com',
    'address' => 'Đà Lạt, Lâm Đồng'
]);
echo "Test 7 - Thêm NCC đầy đủ SĐT và Email hợp lệ:\nStatus: {$t7['code']}\nResponse: {$t7['body']}\n\n";

// Test 8: Thêm nhà cung cấp có tên chứa ký tự đặc biệt (@#$!)
$t8 = postJson("$base?resource=suppliers", [
    'supplierName' => 'HTX Nông Nghiệp @#$ ' . time(),
    'phone' => '0987654321',
    'email' => 'htx@gmail.com',
    'address' => 'Hà Nội'
]);
echo "Test 8 - Thêm NCC tên chứa ký tự đặc biệt (@#$):\nStatus: {$t8['code']}\nResponse: {$t8['body']}\n\n";

// Test 9: Đổi mật khẩu tài khoản (sai mật khẩu cũ)
$t9 = postJson("$base?resource=accounts&action=password", [
    'username' => 'kh01',
    'oldPassword' => 'sai_mat_khau_123',
    'newPassword' => 'matkhau_moi_6789'
]);
echo "Test 9 - Đổi mật khẩu với mật khẩu cũ sai:\nStatus: {$t9['code']}\nResponse: {$t9['body']}\n\n";

// Test 10: Đổi mật khẩu tài khoản thành công
$t10 = postJson("$base?resource=accounts&action=password", [
    'username' => 'kh01',
    'oldPassword' => '123456',
    'newPassword' => 'newpassword123'
]);
echo "Test 10 - Đổi mật khẩu hợp lệ:\nStatus: {$t10['code']}\nResponse: {$t10['body']}\n\n";

// Test 11: Đăng nhập với mật khẩu mới vừa đổi
$t11 = postJson("$base?resource=auth&action=login", [
    'username' => 'kh01',
    'password' => 'newpassword123'
]);
echo "Test 11 - Đăng nhập bằng mật khẩu mới vừa đổi:\nStatus: {$t11['code']}\nResponse: {$t11['body']}\n\n";

// Test 12: Khôi phục lại mật khẩu ban đầu (123456)
$t12 = postJson("$base?resource=accounts&action=password", [
    'username' => 'kh01',
    'oldPassword' => 'newpassword123',
    'newPassword' => '123456'
]);
echo "Test 12 - Khôi phục mật khẩu 123456 cho kh01:\nStatus: {$t12['code']}\nResponse: {$t12['body']}\n\n";

// Cleanup test items
$conn = new PDO('mysql:host=localhost;dbname=web_ban_rau;charset=utf8mb4', 'root', '');
$conn->exec("DELETE FROM SanPham WHERE TenSP LIKE 'Rau Cải Xanh VietGAP 500g%'");
$conn->exec("DELETE FROM NhaCungCap WHERE TenNCC LIKE 'Nông Trại%'");
echo "=== ĐÃ DỌN DẸP DỮ LIỆU TEST XONG ===\n";
