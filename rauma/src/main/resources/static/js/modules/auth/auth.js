/* ==========================================================================
   MODULE: AUTH & USER PROFILE (Đăng nhập, Đăng ký, Thông tin cá nhân, Đổi MK)
   ========================================================================== */

/* ==========================================================================
   LOGIN PAGE FULLSCREEN FUNCTIONS
   ========================================================================== */
function showLoginPage() {
    document.getElementById('login-page').style.display = 'block';
    document.getElementById('main-app').style.display = 'none';
    // Reset về tab login
    lpSwitchTab('login');
    // Reset login form
    const uEl = document.getElementById('lp-username');
    const pEl = document.getElementById('lp-password');
    const errEl = document.getElementById('lp-error');
    if (uEl) uEl.value = '';
    if (pEl) pEl.value = '';
    if (errEl) errEl.style.display = 'none';
    // Reset register form
    ['lp-reg-username','lp-reg-password','lp-reg-fullname','lp-reg-phone','lp-reg-email','lp-reg-address'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });
    const regErr = document.getElementById('lp-reg-error');
    const regOk  = document.getElementById('lp-reg-success');
    if (regErr) regErr.style.display = 'none';
    if (regOk)  regOk.style.display  = 'none';
}

function showMainApp() {
    document.getElementById('login-page').style.display = 'none';
    document.getElementById('main-app').style.display = 'block';
}

function lpQuickFill(username, password) {
    document.getElementById('lp-username').value = username;
    document.getElementById('lp-password').value = password;
    const errEl = document.getElementById('lp-error');
    if (errEl) errEl.style.display = 'none';
}

async function handleLpLoginSubmit(e) {
    e.preventDefault();
    const u = document.getElementById('lp-username').value.trim();
    const p = document.getElementById('lp-password').value.trim();
    const errEl = document.getElementById('lp-error');
    const btn = document.getElementById('lp-submit-btn');

    if (!u || !p) {
        errEl.textContent = 'Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.';
        errEl.style.display = 'block';
        return;
    }

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang đăng nhập...';
    errEl.style.display = 'none';

    try {
        const res = await callApi('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username: u, password: p })
        });
        const json = await res.json();
        if (json.success && json.data) {
            currentUser = { ...json.data, isLoggedIn: true };
            localStorage.setItem('bhx_user', JSON.stringify(currentUser));
            updateUserUI();
            showMainApp();
            showToast(`Xin chào ${currentUser.fullName || currentUser.username}! Đăng nhập thành công.`, 'success');
            if (currentUser.role === 'QuanLy') {
                switchPage('reports');
            } else if (currentUser.role === 'NhanVien') {
                await loadCart();
                switchPage('orders');
            } else {
                await loadCart();
                switchPage('home');
            }
        } else {
            errEl.textContent = json.message || 'Tên đăng nhập hoặc mật khẩu không đúng.';
            errEl.style.display = 'block';
        }
    } catch (err) {
        errEl.textContent = 'Lỗi kết nối. Vui lòng thử lại.';
        errEl.style.display = 'block';
    }

    btn.disabled = false;
    btn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Đăng Nhập';
}

function lpSwitchTab(tab) {
    const isLogin = tab === 'login';
    // Panels
    document.getElementById('lp-panel-login').style.display    = isLogin ? 'block' : 'none';
    document.getElementById('lp-panel-register').style.display = isLogin ? 'none'  : 'block';
    // Tab buttons
    const btnLogin = document.getElementById('lp-tab-login');
    const btnReg   = document.getElementById('lp-tab-register');
    if (btnLogin) {
        btnLogin.style.color       = isLogin ? '#008848' : '#9ca3af';
        btnLogin.style.borderBottom = isLogin ? '3px solid #008848' : '3px solid transparent';
    }
    if (btnReg) {
        btnReg.style.color        = isLogin ? '#9ca3af' : '#008848';
        btnReg.style.borderBottom = isLogin ? '3px solid transparent' : '3px solid #008848';
    }
}

