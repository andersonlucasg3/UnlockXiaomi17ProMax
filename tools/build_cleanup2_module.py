import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\final-cleanup.zip"

MODULE_PROP = """id=final-cleanup
name=Final Cleanup (Shamiko removal)
version=v1.0
versionCode=1
author=local
description=Descartavel: remove Shamiko, artefatos e modulos utilitarios; habilita mount-hiding do ZN.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
{
  rm -rf /data/adb/modules/zygisk_shamiko
  rm -rf /data/adb/shamiko
  rm -f /data/adb/post-fs-data.d/.shamiko_cleanup.sh
  rm -f /data/adb/zygisksu/no_mount_znctl
  rm -rf /data/adb/modules/shamiko-whitelist-flag
  rm -rf /data/adb/modules/adb-backup
  rm -rf /data/adb/modules/adb-cleaner
  rm -rf /data/adb/modules/shamiko-dump
  rm -rf /data/adb/modules/shamiko-dump2
  rm -rf /data/adb/modules/tricky-restore
  rm -rf /data/adb/modules/zygisk-off
  echo "=== modules restantes ==="
  ls /data/adb/modules/
  echo "=== zygisksu dir ==="
  ls -la /data/adb/zygisksu/
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
