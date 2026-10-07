/* ==========================================================================
   MODULE: BÁN HÀNG TẠI QUẦY (POS Bán lẻ, tính tiền thối, in hóa đơn)
   ========================================================================== */

/* ==========================================================================
   STAFF & MANAGER: QL BÁN HÀNG TẠI QUẦY (UC8: POS BÁN HÀNG)
   ========================================================================= */
async function initPos() {
    if (!allProducts || allProducts.length === 0) {
        await loadProducts();
    }
    if (!allCategories || allCategories.length === 0) {
        await loadCategories();
    } else {
        populateCategoryDropdowns();
    }
    renderPosBill();
    await loadPosCustomers();
    renderPosCatalog(allProducts);
}

async function loadPosCustomers() {
    const sel = document.getElementById('pos-customer-select');
    if (!sel) return;
    try {
        const res = await callApi('/customers');
        const json = await res.json();
        if (json.success && json.data) {
            sel.innerHTML = `<option value="">Khách vãng lai (Mua tại quầy)</option>` +
                json.data.map(c => `<option value="${c.customerId}">${c.fullName} - ${c.phone || ''}</option>`).join('');
        }
    } catch (e) {}
}

function renderPosCatalog(products) {
    const grid = document.getElementById('pos-catalog-grid');
    if (!grid) return;

    if (!products || products.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 40px 0;">Không tìm thấy sản phẩm nào trong danh mục.</div>';
        return;
    }

    grid.innerHTML = products.map(p => `
        <div class="bhx-product-card" style="cursor: pointer; display: flex; flex-direction: column; justify-content: space-between; border: 1px solid var(--border-color); border-radius: 8px; overflow: hidden; background: #fff; transition: box-shadow 0.2s;" onclick="addPosItem(${p.productId})">
            <div>
                <div style="height: 100px; overflow: hidden; position: relative;">
                    <img src="${p.image || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=200'}" style="width: 100%; height: 100%; object-fit: cover;">
                    <span style="position: absolute; top: 4px; right: 4px; background: rgba(0,0,0,0.65); color: white; font-size: 0.68rem; padding: 2px 6px; border-radius: 4px; font-weight: 600;">Kho: ${p.stockQuantity}</span>
                </div>
                <div style="padding: 8px 8px 4px;">
                    <div style="font-size: 0.82rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" title="${p.productName}">${p.productName}</div>
                    <div style="font-size: 0.85rem; font-weight: 800; color: var(--bhx-green-dark);">${formatCurrency(p.price)} <span style="font-size: 0.7rem; font-weight: normal; color: #64748b;">/${p.unit || 'kg'}</span></div>
                </div>
            </div>
            <div style="padding: 4px 8px 8px;">
                <button class="btn-primary" style="width: 100%; padding: 4px 6px; font-size: 0.74rem; border-radius: 4px; display: flex; align-items: center; justify-content: center; gap: 4px;" onclick="event.stopPropagation(); addPosItem(${p.productId})">
                    <i class="fa-solid fa-cart-plus"></i> Chọn vào hóa đơn
                </button>
            </div>
        </div>
    `).join('');
}

function filterPosCatalog() {
    const searchInput = document.getElementById('pos-search-input');
    const catSelect = document.getElementById('pos-category-filter');
    const query = (searchInput ? searchInput.value : '').trim().toLowerCase();
    const catId = catSelect ? catSelect.value : '';

    let list = [...allProducts];
    if (catId) {
        list = list.filter(p => p.categoryId == catId);
    }
    if (query) {
        list = list.filter(p => 
            (p.productName && p.productName.toLowerCase().includes(query)) ||
            (p.productId && p.productId.toString().includes(query))
        );
    }
    renderPosCatalog(list);
}

function addPosItem(productId) {
    const numId = parseInt(productId, 10);
    const p = allProducts.find(x => parseInt(x.productId, 10) === numId || x.productId == productId);
    if (!p) {
        console.warn('addPosItem: không tìm thấy sản phẩm có mã', productId, allProducts);
        showToast('Không tìm thấy thông tin sản phẩm!', 'warning');
        return;
    }

    if (parseFloat(p.stockQuantity) <= 0) {
        showToast(`Sản phẩm "${p.productName}" đã hết hàng trong kho!`, 'warning');
        return;
    }

    const exist = posCart.find(i => parseInt(i.productId, 10) === numId || i.productId == productId);
    if (exist) {
        if (exist.quantity + 1 > parseFloat(p.stockQuantity)) {
            showToast(`Vượt quá tồn kho của "${p.productName}" (chỉ còn ${p.stockQuantity} ${p.unit || 'kg'})`, 'warning');
            return;
        }
        exist.quantity += 1;
        showToast(`Đã tăng số lượng "${p.productName}" trong hóa đơn (${exist.quantity})`, 'info');
    } else {
        posCart.push({
            productId: numId || p.productId,
            productName: p.productName,
            price: parseFloat(p.price) || 0,
            unit: p.unit || 'kg',
            stock: parseFloat(p.stockQuantity) || 0,
            quantity: 1
        });
        showToast(`Đã thêm "${p.productName}" vào hóa đơn bán hàng!`, 'success');
    }

    renderPosBill();
}