async function handleLpRegisterSubmit(e) {
    e.preventDefault();
    const payload = {
        username: document.getElementById('lp-reg-username').value.trim(),
        password: document.getElementById('lp-reg-password').value.trim(),
        fullName: document.getElementById('lp-reg-fullname').value.trim(),
        phone:    document.getElementById('lp-reg-phone').value.trim(),
        email:    document.getElementById('lp-reg-email').value.trim(),
        address:  document.getElementById('lp-reg-address').value.trim()
    };
    const errEl = document.getElementById('lp-reg-error');
    const okEl  = document.getElementById('lp-reg-success');
    const btn   = document.getElementById('lp-reg-submit-btn');

    errEl.style.display = 'none';
    okEl.style.display  = 'none';

    const userErr = validateUsername(payload.username, true);
    if (userErr) {
        errEl.textContent = userErr;
        errEl.style.display = 'block';
        showToast(userErr, 'error');
        document.getElementById('lp-reg-username')?.focus();
        return;
    }
    if (!payload.password) { errEl.textContent = 'Mật khẩu không được để trống!'; errEl.style.display = 'block'; return; }
    const nameErr = validateFullName(payload.fullName, true);
    if (nameErr) {
        errEl.textContent = nameErr;
        errEl.style.display = 'block';
        showToast(nameErr, 'error');
        document.getElementById('lp-reg-fullname')?.focus();
        return;
    }
    const phoneErr = validatePhone(payload.phone, true);
    if (phoneErr) {
        errEl.textContent = phoneErr;
        errEl.style.display = 'block';
        showToast(phoneErr, 'error');
        document.getElementById('lp-reg-phone')?.focus();
        return;
    }
    const emailErr = validateEmail(payload.email, true);
    if (emailErr) {
        errEl.textContent = emailErr;
        errEl.style.display = 'block';
        showToast(emailErr, 'error');
        document.getElementById('lp-reg-email')?.focus();
        return;
    }

    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tạo tài khoản...';

    try {
        const res  = await callApi('/auth/register', { method: 'POST', body: JSON.stringify(payload) });
        const json = await res.json();
        if (json.success) {
            okEl.textContent = '✅ Tạo tài khoản thành công! Đang chuyển đến đăng nhập...';
            okEl.style.display = 'block';
            document.getElementById('lp-form-register').reset();
            setTimeout(() => {
                lpSwitchTab('login');
                document.getElementById('lp-username').value = payload.username;
                showToast('Tài khoản đã được tạo, vui lòng đăng nhập!', 'success');
            }, 1500);
        } else {
            errEl.textContent = json.message || 'Đăng ký thất bại, vui lòng thử lại.';
            errEl.style.display = 'block';
        }
    } catch (err) {
        errEl.textContent = 'Lỗi kết nối. Vui lòng thử lại.';
        errEl.style.display = 'block';
    }

    btn.disabled = false;
    btn.innerHTML = '<i class="fa-solid fa-user-plus"></i> Tạo Tài Khoản';
}

function showRegisterFromLoginPage(e) {
    if (e) e.preventDefault();
    lpSwitchTab('register');
}



/* ==========================================================================
   AUTH & USER PROFILE (UC1: ĐĂNG KÝ, UC2: ĐĂNG NHẬP, UC4: TÀI KHOẢN)
   ========================================================================= */
function openAuthModal(tab = 'login') {
    switchAuthTab(tab);
    openModal('modal-auth');
}

function switchAuthTab(tab) {
    document.getElementById('tab-auth-login').classList.toggle('active', tab === 'login');
    document.getElementById('tab-auth-register').classList.toggle('active', tab === 'register');
    document.getElementById('form-login').classList.toggle('hidden', tab !== 'login');
    document.getElementById('form-register').classList.toggle('hidden', tab !== 'register');
    document.getElementById('auth-modal-title').textContent = tab === 'login' ? 'Đăng Nhập Tài Khoản' : 'Đăng Ký Khách Hàng Mới';
}

