/* ==========================================================================
   MODULE: SẢN PHẨM & DANH MỤC RAU CỦ (Tra cứu khách hàng + Quản lý Admin)
   ========================================================================== */

/* ==========================================================================
   PRODUCTS & CATEGORIES (UC3: TRA CỨU)
   ========================================================================= */
async function loadCategories() {
    try {
        const res = await callApi('/categories');
        const json = await res.json();
        if (json.success && json.data) {
            allCategories = json.data;
            populateCategoryDropdowns();
        }
    } catch (e) {
        console.error('Lỗi tải danh mục:', e);
    }
}

function populateCategoryDropdowns() {
    const prodCatSelect = document.getElementById('prod-category');
    const posCatSelect = document.getElementById('pos-category-filter');

    const optionsHtml = allCategories.map(c => `<option value="${c.categoryId}">${c.categoryName}</option>`).join('');

    if (prodCatSelect) prodCatSelect.innerHTML = optionsHtml;
    if (posCatSelect) posCatSelect.innerHTML = `<option value="">Tất cả loại sản phẩm</option>` + optionsHtml;
}

async function loadProducts(categoryId = null, keyword = '') {
    try {
        let url = '/products?onlyActive=true';
        if (categoryId) url += `&categoryId=${categoryId}`;
        if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;

        const res = await callApi(url);
        const json = await res.json();
        if (json.success && json.data) {
            const list = json.data.content || json.data || [];
            allProducts = list.map(p => ({
                ...p,
                productId: parseInt(p.productId, 10),
                categoryId: p.categoryId ? parseInt(p.categoryId, 10) : null,
                price: parseFloat(p.price) || 0,
                stockQuantity: parseFloat(p.stockQuantity) || 0
            }));
            displayedProducts = [...allProducts];
            renderProductGrid(displayedProducts);
            const countEl = document.getElementById('product-total-count');
            if (countEl) countEl.textContent = `${displayedProducts.length} sản phẩm`;
        }
    } catch (e) {
        console.error('Lỗi tải sản phẩm:', e);
    }
}

