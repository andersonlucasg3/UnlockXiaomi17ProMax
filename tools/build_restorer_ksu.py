import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\TrickyStore-v1.4.1.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\tricky-restore-ksu.zip"

MODULE_PROP = """id=tricky-restore-ksu
name=TrickyStore Config Restore (KSU)
version=v1.0
versionCode=1
author=local
description=Restaura keybox.xml e target.txt do TrickyStore (descartavel).
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  mkdir -p /data/adb/tricky_store
  cp /data/local/tmp/restore/keybox.xml /data/adb/tricky_store/keybox.xml
  cp /data/local/tmp/restore/target.txt /data/adb/tricky_store/target.txt
  chown root:root /data/adb/tricky_store/keybox.xml /data/adb/tricky_store/target.txt
  chmod 644 /data/adb/tricky_store/keybox.xml
  chmod 600 /data/adb/tricky_store/target.txt
  echo "=== restaurado ==="
  ls -la /data/adb/tricky_store/
} 2>&1
"""

KEEP = (
    "META-INF/com/google/android/update-binary",
    "META-INF/com/google/android/updater-script",
)

with zipfile.ZipFile(SRC) as zin:
    names = set(zin.namelist())
    for name in KEEP:
        if name not in names:
            raise SystemExit(f"ERRO: {name} ausente no zip de origem")
        payload = {name: zin.read(name) for name in KEEP}

with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as zout:
    for name, data in payload.items():
        zout.writestr(name, data)
    zout.writestr("module.prop", MODULE_PROP)
    zout.writestr("customize.sh", CUSTOMIZE_SH)

print(f"OK: {OUT} ({os.path.getsize(OUT)} bytes)")
