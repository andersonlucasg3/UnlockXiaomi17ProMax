# UnlockXiaomi — Xiaomi 17 Pro Max (popsicle)

Unlocking, xiaomi.eu ROM and root (KernelSU LKM) project for the **Xiaomi 17 Pro Max** (codename `popsicle`, Snapdragon 8 Elite Gen 5, HyperOS 3 / Android 16).

📖 **[HOW TO UNLOCK — step-by-step guide →](HOW_TO_UNLOCK.md)** (bootloader unlock → xiaomi.eu → KernelSU LKM → hiding stack)

## Current status (28/Jul/2026)

- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM** (pt-BR + GMS), bootloader unlocked
- **Root:** KernelSU **LKM** (driver backslashxx 32558) on `init_boot` — kernel **stock, untouched**
- **Stack:** ZygiskNext 1.4.3 (enforce) + PlayIntegrityFork v17 + TrickyStore v1.4.1 (keybox DroidWin) + umount global (except gms/vending/Termux)
- **Result:** Play Integrity 3/3, Google Wallet ✅, BYD ✅, Caixa ✅, **Petal Maps 4.7.0.319 ✅** (spoof prop_area COW), Revolut ✅ (HMA-OSS), Bradesco Seguros ✅ (HMA-OSS), **YT Music Morphe ✅ Android Auto (podcasts; music requires Premium server-side)** — see `docs/SESSION-HANDOVER.md`
- **Custom module:** DeviceID+ **v2.3.1** installed (`modules/deviceidchanger/`, AGPL fork of sidex15) — per-app SSAID, global and per-app prop spoofing (COW prop_area + GOT hooks + **`android.os.Build.*` via JNI**), native DCK hook (passive, for BYD digital key), TrickyStore editor
- **BYD digital key:** 🔄 **REOPENED 28/Jul** — DeviceID+ v2.3.1 native hook deployed, validation pending; full mechanism map in the handover (Part 2.1) and `analysis/byd/`

## Repository structure

| Folder | Contents | In git? |
|---|---|---|
| `docs/` | Session reports (full history: unlock, KSU, OTA) | ✅ |
| `scripts/build/` | .py generators (Magisk/KSU modules, patches) | ✅ |
| `scripts/analysis/` | .py analysis (certs, vbmeta, kernel, keybox) | ✅ |
| `scripts/device/` | Shell scripts that run on the device (.sh) | ✅ |
| `scripts/flash/` | `windows_install_upgrade_auto.bat` (flash ROM without prompts) | ✅ |
| `config/` | `keybox.xml` (DroidWin — public reference) | ✅ |
| `backup/` | Partition images, dumps, backups | ❌ (large) |
| `rom/` | xiaomi.eu ROMs + extracted | ❌ (~18 GB) |
| `tools/` | Binaries: platform-tools, APKs, module zips, magiskboot, ksud | ❌ (public) |
| `quarantine/` | Unlock CVE tool + extracted artifacts | partial |
| `updates/` | System app APKs/modules | ❌ |

Large/public binaries live on disk but outside history (see `.gitignore`).

## Key procedures (documented in `docs/`)

- **xiaomi.eu OTA:** flash via `scripts/flash/windows_install_upgrade_auto.bat` → re-patch `init_boot` with `ksud boot-patch -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell` → `fastboot flash init_boot_a`
- **Rollback to Magisk:** `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img`
- **Keybox treadmill:** swap `/data/adb/tricky_store/keybox.xml` when revoked + reboot
- **NEVER** `fastboot flashing lock` · **NEVER** edit vbmeta by hand · always double-check POPSICLE (not PANDORA) in downloads
