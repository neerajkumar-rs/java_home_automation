/* ============================================================
   HOME//AUTO — user dashboard logic (vanilla JS).
   All fetch URLs, request bodies, and token handling unchanged.
   ============================================================ */

const alertEl  = document.getElementById('alert');
const devicesEl = document.getElementById('devices');
const logsEl   = document.getElementById('logs');
let devices = [];
let logsCache = [];
let activeRoom = 'all';
let searchQuery = '';

/* ---------- helpers ---------- */
function showError(msg) {
    alertEl.textContent = '> error: ' + msg;
    alertEl.classList.remove('hidden');
    uiToast(msg, 'error');
}
function clearError() { alertEl.classList.add('hidden'); }

function el(tag, className, text) {
    const e = document.createElement(tag);
    if (className) e.className = className;
    if (text !== undefined) e.textContent = text;
    return e;
}

/* Inject all inline SVG icons declared via data-icon. */
function injectIcons(root) {
    (root || document).querySelectorAll('[data-icon]').forEach(slot => {
        if (slot.dataset.iconDone) return;
        // The search icon must be pinned to 16px (BUG 1 fix).
        const size = slot.dataset.icon === 'search' ? 16 : undefined;
        slot.innerHTML = uiIconSvg(slot.dataset.icon, size);
        slot.dataset.iconDone = '1';
        // Only apply a default 1em size when the slot has no explicit CSS sizing
        // (search icon, tile icons, etc. size themselves via theme.css).
        const sizedByCss = slot.classList.contains('tile-icon') || slot.dataset.icon === 'search';
        if (!slot.style.width && !sizedByCss) {
            slot.style.display = 'inline-block';
            slot.style.width = '1em';
            slot.style.height = '1em';
        }
    });
}

/* ---------- data loading ---------- */
async function loadDevices() {
    const res = await apiFetch('/api/devices');
    if (!res.ok) { showError(await apiErrorMessage(res, 'Could not load devices')); return; }
    devices = await res.json();
    renderDevices();
    renderStats();
    renderRoomChips();
    loadLogs();
}

/* ---------- device rendering ---------- */
function getRooms() {
    const rooms = new Set();
    devices.forEach(d => { if (d.room) rooms.add(d.room); });
    return Array.from(rooms).sort();
}

function renderRoomChips() {
    const row = document.getElementById('roomChips');
    row.replaceChildren();
    const mk = (label, value) => {
        const c = el('button', 'chip' + (activeRoom === value ? ' active' : ''), label);
        c.dataset.room = value;
        c.addEventListener('click', () => { activeRoom = value; renderRoomChips(); renderDevices(); });
        return c;
    };
    row.appendChild(mk('All', 'all'));
    getRooms().forEach(r => row.appendChild(mk(r, r)));
}

function visibleDevices() {
    return devices.filter(d => {
        const roomOk = activeRoom === 'all' || (d.room || '') === activeRoom;
        const q = searchQuery.trim().toLowerCase();
        const qOk = !q || (d.name || '').toLowerCase().includes(q) || (d.room || '').toLowerCase().includes(q) || (d.type || '').toLowerCase().includes(q);
        return roomOk && qOk;
    });
}

function renderDevices() {
    devicesEl.replaceChildren();
    const list = visibleDevices();
    const hasAny = devices.length > 0;
    const hasVisible = list.length > 0;

    document.getElementById('emptyState').classList.toggle('hidden', hasAny);
    document.getElementById('emptyMsg').classList.add('hidden');

    if (hasAny && !hasVisible) {
        const msg = document.getElementById('emptyMsg');
        msg.textContent = '';
        msg.className = 'empty-state';
        msg.innerHTML = '<div class="empty-hint">&gt; no devices match this filter_</div>';
        msg.classList.remove('hidden');
    }

    list.forEach(d => devicesEl.appendChild(renderDevice(d)));
    injectIcons(devicesEl);
}

