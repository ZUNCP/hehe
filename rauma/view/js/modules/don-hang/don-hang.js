/* ==========================================================================
   MODULE: ĐƠN HÀNG (Thanh toán giỏ hàng, Lịch sử mua hàng, Quản lý đơn hàng)
   ========================================================================== */

/* ==========================================================================
   CHECKOUT (UC6: THANH TOÁN TỪ GIỎ HÀNG & STRATEGY PATTERN)
   ========================================================================= */
function openCheckoutModal() {
    if (!currentCart.items || currentCart.items.length === 0) {
        showToast('Giỏ hàng của bạn đang trống!', 'warning');
        return;
    }
    toggleCartDrawer(false);

    document.getElementById('checkout-subtotal-val').textContent = formatCurrency(currentCart.totalAmount);
    document.getElementById('checkout-address').value = currentUser.address || 'Số 123 Đường Cầu Giấy, Hà Nội';
    handlePaymentMethodChange('COD');
    openModal('modal-checkout');
}

// Strategy Pattern UI Helper
function handlePaymentMethodChange(method) {
    const descEl = document.getElementById('payment-strategy-desc');
    if (method === 'COD') {
        descEl.innerHTML = `<strong>COD (Thanh toán khi nhận hàng):</strong> Shipper Bách Hóa XANH sẽ giao hàng và nhận tiền mặt tận nơi. Bạn được quyền kiểm tra rau củ tươi ngon trước khi nhận.`;
    } else if (method === 'ChuyenKhoan') {
        descEl.innerHTML = `<strong>Chuyển khoản Ngân hàng (Strategy Bank Transfer):</strong><br>
        Ngân hàng: <strong>Vietcombank</strong> • Số TK: <strong>0123456789</strong><br>
        Chủ TK: <strong>CTCP NÔNG SẢN RAU SẠCH XANH</strong> • Nội dung: <strong>BHX [SDT]</strong>`;
    } else {
        descEl.innerHTML = `<strong>Tiền mặt:</strong> Thanh toán trực tiếp tại siêu thị hoặc khi nhận hàng.`;
    }
}

async function handleCheckoutSubmit(e) {
    e.preventDefault();
    const custId = currentUser.customerId || 1;
    const address = document.getElementById('checkout-address').value.trim();
    const method = document.getElementById('checkout-payment-method').value;
    const note = document.getElementById('checkout-note').value.trim();

    try {
        const res = await callApi('/orders/checkout', {
            method: 'POST',
            body: JSON.stringify({
                customerId: custId,
                shippingAddress: address,
                paymentMethod: method,
                note: note
            })
        });
        const json = await res.json();
        if (json.success) {
            closeModal('modal-checkout');
            showToast(json.message || 'Đặt hàng thành công!', 'success');
            await loadCart();
            await loadProducts(); // Refresh stock
            switchPage('my-orders');
        } else {
            showToast(json.message || 'Đặt hàng thất bại', 'error');
        }
    } catch (err) {
        showToast('Lỗi thực hiện thanh toán', 'error');
    }
}

/* ==========================================================================
   CUSTOMER ORDER HISTORY (UC4: TÀI KHOẢN & ĐƠN HÀNG CỦA TÔI)
   ========================================================================= */