function addAndGoToPos(productId) {
    showPage('pos');
    addPosItem(productId);
}

function changePosQty(productId, newQty) {
    const numId = parseInt(productId, 10);
    const it = posCart.find(i => parseInt(i.productId, 10) === numId || i.productId == productId);
    if (!it) return;
    if (newQty <= 0) {
        posCart = posCart.filter(i => parseInt(i.productId, 10) !== numId && i.productId != productId);
    } else {
        if (newQty > it.stock) {
            showToast(`Tồn kho chỉ còn ${it.stock}`, 'warning');
            return;
        }
        it.quantity = newQty;
    }
    renderPosBill();
}

function renderPosBill() {
    const container = document.getElementById('pos-bill-items');
    if (!container) return;

    if (posCart.length === 0) {
        container.innerHTML = `<p style="text-align: center; color: var(--text-muted); padding: 30px 0;">Chưa chọn sản phẩm nào.<br>Bấm vào sản phẩm bên trái để thêm vào hóa đơn.</p>`;
        document.getElementById('pos-subtotal').textContent = '0đ';
        document.getElementById('pos-total-amount').textContent = '0đ';
        calcPosChange();
        return;
    }

    let subtotal = 0;
    container.innerHTML = posCart.map(it => {
        const itemTot = it.quantity * it.price;
        subtotal += itemTot;
        return `
            <div class="pos-item-row">
                <div style="flex: 1;">
                    <div style="font-weight: 700;">${it.productName}</div>
                    <div style="font-size: 0.75rem; color: #64748b;">${formatCurrency(it.price)} / ${it.unit || 'kg'}</div>
                </div>
                <div style="display: flex; align-items: center; gap: 6px;">
                    <button class="qty-btn" onclick="changePosQty(${it.productId}, ${it.quantity - 1})">-</button>
                    <span style="font-weight: 700; min-width: 20px; text-align: center;">${it.quantity}</span>
                    <button class="qty-btn" onclick="changePosQty(${it.productId}, ${it.quantity + 1})">+</button>
                </div>
                <div style="min-width: 75px; text-align: right; font-weight: 800; color: var(--bhx-green-dark);">
                    ${formatCurrency(itemTot)}
                </div>
                <button onclick="changePosQty(${it.productId}, 0)" style="color: #94a3b8; margin-left: 6px;" title="Xóa khỏi hóa đơn"><i class="fa-solid fa-xmark"></i></button>
            </div>
        `;
    }).join('');

    document.getElementById('pos-subtotal').textContent = formatCurrency(subtotal);
    document.getElementById('pos-total-amount').textContent = formatCurrency(subtotal);
    calcPosChange();
}

function calcPosChange() {
    const totalText = document.getElementById('pos-total-amount')?.textContent || '0';
    const total = parseFloat(totalText.replace(/[^\d]/g, '')) || 0;
    const inputEl = document.getElementById('pos-cash-received');
    const errEl = document.getElementById('pos-cash-error');
    const changeEl = document.getElementById('pos-cash-change');
    if (!inputEl) return;

    // Luôn ẩn thông báo lỗi khi đang nhập (lỗi chỉ hiện khi nhấn Thanh Toán)
    if (errEl) errEl.style.display = 'none';
    inputEl.style.borderColor = 'var(--border-color)';

    const rawVal = inputEl.value.trim();
    if (rawVal === '') {
        if (changeEl) changeEl.textContent = '0đ';
        return;
    }

    const received = parseFloat(rawVal);
    if (isNaN(received) || received <= 0) {
        if (changeEl) changeEl.textContent = '0đ';
        return;
    }

    // Chỉ hiển thị tiền thừa, không hiển thị lỗi
    const change = Math.max(0, received - total);
    if (changeEl) changeEl.textContent = received >= total ? formatCurrency(change) : '0đ';
}

