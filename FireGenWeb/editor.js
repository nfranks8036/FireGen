// =========================================================================
// Incident editor (editor.html)
//   - log in:  POST /api/auth/login            {"username","password"}  ->  {"token"}   (no wrapper)
//   - create:  POST /api/incidents/create      Bearer token; only "type" is required
//   - edit:    POST /api/incidents/{id}        Bearer token; every field optional. The edit page
//                                              is "live": each change is sent as soon as you apply it.
// This page is self-contained; it repeats a few small helpers from app.js.
// =========================================================================

// ---- Settings ----------------------------------------------------------
const CONFIG_PREFIX = "MontCo";
const API_BASE = "127.0.0.1:8080";
const USER_NAME_FIELD = "name";              // which /api/users/<id> field to show as a username
const CREATE_PATH = "/api/incidents/create";
const REFRESH_MS = 5000;

const TOKEN_KEY = "incidentEditor.token";
const USER_KEY = "incidentEditor.user";
// -------------------------------------------------------------------------

const cfg = {
  loaded: false,
  types: [],          // [{value, label, sub}]  incident types
  defaultType: "",    // first type tagged NEW_INCIDENT
  locations: [],      // known locations
  unitOptions: [],    // units (short names)
  unitLong: {},       // short -> long name
  assignOptions: [],  // unit assignment statuses (short names, e.g. ENR)
  statusShort: {},    // assignment status name -> short (ENROUTE -> ENR)
  secondaries: {},    // status short -> [{value, label, sub}]  (suggestions only, e.g. LEF -> hospitals)
  removeShort: "REM",
  addShort: "ADD",
  incStatuses: [],    // incident statuses
};

let token = localStorage.getItem(TOKEN_KEY) || "";
let loginName = localStorage.getItem(USER_KEY) || "";
let loginNotice = "";

let busy = false;
let incidents = [];
let loadedOnce = false;
let view = "loading";        // loading | login | list | form
let statusFilter = "ALL";
let repaintNarrative = null; // set while an incident page is open
let liveSync = null;         // set while the live edit page is open

const users = {};
const userFetching = new Set();
const userFailedAt = {};

const $ = (id) => document.getElementById(id);

// ---- Small helpers -----------------------------------------------------

function esc(v) {
  return String(v ?? "").replace(/[&<>"']/g, (c) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]
  ));
}

// el("div", {class: "x", onclick: fn}, child, "text", ...)
function el(tag, attrs = {}, ...kids) {
  const n = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (v === false || v == null) continue;
    if (k === "class") n.className = v;
    else if (k.startsWith("on")) n.addEventListener(k.slice(2), v);
    else n.setAttribute(k, v === true ? "" : v);
  }
  kids.flat().forEach((k) => {
    if (k == null || k === false) return;
    n.append(k instanceof Node ? k : document.createTextNode(String(k)));
  });
  return n;
}

function ago(unixSec) {
  const s = Math.max(0, Math.floor(Date.now() / 1000 - unixSec));
  if (s < 60) return `${s}s ago`;
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ${Math.floor((s % 3600) / 60)}m ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

function fullTime(sec) {
  return new Date(sec * 1000).toLocaleString([], {
    year: "numeric", month: "short", day: "numeric",
    hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23",
  });
}

const pad = (n) => String(n).padStart(2, "0");

// unix seconds -> "YYYY-MM-DD HH:MM:SS" (24-hour, your local time)
function fmtTs(sec) {
  const d = new Date(sec * 1000);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

// "2026-11-21 17:41:00" (or with a T / fractional seconds)  ->  "2026-11-21T17:41:00.0000000", or null if invalid
function apiTs(text) {
  const m = text.trim().match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?(?:\.\d+)?$/);
  if (!m) return null;
  const [, y, mo, d, h, mi] = m;
  const s = m[6] || "00";
  const date = new Date(+y, +mo - 1, +d, +h, +mi, +s);
  if (isNaN(date) || date.getMonth() !== +mo - 1 || date.getDate() !== +d || +h > 23 || +mi > 59 || +s > 59) return null;
  return `${y}-${mo}-${d}T${h}:${mi}:${s}.0000000`;
}

function splitLocation(loc = {}) {
  const common = (loc.commonName || "").trim();
  let address = (loc.format || "").trim();
  if (common && address.startsWith(common + ", ")) address = address.slice(common.length + 2);
  return { address, common };
}

function toast(msg, kind = "ok") {
  const t = $("toast");
  t.textContent = msg;
  t.className = "toast" + (kind === "error" ? " error" : "");
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => { t.hidden = true; }, 4500);
}

// secondary suggestions for a unit status short name (case-insensitive); any text is allowed too
const secFor = (status) => cfg.secondaries[String(status || "").toUpperCase()] || [];

// ---- API ---------------------------------------------------------------

function baseUrl() {
  return API_BASE && !/^https?:\/\//.test(API_BASE) ? "http://" + API_BASE : API_BASE;
}

function errMessage(body, status) {
  const r = body?.response;
  if (typeof r === "string" && r) return r;
  if (r?.message) return r.message;
  if (body?.message) return body.message;
  if (body?.error) return String(body.error);
  return `Request failed (HTTP ${body?.status ?? status})`;
}

