"use strict";
/* DeviceID+ WebUI (fork of sidex15/deviceidchanger, AGPL-3.0).
   All device operations go through ksu.exec shell; config.json is mirrored
   into flat files so boot scripts never need a JSON parser. */

var MODDIR = "/data/adb/modules/deviceidchanger";
var SSAID_PATH = "/data/system/users/0/settings_ssaid.xml";
var BACKUP_PATH = "/storage/emulated/0/settings_ssaid.backup.xml";
var TS_TARGET = "/data/adb/tricky_store/target.txt";

var DEFAULT_CONFIG = {
  ssaid: { globalId: "", apps: {} },
  props: { enabled: false, list: [{ key: "ro.build.host", value: "c3-miui-ota-bd110" }] },
  perapp: { enabled: false, apps: {} }
};

/* Shell script (re)generated on every SSAID apply; written via base64 to avoid
   quoting issues. Consumes .apply_list lines: "<pkg> <uid> <id16hex>".
   Android 16/HyperOS 3 appends a second root-level element (<namespaceHashes/>)
   AFTER </settings>; removing/re-adding the closing tag swallows it into
   <settings> and bootloops system_server. So inserts go strictly BEFORE the
   closing tag (awk, no sed \n portability doubts), a backup is taken before
   any write, and the re-encoded file is validated before it is trusted. */
var APPLY_SCRIPT = [
  "#!/system/bin/sh",
  "# Applies SSAID entries listed in .apply_list (pkg uid id16hex per line).",
  "# Never remove/re-add </settings>: a <namespaceHashes/> element may follow",
  "# it and would end up inside <settings>, killing system_server at boot.",
  "MODDIR=/data/adb/modules/deviceidchanger",
  "SSAID=" + SSAID_PATH,
  "TMP=$MODDIR/.tmp_ssaid_apply.xml",
  "NEW=$MODDIR/.tmp_ssaid_new.xml",
  "CHECK=$MODDIR/.tmp_ssaid_check.xml",
  "LIST=$MODDIR/.apply_list",
  "BAK=$SSAID.devidplus.bak",
  "",
  'cp "$SSAID" "$BAK" || exit 1',
  "",
  'if file "$SSAID" | grep -q ASCII; then',
  '  cp "$SSAID" "$TMP" || exit 1',
  "  plain=1",
  "else",
  '  abx2xml "$SSAID" "$TMP" || exit 1',
  "  plain=0",
  "fi",
  "",
  'precount=$(grep -c \'<setting \' "$TMP")',
  'maxid=$(sed -n \'s/.*<setting id="\\([0-9][0-9]*\\)".*/\\1/p\' "$TMP" | sort -n | tail -n 1)',
  '[ -n "$maxid" ] || maxid=0',
  "",
  "while read -r pkg uid val; do",
  '  [ -n "$pkg" ] || continue',
  '  if grep -q "package=\\"$pkg\\"" "$TMP"; then',
  '    # value precedes package on the line; defaultValue has capital V so',
  "    # the lowercase pattern only hits the current SSAID attribute.",
  '    sed -i "/package=\\"$pkg\\"/s/value=\\"[0-9a-fA-F]*\\"/value=\\"$val\\"/" "$TMP"',
  "  else",
  "    maxid=$((maxid + 1))",
  '    entry="    <setting id=\\"$maxid\\" name=\\"$uid\\" value=\\"$val\\" package=\\"$pkg\\" defaultValue=\\"$val\\" defaultSysSet=\\"false\\" tag=\\"null\\" />"',
  '    awk -v line="$entry" \'{ if (index($0, "</settings>")) print line; print }\' "$TMP" > "$NEW" || exit 1',
  '    mv "$NEW" "$TMP" || exit 1',
  "  fi",
  'done < "$LIST"',
  "",
  "fail=",
  'if [ "$plain" = 1 ]; then',
  '  cat "$TMP" > "$SSAID" || fail=1',
  "else",
  '  xml2abx "$TMP" "$SSAID" || fail=1',
  "fi",
  "",
  "# Validate what was written: decodable, exactly one closing tag, closing tag",
  "# still before <namespaceHashes/>, and no <setting> entries lost.",
  'if [ -z "$fail" ]; then',
  '  if [ "$plain" = 1 ]; then',
  '    cp "$SSAID" "$CHECK" || fail=1',
  "  else",
  '    abx2xml "$SSAID" "$CHECK" || fail=1',
  "  fi",
  "fi",
  'if [ -z "$fail" ]; then',
  '  close_count=$(grep -c \'</settings>\' "$CHECK")',
  '  close_line=$(grep -n \'</settings>\' "$CHECK" | head -n 1 | cut -d: -f1)',
  '  ns_line=$(grep -n \'<namespaceHashes\' "$CHECK" | head -n 1 | cut -d: -f1)',
  '  postcount=$(grep -c \'<setting \' "$CHECK")',
  '  [ "$close_count" = "1" ] || fail=1',
  '  if [ -n "$ns_line" ]; then',
  '    [ -n "$close_line" ] && [ "$close_line" -lt "$ns_line" ] || fail=1',
  "  fi",
  '  [ "$postcount" -ge "$precount" ] || fail=1',
  "fi",
  "",
  'if [ -n "$fail" ]; then',
  '  cp "$BAK" "$SSAID"',
  '  chown 1000:1000 "$SSAID"',
  '  chmod 600 "$SSAID"',
  '  restorecon "$SSAID" 2>/dev/null',
  '  rm -f "$TMP" "$NEW" "$CHECK" "$LIST"',
  '  echo "ERROR: validation failed, backup restored" >&2',
  "  exit 1",
  "fi",
  "",
  'chown 1000:1000 "$SSAID"',
  'chmod 600 "$SSAID"',
  'restorecon "$SSAID" 2>/dev/null',
  'rm -f "$TMP" "$NEW" "$CHECK" "$LIST"',
  "echo OK",
  ""
].join("\n");

