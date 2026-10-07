/* ==========================================================================
   MODULE: GIỎ HÀNG (Thêm, bớt, xóa, lưu trữ giỏ hàng rau củ)
   ========================================================================== */

/* ==========================================================================
   CART OPERATIONS (UC5: GIỎ HÀNG)
   ========================================================================= */
async function loadCart() {
    try {
        const custId = currentUser.customerId || 1;
        const res = await callApi(`/cart?customerId=${custId}`);
        const json = await res.json();
        if (json.success && json.data) {
            currentCart = json.data;
            updateCartUI();
        }
    } catch (e) {
        console.error('Lỗi tải giỏ hàng:', e);
    }
}

function updateCartUI() {
    const count = currentCart.items ? currentCart.items.reduce((s, it) => s + parseFloat(it.quantity), 0) : 0;
    const total = currentCart.totalAmount || 0;

    document.getElementById('header-cart-count').textContent = count;
    document.getElementById('header-cart-total').textContent = formatCurrency(total);
    document.getElementById('cart-drawer-total').textContent = formatCurrency(total);

    const itemsContainer = document.getElementById('cart-drawer-items');
    if (!itemsContainer) return;

    if (!currentCart.items || currentCart.items.length === 0) {
        itemsContainer.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: var(--text-muted);">
                <i class="fa-solid fa-basket-shopping" style="font-size: 3rem; margin-bottom: 12px; color: #cbd5e1;"></i>
                <p style="font-weight: 600;">Giỏ hàng của bạn đang trống</p>
                <p style="font-size: 0.85rem; margin-top: 4px;">Hãy chọn các loại rau củ tươi ngon để thêm vào giỏ nhé!</p>
            </div>
        `;
        return;
    }

    itemsContainer.innerHTML = currentCart.items.map(it => `
        <div class="cart-item-card">
            <img src="${it.image || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=150'}" alt="${it.productName}" onerror="this.src='https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=150'">
            <div class="cart-item-detail">
                <div class="cart-item-title">${it.productName}</div>
                <div class="cart-item-price">${formatCurrency(it.price)} / ${it.unit || 'kg'}</div>
                <div class="qty-control">
                    <button class="qty-btn" onclick="changeCartItemQty(${it.productId}, ${parseFloat(it.quantity) - 1})">-</button>
                    <span style="font-size: 0.9rem; font-weight: 700; min-width: 24px; text-align: center;">${it.quantity}</span>
                    <button class="qty-btn" onclick="changeCartItemQty(${it.productId}, ${parseFloat(it.quantity) + 1})">+</button>
                </div>
            </div>
            <button onclick="removeCartItem(${it.productId})" style="color: #94a3b8; padding: 6px;" title="Xóa món">
                <i class="fa-solid fa-trash-can"></i>
            </button>
        </div>
    `).join('');
}

async function addToCart(productId, quantity = 1) {
    try {
        const custId = currentUser.customerId || 1;
        const res = await callApi('/cart/items', {
            method: 'POST',
            body: JSON.stringify({
                customerId: custId,
                productId: productId,
                quantity: quantity
            })
        });
        const json = await res.json();
        if (json.success) {
            showToast(json.message || 'Đã thêm vào giỏ hàng!', 'success');
            await loadCart();
        } else {
            showToast(json.message || 'Lỗi thêm giỏ hàng', 'error');
        }
    } catch (e) {
        showToast('Lỗi kết nối máy chủ', 'error');
    }
}

async function changeCartItemQty(productId, newQty) {
    try {
        const custId = currentUser.customerId || 1;
        const res = await callApi(`/cart/items/${productId}`, {
            method: 'PUT',
            body: JSON.stringify({
                customerId: custId,
                productId: productId,
                quantity: newQty
            })
        });
        const json = await res.json();
        if (json.success) {
            await loadCart();
        } else {
            showToast(json.message, 'warning');
        }
    } catch (e) {
        showToast('Lỗi cập nhật giỏ hàng', 'error');
    }
}

async function removeCartItem(productId) {
    try {
        const custId = currentUser.customerId || 1;
        await callApi(`/cart/items/${productId}?customerId=${custId}`, { method: 'DELETE' });
        showToast('Đã xóa sản phẩm khỏi giỏ', 'info');
        await loadCart();
    } catch (e) {
        showToast('Lỗi xóa sản phẩm', 'error');
    }
}

function toggleCartDrawer(open) {
    const drawer = document.getElementById('cart-drawer-overlay');
    if (open) {
        drawer.classList.add('active');
    } else {
        drawer.classList.remove('active');
    }
}

