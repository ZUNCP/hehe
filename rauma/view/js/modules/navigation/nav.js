/* ==========================================================================
   MODULE: NAVIGATION & ROLE SWITCHING (Điều hướng trang & phân quyền)
   ========================================================================== */

/* ==========================================================================
   NAVIGATION & ROLE SWITCHING (FULL 3 ROLES)
   ========================================================================== */
function isCustomerShopPage(pageId) {
    return pageId === 'home' || pageId === 'my-orders' || pageId === 'hot-deals';
}

function switchPage(pageId) {
    if (currentUser.isLoggedIn && currentUser.role === 'QuanLy' && isCustomerShopPage(pageId)) {
        pageId = 'reports';
    } else if (currentUser.isLoggedIn && currentUser.role === 'NhanVien' && isCustomerShopPage(pageId)) {
        pageId = 'orders';
    }

    document.querySelectorAll('.page-view').forEach(p => p.classList.remove('active-view'));
    document.querySelectorAll('.subnav-link').forEach(btn => btn.classList.remove('active'));

    const targetPage = document.getElementById(`page-${pageId}`);
    if (targetPage) {
        targetPage.classList.add('active-view');
    }

    const activeNavBtn = document.querySelector(`.subnav-link[data-page="${pageId}"]`);
    if (activeNavBtn) {
        activeNavBtn.classList.add('active');
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Page-specific loaders
    if (pageId === 'orders') loadOrdersList();
    if (pageId === 'pos') initPos();
    if (pageId === 'products') loadProductsMgmt();
    if (pageId === 'suppliers') loadSuppliersMgmt();
    if (pageId === 'customers') loadCustomersMgmt();
    if (pageId === 'employees') loadEmployeesMgmt();
    if (pageId === 'reports') loadReportsData();
    if (pageId === 'my-orders') loadMyOrders();
}

function quickSwitchRole(role) {
    // Legacy function - kept for compatibility but Test Role bar is removed per UC spec
    console.warn('quickSwitchRole: Test Role switcher removed. Use login instead.');
}

function updateUserUI() {
    const isLogged = currentUser.isLoggedIn;
    const role = currentUser.role;

    // Header buttons
    const btnLogin = document.getElementById('btn-open-login');
    const pillUser = document.getElementById('user-logged-pill');
    const nameLabel = document.getElementById('user-display-name');
    const roleLabel = document.getElementById('user-display-role');
    const avatarText = document.getElementById('user-avatar-text');

    if (isLogged) {
        btnLogin.classList.add('hidden');
        pillUser.classList.remove('hidden');
        nameLabel.textContent = currentUser.fullName || currentUser.username;
        roleLabel.textContent = role === 'QuanLy' ? 'Quản Lý' : (role === 'NhanVien' ? 'Nhân Viên' : 'Khách Hàng');
        avatarText.textContent = (currentUser.fullName || currentUser.username).charAt(0).toUpperCase();
    } else {
        btnLogin.classList.remove('hidden');
        pillUser.classList.add('hidden');
    }

    // Role-based navigation buttons
    const staffBtns = document.querySelectorAll('.staff-only');
    const adminBtns = document.querySelectorAll('.admin-only');
    const myOrdersBtn = document.getElementById('nav-my-orders');
    const navHome = document.getElementById('nav-home');
    const navPos = document.getElementById('nav-pos');
    const headerCart = document.getElementById('header-cart-btn');
    const headerSearch = document.querySelector('.header-search');
    const locationBtn = document.querySelector('.location-btn');
    const isManager = isLogged && role === 'QuanLy';
    const isStaffOrManager = isLogged && (role === 'QuanLy' || role === 'NhanVien');

    if (role === 'QuanLy') {
        staffBtns.forEach(el => el.classList.remove('hidden'));
        adminBtns.forEach(el => el.classList.remove('hidden'));
        if (myOrdersBtn) myOrdersBtn.classList.add('hidden');
        if (navHome) navHome.classList.add('hidden');
        if (navPos) navPos.classList.add('hidden');
    } else if (role === 'NhanVien') {
        staffBtns.forEach(el => el.classList.remove('hidden'));
        adminBtns.forEach(el => el.classList.add('hidden'));
        if (myOrdersBtn) myOrdersBtn.classList.add('hidden');
        // Role Nhân Viên: ẨN Trang Chủ
        if (navHome) navHome.classList.add('hidden');
        if (navPos) navPos.classList.remove('hidden');
    } else {
        // Khách hàng
        staffBtns.forEach(el => el.classList.add('hidden'));
        adminBtns.forEach(el => el.classList.add('hidden'));
        if (myOrdersBtn) myOrdersBtn.classList.remove('hidden');
        if (navHome) navHome.classList.remove('hidden');
        if (navPos) navPos.classList.add('hidden');
    }

    // Role Nhân Viên & Quản Lý: ẨN Giỏ Hàng
    if (headerCart) headerCart.classList.toggle('hidden', isStaffOrManager);
    if (headerSearch) headerSearch.classList.toggle('hidden', isManager);
    if (locationBtn) locationBtn.classList.toggle('hidden', isManager);

    const brandLogo = document.querySelector('.brand-logo');
    if (brandLogo) {
        if (role === 'QuanLy') {
            brandLogo.onclick = () => switchPage('reports');
        } else if (role === 'NhanVien') {
            brandLogo.onclick = () => switchPage('orders');
        } else {
            brandLogo.onclick = () => switchPage('home');
        }
    }
}
