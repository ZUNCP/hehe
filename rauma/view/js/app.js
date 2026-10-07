/* ==========================================================================
   BÁCH HÓA XANH WEB BÁN RAU SẠCH - BOOTSTRAP APPLICATION
   Tập trung điều phối và khởi tạo các module chức năng
   ========================================================================== */

document.addEventListener('DOMContentLoaded', async () => {
    // Check saved session in localStorage
    const saved = localStorage.getItem('bhx_user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
        } catch (e) {}
    }

    // Tự động chỉ cho phép nhập số và giới hạn 10 số cho các ô số điện thoại
    ['reg-phone', 'profile-phone', 'cust-phone', 'emp-phone', 'supp-phone', 'lp-reg-phone'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('input', () => {
                el.value = el.value.replace(/\D/g, '').slice(0, 10);
            });
        }
    });

    // Tự động kiểm tra và hiển thị thông báo trực quan cho tất cả các trường dữ liệu Gmail
    if (typeof setupGmailFieldNotifications === 'function') {
        setupGmailFieldNotifications();
    }

    if (!currentUser.isLoggedIn) {
        // Chưa đăng nhập: chỉ hiện login page
        showLoginPage();
        return;
    }

    // Đã đăng nhập: hiện main app và load dữ liệu
    showMainApp();
    updateUserUI();

    if (currentUser.role === 'QuanLy') {
        switchPage('reports');
        loadCategories();
        loadProducts();
    } else if (currentUser.role === 'NhanVien') {
        switchPage('orders');
        loadCategories();
        loadProducts();
    } else {
        await loadCategories();
        await loadProducts();
        await loadCart();
    }
});
