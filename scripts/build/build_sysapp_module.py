"""build_sysapp_module.py — gera modulo KSU/Magisk de substituicao de apps de sistema.

Entrada: pares (arquivo APK no PC, package name). Para cada um, consulta o path
do pacote no aparelho (pm path via adb) e monta UM zip com a arvore magic-mount
(system/, product/, vendor/, system_ext/ conforme o path real).

Uso: editar APPS abaixo e rodar `python -u build_sysapp_module.py`.
"""
import datetime
import os
import subprocess
import sys
import zipfile

ADB = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\platform-tools\adb.exe"
DONOR_ZIP = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\TrickyStore-v1.4.1.zip"
OUT_DIR = r"C:\Users\anderson\Projetos\UnlockXiaomi\updates"

# (apk no PC, package name). None = pular sem erro
APPS = [
    (r"C:\Users\anderson\Projetos\UnlockXiaomi\updates\SecurityCenter-update.apk", "com.miui.securitycenter"),
]

MODULE_ID = "sysapps-update"
MODULE_NAME = "System Apps Update (magic-mount)"
AUTHOR = "sysapp-updater (local)"

META = (
    "META-INF/com/google/android/update-binary",
    "META-INF/com/google/android/updater-script",
)

PARTITION_DIRS = ("system", "product", "vendor", "system_ext", "odm")


def pm_path(pkg):
    """Retorna o path absoluto do apk base no aparelho, ou None."""
    out = subprocess.run(
        [ADB, "shell", "pm", "path", pkg],
        capture_output=True, text=True, timeout=60,
    ).stdout
    for line in out.splitlines():
        line = line.strip()
        if line.startswith("package:"):
            return line[len("package:"):]
    return None


def module_relpath(device_path):
    """Converte /product/priv-app/X/Y.apk -> product/priv-app/X/Y.apk (valida particao)."""
    parts = device_path.lstrip("/").split("/")
    if parts[0] not in PARTITION_DIRS:
        raise ValueError(f"particao inesperada: {parts[0]} em {device_path}")
    return "/".join(parts)


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    entries = []  # (apk_local, rel_no_modulo, pkg)
    for apk, pkg in APPS:
        if not os.path.isfile(apk):
            print(f"[ERRO] APK nao encontrado: {apk}")
            sys.exit(1)
        dev = pm_path(pkg)
        if not dev:
            print(f"[PULA] {pkg} nao instalado no aparelho (pm path vazio)")
            continue
        rel = module_relpath(dev)
        entries.append((apk, rel, pkg))
        print(f"[OK] {pkg:35s} {dev} -> {rel}")

    if not entries:
        print("Nada a empacotar.")
        sys.exit(1)

    stamp = datetime.date.today().isoformat()
    out_zip = os.path.join(OUT_DIR, f"sysapps-{stamp}.zip")
    module_prop = (
        f"id={MODULE_ID}\nname={MODULE_NAME}\nversion={stamp}\n"
        f"versionCode=1\nauthor={AUTHOR}\n"
        f"description=Substitui {len(entries)} app(s) de sistema via magic-mount: "
        + ", ".join(p for _, _, p in entries)
        + "\n"
    )

    with zipfile.ZipFile(DONOR_ZIP) as zin:
        meta = {n: zin.read(n) for n in META}

    with zipfile.ZipFile(out_zip, "w", zipfile.ZIP_DEFLATED) as z:
        for name, data in meta.items():
            z.writestr(name, data)
        z.writestr("module.prop", module_prop)
        for apk, rel, _ in entries:
            z.write(apk, rel)

    print(f"\nMODULO GERADO: {out_zip} ({os.path.getsize(out_zip)} bytes, {len(entries)} apps)")


if __name__ == "__main__":
    main()
