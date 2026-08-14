const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
}

async function handleLogin(event) {
    event.preventDefault();
    clearErrorBlock('loginError');
    clearSuccess('loginSuccess');

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    clearError('emailError');
    clearError('passwordError');

    let isValid = true;
    if (!email) {
        showError('emailError', 'Email is required');
        isValid = false;
    } else if (!isValidEmail(email)) {
        showError('emailError', 'Please enter a valid email address');
        isValid = false;
    }
    if (!password) {
        showError('passwordError', 'Password is required');
        isValid = false;
    }
    if (!isValid) return;

    const submitBtn = loginForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';

    try {
        const response = await apiCall('/api/auth/login', 'POST', { email, password }, false, false);
        if (!response || !response.token) throw { status: 500, message: 'Invalid server response' };
        storeToken(response.token);
        const user = response.user || {};
        // ensure role stored as plain string
        const roleValue = (typeof user.role === 'object' && user.role !== null) ? (user.role.name || String(user.role)) : user.role;
        storeUser({ id: user.id, name: user.name, email: user.email, role: normalizeRole(roleValue), memberSince: user.memberSince });
        showSuccess('loginSuccess', 'Login successful! Redirecting...');
        setTimeout(() => {
            window.location.href = getDashboardPath(roleValue || user.role);
        }, 900);
    } catch (error) {
        if (error.status === 401) {
            showErrorBlock('loginError', 'Invalid email or password.');
        } else {
            showErrorBlock('loginError', error.message || 'Login failed. Please try again.');
        }
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login';
    }
}

const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', handleRegister);
}

async function handleRegister(event) {
    event.preventDefault();
    clearErrorBlock('registerError');
    clearSuccess('registerSuccess');

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const role = document.getElementById('role').value;
    const termsChecked = document.getElementById('terms').checked;

    clearError('nameError');
    clearError('regEmailError');
    clearError('regPasswordError');
    clearError('confirmPasswordError');
    clearError('roleError');
    clearError('termsError');

    let isValid = true;
    if (!name) {
        showError('nameError', 'Name is required.');
        isValid = false;
    }
    if (!email) {
        showError('regEmailError', 'Email is required.');
        isValid = false;
    } else if (!isValidEmail(email)) {
        showError('regEmailError', 'Please enter a valid email address');
        isValid = false;
    }
    if (!password) {
        showError('regPasswordError', 'Password is required.');
        isValid = false;
    } else if (password.length < 8) {
        showError('regPasswordError', 'Password must be at least 8 characters.');
        isValid = false;
    }
    if (password !== confirmPassword) {
        showError('confirmPasswordError', 'Passwords do not match');
        isValid = false;
    }
    if (!role) {
        showError('roleError', 'Please select an account type');
        isValid = false;
    }
    if (!termsChecked) {
        showError('termsError', 'You must agree to the terms');
        isValid = false;
    }
    if (!isValid) return;

    const submitBtn = registerForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Creating account...';

    try {
        const res = await apiCall('/api/users/register', 'POST', { name, email, password, role }, false, false);
        // backend returns created user on success (201)
        showSuccess('registerSuccess', 'Account created successfully! Redirecting to login...');
        setTimeout(() => {
            window.location.href = '/html/login.html';
        }, 1200);
    } catch (error) {
        // display server-provided message when available
        const msg = error?.message || 'Registration failed. Please try again.';
        if (error.status === 409) {
            showError('regEmailError', msg);
        } else if (error.status === 400) {
            showErrorBlock('registerError', msg);
        } else {
            showErrorBlock('registerError', msg);
        }
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Account';
    }
}
