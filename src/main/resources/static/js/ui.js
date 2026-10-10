/* ============================================================
   HOME//AUTO — shared UI helpers (vanilla JS, no framework).
   Toasts, modals, confirm dialog, clock, JWT decode, SVG icons.
   Depends on nothing; safe to load on every page.
   ============================================================ */

/* ---------- Toasts ---------- */
function uiToast(message, type) {
    let stack = document.querySelector('.toast-stack');
    if (!stack) {
        stack = document.createElement('div');
        stack.className = 'toast-stack';
        stack.setAttribute('aria-live', 'polite');
        document.body.appendChild(stack);
    }
    const toast = document.createElement('div');
    toast.className = 'toast' + (type ? ' ' + type : '');
    toast.textContent = message;
    stack.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('leaving');
        setTimeout(() => toast.remove(), 220);
    }, 3600);
}

/* ---------- Modal ---------- */
function uiOpenModal(backdropId) {
    const bd = document.getElementById(backdropId);
    if (!bd) return;
    bd.classList.remove('hidden');
    const first = bd.querySelector('input, select, button:not(.modal-close)');
    if (first) first.focus();
    document.body.style.overflow = 'hidden';
}

function uiCloseModal(backdropId) {
    const bd = document.getElementById(backdropId);
    if (!bd) return;
    bd.classList.add('hidden');
    document.body.style.overflow = '';
}

/* Wire a backdrop: close on backdrop click and Escape. */
function uiWireModal(backdropId) {
    const bd = document.getElementById(backdropId);
    if (!bd) return;
    bd.addEventListener('mousedown', (e) => {
        if (e.target === bd) uiCloseModal(backdropId);
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !bd.classList.contains('hidden')) uiCloseModal(backdropId);
    });
}

/* ---------- Confirm dialog (replaces window.confirm) ----------
   uiConfirm('Delete "Lamp"?').then(ok => { if (ok) ... });
---------------------------------------------------------------- */
function uiConfirm(message, options) {
    const opts = options || {};
    return new Promise((resolve) => {
        const bd = document.createElement('div');
        bd.className = 'modal-backdrop';
        bd.innerHTML =
            '<div class="modal" role="alertdialog" aria-modal="true" aria-label="Confirm action">' +
                '<div class="modal-head">' +
                    '<span class="modal-title">&gt; confirm</span>' +
                '</div>' +
                '<p class="mono" style="font-size:0.85rem;color:var(--text)"></p>' +
                '<div class="modal-actions">' +
                    '<button type="button" class="btn ghost" data-act="cancel">Cancel</button>' +
                    '<button type="button" class="btn danger" data-act="ok"></button>' +
                '</div>' +
            '</div>';
        bd.querySelector('p').textContent = message;
        const okBtn = bd.querySelector('[data-act="ok"]');
        okBtn.textContent = opts.confirmLabel || 'Confirm';
        const done = (val) => { bd.remove(); document.body.style.overflow = ''; resolve(val); };
        bd.querySelector('[data-act="cancel"]').addEventListener('click', () => done(false));
        okBtn.addEventListener('click', () => done(true));
        bd.addEventListener('mousedown', (e) => { if (e.target === bd) done(false); });
        document.addEventListener('keydown', function onKey(e) {
            if (e.key === 'Escape') { document.removeEventListener('keydown', onKey); done(false); }
        });
        document.body.appendChild(bd);
        document.body.style.overflow = 'hidden';
        okBtn.focus();
    });
}

