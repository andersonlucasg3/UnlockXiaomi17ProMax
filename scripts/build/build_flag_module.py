import os
import zipfile

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\Shamiko-v1.2.5-414.zip"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\shamiko-whitelist-flag.zip"

MODULE_PROP = """id=shamiko-whitelist-flag
name=Shamiko Whitelist Flag
version=v1.0
versionCode=1
author=local
description=Cria /data/adb/shamiko/whitelist (modo whitelist do Shamiko). Inerte em runtime.
"""

CUSTOMIZE_SH = """SKIPUNZIP=1
ui_print "- Criando flag de whitelist do Shamiko"
mkdir -p /data/adb/shamiko
touch /data/adb/shamiko/whitelist
ls -la /data/adb/shamiko/
ui_print "- Flag criada"
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

with zipfile.ZipFile(OUT) as z:
    for info in z.infolist():
        print(f"{info.filename}  ({info.file_size} bytes)")

print(f"OK: {OUT} ({os.path.getsize(OUT)} bytes)")