// ---------- KSU helpers ----------

var cbSeq = 0;
function exec(cmd) {
  return new Promise(function (resolve, reject) {
    var name = "devid_cb_" + Date.now() + "_" + (cbSeq++);
    window[name] = function (errno, stdout, stderr) {
      delete window[name];
      resolve({ errno: errno, stdout: stdout, stderr: stderr });
    };
    try {
      ksu.exec(cmd, "{}", name);
    } catch (e) {
      delete window[name];
      reject(e);
    }
  });
}

async function sh(cmd) {
  var r = await exec(cmd);
  if (r.errno !== 0) throw new Error(r.stderr || ("exit code " + r.errno));
  return r.stdout;
}

function toast(msg) {
  try { ksu.toast(msg); } catch (e) { /* toast is best-effort */ }
}

function status(msg) {
  document.getElementById("statusbar").textContent = msg || "";
}

function b64(s) {
  return btoa(unescape(encodeURIComponent(s)));
}

// base64 keeps arbitrary content safe through the single-shell exec channel
async function writeFile(path, content) {
  await sh("echo " + b64(content) + " | base64 -d > " + path);
}

function randHex(n) {
  var chars = "abcdef0123456789", out = "";
  for (var i = 0; i < n; i++) out += chars.charAt(Math.floor(Math.random() * chars.length));
  return out;
}

function isHex16(s) { return /^[0-9a-f]{16}$/.test(s); }

function el(tag, cls, text) {
  var e = document.createElement(tag);
  if (cls) e.className = cls;
  if (text !== undefined) e.textContent = text;
  return e;
}

// ---------- Config ----------

var cfg = JSON.parse(JSON.stringify(DEFAULT_CONFIG));

