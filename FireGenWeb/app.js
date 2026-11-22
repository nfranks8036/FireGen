// ---- Settings ----------------------------------------------------------
// Config prefix (the folder name in config file paths, e.g. "MontCo/units.json").
// Change this one value if it ever changes.
const CONFIG_PREFIX = "MontCo";

// Base URL of your API. Leave empty if this page is served from the same host.
const API_BASE = "127.0.0.1:8080";

// Which field of /api/users/<id> to show as the username.
// "name" -> e.g. "cyberedcake"      "login" -> e.g. "noah"
const USER_NAME_FIELD = "name";

const REFRESH_MS = 5000;
// -------------------------------------------------------------------------

const cfg = {
  unitsByLong: {},        // from config units.json: long name -> unit config
  statusShort: {},        // assignment status name -> abbreviation (ENROUTE -> ENR)
  opStatuses: [],         // operational status names, in config order
  agenciesByTitle: {},    // from /api/agencies (optional)
  agencyShort: {},        // agency long name -> short (built from /api/units)
  municipality: null,
  loaded: false,
};

let busy = false;
let incidents = [];
let unitsList = [];       // /api/units, kept in API order
let unitsById = {};
let unitsLoaded = false;
let loadedOnce = false;

let detailTab = "details";   // incident page: details | log
let statusFilter = "ALL";    // incidents tab: ALL | PENDING | ACTIVE | STALE | CLOSED
let unitStatusFilter = "ALL";// units tab: ALL | <operational status name>
let unitSearch = "";
let seenIds = null;          // incident IDs from the previous refresh
let freshIds = new Set();    // IDs that appeared in the latest refresh (row flashes once)
const logFilter = new Set(); // empty = show all log types

// user id -> username (filled in from /api/users/<id>)
const users = {};
const userFetching = new Set();
const userFailedAt = {};

const $ = (id) => document.getElementById(id);

function esc(v) {
  return String(v ?? "").replace(/[&<>"']/g, (c) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]
  ));
}

async function api(path) {
  // Without a scheme, fetch treats "127.0.0.1:8080/..." as a relative path, so add one.
  const base = API_BASE && !/^https?:\/\//.test(API_BASE) ? "http://" + API_BASE : API_BASE;
  const res = await fetch(base + path, { cache: "no-store" });
  let text = await res.text();

  // User IDs are bigger than JavaScript can hold exactly (> 2^53), so JSON.parse
  // would round them. Wrap them in quotes first so they stay exact.
  text = text
    .replace(/("(?:contributor|id)"\s*:\s*)(\d{16,})/g, '$1"$2"')
    .replace(/("contributors"\s*:\s*)\[([^\]]*)\]/g, (m, p, inner) =>
      p + "[" + inner.replace(/\d{16,}/g, (d) => '"' + d + '"') + "]");

  const body = JSON.parse(text);
  if (!body.success) throw new Error(`API error ${body.status ?? res.status}`);
  return body.response;
}

// Accepts either {"file": data, ...} or [{"file": data}, ...]
function normalizeConfigs(resp) {
  if (Array.isArray(resp)) return Object.assign({}, ...resp);
  return resp;
}

async function loadConfig() {
  const all = normalizeConfigs(await api("/api/structure/configs?file=all"));
  const get = (name) => all[`${CONFIG_PREFIX}/${name}`];

  (get("units.json") || []).forEach((u) => { cfg.unitsByLong[u.long] = u; });
  (get("assignment_statuses.json") || []).forEach((s) => { cfg.statusShort[s.name] = s.short; });
  cfg.opStatuses = [...(get("operational_statuses.json") || [])]
    .sort((a, b) => a.ordinal - b.ordinal)
    .map((s) => s.name);

  cfg.municipality = get("municipality.json");
  if (cfg.municipality) {
    $("place").textContent = `${cfg.municipality.name}, ${cfg.municipality.state.abbreviation}`;
  }

  // Agency details (stations, type). Optional: if this endpoint doesn't exist, the unit page
  // still works using what /api/units provides.
  try {
    (await api("/api/agencies")).forEach((a) => { cfg.agenciesByTitle[a.title] = a; });
  } catch { /* ignore */ }

  cfg.loaded = true;
}

function setUnits(list) {
  unitsList = list;
  unitsById = {};
  cfg.agencyShort = {};
  list.forEach((u) => {
    unitsById[u.id] = u;
    if (u.type === "$AGENCY") cfg.agencyShort[u.long] = u.short;
  });
}

