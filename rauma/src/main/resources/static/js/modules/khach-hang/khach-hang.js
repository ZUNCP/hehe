/* ==========================================================================
   MODULE: KHÁCH HÀNG (Quản lý khách hàng, mở/khóa tài khoản)
   ========================================================================== */

/* ==========================================================================
   MANAGER: QL KHÁCH HÀNG (UC13), NHÂN VIÊN (UC14), TÀI KHOẢN (UC11)
   ========================================================================= */
async function loadCustomersMgmt() {
    try {
        const res = await callApi('/customers');
        const json = await res.json();
        const tbody = document.getElementById('customers-table-body');
        if (!tbody) return;

        if (json.success && json.data) {
            tbody.innerHTML = json.data.map(c => {
                const safe = JSON.stringify(c).replace(/"/g, '&quot;');
                return `
                <tr>
                    <td><strong>#${c.customerId}</strong></td>
                    <td><strong>${c.fullName || '---'}</strong></td>
                    <td><code>${c.username || '---'}</code></td>
                    <td><code>${c.password || '---'}</code></td>
                    <td>${c.phone || '---'}</td>
                    <td>${c.email || '---'}</td>
                    <td>${c.address || '---'}</td>
                    <td><span class="badge-status active">${c.totalOrders || 0} đơn</span></td>
                    <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(c.totalSpent || 0)}</strong></td>
                    <td>
                        <div style="display: flex; gap: 4px;">
                            <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px;" onclick="openEditCustomerModal(${c.customerId}, ${safe})">Sửa</button>
                            <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px; color: var(--bhx-red); border-color: #fca5a5;" onclick="openConfirmDialog('Bạn có chắc chắn muốn xóa khách hàng &lt;strong&gt;${(c.fullName || '').replace(/'/g, '')}&lt;/strong&gt; hay không?', () => deleteCustomer(${c.customerId}))">Xóa</button>
                        </div>
                    </td>
                </tr>`;
            }).join('');
        }
    } catch (e) { console.error('Lỗi tải khách hàng:', e); }
}

function openAddCustomerModal() {
    document.getElementById('customer-form-title').textContent = 'Thêm Khách Hàng Mới';
    document.getElementById('cust-id').value = '';
    document.getElementById('form-customer').reset();
    document.getElementById('cust-username-group').style.display = 'block';
    document.getElementById('cust-username').readOnly = false;
    document.getElementById('cust-password').placeholder = 'Mặc định 123456 nếu để trống';
    document.getElementById('cust-password').value = '';
    document.getElementById('customer-form-submit-btn').textContent = 'Lưu thông tin';
    openModal('modal-customer-form');
}

function openEditCustomerModal(customerId, custObj) {
    let c = typeof custObj === 'string' ? JSON.parse(custObj.replace(/&quot;/g, '"')) : custObj;
    document.getElementById('customer-form-title').textContent = 'Cập Nhật Khách Hàng';
    document.getElementById('cust-id').value = customerId;
    document.getElementById('cust-username-group').style.display = 'block';
    document.getElementById('cust-username').readOnly = true;
    document.getElementById('cust-username').value = c.username || '';
    document.getElementById('cust-password').value = '';
    document.getElementById('cust-password').placeholder = 'Để trống nếu không đổi mật khẩu';
    document.getElementById('cust-fullname').value = c.fullName || '';
    document.getElementById('cust-phone').value = c.phone || '';
    document.getElementById('cust-email').value = c.email || '';
    document.getElementById('cust-address').value = c.address || '';
    document.getElementById('customer-form-submit-btn').textContent = 'Cập nhật thông tin';
    openModal('modal-customer-form');
}

async function handleCustomerSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('cust-id').value;
    const payload = {
        fullName: document.getElementById('cust-fullname').value.trim(),
        phone: document.getElementById('cust-phone').value.trim(),
        email: document.getElementById('cust-email').value.trim(),
        address: document.getElementById('cust-address').value.trim()
    };
    const password = document.getElementById('cust-password').value.trim();
    if (!id) {
        payload.username = document.getElementById('cust-username').value.trim();
        payload.password = password;
        const userErr = validateUsername(payload.username, true);
        if (userErr) {
            showToast(userErr, 'error');
            document.getElementById('cust-username')?.focus();
            return;
        }
    } else if (password) {
        payload.password = password;
    }

    const nameErr = validateFullName(payload.fullName, true);
    if (nameErr) {
        showToast(nameErr, 'error');
        document.getElementById('cust-fullname')?.focus();
        return;
    }

    if (payload.phone) {
        const phoneErr = validatePhone(payload.phone, false);
        if (phoneErr) { showToast(phoneErr, 'error'); return; }
    }
    if (payload.email) {
        const emailErr = validateEmail(payload.email, false);
        if (emailErr) {
            showToast(emailErr, 'error');
            document.getElementById('cust-email')?.focus();
            return;
        }
    }
    try {
        const url = id ? `/customers/${id}` : '/customers';
        const method = id ? 'PUT' : 'POST';
        const res = await callApi(url, { method, body: JSON.stringify(payload) });
        const json = await res.json();
        if (json.success) {
            closeModal('modal-customer-form');
            showToast(id ? 'Cập nhật thông tin khách hàng thành công' : 'Thêm khách hàng thành công', 'success');
            loadCustomersMgmt();
        } else { showToast(json.message || 'Thông tin không hợp lệ', 'error'); }
    } catch (err) { showToast('Lỗi lưu khách hàng', 'error'); }
}

async function deleteCustomer(id) {
    try {
        const res = await callApi(`/customers/${id}`, { method: 'DELETE' });
        const json = await res.json();
        closeModal('modal-confirm');
        if (json.success) {
            showToast('Xóa thành công', 'success');
            loadCustomersMgmt();
        } else { showToast(json.message, 'warning'); }
    } catch (e) { showToast('Lỗi xóa khách hàng', 'error'); }
}

