const $ = s => document.querySelector(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const S = { auth: sessionStorage.getItem('hc.auth'), user: sessionStorage.getItem('hc.user'),
    eps: [], slug: null, reqs: [], sel: null, filter: '', timer: null, sig: '' };

// X-Requested-With stops the browser's native login popup on a 401
async function api(path, opt = {}) {
    const r = await fetch(path, { ...opt, headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest',
            ...(S.auth ? { Authorization: S.auth } : {}) } });
    const t = await r.text(); let b = t; try { b = JSON.parse(t); } catch {}
    if (!r.ok) {
        if (r.status === 401 && S.auth && !opt.login) logout();
        const e = new Error(typeof b === 'string' && b ? b : `Request failed (${r.status})`); e.status = r.status; throw e;
    }
    return b;
}
function toast(m) { const t = document.createElement('div'); t.className = 'toast'; t.textContent = m; document.body.append(t); setTimeout(() => t.remove(), 2200); }
const copy = (txt) => navigator.clipboard.writeText(txt).then(() => toast('Copied to clipboard'));
const fmt = iso => String(iso || '').replace('T', ' ').slice(0, 19);
const qs = r => Object.entries(r.queryParams || {}).map(([k, v]) => `${k}=${v}`).join('&');

/* ---------- auth ---------- */
let mode = 'login';
function setMode(m) {
    mode = m; const login = m === 'login';
    $('#atitle').textContent = login ? 'Sign in' : 'Create your account';
    $('#asub').textContent = login ? 'Use your HookCatcher account.' : 'Pick a username and a password of 6+ characters.';
    $('#asubmit').textContent = login ? 'Sign in' : 'Create account';
    $('#p').autocomplete = login ? 'current-password' : 'new-password';
    $('#aswitch').innerHTML = login ? 'New here? <button type="button" id="sw">Create an account</button>'
        : 'Already registered? <button type="button" id="sw">Sign in</button>';
    $('#sw').onclick = () => setMode(login ? 'register' : 'login'); $('#aerr').textContent = '';
}
$('#aform').onsubmit = async e => {
    e.preventDefault(); const u = $('#u').value.trim(), p = $('#p').value; $('#aerr').textContent = '';
    try {
        if (mode === 'register') { await api('/api/auth/register', { method: 'POST', body: JSON.stringify({ username: u, password: p }) }); toast('Account created'); }
        S.auth = 'Basic ' + btoa(unescape(encodeURIComponent(`${u}:${p}`))); S.user = u;
        await api('/api/endpoints', { login: true });
        sessionStorage.setItem('hc.auth', S.auth); sessionStorage.setItem('hc.user', u); start();
    } catch (err) { S.auth = null; $('#aerr').textContent = err.status === 401 ? 'Wrong username or password.' : err.message; }
};
function start() {
    $('#auth').classList.add('hidden'); $('#app').classList.remove('hidden'); $('#who').textContent = S.user;
    loadEps(true).catch(() => {}); clearInterval(S.timer); S.timer = setInterval(() => poll(), 3000);
}
function logout() {
    clearInterval(S.timer); sessionStorage.clear();
    Object.assign(S, { auth: null, user: null, eps: [], slug: null, reqs: [], sel: null, sig: '' });
    $('#app').classList.add('hidden'); $('#auth').classList.remove('hidden'); $('#p').value = '';
}
$('#logout').onclick = logout;

/* ---------- endpoints ---------- */
async function loadEps(pick) {
    S.eps = await api('/api/endpoints');
    if (pick && S.eps.length && !S.eps.find(e => e.slug === S.slug)) S.slug = S.eps[0].slug;
    renderEps(); await poll(true);
}
function renderEps() {
    $('#eplist').innerHTML = S.eps.length ? S.eps.map(e => `
    <div class="ep ${e.slug === S.slug ? 'on' : ''}" data-s="${esc(e.slug)}">
      <div style="min-width:0"><div class="n">${esc(e.label || 'Untitled endpoint')}</div><div class="s">${esc(e.slug)}</div></div>
      <div class="acts"><button class="ic" data-copy="${esc(e.url)}" title="Copy hook URL">Copy</button><button class="ic d" data-del="${esc(e.slug)}" title="Delete endpoint">Delete</button></div>
    </div>`).join('') : '<div class="empty">No endpoints yet. Create one above and you get a URL that captures anything sent to it.</div>';
}
$('#newform').onsubmit = async e => {
    e.preventDefault(); const label = $('#label').value.trim();
    try {
        const ep = await api('/api/endpoints', { method: 'POST', body: JSON.stringify({ label: label || null }) });
        $('#label').value = ''; S.slug = ep.slug; S.sel = null; S.sig = ''; await loadEps(); toast('Endpoint created');
    } catch (x) { toast(x.message); }
};

