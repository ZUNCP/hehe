/* ==========================================================================
   MODULE: HELPER UTILITIES, MODAL CONTROLLERS & TOASTS
   ========================================================================== */

/* ==========================================================================
   HELPER UTILITIES & MODAL CONTROLLERS
   ========================================================================= */
function openModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.add('active');
}

function closeModal(modalId) {
    const m = document.getElementById(modalId);
    if (m) m.classList.remove('active');
}

function openLocationModal() {
    const loc = prompt('Nhập tỉnh/thành phố hoặc khu vực giao hàng:', 'Hà Nội & lân cận');
    if (loc) {
        document.getElementById('current-location-label').textContent = loc;
        showToast(`Đã chuyển khu vực giao hàng sang: ${loc}`, 'info');
    }
}

function showStoreLocations() {
    alert('Hệ thống Bách Hóa XANH hiện có 3.904 siêu thị trên toàn quốc. Các siêu thị gần bạn luôn có sẵn rau xanh thu hoạch mới mỗi sáng!');
}

function toggleAdvancedFilterModal() {
    const pr = prompt('Lọc theo giá tối đa (VNĐ):', '30000');
    if (pr && !isNaN(pr)) {
        displayedProducts = allProducts.filter(p => parseFloat(p.price) <= parseFloat(pr));
        renderProductGrid(displayedProducts);
        document.getElementById('product-total-count').textContent = `${displayedProducts.length} sản phẩm (<= ${formatCurrency(pr)})`;
        showToast(`Đã lọc sản phẩm có giá <= ${formatCurrency(pr)}`, 'info');
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '---';
    const d = new Date(dateStr);
    return isNaN(d.getTime()) ? dateStr : `${d.toLocaleDateString('vi-VN')} ${d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}`;
}

function renderStatusText(status) {
    switch (status) {
        case 'ChoXacNhan': return 'Chờ xác nhận';
        case 'DangGiao': return 'Đang giao';
        case 'HoanThanh': return 'Hoàn thành';
        case 'DaHuy': return 'Đã hủy';
        default: return status;
    }
}

/* ==========================================================================
   CUSTOM CONFIRM DIALOG (UC03, UC06, UC09, UC12, UC15)
   ========================================================================= */
let _confirmAction = null;

function openConfirmDialog(message, onConfirm) {
    document.getElementById('confirm-message').innerHTML = message;
    _confirmAction = onConfirm;
    openModal('modal-confirm');
}

function executeConfirmAction() {
    if (typeof _confirmAction === 'function') {
        _confirmAction();
        _confirmAction = null;
    }
}

/* ==========================================================================
   SEARCH TABLE HELPER
   ========================================================================= */
function searchTable(tbodyId, query) {
    const q = query.toLowerCase();
    const rows = document.querySelectorAll(`#${tbodyId} tr`);
    rows.forEach(r => {
        r.style.display = r.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'fa-circle-info';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'error') icon = 'fa-circle-exclamation';
    if (type === 'warning') icon = 'fa-triangle-exclamation';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(12px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}