function quickFillAccount(username, password, role) {
    document.getElementById('login-username').value = username;
    document.getElementById('login-password').value = password;
    showToast(`Đã điền sẵn tài khoản mẫu: ${username} (${role})`, 'info');
}

async function handleLoginSubmit(e) {
    e.preventDefault();
    const u = document.getElementById('login-username').value.trim();
    const p = document.getElementById('login-password').value.trim();

    try {
        const res = await callApi('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username: u, password: p })
        });
        const json = await res.json();
        if (json.success && json.data) {
            currentUser = {
                ...json.data,
                isLoggedIn: true
            };
            localStorage.setItem('bhx_user', JSON.stringify(currentUser));
            updateUserUI();
            closeModal('modal-auth');
            showMainApp();
            showToast(`Đăng nhập thành công! Xin chào ${currentUser.fullName || currentUser.username}`, 'success');

            if (currentUser.role === 'QuanLy') {
                switchPage('reports');
            } else {
                await loadCart();
                if (currentUser.role === 'NhanVien') switchPage('orders');
                else switchPage('home');
            }
        } else {
            showToast(json.message || 'Đăng nhập không thành công', 'error');
        }
    } catch (err) {
        showToast('Lỗi kết nối khi đăng nhập', 'error');
    }
}

async function handleRegisterSubmit(e) {
    e.preventDefault();
    const payload = {
        username: document.getElementById('reg-username').value.trim(),
        password: document.getElementById('reg-password').value.trim(),
        fullName: document.getElementById('reg-fullname').value.trim(),
        phone: document.getElementById('reg-phone').value.trim(),
        email: document.getElementById('reg-email').value.trim(),
        address: document.getElementById('reg-address').value.trim()
    };

    const userErr = validateUsername(payload.username, true);
    if (userErr) {
        showToast(userErr, 'error');
        document.getElementById('reg-username')?.focus();
        return;
    }
    if (!payload.password || payload.password.length < 6) {
        showToast('Mật khẩu bảo mật phải có ít nhất 6 ký tự!', 'error');
        return;
    }
    const nameErr = validateFullName(payload.fullName, true);
    if (nameErr) {
        showToast(nameErr, 'error');
        document.getElementById('reg-fullname')?.focus();
        return;
    }
    const phoneErr = validatePhone(payload.phone, true);
    if (phoneErr) {
        showToast(phoneErr, 'error');
        return;
    }
    const emailErr = validateEmail(payload.email, true);
    if (emailErr) {
        showToast(emailErr, 'error');
        document.getElementById('reg-email')?.focus();
        return;
    }

    try {
        const res = await callApi('/auth/register', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (json.success) {
            showToast(json.message || 'Đăng ký thành công! Vui lòng đăng nhập.', 'success');
            switchAuthTab('login');
            document.getElementById('login-username').value = payload.username;
            document.getElementById('login-password').value = payload.password;
        } else {
            showToast(json.message || 'Đăng ký thất bại', 'error');
        }
    } catch (err) {
        showToast('Lỗi thực hiện đăng ký', 'error');
    }
}

function handleLogout() {
    currentUser = {
        role: 'KhachHang',
        username: '',
        fullName: '',
        accountId: null,
        customerId: null,
        employeeId: null,
        token: null,
        isLoggedIn: false
    };
    localStorage.removeItem('bhx_user');
    updateUserUI();
    closeModal('modal-profile');
    showToast('Đã đăng xuất khỏi tài khoản', 'info');
    showLoginPage();
}

function openProfileModal() {
    document.getElementById('profile-username').value = currentUser.username || '';
    document.getElementById('profile-role').value = currentUser.role === 'QuanLy' ? 'Quản Lý (Admin)' : (currentUser.role === 'NhanVien' ? 'Nhân Viên' : 'Khách Hàng');
    document.getElementById('profile-fullname').value = currentUser.fullName || '';
    document.getElementById('profile-phone').value = currentUser.phone || '';
    document.getElementById('profile-email').value = currentUser.email || '';
    document.getElementById('profile-address').value = currentUser.address || '';
    openModal('modal-profile');
}

async function handleProfileUpdate(e) {
    e.preventDefault();
    const fullName = document.getElementById('profile-fullname').value.trim();
    const phone = document.getElementById('profile-phone').value.trim();
    const email = document.getElementById('profile-email').value.trim();
    const address = document.getElementById('profile-address').value.trim();

    const nameErr = validateFullName(fullName, true);
    if (nameErr) { showToast(nameErr, 'error'); return; }

    if (phone) {
        const phoneErr = validatePhone(phone, false);
        if (phoneErr) { showToast(phoneErr, 'error'); return; }
    }
    if (email) {
        const emailErr = validateEmail(email, false);
        if (emailErr) {
            showToast(emailErr, 'error');
            document.getElementById('profile-email')?.focus();
            return;
        }
    }
    // Gửi lên backend kiểm tra email và cập nhật
    if (currentUser.customerId) {
        try {
            const res = await callApi(`/customers/${currentUser.customerId}`, {
                method: 'PUT',
                body: JSON.stringify({ fullName, phone, email, address })
            });
            const json = await res.json();
            if (!json.success) {
                showToast(json.message || 'Lỗi cập nhật tài khoản', 'error');
                return;
            }
        } catch (e) {
            showToast('Lỗi kết nối khi cập nhật tài khoản', 'error');
            return;
        }
    } else if (currentUser.employeeId) {
        try {
            const res = await callApi(`/employees/${currentUser.employeeId}`, {
                method: 'PUT',
                body: JSON.stringify({ fullName, phone, email, address })
            });
            const json = await res.json();
            if (!json.success) {
                showToast(json.message || 'Lỗi cập nhật tài khoản', 'error');
                return;
            }
        } catch (e) {
            showToast('Lỗi kết nối khi cập nhật tài khoản', 'error');
            return;
        }
    }

    currentUser.fullName = fullName;
    currentUser.phone = phone;
    currentUser.email = email;
    currentUser.address = address;
    localStorage.setItem('bhx_user', JSON.stringify(currentUser));
    updateUserUI();
    showToast('Cập nhật thông tin tài khoản thành công', 'success');
    closeModal('modal-profile');
}

function toggleChangePasswordForm() {
    const btn = document.getElementById('btn-toggle-password-form');
    const formContainer = document.getElementById('form-change-password-container');
    if (formContainer.style.display === 'none') {
        formContainer.style.display = 'block';
        btn.style.display = 'none';
    } else {
        formContainer.style.display = 'none';
        btn.style.display = 'flex';
        document.getElementById('form-change-password').reset();
    }
}

async function handleChangePassword(e) {
    e.preventDefault();
    const oldPass = document.getElementById('chg-old-password').value.trim();
    const newPass = document.getElementById('chg-new-password').value.trim();
    const confirmPass = document.getElementById('chg-confirm-password').value.trim();

    if (newPass.length < 6) {
        showToast('Mật khẩu mới phải có ít nhất 6 ký tự!', 'error');
        return;
    }
    if (newPass !== confirmPass) {
        showToast('Mật khẩu mới và xác nhận không khớp!', 'error');
        return;
    }

    try {
        const res = await callApi('/accounts/password', {
            method: 'POST',
            body: JSON.stringify({
                accountId: currentUser.accountId,
                username: currentUser.username,
                customerId: currentUser.customerId,
                employeeId: currentUser.employeeId,
                oldPassword: oldPass,
                newPassword: newPass
            })
        });
        const json = await res.json();
        if (json.success) {
            showToast('Đổi mật khẩu thành công!', 'success');
            toggleChangePasswordForm();
        } else {
            showToast(json.message || 'Lỗi đổi mật khẩu!', 'error');
        }
    } catch (err) {
        showToast('Lỗi đổi mật khẩu', 'error');
    }
}

