# Session Report — 2026-07-22

**Objective:** Migrate root from Magisk to KernelSU (or APatch) on the Xiaomi 17 Pro Max to resolve root detection by the Caixa, BYD, and Revolut apps.

**Device:** Xiaomi 17 Pro Max (popsicle) | xiaomi.eu OS3.0.315.0.WPBCNXM | Android 16 | GKI kernel `6.12.23-android16-*-abogki`

---

# ✅ CURRENT STATUS — definitive (23/Jul/2026, after OTA 317)

## Executive summary

**Upgrade OS3.0.315 → OS3.0.317 completed with KSU preserved.** Stack identical to 22/Jul (below), now on ROM **OS3.0.317.0.WPBCNXM** — same `abogki4639` kernel (317 did not change the kernel; the `.ko` 32558 worked straight out of the box). Verified: KSU alive, ZN enforce, PIF, integrity maintained.

**OTA procedure that worked (via fastboot script):**
1. Download the correct POPSICLE ROM (warning: PANDORA = 17 Pro — we nearly used the wrong zip; always double-check the filename)
2. Extraction + audit of `windows_install_upgrade.bat` (no wipe, no relock, both slots)
3. **Pre-patch** the new ROM's init_boot with `ksud boot-patch --allow-shell` (same .ko)
4. Flash script (interactive prompt bypassed with an `_auto.bat` variant — `set /p` doesn't accept pipes in cmd)
5. Boot stock → `fastboot flash init_boot_a <patched>` → reboot → 317+KSU

**Notes:** (a) the built-in Updater aborted 2× (download failure + engine cancelled) — the script path is the reliable one; (b) OTA abort does not damage anything (A/B fallback); (c) the script flashes both slots — _b finally lost the factory CN partitions.

---

# ✅ STATUS — 22/Jul/2026 (KSU LKM — stack reference)

## Executive summary

**Magisk → KernelSU LKM migration COMPLETED with total success on priority apps.** Root now lives inside the kernel (LKM module) on top of the **intact stock** kernel, with kernel-level hiding. No modification to `boot.img` or `vbmeta`.

| App | Before (Magisk) | **Now (KSU LKM)** |
|---|---|---|
| **Google Wallet** | ❌ security warning | **✅ WORKING** |
| **Caixa / BR banks** | ❌ "disable root" (Magic Mount) | **✅ WORKING** |
| **BYD** | ❌ crash (SIGSEGV/PAC) | **✅ WORKING** |
| **Play Integrity** | 3/3 (unstable) | **✅ 3/3 stable** |
| Revolut | ❌ | ❌ more paranoid detector — documented debt (§ Open items) |

## Production stack (all layers)

| Layer | Component | Detail |
|---|---|---|
| Kernel | **stock** `6.12.23-android16-5-g75e9b1c7ae7c-abogki463945075-4k` | original signed `boot.img`, **zero modification** |
| init_boot | **KSU LKM patch** | `ksuinit` + `kernelsu.ko` (driver **32558**) embedded; generated via `ksud boot-patch --allow-shell`; sha256 `7a9c1fb4…fc904` |
| Root | **KernelSU backslashxx** (upstream-compliant fork) | **LKM** mode — kernel module loaded at boot by the ramdisk init; driver 32558 |
| Manager | `KernelSU_v3.2.5-34_32559-release.apk` | **adb root ON** (shell adb = uid 0, context `u:r:ksu:s0`); umount configured in profiles |
| Zygisk | **ZygiskNext 1.4.3-817** | `enforce-denylist enabled`; injects PIF into `gms.unstable`; status: `root_status ✅KernelSU (32558)`, `inject_state 1` |
| Integrity | **PlayIntegrityFork v17** + **TrickyStore v1.4.1** | keybox **DroidWin v3.6** (1 ECDSA + 1 RSA, valid until 2029) + target.txt (gms, vending, caixa, byd, revolut, test apps) |
| Hiding | **KSU "umount modules" GLOBAL** | ALL apps with umount **except** `com.google.android.gms`, `com.android.vending`, `com.termux` |

**Objective proof of hiding:** `/proc/<app>/mountinfo` of an app with umount contains **zero** occurrences of `magisk|kernelsu|ksu|zygisk|debug_ramdisk`. `/proc/modules`: `ksu 172032 Live`.

## Why KSU won (and Magisk never could)

1. **Shamiko was never active** (session discovery): Shamiko 1.2.5's obfuscated `.so` is silently discarded by ZygiskNext 1.4.3's `zn_loader` — the "Shamiko whitelist stack from 21/07" was an illusion; real hiding never existed.
2. **Magisk leaks mounts in userspace**: `tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` visible in `/proc/self/mountinfo` of any app — the Wallet/Caixa detection vector.
3. **KSU umount is done by the kernel itself** in each app's namespace — no userspace artifacts for scanners to find.
4. **The BYD crash (PAC) was a Magisk-environment artifact**, not an app bug: with a 100% clean process (no injection/mounts), the native code runs stable.

## The AVB lock and the init_boot door (key knowledge of the day)

- The **popsicle ABL rejects unsigned `boot.img`** even when unlocked (direct proof: byte-perfect repack → slot fallback). Stock vbmeta `flags=0`.
- vbmeta descriptors: `boot`, `system`, `recovery` in the chain — **`init_boot` OUTSIDE** (strings-scan). That is why Magisk (init_boot) always worked, and it is the door used by **KSU LKM**.
- **Never** hand-edit vbmeta (byte-patch corrupted and killed both slots; restored via flash of the original). If ever needed: regenerate with `avbtool`.
- **Slot _b is unusable for testing**: `vendor_boot`/`dtbo` from factory HyperOS CN (xiaomi.eu only populated _a) — not even pure stock boots there.
- ABL quirk: generic `--set-active` is a NO-OP for rearming a slot (retry does not reset) — use **`fastboot set_active <a|b>`** (underscore).

## Rollback and recovery (tested today)

| Item | Path | sha256 |
|---|---|---|
| init_boot Magisk 30.7 (full rollback) | `backup\ksu-migration\init_boot_a_backup.img` | `c951cdf4…240bcd9` |
| boot stock | `backup\ksu-migration\boot_a_backup.img` | `6c48dd3f…b5893b` |
| original vbmeta (flags=0) | `rom\popsicle_eu\images\vbmeta.img` | — |
| current KSU LKM init_boot (fast re-flash) | `backup\ksu-migration\init_boot-lkm-allowshell.img` | `7a9c1fb4…fc904` |

Rollback to Magisk: `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img` (5 min, full Magisk state restored).

## Maintenance

- **xiaomi.eu OTA:** BEFORE rebooting, re-patch the inactive slot's init_boot: `ksud boot-patch -b <new_init_boot> -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell` + flash.
- **Keybox treadmill:** if Wallet/integrity break → swap `/data/adb/tricky_store/keybox.xml` for a fresh keybox + reboot. Bad keybox rollback: revert the file + reboot.
- **Modules:** install via manager UI or `ksud module install <zip>` (root).
- **NEVER** `fastboot flashing lock`. **Never** hand-edit vbmeta.
- **adb push to phone:** use `/storage/emulated/0/...` (`/sdcard` does not resolve in the adbd namespace under KSU).

## Open items / technical debt

- **Revolut:** detects beyond mounts/integrity (installer names, /proc/net, direct keymaster). Future levers, in order of preference: **SuSFS** (hiding inside the VFS — WildKernels SUSFS `.ko` for KMI android16-6.12, or Kokuban **ReSukiSU** kernel for sm8850), custom kernel (official `popsicle-w-oss` source exists on MiCode).
- **Optional toolbox:** TWRP 3.7.1 unofficial functional for popsicle (flash recovery); Termux F-Droid (the Google Play version installed lacks RunCommandService).

## Timeline (day synthesis)

1. **Morning:** Wallet broken on Magisk. Diagnosis: integrity 3/3 ok, but Shamiko dead and `no_mount_znctl` suppressing ZN hiding → Magisk mounts leaking. Clean rebuild + ZN native whitelist → hiding proven clean, but Wallet remained flagged (server-side flag registered while the device was still leaking).
2. **Afternoon:** decision to go KSU. Research (yapixel/xxksu/AVB/Kokuban/eng ABL) → AVB wall on boot.img → the `init_boot` door (outside the chain) → live proof of `.ko` on stock kernel → LKM flash on _a → **KSU alive** → ZN+PIF+TS stack → global umount + enforce → Google reset with clean device → **Wallet, Caixa, BYD working, integrity 3/3.**

---
---

# DETAILED HISTORY (chronological)

**Session 1 result (morning, superseded):** KSU and APatch deemed non-viable on the `abogki` kernel. Magisk stack restored. *(Conclusion revised in Session 2 — see CURRENT STATUS above.)*

## 1. Initial state

Functional stack (inherited from the 2026-07-21 session):
- Magisk v30.7 (patched init_boot, 2 slots)
- ZygiskNext v1.4.3 (replaces broken built-in Zygisk on Android 16)
- PlayIntegrityFork v17 + TrickyStore v1.4.1 + keybox DroidWin v3.6
- Shamiko v1.2.5 in whitelist mode (inverted DenyList)
- Play Integrity: 3/3 ✓ | Wallet: ✓

| App | Status |
|---|---|
| Wallet | ✅ |
| Caixa | ❌ "disable root" (Magisk Magic Mount) |
| BYD | ❌ crash (SIGSEGV, PAC corruption) |
| Revolut | ❌ "environment is not secure" (Magic Mount) |

---

## 2. KernelSU investigation — 6 attempts, 0 successes

### Attempt 1 — KSU stock v3.2.5 (boot)
- **Method:** KSU app patch of stock `boot.img` → flash boot_a/b + stock init_boot
- **Result:** ❌ `/sys/module/kernelsu` absent, `su` not found
- **Cause:** Stock KSU does not support kernel 6.12

### Attempt 2 — KSU-Next v3.3.0 (boot)
- **Method:** KSU-Next app patch of stock `boot.img` → flash boot_a/b + stock init_boot
- **Result:** ❌ identical
- **Cause:** File patch does not instrument the kernel correctly

### Attempt 3 — KSU-Next direct install (via Magisk root)
- **Method:** Active Magisk root → KSU-Next "Direct Install" → flash stock init_boot
- **Result:** ❌
- **Cause:** KSU-Next wrote to `init_boot` (correct), but was overwritten by the stock init_boot flash right afterward (**my mistake**)

### Attempt 4 — KSU-Next LKM (init_boot)
- **Method:** KSU-Next patch of `init_boot.img` → flash KSU init_boot + stock boot
- **Result:** ❌
- **Cause:** Stock boot (no KSU hooks) + KSU init_boot (no supporting kernel)

### Attempt 5 — yapixel KSU+SuSFS kernel (boot)
- **Source:** [yapixel/popsicle_ksu_workflow](https://github.com/yapixel/popsicle_ksu_workflow) — popsicle-specific repo, kernel 6.12.23, updated **today** (260722)
- **File:** `xxksu_32558_6.12.23-260722-SuSFS_v2.2.0.zip` (AnyKernel3, 18.7 MB, SHA256 `d85333edd…`)
- **Method:** Manual kernel `Image` extraction + `magiskboot` on-device → unpack stock boot → replace kernel → repack → dd boot_a/b
- **Result:** ❌ kernel successfully replaced (version changed from `abogki4639` to `abogki4443`) but KSU did not load
- **Cause:** init_boot was stock — missing the KSU userspace to activate kernel hooks

### Attempt 6 — yapixel kernel + KSU-Next init_boot
- **Method:** yapixel KSU+SuSFS kernel (boot) + KSU-Next LKM init_boot (userspace)
- **Result:** ❌
- **Cause:** Full kernel+userspace combination still non-functional — fundamental incompatibility with the `abogki` GKI build

### KSU conclusion
The `abogki` GKI kernel 6.12.23 **does not expose the hooks KSU needs**, in any tested kernel + userspace combination. The yapixel repo updates daily — a future build may solve this.

---

## 3. APatch investigation

- **Latest release:** v11142 (12/Nov/2025) — 8 months stalled
- **Support:** kernels 3.18 through 6.1 (6.12 not supported)
- **Issue #1169** (Android 16/kernel 6.12): closed without a response from the maintainer
- **Conclusion:** APatch does not support Android 16. No active forks found.

---

## 4. Additional diagnostics

### 4.1 BYD — native crash (SIGSEGV due to PAC)
- **Tombstone (`tombstone_11`):** `sp=0, lr=0, pc=0x1f8` — stack completely corrupted
- **PAC enabled:** `pac_enabled_keys: 000000000000000f` — ARMv8.3 PAC active
- **Cause:** BYD native code incompatible with Android 16 PAC enforcement, **regardless of root or injection** (confirmed with and without ZygiskNext/DenyList)
- **Solution:** Wait for an app update from BYD

### 4.2 Caixa — Magic Mount
- With integrity 3/3 and DenyList active (no injection into the app), the message changed from "insecure system" to **"disable root mode"**
- Integrity passed, but the app detects Magisk artifacts on the filesystem (Magic Mount)
- **Solution:** Only KSU/APatch would solve it — both non-viable today

### 4.3 DenyList vs AllowList
- **DenyList ON (enforce):** listed apps **do not see root** and **receive no injection** from ZygiskNext
- **AllowList/Whitelist (Shamiko):** listed apps **see root**; all others are hidden (used to isolate Termux)
- Final strategy: **standard DenyList** (no Shamiko), Termux off the list (sees root), banking apps on the list (hidden + no injection)

---

## 5. Final stack state

### Partitions
| Partition | Contents |
|---|---|
| boot_a / boot_b | Stock GKI kernel (original ROM) |
| init_boot_a / init_boot_b | Magisk v30.7 patched |

### Modules (3)
| Module | Version | Function |
|---|---|---|
| ZygiskNext (zygisksu) | v1.4.3-817 | Replaces built-in Zygisk (broken on Android 16) |
| PlayIntegrityFork | v17 | Fingerprint spoof + build fields |
| TrickyStore | v1.4.1 | Intercepts key attestation (hardware-backed) |

### Settings
| Setting | Value |
|---|---|
| Zygisk (Magisk built-in) | ON (ZygiskNext bootstrap) |
| Enforce DenyList | ON |
| DenyList | Wallet, Revolut, Caixa (superapp + cards), BYD |
| keybox.xml | DroidWin v3.6 (sha256 `f6d0b41c…cc78d`) — 1 ECDSA + 1 RSA, valid until 2029 |
| target.txt | gms, vending + test apps + caixa + byd + revolut |
| propspoof | `/data/adb/service.d/propspoof.sh` — verifiedbootstate=green, flash.locked=1 |

### App results
| App | Status | Note |
|---|---|---|
| Play Integrity | ✅ 3/3 | BASIC + DEVICE + STRONG |
| Wallet | ✅ Working | Tap-to-pay OK |
| Caixa | ❌ "disable root" | Magic Mount — bank manager |
| BYD | ❌ crash (PAC) | App incompatible with Android 16 — wait for update |
| Revolut | ⏸️ Via PC/web | KSU migration parked |

---

## 6. Lessons learned

- ❌ **Do NOT test KSU/APatch on this `abogki` kernel** until the [yapixel/popsicle_ksu_workflow](https://github.com/yapixel/popsicle_ksu_workflow) repo has community success reports
- ❌ **Do NOT use Shamiko whitelist** with Magisk's standard DenyList — inverted logic causes confusion
- ❌ **Do NOT modify `ro.build.host`, `ro.product.mod_device`, `ro.xiaomi.eu.*` props** → guaranteed bootloop
- ❌ **Do NOT install Zygisk Assistant** → bootloop on Android 16
- ✅ **ZygiskNext v1.4.3** is mandatory on Android 16 — Magisk 30.7 built-in Zygisk does not load modules
- ✅ **Magisk Zygisk must remain ON** even with ZygiskNext (bootstrap hook)
- ✅ **Always check the correct partition** before flashing: KSU goes on `boot` (kernel), Magisk on `init_boot` (ramdisk). KSU-Next LKM goes on `init_boot`
- ✅ **Hash the file before flashing** — prevents writing an invalid patch

---

## 7. Project files

```
UnlockXiaomi\
├── backup\
│   ├── 2026-07-21\              # Pre-unlock dumps (5 partitions)
│   ├── ksu-migration\           # KSU attempts (boots, init_boots, anykernel)
│   └── 22072026_092522.zip      # User backup (4.59 GB)
├── rom\
│   ├── popsicle_eu\              # Extracted xiaomi.eu ROM
│   └── magisk_patched-30700_ChHUL.img  # Functional Magisk init_boot
├── tools\
│   ├── platform-tools\          # Official adb/fastboot (hash verified)
│   ├── Magisk-v30.7.apk
│   ├── ZygiskNext-v1.4.3.zip
│   ├── PlayIntegrityFork-v17.zip
│   ├── TrickyStore-v1.4.1.zip
│   ├── Shamiko-v1.2.5-414.zip   # Not installed in the final stack
│   ├── KernelSU_Next_v3.3.0.apk
│   ├── popsicle-ksu.zip         # yapixel KSU+SuSFS (AnyKernel3)
│   └── *.sh                     # Utility scripts
└── quarantine\
    ├── droidwin_keybox\          # keybox.xml DroidWin v3.6
    └── extracted\               # CVE v2.0.0/v114514 tool
```

## 8. Future maintenance

- **Keybox treadmill:** ~1 week–months. Wallet breaks → swap `/data/adb/tricky_store/keybox.xml` + reboot
- **Bad keybox:** `touch /data/adb/modules/tricky_store/disable` + reboot reverts
- **xiaomi.eu OTA:** Magisk → "Install to Inactive Slot" BEFORE rebooting
- **NEVER** `fastboot flashing lock`
- **KSU canary repo:** [yapixel/popsicle_ksu_workflow](https://github.com/yapixel/popsicle_ksu_workflow) — check daily
- **Caixa/BYD:** retest when (a) KSU works on `abogki` OR (b) manager enables whitelist OR (c) BYD updates the app

---

# Addendum — Session 2 (22/Jul/2026, afternoon) — Wallet broke + clean rebuild + Shamiko discovery

## Wallet regression diagnosis

- Wallet stopped working hours after Session 1. Verified: keybox intact (`teeBroken=false`), integrity **3/3**, props ok, `/proc` with `hidepid=invisible` (ROM protects)
- **Finding 1:** the "Shamiko whitelist stack from 21/07" **was never active** — Shamiko 1.2.5's `.so` is silently discarded by ZygiskNext 1.4.3's `zn_loader` (registry `{"modules":[]}`, no `.tmp/status`, no injection in maps). Wallet worked *despite* Shamiko being dead
- **Finding 2:** with Shamiko dead, the `no_mount_znctl` flag (created by its post-fs-data) **suppressed ZN's own mount hiding** → apps could read `tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` in `/proc/self/mountinfo` → Wallet detection vector

## Clean rebuild executed

1. Full backup of `/data/adb` → `backup\adb-state-2026-07-22\` (keybox, target.txt, key_db, configs)
2. `magisk --remove-modules` + wipe of `/data/adb/{zygisksu,shamiko,tricky_store}` + drop `propspoof.sh` (redundant with Shamiko's service.sh)
3. Clean reinstall: ZygiskNext 1.4.3 → PIF v17 → TrickyStore v1.4.1 (+keybox/target restored)
4. Magisk built-in Zygisk **turned OFF** (ZN README requires OFF; the "bootstrap ON" lesson from Session 1 was wrong — ZN injects fine without it)
5. **Route B — ZN native hiding** (Shamiko abandoned; v1.2.5 is the last release and does not load on ZN 1.4.3): removed Shamiko + `no_mount_znctl` → `znctl denylist-policy whitelist` + `znctl enforce-denylist enabled` → allowlist: termux, gms, gms.unstable, vending

## State after clean rebuild

| Item | Status |
|---|---|
| Integrity | ✅ 3/3 |
| Mountinfo of non-listed app | ✅ Clean (verified via /proc — zero magisk mounts) |
| Termux root | ✅ |
| Magisk app | ✅ Normal |
| Wallet | ❌ Security warning (server-side flag — previous GSF reset was done while the device was still leaking) |

## Operational facts (Magisk 30.7 + Android 16)

- `/data/adb` **sealed by Magisk itself** — read/write only via daemon-context (micro-module whose customize.sh runs as daemon root; builders in `tools\build_*_module.py`)
- `znctl` = `/data/adb/modules/zygisksu/bin/zygiskd`; flaky from shell → retry wrapper (`tools\zn_whitelist_setup.sh`)
- `magisk --denylist status` / `--sqlite` trigger intermittent SIGTRAP ("selfchecker remold.magisk sig5") — use retry/daemon-context
- TrickyStore is daemon-based — **does not appear** in `zygiskd status` (normal)

## Phase 2 — KSU re-investigation (in progress)

- Autopsy of the 6 Session 1 attempts: attempt 5 (yapixel kernel GKI-mode) was discarded due to a **false negative** (`/sys/module/kernelsu` does not exist in GKI-mode builds; correct detection is via the Manager app). Attempt 6 mixed GKI+LKM modes (conflicting)
- yapixel kernel verified locally: `6.12.23-android16-5-...-abogki444322847-4k` — **same KMI generation** as stock (`android16-5`) → vendor modules should match. KSU ×360 + susfs ×185 in the binary (actually compiled in)
- Plan: flash pure GKI-mode (yapixel boot + **stock** init_boot + KSU-Next Manager), rollback = `dd` stock images from ROM (`rom\popsicle_eu\images\`)

---

# Addendum 2 — Session 2 continued (22/Jul/2026, afternoon) — KSU VICTORY

## The winning path: KSU LKM via init_boot

**Discoveries that unlocked the path:**
1. **ABL rejects unsigned boot.img** (direct proof: byte-perfect repack → slot fallback). Stock vbmeta `flags=0`; `boot`/`system`/`recovery` in the chain, **`init_boot` OUTSIDE the chain** (strings-scan of descriptors) — that's why Magisk works and KSU LKM does too
2. **`init_boot` is the door**: KSU as a kernel module (.ko) loaded at boot by the ramdisk init — signed stock kernel remains intact, zero AVB impact
3. **Live proof-of-life**: `ksud insmod android16-6.12_kernelsu.ko` on the running stock kernel → module `ksu` LIVE + `Kernel Version: 32558` via prctl
4. **Slot _b unusable for tests**: `vendor_boot`/`dtbo` partitions from factory HyperOS CN (xiaomi.eu only populated _a) — not even pure stock boots on _b. Tests on _a with fastboot recovery
5. **Xiaomi ABL quirks**: generic `--set-active` is a NO-OP for rearming a slot (retry-count does not reset) — use `fastboot set_active <a|b>` (underscore)
6. **vbmeta must not be hand-edited** (byte-patch corrupted and killed both slots; restored via flash of the original) — regenerate with avbtool if needed

## Final production stack

| Layer | Contents |
|---|---|
| boot.img | **Stock** abogki4639 (untouched) |
| init_boot | **KSU LKM 32558** (backslashxx .ko + ksuinit, `ksud boot-patch --allow-shell`) |
| Root | KernelSU driver 32558 + backslashxx manager v3.2.5-34 (32559), **adb root ON** |
| Zygisk | ZygiskNext 1.4.3-817, `enforce-denylist enabled` |
| Integrity | PlayIntegrityFork v17 + TrickyStore v1.4.1 (keybox DroidWin v3.6) |
| Hiding | **umount modules for ALL apps** except gms/vending/Termux (KSU profiles) |

## App results (FINAL)

| App | Magisk (yesterday) | **KSU (today)** |
|---|---|---|
| Play Integrity | 3/3 | **3/3 ✅** |
| Wallet | ❌ security warning | **✅ WORKING** |
| Caixa/banks | ❌ Magic Mount | **✅ WORKING** |
| BYD | ❌ crash PAC | **✅ WORKING** |
| Revolut | ❌ insecure environment | ❌ (more paranoid detector; documented debt — future lever: SuSFS) |

**BYD lesson:** the "root-independent PAC crash" was actually a Magisk-environment artifact (injection/mount residues in the process). With a 100% clean process (global umount + ZN enforce), the native code runs stable.

## KSU operations — quick reference

- **Rollback to Magisk:** `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img` (c951cdf4)
- **adb root:** enabled in manager (shell = uid 0, `u:r:ksu:s0`); `su` in /system/bin
- **Modules:** manager UI OR `ksud module install <zip>` (root)
- **znctl:** `/data/adb/ksu/bin/znctl` (status, enforce-denylist — no denylist-policy in KSU)
- **adb push to phone:** use `/storage/emulated/0/...` (`/sdcard` breaks in the adbd namespace under KSU)
- **xiaomi.eu OTA:** re-patch init_boot on the inactive slot via `ksud boot-patch` before rebooting
- **Keybox treadmill:** swap `/data/adb/tricky_store/keybox.xml` when revoked
