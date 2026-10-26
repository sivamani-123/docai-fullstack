"use strict";
const $ = (sel) => document.querySelector(sel);
const state = {
  token: localStorage.getItem("token"),
  email: localStorage.getItem("email"),
  sessionId: null,
  docs: [],
};

/* ---------- helpers ---------- */
function el(tag, cls, text) {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  if (text !== undefined) e.textContent = text;   // textContent only: model output is never parsed as HTML
  return e;
}
function setMsg(sel, text, isError) {
  const m = $(sel);
  m.textContent = text || "";
  m.classList.toggle("error", !!isError);
}
async function api(path, { method = "GET", body, form } = {}) {
  const headers = {};
  if (state.token) headers.Authorization = "Bearer " + state.token;
  let payload;
  if (form) payload = form;
  else if (body) { headers["Content-Type"] = "application/json"; payload = JSON.stringify(body); }
  const res = await fetch(path, { method, headers, body: payload });
  let data = null;
  try { data = await res.json(); } catch (e) { /* empty body */ }
  if (res.status === 401 && state.token) { logout(); throw new Error("Your session expired. Please log in again."); }
  if (!res.ok) throw new Error((data && data.error) || "Request failed (" + res.status + ")");
  return data;
}

/* ---------- auth ---------- */
function showAuth() { $("#auth").hidden = false; $("#app").hidden = true; }
function showApp() {
  $("#auth").hidden = true; $("#app").hidden = false;
  $("#who").textContent = state.email;
  loadDocs(); loadSessions();
}
async function authenticate(kind) {
  setMsg("#auth-msg", "");
  try {
    const data = await api("/api/auth/" + kind, { method: "POST", body: { email: $("#email").value, password: $("#password").value } });
    state.token = data.token; state.email = data.email;
    localStorage.setItem("token", data.token); localStorage.setItem("email", data.email);
    $("#password").value = "";
    showApp();
  } catch (e) { setMsg("#auth-msg", e.message, true); }
}
function logout() {
  state.token = null; state.email = null; state.sessionId = null;
  localStorage.removeItem("token"); localStorage.removeItem("email");
  showAuth();
}
$("#btn-login").onclick = () => authenticate("login");
$("#btn-register").onclick = () => authenticate("register");
$("#btn-logout").onclick = logout;

/* ---------- tabs ---------- */
document.querySelectorAll(".tab").forEach((t) => t.onclick = () => {
  document.querySelectorAll(".tab").forEach((x) => x.classList.toggle("active", x === t));
  $("#tab-chat").hidden = t.dataset.tab !== "chat";
  $("#tab-tools").hidden = t.dataset.tab !== "tools";
});

/* ---------- documents ---------- */
function fillScopes() {
  for (const sel of [$("#scope"), $("#search-scope")]) {
    const keep = sel.value;
    sel.innerHTML = "";
    const all = el("option", null, "All documents"); all.value = ""; sel.appendChild(all);
    state.docs.forEach((d) => { const o = el("option", null, d.filename); o.value = d.id; sel.appendChild(o); });
    sel.value = keep;
  }
}
function renderDocs() {
  const ul = $("#docs"); ul.innerHTML = "";
  if (!state.docs.length) ul.appendChild(el("li", "doc", "No documents yet"));
  state.docs.forEach((d) => {
    const li = el("li", "doc");
    li.appendChild(el("div", null, d.filename + " (" + d.chunkCount + " chunks)"));
    const actions = el("div", "actions");
    const sum = el("button", null, "Summarize");
    sum.onclick = async () => {
      sum.disabled = true; sum.textContent = "Working...";
      try { const u = await api("/api/documents/" + d.id + "/summarize", { method: "POST" }); d.summary = u.summary; renderDocs(); }
      catch (e) { setMsg("#upload-msg", e.message, true); sum.disabled = false; sum.textContent = "Summarize"; }
    };
    const del = el("button", null, "Delete");
    del.onclick = async () => {
      if (!confirm("Delete " + d.filename + "?")) return;
      try { await api("/api/documents/" + d.id, { method: "DELETE" }); loadDocs(); }
      catch (e) { setMsg("#upload-msg", e.message, true); }
    };
    actions.append(sum, del); li.appendChild(actions);
    if (d.summary) li.appendChild(el("p", "sum", d.summary));
    ul.appendChild(li);
  });
  fillScopes();
}
async function loadDocs() {
  try { state.docs = await api("/api/documents"); renderDocs(); } catch (e) { setMsg("#upload-msg", e.message, true); }
}
$("#file").onchange = async (ev) => {
  const f = ev.target.files[0]; if (!f) return;
  setMsg("#upload-msg", "Indexing " + f.name + ". Large files can take a minute...");
  const form = new FormData(); form.append("file", f);
  try { await api("/api/documents", { method: "POST", form }); setMsg("#upload-msg", f.name + " is ready."); loadDocs(); }
  catch (e) { setMsg("#upload-msg", e.message, true); }
  ev.target.value = "";
};

