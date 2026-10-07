/* ==========================================================================
   MODULE: NHÂN VIÊN (Quản lý nhân viên, phân quyền vai trò)
   ========================================================================== */

async function loadEmployeesMgmt() {
    try {
        const res = await callApi('/employees');
        const json = await res.json();
        const tbody = document.getElementById('employees-table-body');
        if (!tbody) return;

        if (json.success && json.data) {
            tbody.innerHTML = json.data.map(emp => {
                const isSelf = currentUser.isLoggedIn && (
                    Number(emp.accountId) === Number(currentUser.accountId) ||
                    Number(emp.employeeId) === Number(currentUser.employeeId)
                );
                const safe = JSON.stringify(emp).replace(/"/g, '&quot;');
                const deleteBtn = isSelf
                    ? `<button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px; opacity: 0.45; cursor: not-allowed;" title="Không thể tự xóa tài khoản đang đăng nhập" onclick="showToast('Admin không thể tự xóa tài khoản của chính mình', 'warning')">Xóa</button>`
                    : `<button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px; color: var(--bhx-red); border-color: #fca5a5;" onclick="openConfirmDialog('Bạn có chắc chắn muốn xóa nhân viên &lt;strong&gt;${(emp.fullName || '').replace(/'/g, '')}&lt;/strong&gt; hay không?', () => deleteEmployee(${emp.employeeId}))">Xóa</button>`;
                return `
                <tr>
                    <td><strong>#${emp.employeeId}</strong></td>
                    <td><strong>${emp.fullName || '---'}</strong></td>
                    <td><code>${emp.username || '---'}</code></td>
                    <td><code>${emp.password || '---'}</code></td>
                    <td><span class="badge-status ${emp.role}">${emp.role === 'QuanLy' ? 'Quản Lý' : 'Nhân Viên'}</span></td>
                    <td>${emp.phone || '---'}</td>
                    <td>${emp.gender || '---'}</td>
                    <td>${formatDate(emp.hireDate)}</td>
                    <td><span class="badge-status active">${emp.status === 0 || emp.status === false ? 'Ngừng làm' : 'Đang làm'}</span></td>
                    <td>
                        <div style="display: flex; gap: 4px;">
                            <button class="btn-outline" style="font-size: 0.78rem; padding: 4px 8px;" onclick="openEditEmployeeModal(${emp.employeeId}, ${safe})">Sửa</button>
                            ${deleteBtn}
                        </div>
                    </td>
                </tr>`;
            }).join('');
        }
    } catch (e) { console.error('Lỗi tải nhân viên:', e); }
}

function openAddEmployeeModal() {
    document.getElementById('employee-form-title').textContent = 'Thêm Nhân Viên Mới';
    document.getElementById('emp-id').value = '';
    document.getElementById('form-employee').reset();
    document.getElementById('emp-username').readOnly = false;
    document.getElementById('emp-password').placeholder = 'Mặc định 123456 nếu để trống';
    document.getElementById('emp-role').value = 'NhanVien';
    document.getElementById('employee-form-submit-btn').textContent = 'Lưu thông tin';
    openModal('modal-employee-form');
}

function openEditEmployeeModal(employeeId, empObj) {
    let e = typeof empObj === 'string' ? JSON.parse(empObj.replace(/&quot;/g, '"')) : empObj;
    document.getElementById('employee-form-title').textContent = 'Cập Nhật Thông Tin Nhân Viên';
    document.getElementById('emp-id').value = employeeId;
    document.getElementById('emp-username').readOnly = false;
    document.getElementById('emp-username').value = e.username || '';
    document.getElementById('emp-password').value = '';
    document.getElementById('emp-password').placeholder = 'Để trống nếu không đổi mật khẩu';
    document.getElementById('emp-role').value = e.role === 'QuanLy' ? 'QuanLy' : 'NhanVien';
    document.getElementById('emp-fullname').value = e.fullName || e.hoTen || '';
    document.getElementById('emp-birthday').value = (e.birthDate || e.ngaySinh || '').toString().slice(0, 10);
    document.getElementById('emp-gender').value = e.gender || e.gioiTinh || 'Nam';
    document.getElementById('emp-phone').value = e.phone || e.sdt || '';
    document.getElementById('emp-address').value = e.address || e.diaChi || '';
    document.getElementById('emp-hiredate').value = (e.hireDate || e.ngayVaoLam || '').toString().slice(0, 10);
    document.getElementById('employee-form-submit-btn').textContent = 'Cập nhật thông tin';
    openModal('modal-employee-form');
}

async function handleEmployeeSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('emp-id').value;
    const username = document.getElementById('emp-username').value.trim();
    const password = document.getElementById('emp-password').value.trim();
    const fullName = document.getElementById('emp-fullname').value.trim();
    const phone = document.getElementById('emp-phone').value.trim();

    const nameErr = validateFullName(fullName, true);
    if (nameErr) {
        showToast(nameErr, 'error');
        document.getElementById('emp-fullname')?.focus();
        return;
    }
    if (!id || username) {
        const userErr = validateUsername(username, !id);
        if (userErr) {
            showToast(userErr, 'error');
            document.getElementById('emp-username')?.focus();
            return;
        }
    }
    if (phone) {
        const phoneErr = validatePhone(phone, false);
        if (phoneErr) { showToast(phoneErr, 'error'); return; }
    }

    const payload = {
        hoTen: fullName,
        fullName: fullName,
        username: username,
        role: document.getElementById('emp-role').value,
        ngaySinh: document.getElementById('emp-birthday').value || null,
        birthDate: document.getElementById('emp-birthday').value || null,
        gioiTinh: document.getElementById('emp-gender').value,
        gender: document.getElementById('emp-gender').value,
        sdt: phone,
        phone: phone,
        diaChi: document.getElementById('emp-address').value.trim(),
        address: document.getElementById('emp-address').value.trim(),
        ngayVaoLam: document.getElementById('emp-hiredate').value || null,
        hireDate: document.getElementById('emp-hiredate').value || null
    };
    if (password) payload.password = password;
    try {
        const url = id ? `/employees/${id}` : '/employees';
        const method = id ? 'PUT' : 'POST';
        const res = await callApi(url, { method, body: JSON.stringify(payload) });
        const json = await res.json();
        if (json.success) {
            closeModal('modal-employee-form');
            showToast(id ? 'Cập nhật thông tin nhân viên thành công' : 'Thêm nhân viên mới thành công', 'success');
            loadEmployeesMgmt();
        } else { showToast(json.message || 'Thông tin không hợp lệ', 'error'); }
    } catch (err) { showToast('Lỗi lưu nhân viên', 'error'); }
}

async function deleteEmployee(id) {
    if (currentUser.isLoggedIn && Number(id) === Number(currentUser.employeeId)) {
        showToast('Admin không thể tự xóa tài khoản của chính mình', 'warning');
        closeModal('modal-confirm');
        return;
    }
    try {
        const actor = currentUser.accountId ? `?currentAccountId=${currentUser.accountId}` : '';
        const res = await callApi(`/employees/${id}${actor}`, { method: 'DELETE' });
        const json = await res.json();
        closeModal('modal-confirm');
        if (json.success) {
            showToast('Xóa thành công', 'success');
            loadEmployeesMgmt();
        } else { showToast(json.message, 'warning'); }
    } catch (e) { showToast('Lỗi xóa nhân viên', 'error'); }
}

