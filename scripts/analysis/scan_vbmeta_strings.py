"""Strings-scan do vbmeta: quais particoes aparecem nos descriptors."""
PATH = r"C:\Users\anderson\Projetos\UnlockXiaomi\rom\popsicle_eu\images\vbmeta.img"

NAMES = [b"init_boot", b"boot", b"system", b"vendor", b"dtbo", b"recovery",
         b"vbmeta", b"system_ext", b"product", b"odm", b"vendor_boot", b"dlkm"]

with open(PATH, "rb") as f:
    data = f.read()

for name in NAMES:
    idx = data.find(name + b"\x00")
    print(f"{name.decode():15s} -> {'ENCONTRADO @' + str(idx) if idx >= 0 else 'ausente'}")
