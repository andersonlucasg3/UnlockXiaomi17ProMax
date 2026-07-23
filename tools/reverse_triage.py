#!/usr/bin/env python3
"""Triage estatica dos APKs Caixa/Gabba: inventario + strings de deteccao."""
import os, re, zipfile, sys

ROOTS = {
    "superapp": r"reverse/caixa-superapp",
    "gabba": r"reverse/gabba",
}
OUT = r"reverse/report.txt"

INDICATORS = {
    "su_paths": re.compile(rb"/s?bin/(x?bin/)?su\b|/su/bin|/magisk|/debug_ramdisk|/data/adb[^\x00]{0,40}"),
    "root_pkgs": re.compile(rb"(topjohnwu|weishu\.kernelsu|kernelsu|ksunext|morphe\.manager|magisk|zygisk|riru|xposed|lsposed|frida|substrate|supersu|superuser|termux|gameguardian|revanced|morphe)[^\x00]{0,30}", re.I),
    "props": re.compile(rb"(ro\.debuggable|ro\.secure|service\.adb\.root|ro\.build\.tags|test-keys|ro\.build\.type|init\.svc\.adbd)[^\x00]{0,20}"),
    "settings": re.compile(rb"(adb_enabled|development_settings_enabled|http_proxy|always_finish_activities|animator_duration)[^\x00]{0,20}", re.I),
    "integrity": re.compile(rb"(playintegrity|safetynet|droidguard|attestation|keymaster|verifiedboot|boot_state|green|orange)[^\x00]{0,30}", re.I),
    "emulator": re.compile(rb"(goldfish|ranchu|genymotion|bluestacks|nox|vbox|qemu)[^\x00]{0,20}", re.I),
    "detector_names": re.compile(rb"(rootbeer|jail.?monkey|root.?detect|isrooted|isRooted|RootCheck|Promon|Trusteer|Appdome|Warsaw|Vkey|Incode|Unico|OzForensics|gabba)[^\x00]{0,40}"),
    "crypto_ids": re.compile(rb"(mediadrm|widevine|advertising[_ ]?id|android[_ ]?id|ssaid|device[_ ]?id)[^\x00]{0,30}", re.I),
}

def strings(data, minlen=6):
    out, cur = [], bytearray()
    for b in data:
        if 32 <= b < 127:
            cur.append(b)
        else:
            if len(cur) >= minlen:
                out.append(bytes(cur))
            cur = bytearray()
    if len(cur) >= minlen:
        out.append(bytes(cur))
    return out

def scan_file(name, data, rep):
    hits = {k: set() for k in INDICATORS}
    # scan raw bytes directly (regexes work on bytes); strings() only for context
    for key, rx in INDICATORS.items():
        for m in rx.finditer(data):
            s = m.group(0)[:120]
            try:
                hits[key].add(s.decode("utf-8", "replace"))
            except Exception:
                pass
    any_hit = False
    for key, vals in hits.items():
        if vals:
            any_hit = True
            rep.append(f"  [{key}] ({len(vals)} hits)")
            for v in sorted(vals)[:15]:
                rep.append(f"    {v}")
    return any_hit

rep = []
for label, root in ROOTS.items():
    for dirpath, _, files in os.walk(root):
        for f in sorted(files):
            if not f.endswith(".apk"):
                continue
            path = os.path.join(dirpath, f)
            rep.append(f"\n=== {label}/{f} ({os.path.getsize(path)//1024} KB) ===")
            try:
                z = zipfile.ZipFile(path)
            except Exception as e:
                rep.append(f"  ERRO zip: {e}")
                continue
            names = z.namelist()
            interesting = [n for n in names if re.search(r"\.(so|jsbundle|bundle)$|classes\d*\.dex$|assets/.*(js|json)$", n)]
            rep.append(f"  entries: {len(names)}, interessantes: {len(interesting)}")
            for n in names:
                if n.endswith(".so"):
                    info = z.getinfo(n)
                    rep.append(f"  .so: {n} ({info.file_size//1024} KB)")
            # scan: all .so + bundles + dex (dex only headers/strings scan)
            for n in interesting:
                info = z.getinfo(n)
                if info.file_size > 400 * 1024 * 1024:
                    continue
                data = z.read(n)
                kind = n.split("/")[-1]
                if scan_file(f"{f}:{n}", data, rep):
                    rep.insert(len(rep), "")  # spacing
                    rep[-2:-2] = [f"  -- HITS em {n}"]
with open(OUT, "w", encoding="utf-8") as fh:
    fh.write("\n".join(rep))
print(f"linhas: {len(rep)} -> {OUT}")
