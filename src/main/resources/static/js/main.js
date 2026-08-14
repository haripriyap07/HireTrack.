const API_BASE_URL = window.location.origin;

function showError(elementId, message) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = message;
        element.classList.add('show');
    }
}

function clearError(elementId) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = '';
        element.classList.remove('show');
    }
}

function showSuccess(elementId, message) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = message;
        element.style.display = 'block';
        element.classList.add('show');
    }
}

function clearSuccess(elementId) {
    const element = document.getElementById(elementId);

    if (element) {
        element.textContent = '';
        element.classList.remove('show');
    }
}

function showErrorBlock(elementId, message) {
    showError(elementId, message);
}

function clearErrorBlock(elementId) {
    clearError(elementId);
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function showLoading(target, message = 'Loading...') {
    const el =
        typeof target === 'string'
            ? document.getElementById(target)
            : target;

    if (!el) return;

    el.innerHTML =
        `<div class="loading">${message}</div>`;
}

function clearLoading(target) {
    const el =
        typeof target === 'string'
            ? document.getElementById(target)
            : target;

    if (!el) return;

    el.innerHTML = '';
}

function storeToken(token) {
    localStorage.setItem('token', token);
}

function getToken() {
    return localStorage.getItem('token');
}

function clearToken() {
    localStorage.removeItem('token');
}

function isLoggedIn() {
    return !!getToken();
}

function getStoredUser() {
    try {
        return JSON.parse(
            localStorage.getItem('user') || '{}'
        );
    } catch (e) {
        return {};
    }
}

function storeUser(user) {
    localStorage.setItem(
        'user',
        JSON.stringify(user)
    );
}


/*
 * IMPORTANT:
 *
 * Do NOT put Content-Type: application/json here.
 *
 * For normal JSON requests, apiCall() adds it.
 *
 * For FormData requests, the browser automatically
 * creates the correct multipart/form-data header.
 */
function getAuthHeader() {

    const token = getToken();

    return token
        ? {
            'Authorization': `Bearer ${token}`
        }
        : {};
}

function getHeader() {
    return {};
}

function normalizeRole(role) {

    if (!role) {
        return 'CANDIDATE';
    }

    return role.startsWith('ROLE_')
        ? role.substring(5)
        : role;
}

function formatStatus(status) {

    if (!status) {
        return '';
    }

    return status
        .replace(/_/g, ' ')
        .replace(/\b\w/g, c => c.toUpperCase());
}

function formatDate(value) {

    if (!value) {
        return 'N/A';
    }

    return new Date(value).toLocaleDateString(
        undefined,
        {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        }
    );
}

function showToast(
    message,
    type = 'success'
) {

    let toast =
        document.getElementById('appToast');

    if (!toast) {

        toast =
            document.createElement('div');

        toast.id = 'appToast';
        toast.className = 'toast';

        document.body.appendChild(toast);
    }

    toast.textContent = message;

    toast.className =
        `toast toast-${type} show`;

    setTimeout(
        () => toast.classList.remove('show'),
        3200
    );
}


/*
 * =====================================================
 * API CALL
 * =====================================================
 *
 * Supports both:
 *
 * 1. JSON
 * 2. FormData / file uploads
 *
 */
async function apiCall(
    endpoint,
    method = 'GET',
    data = null,
    requireAuth = false,
    redirectOn401 = true
) {

    const url =
        `${API_BASE_URL}${endpoint}`;

    const headers =
        requireAuth
            ? getAuthHeader()
            : getHeader();

    const options = {
        method,
        headers
    };


    /*
     * -------------------------------------------------
     * FORM DATA
     * -------------------------------------------------
     *
     * This is used when uploading resumes.
     *
     * NEVER manually set:
     *
     * Content-Type: multipart/form-data
     *
     * The browser must set it because it adds
     * the multipart boundary automatically.
     */
    if (data instanceof FormData) {

        options.body = data;

    }


    /*
     * -------------------------------------------------
     * NORMAL JSON DATA
     * -------------------------------------------------
     */
    else if (
        data !== null &&
        data !== undefined &&
        method !== 'GET'
    ) {

        headers['Content-Type'] =
            'application/json';

        options.body =
            JSON.stringify(data);
    }


    let response;

    try {

        response =
            await fetch(
                url,
                options
            );

    } catch (error) {

        throw {
            status: 0,
            message:
                'Unable to connect to the server. ' +
                'Make sure the backend is running.',
            data: null
        };
    }


    const contentType =
        response.headers.get(
            'content-type'
        ) || '';

    let payload = null;


    /*
     * -------------------------------------------------
     * RESPONSE
     * -------------------------------------------------
     */
    if (response.status !== 204) {

        if (
            contentType.includes(
                'application/json'
            )
        ) {

            payload =
                await response
                    .json()
                    .catch(() => null);

        } else {

            const text =
                await response.text();

            payload =
                text || null;
        }
    }


    /*
     * -------------------------------------------------
     * ERROR HANDLING
     * -------------------------------------------------
     */
    if (!response.ok) {

        let message;

        if (
            payload &&
            typeof payload === 'object' &&
            (payload.message ||
                payload.error)
        ) {

            message =
                payload.message ||
                payload.error;
        }

        else if (
            typeof payload === 'string'
        ) {

            message = payload;
        }

        else {

            message =
                `Request failed (${response.status})`;
        }


        /*
         * Unauthorized
         */
        if (
            response.status === 401 &&
            redirectOn401
        ) {

            clearToken();

            localStorage.removeItem(
                'user'
            );

            window.location.href =
                '/html/login.html';

            return;
        }


        throw {
            status: response.status,
            message,
            data: payload
        };
    }


    return payload;
}


/*
 * =====================================================
 * AUTHENTICATION
 * =====================================================
 */

function requireAuth() {

    if (!isLoggedIn()) {

        window.location.href =
            '/html/login.html';

        return false;
    }

    return true;
}

function logout() {

    clearToken();

    localStorage.removeItem(
        'user'
    );

    window.location.href =
        '/html/login.html';
}


/*
 * =====================================================
 * DASHBOARD ROUTING
 * =====================================================
 */

function getDashboardPath(role) {

    const normalized =
        normalizeRole(role);

    if (normalized === 'ADMIN') {

        return '/admin/dashboard.html';
    }

    if (normalized === 'RECRUITER') {

        return '/recruiter/dashboard.html';
    }

    return '/dashboard.html';
}


/*
 * =====================================================
 * NAVBAR
 * =====================================================
 */

function renderAppNavbar(activePage) {

    const user =
        getStoredUser();

    const role =
        normalizeRole(user.role);

    const name =
        user.name || 'User';

    const isCandidate =
        role === 'CANDIDATE';

    const isRecruiter =
        role === 'RECRUITER';

    const isAdmin =
        role === 'ADMIN';


    let links = '';


    if (isCandidate) {

        links = `
            <li>
                <a
                    href="/dashboard.html"
                    class="${activePage === 'dashboard' ? 'active' : ''}">
                    Dashboard
                </a>
            </li>

            <li>
                <a
                    href="/jobs.html"
                    class="${activePage === 'jobs' ? 'active' : ''}">
                    Jobs
                </a>
            </li>

            <li>
                <a
                    href="/applications.html"
                    class="${activePage === 'applications' ? 'active' : ''}">
                    My Applications
                </a>
            </li>
        `;
    }


    else if (isRecruiter) {

        links = `
            <li>
                <a
                    href="/recruiter/dashboard.html"
                    class="${activePage === 'dashboard' ? 'active' : ''}">
                    Dashboard
                </a>
            </li>

            <li>
                <a
                    href="/recruiter/jobs.html"
                    class="${activePage === 'jobs' ? 'active' : ''}">
                    My Jobs
                </a>
            </li>

            <li>
                <a
                    href="/recruiter/applications.html"
                    class="${activePage === 'applications' ? 'active' : ''}">
                    Applications
                </a>
            </li>
        `;
    }


    else if (isAdmin) {

        links = `
            <li>
                <a
                    href="/admin/dashboard.html"
                    class="${activePage === 'dashboard' ? 'active' : ''}">
                    Dashboard
                </a>
            </li>

            <li>
                <a
                    href="/admin/users.html"
                    class="${activePage === 'users' ? 'active' : ''}">
                    Users
                </a>
            </li>

            <li>
                <a
                    href="/admin/jobs.html"
                    class="${activePage === 'jobs' ? 'active' : ''}">
                    Jobs
                </a>
            </li>

            <li>
                <a
                    href="/admin/applications.html"
                    class="${activePage === 'applications' ? 'active' : ''}">
                    Applications
                </a>
            </li>
        `;
    }


    return `
        <nav class="navbar app-navbar">

            <div class="container navbar-inner">

                <div class="nav-brand">
                    <a href="${getDashboardPath(role)}">
                        HireTrack
                    </a>
                </div>

                <button
                    class="nav-toggle"
                    id="navToggle"
                    aria-label="Toggle navigation">
                    ☰
                </button>

                <ul
                    class="nav-menu"
                    id="navMenu">

                    ${links}

                    <li class="nav-user">

                        <button
                            class="user-menu-btn"
                            id="userMenuBtn">

                            <span
                                class="avatar-circle small">
                                ${name.charAt(0).toUpperCase()}
                            </span>

                            <span class="user-meta">

                                <strong>
                                    ${name}
                                </strong>

                                <small>
                                    ${formatStatus(role)}
                                </small>

                            </span>

                        </button>

                        <div
                            class="user-dropdown"
                            id="userDropdown">

                            <a href="/profile.html">
                                Profile
                            </a>

                            ${
                                isCandidate
                                    ? '<a href="/applications.html">My Applications</a>'
                                    : ''
                            }

                            <a
                                href="#"
                                onclick="logout(); return false;">
                                Logout
                            </a>

                        </div>

                    </li>

                </ul>

            </div>

        </nav>
    `;
}


/*
 * =====================================================
 * NAVBAR INITIALIZATION
 * =====================================================
 */

function initNavbar(activePage) {

    const mount =
        document.getElementById(
            'navbarMount'
        );

    if (mount) {

        mount.innerHTML =
            renderAppNavbar(
                activePage
            );
    }


    const toggle =
        document.getElementById(
            'navToggle'
        );

    const menu =
        document.getElementById(
            'navMenu'
        );


    if (toggle && menu) {

        toggle.addEventListener(
            'click',
            () => {

                menu.classList.toggle(
                    'open'
                );
            }
        );
    }


    const userBtn =
        document.getElementById(
            'userMenuBtn'
        );

    const dropdown =
        document.getElementById(
            'userDropdown'
        );


    if (userBtn && dropdown) {

        userBtn.addEventListener(
            'click',
            (e) => {

                e.stopPropagation();

                dropdown.classList.toggle(
                    'open'
                );
            }
        );


        document.addEventListener(
            'click',
            () => {

                dropdown.classList.remove(
                    'open'
                );
            }
        );
    }
}


/*
 * =====================================================
 * STATUS
 * =====================================================
 */

function statusBadgeClass(status) {

    return (
        `status-badge status-${
            (status || '')
                .toLowerCase()
                .replace(/_/g, '-')
        }`
    );
}


/*
 * =====================================================
 * CURRENT PAGE NAVIGATION
 * =====================================================
 */

document.addEventListener(
    'DOMContentLoaded',
    () => {

        const navLinks =
            document.querySelectorAll(
                '.nav-menu a'
            );

        const currentPath =
            window.location.pathname;

        navLinks.forEach(
            link => {

                if (
                    link.getAttribute('href') ===
                    currentPath
                ) {

                    link.classList.add(
                        'active'
                    );
                }
            }
        );
    }
);