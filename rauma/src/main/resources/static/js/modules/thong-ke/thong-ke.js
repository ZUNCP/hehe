/* ==========================================================================
   MODULE: THỐNG KÊ & BÁO CÁO (Doanh thu ngày/tháng, top bán chạy, tồn kho)
   ========================================================================== */

/* ==========================================================================
   MANAGER: BÁO CÁO & THỐNG KÊ (UC12: 3 VIEW CSDL: v_doanh_thu_ngay, v_doanh_thu_thang, v_sp_ban_chay)
   ========================================================================= */
async function loadReportsData() {
    // 1. Overview KPIs
    try {
        const res = await callApi('/reports');
        const json = await res.json();
        if (json.success && json.data) {
            const k = json.data;
            document.getElementById('kpi-total-revenue').textContent = formatCurrency(k.totalRevenue || 0);
            document.getElementById('kpi-completed-orders').textContent = k.completedOrders || 0;
            document.getElementById('kpi-total-products').textContent = k.totalProducts || 0;
        }
    } catch (e) {}

    // 2. Best Sellers (v_sp_ban_chay)
    try {
        const res = await callApi('/reports/best-sellers');
        const json = await res.json();
        const container = document.getElementById('report-best-sellers-list');
        if (container && json.success && json.data) {
            const maxSold = json.data.length ? Math.max(...json.data.map(i => parseFloat(i.totalSold || 0))) : 1;
            container.innerHTML = json.data.map((item, idx) => `
                <div style="margin-bottom: 12px;">
                    <div style="display: flex; justify-content: space-between; font-size: 0.88rem; font-weight: 700;">
                        <span>#${idx + 1} ${item.productName}</span>
                        <span style="color: var(--bhx-green-dark);">${item.totalSold} đã bán • ${formatCurrency(item.revenue || 0)}</span>
                    </div>
                    <div class="rank-bar-wrap">
                        <div class="rank-bar-fill" style="width: ${(parseFloat(item.totalSold || 0) / maxSold * 100)}%;"></div>
                    </div>
                </div>
            `).join('');
        }
    } catch (e) {}

    // 3. Daily Revenue (v_doanh_thu_ngay)
    try {
        const res = await callApi('/reports/revenue-daily');
        const json = await res.json();
        const tbody = document.getElementById('report-daily-tbody');
        if (tbody && json.success && json.data) {
            tbody.innerHTML = json.data.map(d => `
                <tr>
                    <td><strong>${formatDate(d.date)}</strong></td>
                    <td>${d.orderCount} đơn</td>
                    <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(d.revenue)}</strong></td>
                </tr>
            `).join('');
        }
    } catch (e) {}

    // 5. Monthly Revenue (v_doanh_thu_thang)
    try {
        const res = await callApi('/reports/revenue-monthly');
        const json = await res.json();
        const tbody = document.getElementById('report-monthly-tbody');
        if (tbody && json.success && json.data) {
            tbody.innerHTML = json.data.map(m => `
                <tr>
                    <td>${m.year}</td>
                    <td>Tháng ${m.month}</td>
                    <td><strong style="color: var(--bhx-green-dark);">${formatCurrency(m.revenue)}</strong></td>
                </tr>
            `).join('');
        }
    } catch (e) {}
}

