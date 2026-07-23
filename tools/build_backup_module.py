import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\adb-backup.zip"

MODULE_PROP = """id=adb-backup
name=ADB State Backup
version=v1.0
versionCode=1
author=local
description=Descartavel: copia estado de /data/adb para /data/local/tmp/qwen_adb_backup.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
OUT=/data/local/tmp/qwen_adb_backup
rm -rf "$OUT"
mkdir -p "$OUT"
{
  echo "=== backup tricky_store ==="
  cp -a /data/adb/tricky_store "$OUT/tricky_store" 2>&1
  echo "=== backup zygisksu ==="
  cp -a /data/adb/zygisksu "$OUT/zygisksu" 2>&1
  echo "=== backup service.d ==="
  mkdir -p "$OUT/service.d"
  cp -a /data/adb/service.d/* "$OUT/service.d/" 2>&1
  echo "=== denylist atual ==="
  magisk --denylist ls > "$OUT/denylist.txt" 2>&1
  echo "=== resultado ==="
  ls -laR "$OUT"
} 2>&1
"""

KEEP = (
    "META-INF/com/google/android/update-binary",
    "META-INF/com/google/android/updater-script",
)

with zipfile.ZipFile(SRC) as zin:
    payload = {name: zin.read(name) for name in KEEP}

with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in payload.items():
        zout.writestr(name, data)
    zout.writestr("module.prop", MODULE_PROP)
    zout.writestr("customize.sh", CUSTOMIZE_SH)

print(f"OK: {OUT} ({os.path.getsize(OUT)} bytes)")