// ---- Users -------------------------------------------------------------

function userName(id) {
  return users[id] ?? String(id ?? "—");
}

// Look up the username for every user who appears in an incident log (once each).
function ensureUsers(list) {
  const ids = new Set();
  list.forEach((i) => (i.log || []).forEach((l) => { if (l.contributor != null) ids.add(String(l.contributor)); }));

  ids.forEach((id) => {
    if (id in users || userFetching.has(id)) return;
    if (userFailedAt[id] && Date.now() - userFailedAt[id] < 60000) return; // retry after a minute

    userFetching.add(id);
    api(`/api/users/${id}`)
      .then((u) => { users[id] = u?.[USER_NAME_FIELD] ?? u?.name ?? id; })
      .catch(() => { userFailedAt[id] = Date.now(); })
      .finally(() => { userFetching.delete(id); render(); });
  });
}

// ---- Status style colors -----------------------------------------------
// The API gives each status a style like {"color":"YELLOW","background":"DEFAULT"}.
// Edit these tables if you use other color names.

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
  // unknown name: use it if the browser understands it as a CSS color ("teal", "#abc123", ...)
  return window.CSS?.supports?.("color", raw) ? raw : null;
}

// Returns an inline style string setting --rc (text) and --rb (background)
function styleAttr(style) {
  const c = cssColor(style?.color, TEXT_COLORS);
  const b = cssColor(style?.background, BG_COLORS);
  return esc((c ? `--rc:${c};` : "") + (b ? `--rb:${b};` : ""));
}

// ---- Helpers -----------------------------------------------------------

// Incident units look like {unit: <id>, short, channel, assignments}
function unitShort(u) {
  return u.short || unitsById[u.unit]?.short || cfg.unitsByLong[u.title]?.short || u.title || String(u.unit ?? "");
}

function unitLong(u) {
  return unitsById[u.unit]?.long || u.long || u.title || "";
}

function latestAssignment(u) {
  const list = u.assignments || [];
  return list.length ? list.reduce((a, b) => (b.timestamp >= a.timestamp ? b : a)) : null;
}

function isRemoved(u) {
  return latestAssignment(u)?.status === "REMOVED";
}

function statusAbbr(name) {
  return cfg.statusShort[name] || name;
}

// Keep the original casing; only turn underscores (and a leading "$") into spaces.
function clean(s) {
  return String(s ?? "").replace(/^\$/, "").replace(/_/g, " ");
}

// There is exactly one CREATE entry per incident, so show/group it as UPDATE.
function logType(l) {
  return l.type === "CREATE" ? "UPDATE" : l.type;
}

// Entry text sometimes starts with a "<HH:MM>" stamp; remove it only when it matches exactly.
function entryText(l) {
  return String(l.text ?? "").replace(/^<\d{2}:\d{2}>\s*/, "");
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
    hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23", // 24-hour time
  });
}

// The "format" string starts with the common name when one is set; split them apart.
// Incidents without a location come through as a blank string.
function splitLocation(loc = {}) {
  const common = (loc.commonName || "").trim();
  let address = (loc.format || "").trim();
  if (common && address.startsWith(common + ", ")) address = address.slice(common.length + 2);
  return { address, common };
}

function field(label, value) {
  return `<div class="field"><span class="label">${esc(label)}</span><span class="value">${value}</span></div>`;
}

// Fixed-width table: cols = [[label, widthPercent], ...]. Widths never change with content.
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

// Which incident is this unit attached to? (looks through the active incidents first)
function attachedIncidentId(unit) {
  for (const i of incidents) {
    for (const u of i.units || []) {
      if (u.unit !== unit.id) continue;
      const a = latestAssignment(u);
      if (!a || (a.status !== "REMOVED" && a.status !== "CLEAR")) return String(i.id);
    }
  }
  const a = unit.currentStatus?.assignment;
  if (a == null) return null;
  const id = typeof a === "object" ? (a.id ?? a.incident ?? a.incidentId) : a;
  return /^\d+$/.test(String(id)) ? String(id) : null;
}

function notFound(kind, id, backKey, backLabel, why) {
  return `
    <div class="group notfound">
      <div class="big">${esc(kind)} Not Found</div>
      <p>${esc(kind)} <b>${esc(id)}</b> ${esc(why)}</p>
      <button class="back" data-back="${esc(backKey)}">‹ Back to ${esc(backLabel)}</button>
    </div>`;
}