function setPosCashExact() {
    const totalText = document.getElementById('pos-total-amount')?.textContent || '0';
    const total = parseFloat(totalText.replace(/[^\d]/g, '')) || 0;
    const inputEl = document.getElementById('pos-cash-received');
    if (inputEl) {
        inputEl.value = total;
        calcPosChange();
    }
}

function setPosCash(amount) {
    const inputEl = document.getElementById('pos-cash-received');
    if (inputEl) {
        inputEl.value = amount;
        calcPosChange();
    }
}

function onPosPaymentMethodChange() {
    const method = document.getElementById('pos-payment-method')?.value;
    const totalText = document.getElementById('pos-total-amount')?.textContent || '0';
    const total = parseFloat(totalText.replace(/[^\d]/g, '')) || 0;
    const inputEl = document.getElementById('pos-cash-received');

    if (method === 'ChuyenKhoan' && total > 0 && inputEl) {
        inputEl.value = total;
    }
    calcPosChange();
}

function resetPosSale() {
    posCart = [];
    const cashInput = document.getElementById('pos-cash-received');
    if (cashInput) {
        cashInput.value = '';
        cashInput.style.borderColor = 'var(--border-color)';
    }
    const errEl = document.getElementById('pos-cash-error');
    if (errEl) errEl.style.display = 'none';
    renderPosBill();
}

async function submitPosSale() {
    if (posCart.length === 0) {
        showToast('Vui lòng chọn ít nhất một sản phẩm từ danh mục để lập hóa đơn!', 'warning');
        return;
    }

    const totalText = document.getElementById('pos-total-amount')?.textContent || '0';
    const totalAmount = parseFloat(totalText.replace(/[^\d]/g, '')) || 0;
    const custId = document.getElementById('pos-customer-select')?.value || null;
    const method = document.getElementById('pos-payment-method')?.value || 'TienMat';
    const cashInput = document.getElementById('pos-cash-received');
    const rawCash = cashInput ? cashInput.value.trim() : '';
    const received = parseFloat(rawCash);

    // Kiểm tra điều kiện: tiền khách đưa (> 1k VND)
    if (method === 'TienMat') {
        if (rawCash === '' || isNaN(received)) {
            showToast('Vui lòng nhập tiền khách đưa (> 1.000 VNĐ)!', 'warning');
            if (cashInput) cashInput.focus();
            calcPosChange();
            return;
        }
        if (received <= 1000) {
            showToast('Tiền khách đưa không hợp lệ! Phải lớn hơn 1.000 VNĐ (> 1k VNĐ).', 'error');
            if (cashInput) cashInput.focus();
            calcPosChange();
            return;
        }
        if (totalAmount > 0 && received < totalAmount) {
            showToast(`Tiền khách đưa chưa đủ! Khách cần trả ${formatCurrency(totalAmount)} (còn thiếu ${formatCurrency(totalAmount - received)}).`, 'error');
            if (cashInput) cashInput.focus();
            calcPosChange();
            return;
        }
    } else {
        // Chuyển khoản QR
        if (rawCash !== '' && !isNaN(received) && received <= 1000) {
            showToast('Tiền khách đưa phải lớn hơn 1.000 VNĐ (> 1k VNĐ)!', 'warning');
            if (cashInput) cashInput.focus();
            calcPosChange();
            return;
        }
    }

    const finalReceived = !isNaN(received) && received > 0 ? received : totalAmount;
    const change = Math.max(0, finalReceived - totalAmount);
    const saleNote = `Bán tại quầy (${method === 'TienMat' ? 'Tiền mặt' : 'Chuyển khoản'}). Khách đưa: ${formatCurrency(finalReceived)}, Tiền thừa: ${formatCurrency(change)}`;

    try {
        const res = await callApi('/sales', {
            method: 'POST',
            body: JSON.stringify({
                employeeId: currentUser.employeeId || 1,
                customerId: custId,
                paymentMethod: method,
                cashReceived: finalReceived,
                note: saleNote,
                items: posCart.map(i => ({ productId: i.productId, quantity: i.quantity }))
            })
        });
        const json = await res.json();
        if (json.success) {
            const billId = json.data?.orderId || json.data?.MaDH || '';
            showToast(`Thanh toán thành công hóa đơn #${billId}! Tiền thối: ${formatCurrency(change)}. Tồn kho đã tự động cập nhật.`, 'success');
            resetPosSale();
            await loadProducts();
            renderPosCatalog(allProducts);
            if (typeof loadOrdersList === 'function') loadOrdersList();
        } else {
            showToast(json.message || 'Lỗi thanh toán tại quầy', 'error');
        }
    } catch (e) {
        showToast('Lỗi kết nối máy chủ', 'error');
    }
}