/* ---------- chat ---------- */
function renderSources(sources) {
  if (!sources || !sources.length) return null;
  const det = el("details"); det.appendChild(el("summary", null, "Sources (" + sources.length + ")"));
  sources.forEach((s, i) => {
    const d = el("div", "src");
    const label = "[" + (i + 1) + "] " + s.filename + (s.page != null ? ", page " + s.page : "") + " - similarity " + s.score.toFixed(2);
    d.appendChild(el("b", null, label)); d.appendChild(el("div", null, s.text));
    det.appendChild(d);
  });
  return det;
}
function addTurn(question, answer, sources) {
  const log = $("#log"); const empty = log.querySelector(".empty"); if (empty) empty.remove();
  if (question !== null) log.appendChild(el("div", "q", question));
  const a = el("div", "a", answer); log.appendChild(a);
  const src = renderSources(sources); if (src) log.appendChild(src);
  log.scrollTop = log.scrollHeight;
  return a;
}
async function loadSessions() {
  try {
    const list = await api("/api/chat/sessions");
    const ul = $("#sessions"); ul.innerHTML = "";
    if (!list.length) ul.appendChild(el("li", "doc", "No chats yet"));
    list.forEach((s) => {
      const li = el("li", s.id === state.sessionId ? "active" : "", s.title);
      li.onclick = () => openSession(s.id);
      ul.appendChild(li);
    });
  } catch (e) { /* shown elsewhere */ }
}
async function openSession(id) {
  state.sessionId = id; $("#log").innerHTML = ""; setMsg("#chat-msg", "");
  try {
    const msgs = await api("/api/chat/sessions/" + id + "/messages");
    msgs.forEach((m) => {
      if (m.role === "USER") $("#log").appendChild(el("div", "q", m.content));
      else addTurn(null, m.content, m.sources);
    });
  } catch (e) { setMsg("#chat-msg", e.message, true); }
  loadSessions();
}
$("#btn-new").onclick = () => {
  state.sessionId = null; $("#log").innerHTML = '<p class="empty">New chat. Ask a question below.</p>'; loadSessions();
};
async function ask() {
  const q = $("#question").value.trim(); if (q.length < 3) return;
  setMsg("#chat-msg", ""); $("#btn-ask").disabled = true; $("#question").value = "";
  const pending = addTurn(q, "Thinking...", null);
  try {
    const res = await api("/api/chat/ask", { method: "POST", body: {
      question: q, sessionId: state.sessionId, mode: $("#mode").value,
      documentId: $("#scope").value ? Number($("#scope").value) : null } });
    state.sessionId = res.sessionId;
    pending.textContent = res.answer;
    const src = renderSources(res.sources); if (src) pending.after(src);
    loadSessions();
  } catch (e) { pending.textContent = e.message; pending.style.borderLeftColor = "var(--err)"; }
  $("#btn-ask").disabled = false; $("#log").scrollTop = $("#log").scrollHeight;
}
$("#btn-ask").onclick = ask;
$("#question").addEventListener("keydown", (e) => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); ask(); } });

/* ---------- tools ---------- */
$("#btn-search").onclick = async () => {
  const q = $("#search-q").value.trim(); if (!q) return;
  setMsg("#tools-msg", ""); const out = $("#search-out"); out.innerHTML = "";
  try {
    const scope = $("#search-scope").value;
    const res = await api("/api/ai/search", { method: "POST", body: { query: q, documentId: scope ? Number(scope) : null } });
    if (!res.length) out.appendChild(el("p", "muted", "No documents to search yet."));
    res.forEach((s) => {
      const d = el("div", "src");
      d.appendChild(el("b", null, s.filename + (s.page != null ? ", page " + s.page : "") + " - similarity " + s.score.toFixed(2)));
      d.appendChild(el("div", null, s.text)); out.appendChild(d);
    });
  } catch (e) { setMsg("#tools-msg", e.message, true); }
};
$("#btn-classify").onclick = async () => {
  const text = $("#cls-text").value.trim();
  const labels = $("#cls-labels").value.split(",").map((s) => s.trim()).filter(Boolean);
  if (!text || !labels.length) return;
  setMsg("#tools-msg", ""); $("#cls-out").textContent = "Classifying...";
  try { const r = await api("/api/ai/classify", { method: "POST", body: { text, labels } }); $("#cls-out").textContent = "Label: " + r.label; }
  catch (e) { $("#cls-out").textContent = ""; setMsg("#tools-msg", e.message, true); }
};

/* ---------- start ---------- */
state.token ? showApp() : showAuth();
