/* ==========================================================================
   MODULE: VALIDATION UTILITIES - KIỂM TRA ĐỊNH DẠNG DỮ LIỆU
   ========================================================================== */

// ==========================================
// HÀM KIỂM TRA ĐỊNH DẠNG DỮ LIỆU (VALIDATION)
// ==========================================
function validateFullName(name, isRequired = true) {
    const val = (name || '').trim();
    if (!val) {
        return isRequired ? 'Họ và tên không được để trống!' : null;
    }
    if (/\d/.test(val)) {
        return 'Họ và tên không được nhập số (chỉ chứa chữ cái)!';
    }
    if (/[^\p{L}\s]/u.test(val)) {
        return 'Họ và tên không được chứa ký tự đặc biệt!';
    }
    return null;
}

function validateUsername(username, isRequired = true) {
    const val = (username || '').trim();
    if (!val) {
        return isRequired ? 'Tên đăng nhập không được để trống!' : null;
    }
    if (/\s/.test(val)) {
        return 'Tên đăng nhập không được chứa khoảng trắng!';
    }
    if (/[^\p{L}\p{N}]/u.test(val)) {
        return 'Tên đăng nhập không được chứa ký tự đặc biệt (chỉ gồm chữ cái và số)!';
    }
    return null;
}

function validateProductName(name, isRequired = true) {
    const val = (name || '').trim();
    if (!val) {
        return isRequired ? 'Tên sản phẩm không được để trống!' : null;
    }
    if (/[^\p{L}\p{N}\s]/u.test(val)) {
        return 'Tên sản phẩm không được chứa ký tự đặc biệt!';
    }
    return null;
}

function validateSupplierName(name, isRequired = true) {
    const val = (name || '').trim();
    if (!val) {
        return isRequired ? 'Tên nhà cung cấp không được để trống!' : null;
    }
    if (/[^\p{L}\p{N}\s]/u.test(val)) {
        return 'Tên nhà cung cấp không được chứa ký tự đặc biệt!';
    }
    return null;
}

function validatePhone(phone, isRequired = true) {
    const val = (phone || '').trim();
    if (!val) {
        return isRequired ? 'Số điện thoại không được để trống!' : null;
    }
    if (!/^0\d{9}$/.test(val)) {
        return 'Số điện thoại phải gồm đúng 10 ký tự số, bắt đầu bằng số 0 và không chứa chữ cái!';
    }
    return null;
}

function validateEmail(email, isRequired = true) {
    const val = (email || '').trim();
    if (!val) {
        return isRequired ? 'Email không được để trống!' : null;
    }
    if (!/^[a-zA-Z0-9._%+-]+@gmail\.com$/i.test(val)) {
        return 'Email phải đúng định dạng @gmail.com (ví dụ: example@gmail.com)!';
    }
    return null;
}

// ==========================================
// TỰ ĐỘNG GẮN THÔNG BÁO VÀ KIỂM TRA ĐỊNH DẠNG CHO CÁC TRƯỜNG GMAIL
// ==========================================
function setupGmailFieldNotifications() {
    const emailFields = [
        { id: 'profile-email', required: false, label: 'Email tài khoản' },
        { id: 'reg-email', required: true, label: 'Email đăng ký' },
        { id: 'lp-reg-email', required: true, label: 'Email đăng ký' },
        { id: 'supp-email', required: true, label: 'Email nhà cung cấp' },
        { id: 'cust-email', required: false, label: 'Email khách hàng' }
    ];

    emailFields.forEach(({ id, required }) => {
        const input = document.getElementById(id);
        if (!input || input.dataset.gmailNotifAttached) return;
        input.dataset.gmailNotifAttached = 'true';

        // Tạo phần tử thông báo phản hồi ngay dưới input
        let feedback = input.parentElement.querySelector('.email-feedback-msg');
        if (!feedback) {
            feedback = document.createElement('div');
            feedback.className = 'email-feedback-msg';
            feedback.style.fontSize = '0.78rem';
            feedback.style.marginTop = '4px';
            feedback.style.fontWeight = '500';
            feedback.style.display = 'none';
            feedback.style.transition = 'all 0.2s ease';
            input.insertAdjacentElement('afterend', feedback);
        }

        const updateFeedback = (isBlur = false) => {
            const val = input.value.trim();
            if (!val) {
                if (required && isBlur) {
                    input.style.borderColor = '#dc2626';
                    feedback.style.color = '#dc2626';
                    feedback.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> Email không được để trống!';
                    feedback.style.display = 'block';
                    if (typeof showToast === 'function') {
                        showToast('Email không được để trống!', 'error');
                    }
                } else {
                    input.style.borderColor = '';
                    feedback.style.display = 'none';
                }
                return;
            }

            const err = validateEmail(val, false);
            if (err) {
                input.style.borderColor = '#dc2626';
                feedback.style.color = '#dc2626';
                feedback.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> Email phải có đuôi @gmail.com (ví dụ: example@gmail.com)';
                feedback.style.display = 'block';
                if (isBlur && typeof showToast === 'function') {
                    showToast(err, 'warning');
                }
            } else {
                input.style.borderColor = '#16a34a';
                feedback.style.color = '#16a34a';
                feedback.innerHTML = '<i class="fa-solid fa-circle-check"></i> Định dạng Gmail hợp lệ';
                feedback.style.display = 'block';
            }
        };

        input.addEventListener('input', () => updateFeedback(false));
        input.addEventListener('blur', () => updateFeedback(true));
        input.addEventListener('focus', () => {
            if (input.value.trim()) updateFeedback(false);
        });
    });
}