async function loadConfig() {
  try {
    var raw = await sh("cat " + MODDIR + "/config.json 2>/dev/null");
    var parsed = JSON.parse(raw);
    if (parsed.ssaid) {
      cfg.ssaid.globalId = parsed.ssaid.globalId || "";
      cfg.ssaid.apps = parsed.ssaid.apps || {};
    }
    if (parsed.props) {
      cfg.props.enabled = !!parsed.props.enabled;
      if (Array.isArray(parsed.props.list)) cfg.props.list = parsed.props.list;
    }
    if (parsed.perapp) {
      cfg.perapp.enabled = !!parsed.perapp.enabled;
      if (parsed.perapp.apps && typeof parsed.perapp.apps === "object") {
        // normalize: keep only arrays of {key, value}
        cfg.perapp.apps = {};
        Object.keys(parsed.perapp.apps).forEach(function (pkg) {
          var arr = parsed.perapp.apps[pkg];
          if (!Array.isArray(arr)) return;
          cfg.perapp.apps[pkg] = arr.filter(function (kv) {
            return kv && typeof kv.key === "string";
          }).map(function (kv) {
            return { key: kv.key, value: typeof kv.value === "string" ? kv.value : "" };
          });
        });
      }
    }
  } catch (e) {
    // missing/corrupt config: keep defaults
  }
}

async function saveConfig() {
  await writeFile(MODDIR + "/config.json", JSON.stringify(cfg, null, 2) + "\n");
  // flat mirrors for the boot scripts (no jq on device)
  await writeFile(MODDIR + "/.props_enabled", cfg.props.enabled ? "1\n" : "0\n");
  await writeFile(MODDIR + "/.props_spoof",
    cfg.props.list.map(function (p) { return p.key + "=" + p.value; }).join("\n") + "\n");
  // flat mirror for the zygisk lib: "pkg|key=value" per line. Written only when
  // per-app spoof is enabled; an empty file is the kill switch (config.json
  // keeps the data so re-enabling restores everything).
  var perappLines = [];
  if (cfg.perapp.enabled) {
    Object.keys(cfg.perapp.apps).sort().forEach(function (pkg) {
      cfg.perapp.apps[pkg].forEach(function (kv) {
        if (!kv.key) return;
        var safeVal = kv.value.replace(/[|\r\n]/g, "");
        perappLines.push(pkg + "|" + kv.key + "=" + safeVal);
      });
    });
  }
  await writeFile(MODDIR + "/.perapp_props",
    perappLines.length ? perappLines.join("\n") + "\n" : "");
}

// ---------- Packages ----------

var pkgs3p = [];   // [{pkg, uid}]
var pkgsAll = [];

function parsePkgList(out) {
  var res = [];
  out.split("\n").forEach(function (line) {
    var m = line.match(/^package:(\S+)\s+uid:(\d+)/);
    if (m) res.push({ pkg: m[1], uid: m[2] });
  });
  res.sort(function (a, b) { return a.pkg < b.pkg ? -1 : 1; });
  return res;
}

async function loadPackages() {
  var r3 = await sh("pm list packages -U -3");
  pkgs3p = parsePkgList(r3);
  var ra = await sh("pm list packages -U");
  pkgsAll = parsePkgList(ra);
}

function uidOf(pkg) {
  var i;
  for (i = 0; i < pkgsAll.length; i++) if (pkgsAll[i].pkg === pkg) return pkgsAll[i].uid;
  for (i = 0; i < pkgs3p.length; i++) if (pkgs3p[i].pkg === pkg) return pkgs3p[i].uid;
  return "";
}

// ---------- SSAID tab ----------

var ssaidMap = {}; // package -> current 16-hex SSAID

