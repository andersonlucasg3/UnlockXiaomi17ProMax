"""Inspeciona cabecalho do vbmeta.img da ROM."""
PATH = r"C:\Users\anderson\Projetos\UnlockXiaomi\rom\popsicle_eu\images\vbmeta.img"

with open(PATH, "rb") as f:
    data = f.read()

print(f"Tamanho: {len(data)} bytes")
print(f"Primeiros 64 bytes (hex): {data[:64].hex()}")
idx = data.find(b"AVB0")
print(f"'AVB0' encontrado no offset: {idx}")
zeros = data.count(0)
print(f"Bytes zero: {zeros}/{len(data)} ({100.0 * zeros / len(data):.1f}%)")