function renderProductGrid(products) {
    const container = document.getElementById('product-grid-container');
    if (!container) return;

    if (!products || products.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 50px 20px; background: #fff; border-radius: 12px; border: 1px dashed #cbd5e1;">
                <i class="fa-solid fa-leaf" style="font-size: 3rem; color: #94a3b8; margin-bottom: 12px;"></i>
                <h3 style="font-size: 1.1rem; color: #475569;">Không tìm thấy rau củ quả phù hợp</h3>
                <p style="color: #94a3b8; font-size: 0.9rem; margin-top: 4px;">Hãy thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc</p>
                <button class="btn-primary" style="margin-top: 14px;" onclick="selectCategory(null)">Xem tất cả sản phẩm</button>
            </div>
        `;
        return;
    }

    container.innerHTML = products.map(p => {
        const isOutOfStock = parseFloat(p.stockQuantity) <= 0;
        const isLowStock = parseFloat(p.stockQuantity) > 0 && parseFloat(p.stockQuantity) < 10;

        return `
            <div class="bhx-product-card">
                <span class="card-top-tag green">TÍCH XANH</span>
                <div class="product-img-wrap" onclick="openProductDetail(${p.productId})">
                    <img src="${p.image || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=400'}" alt="${p.productName}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=400'">
                    <span class="vietgap-badge"><i class="fa-solid fa-circle-check"></i> VietGAP</span>
                </div>
                <div class="card-body">
                    <div>
                        <div class="card-category-hint">${p.categoryName || 'Rau củ sạch'}</div>
                        <h3 class="card-product-name" onclick="openProductDetail(${p.productId})" title="${p.productName}">${p.productName}</h3>
                        <div class="price-row">
                            <span class="current-price">${formatCurrency(p.price)}</span>
                            <span class="price-unit">/${p.unit || 'kg'}</span>
                        </div>
                    </div>
                    <div style="margin-top: 10px;">
                        <div class="stock-indicator ${isLowStock ? 'low' : ''}">
                            <i class="fa-solid ${isOutOfStock ? 'fa-circle-xmark' : (isLowStock ? 'fa-triangle-exclamation' : 'fa-circle-check')}"></i>
                            <span>${isOutOfStock ? 'Hết hàng' : (isLowStock ? `Sắp hết (còn ${p.stockQuantity})` : `Còn hàng (${p.stockQuantity})`)}</span>
                        </div>
                        <button class="add-to-cart-btn" onclick="addToCart(${p.productId}, 1)" ${isOutOfStock ? 'disabled' : ''}>
                            <i class="fa-solid fa-basket-shopping"></i>
                            <span>${isOutOfStock ? 'Tạm hết' : 'Thêm vào giỏ'}</span>
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function selectCategory(catId, element) {
    selectedCategoryId = catId;

    document.querySelectorAll('.subcat-card').forEach(c => c.classList.remove('active'));
    if (element) {
        element.classList.add('active');
    }

    const catObj = allCategories.find(c => c.categoryId === catId);
    const label = catObj ? catObj.categoryName : 'Tất cả rau củ quả tươi sạch';
    document.getElementById('breadcrumb-current-label').textContent = label;

    loadProducts(catId);
}

function filterCategoryByMockName(name) {
    showToast(`Đang hiển thị nhóm sản phẩm: ${name}`, 'info');
    loadProducts();
}

function applyTagFilter(tag, element) {
    document.querySelectorAll('.filter-tag-pill').forEach(b => b.classList.remove('active'));
    if (element) element.classList.add('active');

    if (tag === 'all') {
        displayedProducts = [...allProducts];
    } else if (tag === 'under15k') {
        displayedProducts = allProducts.filter(p => parseFloat(p.price) <= 15000);
    } else {
        displayedProducts = allProducts.filter(p => 
            p.productName.toLowerCase().includes(tag.toLowerCase()) || 
            (p.description && p.description.toLowerCase().includes(tag.toLowerCase()))
        );
    }
    renderProductGrid(displayedProducts);
    document.getElementById('product-total-count').textContent = `${displayedProducts.length} sản phẩm`;
}

function filterHotDeals() {
    switchPage('home');
    displayedProducts = allProducts.filter(p => parseFloat(p.price) <= 15000);
    renderProductGrid(displayedProducts);
    document.getElementById('breadcrumb-current-label').textContent = 'Khuyến Mãi Sốc Đồng Giá Rau Xanh';
    document.getElementById('product-total-count').textContent = `${displayedProducts.length} sản phẩm khuyến mãi`;
    showToast('Đang hiển thị danh sách rau củ Khuyến Mãi Sốc!', 'success');
}

function executeSearch() {
    const kw = document.getElementById('global-search-input').value.trim();
    loadProducts(selectedCategoryId, kw);
    switchPage('home');
}

function sortProducts(order) {
    if (order === 'price-asc') {
        displayedProducts.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
    } else if (order === 'price-desc') {
        displayedProducts.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
    } else if (order === 'name-asc') {
        displayedProducts.sort((a, b) => a.productName.localeCompare(b.productName));
    } else {
        displayedProducts = [...allProducts];
    }
    renderProductGrid(displayedProducts);
}

function openProductDetail(productId) {
    const p = allProducts.find(x => x.productId === productId);
    if (!p) return;

    document.getElementById('pdetail-title').textContent = p.productName;
    document.getElementById('pdetail-body').innerHTML = `
        <div style="display: grid; grid-template-columns: 200px 1fr; gap: 16px; margin-bottom: 16px;">
            <img src="${p.image || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=400'}" style="width: 100%; height: 200px; object-fit: cover; border-radius: 8px;">
            <div>
                <div style="font-size: 0.82rem; color: var(--bhx-green-dark); font-weight: 700; text-transform: uppercase;">
                    ${p.categoryName || 'Rau củ sạch'} • ${p.supplierName ? `NCC: ${p.supplierName}` : 'HTX Rau Sạch'}
                </div>
                <h2 style="font-size: 1.25rem; font-weight: 800; margin: 4px 0 10px;">${p.productName}</h2>
                <div style="font-size: 1.4rem; font-weight: 900; color: var(--bhx-green-dark); margin-bottom: 8px;">
                    ${formatCurrency(p.price)} <span style="font-size: 0.9rem; font-weight: 500; color: var(--text-secondary);">/${p.unit || 'kg'}</span>
                </div>
                <div style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 14px;">
                    <strong>Tình trạng:</strong> Còn ${p.stockQuantity} ${p.unit || 'kg'} trong kho
                </div>
                <button class="btn-primary" style="padding: 10px 20px;" onclick="addToCart(${p.productId}, 1); closeModal('modal-product-detail');">
                    <i class="fa-solid fa-basket-shopping"></i> Thêm Vào Giỏ Hàng
                </button>
            </div>
        </div>
        <div style="border-top: 1px solid var(--border-color); padding-top: 14px;">
            <h4 style="font-size: 0.95rem; font-weight: 800; margin-bottom: 6px;">Mô tả sản phẩm & Nguồn gốc</h4>
            <p style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.6;">${p.description || 'Sản phẩm được trồng và chăm sóc theo quy chuẩn VietGAP, đảm bảo an toàn vệ sinh thực phẩm, không hóa chất độc hại.'}</p>
        </div>
    `;
    openModal('modal-product-detail');
}



/* ==========================================================================
   MANAGER & STAFF: QL SẢN PHẨM (UC9)
   ========================================================================= */
async function loadProductsMgmt() {
    try {
        const res = await callApi('/products');
        const json = await res.json();
        const tbody = document.getElementById('product-mgmt-table-body');
        if (!tbody) return;

        const list = json.data.content || json.data || [];
        tbody.innerHTML = list.map(p => `
            <tr>
                <td>
                    <img src="${p.image || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=100'}" style="width: 48px; height: 48px; object-fit: cover; border-radius: 6px;">
                </td>
                <td><strong>${p.productName}</strong></td>
                <td>${p.categoryName || 'Rau củ'}</td>
                <td>${p.supplierName || '---'}</td>
                <td>${p.unit || 'kg'}</td>
                <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(p.price)}</strong></td>
                <td>
                    <span style="font-weight: 700; ${parseFloat(p.stockQuantity) < 10 ? 'color: var(--bhx-red);' : ''}">
                        ${p.stockQuantity}
                    </span>
                </td>
                <td>
                    <span class="badge-status ${p.status ? 'active' : 'inactive'}">
                        ${p.status ? 'Đang bán' : 'Ngừng bán'}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 4px; align-items: center;">
                        <button class="btn-outline admin-only" style="font-size: 0.78rem; padding: 4px 8px;" onclick="openEditProductModal(${p.productId})">Sửa</button>
                        <button class="btn-outline admin-only" style="font-size: 0.78rem; padding: 4px 8px; color: ${p.status ? 'var(--bhx-red)' : 'var(--bhx-green)'};" onclick="toggleProductStatus(${p.productId}, ${p.status})">
                            ${p.status ? 'Ẩn' : 'Hiện'}
                        </button>
                        <button class="btn-outline admin-only" style="font-size: 0.78rem; padding: 4px 8px; color: var(--bhx-red); border-color: #fca5a5;" onclick="confirmDeleteProduct(${p.productId}, '${(p.productName || '').replace(/['"\\]/g, '')}')">Xóa</button>
                    </div>
                </td>
            </tr>
        `).join('');
        if (typeof updateRoleUI === 'function') updateRoleUI();
    } catch (e) {
        console.error('Lỗi tải sản phẩm quản lý:', e);
    }
}

function confirmDeleteProduct(productId, productName) {
    openConfirmDialog(`Bạn có chắc chắn muốn xóa sản phẩm <strong>${productName}</strong> hay không?`, () => deleteProduct(productId));
}

async function deleteProduct(productId) {
    try {
        const res = await callApi(`/products/${productId}`, { method: 'DELETE' });
        const json = await res.json();
        closeModal('modal-confirm');
        if (json.success) {
            showToast('Xóa sản phẩm thành công', 'success');
            await loadProductsMgmt();
            await loadProducts();
        } else {
            showToast(json.message || 'Không thể xóa sản phẩm', 'warning');
        }
    } catch (e) {
        closeModal('modal-confirm');
        showToast('Lỗi kết nối khi xóa sản phẩm', 'error');
    }
}

function searchProductMgmt(query) {
    const q = query.toLowerCase();
    const rows = document.querySelectorAll('#product-mgmt-table-body tr');
    rows.forEach(r => {
        r.style.display = r.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
}


async function loadCategoriesIntoSelect(selectId, selectedVal = null) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    try {
        if (!allCategories || allCategories.length === 0) {
            const res = await callApi('/categories');
            const json = await res.json();
            if (json.success && json.data) {
                allCategories = json.data;
            }
        }
        if (allCategories && allCategories.length > 0) {
            sel.innerHTML = allCategories.map(c => `
                <option value="${c.categoryId}" ${String(c.categoryId) === String(selectedVal) ? 'selected' : ''}>${c.categoryName}</option>
            `).join('');
            if (selectedVal !== null) {
                sel.value = selectedVal;
            }
        }
    } catch (e) {
        console.error('Lỗi tải danh mục vào form:', e);
    }
}

async function openAddProductModal() {
    document.getElementById('product-form-modal-title').textContent = 'Thêm Sản Phẩm Rau Củ Mới';
    document.getElementById('product-form-submit-btn').textContent = 'Lưu thông tin';
    document.getElementById('prod-form-id').value = '';
    document.getElementById('form-product').reset();
    document.getElementById('prod-stock-group').style.display = 'block';

    await loadCategoriesIntoSelect('prod-category');
    await loadSuppliersIntoSelect('prod-supplier');
    openModal('modal-product-form');
}

async function openEditProductModal(productId) {
    let p = allProducts.find(x => x.productId === productId);
    if (!p) {
        // Try fetch from mgmt list (which includes all products including inactive)
        try {
            const res = await callApi(`/products/${productId}`);
            const json = await res.json();
            if (json.success) p = json.data;
        } catch (e) {}
    }
    if (!p) return;

    document.getElementById('product-form-modal-title').textContent = 'Cập Nhật Thông Tin Sản Phẩm';
    document.getElementById('product-form-submit-btn').textContent = 'Cập nhật thông tin';
    document.getElementById('prod-form-id').value = p.productId;
    document.getElementById('prod-name').value = p.productName;
    document.getElementById('prod-unit').value = p.unit || 'kg';
    document.getElementById('prod-price').value = p.price;
    document.getElementById('prod-image').value = p.image || '';
    document.getElementById('prod-description').value = p.description || '';
    document.getElementById('prod-stock-group').style.display = 'block';
    document.getElementById('prod-stock').value = (p.stockQuantity !== undefined && p.stockQuantity !== null) ? p.stockQuantity : 0;

    await loadCategoriesIntoSelect('prod-category', p.categoryId);
    await loadSuppliersIntoSelect('prod-supplier', p.supplierId);
    if (p.categoryId) {
        document.getElementById('prod-category').value = p.categoryId;
    }
    openModal('modal-product-form');
}

async function loadSuppliersIntoSelect(selectId, selectedVal = null) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    try {
        const res = await callApi('/suppliers');
        const json = await res.json();
        if (json.success && json.data) {
            allSuppliers = json.data;
            sel.innerHTML = json.data.map(s => `
                <option value="${s.supplierId}" ${s.supplierId == selectedVal ? 'selected' : ''}>${s.supplierName}</option>
            `).join('');
        }
    } catch (e) {}
}

async function handleProductSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('prod-form-id').value;
    const name = document.getElementById('prod-name').value.trim();
    const catId = parseInt(document.getElementById('prod-category').value);
    const supId = parseInt(document.getElementById('prod-supplier').value) || null;
    const unit = document.getElementById('prod-unit').value.trim();
    const rawPrice = document.getElementById('prod-price').value.trim();
    const stock = parseFloat(document.getElementById('prod-stock').value) || 0;
    const img = document.getElementById('prod-image').value.trim();
    const desc = document.getElementById('prod-description').value.trim();

    const nameErr = validateProductName(name, true);
    if (nameErr) {
        showToast(nameErr, 'error');
        document.getElementById('prod-name')?.focus();
        return;
    }

    // Để Backend kiểm tra và thông báo lỗi giá bán
    const payload = {
        productName: name,
        categoryId: catId,
        supplierId: supId,
        unit: unit,
        price: rawPrice,
        stockQuantity: stock,
        description: desc,
        image: img,
        status: true
    };

    try {
        const url = id ? `/products/${id}` : '/products';
        const method = id ? 'PUT' : 'POST';
        const res = await callApi(url, { method, body: JSON.stringify(payload) });
        const json = await res.json();
        if (json.success) {
            closeModal('modal-product-form');
            showToast(id ? 'Cập nhật thông tin sản phẩm thành công' : 'Thêm sản phẩm mới thành công', 'success');
            await loadProducts();
            loadProductsMgmt();
        } else {
            showToast(json.message || 'Thông tin không hợp lệ', 'error');
        }
    } catch (err) {
        showToast('Lỗi lưu sản phẩm', 'error');
    }
}

async function toggleProductStatus(productId, currentStatus) {
    const isCurrentlyActive = (currentStatus === true || currentStatus === 1 || currentStatus === '1' || currentStatus === 'true');
    const newStatus = !isCurrentlyActive;
    const newStatusInt = newStatus ? 1 : 0;

    try {
        const res = await callApi(`/products/${productId}`, {
            method: 'PUT',
            body: JSON.stringify({ status: newStatusInt })
        });
        const json = await res.json();
        if (json.success || res.ok) {
            // Cập nhật DOM ngay lập tức - không reload toàn bảng
            const allRows = document.querySelectorAll('#product-mgmt-table-body tr');
            allRows.forEach(row => {
                const btn = row.querySelector(`button[onclick*="toggleProductStatus(${productId},"]`);
                if (!btn) return;

                // Cập nhật badge trạng thái
                const badge = row.querySelector('.badge-status');
                if (badge) {
                    badge.className = `badge-status ${newStatus ? 'active' : 'inactive'}`;
                    badge.textContent = newStatus ? 'Đang bán' : 'Ngừng bán';
                }

                // Cập nhật nút Ẩn/Hiện
                btn.textContent = newStatus ? 'Ẩn' : 'Hiện';
                btn.style.color = newStatus ? 'var(--bhx-red)' : 'var(--bhx-green)';
                btn.setAttribute('onclick', `toggleProductStatus(${productId}, ${newStatusInt})`);
            });

            showToast(newStatus ? '✅ Sản phẩm đã được hiển thị!' : '🙈 Sản phẩm đã được ẩn!', 'success');

            // Đồng bộ allProducts trong bộ nhớ
            if (typeof allProducts !== 'undefined' && Array.isArray(allProducts)) {
                const prod = allProducts.find(p => parseInt(p.productId, 10) === parseInt(productId, 10));
                if (prod) prod.status = newStatusInt;
            }
        } else {
            showToast(json.message || 'Lỗi cập nhật trạng thái sản phẩm', 'error');
        }
    } catch (e) {
        showToast('Lỗi kết nối khi cập nhật trạng thái', 'error');
    }
}

