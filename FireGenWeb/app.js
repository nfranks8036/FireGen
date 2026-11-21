// ---- Settings ----------------------------------------------------------
// Config prefix (the folder name in config file paths, e.g. "MontCo/units.json").
// Change this one value if it ever changes.
const CONFIG_PREFIX = "MontCo";

// Base URL of your API. Leave empty if this page is served from the same host.
const API_BASE = "http://127.0.0.1:8080";

const REFRESH_MS = 5000;
// -------------------------------------------------------------------------

const cfg = { unitsByLong: {}, statusShort: {}, municipality: null, loaded: false };
let busy = false;
let incidents = [];
let loadedOnce = false;
let openId = null;
const logFilter = new Set(); // empty = show all log types

const $ = (id) => document.getElementById(id);

function esc(v) {
  return String(v ?? "").replace(/[&<>"']/g, (c) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]
  ));
}

async function api(path) {
  const res = await fetch(API_BASE + path, { cache: "no-store" });
  const body = await res.json();
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

  (get("units.json") || []).forEach((u) => {
    cfg.unitsByLong[u.long] = u;
  });
  (get("assignment_statuses.json") || []).forEach((s) => {
    cfg.statusShort[s.name] = s.short;
  });
  cfg.municipality = get("municipality.json");
  if (cfg.municipality) {
    $("title").textContent = `Active Incidents – ${cfg.municipality.name}, ${cfg.municipality.state.abbreviation}`;
  }
  cfg.loaded = true;
}

function unitShort(u) {
  const match = cfg.unitsByLong[u.title];
  return match ? match.short : u.title;
}

function latestAssignment(u) {
  const list = u.assignments || [];
  return list.length ? list.reduce((a, b) => (b.timestamp >= a.timestamp ? b : a)) : null;
}

function statusAbbr(name) {
  return cfg.statusShort[name] || name;
}

