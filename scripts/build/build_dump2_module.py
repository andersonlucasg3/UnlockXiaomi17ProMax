import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\shamiko-dump2.zip"

MODULE_PROP = """id=shamiko-dump2
name=Shamiko Dump 2
version=v1.0
versionCode=1
author=local
description=Descartavel: verifica disable flag e registry do ZygiskNext.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  echo "=== module dir zygisk_shamiko ==="
  ls -la /data/adb/modules/zygisk_shamiko/ 2>&1
  echo
  echo "=== disable flag? ==="
  ls -la /data/adb/modules/zygisk_shamiko/disable 2>&1
  echo
  echo "=== zygisk libs ==="
  ls -la /data/adb/modules/zygisk_shamiko/zygisk/ 2>&1
  echo
  echo "=== zygisksu modules_info ==="
  cat /data/adb/zygisksu/modules_info 2>&1
  echo
  echo "=== zygisksu znctx ==="
  cat /data/adb/zygisksu/znctx 2>&1
  echo
  echo "=== zygisksu .magic ==="
  cat /data/adb/zygisksu/.magic 2>&1
  echo
  echo "=== fim ==="
} | tee /data/local/tmp/shamiko_dump2.txt
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
