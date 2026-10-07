/* ==========================================================================
   MODULE: NHÀ CUNG CẤP (Quản lý đối tác cung cấp rau sạch)
   ========================================================================== */

/* ==========================================================================
   MANAGER: QL NHÀ CUNG CẤP & NHẬP HÀNG (UC10)
   ========================================================================= */
function switchSupplierSubTab(tab) {
    document.getElementById('tab-btn-suppliers').classList.toggle('active', tab === 'suppliers');
    document.getElementById('tab-btn-receipts').classList.toggle('active', tab === 'receipts');
    document.getElementById('subtab-suppliers').classList.toggle('hidden', tab !== 'suppliers');
    document.getElementById('subtab-receipts').classList.toggle('hidden', tab !== 'receipts');

    if (tab === 'receipts') loadReceiptsList();
}

async function loadSuppliersMgmt() {
    try {
        const res = await callApi('/suppliers');
        const json = await res.json();
        const tbody = document.getElementById('suppliers-table-body');
        if (!tbody) return;

        if (json.success && json.data) {
            allSuppliers = json.data;
            tbody.innerHTML = allSuppliers.map(s => `
                <tr>
                    <td><strong>#${s.supplierId}</strong></td>
                    <td><strong>${s.supplierName}</strong></td>
                    <td>${s.phone || '---'}</td>
                    <td>${s.email || '---'}</td>
                    <td>${s.address || '---'}</td>
                    <td><span class="badge-status active">${s.productCount || 0} sản phẩm</span></td>
                    <td>
                        <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px;" onclick="openEditSupplierModal(${s.supplierId})">Sửa</button>
                        <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px; color: var(--bhx-red); border-color: #fca5a5; margin-left: 4px;" onclick="deleteSupplier(${s.supplierId})">Xóa</button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (e) {}
}

function openAddSupplierModal() {
    document.getElementById('supplier-form-title').textContent = 'Thêm Nhà Cung Cấp Mới';
    document.getElementById('supp-id').value = '';
    document.getElementById('form-supplier').reset();
    openModal('modal-supplier-form');
}

function openEditSupplierModal(supId) {
    const s = allSuppliers.find(x => x.supplierId === supId);
    if (!s) return;
    document.getElementById('supplier-form-title').textContent = 'Cập Nhật Nhà Cung Cấp';
    document.getElementById('supp-id').value = s.supplierId;
    document.getElementById('supp-name').value = s.supplierName;
    document.getElementById('supp-phone').value = s.phone || '';
    document.getElementById('supp-email').value = s.email || '';
    document.getElementById('supp-address').value = s.address || '';
    openModal('modal-supplier-form');
}



async function handleSupplierSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('supp-id').value;
    const payload = {
        supplierName: document.getElementById('supp-name').value.trim(),
        phone: document.getElementById('supp-phone').value.trim(),
        email: document.getElementById('supp-email').value.trim(),
        address: document.getElementById('supp-address').value.trim()
    };

    const nameErr = validateSupplierName(payload.supplierName, true);
    if (nameErr) {
        showToast(nameErr, 'error');
        document.getElementById('supp-name')?.focus();
        return;
    }
    if (!payload.phone) {
        showToast('Số điện thoại nhà cung cấp không được để trống!', 'error');
        document.getElementById('supp-phone')?.focus();
        return;
    }
    const phoneErr = validatePhone(payload.phone, true);
    if (phoneErr) {
        showToast(phoneErr, 'error');
        document.getElementById('supp-phone')?.focus();
        return;
    }

    if (!payload.email) {
        showToast('Email nhà cung cấp không được để trống!', 'error');
        document.getElementById('supp-email')?.focus();
        return;
    }
    const emailErr = validateEmail(payload.email, true);
    if (emailErr) {
        showToast(emailErr, 'error');
        document.getElementById('supp-email')?.focus();
        return;
    }

    try {
        const url = id ? `/suppliers/${id}` : '/suppliers';
        const method = id ? 'PUT' : 'POST';
        const res = await callApi(url, { method, body: JSON.stringify(payload) });
        const json = await res.json();
        if (json.success) {
            closeModal('modal-supplier-form');
            showToast('Lưu thông tin nhà cung cấp thành công', 'success');
            loadSuppliersMgmt();
        } else {
            showToast(json.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi lưu nhà cung cấp', 'error');
    }
}

async function deleteSupplier(id) {
    openConfirmDialog(`Bạn có chắc chắn muốn xóa nhà cung cấp này hay không?`, async () => {
        try {
            const res = await callApi(`/suppliers/${id}`, { method: 'DELETE' });
            const json = await res.json();
            closeModal('modal-confirm');
            if (json.success) {
                showToast('Xóa thành công', 'success');
                loadSuppliersMgmt();
            } else {
                showToast(json.message, 'warning');
            }
        } catch (e) {
            showToast('Lỗi xóa nhà cung cấp', 'error');
        }
    });
}

/* Lập phiếu nhập hàng & trigger DB cộng kho */