// Sends a request. Normally returns the "response" part of the usual
// {"success","status","timestamp","response"} wrapper; with raw: true returns the body as is.
async function request(path, { method = "GET", json, auth = false, raw = false } = {}) {
  const headers = {};
  if (json !== undefined) headers["Content-Type"] = "application/json";
  if (auth && token) headers["Authorization"] = "Bearer " + token;

  const res = await fetch(baseUrl() + path, {
    method,
    headers,
    cache: "no-store",
    body: json !== undefined ? JSON.stringify(json) : undefined,
  });

  // User IDs are larger than JavaScript can hold exactly; quote them so they stay intact.
  let text = await res.text();
  text = text
    .replace(/("(?:contributor|id)"\s*:\s*)(\d{16,})/g, '$1"$2"')
    .replace(/("contributors"\s*:\s*)\[([^\]]*)\]/g, (m, p, inner) =>
      p + "[" + inner.replace(/\d{16,}/g, (d) => '"' + d + '"') + "]");

  let body;
  try { body = JSON.parse(text); } catch { throw new Error(`Unexpected response from the server (HTTP ${res.status})`); }

  if (res.status === 401 && auth) {
    logout("Your session expired. Please log in again.");
    throw new Error("Your session expired. Please log in again.");
  }
  if (res.status === 403 && auth) throw new Error(errMessage(body, res.status) || "You don't have permission to do that.");
  if (body.success === false || !res.ok) throw new Error(errMessage(body, res.status));
  if (raw) return body;
  return body.response !== undefined ? body.response : body;
}

function normalizeConfigs(resp) {
  return Array.isArray(resp) ? Object.assign({}, ...resp) : resp;
}

async function loadConfig() {
  const all = normalizeConfigs(await request("/api/structure/configs?file=all"));
  const get = (name) => all[`${CONFIG_PREFIX}/${name}`] || [];

  // incident types (names repeat in the file, so de-duplicate)
  const seenT = new Set();
  get("incident_types.json").forEach((t) => {
    if (seenT.has(t.name)) return;
    seenT.add(t.name);
    cfg.types.push({ value: t.name, label: t.name, sub: t.tag });
  });
  // new incidents start as the first type tagged NEW_INCIDENT
  cfg.defaultType = (get("incident_types.json").find((t) => t.tag === "NEW_INCIDENT") || {}).name || "";

  // known locations
  const seenL = new Set();
  get("locations.json").forEach((l) => {
    const f = (l.format || "").trim();
    if (!f || seenL.has(f)) return;
    seenL.add(f);
    cfg.locations.push({ value: f, label: f, sub: l.venue || "" });
  });

  // units (short names)
  const seenU = new Set();
  get("units.json").forEach((u) => {
    if (seenU.has(u.short)) return;
    seenU.add(u.short);
    cfg.unitOptions.push({ value: u.short, label: u.short, sub: u.long });
    cfg.unitLong[u.short] = u.long;
  });

  // unit assignment statuses (e.g. ENR = ENROUTE) and their suggested secondaries
  get("assignment_statuses.json").forEach((s) => {
    cfg.assignOptions.push({ value: s.short, label: s.short, sub: s.name });
    cfg.statusShort[s.name] = s.short;
    cfg.secondaries[s.short.toUpperCase()] = (s.secondaries || []).map((x) => ({ value: x.short, label: x.short, sub: x.long }));
  });
  cfg.removeShort = cfg.statusShort["REMOVED"] || "REM";
  cfg.addShort = cfg.statusShort["ADDED"] || "ADD";

  // incident statuses live at the top level (no prefix)
  cfg.incStatuses = (all["incident_status.json"] || ["PENDING", "ACTIVE", "STALE", "CLOSED"])
    .map((s) => ({ value: s, label: s }));

  const muni = all[`${CONFIG_PREFIX}/municipality.json`];
  if (muni) $("place").textContent = `${muni.name}, ${muni.state.abbreviation}`;

  cfg.loaded = true;
}

// ---- Login / logout ----------------------------------------------------

async function doLogin(username, password) {
  // the login response is just {"token": "..."} (no wrapper)
  const res = await request("/api/auth/login", { method: "POST", json: { username, password }, raw: true });
  if (!res?.token) throw new Error("Login worked, but the server didn't return a token.");
  token = res.token;
  loginName = username;
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, loginName);
}

function logout(notice = "") {
  token = "";
  loginName = "";
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  loginNotice = notice;
  showView();
}

function updateUserBox() {
  $("userbox").replaceChildren(...(token
    ? [el("span", { class: "userchip" }, "👤 ", el("b", {}, loginName || "user")),
       el("button", { class: "btn small", onclick: () => logout() }, "Log out")]
    : [el("span", { class: "gray" }, "Not logged in")]));
}

function showLogin() {
  view = "login";
  const notice = loginNotice;
  loginNotice = "";

  const user = el("input", { class: "text", type: "text", autocomplete: "username", placeholder: "Username" });
  const pass = el("input", { class: "text", type: "password", autocomplete: "current-password", placeholder: "Password" });
  const err = el("div", { class: "formerr", hidden: !notice }, notice);
  const btn = el("button", { class: "btn primary", type: "submit" }, "Log in");

  const form = el("form", {
    class: "group login",
    onsubmit: async (e) => {
      e.preventDefault();
      err.hidden = true;
      btn.disabled = true;
      try {
        await doLogin(user.value.trim(), pass.value);
        pass.value = "";
        showView();
      } catch (ex) {
        err.textContent = ex.message;
        err.hidden = false;
      } finally {
        btn.disabled = false;
      }
    },
  },
  el("div", { class: "big orange" }, "Editor Login"),
  el("p", { class: "gray" }, "Log in to create or edit incidents."),
  err,
  el("div", { class: "frow" }, el("label", {}, "Username"), el("div", { class: "ctl" }, user)),
  el("div", { class: "frow" }, el("label", {}, "Password"), el("div", { class: "ctl" }, pass)),
  el("div", { style: "margin-top:12px" }, btn));

  $("view").replaceChildren(form);
  user.focus();
}

