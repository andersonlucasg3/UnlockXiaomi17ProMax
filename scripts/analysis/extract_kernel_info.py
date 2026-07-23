"""Extrai versao e marcadores KSU/SuSFS de um kernel Image (raw ou comprimido)."""
import gzip
import lzma
import sys
import zlib

PATH = r"C:\Users\anderson\Projetos\UnlockXiaomi\backup\ksu-migration\anykernel\Image"

with open(PATH, "rb") as f:
    data = f.read()

print(f"Tamanho: {len(data)} bytes")
print(f"Magic 16B: {data[:16].hex()}")


def scan(blob, label):
    idx = blob.find(b"Linux version")
    if idx >= 0:
        end = blob.find(b"\x00", idx)
        ver = blob[idx:end if end > idx else idx + 200].decode("ascii", "replace")
        print(f"[{label}] {ver}")
    else:
        print(f"[{label}] string 'Linux version' NAO encontrada")
    markers = (b"KernelSU", b"kernelsu", b"ksud", b"SuSFS", b"susfs", b"ksu_su", b"KSU")
    hits = {m.decode(): blob.count(m) for m in markers}
    print(f"[{label}] marcadores: {hits}")
    return idx >= 0


if scan(data, "raw"):
    sys.exit(0)

for name, func in (
    ("gzip", lambda b: gzip.decompress(b)),
    ("zlib", lambda b: zlib.decompress(b)),
    ("lzma", lambda b: lzma.decompress(b)),
):
    try:
        out = func(data)
    except Exception:
        continue
    print(f"--- descomprimido via {name}: {len(out)} bytes ---")
    if scan(out, name):
        sys.exit(0)

print("Nao foi possivel extrair (compressao nao suportada pelo script: lz4/zstd?)")
