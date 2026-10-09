// Shared auth helpers. Plain JavaScript, no framework.

const TOKEN_KEY = 'token';

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
}

/**
 * fetch() wrapper that attaches the JWT Authorization header.
 * On a 401 the session is cleared and the browser is sent back to login.
 * Never throws on HTTP errors: returns the Response; callers read res.ok.
 */
async function apiFetch(url, options = {}) {
    const token = getToken();
    const headers = Object.assign({}, options.headers);
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }
    const res = await fetch(url, Object.assign({}, options, { headers }));
    if (res.status === 401) {
        clearSession();
        window.location.href = '/login.html';
        // Return a never-resolving promise so callers stop after redirect.
        return new Promise(() => {});
    }
    return res;
}

/** Read the standard API error body and return a user-friendly message. */
async function apiErrorMessage(res, fallback) {
    try {
        const data = await res.json();
        return data.message || data.error || fallback;
    } catch (e) {
        return fallback;
    }
}

function logout() {
    clearSession();
    window.location.href = '/login.html';
}

/** Redirect to login when there is no token. Returns true when logged in. */
function requireAuth() {
    if (!getToken()) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}