// ---- Users (usernames shown in the narrative list) ---------------------

function userName(id) {
  return users[id] ?? String(id ?? "—");
}

function ensureUsers(list) {
  const ids = new Set();
  list.forEach((i) => (i.log || []).forEach((l) => { if (l.contributor != null) ids.add(String(l.contributor)); }));
  ids.forEach((id) => {
    if (id in users || userFetching.has(id)) return;
    if (userFailedAt[id] && Date.now() - userFailedAt[id] < 60000) return;
    userFetching.add(id);
    request(`/api/users/${id}`)
      .then((u) => { users[id] = u?.[USER_NAME_FIELD] ?? u?.name ?? id; })
      .catch(() => { userFailedAt[id] = Date.now(); })
      .finally(() => { userFetching.delete(id); if (repaintNarrative) repaintNarrative(); });
  });
}

// ---- Status colors (same as the viewer) --------------------------------

const TEXT_COLORS = {
  GRAY: "#9aa4b2", RED: "#ff5a4f", GREEN: "#3ddc84", YELLOW: "#f5c542",
  BLUE: "#4aa3ff", PINK: "#f06bb0", CYAN: "#36c5d0", WHITE: "#ffffff",
  ORANGE: "#ff8a1f", DEFAULT: null,
};
const BG_COLORS = {
  DEFAULT: null, FIREFLY_DARK_BLUE: "#0b3a4a", ORANGE: "#a8461a", MARBLE_BLUE: "#4b616a",
  GREYISH_TURQUOISE: "#5a6f73", GRAY: "#6b7780", INDIGO: "#5c61b0", LIGHT_GRAY: "#8a9696",
  WHITE: "#e8e8e8",
};

function cssColor(name, map) {
  if (!name) return null;
  const key = String(name).toUpperCase().replace(/[^A-Z0-9]+/g, "_");
  if (key in map) return map[key];
  const raw = String(name).trim();
  return window.CSS?.supports?.("color", raw) ? raw : null;
}

function styleAttr(style) {
  const c = cssColor(style?.color, TEXT_COLORS);
  const b = cssColor(style?.background, BG_COLORS);
  return esc((c ? `--rc:${c};` : "") + (b ? `--rb:${b};` : ""));
}

function latestAssignment(u) {
  const list = u.assignments || [];
  return list.length ? list.reduce((a, b) => (b.timestamp >= a.timestamp ? b : a)) : null;
}
const isRemoved = (u) => latestAssignment(u)?.status === "REMOVED";
const unitShort = (u) => u.short || String(u.unit ?? "");

function logType(l) { return l.type === "CREATE" ? "UPDATE" : l.type; }
function entryText(l) { return String(l.text ?? "").replace(/^<\d{2}:\d{2}>\s*/, ""); }

// Fixed-width table: cols = [[label, widthPercent], ...]
function gridTable(cols, rowsHtml, { minWidth = 800, empty = "Nothing to show." } = {}) {
  return `
    <div class="table-wrap">
      <table class="grid" style="min-width:${minWidth}px">
        <colgroup>${cols.map((c) => `<col style="width:${c[1]}%">`).join("")}</colgroup>
        <thead><tr>${cols.map((c) => `<th>${esc(c[0])}</th>`).join("")}</tr></thead>
        <tbody>${rowsHtml || `<tr class="empty-row"><td colspan="${cols.length}">${esc(empty)}</td></tr>`}</tbody>
      </table>
    </div>`;
}

const ENTRY_COLS = [["Date/Time", 12], ["Type", 9], ["User", 12], ["Entry Text", 67]];

function entryRows(list) {
  return [...list].sort((a, b) => b.time - a.time).map((l) => `
    <tr>
      <td class="meta mono">${esc(fullTime(l.time))}</td>
      <td class="meta">${esc(logType(l))}</td>
      <td class="meta">${esc(userName(l.contributor))}</td>
      <td>${esc(entryText(l))}</td>
    </tr>`).join("");
}

// ---- Autocomplete dropdown ---------------------------------------------
// Type to filter, arrow keys to move, Enter (or click) to pick, Esc to cancel.
//   options     an array, or a function returning an array (for lists that change)
//   allowCustom free text is accepted (locations, new units, secondaries)
//   onChange    called on every edit
//   onCommit    called when a value is picked, or Enter is pressed on a valid value
//   onCancel    called on Esc (use it to put the old value back)