/* ---------- Live clock ---------- */
function uiStartClock(el) {
    if (!el) return;
    const fmt = new Intl.DateTimeFormat(undefined, {
        weekday: 'short', year: 'numeric', month: 'short', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
    const tick = () => { el.textContent = fmt.format(new Date()); };
    tick();
    setInterval(tick, 1000);
}

/* ---------- JWT payload decode (no backend call) ---------- */
function uiDecodeJwt(token) {
    try {
        const part = token.split('.')[1];
        const json = atob(part.replace(/-/g, '+').replace(/_/g, '/'));
        return JSON.parse(decodeURIComponent(escape(json)));
    } catch (e) {
        return null;
    }
}

/* Extract a display name from the JWT (subject = email). */
function uiUserEmail() {
    const token = getToken();
    if (!token) return null;
    const payload = uiDecodeJwt(token);
    return payload && payload.sub ? payload.sub : null;
}

function uiInitials(name) {
    if (!name) return '?';
    const clean = name.split('@')[0];
    const parts = clean.split(/[.\s_-]+/).filter(Boolean);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return clean.slice(0, 2).toUpperCase();
}

/* ---------- Inline SVG line icons (stroke = currentColor) ---------- */
const UI_ICONS = {
    dashboard: '<rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>',
    rooms:     '<path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/><path d="M9 21v-6h6v6"/>',
    activity:  '<polyline points="3 12 7 12 10 5 14 19 17 12 21 12"/>',
    users:     '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20c.8-3.4 3.4-5 6.5-5s5.7 1.6 6.5 5"/><circle cx="17" cy="9" r="2.5"/><path d="M16 14.6c2.6.3 4.7 1.8 5.5 4.9"/>',
    devices:   '<rect x="4" y="4" width="16" height="12" rx="2"/><path d="M8 20h8M12 16v4"/>',
    logs:      '<path d="M5 4h14v16H5z"/><path d="M9 8h6M9 12h6M9 16h4"/>',
    settings:  '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M2 12h3M19 12h3M4.9 19.1 7 17M17 7l2.1-2.1"/>',
    search:    '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
    plus:      '<path d="M12 5v14M5 12h14"/>',
    trash:     '<path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3"/>',
    edit:      '<path d="M4 20h4L19.5 8.5a2.1 2.1 0 0 0-3-3L5 17v3z"/><path d="m13.5 6.5 3 3"/>',
    power:     '<path d="M12 3v9"/><path d="M6.3 6.3a8 8 0 1 0 11.4 0"/>',
    bulb:      '<path d="M9 18h6M10 21h4"/><path d="M12 3a6 6 0 0 0-4 10.5c.7.7 1 1.5 1 2.5h6c0-1 .3-1.8 1-2.5A6 6 0 0 0 12 3z"/>',
    fan:       '<circle cx="12" cy="12" r="2"/><path d="M12 10c0-3 1-6 4-6 2 0 3 1.5 3 3 0 2.5-3.5 3-7 3zM14 12c3 0 6 1 6 4 0 2-1.5 3-3 3-2.5 0-3-3.5-3-7zM12 14c0 3-1 6-4 6-2 0-3-1.5-3-3 0-2.5 3.5-3 7-3zM10 12c-3 0-6-1-6-4 0-2 1.5-3 3-3 2.5 0 3 3.5 3 7z"/>',
    ac:        '<rect x="3" y="5" width="18" height="8" rx="2"/><path d="M7 17c0 1.5-1 1.5-1 3M12 17c0 1.5-1 1.5-1 3M17 17c0 1.5-1 1.5-1 3"/><path d="M7 9h10"/>',
    lock:      '<rect x="5" y="11" width="14" height="9" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>',
    sensor:    '<circle cx="12" cy="12" r="2"/><path d="M8.5 8.5a5 5 0 0 0 0 7M15.5 8.5a5 5 0 0 1 0 7M5.6 5.6a9 9 0 0 0 0 12.8M18.4 5.6a9 9 0 0 1 0 12.8"/>',
    curtain:   '<path d="M4 3h16"/><path d="M6 3v18M18 3v18M6 3c3 4 3 14 0 18M18 3c-3 4-3 14 0 18M12 3v18"/>',
    tv:        '<rect x="3" y="6" width="18" height="12" rx="2"/><path d="m9 3 3 3 3-3"/>',
    logout:    '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5M21 12H9"/>',
    trophy:    '<path d="M8 21h8M12 17v4M7 4h10v6a5 5 0 0 1-10 0V4z"/><path d="M7 6H4a1 1 0 0 0-1 1c0 2.5 2 4 4 4M17 6h3a1 1 0 0 1 1 1c0 2.5-2 4-4 4"/>',
    star:      '<path d="m12 3 2.7 5.6 6.3.9-4.5 4.4 1 6.1-5.5-2.9L6.5 20l1-6.1L3 9.5l6.3-.9L12 3z"/>',
    moon:      '<path d="M20 14.5A8.5 8.5 0 0 1 9.5 4 8.5 8.5 0 1 0 20 14.5z"/>',
    menu:      '<path d="M4 7h16M4 12h16M4 17h16"/>',
    box:       '<path d="M21 8 12 3 3 8v8l9 5 9-5V8z"/><path d="M3 8l9 5 9-5M12 13v8"/>'
};

function uiIcon(name, cls) {
    const path = UI_ICONS[name] || UI_ICONS.box;
    const span = document.createElement('span');
    span.className = cls || '';
    span.setAttribute('aria-hidden', 'true');
    span.innerHTML =
        '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" ' +
        'stroke-linecap="round" stroke-linejoin="round" style="width:100%;height:100%;display:block">' +
        path + '</svg>';
    return span;
}

function uiIconSvg(name, size) {
    const path = UI_ICONS[name] || UI_ICONS.box;
    // When a pixel size is given, pin it with attributes AND inline style so the
    // icon can never fall back to a default/100% size (fixes oversized icons).
    const sizeAttr = size ? ' width="' + size + '" height="' + size + '"' : '';
    const sizeStyle = size ? ('width:' + size + 'px;height:' + size + 'px;') : 'width:100%;height:100%;';
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" ' +
           'stroke-linecap="round" stroke-linejoin="round"' + sizeAttr +
           ' style="' + sizeStyle + 'display:block">' +
           path + '</svg>';
}

/* ---------- Device type -> icon name ---------- */
function uiDeviceIcon(device) {
    const type = (device.type || '').toUpperCase();
    const name = (device.name || '').toLowerCase();
    if (name.includes('fan')) return 'fan';
    if (name.includes('ac') || name.includes('air')) return 'ac';
    if (name.includes('lock') || name.includes('door')) return 'lock';
    if (name.includes('curtain') || name.includes('blind')) return 'curtain';
    if (name.includes('tv') || name.includes('television')) return 'tv';
    if (type === 'RGB' || name.includes('light') || name.includes('lamp') || name.includes('bulb')) return 'bulb';
    if (type === 'SENSOR') return 'sensor';
    if (type === 'SLIDER') return 'settings';
    return 'power';
}

/* ---------- Mobile sidebar (hamburger) ---------- */
function uiWireSidebar() {
    const burger = document.querySelector('.hamburger');
    const sidebar = document.querySelector('.sidebar');
    if (!burger || !sidebar) return;
    let backdrop = null;
    const open = () => {
        sidebar.classList.add('open');
        backdrop = document.createElement('div');
        backdrop.className = 'sidebar-backdrop';
        backdrop.addEventListener('click', close);
        document.body.appendChild(backdrop);
    };
    const close = () => {
        sidebar.classList.remove('open');
        if (backdrop) { backdrop.remove(); backdrop = null; }
    };
    burger.addEventListener('click', open);
    sidebar.querySelectorAll('a, button.nav-item').forEach(item =>
        item.addEventListener('click', close));
}

/* ---------- "I'm home / I'm away" segmented switch (localStorage) ---------- */
function uiWirePresence(container) {
    if (!container) return;
    const KEY = 'homeauto-presence';
    const buttons = container.querySelectorAll('button[data-mode]');
    const apply = (mode) => {
        buttons.forEach(b => b.classList.toggle('active', b.dataset.mode === mode));
    };
    apply(localStorage.getItem(KEY) || 'home');
    buttons.forEach(b => b.addEventListener('click', () => {
        localStorage.setItem(KEY, b.dataset.mode);
        apply(b.dataset.mode);
    }));
}
