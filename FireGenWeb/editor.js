// =========================================================================
// Incident editor (editor.html)
//   - log in:   POST  /api/auth/login               {"username","password"}  ->  {"token"}   (no wrapper)
//   - create:   POST  /api/incidents/create         Bearer token; only "type" is required
//   - edit:     PATCH /api/incidents/{id}           type / units / timestamp / narrative / published
//   - location: PATCH /api/incidents/{id}/location  {"data","type","commonName","venue"}
//   - close:    PATCH /api/incidents/{id}/status
// The edit page is "live": each change is sent as soon as you apply it.
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
  locations: [],      // known locations: {value (the formatted text), label, sub, loc (the location object)}
  locTypes: [],       // location types (config: location_types.json)
  venues: [],         // venue names (config: venues.json)
  venueDisplay: {},   // venue name -> "Blacksburg, VA"
  unitOptions: [],    // units (short names)
  unitLong: {},       // short -> long name
  assignOptions: [],  // unit assignment statuses (short names, e.g. ENR)
  statusShort: {},    // assignment status name -> short (ENROUTE -> ENR)
  secondaries: {},    // status short -> [{value, label, sub}]  (suggestions only, e.g. LEF -> hospitals)
  removeShort: "REM",
  addShort: "ADD",
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
let autoReopen = null;       // incident id whose "Re-open" box should open as soon as its page loads

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

  // location types (ADDRESS, INTERSECTION, ...). Not under the municipality folder; look in both places.
  let locTypes = all["location_types.json"] || all[`${CONFIG_PREFIX}/location_types.json`];
  if (!locTypes) {
    try { locTypes = await request("/api/structure/configs?file=location_types.json"); } catch { /* handled below */ }
  }
  cfg.locTypes = Array.isArray(locTypes) ? locTypes : [];

  // venues
  get("venues.json").forEach((v) => {
    cfg.venues.push(v.name);
    cfg.venueDisplay[v.name] = v.display || "";
  });

  // known locations (used to quick-fill the location fields)
  const seenL = new Set();
  get("locations.json").forEach((l) => {
    const f = (l.format || "").trim();
    if (!f || seenL.has(f)) return;
    seenL.add(f);
    cfg.locations.push({ value: f, label: f, sub: l.venue || "", loc: l });
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
//   allowCustom free text is accepted (new units, secondaries)
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

  const isExact = () => {
    const v = input.value.trim();
    return !!v && opts().some((o) => o.value === v);
  };
  let selectPending = false;

  // showAll: when the box already holds one of the options, list every option (handy for switching)
  function open(showAll = false) {
    if (showAll && isExact()) {
      items = opts().slice(0, 300);
      active = items.findIndex((o) => o.value === input.value.trim());
    } else {
      items = filter(input.value);
      active = allowCustom ? -1 : (items.length ? 0 : -1);
    }
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

  input.addEventListener("focus", () => {
    open(true);
    if (isExact()) { input.select(); selectPending = true; }   // so typing replaces the current value
  });
  input.addEventListener("mouseup", (e) => { if (selectPending) { e.preventDefault(); selectPending = false; } });
  input.addEventListener("input", () => { open(); mark(); onChange(input.value.trim()); });
  input.addEventListener("blur", () => {
    selectPending = false;
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
      if (list.hidden) open(true);
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

// ---- Location editor ---------------------------------------------------
// A location is a type (ADDRESS, INTERSECTION, ...) plus a list of strings that the type
// interprets (e.g. ADDRESS: ["630", "Washington St SW"]), and optionally a common name and venue.
// The fields shown for each type come from the location_types.json config.

const SPECIAL_FIELDS = ["venue", "common"];   // sent as "venue" / "commonName", not in the data list
const locTypeById = (id) => cfg.locTypes.find((t) => t.id === id) || null;
const locTypeByTitle = (title) => cfg.locTypes.find((t) => t.title.toLowerCase() === String(title || "").trim().toLowerCase()) || null;
const dataFieldsOf = (t) => (t?.fields || []).filter((f) => !SPECIAL_FIELDS.includes(f.id)).sort((a, b) => a.autofillIndex - b.autofillIndex);

// A stable text form of a location, to tell whether it changed.
function locSigOf(loc) {
  const data = [...(loc?.data || [])].map((s) => String(s).trim());
  while (data.length && data[data.length - 1] === "") data.pop();
  return JSON.stringify({
    type: loc?.type ?? null,
    data,
    commonName: (loc?.commonName || "").trim() || null,
    venue: (loc?.venue || "").trim() || null,
  });
}

function frow(label, control, hint) {
  return el("div", { class: "frow" }, el("label", {}, label), el("div", { class: "ctl" }, control, hint ? el("div", { class: "hint" }, hint) : null));
}

function group(title, ...kids) {
  return el("div", { class: "group" }, title ? el("div", { class: "sub-label", style: "margin:0 0 8px" }, title) : null, ...kids);
}

function makeLocationEditor({ initial = null, showQuickFill = true, onChange = () => {}, onEnter = () => {} }) {
  let type = locTypeById(initial?.type) || null;     // blank until a type is chosen
  const vals = {};                       // field id -> text
  let venueCombo = null;
  const fieldsBox = el("div", {});
  const previewEl = el("div", { class: "hint loc-preview" });

  const typeCombo = makeCombo({
    options: cfg.locTypes.map((t) => ({ value: t.title, label: t.title, sub: t.shortName })),
    placeholder: "Select a location type…",
    onChange: (v) => {
      const t = locTypeByTitle(v);
      if (t && t !== type) switchType(t);
      else if (!v.trim() && type) switchType(null);    // cleared: hide the fields
      onChange();
    },
  });

  const known = makeCombo({
    options: cfg.locations,
    placeholder: "Search known locations to fill in the fields below…",
    onCommit: (v) => {
      const o = cfg.locations.find((x) => x.value === v);
      if (!o) return;
      load(o.loc);
      known.set("");
      onChange();
    },
  });

  function read() {
    const data = dataFieldsOf(type).map((f) => (vals[f.id] || "").trim());
    while (data.length && data[data.length - 1] === "") data.pop();
    const hasCommon = !!type?.fields.some((f) => f.id === "common");
    const hasVenue = !!type?.fields.some((f) => f.id === "venue");
    return {
      type: type?.id ?? null,
      data,
      commonName: hasCommon && (vals.common || "").trim() ? vals.common.trim() : null,
      venue: hasVenue && (vals.venue || "").trim() ? vals.venue.trim() : null,
    };
  }

  function refreshPreview() {
    const r = read();
    if (!type || !r.data.length) { previewEl.textContent = ""; return; }
    const text = [r.commonName, r.data.join(type.defaultDataDelimiter), r.venue ? cfg.venueDisplay[r.venue] || "" : ""].filter(Boolean).join(", ");
    previewEl.textContent = `Will read as: ${text}`;
  }

  const labelOf = (f) => f.title + (f.required && (f.minLength > 0 || f.autofillIndex === 0) ? " *" : "");

  function textInput(f) {
    const input = el("input", {
      class: "text", type: "text", value: vals[f.id] || "", placeholder: f.placeholder || "",
      maxlength: f.maxLength > 0 ? String(f.maxLength) : null,
    });
    input.addEventListener("input", () => { vals[f.id] = input.value; refreshPreview(); onChange(); });
    input.addEventListener("keydown", (e) => { if (e.key === "Enter") { e.preventDefault(); onEnter(); } });
    return input;
  }

  function paintFields() {
    fieldsBox.replaceChildren();
    venueCombo = null;
    if (!type) {                 // no type chosen: no fields
      refreshPreview();
      return;
    }
    dataFieldsOf(type).forEach((f) => fieldsBox.append(frow(labelOf(f), textInput(f), f.description)));

    const common = type.fields.find((f) => f.id === "common");
    if (common) fieldsBox.append(frow(common.title, textInput(common), common.description));

    const venueField = type.fields.find((f) => f.id === "venue");
    if (venueField) {
      venueCombo = makeCombo({
        options: cfg.venues.map((n) => ({ value: n, label: n, sub: cfg.venueDisplay[n] })),
        value: vals.venue || "",
        placeholder: "(none)",
        onChange: (v) => { vals.venue = v; refreshPreview(); onChange(); },
      });
      fieldsBox.append(frow(venueField.title, venueCombo.el, venueField.description));
    }
    refreshPreview();
  }

  // load a location object: {type, data, commonName, venue}
  function load(loc) {
    type = locTypeById(loc?.type) || null;
    Object.keys(vals).forEach((k) => delete vals[k]);
    dataFieldsOf(type).forEach((f, i) => { vals[f.id] = String((loc?.data || [])[i] ?? ""); });
    vals.venue = loc?.venue ?? "";
    vals.common = loc?.commonName ?? "";
    typeCombo.set(type?.title ?? "");
    paintFields();
  }

  // Changing the type starts the data fields over (each type reads them differently).
  // Only the venue, and the common name where the new type has one, carry across.
  function switchType(t) {
    const venue = vals.venue || "";
    const common = vals.common || "";
    type = t;
    Object.keys(vals).forEach((k) => delete vals[k]);
    vals.venue = venue;
    if (t?.fields.some((f) => f.id === "common")) vals.common = common;
    paintFields();
  }

  function validate() {
    const errors = [];
    if (!type || !locTypeByTitle(typeCombo.get())) {
      errors.push("Pick a location type from the list.");
      return errors;
    }
    for (const f of dataFieldsOf(type)) {
      const v = (vals[f.id] || "").trim();
      if (!v) {
        if (f.required && f.minLength > 0) errors.push(`${f.title} is required.`);
        continue;
      }
      if (f.minLength > 0 && v.length < f.minLength) errors.push(`${f.title} must be at least ${f.minLength} characters.`);
      if (f.maxLength > 0 && v.length > f.maxLength) errors.push(`${f.title} can be at most ${f.maxLength} characters.`);
      if (type.id === "LATITUDE_LONGITUDE" && (f.id === "latitude" || f.id === "longitude") && isNaN(Number(v))) errors.push(`${f.title} must be a number (decimal degrees).`);
    }
    if (!read().data.length) errors.push("Enter the location details.");
    const common = type.fields.find((f) => f.id === "common");
    if (common && common.maxLength > 0 && (vals.common || "").trim().length > common.maxLength) errors.push(`${common.title} can be at most ${common.maxLength} characters.`);
    if (venueCombo && !venueCombo.valid()) errors.push("Venue must be chosen from the list.");
    return errors;
  }

  const root = el("div", { class: "loc-editor" },
    showQuickFill ? frow("Quick fill", known.el) : null,
    frow("Location Type", typeCombo.el),
    fieldsBox,
    previewEl);

  load(initial);

  return {
    el: root,
    read,
    validate,
    set: load,
    sig: () => JSON.stringify(read()),
    isEmpty: () => { const r = read(); return !r.data.length && !r.commonName && !r.venue; },
  };
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

    const closed = i.status === "CLOSED";   // closed incidents can't be edited
    return `
      <tr class="click cat-${cat} ${i.status === "PENDING" ? "pending" : ""} ${closed ? "closed" : ""}" ${closed ? "" : `data-edit="${esc(i.id)}" tabindex="0"`}>
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
        <td>${closed ? `<button class="btn small" data-reopen="${esc(i.id)}">↺ Re-open</button>` : `<button class="btn small" data-edit="${esc(i.id)}">✎ Edit</button>`}</td>
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
  const ro = e.target.closest("[data-reopen]");
  if (ro) { autoReopen = ro.dataset.reopen; location.hash = `edit=${ro.dataset.reopen}`; return; }
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

// -- CREATE: one form, sent with a single button --

function buildCreateForm() {
  view = "form";

  const typeCombo = makeCombo({ options: cfg.types, value: cfg.defaultType, placeholder: "Search incident types…", onChange: () => update() });
  const locEditor = makeLocationEditor({ onChange: () => update() });
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

    // The location is optional. If filled in, it's set (PATCH .../location) right after the incident is created.
    let locBody = null;
    if (!locEditor.isEmpty()) {
      const locErrors = locEditor.validate();
      errors.push(...locErrors);
      if (!locErrors.length) locBody = locEditor.read();
    }

    return { p, errors, locBody };
  }

  function update() {
    const { p, errors, locBody } = compute();
    errBox.hidden = !errors.length;
    errBox.textContent = errors.join(" ");
    saveBtn.disabled = errors.length > 0 || !p.type;
    summary.textContent = `Will send: ${Object.keys(p).join(", ") || "nothing yet"}${locBody ? " + location" : ""}`;
    preview.textContent = `POST ${CREATE_PATH}\n${JSON.stringify(p, null, 2)}`
      + (locBody ? `\n\nthen PATCH /api/incidents/{new id}/location\n${JSON.stringify(locBody, null, 2)}` : "");
  }

  saveBtn.addEventListener("click", async () => {
    const { p, errors, locBody } = compute();
    if (errors.length || !p.type) return;
    saveBtn.disabled = true;
    resetBtn.disabled = true;
    try {
      const before = new Set(incidents.map((i) => String(i.id)));
      const res = await request(CREATE_PATH, { method: "POST", json: p, auth: true });
      toast("Incident created.");
      incidents = await request("/api/incidents");

      // find the new incident: use the response if it has a numeric id, otherwise the one that just appeared
      let nid = null;
      const rid = res && typeof res === "object" ? (res.id ?? res.incident?.id ?? res.incidentId) : null;
      if (rid != null && /^\d+$/.test(String(rid))) nid = String(rid);
      else {
        const fresh = incidents.filter((i) => !before.has(String(i.id))).sort((a, b) => b.time - a.time);
        if (fresh.length) nid = String(fresh[0].id);
      }

      if (locBody) {
        if (!nid) toast("Incident created, but I couldn't tell which one it is, so the location wasn't set. Set it from the edit page.", "error");
        else {
          try {
            await request(`/api/incidents/${nid}/location`, { method: "PATCH", json: locBody, auth: true });
            incidents = await request("/api/incidents");
          } catch (ex) {
            toast(`Incident created, but the location wasn't set: ${ex.message}`, "error");
          }
        }
      }
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
      frow("Timestamp", el("div", { class: "inline" }, tsInput, tsNow), "Leave blank for the current time. 24-hour, your local time."),
      frow("Initial Narrative", narrative)),

    group("Location (optional)",
      locEditor.el,
      el("div", { class: "hint" }, "Set right after the incident is created. Needs the CHANGE_LOCATION permission (USE_CUSTOM_LOCATION for the custom type).")),

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
    ts: cur.time != null ? fmtTs(cur.time) : "",
  });
  let base = snapshot();            // what the server had the last time we synced

  // Send one change (PATCH), then reload so the page shows what the server now has.
  async function send(body, okMsg = "Saved", sub = "") {
    try {
      await request(path + sub, { method: "PATCH", json: body, auth: true });
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
    tsInput.classList.toggle("dirty", tsInput.value.trim() !== base.ts);
  };

  const typeCombo = makeCombo({
    options: cfg.types, value: base.type, placeholder: "Search incident types…",
    onChange: flagAll,
    onCommit: (v) => { if (v && v !== base.type) send({ type: v }, "Type updated"); },
    onCancel: () => { typeCombo.set(base.type); flagAll(); },
  });

  // Status isn't editable. The only action is closing the incident (PATCH .../status),
  // and a closed incident is locked.
  const statusBox = el("span", {});
  statusBox.innerHTML = statusPill(cur.status);
  let confirming = false;
  let confirmTimer;
  const disarm = () => {
    confirming = false;
    clearTimeout(confirmTimer);
    closeBtn.textContent = "Close Incident";
    closeBtn.classList.remove("armed");
  };
  const closeBtn = el("button", {
    class: "btn danger small",
    type: "button",
    onclick: async () => {
      if (!confirming) {           // first click arms it, a second click confirms
        confirming = true;
        closeBtn.textContent = "Click again to close";
        closeBtn.classList.add("armed");
        confirmTimer = setTimeout(disarm, 4000);
        return;
      }
      disarm();
      closeBtn.disabled = true;
      try {
        const res = await request(`${path}/status`, { method: "PATCH", auth: true });
        toast(`Incident ${res?.incident ?? id} is now ${res?.newStatus ?? "updated"}.`);
        incidents = await request("/api/incidents");
        if (String(res?.newStatus).toUpperCase() === "CLOSED") location.hash = "list";
        else apply();
      } catch (ex) {
        toast(ex.message, "error");
      }
      closeBtn.disabled = false;
    },
  }, "Close Incident");

  // A closed incident is locked. The only thing you can do is re-open it, with a reason:
  // the status is toggled back, then "Reopen Reason: <text>" is added as a narrative.
  const reopenBtn = el("button", { class: "btn small", type: "button", onclick: () => openReopen() }, "↺ Re-open");
  const reasonInput = el("input", {
    class: "text", type: "text", placeholder: "Reason for re-opening (required)",
    onkeydown: (e) => {
      if (e.key === "Enter") { e.preventDefault(); doReopen(); }
      else if (e.key === "Escape") closeReopen();
    },
  });
  const reasonConfirm = el("button", { class: "btn primary small", type: "button", onclick: () => doReopen() }, "Confirm Re-open");
  const reasonCancel = el("button", { class: "btn small", type: "button", onclick: () => closeReopen() }, "Cancel");
  const reasonRow = el("div", { class: "reopen-row", hidden: true }, reasonInput, reasonConfirm, reasonCancel);
  const lockNote = el("div", { class: "locknote", hidden: true, "data-keep": "" },
    el("div", { class: "lock-line" }, el("span", {}, "🔒 This incident is closed and can't be edited."), reopenBtn),
    reasonRow);

  function openReopen() { reopenBtn.hidden = true; reasonRow.hidden = false; reasonInput.focus(); }
  function closeReopen() { reasonRow.hidden = true; reopenBtn.hidden = false; reasonInput.value = ""; }

  async function doReopen() {
    const reason = reasonInput.value.trim();
    if (!reason) { toast("Enter a reason for re-opening.", "error"); reasonInput.focus(); return; }
    reasonConfirm.disabled = true;
    try {
      const res = await request(`${path}/status`, { method: "PATCH", auth: true });
      if (String(res?.newStatus).toUpperCase() === "CLOSED") throw new Error("The incident is still closed. Nothing was changed.");
      toast(`Incident ${res?.incident ?? id} re-opened.`);
      // separate request: the reason goes into the narrative
      try {
        await request(path, { method: "PATCH", json: { narrative: `Reopen Reason: ${reason}` }, auth: true });
      } catch (ex) {
        toast(`Re-opened, but the reason wasn't saved: ${ex.message}`, "error");
      }
      closeReopen();
      await pull().catch(() => {});
    } catch (ex) {
      toast(ex.message, "error");
    }
    reasonConfirm.disabled = false;
  }

  let wasLocked = false;
  function lockAll(on) {
    page.querySelectorAll("input, textarea, button").forEach((n) => { if (!n.closest("[data-keep]")) n.disabled = on; });
  }
  function refreshLock() {
    const locked = cur.status === "CLOSED";
    lockNote.hidden = !locked;
    closeBtn.hidden = locked;
    if (locked) { lockAll(true); wasLocked = true; }
    else if (wasLocked) {          // just re-opened: unlock the page again
      lockAll(false);
      wasLocked = false;
      updateLocState();
    }
  }

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

  // ----- location (applies as a whole: Enter in a field, or the Update Location button) -----
  let baseLoc = locSigOf(cur.location);
  const locState = el("span", { class: "hint" });
  const locApply = el("button", { class: "btn primary small", type: "button", disabled: true, onclick: () => applyLocation() }, "Update Location");
  const locEditor = makeLocationEditor({ initial: cur.location, onChange: () => updateLocState(), onEnter: () => applyLocation() });
  const locGroup = group("Location",
    locEditor.el,
    el("div", { class: "loc-actions" }, locApply, locState),
    el("div", { class: "hint" }, "Needs the CHANGE_LOCATION permission (USE_CUSTOM_LOCATION for the custom type). Asterisk (*) = required."));

  function locDirty() { return locEditor.sig() !== baseLoc; }
  function updateLocState() {
    const dirty = locDirty();
    if (cur.status !== "CLOSED") locApply.disabled = !dirty;
    locState.textContent = dirty ? "Unapplied changes — press Enter in a field, or click Update Location." : "";
    locGroup.classList.toggle("dirty-group", dirty);
  }
  async function applyLocation() {
    if (cur.status === "CLOSED" || !locDirty()) return;
    const errors = locEditor.validate();
    if (errors.length) { toast(errors[0], "error"); return; }
    const r = locEditor.read();
    locApply.disabled = true;
    await send({ data: r.data, type: r.type, commonName: r.commonName, venue: r.venue }, "Location updated", "/location");
    updateLocState();
  }

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
    if (tsInput.value.trim() === base.ts) tsInput.value = nb.ts;
    base = nb;
    statusBox.innerHTML = statusPill(cur.status);
    pub.checked = cur.publishStatus === "PUBLISHED";
    title.textContent = `Edit ${nb.type || "Incident"}`;
    flagAll();

    // location: follow the server unless you've changed something
    const nbLoc = locSigOf(cur.location);
    if (!locDirty() && nbLoc !== baseLoc) locEditor.set(cur.location);
    baseLoc = nbLoc;
    updateLocState();

    if (sigOf(cur) !== unitSig && !unitsBox.contains(document.activeElement)) paintUnits();
    repaintNarrative();
    refreshLock();
  }
  liveSync = apply;

  const page = el("div", { class: "page" },
    el("div", { class: "subhead", "data-keep": "" },
      el("button", { class: "back", onclick: () => { location.hash = "list"; } }, "‹ Incidents"),
      el("span", { class: "gray" }, `Incident #${id}`),
      el("a", { class: "link", href: `index.html#incident=${id}`, target: "_blank", rel: "noopener" }, "View in viewer ↗")),
    el("div", { class: "page-title" }, title),
    el("div", { class: "live-note" }, "Live edit — changes are sent the moment you pick a value or press Enter. An orange outline means it's typed but not applied yet (Esc undoes it)."),
    lockNote,

    group("Details",
      frow("Type", typeCombo.el),
      frow("Status", el("div", { class: "inline" }, statusBox, closeBtn), "Status can't be edited. Closing is final — a closed incident can't be edited."),
      frow("Published", el("label", { class: "check" }, pub, "Published")),
      frow("Timestamp", el("div", { class: "inline" }, tsInput, tsNow))),

    locGroup,

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
  updateLocState();
  refreshLock();
  if (autoReopen === String(id) && cur.status === "CLOSED") openReopen();   // came from the list's Re-open button
  autoReopen = null;
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