function makeCombo({ options, value = "", placeholder = "", allowCustom = false, compact = false, onChange = () => {}, onCommit = () => {}, onCancel = () => {} }) {
  const opts = () => (typeof options === "function" ? options() : options);
  const wrap = el("div", { class: "combo" + (compact ? " compact" : "") });
  const input = el("input", { type: "text", placeholder, autocomplete: "off", spellcheck: "false" });
  const list = el("ul", { class: "combo-list", hidden: true });
  wrap.append(input, list);
  input.value = value;

  let items = [];
  let active = -1;

  const valid = () => {
    const v = input.value.trim();
    return !v || allowCustom || opts().some((o) => o.value === v);
  };
  const mark = () => wrap.classList.toggle("invalid", !valid());

  function filter(q) {
    q = q.trim().toLowerCase();
    const all = opts();
    if (!q) return all.slice(0, 100);
    const starts = [];
    const contains = [];
    for (const o of all) {
      const v = o.value.toLowerCase();
      const l = (o.label || "").toLowerCase();
      const s = (o.sub || "").toLowerCase();
      if (v.startsWith(q) || l.startsWith(q)) starts.push(o);
      else if (v.includes(q) || l.includes(q) || s.includes(q)) contains.push(o);
    }
    return [...starts, ...contains].slice(0, 100);
  }

  function paint() {
    list.replaceChildren();
    if (!items.length) {
      if (allowCustom && input.value.trim()) list.append(el("li", { class: "empty" }, "Press Enter to use this text"));
      else if (!allowCustom) list.append(el("li", { class: "empty" }, "No matches"));
      else list.hidden = true;
      return;
    }
    items.forEach((o, i) => {
      const li = el("li", { class: i === active ? "on" : "" }, el("span", { class: "v" }, o.label ?? o.value), o.sub ? el("span", { class: "s" }, o.sub) : null);
      li.addEventListener("mousedown", (e) => { e.preventDefault(); choose(o); });
      list.append(li);
    });
    list.children[active]?.scrollIntoView({ block: "nearest" });
  }

  function open() {
    items = filter(input.value);
    active = allowCustom ? -1 : (items.length ? 0 : -1);
    list.hidden = false;
    paint();
  }
  function close() { list.hidden = true; }
  function choose(o) {
    input.value = o.value;
    close();
    mark();
    onChange(input.value.trim());
    onCommit(input.value.trim());
  }

  input.addEventListener("focus", open);
  input.addEventListener("input", () => { open(); mark(); onChange(input.value.trim()); });
  input.addEventListener("blur", () => {
    close();
    const v = input.value.trim();
    const m = opts().find((o) => o.value.toLowerCase() === v.toLowerCase());
    if (m) input.value = m.value;      // fix capitalisation of an exact match
    mark();
    onChange(input.value.trim());
  });
  input.addEventListener("keydown", (e) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      if (list.hidden) open();
      else { active = Math.min(active + 1, items.length - 1); paint(); }
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      active = Math.max(active - 1, 0);
      paint();
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (!list.hidden && items[active]) choose(items[active]);
      else { close(); if (valid()) onCommit(input.value.trim()); }
    } else if (e.key === "Escape") {
      close();
      onCancel();
    }
  });

  mark();
  return {
    el: wrap,
    input,
    get: () => input.value.trim(),
    set: (v) => { input.value = v; mark(); },
    valid,
    focus: () => input.focus(),
  };
}

// A unit's status box + secondary box. The secondary takes any text; for some statuses
// (e.g. LEF/ARR) the config's list is offered as suggestions.
function statusPair({ status = "", secondary = "", statusPlaceholder = "Status", onChange = () => {}, onStatusCommit = () => {}, onSecCommit = () => {}, onCancel = () => {} }) {
  const st = makeCombo({ options: cfg.assignOptions, value: status, placeholder: statusPlaceholder, compact: true, onChange, onCommit: onStatusCommit, onCancel });
  const sec = makeCombo({ options: () => secFor(st.get()), value: secondary, allowCustom: true, placeholder: "Secondary (optional)", compact: true, onChange, onCommit: onSecCommit, onCancel });
  return { st, sec };
}

// "101", "101:ENR" or "101:LEF>LGHM"
const unitItem = (short, status, sec) => short + (status ? `:${status}${sec ? ">" + sec : ""}` : "");

// Turns a stored secondary (short or long name) into the short name used in requests.
function resolveSecondary(statusShort, val) {
  if (!val) return "";
  const v = String(val);
  const m = secFor(statusShort).find((o) => o.value.toLowerCase() === v.toLowerCase() || (o.sub || "").toLowerCase() === v.toLowerCase());
  return m ? m.value : v;
}

// ---- Views -------------------------------------------------------------

function showView() {
  updateUserBox();
  repaintNarrative = null;
  liveSync = null;
  if (!token) { showLogin(); return; }
  const r = route();
  if (r.name === "list") renderList();
  else openForm(r);
}