async function loadSsaids() {
  var out = await sh(
    "F=" + SSAID_PATH + "; T=" + MODDIR + "/.tmp_ssaid_read.xml; " +
    'if file "$F" | grep -q ASCII; then cat "$F"; ' +
    'else abx2xml "$F" "$T" >/dev/null 2>&1 && cat "$T"; rm -f "$T"; fi'
  );
  ssaidMap = {};
  var tagRe = /<setting\b[^>]*>/g, attrRe = /(\w+)="([^"]*)"/g, m, a;
  while ((m = tagRe.exec(out)) !== null) {
    var attrs = {};
    while ((a = attrRe.exec(m[0])) !== null) attrs[a[1]] = a[2];
    if (attrs.package) ssaidMap[attrs.package] = attrs.value || "";
  }
}

function visiblePkgs(thirdOnly, query) {
  var src = thirdOnly ? pkgs3p : pkgsAll;
  query = (query || "").toLowerCase();
  return src.filter(function (p) { return !query || p.pkg.toLowerCase().indexOf(query) !== -1; });
}

function renderSsaidList() {
  var box = document.getElementById("ssaid-list");
  box.innerHTML = "";
  var third = document.getElementById("ssaid-3p").checked;
  var query = (document.getElementById("ssaid-search").value || "").toLowerCase();
  var list = visiblePkgs(third, query);
  // Enrolled apps that were uninstalled stay listed (marked "desinstalado")
  // so they can be unchecked; pm list packages no longer returns them.
  var stale = Object.keys(cfg.ssaid.apps).filter(function (pkg) {
    if (uidOf(pkg)) return false;
    return !query || pkg.toLowerCase().indexOf(query) !== -1;
  }).sort().map(function (pkg) { return { pkg: pkg, uid: "" }; });
  list = stale.concat(list);
  if (!list.length) { box.appendChild(el("p", "muted", "Nenhum pacote.")); return; }
  list.forEach(function (p) { box.appendChild(buildSsaidRow(p)); });
}

function buildSsaidRow(p) {
  var enrolled = Object.prototype.hasOwnProperty.call(cfg.ssaid.apps, p.pkg);
  var appCfg = cfg.ssaid.apps[p.pkg] || { mode: "global", id: "" };

  var item = el("div", "appitem");
  var head = el("div", "row");
  var chk = document.createElement("input");
  chk.type = "checkbox";
  chk.checked = enrolled;
  var labelWrap = el("div", "grow");
  labelWrap.appendChild(el("div", "pkg", p.pkg));
  if (uidOf(p.pkg)) {
    var cur = ssaidMap[p.pkg];
    labelWrap.appendChild(el("div", "ssaid" + (cur ? "" : " none"), cur || "sem SSAID"));
  } else {
    labelWrap.appendChild(el("div", "ssaid none", "desinstalado"));
  }
  head.appendChild(chk);
  head.appendChild(labelWrap);
  item.appendChild(head);

  var sub = el("div", "sub" + (enrolled ? "" : " hidden"));

  var modeRow = el("div", "row");
  var rGlobal = document.createElement("input");
  rGlobal.type = "radio"; rGlobal.name = "mode_" + p.pkg; rGlobal.checked = appCfg.mode !== "custom";
  var rCustom = document.createElement("input");
  rCustom.type = "radio"; rCustom.name = "mode_" + p.pkg; rCustom.checked = appCfg.mode === "custom";
  var l1 = el("label", "chk"); l1.appendChild(rGlobal); l1.appendChild(document.createTextNode("Global"));
  var l2 = el("label", "chk"); l2.appendChild(rCustom); l2.appendChild(document.createTextNode("Personalizado"));
  modeRow.appendChild(l1); modeRow.appendChild(l2);

  var customRow = el("div", "row" + (appCfg.mode === "custom" ? "" : " hidden"));
  var input = document.createElement("input");
  input.type = "text"; input.maxLength = 16; input.className = "mono grow";
  input.placeholder = "16 hex"; input.value = appCfg.id || "";
  var dice = el("button", "btn secondary small", "Regenerar");
  dice.addEventListener("click", function () {
    input.value = randHex(16);
    appCfg.id = input.value;
  });
  customRow.appendChild(input); customRow.appendChild(dice);

  input.addEventListener("input", function () { appCfg.id = input.value.trim(); });
  rGlobal.addEventListener("change", function () {
    appCfg.mode = "global"; customRow.classList.add("hidden");
  });
  rCustom.addEventListener("change", function () {
    appCfg.mode = "custom"; customRow.classList.remove("hidden");
    if (!isHex16(appCfg.id || "")) { appCfg.id = randHex(16); input.value = appCfg.id; }
  });
  chk.addEventListener("change", function () {
    if (chk.checked) {
      if (appCfg.mode === "custom" && !isHex16(appCfg.id || "")) appCfg.id = randHex(16);
      cfg.ssaid.apps[p.pkg] = appCfg;
    } else {
      delete cfg.ssaid.apps[p.pkg];
    }
    sub.classList.toggle("hidden", !chk.checked);
  });

  sub.appendChild(modeRow);
  sub.appendChild(customRow);
  item.appendChild(sub);
  return item;
}

