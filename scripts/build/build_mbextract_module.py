import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\mb-extract.zip"

MODULE_PROP = """id=mb-extract
name=Magiskboot Extract
version=v1.0
versionCode=1
author=local
description=Descartavel: copia magiskboot oficial do /data/adb/magisk para /data/local/tmp.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  cp /data/adb/magisk/magiskboot /data/local/tmp/magiskboot_oficial 2>&1
  chmod 755 /data/local/tmp/magiskboot_oficial 2>&1
  ls -la /data/local/tmp/magiskboot_oficial 2>&1
  /data/local/tmp/magiskboot_oficial 2>&1 | head -3
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