function renderDevice(d) {
    const isOn = d.state === 'ON';
    const tile = el('div', 'tile device-card ' + (isOn ? 'on' : 'off'));

    // delete (revealed on hover)
    const delBtn = el('button', 'tile-delete');
    delBtn.setAttribute('data-tooltip', 'Delete device');
    delBtn.setAttribute('aria-label', 'Delete ' + (d.name || 'device'));
    delBtn.innerHTML = uiIconSvg('trash');
    delBtn.addEventListener('click', () => removeDevice(d));
    tile.appendChild(delBtn);

    // head: icon
    const head = el('div', 'tile-head');
    const icon = el('span', 'tile-icon');
    icon.innerHTML = uiIconSvg(uiDeviceIcon(d));
    head.appendChild(icon);
    tile.appendChild(head);

    // name + room
    tile.appendChild(el('div', 'tile-name', d.name));
    tile.appendChild(el('div', 'tile-room', (d.type || '') + (d.room ? ' · ' + d.room : '')));

    // foot: toggle + status pill
    const foot = el('div', 'tile-foot');

    const toggleLabel = el('label', 'toggle');
    const cb = document.createElement('input');
    cb.type = 'checkbox';
    cb.checked = isOn;
    cb.setAttribute('aria-label', 'Toggle ' + (d.name || 'device'));
    cb.addEventListener('change', () => setState(d, cb.checked ? 'ON' : 'OFF'));
    toggleLabel.appendChild(cb);
    toggleLabel.appendChild(el('span', 'track'));
    toggleLabel.appendChild(el('span', 'thumb'));
    foot.appendChild(toggleLabel);

    foot.appendChild(el('span', 'pill ' + (isOn ? 'on' : 'off'), d.state));
    tile.appendChild(foot);

    // extra controls (slider / color)
    if (d.type === 'SLIDER' || d.type === 'RGB') {
        const extra = el('div', 'tile-extra');
        const slider = document.createElement('input');
        slider.type = 'range'; slider.min = 0; slider.max = 100; slider.value = d.value || 0;
        slider.setAttribute('aria-label', 'Adjust ' + (d.name || 'device'));
        slider.addEventListener('change', () => setState(d, d.state, Number(slider.value)));
        extra.appendChild(slider);

        if (d.type === 'RGB') {
            const color = document.createElement('input');
            color.type = 'color';
            color.value = rgbToHex(d.red, d.green, d.blue);
            color.setAttribute('aria-label', 'Set color for ' + (d.name || 'device'));
            color.addEventListener('change', () => setColor(d, color.value));
            extra.appendChild(color);
        }
        tile.appendChild(extra);
    }

    return tile;
}

function rgbToHex(r, g, b) {
    const h = n => Number(n || 0).toString(16).padStart(2, '0');
    return '#' + h(r) + h(g) + h(b);
}

/* ---------- device actions (unchanged endpoints) ---------- */
async function setState(d, state, value) {
    clearError();
    const body = { state };
    if (value !== undefined) body.value = value;
    const res = await apiFetch('/api/devices/' + d.id + '/state', { method: 'POST', body: JSON.stringify(body) });
    if (!res.ok) { showError(await apiErrorMessage(res, 'State change failed')); return; }
    uiToast(d.name + ' -> ' + state, state === 'ON' ? 'success' : '');
    loadDevices();
}

async function setColor(d, hex) {
    clearError();
    const body = {
        red: parseInt(hex.slice(1, 3), 16),
        green: parseInt(hex.slice(3, 5), 16),
        blue: parseInt(hex.slice(5, 7), 16)
    };
    const res = await apiFetch('/api/devices/' + d.id + '/color', { method: 'POST', body: JSON.stringify(body) });
    if (!res.ok) { showError(await apiErrorMessage(res, 'Color change failed')); return; }
    loadDevices();
}