async function applySsaid() {
  var globalId = document.getElementById("global-id").value.trim();
  // Prune enrolled apps that are no longer installed: uidOf() only resolves
  // via `pm list packages`, so a stale entry would abort every apply with
  // "UID não encontrado" — and the app is invisible in the list, so it could
  // never be unchecked either.
  var stale = [];
  Object.keys(cfg.ssaid.apps).forEach(function (p) {
    if (!uidOf(p)) { stale.push(p); delete cfg.ssaid.apps[p]; }
  });
  if (stale.length) toast("Removidos (desinstalados): " + stale.join(", "));
  var enrolled = Object.keys(cfg.ssaid.apps);
  if (!enrolled.length) {
    if (stale.length) {
      // persist the prune even with nothing left to apply
      try { await saveConfig(); renderSsaidList(); } catch (e) { /* keep toast */ }
    } else {
      toast("Nenhum app selecionado.");
    }
    return;
  }
  var needsGlobal = enrolled.some(function (p) { return cfg.ssaid.apps[p].mode !== "custom"; });
  if (needsGlobal && !isHex16(globalId)) { toast("ID Global inválido (16 hex minúsculo)."); return; }
  var i, p, c;
  for (i = 0; i < enrolled.length; i++) {
    c = cfg.ssaid.apps[enrolled[i]];
    if (c.mode === "custom" && !isHex16(c.id || "")) {
      toast("ID custom inválido para " + enrolled[i]); return;
    }
  }
  cfg.ssaid.globalId = globalId;

  var lines = [];
  for (i = 0; i < enrolled.length; i++) {
    p = enrolled[i];
    c = cfg.ssaid.apps[p];
    lines.push(p + " " + uidOf(p) + " " + (c.mode === "custom" ? c.id : globalId));
  }

  status("Aplicando SSAID...");
  try {
    await writeFile(MODDIR + "/.apply_list", lines.join("\n") + "\n");
    await writeFile(MODDIR + "/apply_ssaid.sh", APPLY_SCRIPT);
    await sh("sh " + MODDIR + "/apply_ssaid.sh");
    await saveConfig();
    await loadSsaids();
    renderSsaidList();
    status("");
    showRebootModal("SSAID aplicado para " + enrolled.length + " app(s). Reiniciar agora?");
  } catch (e) {
    status("");
    toast("Falha ao aplicar: " + e.message);
  }
}

function showRebootModal(msg) {
  document.getElementById("modal-msg").textContent = msg;
  document.getElementById("modal-wrap").hidden = false;
}

async function backupSsaid() {
  try {
    await sh("cp " + SSAID_PATH + " " + BACKUP_PATH);
    toast("Backup salvo em " + BACKUP_PATH);
  } catch (e) { toast("Falha no backup: " + e.message); }
}

