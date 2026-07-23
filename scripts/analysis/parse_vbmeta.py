"""Parse completo de vbmeta (AVB 1.0): header, flags e descriptors."""
import struct

PATH = r"C:\Users\anderson\Projetos\UnlockXiaomi\rom\popsicle_eu\images\vbmeta.img"

TAGS = {0: "property", 1: "hashtree", 2: "hash", 3: "chain_partition", 4: "kernel_cmdline"}

with open(PATH, "rb") as f:
    data = f.read()

assert data[:4] == b"AVB0"
(major, minor, auth_sz, aux_sz, algo, hash_off, hash_sz) = struct.unpack(">QQQQQIQ".replace("QI", "Q"), b"") if False else (None,)*7
major, minor = struct.unpack(">QQ", data[4:20])
auth_sz, aux_sz = struct.unpack(">QQ", data[20:36])
(algo,) = struct.unpack(">I", data[36:40])
hash_off, hash_sz = struct.unpack(">QQ", data[40:56])
(flags,) = struct.unpack(">I", data[56:60])
print(f"libavb {major}.{minor} auth={auth_sz}B aux={aux_sz}B algo={algo} hash_off={hash_off} hash_sz={hash_sz} flags={flags}")

# Header tem 256 bytes; auth block em 256, aux logo apos
aux_start = 256 + auth_sz
aux_end = aux_start + aux_sz
print(f"aux block: {aux_start}..{aux_end}")
pos = aux_start
n = 0
while pos + 16 <= aux_end:
    tag, size = struct.unpack(">QQ", data[pos:pos + 16])
    payload = data[pos + 16:pos + 16 + size]
    name = TAGS.get(tag, f"?{tag}")
    print(f"  [{n}] tag={tag} ({name}) size={size}")
    if tag == 3:
        (rib_loc,) = struct.unpack(">I", payload[:4])
        pname = payload[4:].split(b"\x00")[0].decode()
        print(f"      chain: partition='{pname}' rollback_index_location={rib_loc} pubkey={len(payload) - 4 - len(pname) - 1}B")
    elif tag == 2:
        (rib_loc,) = struct.unpack(">I", payload[:4])
        plen = struct.unpack(">I", payload[4:8])[0]
        pname = payload[8:8 + plen].decode()
        print(f"      hash: partition='{pname}' rollback_index_location={rib_loc}")
    elif tag == 1:
        (dmver, alg_len) = struct.unpack(">II", payload[:8])
        alg = payload[8:8 + alg_len].decode(errors="replace")
        pname_len = struct.unpack(">I", payload[8 + alg_len:12 + alg_len])[0]
        pname = payload[12 + alg_len:12 + alg_len + pname_len].decode()
        print(f"      hashtree: partition='{pname}' alg={alg} dm_verity={dmver}")
    elif tag == 0:
        klen, vlen = struct.unpack(">II", payload[:8])
        key = payload[8:8 + klen].decode(errors="replace")
        val = payload[8 + klen:8 + klen + vlen].decode(errors="replace")
        print(f"      prop: {key}={val}")
    pos += 16 + size
    n += 1
print(f"total descriptors: {n}")
