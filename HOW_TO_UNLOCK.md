# How to Unlock — Xiaomi 17 Pro Max (popsicle)

Step-by-step reproducible guide: bootloader unlock → xiaomi.eu ROM → KernelSU LKM root with full hiding stack. **Device-specific for the Xiaomi 17 Pro Max** (`popsicle`, 2509FPN0BC, China-only, Snapdragon 8 Elite Gen 5, HyperOS 3 / Android 16). Many steps are generic Xiaomi; popsicle-specific quirks are called out explicitly.

**As of 28/Jul/2026.** For the latest state, versions, and known issues, see [`docs/SESSION-HANDOVER.md`](docs/SESSION-HANDOVER.md) — the living continuity document.

---

## 0. Warnings — read before you start

- **Unlocking wipes ALL user data** (factory reset). Back up everything off-device first.
- **NEVER re-lock the bootloader** after flashing a custom ROM (`fastboot flashing lock`). Re-locking on non-stock firmware = hard brick. Even on stock, re-locking a China device that was cross-flashed may brick.
- **Anti-rollback:** do not attempt to downgrade to an older MIUI/HyperOS version. Xiaomi's ARB (anti-rollback) counters can permanently brick the device.
- **Warranty is void** the moment you request the unlock. China-model devices may have additional restrictions.
- **Banking/streaming apps may break** at any update — the hiding stack helps but is a cat-and-mouse game. Google can revoke Play Integrity on your fingerprint at any time.
- **You are responsible.** This guide documents what worked on one specific device. Commands, button combos, and boot behavior can differ across firmware versions.

---

## 1. Prerequisites

### 1.1 Accounts & tools

