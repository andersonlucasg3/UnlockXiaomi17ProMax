# -*- coding: utf-8 -*-
"""
Extrai cadeias de certificados X.509 (DER) embutidas em imagens Qualcomm
Secure Boot (abl.elf) e decodifica subject/issuer para verificar
proveniência (Xiaomi/Qualcomm vs. assinatura desconhecida).
Read-only: apenas lê bytes e converte DER->PEM para decode via ssl stdlib.
"""
import os
import re
import ssl
import struct
import tempfile

ABL = r"C:\Users\anderson\Projetos\UnlockXiaomi\quarantine\extracted\v2.0.0\unlockFolder\factoryABL\popsicle_abl.elf"


def find_der_certs(data):
    """Procura sequências DER: 30 82 <len_hi> <len_lo>."""
    certs = []
    i = 0
    while True:
        i = data.find(b"\x30\x82", i)
        if i < 0 or i + 4 > len(data):
            break
        declared = struct.unpack(">H", data[i + 2:i + 4])[0]
        end = i + 4 + declared
        if 200 < declared < 4096 and end <= len(data):
            blob = data[i:end]
            # heurística: cert X.509 v3 tem 02 01 02 (version 3) logo após o SEQ inicial
            inner = data.find(b"\xa0\x03\x02\x01\x02", i, min(end, i + 32))
            if inner > 0:
                certs.append(blob)
                i = end
                continue
        i += 2
    return certs


def main():
    data = open(ABL, "rb").read()
    print(f"arquivo: {os.path.basename(ABL)} ({len(data):,} bytes)")
    certs = find_der_certs(data)
    print(f"certificados DER candidatos: {len(certs)}")
    for idx, der in enumerate(certs):
        pem = (
            "-----BEGIN CERTIFICATE-----\n"
            + "\n".join(
                re.findall(".{1,64}", __import__("base64").b64encode(der).decode())
            )
            + "\n-----END CERTIFICATE-----\n"
        )
        tmp = tempfile.NamedTemporaryFile(
            "w", suffix=".pem", delete=False, encoding="ascii"
        )
        try:
            tmp.write(pem)
            tmp.close()
            info = ssl._ssl._test_decode_cert(tmp.name)
            print(f"\n--- cert[{idx}] ---")
            for campo in ("subject", "issuer", "notBefore", "notAfter", "serialNumber"):
                if campo in info:
                    print(f"  {campo}: {info[campo]}")
        except Exception as e:
            print(f"\n--- cert[{idx}] --- FALHOU decode: {e}")
        finally:
            os.unlink(tmp.name)


if __name__ == "__main__":
    main()
