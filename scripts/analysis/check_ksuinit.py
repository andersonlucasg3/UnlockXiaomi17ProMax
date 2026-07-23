"""Verifica header ELF do ksuinit (arquitetura, tipo)."""
PATH = r"C:\Users\anderson\Projetos\UnlockXiaomi\tools\ksuinit"

MACHINES = {0x28: "ARM (32)", 0x3E: "x86-64", 0xB7: "AArch64 (arm64)", 0xF3: "RISC-V"}

with open(PATH, "rb") as f:
    data = f.read(64)

assert data[:4] == b"\x7fELF", "nao eh ELF"
ei_class = data[4]
ei_data = data[5]
e_type = int.from_bytes(data[16:18], "little")
e_machine = int.from_bytes(data[18:20], "little")
print(f"classe: {'64-bit' if ei_class == 2 else '32-bit'}")
print(f"endianness: {'LE' if ei_data == 1 else 'BE'}")
print(f"tipo: {e_type} (2=EXEC, 3=DYN/PIE)")
print(f"maquina: {MACHINES.get(e_machine, hex(e_machine))}")