async function restoreSsaid() {
  try {
    await sh(
      "cat " + BACKUP_PATH + " > " + SSAID_PATH +
      " && chown 1000:1000 " + SSAID_PATH +
      " && chmod 600 " + SSAID_PATH +
      " && restorecon " + SSAID_PATH
    );
    await loadSsaids();
    renderSsaidList();
    showRebootModal("Backup restaurado. Reiniciar agora?");
  } catch (e) { toast("Falha ao restaurar: " + e.message); }
}

// ---------- Props tab ----------

function renderProps() {
  document.getElementById("props-enabled").checked = cfg.props.enabled;
  var box = document.getElementById("props-list");
  box.innerHTML = "";
  cfg.props.list.forEach(function (p, idx) {
    var row = el("div", "row");
    row.style.marginBottom = "6px";
    var k = document.createElement("input");
    k.type = "text"; k.className = "mono grow"; k.placeholder = "chave"; k.value = p.key;
    var v = document.createElement("input");
    v.type = "text"; v.className = "mono grow"; v.placeholder = "valor"; v.value = p.value;
    var del = el("button", "btn danger small", "X");
    k.addEventListener("input", function () { p.key = k.value.trim(); });
    v.addEventListener("input", function () { p.value = v.value.trim(); });
    del.addEventListener("click", function () {
      cfg.props.list.splice(idx, 1);
      renderProps();
    });
    row.appendChild(k); row.appendChild(v); row.appendChild(del);
    box.appendChild(row);
  });
}

function collectProps() {
  cfg.props.enabled = document.getElementById("props-enabled").checked;
  cfg.props.list = cfg.props.list.filter(function (p) { return p.key; });
}

