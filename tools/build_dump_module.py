import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\shamiko-dump.zip"

MODULE_PROP = """id=shamiko-dump
name=Shamiko Status Dump
version=v1.0
versionCode=1
author=local
description=Descartavel: despeja estado do Shamiko/ZygiskNext na instalacao.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  echo "=== live module.prop (zygisk_shamiko) ==="
  cat /data/adb/modules/zygisk_shamiko/module.prop 2>&1
  echo
  echo "=== /data/adb/shamiko/.tmp/status ==="
  cat /data/adb/shamiko/.tmp/status 2>&1
  echo
  echo "=== /data/adb/shamiko/ (recursivo) ==="
  ls -laR /data/adb/shamiko/ 2>&1
  echo
  echo "=== /data/adb/zygisksu/ ==="
  ls -la /data/adb/zygisksu/ 2>&1
  echo
  echo "=== shamiko .so em gms.unstable ==="
  grep -i shamiko /proc/$(pidof com.google.android.gms.unstable)/maps 2>&1 | head -5
  echo "=== fim ==="
} | tee /data/local/tmp/shamiko_dump.txt
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
