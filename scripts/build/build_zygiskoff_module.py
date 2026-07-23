import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\zygisk-off.zip"

MODULE_PROP = """id=zygisk-off
name=Magisk Zygisk Off
version=v1.0
versionCode=1
author=local
description=Descartavel: desliga o Zygisk built-in do Magisk (ZN assume).
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  magisk --sqlite "UPDATE settings SET value=0 WHERE key='zygisk';"
  echo "=== settings apos update ==="
  magisk --sqlite "SELECT key, value FROM settings;"
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