/* ---------- requests ---------- */
async function poll(force) {
    if (!S.slug) { S.reqs = []; renderReqs(); renderDetail(); return; }
    try {
        const r = await api(`/api/endpoints/${S.slug}/requests${S.filter ? '?method=' + S.filter : ''}`);
        const sig = S.slug + S.filter + r.map(x => x.id).join();
        if (!force && sig === S.sig) return;
        S.sig = sig; S.reqs = r;
        if (!r.find(x => x.id === S.sel)) S.sel = r[0]?.id || null;
        renderReqs(); renderDetail();
    } catch (e) { if (e.status === 404) { S.slug = null; S.sel = null; S.reqs = []; renderReqs(); renderDetail(); } }
}
function renderReqs() {
    const ep = S.eps.find(e => e.slug === S.slug);
    $('#rtitle').textContent = ep ? (ep.label || ep.slug) : 'Requests';
    $('#rlist').innerHTML = S.reqs.length ? S.reqs.map(r => `
    <div class="rq ${r.id === S.sel ? 'on' : ''}" data-id="${esc(r.id)}">
      <span class="m" data-m="${esc(r.method)}">${esc(r.method)}</span>
      <div class="rm"><div class="rp">${esc(qs(r) || 'No query string')}</div><div class="rt">${esc(fmt(r.receivedAt))} from ${esc(r.ip)}</div></div>
    </div>`).join('') : `<div class="empty">${S.filter ? 'No ' + S.filter + ' requests captured.' : 'Nothing captured yet.'}</div>`;
}
const kv = o => { const e = Object.entries(o || {}); return e.length ? e.map(([k, v]) => `<div class="kv"><span>${esc(k)}</span><span>${esc(v)}</span></div>`).join('') : '<div class="none">None</div>'; };
function pretty(b) { if (!b) return ''; try { return JSON.stringify(JSON.parse(b), null, 2); } catch { return b; } }
function renderDetail() {
    const r = S.reqs.find(x => x.id === S.sel), ep = S.eps.find(e => e.slug === S.slug), d = $('#detail');
    if (!ep) { d.innerHTML = '<div class="empty">Create or select an endpoint to start capturing requests.</div>'; return; }
    if (!r) {
        d.innerHTML = `<div class="empty"><b>Listening on this endpoint</b><br>Send any request to its URL and it shows up here within a few seconds.
      <code>curl -X POST ${esc(ep.url)} \\\n  -H "Content-Type: application/json" \\\n  -d '{"hello":"world"}'</code>
      <button class="btn" style="margin-top:14px" data-copy="${esc(ep.url)}">Copy URL</button></div>`; return;
    }
    const body = pretty(r.body);
    d.innerHTML = `
    <div class="dh"><span class="m" data-m="${esc(r.method)}">${esc(r.method)}</span>
      <div class="t">${esc(fmt(r.receivedAt))}<small>from ${esc(r.ip)}${r.contentType ? ' as ' + esc(r.contentType) : ''}</small></div>
      <div class="sp"></div><button class="btn primary" id="rp">Replay</button></div>
    <div class="sec"><h4>Query parameters</h4>${kv(r.queryParams)}</div>
    <div class="sec"><h4>Headers</h4>${kv(r.headers)}</div>
    <div class="sec"><h4>Body</h4>${body ? `<pre>${esc(body)}</pre>` : '<div class="none">No body</div>'}</div>`;
}
$('#mf').onchange = e => { S.filter = e.target.value; S.sig = ''; poll(true); };

/* ---------- replay ---------- */
const openReplay = () => { $('#rout').innerHTML = ''; $('#modal').classList.remove('hidden'); $('#target').focus(); };
const closeReplay = () => $('#modal').classList.add('hidden');
$('#rclose').onclick = closeReplay;
$('#modal').onclick = e => { if (e.target.id === 'modal') closeReplay(); };
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeReplay(); });
$('#rform').onsubmit = async e => {
    e.preventDefault(); const out = $('#rout'); out.innerHTML = '<div class="rs">Sending...</div>';
    try {
        const res = await api(`/api/endpoints/${S.slug}/requests/${S.sel}/replay`, { method: 'POST', body: JSON.stringify({ targetUrl: $('#target').value.trim() }) });
        out.innerHTML = `<div class="rs ${res.statusCode < 400 ? 'ok' : 'bad'}">Target responded with ${res.statusCode}</div><pre>${esc(res.responseBody)}</pre>`;
    } catch (x) { out.innerHTML = `<div class="rs bad">${esc(x.message)}</div>`; }
};

/* ---------- clicks ---------- */
document.addEventListener('click', async e => {
    const t = e.target;
    if (t.dataset.copy) { e.stopPropagation(); return copy(t.dataset.copy); }
    if (t.dataset.del) {
        e.stopPropagation(); if (!confirm('Delete this endpoint and its captured requests?')) return;
        try { await api('/api/endpoints/' + t.dataset.del, { method: 'DELETE' }); if (S.slug === t.dataset.del) { S.slug = null; S.sel = null; }
            S.sig = ''; await loadEps(true); toast('Endpoint deleted'); } catch (x) { toast(x.message); }
        return;
    }
    if (t.id === 'rp') return openReplay();
    const ep = t.closest('.ep'); if (ep) { S.slug = ep.dataset.s; S.sel = null; S.sig = ''; renderEps(); poll(true); return; }
    const rq = t.closest('.rq'); if (rq) { S.sel = rq.dataset.id; renderReqs(); renderDetail(); }
});

setMode('login');
if (S.auth) start();