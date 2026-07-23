#!/usr/bin/env python3
"""Extrai strings categorizadas de uma .so dentro de um APK."""
import zipfile, sys, re

apk, member, out = sys.argv[1], sys.argv[2], sys.argv[3]
z = zipfile.ZipFile(apk)
data = z.read(member)

cur, strs = bytearray(), []
for b in data:
    if 32 <= b < 127:
        cur.append(b)
    else:
        if len(cur) >= 4:
            strs.append(bytes(cur).decode("ascii", "replace"))
        cur = bytearray()
if len(cur) >= 4:
    strs.append(bytes(cur).decode("ascii", "replace"))

CATS = {
    "paths": re.compile(r"^/[a-zA-Z0-9_./{}%-]{3,}$"),
    "pkgs": re.compile(r"^(com|eu|io|me|net|org|br|de|topjohnwu|icu)\.[a-zA-Z0-9_.]{4,}$"),
    "props": re.compile(r"^(ro|sys|persist|init|service|dalvik|debug)\.[a-z0-9_.]{3,}$"),
    "detect_words": re.compile(r"(?i)(detect|root|magisk|su$|frida|xposed|riru|substrate|hook|tamper|debug|emulat|integrity|attest|mount|zygisk|kernelsu|susfs)"),
}
buckets = {k: set() for k in CATS}
buckets["other"] = set()
for s in set(strs):
    for k, rx in CATS.items():
        if rx.search(s):
            buckets[k].add(s)
            break
    else:
        if len(s) >= 8:
            buckets["other"].add(s)

with open(out, "w", encoding="utf-8") as fh:
    for k in CATS:
        fh.write(f"\n##### {k} ({len(buckets[k])})\n")
        for s in sorted(buckets[k]):
            fh.write(s + "\n")
    fh.write(f"\n##### other ({len(buckets['other'])})\n")
    for s in sorted(buckets["other"]):
        fh.write(s + "\n")
print(f"{member}: {len(strs)} strings -> {out}")
