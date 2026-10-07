/* ==========================================================================
   MODULE: CORE STATE - BÁCH HÓA XANH WEB BÁN RAU SẠCH
   ========================================================================== */

let currentUser = {
    role: 'KhachHang',
    username: 'kh01',
    fullName: 'Lê Văn Khách',
    accountId: 3,
    customerId: 1,
    employeeId: null,
    token: null,
    isLoggedIn: false
};

let allProducts = [];
let displayedProducts = [];
let allCategories = [];
let allSuppliers = [];
let currentCart = { items: [], totalAmount: 0, itemCount: 0 };
let posCart = [];
let selectedCategoryId = null;