function ago(unixSec) {
  const s = Math.max(0, Math.floor(Date.now() / 1000 - unixSec));
  if (s < 60) return `${s}s ago`;
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ${Math.floor((s % 3600) / 60)}m ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

function clock(unixSec) {
  return new Date(unixSec * 1000).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function render(incidents) {
  const sorted = [...incidents].sort((a, b) => b.time - a.time);

  $("count").textContent = `${sorted.length} incident${sorted.length === 1 ? "" : "s"}`;
  $("empty").hidden = sorted.length > 0;
  $("incidents").hidden = sorted.length === 0;

  $("rows").innerHTML = sorted.map((i) => {
    const loc = i.location || {};
    const locMain = loc.commonName || loc.format || "Unknown";
    const locSub = loc.commonName ? loc.format : (loc.venue || "");

    const units = (i.units || []).map((u) => {
      const a = latestAssignment(u);
      // hide units that were removed from the incident
      if (a && a.status === "REMOVED") return "";
      const st = a ? `<span class="st">${esc(statusAbbr(a.status))}</span>` : "";
      return `<span class="chip" title="${esc(u.title)}">${esc(unitShort(u))}${st}</span>`;
    }).join("");

    return `
      <tr data-id="${esc(i.id)}" tabindex="0" class="tag-${esc(i.type?.tag)} status-${esc(i.status)}">
        <td class="mono">${esc(clock(i.time))}<div class="sub">${esc(ago(i.time))}</div></td>
        <td><div class="type">${esc(i.type?.name)}</div><div class="sub">#${esc(i.id)}</div></td>
        <td>${esc(locMain)}<div class="sub">${esc(locSub)}</div></td>
        <td><div class="units">${units || '<span class="sub">None</span>'}</div></td>
        <td><span class="badge ${esc(i.status)}">${esc(i.status)}</span></td>
      </tr>`;
  }).join("");
}

async function refresh() {
  if (busy) return;
  busy = true;
  try {
    if (!cfg.loaded) await loadConfig();
    incidents = await api("/api/incidents");
    loadedOnce = true;
    render(incidents);
    renderDetail();
    $("error").hidden = true;
    $("updated").textContent = `Updated ${new Date().toLocaleTimeString()}`;
  } catch (err) {
    $("error").hidden = false;
    $("error").textContent = `Could not refresh: ${err.message}. Retrying…`;
  } finally {
    busy = false;
  }
}

// ---- Incident detail view ----------------------------------------------

function fullTime(sec) {
  return new Date(sec * 1000).toLocaleString([], {
    month: "short", day: "numeric", hour: "2-digit", minute: "2-digit", second: "2-digit",
  });
}

function renderDetail() {
  const ov = $("detail");
  if (!openId) {
    ov.hidden = true;
    document.body.classList.remove("noscroll");
    ov.innerHTML = "";
    return;
  }
  ov.hidden = false;
  document.body.classList.add("noscroll");

  const oldPanel = ov.querySelector(".panel");
  const scrollTop = oldPanel ? oldPanel.scrollTop : 0;

  const i = incidents.find((x) => String(x.id) === openId);
  let body;

  if (!i) {
    body = `<div class="panel-head"><h2>Incident #${esc(openId)}</h2>
      <button class="close" data-close aria-label="Close">✕</button></div>
      <p class="sub">${loadedOnce ? "This incident is no longer in the active list." : "Loading…"}</p>`;
  } else {
    const loc = i.location || {};
    const units = (i.units || []).map((u) => {
      const assigns = [...(u.assignments || [])].sort((a, b) => a.timestamp - b.timestamp);
      const channel = u.channel && u.channel !== "NONE SET" ? ` · ${esc(u.channel)}` : "";
      return `
        <div class="unit-card">
          <div class="name">${esc(unitShort(u))} <span class="sub">${esc(u.title)}${channel}</span></div>
          <ul>
            ${assigns.map((a) => `
              <li class="${a.status === "REMOVED" ? "removed" : ""}">
                <span>${esc(a.status)}${a.secondary ? " → " + esc(a.secondary) : ""}</span>
                <span class="mono">${esc(clock(a.timestamp))}</span>
              </li>`).join("")}
          </ul>
        </div>`;
    }).join("");

    const allLog = i.log || [];
    const counts = {};
    allLog.forEach((l) => { counts[l.type] = (counts[l.type] || 0) + 1; });
    const active = [...logFilter].filter((t) => counts[t]);
    const shown = allLog.filter((l) => !active.length || active.includes(l.type));
    const filters = `
      <div class="filters">
        <button class="chip fbtn ${active.length ? "" : "on"}" data-filter="">All <span class="st">${allLog.length}</span></button>
        ${Object.keys(counts).sort().map((t) => `
          <button class="chip fbtn ${active.includes(t) ? "on" : ""}" data-filter="${esc(t)}">${esc(t)} <span class="st">${counts[t]}</span></button>`).join("")}
      </div>`;

    const log = [...shown].sort((a, b) => b.time - a.time).map((l) => `
      <li>
        <span class="t">${esc(clock(l.time))}</span>
        <span class="k ${esc(l.type)}">${esc(l.type)}</span>
        <span class="x">${esc(l.text)}</span>
      </li>`).join("");

    const open = i.status !== "CLOSED";
    const links = (i.links || []).map((l) => `<div>${esc(typeof l === "string" ? l : JSON.stringify(l))}</div>`).join("");

    body = `
      <div class="panel-head">
        <div>
          <h2>${esc(i.type?.name)}</h2>
          <span class="badge ${esc(i.status)}">${esc(i.status)}</span>
          <span class="sub">#${esc(i.id)}</span>
        </div>
        <button class="close" data-close aria-label="Close">✕</button>
      </div>

      <h3>Details</h3>
      <dl class="kv">
        <dt>Location</dt><dd>${esc(loc.commonName || loc.format || "Unknown")}${loc.commonName ? `<div class="sub">${esc(loc.format)}</div>` : ""}</dd>
        ${loc.venue ? `<dt>Venue</dt><dd>${esc(loc.venue)}</dd>` : ""}
        <dt>Started</dt><dd>${esc(fullTime(i.time))} <span class="sub">(${esc(ago(i.time))})</span></dd>
        <dt>Category</dt><dd>${esc(i.type?.generic)}${i.type?.selectedQualifier && i.type.selectedQualifier !== "None" ? ` · ${esc(i.type.selectedQualifier)}` : ""} <span class="sub">${esc(i.type?.tag)}</span></dd>
        ${open && i.nextStale ? `<dt>Marked stale</dt><dd>${esc(fullTime(i.nextStale))}</dd>` : ""}
        ${open && i.nextClosedDueToStale ? `<dt>Auto-closes</dt><dd>${esc(fullTime(i.nextClosedDueToStale))}</dd>` : ""}
        <dt>Contributors</dt><dd>${(i.contributors || []).length}</dd>
        <dt>Published</dt><dd>${esc(i.publishStatus)}</dd>
        ${links ? `<dt>Links</dt><dd>${links}</dd>` : ""}
      </dl>

      <h3>Units (${(i.units || []).length})</h3>
      ${units || '<p class="sub">No units assigned.</p>'}

      <h3>Log (${shown.length}${active.length ? " of " + allLog.length : ""})</h3>
      ${filters}
      <ul class="log">${log || '<li><span></span><span></span><span class="x">No log entries.</span></li>'}</ul>`;
  }

  ov.innerHTML = `<div class="panel">${body}</div>`;
  ov.querySelector(".panel").scrollTop = scrollTop;
}

function syncFromHash() {
  const m = location.hash.match(/^#incident=(\d+)$/);
  openId = m ? m[1] : null;
  renderDetail();
}

function openIncident(id) { location.hash = `incident=${id}`; }
function closeDetail() { history.pushState("", document.title, location.pathname + location.search); syncFromHash(); }

$("rows").addEventListener("click", (e) => {
  const tr = e.target.closest("tr[data-id]");
  if (tr) openIncident(tr.dataset.id);
});
$("rows").addEventListener("keydown", (e) => {
  if (e.key !== "Enter" && e.key !== " ") return;
  const tr = e.target.closest("tr[data-id]");
  if (tr) { e.preventDefault(); openIncident(tr.dataset.id); }
});
$("detail").addEventListener("click", (e) => {
  const f = e.target.closest("[data-filter]");
  if (f) {
    const t = f.dataset.filter;
    if (!t) logFilter.clear();                       // "All"
    else if (logFilter.has(t)) logFilter.delete(t);  // toggle off
    else logFilter.add(t);                           // toggle on (multi-select)
    renderDetail();
    return;
  }
  if (e.target.id === "detail" || e.target.closest("[data-close]")) closeDetail();
});
document.addEventListener("keydown", (e) => { if (e.key === "Escape" && openId) closeDetail(); });
window.addEventListener("hashchange", syncFromHash);

syncFromHash();
refresh();
setInterval(refresh, REFRESH_MS);
