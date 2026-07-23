"""Compara os certificados de assinatura de dois APKs (META-INF/*.RSA)."""
import hashlib
import zipfile

FILES = {
    "INSTALADO (EU)": r"C:\Users\anderson\Projetos\UnlockXiaomi\updates\SecurityCenter-instalado.apk",
    "UPDATE (Xiaomi)": r"C:\Users\anderson\Projetos\UnlockXiaomi\updates\SecurityCenter-update.apk",
}

STR_MARKERS = (b"Xiaomi", b"Android", b"Beijing", b"xiaomi.eu", b"EU", b"test", b"MIUI")


def rsa_entries(zf):
    return [n for n in zf.namelist()
            if n.startswith("META-INF/") and n.upper().endswith((".RSA", ".DSA", ".EC"))]


def visible_strings(blob, minlen=5):
    out, cur = [], bytearray()
    for b in blob:
        if 32 <= b < 127:
            cur.append(b)
        else:
            if len(cur) >= minlen:
                out.append(cur.decode("ascii", "replace"))
            cur = bytearray()
    if len(cur) >= minlen:
        out.append(cur.decode("ascii", "replace"))
    return out


for label, path in FILES.items():
    print(f"\n=== {label} ===")
    print(f"arquivo: {path.split(chr(92))[-1]}")
    with zipfile.ZipFile(path) as z:
        names = rsa_entries(z)
        print(f"blocos de assinatura: {names}")
        for n in names:
            data = z.read(n)
            print(f"  {n}: {len(data)} bytes, sha256={hashlib.sha256(data).hexdigest()[:24]}…")
            strs = visible_strings(data)
            hits = sorted({s for s in strs for m in STR_MARKERS if m.decode().lower() in s.lower()})
            cert_like = [s for s in strs if any(k in s for k in ("CN=", "OU=", "O=", "C="))][:4]
            print(f"  strings nome: {hits[:6]}")
            if cert_like:
                print(f"  DN: {cert_like}")
