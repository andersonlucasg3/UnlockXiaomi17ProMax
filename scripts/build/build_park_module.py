import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\module-park.zip"

MODULE_PROP = """id=module-park
name=Module Park (pre-KSU boot)
version=v1.0
versionCode=1
author=local
description=Descartavel: move /data/adb/modules para modules_bak antes do boot KSU.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  if [ -d /data/adb/modules ] && [ ! -d /data/adb/modules_bak ]; then
    mv /data/adb/modules /data/adb/modules_bak
    mkdir -p /data/adb/modules
    echo "OK: modules -> modules_bak"
  else
    echo "ESTADO INESPERADO - nada feito"
  fi
  echo "=== /data/adb ==="
  ls /data/adb/
  echo "=== modules_bak ==="
  ls /data/adb/modules_bak/ 2>&1
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
