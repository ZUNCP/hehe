/* ==========================================================================
   MODULE: NHẬP KHO (Lập phiếu nhập rau củ từ nhà cung cấp)
   ========================================================================== */

async function openCreateReceiptModal() {
    await loadSuppliersIntoSelect('receipt-supplier-select');
    const container = document.getElementById('receipt-items-container');
    container.innerHTML = '';
    addReceiptRow();
    calcReceiptTotal();
    openModal('modal-receipt-form');
}

function addReceiptRow() {
    const container = document.getElementById('receipt-items-container');
    const rowId = Date.now();
    const productOptions = allProducts.map(p => `<option value="${p.productId}">${p.productName} (${p.unit || 'kg'})</option>`).join('');

    const row = document.createElement('div');
    row.id = `rec-row-${rowId}`;
    row.style = 'display: grid; grid-template-columns: 2fr 1fr 1.5fr 40px; gap: 10px; align-items: center; background: #fff; padding: 6px; border: 1px solid var(--border-color); border-radius: 6px;';
    row.innerHTML = `
        <select class="rec-prod-select" onchange="calcReceiptTotal()">${productOptions}</select>
        <input type="number" class="rec-qty-input" value="10" min="1" placeholder="Số lượng" oninput="calcReceiptTotal()">
        <input type="number" class="rec-price-input" value="10000" min="0" placeholder="Giá nhập (VNĐ)" oninput="calcReceiptTotal()">
        <button type="button" onclick="document.getElementById('rec-row-${rowId}').remove(); calcReceiptTotal();" style="color: var(--bhx-red);"><i class="fa-solid fa-trash-can"></i></button>
    `;
    container.appendChild(row);
    calcReceiptTotal();
}

function calcReceiptTotal() {
    let total = 0;
    document.querySelectorAll('#receipt-items-container > div').forEach(row => {
        const qty = parseFloat(row.querySelector('.rec-qty-input').value) || 0;
        const price = parseFloat(row.querySelector('.rec-price-input').value) || 0;
        total += qty * price;
    });
    document.getElementById('receipt-calc-total').textContent = formatCurrency(total);
}

async function handlePurchaseReceiptSubmit(e) {
    e.preventDefault();
    const supId = parseInt(document.getElementById('receipt-supplier-select').value);
    const items = [];

    document.querySelectorAll('#receipt-items-container > div').forEach(row => {
        const pid = parseInt(row.querySelector('.rec-prod-select').value);
        const qty = parseFloat(row.querySelector('.rec-qty-input').value);
        const price = parseFloat(row.querySelector('.rec-price-input').value);
        if (pid && qty > 0) {
            items.push({ productId: pid, quantity: qty, importPrice: price });
        }
    });

    if (items.length === 0) {
        showToast('Vui lòng nhập ít nhất một mặt hàng', 'warning');
        return;
    }

    try {
        const res = await callApi('/purchase-receipts', {
            method: 'POST',
            body: JSON.stringify({
                supplierId: supId,
                employeeId: currentUser.employeeId || 1,
                items: items
            })
        });
        const json = await res.json();
        if (json.success) {
            closeModal('modal-receipt-form');
            showToast(json.message || 'Lập phiếu nhập thành công! Tồn kho đã tự động tăng.', 'success');
            await loadProducts();
            switchSupplierSubTab('receipts');
        } else {
            showToast(json.message, 'error');
        }
    } catch (err) {
        showToast('Lỗi lập phiếu nhập hàng', 'error');
    }
}

async function loadReceiptsList() {
    try {
        const res = await callApi('/purchase-receipts');
        const json = await res.json();
        const tbody = document.getElementById('receipts-table-body');
        if (!tbody) return;

        if (json.success && json.data) {
            tbody.innerHTML = json.data.map(r => `
                <tr>
                    <td><strong>#${r.receiptId}</strong></td>
                    <td><strong>${r.supplierName || '---'}</strong></td>
                    <td>${r.employeeName || 'Nguyễn Văn Quản'}</td>
                    <td>${formatDate(r.receiptDate)}</td>
                    <td>${r.itemCount || 1} mặt hàng</td>
                    <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(r.totalAmount)}</strong></td>
                    <td><button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px;" onclick="showToast('Phiếu nhập #${r.receiptId} đã ghi nhận vào kho', 'info')">Xem</button></td>
                </tr>
            `).join('');
        }
    } catch (e) {}
}