// ---- Routing (hash based) ----------------------------------------------
//   #incidents (default) | #units | #maps | #incident=<id> | #unit=<id>

function route() {
  const h = location.hash.replace(/^#/, "");
  const dec = (s) => { try { return decodeURIComponent(s); } catch { return s; } };
  let m = h.match(/^incident=(.*)$/);
  if (m) return { name: "incident", id: dec(m[1]) };
  m = h.match(/^unit=(.*)$/);
  if (m) return { name: "unit", id: dec(m[1]) };
  if (h === "units") return { name: "units" };
  if (h === "maps") return { name: "maps" };
  return { name: "incidents" };
}

function render() {
  const r = route();

  const activeTab = r.name === "incident" ? "incidents" : r.name === "unit" ? "units" : r.name;
  document.querySelectorAll("#tabs button").forEach((b) => {
    b.classList.toggle("on", b.dataset.view === activeTab);
  });

  // keep the search box focused while the page refreshes underneath it
  const a = document.activeElement;
  const keep = a && a.id === "unitSearch" ? [a.selectionStart, a.selectionEnd] : null;

  let html;
  if (r.name === "maps") html = `<div class="construction">Under construction</div>`;
  else if (r.name === "units") html = unitsView();
  else if (r.name === "unit") html = `<div class="page">${unitPage(r.id)}</div>`;
  else if (r.name === "incident") html = `<div class="page">${incidentPage(r.id)}</div>`;
  else html = incidentTable();

  $("view").innerHTML = html;

  if (keep) {
    const el = $("unitSearch");
    if (el) { el.focus(); el.setSelectionRange(keep[0], keep[1]); }
  }
}

// ---- Incidents tab -----------------------------------------------------

const STATUSES = ["PENDING", "ACTIVE", "STALE", "CLOSED"];
const CATEGORY_ICON = { FIRE: "🔥", EMS: "🚑", POLICE: "🚓", OTHER: "📋" };

function category(i) {
  const t = i.type?.tag;
  if (t === "FIRE") return "FIRE";
  if (t === "EMS") return "EMS";
  if (t === "POLICE" || t === "POLICE_WITH_WEAPON") return "POLICE";
  return "OTHER";
}

function statusPill(s) {
  return `<span class="pill ${esc(s)}">${esc(s)}</span>`;
}

// Clickable summary tiles above the table: click one to filter by status, click again to clear.
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

const INCIDENT_COLS = [["Status", 11], ["Type", 22], ["Location", 24], ["Common Name", 20], ["Units", 23]];

function incidentTable() {
  const sorted = [...incidents]
    .filter((i) => statusFilter === "ALL" || i.status === statusFilter)
    .sort((a, b) => b.time - a.time);

  const rows = sorted.map((i) => {
    const { address, common } = splitLocation(i.location);
    const cat = category(i);
    const chips = (i.units || []).filter((u) => !isRemoved(u)).map((u) => {
      const a = latestAssignment(u);
      return `<span class="chip" style="${styleAttr(a?.style)}" title="${esc(unitLong(u))}${a ? " – " + esc(a.status) : ""}"><b>${esc(unitShort(u))}</b>${a ? `<span class="st">${esc(statusAbbr(a.status))}</span>` : ""}</span>`;
    }).join("");

    return `
      <tr class="click cat-${cat} ${i.status === "PENDING" ? "pending" : ""} ${freshIds.has(String(i.id)) ? "fresh" : ""}" data-id="${esc(i.id)}" tabindex="0">
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
      </tr>`;
  }).join("");

  const empty = !loadedOnce ? "Loading…" : incidents.length ? "No incidents with this status." : "No active incidents.";

  return statTiles() + gridTable(INCIDENT_COLS, rows, { minWidth: 900, empty });
}

// ---- Units tab ---------------------------------------------------------

function unitStatusText(u) {
  const s = u.currentStatus || {};
  return (s.status || "—") + (s.secondary ? " → " + s.secondary : "");
}

const UNIT_COLS = [["Unit", 9], ["Name", 25], ["Agency", 11], ["Status", 22], ["Last Updated", 21], ["Incident", 12]];

function unitsView() {
  if (!unitsLoaded) return `<div class="group gray">Loading…</div>`;

  const counts = {};
  unitsList.forEach((u) => {
    const s = u.currentStatus?.status || "UNKNOWN";
    counts[s] = (counts[s] || 0) + 1;
  });
  // only statuses that actually have units, in config order
  const names = [...cfg.opStatuses, ...Object.keys(counts).filter((s) => !cfg.opStatuses.includes(s))]
    .filter((s) => counts[s]);

  // compact filter chips; each takes the text color of a unit with that status
  const chip = (key, label, n, sample) => {
    const c = sample ? cssColor(sample.currentStatus?.style?.color, TEXT_COLORS) : null;
    return `
      <button class="mini ${unitStatusFilter === key ? "on" : ""}" data-ustatus="${esc(key)}" style="${c ? `--sc:${esc(c)}` : ""}">
        <span class="dot"></span><span class="n">${n}</span>${esc(label)}
      </button>`;
  };
  const chips = chip("ALL", "TOTAL", unitsList.length, null)
    + names.map((s) => chip(s, s, counts[s], unitsList.find((u) => u.currentStatus?.status === s))).join("");

  const q = unitSearch.trim().toLowerCase();
  const shown = unitsList.filter((u) => {
    if (unitStatusFilter !== "ALL" && (u.currentStatus?.status || "UNKNOWN") !== unitStatusFilter) return false;
    if (!q) return true;
    return [u.short, u.long, u.parentAgency, cfg.agencyShort[u.parentAgency], unitStatusText(u)]
      .some((v) => String(v ?? "").toLowerCase().includes(q));
  });

  const rows = shown.map((u) => {
    const s = u.currentStatus || {};
    const iid = attachedIncidentId(u);
    return `
      <tr class="click styled" data-uid="${esc(u.id)}" tabindex="0" style="${styleAttr(s.style)}">
        <td><b>${esc(u.short)}</b></td>
        <td>${esc(u.long)}</td>
        <td>${esc(cfg.agencyShort[u.parentAgency] || u.parentAgency)}</td>
        <td>${esc(unitStatusText(u))}</td>
        <td class="mono">${s.lastUpdate ? esc(fullTime(s.lastUpdate)) : "—"}</td>
        <td>${iid ? `<a class="link" href="#incident=${esc(iid)}">#${esc(iid)}</a>` : "—"}</td>
      </tr>`;
  }).join("");

  return `
    <div class="toolbar">
      ${chips}
      <input id="unitSearch" class="search" type="search" placeholder="Search units, agencies, statuses…" value="${esc(unitSearch)}" autocomplete="off">
      <span class="gray">${shown.length} of ${unitsList.length}</span>
    </div>
    ${gridTable(UNIT_COLS, rows, { minWidth: 950, empty: "No units match." })}`;
}

// ---- Single unit page --------------------------------------------------

function unitPage(id) {
  if (!unitsLoaded) return `<div class="group gray">Loading…</div>`;

  const u = unitsById[id];
  if (!u) return notFound("Unit", id, "units", "Units", "does not exist.");

  const head = `
    <div class="subhead">
      <button class="back" data-back="units">‹ Units</button>
      <span class="gray">Unit #${esc(id)}</span>
    </div>`;

  const s = u.currentStatus || {};
  const agency = cfg.agenciesByTitle[u.parentAgency];
  const agencyUnits = unitsList.filter((x) => x.parentAgency === u.parentAgency && x.type !== "$AGENCY").length;

  const iid = attachedIncidentId(u);
  const inc = iid ? incidents.find((i) => String(i.id) === iid) : null;
  const incAddress = inc ? splitLocation(inc.location).address : "";
  const incText = iid
    ? `<a class="link" href="#incident=${esc(iid)}">#${esc(iid)}${inc ? ` · ${esc(inc.type?.name)}${incAddress ? " · " + esc(incAddress) : ""}` : ""}</a>`
    : `<span class="gray">None</span>`;

  // 1. Unit
  const g1 = `
    <div class="group">
      <div class="big orange">${esc(u.long)}</div>
      ${field("Agency", esc(agency?.formatted || cfg.agencyShort[u.parentAgency] || u.parentAgency))}
    </div>`;

  // 2. Status
  const g2 = `
    <div class="group">
      <div class="big"><span class="statline${cssColor(s.style?.background, BG_COLORS) ? " filled" : ""}" style="${styleAttr(s.style)}">${esc(unitStatusText(u))}</span></div>
      ${field("Short Name", esc(u.short))}
      ${field("Unit ID", esc(u.id))}
      ${field("Type", esc(clean(u.type)))}
      ${u.number !== -999999 && u.number != null ? field("Number", esc(u.number)) : ""}
      ${field("Last Updated", s.lastUpdate ? `${esc(fullTime(s.lastUpdate))} <span class="gray">(${esc(ago(s.lastUpdate))})</span>` : "—")}
      ${field("Current Incident", incText)}
    </div>`;

  // 3. Agency
  const station = agency?.station ? esc(agency.station).replace(/\n/g, "<br>") : "";
  const g3 = `
    <div class="group">
      <div class="big">${esc(u.parentAgency)}</div>
      ${field("Short Name", esc(cfg.agencyShort[u.parentAgency] || agency?.short || "—"))}
      ${agency?.type ? field("Agency Type", esc(clean(agency.type))) : ""}
      ${station ? field("Station", station) : ""}
      ${field("Units", String(agencyUnits))}
    </div>`;

  return head + g1 + g2 + g3;
}

// ---- Single incident page ----------------------------------------------

function incidentPage(id) {
  if (!loadedOnce) return `<div class="group gray">Loading…</div>`;

  const i = incidents.find((x) => String(x.id) === id);
  if (!i) return notFound("Incident", id, "incidents", "Incidents", "is not in the active incident list. It may have been closed, or the link may be wrong.");

  const head = `
    <div class="subhead">
      <button class="back" data-back="incidents">‹ Incidents</button>
      <span class="gray">Incident #${esc(id)}</span>
    </div>`;

  const tabs = `
    <nav class="subtabs">
      <button data-sub="details" class="${detailTab === "details" ? "on" : ""}">Incident Details</button>
      <button data-sub="log" class="${detailTab === "log" ? "on" : ""}">Incident Log</button>
    </nav>`;

  return head + tabs + (detailTab === "log" ? logGroup(i) : detailsTab(i));
}

// Narrative and Log share one table: small time / type / user columns, wide entry text.
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

function detailsTab(i) {
  const loc = i.location || {};
  const address = (loc.format || "").trim();

  // 1. Location
  const g1 = `
    <div class="group">
      <div class="big orange">${address ? esc(address) : '<span class="gray">No location set</span>'}</div>
      ${loc.venue ? field("Venue", esc(loc.venue)) : ""}
    </div>`;

  // 2. Incident type
  const g2 = `
    <div class="group">
      <div class="big">${esc(i.type?.name)}</div>
      ${field("Incident Number", esc(i.id))}
      ${field("Status", statusPill(i.status))}
      ${field("Timestamp", `${esc(fullTime(i.time))} <span class="gray">(${esc(ago(i.time))})</span>`)}
      ${field("Contributor Count", String((i.contributors || []).length))}
      ${field("Published", esc(i.publishStatus))}
    </div>`;

  // 3. Narrative
  const narrative = (i.log || []).filter((l) => l.type === "NARRATIVE");
  const g3 = `
    <div class="group flush">
      <h3 class="group-title">Narrative</h3>
      ${gridTable(ENTRY_COLS, entryRows(narrative), { minWidth: 720, empty: "No narrative entries." })}
    </div>`;

  // 4. Units (rows take the color of their status style)
  const units = i.units || [];
  const unitRows = units.map((u) => {
    const a = latestAssignment(u);
    const status = a ? a.status + (a.secondary ? " → " + a.secondary : "") : "—";
    const short = esc(unitShort(u));
    return `
      <tr class="styled ${a?.status === "REMOVED" ? "removed" : ""}" style="${styleAttr(a?.style)}">
        <td><b>${unitsById[u.unit] ? `<a class="link" href="#unit=${esc(u.unit)}">${short}</a>` : short}</b></td>
        <td>${esc(unitLong(u))}</td>
        <td>${esc(status)}</td>
        <td class="mono">${a ? `${esc(fullTime(a.timestamp))} <span class="gray">(${esc(ago(a.timestamp))})</span>` : "—"}</td>
      </tr>`;
  }).join("");
  const g4 = `
    <div class="group flush">
      <h3 class="group-title">Units</h3>
      ${gridTable([["Unit", 12], ["Name", 30], ["Status", 28], ["Last Updated", 30]], unitRows, { minWidth: 700, empty: "No units assigned." })}
    </div>`;

  return g1 + g2 + g3 + g4;
}

function logGroup(i) {
  const allLog = i.log || [];
  const counts = {};
  allLog.forEach((l) => { counts[logType(l)] = (counts[logType(l)] || 0) + 1; });
  const active = [...logFilter].filter((t) => counts[t]);
  const shown = allLog.filter((l) => !active.length || active.includes(logType(l)));

  const filters = `
    <div class="filters">
      <button class="${active.length ? "" : "on"}" data-filter="">All ${allLog.length}</button>
      ${Object.keys(counts).sort((a, b) => counts[b] - counts[a] || a.localeCompare(b)).map((t) => `
        <button class="${active.includes(t) ? "on" : ""}" data-filter="${esc(t)}">${esc(t)} ${counts[t]}</button>`).join("")}
    </div>`;

  return `
    <div class="group flush">
      <h3 class="group-title">Log (${shown.length}${active.length ? " of " + allLog.length : ""})</h3>
      ${filters}
      ${gridTable(ENTRY_COLS, entryRows(shown), { minWidth: 720, empty: "No log entries." })}
    </div>`;
}

// ---- Refresh loop ------------------------------------------------------

async function refresh() {
  if (busy) return;
  busy = true;
  try {
    if (!cfg.loaded) await loadConfig();

    // /api/units is only polled while you're looking at units (and once at startup
    // so incident pages can show unit names).
    const r = route();
    const onUnits = r.name === "units" || r.name === "unit";
    const [inc, un] = await Promise.all([
      api("/api/incidents"),
      onUnits || !unitsLoaded
        ? api("/api/units").catch((e) => { if (onUnits) throw e; return null; })
        : null,
    ]);

    incidents = inc;
    if (un) { setUnits(un); unitsLoaded = true; }

    const ids = new Set(incidents.map((i) => String(i.id)));
    freshIds = seenIds ? new Set([...ids].filter((id) => !seenIds.has(id))) : new Set();
    seenIds = ids;
    loadedOnce = true;
    render();
    freshIds = new Set(); // flash only once
    ensureUsers(incidents);

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

// ---- Events ------------------------------------------------------------

$("tabs").addEventListener("click", (e) => {
  const b = e.target.closest("[data-view]");
  if (b) location.hash = b.dataset.view;
});

$("view").addEventListener("click", (e) => {
  const st = e.target.closest("[data-status]");
  if (st) {
    statusFilter = statusFilter === st.dataset.status ? "ALL" : st.dataset.status;
    render();
    return;
  }

  const us = e.target.closest("[data-ustatus]");
  if (us) {
    unitStatusFilter = unitStatusFilter === us.dataset.ustatus ? "ALL" : us.dataset.ustatus;
    render();
    return;
  }

  const sub = e.target.closest("[data-sub]");
  if (sub) { detailTab = sub.dataset.sub; render(); return; }

  const f = e.target.closest("[data-filter]");
  if (f) {
    const type = f.dataset.filter;
    if (!type) logFilter.clear();                          // "All"
    else if (logFilter.has(type)) logFilter.delete(type);  // toggle off
    else logFilter.add(type);                              // toggle on (multi-select)
    render();
    return;
  }

  const back = e.target.closest("[data-back]");
  if (back) { location.hash = back.dataset.back; return; }

  if (e.target.closest("a")) return; // normal links (#incident=… / #unit=…) navigate on their own

  const row = e.target.closest("tr[data-id], tr[data-uid]");
  if (row) location.hash = row.dataset.id ? `incident=${row.dataset.id}` : `unit=${row.dataset.uid}`;
});

$("view").addEventListener("keydown", (e) => {
  if (e.key !== "Enter" && e.key !== " ") return;
  const row = e.target.closest("tr[data-id], tr[data-uid]");
  if (row) {
    e.preventDefault();
    location.hash = row.dataset.id ? `incident=${row.dataset.id}` : `unit=${row.dataset.uid}`;
  }
});

$("view").addEventListener("input", (e) => {
  if (e.target.id === "unitSearch") { unitSearch = e.target.value; render(); }
});

document.addEventListener("keydown", (e) => {
  if (e.key !== "Escape") return;
  const r = route();
  if (r.name === "incident") location.hash = "incidents";
  else if (r.name === "unit") location.hash = "units";
});

window.addEventListener("hashchange", () => {
  detailTab = "details";
  window.scrollTo(0, 0);
  refresh(); // make sure the page you just opened has fresh data (e.g. units)
  render();
});

render();
refresh();
setInterval(refresh, REFRESH_MS);