| What | Where / notes |
|---|---|
| Xiaomi Community account | Required for official unlock; must be ≥ 30 days old; bound to the device in Developer Options → Mi Unlock status |
| **Platform-tools** (adb + fastboot) | [Google's official SDK](https://developer.android.com/tools/releases/platform-tools). Verify hashes. |
| USB drivers | Windows: [Xiaomi USB Driver](https://developer.android.com/studio/run/win-usb) or the one bundled with Mi Unlock. Linux: no extra driver needed. |
| **TWRP 3.7.1 unofficial** | [XDA thread](https://xdaforums.com/t/recovery-unofficial-a16-twrp-3-7-1-for-xiaomi-17-series.4784052/) — variant `fix22ZX_pinwork_partialdecryption`. Flash once as a safety net. |
| **Termux (F-Droid)** | Needed for on-device tooling (the Google Play build lacks `RunCommandService`). |

### 1.2 Downloads — ROM

- **xiaomi.eu ROM for POPSICLE** — **NOT pandora** (17 Pro) or pudding (17 base).
  - Current as of this guide: `OS3.0.317.0.WPBCNXM` (Android 16, HyperOS 3).
  - Download from [xiaomi.eu](https://xiaomi.eu/community/) — always verify the filename contains `POPSICLE`.
  - The ROM ships as a fastboot-flashable zip. Extract it; you will use the `windows_install_upgrade.bat` script (or the `_auto.bat` variant that skips the interactive prompt — see [`scripts/flash/`](scripts/flash/)).

### 1.3 Downloads — KernelSU

- **Kernel module (`.ko`):** from [backslashxx/KernelSU](https://github.com/backslashxx/KernelSU) releases — the file named `android16-6.12_kernelsu.ko`.
  - **Recommended fork:** [`andersonlucasg3/KernelSU`](https://github.com/andersonlucasg3/KernelSU), branch `hide-lkm`. Patch `9ea26f3` adds `kobject_del`+`list_del` so the module is **invisible in `/proc/modules` and `/sys/module`** — some banking apps scan those paths. Built via GitHub Actions; the `.ko` is in `tools/ksu_apk/` of the companion repo.
  - If you use the stock backslashxx `.ko` instead of the fork: the module **will** appear in `/proc/modules`. HMA-OSS can mitigate app enumeration, but invisibility is cleaner.
- **KSU manager APK:** from the same backslashxx release — e.g. `KernelSU_v3.2.5-34_32559-release.apk` (or newer). Driver requires manager ≥ 32513.
- **`ksud` binary:** bundled inside the manager APK, or standalone from the release. Needed for `boot-patch` and `insmod`.

### 1.4 Downloads — Zygisk & integrity stack

| Component | Version (tested) | Source |
|---|---|---|
| **ZygiskNext** | 1.4.3-817 | [GitHub releases](https://github.com/Dr-TSNG/ZygiskNext/releases) |
| **PlayIntegrityFork** | v17 | [GitHub releases](https://github.com/osm0sis/PlayIntegrityFork/releases) |
| **TrickyStore** | v1.4.1 | [GitHub releases](https://github.com/5ec1cff/TrickyStore/releases) |

All three are KSU-compatible `.zip` modules — install via the manager UI or `ksud module install <zip>`.

### 1.5 Keybox

You need a valid, **non-revoked** keybox XML for TrickyStore. Public keyboxes (like DroidWin v3.6) work initially but get burned quickly. Sources: Telegram channels, XDA. Place at `/data/adb/tricky_store/keybox.xml`.

---

## 2. Step 1 — Unlock the bootloader

### 2.1 Official path (Mi Unlock / Xiaomi Community)

1. On the phone: **Settings → Developer Options → Mi Unlock status** → bind your Xiaomi account. The device must be connected to mobile data (Wi-Fi alone may fail).
2. Install **Mi Unlock** on a Windows PC, sign in with the same account.
3. Power off the phone → hold **Volume Down + Power** → Fastboot mode.
4. Connect USB, run Mi Unlock → follow prompts. A **waiting period** (typically 168 hours for China models) is enforced server-side.

### 2.2 Alternative path (CVE — the path actually used for this device)

The popsicle in this project was unlocked via **CVE-2026-43499** (Xiaomi bootrom exploit), using the audited **Linuxoid-cn v2.0.0** tool. This bypasses the waiting period and account binding, but:

- Requires EDL mode (Qualcomm emergency download) — usually triggered via test-point or a deep-flash cable.
- The tool is third-party; audit it yourself before running.
- The original tool artifacts are in `quarantine/` (not checked into git).

**If you can wait the 168 hours, prefer the official path.** The CVE path is documented here because it is what this project actually used — not an endorsement.

### 2.3 Post-unlock check

After unlock succeeds, the device will factory-reset and reboot. Verify:

```
fastboot oem device-info
```

Expected: `Device unlocked: true`.

---

## 3. Step 2 — Flash xiaomi.eu ROM

### 3.1 Prepare

1. Extract the xiaomi.eu fastboot ROM zip.
2. **Audit the flash script** (`windows_install_upgrade.bat`): confirm it does **not** contain `fastboot flashing lock`, does **not** wipe userdata a second time, and flashes both slots.
3. The script has an interactive prompt (`set /p`). On Windows `cmd.exe`, pipes don't work with `set /p`. Use the `_auto.bat` variant from this repo's [`scripts/flash/`](scripts/flash/) if you want unattended execution.

### 3.2 Flash

1. Reboot to fastboot: `adb reboot bootloader` (or Volume Down + Power).
2. Run the script: `windows_install_upgrade_auto.bat`.
3. The script flashes `super` (in ~14 sparse chunks), `boot`, `vendor_boot`, `dtbo`, `init_boot` (stock), and both slots. It ends with `fastboot set_active a` and `fastboot reboot`.
4. First boot takes 5–10 minutes. Let it finish.

### 3.3 Verify

- **Settings → About phone:** MIUI version should show `OS3.0.317.0.WPBCNXM` (or newer).
- **Slot:** `adb shell getprop ro.boot.slot_suffix` → `_a` (the script leaves slot _a active).
- **Google Play:** sign in, run Play Integrity Checker → expect BASIC + DEVICE to pass (STRONG comes after the KSU stack in Step 5).

---

## 4. Step 3 — TWRP as a safety net

TWRP is **not required** for root, but it saved this device from two bootloops (Caixa SSAID corruption, vbmeta experiment). It decrypts `/data` with your PIN (variant `fix22ZX_pinwork_partialdecryption`).

```
fastboot flash recovery twrp-3.7.1-popsicle-fix22ZX.img
```

⚠️ `fastboot boot twrp.img` does **not** work on popsicle — you must flash it to the recovery partition. Once flashed, boot into it with **Volume Up + Power** from power-off.

Keep the TWRP image on your PC. Do **not** use TWRP to flash ROMs unless you are certain the zip is TWRP-compatible (xiaomi.eu fastboot ROMs are NOT).

---

## 5. Step 4 — KernelSU LKM (the core)

This is the most critical section. **Read all of it before typing anything.**

### 5.1 Why LKM and not a patched boot.img

Xiaomi's ABL (bootloader) on popsicle **rejects unsigned `boot.img` even when unlocked**. The stock `vbmeta` has `flags=0` and verifies `boot`, `system`, and `recovery`. However, `init_boot` is **not** in the verification chain — that's why Magisk (which patches `init_boot`) always worked, and it's the door for KSU LKM.

KSU as a **Loadable Kernel Module** lives entirely inside `init_boot`. The stock kernel in `boot.img` stays untouched → AVB passes → the kernel boots → `ksuinit` in the ramdisk loads the `.ko` → root is alive.

**You never touch `boot.img` or `vbmeta`.** This is the only path validated on popsicle.

### 5.2 Live proof-of-life (optional but recommended)

Before flashing anything, verify the `.ko` actually loads on your kernel:

```bash
adb push android16-6.12_kernelsu.ko /data/local/tmp/
adb shell
su                                          # or: ksud insmod directly from adb root
ksud insmod /data/local/tmp/android16-6.12_kernelsu.ko
ksud debug version
# Expected: "Kernel Version: 32558"
```

If this works, the `.ko` is compatible with your kernel. (The 317 OTA did not change the kernel — same `abogki4639` from 315 — so the same `.ko` works across ROM versions that share the KMI.)

### 5.3 Backup the stock init_boot

**Do this before any patching.** This is your rollback if something goes wrong:

```bash
adb shell
su -mm
dd if=/dev/block/by-name/init_boot_a of=/sdcard/init_boot_stock_a.img
# Also copy to PC:
adb pull /storage/emulated/0/init_boot_stock_a.img
```

Verify the dump isn't all-zeros: `file init_boot_stock_a.img` should report an Android boot image.

### 5.4 Patch init_boot with ksud

`ksud boot-patch` runs **without root** — it only needs the input image, the `.ko`, and an output directory:

```bash
ksud boot-patch \
  -b init_boot_stock_a.img \
  -m android16-6.12_kernelsu.ko \
  --partition init_boot \
  --allow-shell \
  -o ./ksu-output/
```

| Flag | Meaning |
|---|---|
| `-b` | Input `init_boot` image to patch |
| `-m` | Path to the `.ko` kernel module |
| `--partition init_boot` | Target partition type (NOT boot — critical) |
| `--allow-shell` | Enables `adb root` (shell gets uid 0, context `u:r:ksu:s0`) |
| `-o` | Output directory |

The output is a patched `init_boot.img` (plus a `ksuinit` binary alongside it).

### 5.5 Flash and boot

```bash
adb reboot bootloader
fastboot flash init_boot_a ksu-output/init_boot.img
fastboot reboot
```

After boot, install the KSU manager APK and open it. It should show:

- **Working:** kernel version `32558`, manager version ≥ 32513
- **Superuser:** empty (no apps granted yet)

Enable **adb root** in the manager settings. Test:

```bash
adb shell
# Should land in a root shell (#) immediately
```

### 5.6 If it fails

Flash the stock `init_boot` back and reboot:

```bash
fastboot flash init_boot_a init_boot_stock_a.img
fastboot reboot
```

The device will boot with the stock ROM, no root, no damage. Diagnose and retry.

---

## 6. Step 5 — Hiding stack (Zygisk + integrity + umount)

This stack delivers **Play Integrity 3/3 (BASIC + DEVICE + STRONG)** and hides root from apps.

### 6.1 Install the three modules

In the KSU manager: **Modules → Install** (or via shell):

```bash
ksud module install ZygiskNext-v1.4.3-817.zip
ksud module install PlayIntegrityFork-v17.zip
ksud module install TrickyStore-v1.4.1.zip
```

**Reboot.**

### 6.2 Configure ZygiskNext

After reboot, verify ZN is injecting:

```bash
su -mm
znctl status
# Expected: root_status ✅KernelSU (32558), inject_state 1, modules64: playintegrityfix
```

**USAP pool MUST be disabled** — ZN 1.4.3 does **not** inject into pool-spawned processes (documented at NeoZygisk#73; confirmed on MIUI):

```bash
su
device_config put runtime_native usap_pool_enabled false
```

This persists across reboots. Also set the volatile props (re-apply after each boot, or add to a service script):

```bash
setprop persist.sys.usap_pool_enabled false
setprop dalvik.vm.usap_pool_enabled false
```

Enable `enforce-denylist`:

```bash
znctl enforce-denylist enabled
```

### 6.3 Configure PlayIntegrityFork

Place a valid **`custom.pif.prop`** in `/data/adb/modules/playintegrityfix/`. The one used in this project: **Pixel 10 (frankel, Canary ZP11.260618.005)** — expires 2026-08-19. Run the PIF Action script to refresh before expiry.

This spoofs the device fingerprint for DroidGuard attestation (GMS's integrity check). The actual device identity seen by GMS for non-attestation purposes (digital car key eligibility, etc.) remains the real model.

### 6.4 Configure TrickyStore

1. Place `keybox.xml` at `/data/adb/tricky_store/keybox.xml`.
2. Create `/data/adb/tricky_store/target.txt` — list packages that need certificate chain spoofing. Minimum:

   ```
   com.google.android.gms
   com.android.vending
   ```

   Add banking apps as needed. One package per line.
3. Create `/data/adb/tricky_store/security_patch.txt` containing a date string like `2026-07-05`.
4. **Reboot.**

### 6.5 Umount global — kernel-level mount hiding

In the KSU manager: **Superuser → ⚙️ (settings icon) → Umount modules**.

Set it to **global** (all apps). Then add exceptions for apps that **need** to see KSU mounts or receive Zygisk injection:

- `com.google.android.gms` + `com.google.android.gms.unstable` (integrity needs injection)
- `com.android.vending` (Play Store)
- `com.termux` (root shell)

**On KSU, umount = Zygisk denylist.** Apps under umount do **not** receive ZygiskNext injection. If you later install DeviceID+ or another Zygisk module, you must turn umount **OFF** for any app you want injected (see §6.1 of the handover).

### 6.6 Validate

1. Install **Play Integrity API Checker** from Google Play.
2. Run it → expect **3/3 (BASIC + DEVICE + STRONG)**.
3. If you get only 2/3, check: keybox valid? `teeBroken` in TrickyStore log? PIF print expired?
4. **BEFORE logging into sensitive apps**, clear Google Play Services + Play Store data (or do a full GSF reset if you were previously flagged). Open Play Store → settings → "Device certification" should show "Certified".

---

## 7. Step 6 — Optional: author's modules

These are modules developed during this project. They are **not required** for basic root + hiding; include them only if you need per-app SSAID spoofing or applist hiding.

### 7.1 DeviceID+ (per-app SSAID + prop spoofing)

AGPL fork of [sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger). Source: `modules/deviceidchanger/`. Current: **v2.3.1** (2003001).

**Features:**
- Per-app **ANDROID_ID (SSAID)** randomization via KSU WebUI — randomize per app or share a global ID
- **Global prop spoof** (`ro.build.host`, `ro.gms.dck.eligible_wcc=3`) persisted via `service.sh`
- **Per-app prop spoof** via three mechanisms: prop_area COW (covers all read paths), GOT/PLT hooks, and `Build.*` JNI field spoof in postSpecialize
- **Native DCK hook** (v2.3.1): passive hook for BYD digital key eligibility (`dck.hook=1` key in `.perapp_props`)

**Build:** `cd native && ./build.sh` (Termux clang 21). Output: `module/zygisk/arm64-v8a.so`. Then zip the `module/` folder and install via KSU manager.

**⚠️ Rules:**
- NEVER hot-swap the `.so` without reboot — ZN caches the entry offset in the zygote (every injected app crashes)
- KSU module updates overwrite the module dir — re-merge `config.json`, `.props_spoof`, `.perapp_props` into `/data/adb/modules_update/deviceidchanger/` before reboot
- Config match is by **process name** (nice_name), not package — `com.google.android.gms` ≠ `com.google.android.gms.persistent`

### 7.2 HMA-OSS (applist hiding)

[hma_oss](https://github.com/HM-OSS/HM-OSS) **oss-164** — filters the app list in `system_server`, covering raw Binder enumeration (the detection vector used by DexProtector-protected apps like Revolut, Bradesco Seguros, and BYD).

**Install:** zip in `tools/hma_oss/`. Config lives at `/data/misc/hide_my_applist_hmaosspreseedab/config.json`. The `bancos` template (empty whitelist → target app sees zero user apps) is applied per-app via the manager UI.

**⚠️** Shamiko 1.2.5 does **not** load on ZygiskNext 1.4.3 (silent discard by `zn_loader`). HMA-OSS is the replacement for app hiding on this stack.

### 7.3 Example: hiding root from a specific app

If an app detects root despite the stack above:

1. KSU manager → **Umount modules → OFF** for that app (allows Zygisk injection).
2. HMA-OSS → apply `bancos` template to the app (hides all other user apps from it).
3. DeviceID+ → randomize SSAID for the app + spoof `ro.product.manufacturer` or other props as needed via `.perapp_props`.
4. Force-stop the app and relaunch.

See `docs/SESSION-HANDOVER.md` Part 2 (Petal Maps, Revolut, Bradesco Seguros) and Part 3 (Caixa) for worked examples.

---

## 8. Maintenance

### 8.1 xiaomi.eu OTA with KSU preserved

When a new ROM version is released:

1. **Download the correct POPSICLE ROM** — double-check the filename. `PANDORA` = 17 Pro, not 17 Pro Max.
2. Extract the new ROM and copy its `init_boot.img` (from `images/init_boot.img`).
3. **Pre-patch** the new init_boot **before** flashing the ROM:

   ```bash
   ksud boot-patch \
     -b <new_rom_init_boot.img> \
     -m android16-6.12_kernelsu.ko \
     --partition init_boot \
     --allow-shell \
     -o ./patched-init_boot/
   ```

4. Flash the ROM (fastboot script). It will write the stock `init_boot` and set `set_active a`.
5. **Before rebooting**, flash the pre-patched init_boot:

   ```bash
   fastboot flash init_boot_a patched-init_boot/init_boot.img
   ```

6. Reboot → new ROM + KSU alive.

**Why pre-patch:** the flash script writes the stock `init_boot` to both slots and boots with `set_active a`. If you reboot without flashing the patched image first, you boot into stock → no root → no `ksud` to patch from the running system → you need a PC to fastboot-flash the patched image anyway. Pre-patching avoids the extra round-trip.

**Note:** the built-in OTA updater may abort (download failure, engine cancel). The fastboot script path is the reliable one. OTAs that abort do no damage (A/B fallback).

### 8.2 Keybox treadmill

Keyboxes get revoked over time. When Play Integrity drops:

1. Swap `/data/adb/tricky_store/keybox.xml` for a fresh keybox.
2. Reboot.
3. Verify PI 3/3 before using Wallet or banking apps.

Bad keybox rollback: revert the file + reboot.

### 8.3 PIF print expiry

The PIF print (fingerprint) has an expiration date embedded in the build fingerprint. Run the **PIF Action** script (GitHub Actions in the PlayIntegrityFork repo) to generate a fresh `custom.pif.prop` before the current one expires.

### 8.4 Never do these

- `fastboot flashing lock` → **brick.**
- Hand-edit `vbmeta` → corrupted both slots once; recovery required flashing the original.
- Downgrade ROM → ARB brick.
- Update system apps from the Play Store → xiaomi.eu signs them with a different certificate; updates fail with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. System apps should come from the ROM.

---

## 9. Troubleshooting

| Symptom | Likely cause | Action |
|---|---|---|
| **Bootloop** | Corrupted module config, bad SSAID edit | Boot TWRP (Vol Up + Power) → wipe `/data/adb/modules/<offender>` → reboot. Or use KSU safe mode (hold Volume Down during boot — disables all modules). |
| **PI drops to 2/3** | Keybox revoked or PIF print expired | Swap keybox → reboot → retest. If still 2/3, refresh PIF print. |
| **App detects root** | Mounts visible, package enumeration, Build.* mismatch | Check umount is ON for the app. If the app needs injection (props, SSAID): turn umount OFF → apply HMA-OSS `bancos` template → verify Zygisk injection with `znctl status` and `/proc/<pid>/maps`. |
| **Zygisk module not injecting** | USAP pool ON, app under umount, or ZN flaky skip | Verify `device_config get runtime_native usap_pool_enabled` → `false`. Check umount toggle in KSU manager. Force-stop the app + relaunch. |
| **OTA flash script prompts hang** | `set /p` doesn't accept pipe on cmd.exe | Use `windows_install_upgrade_auto.bat` from `scripts/flash/`. |
| **`adb root` not working** | KSU manager setting off, or `--allow-shell` not used during `boot-patch` | Enable "adb root" in manager. If the patched image was built without `--allow-shell`, re-patch and re-flash. |
| **`/sdcard` path not found on push/pull** | adbd namespace under KSU | Use `/storage/emulated/0/...` instead. |
| **Slot _b won't boot after tests** | `vendor_boot`/`dtbo` from old HyperOS CN on that slot | Flash the ROM script (it writes both slots). After the first flash, _b is normalized. |

### 9.1 Recovery commands (keep handy)

```bash
# Reflash stock init_boot (no root)
fastboot flash init_boot_a init_boot_stock_a.img
fastboot reboot

# Reflash current patched init_boot (root back)
fastboot flash init_boot_a backup\ksu-migration\init_boot-317-ksu-hide.img
fastboot reboot

# Check active slot
adb shell getprop ro.boot.slot_suffix

# Switch slot (resets retry-count — use underscore)
fastboot set_active a
```

---

## 10. Reference — architecture of this stack

```
┌──────────────────────────────────────────┐
│  APPS                                    │
│  ┌─────────┐ ┌──────────┐ ┌───────────┐ │
│  │ Wallet  │ │ Banking  │ │ Root apps │ │
│  │ (umount)│ │ (umount) │ │ (injected)│ │
│  └────┬────┘ └────┬─────┘ └─────┬─────┘ │
│       │           │              │       │
│  ┌────▼───────────▼──────────────▼─────┐ │
│  │  ZygiskNext 1.4.3 (enforce)        │ │
│  │  ┌──────────┐ ┌──────────────────┐  │ │
│  │  │ PIF v17  │ │ DeviceID+ v2.3.1 │  │ │
│  │  └──────────┘ └──────────────────┘  │ │
│  └────────────────┬───────────────────┘ │
│                   │                     │
│  ┌────────────────▼───────────────────┐ │
│  │  TrickyStore v1.4.1 + keybox      │ │
│  │  (intercepts key attestation)      │ │
│  └────────────────┬───────────────────┘ │
│                   │                     │
│  ┌────────────────▼───────────────────┐ │
│  │  HMA-OSS oss-164                   │ │
│  │  (applist filter in system_server) │ │
│  └────────────────┬───────────────────┘ │
├───────────────────┼─────────────────────┤
│  KERNEL           │                     │
│  ┌────────────────▼───────────────────┐ │
│  │  KernelSU LKM 32558               │ │
│  │  (hide-lkm fork: kobject_del)     │ │
│  │  umount modules per namespace     │ │
│  └────────────────┬───────────────────┘ │
│                   │                     │
│  ┌────────────────▼───────────────────┐ │
│  │  Stock kernel 6.12.23-abogki4639  │ │
│  │  (boot.img — untouched, signed)   │ │
│  └────────────────────────────────────┘ │
│                   │                     │
│  ┌────────────────▼───────────────────┐ │
│  │  init_boot (KSU patched)           │ │
│  │  ksuinit → insmod .ko at boot     │ │
│  │  NOT in vbmeta chain → AVB passes │ │
│  └────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

---

## Further reading

- [`docs/SESSION-HANDOVER.md`](docs/SESSION-HANDOVER.md) — living document: current state, all 32 field-proven facts, golden rules, session arc (13 sessions)
- [`docs/relatorio-sessao-2026-07-22.md`](docs/relatorio-sessao-2026-07-22.md) — the KSU migration journey (Magisk→KSU, AVB wall, init_boot door, live `.ko` proof)
- [`docs/relatorio-sessao-2026-07-27.md`](docs/relatorio-sessao-2026-07-27.md) — HMA-OSS deployment, YT Music Morphe on Android Auto
- [`docs/relatorio-sessao-2026-07-28.md`](docs/relatorio-sessao-2026-07-28.md) — BYD digital key front reopened, DeviceID+ v2.3.1 native DCK hook