async function removeDevice(d) {
    const ok = await uiConfirm('Delete "' + (d.name || 'device') + '"?', { confirmLabel: 'Delete' });
    if (!ok) return;
    clearError();
    const res = await apiFetch('/api/devices/' + d.id, { method: 'DELETE' });
    if (!res.ok) { showError(await apiErrorMessage(res, 'Delete failed')); return; }
    uiToast('Device removed', 'success');
    loadDevices();
}

/* ---------- logs / activity ---------- */
async function loadLogs() {
    const emptyLine = document.getElementById('logsEmptyLine');
    const logsEmptyLegacy = document.getElementById('logsEmpty');
    logsEl.replaceChildren();
    logsEmptyLegacy.classList.add('hidden');

    if (devices.length === 0) {
        logsCache = [];
        logsEl.appendChild(emptyLine);
        emptyLine.classList.remove('hidden');
        renderGamify();
        return;
    }
    const res = await apiFetch('/api/devices/' + devices[0].id + '/logs');
    if (!res.ok) { renderGamify(); return; }
    const logs = await res.json();
    logsCache = Array.isArray(logs) ? logs : [];

    if (logsCache.length === 0) {
        logsEl.appendChild(emptyLine);
        emptyLine.classList.remove('hidden');
    } else {
        logsCache.slice(0, 12).forEach(l => logsEl.appendChild(renderLogLine(l)));
    }
    renderGamify();
}

function formatLogTime(l) {
    const raw = l.logTime || l.timestamp || l.createdAt;
    if (!raw) return '--:--:--';
    const d = new Date(raw);
    if (isNaN(d)) return '--:--:--';
    return d.toTimeString().slice(0, 8);
}

function renderLogLine(l) {
    const line = el('div', 'terminal-line');
    const time = el('span', 't-time', '[' + formatLogTime(l) + '] ');
    line.appendChild(time);

    const prev = (l.previousState ?? '?');
    const next = (l.newState ?? '?');
    const cls = next === 'ON' ? 't-on' : (next === 'OFF' ? 't-off' : 't-meta');
    line.appendChild(el('span', cls, prev + ' -> ' + next));

    if (l.newValue) {
        line.appendChild(el('span', 't-meta', ' (' + (l.previousValue ?? '') + ' -> ' + l.newValue + ')'));
    }
    const meta = [];
    if (l.triggeredBy) meta.push(l.triggeredBy);
    if (l.changeReason) meta.push(l.changeReason);
    if (meta.length) line.appendChild(el('span', 't-meta', ' · ' + meta.join(' · ')));
    return line;
}

/* ---------- stats ---------- */
function renderStats() {
    document.getElementById('statTotal').textContent = devices.length;
    document.getElementById('statOn').textContent = devices.filter(d => d.state === 'ON').length;
    document.getElementById('statRooms').textContent = getRooms().length;
    const today = new Date(); today.setHours(0,0,0,0);
    const changes = logsCache.filter(l => {
        const raw = l.logTime || l.timestamp || l.createdAt;
        if (!raw) return false;
        const d = new Date(raw);
        return !isNaN(d) && d >= today;
    }).length;
    document.getElementById('statChanges').textContent = changes;
}

/* ---------- gamified strip ---------- */
function renderGamify() {
    renderHeatmap();
    renderBadges();
    renderStats();
}

function renderHeatmap() {
    const hm = document.getElementById('heatmap');
    hm.replaceChildren();
    const days = 12 * 7; // 12 weeks
    const counts = {};
    logsCache.forEach(l => {
        const raw = l.logTime || l.timestamp || l.createdAt;
        if (!raw) return;
        const d = new Date(raw);
        if (isNaN(d)) return;
        const key = d.toISOString().slice(0, 10);
        counts[key] = (counts[key] || 0) + 1;
    });
    const today = new Date(); today.setHours(0,0,0,0);
    for (let i = days - 1; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(d.getDate() - i);
        const key = d.toISOString().slice(0, 10);
        const c = counts[key] || 0;
        const level = c === 0 ? 0 : c < 2 ? 1 : c < 4 ? 2 : c < 7 ? 3 : 4;
        const cell = el('div', 'hm-cell' + (level ? ' l' + level : ''));
        cell.title = key + ': ' + c + ' change' + (c === 1 ? '' : 's');
        hm.appendChild(cell);
    }
}

