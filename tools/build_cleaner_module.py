import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\adb-cleaner.zip"

MODULE_PROP = """id=adb-cleaner
name=ADB Residual Cleaner
version=v1.0
versionCode=1
author=local
description=Descartavel: wipe de dirs residuais em /data/adb apos remocao de modulos.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  rm -rf /data/adb/zygisksu
  rm -rf /data/adb/shamiko
  rm -rf /data/adb/tricky_store
  rm -f /data/adb/service.d/propspoof.sh
  rm -f /data/adb/service.d/.zn_cleanup.sh
  rm -f /data/adb/post-fs-data.d/.shamiko_cleanup.sh
  echo "=== wipe feito ==="
  ls -la /data/adb/
  echo "=== service.d ==="
  ls -la /data/adb/service.d/
  echo "=== post-fs-data.d ==="
  ls -la /data/adb/post-fs-data.d/
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