function route() {
  const h = location.hash.replace(/^#/, "");
  const dec = (s) => { try { return decodeURIComponent(s); } catch { return s; } };
  const m = h.match(/^edit=(.*)$/);
  if (m) return { name: "edit", id: dec(m[1]) };
  if (h === "new") return { name: "new" };
  return { name: "list" };
}

// -- List (mirrors the viewer's Incidents page, plus an Edit button) --

const STATUSES = ["PENDING", "ACTIVE", "STALE", "CLOSED"];
const CATEGORY_ICON = { FIRE: "🔥", EMS: "🚑", POLICE: "🚓", OTHER: "📋" };
const LIST_COLS = [["Status", 10], ["Type", 20], ["Location", 22], ["Common Name", 17], ["Units", 21], ["", 10]];

function category(i) {
  const t = i.type?.tag;
  if (t === "FIRE") return "FIRE";
  if (t === "EMS") return "EMS";
  if (t === "POLICE" || t === "POLICE_WITH_WEAPON") return "POLICE";
  return "OTHER";
}

const statusPill = (s) => `<span class="pill ${esc(s)}">${esc(s)}</span>`;

function statTiles() {
  const counts = { ALL: incidents.length };
  STATUSES.forEach((s) => { counts[s] = 0; });
  incidents.forEach((i) => { if (i.status in counts) counts[i.status]++; });
  const unitsOut = incidents.reduce((n, i) => n + (i.units || []).filter((u) => !isRemoved(u)).length, 0);
  const tile = (key, label, n) => `
    <button class="tile ${key} ${statusFilter === key ? "on" : ""}" data-status="${key}">
      <span class="num">${n}</span><span class="lbl">${label}</span>
    </button>`;
  return `
    <div class="stats">
      ${tile("ALL", "Total", counts.ALL)}
      ${STATUSES.filter((s) => s !== "CLOSED" || counts.CLOSED).map((s) => tile(s, s, counts[s])).join("")}
      <div class="tile UNITS"><span class="num">${unitsOut}</span><span class="lbl">Units out</span></div>
    </div>`;
}

function renderList() {
  view = "list";
  const sorted = [...incidents]
    .filter((i) => statusFilter === "ALL" || i.status === statusFilter)
    .sort((a, b) => b.time - a.time);

  const rows = sorted.map((i) => {
    const { address, common } = splitLocation(i.location);
    const cat = category(i);
    const chips = (i.units || []).filter((u) => !isRemoved(u)).map((u) => {
      const a = latestAssignment(u);
      return `<span class="chip" style="${styleAttr(a?.style)}" title="${a ? esc(a.status) : ""}"><b>${esc(unitShort(u))}</b>${a ? `<span class="st">${esc(cfg.statusShort[a.status] || a.status)}</span>` : ""}</span>`;
    }).join("");

    return `
      <tr class="click cat-${cat} ${i.status === "PENDING" ? "pending" : ""}" data-edit="${esc(i.id)}" tabindex="0">
        <td>${statusPill(i.status)}</td>
        <td>
          <div class="typecell">
            <span class="icon">${CATEGORY_ICON[cat]}</span>
            <div><div class="tname">${esc(i.type?.name)}</div><div class="sub">${esc(ago(i.time))} · #${esc(i.id)}</div></div>
          </div>
        </td>
        <td>${address ? esc(address) : '<span class="sub">—</span>'}</td>
        <td>${esc(common)}</td>
        <td><div class="chips">${chips}</div></td>
        <td><button class="btn small" data-edit="${esc(i.id)}">✎ Edit</button></td>
      </tr>`;
  }).join("");

  const empty = !loadedOnce ? "Loading…" : incidents.length ? "No incidents with this status." : "No active incidents.";

  $("view").innerHTML = `
    <div class="toolbar">
      <span class="gray">Pick an incident to edit it, or create a new one.</span>
      <button class="btn primary" data-new style="margin-left:auto">＋ New Incident</button>
    </div>
    ${statTiles()}
    ${gridTable(LIST_COLS, rows, { minWidth: 900, empty })}`;
}

$("view").addEventListener("click", (e) => {
  if (view !== "list") return;
  const st = e.target.closest("[data-status]");
  if (st) {
    statusFilter = statusFilter === st.dataset.status ? "ALL" : st.dataset.status;
    renderList();
    return;
  }
  if (e.target.closest("[data-new]")) { location.hash = "new"; return; }
  const ed = e.target.closest("[data-edit]");
  if (ed) location.hash = `edit=${ed.dataset.edit}`;
});

$("view").addEventListener("keydown", (e) => {
  if (view !== "list" || (e.key !== "Enter" && e.key !== " ")) return;
  const ed = e.target.closest("tr[data-edit]");
  if (ed) { e.preventDefault(); location.hash = `edit=${ed.dataset.edit}`; }
});

// -- Shared form pieces --

function openForm(r) {
  if (!cfg.loaded || !loadedOnce) {
    view = "loading";
    $("view").replaceChildren(el("div", { class: "group gray" }, "Loading…"));
    return;
  }
  if (r.name === "new") { buildCreateForm(); return; }

  const inc = incidents.find((i) => String(i.id) === r.id);
  if (!inc) {
    view = "form";
    $("view").replaceChildren(el("div", { class: "page" }, el("div", { class: "group notfound" },
      el("div", { class: "big" }, "Incident Not Found"),
      el("p", {}, `Incident ${r.id} is not in the active incident list. It may have been closed, or the link may be wrong.`),
      el("button", { class: "btn", onclick: () => { location.hash = "list"; } }, "‹ Back to Incidents"))));
    return;
  }
  buildEditPage(inc);
}

function frow(label, control, hint) {
  return el("div", { class: "frow" }, el("label", {}, label), el("div", { class: "ctl" }, control, hint ? el("div", { class: "hint" }, hint) : null));
}

function group(title, ...kids) {
  return el("div", { class: "group" }, title ? el("div", { class: "sub-label", style: "margin:0 0 8px" }, title) : null, ...kids);
}

// -- CREATE: one form, sent with a single button --

function buildCreateForm() {
  view = "form";

  const typeCombo = makeCombo({ options: cfg.types, value: cfg.defaultType, placeholder: "Search incident types…", onChange: () => update() });
  const locCombo = makeCombo({ options: cfg.locations, allowCustom: true, placeholder: "Search known locations, or type a custom one…", onChange: () => update() });
  const tsInput = el("input", { class: "text", type: "text", placeholder: "YYYY-MM-DD HH:MM:SS", oninput: () => update() });
  const tsNow = el("button", { class: "btn small", type: "button", onclick: () => { tsInput.value = fmtTs(Math.floor(Date.now() / 1000)); update(); } }, "Now");
  const narrative = el("input", { class: "text", type: "text", placeholder: "Initial narrative (optional)", oninput: () => update() });

  // validation messages float over the page, so they never push anything around
  const errBox = el("div", { class: "formerr floating", hidden: true });
  const summary = el("div", { class: "grow" });
  const preview = el("pre", {});
  const saveBtn = el("button", { class: "btn primary", type: "button" }, "Create Incident");
  const resetBtn = el("button", { class: "btn", type: "button", onclick: () => buildCreateForm() }, "Reset");

  // units
  const rows = [];
  const unitsBox = el("div", {});
  const addUnit = makeCombo({ options: cfg.unitOptions, allowCustom: true, placeholder: "Add a unit…", compact: true, onCommit: () => addPair.st.focus() });
  const addPair = statusPair({ onStatusCommit: () => addPair.sec.focus(), onSecCommit: () => addRow() });

  function paintUnits() {
    unitsBox.replaceChildren();
    rows.forEach((row) => {
      unitsBox.append(el("div", { class: "unit-edit-row" },
        el("div", { class: "uname" }, el("b", {}, row.short), el("span", { class: "gray" }, row.long)),
        row.pair.st.el,
        row.pair.sec.el,
        el("button", { class: "btn small", type: "button", onclick: () => { rows.splice(rows.indexOf(row), 1); paintUnits(); update(); } }, "Remove")));
    });
  }

  function addRow() {
    const typed = addUnit.get();
    if (!typed) return;
    const known = cfg.unitOptions.find((u) => u.value.toLowerCase() === typed.toLowerCase());
    const short = known ? known.value : typed;
    if (rows.some((r) => r.short.toLowerCase() === short.toLowerCase())) {
      toast(`${short} is already in the list.`, "error");
      return;
    }
    if (!addPair.st.valid()) { toast("That unit status isn't in the list.", "error"); return; }
    const pair = statusPair({ status: addPair.st.get(), secondary: addPair.sec.get(), onChange: () => update() });
    rows.push({ short, long: cfg.unitLong[short] || "", pair });
    addUnit.set("");
    addPair.st.set("");
    addPair.sec.set("");
    paintUnits();
    update();
    addUnit.focus();
  }

  function compute() {
    const p = {};
    const errors = [];

    const t = typeCombo.get();
    if (!typeCombo.valid()) errors.push("Type must be chosen from the list.");
    else if (!t) errors.push("Pick an incident type.");
    else p.type = t;                                   // the only required field

    const loc = locCombo.get();
    if (loc) p.location = loc;

    if (rows.length) {
      p.units = [];
      for (const r of rows) {
        if (!r.pair.st.valid()) { errors.push(`Unknown status for ${r.short}.`); continue; }
        p.units.push(unitItem(r.short, r.pair.st.get(), r.pair.sec.get()));
      }
    }

    const ts = tsInput.value.trim();
    if (ts) {
      const s = apiTs(ts);
      if (!s) errors.push("Timestamp must look like 2026-11-21 17:41:00 (24-hour).");
      else p.timestamp = s;
    }

    const n = narrative.value.trim();
    if (n) p.initialNarrative = n;

    return { p, errors };
  }

  function update() {
    const { p, errors } = compute();
    errBox.hidden = !errors.length;
    errBox.textContent = errors.join(" ");
    saveBtn.disabled = errors.length > 0 || !p.type;
    summary.textContent = `Will send: ${Object.keys(p).join(", ") || "nothing yet"}`;
    preview.textContent = `POST ${CREATE_PATH}\n${JSON.stringify(p, null, 2)}`;
  }

  saveBtn.addEventListener("click", async () => {
    const { p, errors } = compute();
    if (errors.length || !p.type) return;
    saveBtn.disabled = true;
    resetBtn.disabled = true;
    try {
      const res = await request(CREATE_PATH, { method: "POST", json: p, auth: true });
      toast("Incident created.");
      incidents = await request("/api/incidents");
      const nid = res && typeof res === "object" ? (res.id ?? res.incident?.id ?? res.incidentId) : null;
      location.hash = nid ? `edit=${nid}` : "list";
    } catch (ex) {
      toast(ex.message, "error");
      saveBtn.disabled = false;
    } finally {
      resetBtn.disabled = false;
    }
  });

  const page = el("div", { class: "page" },
    el("div", { class: "subhead" },
      el("button", { class: "back", onclick: () => { location.hash = "list"; } }, "‹ Incidents"),
      el("span", { class: "gray" }, "New incident")),
    el("div", { class: "page-title" }, el("div", { class: "big orange" }, "Create Incident")),

    group("Incident",
      frow("Type", typeCombo.el, "Required. Starts as the new-incident type; type to search."),
      frow("Location", locCombo.el, "Pick a known location, or type a custom one."),
      frow("Timestamp", el("div", { class: "inline" }, tsInput, tsNow), "Leave blank for the current time. 24-hour, your local time."),
      frow("Initial Narrative", narrative)),

    group("Units",
      unitsBox,
      el("div", { class: "add-unit-row" }, addUnit.el, addPair.st.el, addPair.sec.el, el("button", { class: "btn small", type: "button", onclick: addRow }, "Add")),
      el("div", { class: "hint" }, "Sent as e.g. 101, 101:ENR or 101:LEF>LGHM. The status and secondary are optional, and the secondary can be any text.")),

    el("details", { class: "preview" }, el("summary", {}, "Request preview"), preview),
    errBox,
    el("div", { class: "savebar" }, summary, resetBtn, saveBtn));

  $("view").replaceChildren(page);
  paintUnits();
  update();
  typeCombo.focus();
}

// -- EDIT: live. Every change is sent the moment you apply it. --

function buildEditPage(inc) {
  view = "form";
  const id = inc.id;
  const path = `/api/incidents/${id}`;
  let cur = inc;                    // latest copy from the server

  const snapshot = () => ({
    type: cur.type?.name ?? "",
    status: cur.status ?? "",
    location: (cur.location?.format || "").trim(),
    ts: cur.time != null ? fmtTs(cur.time) : "",
  });
  let base = snapshot();            // what the server had the last time we synced

  // Send one change, then reload so the page shows what the server now has.
  async function send(body, okMsg = "Saved") {
    try {
      await request(path, { method: "POST", json: body, auth: true });
      toast(okMsg);
    } catch (ex) {
      toast(ex.message, "error");
      await pull().catch(() => {});
      return false;
    }
    await pull().catch(() => {});
    return true;
  }
  async function pull() {
    incidents = await request("/api/incidents");
    apply();
  }

  // ----- controls -----
  const flag = (combo, baseVal) => combo.el.classList.toggle("dirty", combo.get() !== baseVal);
  const flagAll = () => {
    flag(typeCombo, base.type);
    flag(statusCombo, base.status);
    flag(locCombo, base.location);
    tsInput.classList.toggle("dirty", tsInput.value.trim() !== base.ts);
  };

  const typeCombo = makeCombo({
    options: cfg.types, value: base.type, placeholder: "Search incident types…",
    onChange: flagAll,
    onCommit: (v) => { if (v && v !== base.type) send({ type: v }, "Type updated"); },
    onCancel: () => { typeCombo.set(base.type); flagAll(); },
  });
  const statusCombo = makeCombo({
    options: cfg.incStatuses, value: base.status,
    onChange: flagAll,
    onCommit: (v) => { if (v && v !== base.status) send({ status: v }, "Status updated"); },
    onCancel: () => { statusCombo.set(base.status); flagAll(); },
  });
  const locCombo = makeCombo({
    options: cfg.locations, value: base.location, allowCustom: true, placeholder: "Search known locations, or type a custom one…",
    onChange: flagAll,
    onCommit: (v) => { if (v && v !== base.location) send({ location: v }, "Location updated"); },
    onCancel: () => { locCombo.set(base.location); flagAll(); },
  });

  const commitTs = () => {
    const t = tsInput.value.trim();
    if (!t || t === base.ts) return;
    const s = apiTs(t);
    if (!s) { toast("Timestamp must look like 2026-11-21 17:41:00 (24-hour).", "error"); return; }
    send({ timestamp: s }, "Timestamp updated");
  };
  const tsInput = el("input", {
    class: "text", type: "text", value: base.ts, placeholder: "YYYY-MM-DD HH:MM:SS",
    oninput: flagAll,
    onkeydown: (e) => {
      if (e.key === "Enter") { e.preventDefault(); commitTs(); }
      else if (e.key === "Escape") { tsInput.value = base.ts; flagAll(); }
    },
  });
  const tsNow = el("button", { class: "btn small", type: "button", onclick: () => { tsInput.value = fmtTs(Math.floor(Date.now() / 1000)); commitTs(); } }, "Now");

  const pub = el("input", { type: "checkbox", onchange: () => send({ published: pub.checked }, pub.checked ? "Published" : "Unpublished") });
  pub.checked = cur.publishStatus === "PUBLISHED";

  // ----- units -----
  const unitsBox = el("div", {});
  let unitSig = "";
  const sigOf = (c) => JSON.stringify((c.units || []).map((u) => {
    const a = latestAssignment(u);
    return [unitShort(u), a?.status, a?.secondary];
  }));

  function paintUnits() {
    unitSig = sigOf(cur);
    unitsBox.replaceChildren();
    const list = cur.units || [];
    if (!list.length) unitsBox.append(el("div", { class: "gray", style: "padding:4px 0" }, "No units on this incident."));
    list.forEach((u) => {
      const a = latestAssignment(u);
      const short = unitShort(u);
      const origStatus = a ? (cfg.statusShort[a.status] || a.status) : "";
      const origSec = resolveSecondary(origStatus, a?.secondary);
      const removed = origStatus === cfg.removeShort;

      const mark = () => {
        pair.st.el.classList.toggle("dirty", pair.st.get() !== origStatus);
        pair.sec.el.classList.toggle("dirty", pair.sec.get() !== origSec);
      };
      const commit = () => {
        const s = pair.st.get();
        const c = pair.sec.get();
        if (!s) return;
        if (!pair.st.valid()) { toast("That unit status isn't in the list.", "error"); return; }
        if (s === origStatus && c === origSec) return;
        send({ units: [unitItem(short, s, c)] }, `${short} updated`);
      };
      const pair = statusPair({
        status: origStatus, secondary: origSec, onChange: mark, onStatusCommit: commit, onSecCommit: commit,
        onCancel: () => { pair.st.set(origStatus); pair.sec.set(origSec); mark(); },
      });

      const btn = removed
        ? el("button", { class: "btn small", type: "button", onclick: () => send({ units: [`${short}:${cfg.addShort}`] }, `${short} re-added`) }, "Re-add")
        : el("button", { class: "btn small", type: "button", onclick: () => send({ units: [`${short}:${cfg.removeShort}`] }, `${short} removed`) }, "Remove");

      unitsBox.append(el("div", { class: "unit-edit-row" + (removed ? " removed" : "") },
        el("div", { class: "uname" }, el("b", {}, short), el("span", { class: "gray" }, cfg.unitLong[short] || "")),
        pair.st.el,
        pair.sec.el,
        btn));
    });
  }

  const addUnit = makeCombo({ options: cfg.unitOptions, allowCustom: true, placeholder: "Add a unit…", compact: true, onCommit: () => addPair.st.focus() });
  const addPair = statusPair({ onStatusCommit: () => addPair.sec.focus(), onSecCommit: () => addNow() });
  async function addNow() {
    const typed = addUnit.get();
    if (!typed) return;
    if (!addPair.st.valid()) { toast("That unit status isn't in the list.", "error"); return; }
    const known = cfg.unitOptions.find((u) => u.value.toLowerCase() === typed.toLowerCase());
    const short = known ? known.value : typed;
    if ((cur.units || []).some((u) => unitShort(u).toLowerCase() === short.toLowerCase() && !isRemoved(u))) {
      toast(`${short} is already on this incident — change its status in the list above.`, "error");
      return;
    }
    if (await send({ units: [unitItem(short, addPair.st.get(), addPair.sec.get())] }, `${short} added`)) {
      addUnit.set("");
      addPair.st.set("");
      addPair.sec.set("");
      addUnit.focus();
    }
  }

  // ----- narrative: type, press Enter, it is sent right away -----
  const narrInput = el("input", {
    class: "text", type: "text", placeholder: "Add a narrative entry — press Enter to send",
    onkeydown: async (e) => {
      if (e.key !== "Enter") return;
      e.preventDefault();
      const text = narrInput.value.trim();
      if (!text) return;
      narrInput.disabled = true;
      const ok = await send({ narrative: text }, "Narrative added");
      narrInput.disabled = false;
      if (ok) narrInput.value = "";
      narrInput.focus();
    },
  });
  const narrTable = el("div", {});
  repaintNarrative = () => {
    const list = (cur.log || []).filter((l) => l.type === "NARRATIVE");
    narrTable.innerHTML = gridTable(ENTRY_COLS, entryRows(list), { minWidth: 720, empty: "No narrative entries yet." });
  };

  // ----- keep the page in step with the server -----
  const title = el("div", { class: "big orange" });
  function apply() {
    const fresh = incidents.find((i) => String(i.id) === String(id));
    if (!fresh) { showView(); return; }          // closed or removed: shows "Incident Not Found"
    cur = fresh;

    const nb = snapshot();
    // a box is only updated if you haven't typed something different into it
    if (typeCombo.get() === base.type) typeCombo.set(nb.type);
    if (statusCombo.get() === base.status) statusCombo.set(nb.status);
    if (locCombo.get() === base.location) locCombo.set(nb.location);
    if (tsInput.value.trim() === base.ts) tsInput.value = nb.ts;
    base = nb;
    pub.checked = cur.publishStatus === "PUBLISHED";
    title.textContent = `Edit ${nb.type || "Incident"}`;
    flagAll();

    if (sigOf(cur) !== unitSig && !unitsBox.contains(document.activeElement)) paintUnits();
    repaintNarrative();
  }
  liveSync = apply;

  const page = el("div", { class: "page" },
    el("div", { class: "subhead" },
      el("button", { class: "back", onclick: () => { location.hash = "list"; } }, "‹ Incidents"),
      el("span", { class: "gray" }, `Incident #${id}`),
      el("a", { class: "link", href: `index.html#incident=${id}`, target: "_blank", rel: "noopener" }, "View in viewer ↗")),
    el("div", { class: "page-title" }, title),
    el("div", { class: "live-note" }, "Live edit — changes are sent the moment you pick a value or press Enter. An orange outline means it's typed but not applied yet (Esc undoes it)."),

    group("Details",
      frow("Type", typeCombo.el),
      frow("Status", statusCombo.el),
      frow("Published", el("label", { class: "check" }, pub, "Published")),
      frow("Location", locCombo.el),
      frow("Timestamp", el("div", { class: "inline" }, tsInput, tsNow))),

    group("Units",
      unitsBox,
      el("div", { class: "add-unit-row" }, addUnit.el, addPair.st.el, addPair.sec.el, el("button", { class: "btn small", type: "button", onclick: addNow }, "Add")),
      el("div", { class: "hint" }, "Change a unit's status or secondary and press Enter. The secondary can be any text. Remove sends REM.")),

    el("div", { class: "group flush" },
      el("h3", { class: "group-title" }, "Narrative"),
      el("div", { class: "narr-add" }, narrInput),
      narrTable));

  $("view").replaceChildren(page);
  title.textContent = `Edit ${base.type || "Incident"}`;
  paintUnits();
  repaintNarrative();
  flagAll();
}

// ---- Refresh loop ------------------------------------------------------

async function refresh() {
  if (busy) return;
  busy = true;
  try {
    if (!cfg.loaded) await loadConfig();
    incidents = await request("/api/incidents");
    loadedOnce = true;
    ensureUsers(incidents);

    if (view === "list") renderList();
    else if (view === "loading") showView();   // data just arrived for a page that was waiting
    else if (view === "form" && liveSync) liveSync();

    $("error").hidden = true;
    $("live").className = "live ok";
    $("updated").textContent = `Updated ${new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23" })}`;
  } catch (err) {
    $("error").hidden = false;
    $("error").textContent = `Could not refresh: ${err.message}. Retrying…`;
    $("live").className = "live bad";
  } finally {
    busy = false;
  }
}

window.addEventListener("hashchange", () => { window.scrollTo(0, 0); showView(); });

showView();
refresh();
setInterval(refresh, REFRESH_MS);