function renderBadges() {
    const wrap = document.getElementById('badges');
    wrap.replaceChildren();

    // Derive unlocks from existing data only.
    let nightOwl = false;
    logsCache.forEach(l => {
        const raw = l.logTime || l.timestamp || l.createdAt;
        if (!raw) return;
        const h = new Date(raw).getHours();
        if (h >= 0 && h < 5) nightOwl = true;
    });

    const badges = [
        { icon: 'star',   name: 'First device', desc: 'provision 1 node',  unlocked: devices.length >= 1 },
        { icon: 'trophy', name: '5 devices',    desc: 'provision 5 nodes', unlocked: devices.length >= 5 },
        { icon: 'moon',   name: 'Night owl',    desc: 'activity 00-05h',   unlocked: nightOwl }
    ];

    badges.forEach(b => {
        const card = el('div', 'ach-badge ' + (b.unlocked ? 'unlocked' : 'locked'));
        const icon = el('span');
        icon.style.width = '26px'; icon.style.height = '26px'; icon.style.display = 'inline-block';
        icon.innerHTML = uiIconSvg(b.icon);
        card.appendChild(icon);
        card.appendChild(el('div', 'ach-name', b.name));
        card.appendChild(el('div', 'ach-desc', b.unlocked ? 'UNLOCKED' : b.desc));
        wrap.appendChild(card);
    });
}

/* ---------- identity (decoded from JWT, no backend call) ---------- */
function renderIdentity() {
    const email = uiUserEmail() || 'user';
    const name = email.split('@')[0];
    document.getElementById('userName').textContent = name;
    document.getElementById('userEmail').textContent = email;
    document.getElementById('userAvatar').textContent = uiInitials(email);
    document.getElementById('greetingUser').textContent = name;
}

/* ---------- wiring ---------- */
document.getElementById('logoutBtn').addEventListener('click', logout);

document.getElementById('searchInput').addEventListener('input', (e) => {
    searchQuery = e.target.value;
    renderDevices();
});

// Add device modal
uiWireModal('addModal');
document.getElementById('addDeviceBtn').addEventListener('click', () => uiOpenModal('addModal'));
document.getElementById('addModalClose').addEventListener('click', () => uiCloseModal('addModal'));
document.getElementById('addModalCancel').addEventListener('click', () => uiCloseModal('addModal'));

document.getElementById('addForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    clearError();
    const body = {
        name: document.getElementById('newName').value.trim(),
        type: document.getElementById('newType').value,
        room: document.getElementById('newRoom').value.trim() || null
    };
    const res = await apiFetch('/api/devices', { method: 'POST', body: JSON.stringify(body) });
    if (!res.ok) { showError(await apiErrorMessage(res, 'Could not add device')); return; }
    document.getElementById('newName').value = '';
    document.getElementById('newRoom').value = '';
    uiCloseModal('addModal');
    uiToast(body.name + ' added', 'success');
    loadDevices();
});

// Sidebar nav active state (visual only)
document.querySelectorAll('[data-nav]').forEach(a => {
    a.addEventListener('click', () => {
        document.querySelectorAll('[data-nav]').forEach(x => x.classList.remove('active'));
        a.classList.add('active');
    });
});

/* ---------- init ---------- */
injectIcons(document);
uiStartClock(document.getElementById('clock'));
uiWirePresence(document.getElementById('presenceSwitch'));
uiWireSidebar();
renderIdentity();

if (requireAuth()) loadDevices();
