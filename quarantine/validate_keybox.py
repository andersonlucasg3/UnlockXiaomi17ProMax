# -*- coding: utf-8 -*-
"""Valida estrutura e cadeia de certificados de um keybox.xml (TrickyStore)."""
import re
import ssl
import sys
import tempfile
import os
import xml.etree.ElementTree as ET

path = sys.argv[1]
text = open(path, "r", encoding="utf-8", errors="replace").read()
print(f"arquivo: {os.path.basename(path)} ({len(text)} chars)")

try:
    root = ET.fromstring(text)
    kbs = root.findall(".//Keybox")
    print(f"NumberOfKeyboxes: {len(kbs)}")
    for i, kb in enumerate(kbs):
        for key in kb.findall("Key"):
            print(f"  keybox[{i}] algoritmo: {key.get('algorithm')}")
except Exception as e:
    print(f"XML parse erro: {e}")

pems = re.findall(r"-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----", text, re.S)
print(f"certificados PEM: {len(pems)}")
for idx, pem in enumerate(pems):
    tmp = tempfile.NamedTemporaryFile("w", suffix=".pem", delete=False, encoding="ascii")
    try:
        tmp.write(pem)
        tmp.close()
        info = ssl._ssl._test_decode_cert(tmp.name)
        subj = dict(x[0] for x in info.get("subject", []))
        iss = dict(x[0] for x in info.get("issuer", []))
        print(f"--- cert[{idx}] ---")
        print(f"  subject:  {subj.get('commonName','?')} | {subj.get('organizationName','')}")
        print(f"  issuer:   {iss.get('commonName','?')} | {iss.get('organizationName','')}")
        print(f"  validade: {info.get('notBefore','?')} -> {info.get('notAfter','?')}")
    except Exception as e:
        print(f"--- cert[{idx}] --- decode falhou: {e}")
    finally:
        os.unlink(tmp.name)