async function loadMyOrders() {
    try {
        const custId = currentUser.customerId || 1;
        const res = await callApi(`/orders/my?customerId=${custId}`);
        const json = await res.json();
        const tbody = document.getElementById('my-orders-table-body');
        if (!tbody) return;

        if (!json.success || !json.data || json.data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 40px; color: var(--text-muted);">Bạn chưa có đơn hàng nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = json.data.map(ord => {
            const itemsText = ord.items ? ord.items.map(i => `${i.productName} (x${i.quantity})`).join(', ') : 'Rau sạch';
            const canCancel = ord.status === 'ChoXacNhan';

            return `
                <tr>
                    <td><strong>#${ord.orderId}</strong></td>
                    <td>${formatDate(ord.orderDate)}</td>
                    <td style="max-width: 220px; font-size: 0.85rem;">${ord.shippingAddress || 'Tại quầy'}</td>
                    <td><span class="badge-status ${ord.paymentMethod}">${ord.paymentMethod}</span></td>
                    <td style="max-width: 260px; font-size: 0.85rem;">${itemsText}</td>
                    <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(ord.totalAmount)}</strong></td>
                    <td><span class="badge-status ${ord.status}">${renderStatusText(ord.status)}</span></td>
                    <td>
                        <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px;" onclick="viewOrderDetail(${ord.orderId})">Chi tiết</button>
                        ${canCancel ? `<button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px; color: var(--bhx-red); border-color: #fca5a5; margin-left: 4px;" onclick="cancelMyOrder(${ord.orderId})">Hủy đơn</button>` : ''}
                    </td>
                </tr>
            `;
        }).join('');
    } catch (e) {
        console.error('Lỗi tải đơn hàng của tôi:', e);
    }
}

async function cancelMyOrder(orderId) {
    if (!confirm(`Bạn có chắc chắn muốn hủy đơn hàng #${orderId}? Hệ thống sẽ tự động hoàn lại tồn kho.`)) return;

    try {
        const res = await callApi(`/orders/${orderId}/cancel`, { method: 'PUT' });
        const json = await res.json();
        if (json.success) {
            showToast(json.message || 'Đã hủy đơn hàng thành công, tồn kho đã hoàn lại.', 'success');
            loadMyOrders();
            loadProducts();
        } else {
            showToast(json.message || 'Không thể hủy đơn', 'error');
        }
    } catch (e) {
        showToast('Lỗi hủy đơn hàng', 'error');
    }
}

/* ==========================================================================
   STAFF & MANAGER: QL ĐƠN HÀNG (UC7)
   ========================================================================= */
let allOrdersCache = [];

async function loadOrdersList(statusFilter = '') {
    try {
        let url = '/orders';
        if (statusFilter) url += `?status=${statusFilter}`;
        const res = await callApi(url);
        const json = await res.json();
        const tbody = document.getElementById('orders-table-body');
        if (!tbody) return;

        if (json.success && json.data) {
            allOrdersCache = json.data;
            renderOrdersTable(allOrdersCache);
        }
    } catch (e) {
        console.error('Lỗi tải danh sách đơn hàng:', e);
    }
}

function renderOrdersTable(orders) {
    const tbody = document.getElementById('orders-table-body');
    if (!tbody) return;

    if (!orders || orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 40px; color: var(--text-muted);">Không tìm thấy đơn hàng nào.</td></tr>`;
        return;
    }

    tbody.innerHTML = orders.map(ord => {
        return `
            <tr>
                <td><strong>#${ord.orderId}</strong></td>
                <td>
                    <div><strong>${ord.customerName || 'Khách vãng lai'}</strong></div>
                    <div style="font-size: 0.78rem; color: #64748b;">${ord.customerPhone || ''}</div>
                </td>
                <td><span style="font-size: 0.8rem; font-weight: 700;">${ord.orderType === 'Online' ? '🌐 Online' : '🏪 Tại Quầy'}</span></td>
                <td><span class="badge-status ${ord.paymentMethod}">${ord.paymentMethod}</span></td>
                <td>${formatDate(ord.orderDate)}</td>
                <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(ord.totalAmount)}</strong></td>
                <td><span class="badge-status ${ord.status}">${renderStatusText(ord.status)}</span></td>
                <td>
                    <div style="display: flex; gap: 4px; flex-wrap: wrap;">
                        <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px;" onclick="viewOrderDetail(${ord.orderId})">Xem</button>
                        ${ord.status === 'ChoXacNhan' ? `<button class="btn-primary" style="font-size: 0.78rem; padding: 4px 8px;" onclick="updateOrderStatus(${ord.orderId}, 'confirm')">Giao</button>` : ''}
                        ${ord.status === 'DangGiao' ? `<button class="btn-primary" style="font-size: 0.78rem; padding: 4px 8px; background: #059669;" onclick="updateOrderStatus(${ord.orderId}, 'complete')">Xong</button>` : ''}
                        ${(ord.status === 'ChoXacNhan' || ord.status === 'DangGiao') ? `<button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px; color: var(--bhx-red); border-color: #fca5a5;" onclick="updateOrderStatus(${ord.orderId}, 'cancel')">Hủy</button>` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function filterOrderStatus(status, element) {
    document.querySelectorAll('#page-orders .filter-tag-pill').forEach(b => b.classList.remove('active'));
    if (element) element.classList.add('active');
    loadOrdersList(status);
}

function searchOrdersTable(query) {
    const q = query.toLowerCase().trim();
    const filtered = allOrdersCache.filter(o => 
        String(o.orderId).includes(q) ||
        (o.customerName && o.customerName.toLowerCase().includes(q)) ||
        (o.customerPhone && o.customerPhone.includes(q))
    );
    renderOrdersTable(filtered);
}

async function updateOrderStatus(orderId, action) {
    if (action === 'cancel' && !confirm(`Xác nhận hủy đơn hàng #${orderId}? Tồn kho sẽ tự động hoàn lại.`)) {
        return;
    }

    try {
        const res = await callApi(`/orders/${orderId}/${action}`, { method: 'PUT' });
        const json = await res.json();
        if (json.success) {
            showToast(json.message, 'success');
            loadOrdersList();
            loadProducts();
        } else {
            showToast(json.message, 'error');
        }
    } catch (e) {
        showToast('Lỗi cập nhật đơn hàng', 'error');
    }
}

async function viewOrderDetail(orderId) {
    try {
        const res = await callApi(`/orders/${orderId}`);
        const json = await res.json();
        if (!json.success || !json.data) return;

        const ord = json.data;
        const ordId = ord.orderId || ord.MaDH || orderId;
        const customerName = ord.customerName || ord.HoTen || 'Khách vãng lai';
        const customerPhone = ord.customerPhone || ord.SDT || '---';
        const orderType = ord.orderType || ord.HinhThuc || 'Online';
        const status = ord.status || ord.TrangThai || '';
        const paymentMethod = ord.paymentMethod || ord.PhuongThucTT || 'COD';
        const orderDate = ord.orderDate || ord.NgayDat || '';
        const shippingAddress = ord.shippingAddress || ord.DiaChiGiao || 'Tại quầy siêu thị';
        const items = ord.items || [];

        let totalAmount = parseFloat(ord.totalAmount ?? ord.TongTien ?? 0);
        if ((!totalAmount || totalAmount <= 0) && items.length > 0) {
            totalAmount = items.reduce((sum, it) => sum + (parseFloat(it.unitPrice || 0) * parseFloat(it.quantity || 0)), 0);
        }

        document.getElementById('odetail-title').textContent = `Chi Tiết Đơn Hàng #${ordId}`;
        document.getElementById('odetail-body').innerHTML = `
            <div style="background: #f8fafc; border-radius: 8px; padding: 14px; margin-bottom: 16px;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 0.88rem;">
                    <div><strong>Khách hàng:</strong> ${customerName}</div>
                    <div><strong>Số điện thoại:</strong> ${customerPhone}</div>
                    <div><strong>Hình thức:</strong> ${orderType}</div>
                    <div><strong>Trạng thái:</strong> <span class="badge-status ${status}">${renderStatusText(status)}</span></div>
                    <div><strong>Phương thức TT:</strong> ${paymentMethod}</div>
                    <div><strong>Ngày đặt:</strong> ${formatDate(orderDate)}</div>
                    <div style="grid-column: 1/-1;"><strong>Địa chỉ giao:</strong> ${shippingAddress}</div>
                    ${ord.note || ord.GhiChu ? `<div style="grid-column: 1/-1;"><strong>Ghi chú:</strong> ${ord.note || ord.GhiChu}</div>` : ''}
                </div>
            </div>
            <h4 style="font-size: 0.95rem; font-weight: 800; margin-bottom: 10px;">Các mặt hàng đã đặt:</h4>
            <div style="display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px;">
                ${items.map(it => `
                    <div style="display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #fff; border: 1px solid var(--border-color); border-radius: 6px;">
                        <div>
                            <strong>${it.productName || it.TenSP}</strong>
                            <div style="font-size: 0.8rem; color: #64748b;">Đơn giá: ${formatCurrency(it.unitPrice || it.DonGia)} / ${it.unit || it.DonViTinh || 'kg'}</div>
                        </div>
                        <div style="text-align: right;">
                            <span style="font-weight: 700; color: #334155;">x ${it.quantity || it.SoLuong}</span>
                            <div style="font-weight: 800; color: var(--bhx-green-dark);">${formatCurrency(parseFloat(it.unitPrice || it.DonGia || 0) * parseFloat(it.quantity || it.SoLuong || 0))}</div>
                        </div>
                    </div>
                `).join('')}
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background: #f0fdf4; border-radius: 8px;">
                <span style="font-weight: 700;">Tổng thanh toán:</span>
                <strong style="font-size: 1.35rem; color: var(--bhx-green-dark);">${formatCurrency(totalAmount)}</strong>
            </div>
        `;
        openModal('modal-order-detail');
    } catch (e) {
        showToast('Lỗi xem chi tiết đơn hàng', 'error');
    }
}