async function applyPropsNow() {
  collectProps();
  status("Aplicando props...");
  try {
    for (var i = 0; i < cfg.props.list.length; i++) {
      var p = cfg.props.list[i];
      if (!/^[A-Za-z0-9_.\-]+$/.test(p.key)) { toast("Chave inválida: " + p.key); status(""); return; }
      // single-quoted shell args: only an embedded quote is dangerous, drop it
      var safeVal = p.value.replace(/'/g, "");
      await sh("ksud resetprop '" + p.key + "' '" + safeVal + "'");
    }
    await saveConfig();
    status("");
    toast(cfg.props.list.length + " prop(s) aplicada(s).");
  } catch (e) {
    status("");
    toast("Falha ao aplicar props: " + e.message);
  }
}

async function saveProps() {
  collectProps();
  try {
    await saveConfig();
    toast("Props salvas.");
  } catch (e) { toast("Falha ao salvar: " + e.message); }
}

// ---------- Per-app props tab (zygisk) ----------

var PERAPP_KEY_RE = /^[A-Za-z0-9_.\-]+$/;

function renderPerappList() {
  document.getElementById("perapp-enabled").checked = cfg.perapp.enabled;
  var box = document.getElementById("perapp-list");
  box.innerHTML = "";
  var third = document.getElementById("perapp-3p").checked;
  var query = document.getElementById("perapp-search").value;
  var list = visiblePkgs(third, query);
  if (!list.length) { box.appendChild(el("p", "muted", "Nenhum pacote.")); return; }
  list.forEach(function (p) { box.appendChild(buildPerappRow(p)); });
}

function buildPerappRow(p) {
  var enrolled = Object.prototype.hasOwnProperty.call(cfg.perapp.apps, p.pkg);
  var appProps = enrolled ? cfg.perapp.apps[p.pkg] : [];

  var item = el("div", "appitem");
  var head = el("div", "row");
  var chk = document.createElement("input");
  chk.type = "checkbox";
  chk.checked = enrolled;
  var labelWrap = el("div", "grow");
  labelWrap.appendChild(el("div", "pkg", p.pkg));
  labelWrap.appendChild(el("div", "ssaid" + (enrolled ? "" : " none"),
    enrolled ? appProps.length + " prop(s)" : "sem spoof"));
  head.appendChild(chk);
  head.appendChild(labelWrap);
  item.appendChild(head);

  var sub = el("div", "sub" + (enrolled ? "" : " hidden"));

  appProps.forEach(function (kv, idx) {
    var row = el("div", "row");
    var k = document.createElement("input");
    k.type = "text"; k.className = "mono grow"; k.placeholder = "chave"; k.value = kv.key;
    var v = document.createElement("input");
    v.type = "text"; v.className = "mono grow"; v.placeholder = "valor"; v.value = kv.value;
    var del = el("button", "btn danger small", "X");
    k.addEventListener("input", function () { kv.key = k.value.trim(); });
    v.addEventListener("input", function () { kv.value = v.value.trim(); });
    del.addEventListener("click", function () {
      appProps.splice(idx, 1);
      renderPerappList();
    });
    row.appendChild(k); row.appendChild(v); row.appendChild(del);
    sub.appendChild(row);
  });

  var btnRow = el("div", "row");
  var add = el("button", "btn secondary small", "+ Adicionar prop");
  add.addEventListener("click", function () {
    appProps.push({ key: "", value: "" });
    renderPerappList();
  });
  var rm = el("button", "btn danger small", "Remover app");
  rm.addEventListener("click", function () {
    delete cfg.perapp.apps[p.pkg];
    renderPerappList();
  });
  btnRow.appendChild(add);
  if (enrolled) btnRow.appendChild(rm);
  sub.appendChild(btnRow);

  chk.addEventListener("change", function () {
    if (chk.checked) {
      if (!appProps.length) appProps.push({ key: "", value: "" });
      cfg.perapp.apps[p.pkg] = appProps;
    } else {
      delete cfg.perapp.apps[p.pkg];
    }
    renderPerappList();
  });

  item.appendChild(sub);
  return item;
}

async function applyPerapp() {
  cfg.perapp.enabled = document.getElementById("perapp-enabled").checked;
  // drop rows without key, validate the rest
  var pkgs = Object.keys(cfg.perapp.apps);
  var i, j;
  for (i = 0; i < pkgs.length; i++) {
    var arr = cfg.perapp.apps[pkgs[i]].filter(function (kv) { return kv.key; });
    for (j = 0; j < arr.length; j++) {
      if (!PERAPP_KEY_RE.test(arr[j].key)) {
        toast("Chave inválida (" + pkgs[i] + "): " + arr[j].key);
        return;
      }
    }
    cfg.perapp.apps[pkgs[i]] = arr;
  }

  status("Aplicando props por app...");
  try {
    await saveConfig();
    // force-stop so the spoof is picked up when each app reopens
    var stopped = 0;
    for (i = 0; i < pkgs.length; i++) {
      if (!cfg.perapp.apps[pkgs[i]].length) continue;
      var r = await exec("am force-stop " + pkgs[i]);
      if (r.errno === 0) stopped++;
    }
    status("");
    toast("Salvo. Spoof vale ao reabrir o app (" + stopped + " app(s) reiniciado(s)); não precisa reboot.");
  } catch (e) {
    status("");
    toast("Falha ao aplicar: " + e.message);
  }
}

// ---------- TrickyStore tab ----------

async function renderTricky() {
  var body = document.getElementById("tricky-body");
  body.innerHTML = "";
  var probe = await exec("if [ -f " + TS_TARGET + " ]; then echo OK; cat " + TS_TARGET + "; else echo MISSING; fi");
  var out = probe.stdout || "";
  if (out.indexOf("OK") !== 0) {
    body.appendChild(el("p", "muted", "TrickyStore não encontrado (" + TS_TARGET + " ausente)."));
    return;
  }
  body.appendChild(el("h2", null, "target.txt"));
  var listBox = el("div");
  var lines = out.split("\n").slice(1)
    .map(function (s) { return s.trim(); })
    .filter(function (s) { return s.length > 0; });
  if (!lines.length) listBox.appendChild(el("p", "muted", "Lista vazia."));
  lines.forEach(function (line) {
    var row = el("div", "row appitem");
    row.appendChild(el("span", "pkg grow", line));
    var del = el("button", "btn danger small", "Remover");
    del.addEventListener("click", async function () {
      try {
        await sh("grep -vxF '" + line.replace(/'/g, "") + "' " + TS_TARGET + " > " + TS_TARGET + ".tmp; " +
          "cat " + TS_TARGET + ".tmp > " + TS_TARGET + "; rm -f " + TS_TARGET + ".tmp");
        toast("Removido: " + line);
        renderTricky();
      } catch (e) { toast("Falha: " + e.message); }
    });
    row.appendChild(del);
    listBox.appendChild(row);
  });
  body.appendChild(listBox);
  var addRow = el("div", "row");
  addRow.style.marginTop = "10px";
  var input = document.createElement("input");
  input.type = "text"; input.className = "mono grow"; input.placeholder = "com.exemplo.app";
  var add = el("button", "btn", "Adicionar");
  add.addEventListener("click", async function () {
    var pkg = input.value.trim();
    if (!/^[A-Za-z0-9_.]+$/.test(pkg)) { toast("Pacote inválido."); return; }
    try {
      await sh("grep -qx " + pkg + " " + TS_TARGET + " 2>/dev/null || echo " + pkg + " >> " + TS_TARGET);
      toast("Adicionado: " + pkg);
      renderTricky();
    } catch (e) { toast("Falha: " + e.message); }
  });
  addRow.appendChild(input); addRow.appendChild(add);
  body.appendChild(addRow);
}

// ---------- Tabs & init ----------

function initTabs() {
  var buttons = document.querySelectorAll("nav.tabs button");
  buttons.forEach(function (b) {
    b.addEventListener("click", function () {
      buttons.forEach(function (x) { x.classList.toggle("active", x === b); });
      document.querySelectorAll("section.tabpage").forEach(function (s) {
        s.classList.toggle("active", s.id === "tab-" + b.dataset.tab);
      });
      if (b.dataset.tab === "tricky") renderTricky();
    });
  });
}

async function init() {
  document.getElementById("app").hidden = false;
  initTabs();

  document.getElementById("btn-regen-global").addEventListener("click", function () {
    document.getElementById("global-id").value = randHex(16);
  });
  document.getElementById("btn-apply-ssaid").addEventListener("click", applySsaid);
  document.getElementById("btn-backup").addEventListener("click", backupSsaid);
  document.getElementById("btn-restore").addEventListener("click", restoreSsaid);
  document.getElementById("ssaid-search").addEventListener("input", renderSsaidList);
  document.getElementById("ssaid-3p").addEventListener("change", renderSsaidList);

  document.getElementById("btn-add-prop").addEventListener("click", function () {
    collectProps();
    cfg.props.list.push({ key: "", value: "" });
    renderProps();
  });
  document.getElementById("btn-apply-props").addEventListener("click", applyPropsNow);
  document.getElementById("btn-save-props").addEventListener("click", saveProps);

  document.getElementById("perapp-search").addEventListener("input", renderPerappList);
  document.getElementById("perapp-3p").addEventListener("change", renderPerappList);
  document.getElementById("btn-apply-perapp").addEventListener("click", applyPerapp);

  document.getElementById("modal-no").addEventListener("click", function () {
    document.getElementById("modal-wrap").hidden = true;
  });
  document.getElementById("modal-yes").addEventListener("click", function () {
    exec("reboot");
  });

  await loadConfig();
  document.getElementById("global-id").value = cfg.ssaid.globalId || randHex(16);
  renderProps();

  status("Carregando pacotes e SSAIDs...");
  try {
    await loadPackages();
    await loadSsaids();
    status("");
  } catch (e) {
    status("");
    toast("Falha ao carregar: " + e.message);
  }
  renderSsaidList();
  renderPerappList();
}

if (typeof ksu === "undefined" || typeof ksu.exec !== "function") {
  document.getElementById("fatal").hidden = false;
} else {
  init();
}
