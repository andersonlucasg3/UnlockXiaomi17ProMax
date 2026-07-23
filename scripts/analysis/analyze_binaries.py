# -*- coding: utf-8 -*-
"""
Análise estática (read-only) dos binários da ferramenta Mi8E5-Unlocker.
Extrai strings imprimíveis, identifica formato (ELF/PE), e procura
indicadores: rede (URLs/IPs), comandos privilegiados, persistência,
e certificados embutidos no ABL.
NENHUM arquivo é executado — apenas leitura de bytes.
"""
import os
import re
import struct
import sys

BASE = r"C:\Users\anderson\Projetos\UnlockXiaomi\quarantine\extracted\v2.0.0"
TARGETS = [
    "preload-8e5-Ennea.so",
    "linuxloader_unlock.efi",
    "misc_wipedata_mi.img",
    os.path.join("unlockFolder", "factoryABL", "popsicle_abl.elf"),
]

INDICATORS = {
    "rede_url": re.compile(rb"https?://[^\s\x00\"']+", re.I),
    "rede_ip": re.compile(rb"\b(?:\d{1,3}\.){3}\d{1,3}\b"),
    "socket": re.compile(rb"\b(socket|connect|sendto|recvfrom|getaddrinfo|inet_pton)\b"),
    "su_root": re.compile(rb"(/system/bin/su|/su|su -c|setuid|setgid|capset|prctl)"),
    "selinux": re.compile(rb"(setenforce|selinux|sepolicy|permissive)", re.I),
    "particoes": re.compile(rb"/dev/block[^\s\x00\"']*"),
    "persistencia_android": re.compile(rb"(init\.rc|/system/etc/init|magisk|kernelsu|apatch)", re.I),
    "credenciais": re.compile(rb"(password|passwd|token|secret|apikey|api_key)", re.I),
    "shell_cmd": re.compile(rb"(/system/bin/sh|/bin/sh|toybox|busybox)"),
    "certs": re.compile(rb"(BEGIN CERTIFICATE|X509|MIIB|MIIC)"),
    "exfil": re.compile(rb"(curl|wget|upload|exfil|telegram|t\.me|webhook)", re.I),
}

MIN_STR = 6


def extract_strings(data, min_len=MIN_STR):
    out = []
    cur = bytearray()
    for b in data:
        if 32 <= b < 127:
            cur.append(b)
        else:
            if len(cur) >= min_len:
                out.append(bytes(cur))
            cur = bytearray()
    if len(cur) >= min_len:
        out.append(bytes(cur))
    return out


def elf_info(data):
    if data[:4] != b"\x7fELF":
        return None
    ei_class = {1: "32-bit", 2: "64-bit"}.get(data[4], "?")
    endian = {1: "<", 2: ">"}.get(data[5], "<")
    e_machine = struct.unpack(endian + "H", data[18:20])[0]
    machines = {0x28: "ARM", 0x3E: "x86-64", 0xB7: "AArch64"}
    e_type = struct.unpack(endian + "H", data[16:18])[0]
    types = {1: "reloc", 2: "exec", 3: "shared (ET_DYN)", 4: "core"}
    return f"ELF {ei_class} {machines.get(e_machine, hex(e_machine))} {types.get(e_type, e_type)}"


def pe_info(data):
    if data[:2] != b"MZ":
        return None
    pe_off = struct.unpack("<I", data[0x3C:0x40])[0]
    if data[pe_off:pe_off + 4] != b"PE\x00\x00":
        return None
    machine = struct.unpack("<H", data[pe_off + 4:pe_off + 6])[0]
    machines = {0x14C: "x86", 0x8664: "x86-64", 0xAA64: "AArch64 (EFI ARM64)"}
    return f"PE/COFF {machines.get(machine, hex(machine))}"


def main():
    for rel in TARGETS:
        path = os.path.join(BASE, rel)
        print("=" * 78)
        print(f"ARQUIVO: {rel}")
        if not os.path.exists(path):
            print("  [!!] NAO ENCONTRADO")
            continue
        data = open(path, "rb").read()
        print(f"  tamanho: {len(data):,} bytes")
        fmt = elf_info(data) or pe_info(data) or "formato desconhecido (raw)"
        print(f"  formato: {fmt}")

        strings = extract_strings(data)
        print(f"  strings extraidas (>= {MIN_STR} chars): {len(strings)}")

        blob = b"\n".join(strings)
        for nome, rx in INDICATORS.items():
            hits = sorted(set(m.group(0) for m in rx.finditer(blob)))
            if hits:
                print(f"  [{nome}] {len(hits)} ocorrencia(s):")
                for h in hits[:15]:
                    try:
                        print(f"      {h.decode('ascii', 'replace')[:120]}")
                    except Exception:
                        print(f"      {h!r}")

        # Amostra de strings "interessantes" (caminhos, nomes de funcao)
        interessantes = [s for s in strings if b"/" in s or b"_" in s][:30]
        print("  amostra de strings:")
        for s in interessantes:
            print(f"      {s.decode('ascii', 'replace')[:120]}")
    print("=" * 78)
    print("FIM DA ANALISE")


if __name__ == "__main__":
    sys.exit(main())
