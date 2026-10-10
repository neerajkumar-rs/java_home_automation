/* ============================================================
   HOME//AUTO — admin console logic (vanilla JS).
   All fetch URLs, request bodies, and token handling unchanged.
   ============================================================ */

const alertEl = document.getElementById('alert');
document.getElementById('logoutBtn').addEventListener('click', logout);

function showError(msg) {
    alertEl.textContent = '> error: ' + msg;
    alertEl.classList.remove('hidden');
    uiToast(msg, 'error');
}

function el(tag, className, text) {
    const e = document.createElement(tag);
    if (className) e.className = className;
    if (text !== undefined) e.textContent = text;
    return e;
}

function injectIcons(root) {
    (root || document).querySelectorAll('[data-icon]').forEach(slot => {
        if (slot.dataset.iconDone) return;
        slot.innerHTML = uiIconSvg(slot.dataset.icon);
        slot.dataset.iconDone = '1';
        if (!slot.style.width) {
            slot.style.display = 'inline-block';
            slot.style.width = '1em';
            slot.style.height = '1em';
        }
    });
}

async function guard() {
    const res = await apiFetch('/api/auth/me');
    if (!res.ok) { window.location.href = '/login.html'; return false; }
    const me = await res.json();
    if (!me.isAdmin) { window.location.href = '/dashboard.html'; return false; }
    renderIdentity(me);
    return true;
}

function renderIdentity(me) {
    const email = (me && (me.email || me.username)) || uiUserEmail() || 'admin';
    const name = (me && (me.firstName || me.username)) || email.split('@')[0];
    document.getElementById('userName').textContent = name;
    document.getElementById('userEmail').textContent = email;
    document.getElementById('userAvatar').textContent = uiInitials(email);
    document.getElementById('greetingUser').textContent = name;
}

/* ---------- users ---------- */
async function loadUsers() {
    const res = await apiFetch('/api/admin/users');
    if (!res.ok) { showError(await apiErrorMessage(res, 'Could not load users')); return; }
    const users = await res.json();
    const body = document.getElementById('usersBody');
    body.replaceChildren();

    users.forEach(u => {
        const tr = document.createElement('tr');
        tr.appendChild(el('td', null, u.username));
        tr.appendChild(el('td', 'td-mono', u.email));
        const roleTd = el('td');
        const rolePill = el('span', 'pill ' + ((u.role || '').toUpperCase() === 'ADMIN' ? 'warn' : 'cyan'), u.role || 'USER');
        roleTd.appendChild(rolePill);
        tr.appendChild(roleTd);

        const statusTd = el('td');
        statusTd.appendChild(el('span', 'pill ' + (u.active ? 'on' : 'err'), u.active ? 'active' : 'disabled'));
        tr.appendChild(statusTd);

        const actionTd = document.createElement('td');
        const btn = el('button', 'icon-btn' + (u.active ? ' danger' : ''));
        btn.setAttribute('data-tooltip', u.active ? 'Disable user' : 'Enable user');
        btn.setAttribute('aria-label', (u.active ? 'Disable ' : 'Enable ') + u.username);
        btn.innerHTML = uiIconSvg(u.active ? 'trash' : 'power');
        btn.addEventListener('click', () => toggleUser(u));
        actionTd.appendChild(btn);
        tr.appendChild(actionTd);

        body.appendChild(tr);
    });

    document.getElementById('statUsers').textContent = users.length;
    document.getElementById('statInactive').textContent = users.filter(u => !u.active).length;
}

async function toggleUser(u) {
    const action = u.active ? 'deactivate' : 'activate';
    const res = await apiFetch('/api/admin/users/' + u.id + '/' + action, { method: 'POST' });
    if (!res.ok) { showError(await apiErrorMessage(res, 'Could not update user')); return; }
    uiToast(u.username + ' ' + action + 'd', 'success');
    loadUsers();
}

/* ---------- devices ---------- */
async function loadDevices() {
    const res = await apiFetch('/api/admin/devices');
    if (!res.ok) { showError(await apiErrorMessage(res, 'Could not load devices')); return; }
    const devices = await res.json();
    const body = document.getElementById('devicesBody');
    body.replaceChildren();

    devices.forEach(d => {
        const tr = document.createElement('tr');
        tr.appendChild(el('td', null, d.name));
        tr.appendChild(el('td', 'td-mono', d.type));

        const stateTd = el('td');
        stateTd.appendChild(el('span', 'pill ' + (d.state === 'ON' ? 'on' : 'off'), d.state));
        tr.appendChild(stateTd);

        tr.appendChild(el('td', 'td-mono', d.ownerEmail));
        tr.appendChild(el('td', 'td-mono', d.room || '—'));
        body.appendChild(tr);
    });

    document.getElementById('statDevices').textContent = devices.length;
    document.getElementById('statOnline').textContent = devices.filter(d => d.state === 'ON').length;
}

/* ---------- wiring ---------- */
document.querySelectorAll('[data-nav]').forEach(a => {
    a.addEventListener('click', () => {
        document.querySelectorAll('[data-nav]').forEach(x => x.classList.remove('active'));
        a.classList.add('active');
    });
});

/* ---------- init ---------- */
injectIcons(document);
uiStartClock(document.getElementById('clock'));
uiWireSidebar();

(async () => {
    if (!requireAuth()) return;
    if (await guard()) {
        loadUsers();
        loadDevices();
    }
})();
