# -*- coding: utf-8 -*-
"""Identifica o formato de um arquivo baixado e, se for zip, lista o conteúdo."""
import sys
import zipfile

path = sys.argv[1]
data = open(path, "rb").read()
print(f"tamanho: {len(data)} bytes")
print(f"magic bytes (hex): {data[:16].hex(' ')}")
print(f"magic bytes (repr): {data[:16]!r}")

if zipfile.is_zipfile(path):
    print("=> ZIP VALIDO")
    with zipfile.ZipFile(path) as z:
        for n in z.namelist():
            print(f"   {n} ({z.getinfo(n).file_size} bytes)")
else:
    print("=> NAO é zip (zipfile)")
    # procura assinatura PK em qualquer offset (zip com prefixo)
    idx = data.find(b"PK\x03\x04")
    print(f"   assinatura PK encontrada no offset: {idx}")
