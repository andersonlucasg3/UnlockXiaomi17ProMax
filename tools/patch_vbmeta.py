"""Seta flags=3 (VERIFICATION_DISABLED | VERITY_DISABLED) no vbmeta.img (offset 56, u32 BE)."""
import struct

SRC = r"C:\Users\anderson\Projetos\UnlockXiaomi\rom\popsicle_eu\images\vbmeta.img"
OUT = r"C:\Users\anderson\Projetos\UnlockXiaomi\rom\popsicle_eu\images\vbmeta-disabled.img"

with open(SRC, "rb") as f:
    data = bytearray(f.read())

assert data[:4] == b"AVB0", "magic AVB0 ausente"
old = struct.unpack(">I", data[56:60])[0]
print(f"flags atual: {old}")
struct.pack_into(">I", data, 56, 3)
new = struct.unpack(">I", data[56:60])[0]
print(f"flags novo:  {new}")

with open(OUT, "wb") as f:
    f.write(data)
print(f"OK: {OUT} ({len(data)} bytes)")
