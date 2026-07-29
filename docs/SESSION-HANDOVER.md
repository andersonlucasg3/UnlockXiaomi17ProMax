# SESSION HANDOVER — UnlockXiaomi (popsicle)
**Living continuity document between sessions. Last updated: 27/Jul/2026 (Session 12 — YT Music Morphe on Android Auto ✅). Detailed chronological history: `docs/relatorio-sessao-2026-07-22.md` (21–22/Jul, KSU root), `docs/relatorio-sessao-2026-07-27.md` (27/Jul, YT Music AA).**

---

## PART 1 — CURRENT STATE (snapshot 24/Jul/2026 ~23:45)

### 1.1 Device and ROM
- **Device:** Xiaomi 17 Pro Max (`popsicle`, 2509FPN0BC), SD 8 Elite Gen 5, Android 16 (SDK 36)
- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM**
- **Kernel:** stock `6.12.23-android16-5-...-abogki463945075-4k` (untouched)
- **Bootloader:** unlocked (NEVER re-lock)
- **Recovery:** TWRP 3.7.1 unofficial (variant `fix22ZX_pinwork_partialdecryption`) — kept as safety net, decrypts /data with PIN (saved us from the 2 Caixa bootloops)

### 1.2 Root and stack
- **KernelSU LKM** driver **32558** (fork `andersonlucasg3/KernelSU` branch `hide-lkm`, patch `9ea26f3`: `kobject_del`+`list_del` — module **invisible** in `/proc/modules` and `/sys/module`, build via Actions run 30156878115; `.ko` in `tools/ksu_apk/`, image in `backup\ksu-migration\init_boot-317-ksu-hide.img` sha `2079b885…`) in `init_boot`; manager `me.weishu.kernelsu` **32562 (v3.2.4-36)** — updated on 24/Jul via `pm install` from the backslashxx release APK (in `tools/ksu_apk/`), **adb root ON** (also works via Wi-Fi: `adb tcpip 5555` → `adb connect <ip>:5555`; does not persist across reboot)
- **ZygiskNext 1.4.3-817** (enforce-denylist; `modules64: deviceidchanger, playintegrityfix`)
- **USAP pool DISABLED** (facts 19/20): `device_config put runtime_native usap_pool_enabled false` (persists) + `persist.sys.usap_pool_enabled=false` + `setprop dalvik.vm.usap_pool_enabled false` (volatile). **Petal front closed 24/Jul — reactivation is an open decision** (risk: flaky ZN injection returns; requires reboot)
- **PlayIntegrityFork v17** with `custom.pif.prop` = **Pixel 10 (frankel, Canary ZP11.260618.005 — expires 2026-08-19, run PIF Action to renew)**
- **TrickyStore v1.4.1** (keybox DroidWin v3.6 + security_patch.txt=2026-07-05; target.txt includes the 3 Caixa packages + `br.com.gabba.Caixa`)
- **Umount global** (except gms/vending/termux **and `com.huawei.maps.app`** — see Part 5, fact 16). **BYD app:** umount was turned off in Session 9 for injection (DCK front) — re-enable in manager if not yet done
- **HMA-OSS oss-164** (installed 27/Jul — Part 2.5): filters applist in system_server; `bancos` template (empty whitelist) applied to `br.com.bradseg.bscelular` and `com.revolut.revolut`; config at `/data/misc/hide_my_applist_hmaosspreseedab/config.json`
- **COPG 5.9.0** — **REMOVED by user on 24/Jul** (DeviceID+ COW covers the Petal case; was belt-and-suspenders)
- **Play Integrity: 3/3 ✅**

### 1.3 Apps
| App | Status |
|---|---|
| Google Wallet | ✅ |
| Caixa / BR banks | ✅ **RESOLVED 23/Jul ~21:00** (Part 3) |
| Revolut | ✅ **RESOLVED 27/Jul ~11:35** — same vector/fix as bradseguros: HMA-OSS (Part 2.5). Leaked keybox hypothesis DISPROVEN |
| Bradesco Seguros | ✅ **RESOLVED 27/Jul ~11:20** — vector: package enumeration; fix: HMA-OSS (Part 2.5) |
| **Petal Maps 4.7.0.319** | ✅ **RESOLVED 24/Jul ~18:20** — COW prop_area validated: `Get Manufacturer: HUAWEI`, app passes the gate (Part 2.2) |
| **YT Music Morphe 9.15.51 (AA)** | ✅ **RESOLVED 27/Jul** — podcasts work on Android Auto; music requires YouTube Premium (server-side, ReVanced#6185). Fix: `tcn.c→false` (Dynamite bypass) + 37 CLI patches (Part 2.6 / Session 12) |

### 1.4 DeviceID+ Module (own fork)
- **Location:** `modules/deviceidchanger/` (AGPL fork of sidex15/deviceidchanger, credits in README/LICENSE). Zip rebuild: `native/build.sh` (the `.so`) + zip the `module/` folder (zip gitignored; `build_zip.ps1` on Windows). build.sh targets: no arg = module; `test` = smoke test `test_hook`; `inspect` = standalone inspector in /data/local/tmp
- **Versions:** v2.1.1 **installed on device 24/Jul ~18:17** (zip rebuilt **with the COW `.so` included** — the old zip lacked the `.so` and would have removed the spoof on update; the deployed `.so` was pulled from the device into `module/zygisk/arm64-v8a.so` before rebuild) + live config re-merged into staged. **Native v2.2.0-dev: prop_area COW VALIDATED 24/Jul ~18:20 (Petal) + `Build.*` spoof via JNI added and deployed ~19:50 (hash `06dcaf4c…`)** — field-validated on gms/BYD app (DCK front) and on Petal (23:21). Pending: bump v2.2.0 + native commit (prop_cow.cpp, Build.* JNI etc., still uncommitted)
- **Features:** Per-app SSAID (lists all packages, checkbox enroll, shared global ID OR custom per app, regen for both, backup/restore + anti-bootloop validation) · persistent global prop spoof (service.sh post-boot, `ro.build.host=c3-miui-ota-bd110`) · **per-app prop spoof** — three complementary mechanisms: (1) **prop_area COW** (`prop_cow.cpp`: copies prop pages to a private mapping and rewrites the value in-place using bionic's serial protocol — covers ALL read paths: JNI, native, direct parse, static-linked); (2) GOT/PLT hook of the 3 bionic functions (`perapp_hooks.cpp`, complement for dynamic readers); (3) **`android.os.Build.*` spoof via JNI** (`deviceid_zygisk.cpp`: rewrites the MODEL/DEVICE/PRODUCT/BRAND/MANUFACTURER/etc. static fields in the app process in postSpecialize — needed because the Build class is initialized in the zygote with the real values and neither COW nor hooks reach it; same technique as PIF). Flat config `.perapp_props` lines `pkg|key=value` — **matched by PROCESS NAME (nice_name), not package** (fact 25); applied with app force-stop, no reboot; stealth unload on non-target apps · TrickyStore target.txt editor
- **⚠️ KSU module update overwrites the entire dir** (`/data/adb/modules/deviceidchanger/`) → always re-merge `config.json` + `.props_*` + `.perapp_props` into staged (`/data/adb/modules_update/...`) before reboot
- **⚠️ NEVER hot-swap the zygisk `.so`** — ZN caches the entry offset in the zygote; swapping the file without reboot crashes every injected app (fact 20)

### 1.5 Rollback and recovery
| Item | Path | sha256 |
|---|---|---|
| init_boot Magisk 30.7 (full rollback) | `backup\ksu-migration\init_boot_a_backup.img` | `c951cdf4…` |
| boot stock 315 | `backup\ksu-migration\boot_a_backup.img` | `6c48dd3f…` |
| init_boot KSU 317 (quick re-flash) | `backup\ksu-migration\init_boot-317-ksu.img` | `68f996d9…` |
| init_boot KSU 317 **+hide** (current) | `backup\ksu-migration\init_boot-317-ksu-hide.img` | `2079b885…` |
| vbmeta original (flags=0) | `rom\popsicle_eu_3.0.315\images\vbmeta.img` | — |
| SSAID pre-randomize | `backup\settings_ssaid-pre-randomize.xml` | — |

### 1.6 Git/GitHub
- Repo: `github.com/andersonlucasg3/UnlockXiaomi` (branch `main`, push via authenticated `gh`)
- Local git identity configured in the repo (`user.name`/`user.email` — previously failed with "Author identity unknown"). Direct push required `gh auth setup-git` (done on 24/Jul)
- `tools/` kept untracked (large binaries). `.gitattributes`: `*.sh` always LF. `.gitignore`: `*.so`, `*.zip`, `native/test_hook` (regeneratable binaries stay out of git)

---

## PART 2 — PENDING ITEMS

### 2.1 BYD digital key (⏸️ CLOSED 24/Jul ~23:30 — client-side exhausted; verdict: Google gate with no known workaround)
**Goal:** provision the BYD digital key (Destroyer 05/King BR) on the phone. **Final state: blocked at GMS DCK `downloadAllowed` — production GMS does not apply phenotype overrides through any channel (all tested); server does not serve DCK config for this model. Full rollback performed** (see end of Session 9). Lasting legacy: DeviceID+ with `Build.*` spoof (JNI), `ro.gms.dck.eligible_wcc` prop documented, and the full DCK map below. If Google ever whitelists this model, resume from here.

**Facts (device):**
- Hardware OK (verified on device): `nfc.ese` + `nfc.uicc` + OMAPI (`android.hardware.se.omapi.ese.xml`), `android.hardware.uwb`, HAL `secure_element-service.qti` and `com.android.se` running
- **Experiment applied:** PIF `action.sh` run → gms/vending now = **Pixel 10 (frankel)** via `/data/adb/modules/playintegrityfix/custom.pif.prop`
- **Experiment rollback:** delete `custom.pif.prop` + force-stop gms/vending (PIF falls back to internal defaults that gave PI 3/3)
- ⚠️ Check Play Integrity after retest (new print may change the verdict)

**Research on 24/Jul (conclusions — sources: support.google.com/wallet/answer/12060041, /11358016, /13037118, byd.com/br/chave-digital, byd.com/eu, dolphinbyd.com.br forum threads 4976/2935, Reddit r/BYD 1on9yem/1uhy20t/1usp25c):**
- **The block is from Google Wallet/Google, server-side, by model+ROM** — NOT region (BR is supported by the BYD app) nor integrity. The public list is short ("Pixel 6+, S21+, and some Android 12+") but the real one is server-side and unpublished; toggles server-side without app update (evidence: Xiaomi 15T Pro and 15 Ultra started working overnight).
- **Depends on Google's "digital key API" embedded in the ROM by the OEM** — Xiaomi only included it in recent HyperOS builds, model by model ("The HyperOS update to 3.0.3 enables Google's digital key API"). **Real risk: xiaomi.eu may not include this API** (and Lineage/Graphene/Huawei don't have it).
- Hardware: NFC+eSE is enough; **UWB is optional** — BYD is NFC-only (all BYDs on the Wikipedia table are "NFC"; homologated Xiaomis 12→14 don't have UWB).
- BYD's actual list (BR forum, Apr/2025): Samsung S20→S25/Note20/Z/A56/A36, **Xiaomi 12/12 Pro/13/13 Pro/13 Ultra/13T/13T Pro/14/14 Ultra**, OPPO Find X8/Pro. **No Xiaomi 17** — 17/17 Ultra owners on Reddit say it still doesn't work (but there is an isolated report of a 17 working).
- Restriction is **dual**: device must be on Google's list AND BYD's list. The error in our case comes from the Wallet step = Google block.
- **PIF spoof to Pixel 10 does not solve it**: injects Build.* only in DroidGuard/attestation; car key eligibility is decided server-side against the real model that GMS reports.
- **No documented workaround** (LSPosed/prop spoof) with proven success for car key — uncharted territory.
- Risk even if provisioned: silent key revocation on integrity re-checks (BMW + Xiaomi 15 case).
- **Samsung Wallet route DISPROVEN (research 24/Jul, 3 agents):** Samsung Wallet requires Galaxy hardware + One UI framework + server-side validated Samsung account (rejects non-Samsung since 2022, "ID not valid"); Samsung digital key lives in Galaxy eSE with Knox attestation (unique SAK per device in TrustZone, tied to IMEI+serial — NO batch keybox like TrickyStore for Samsung); KnoxPatch (state of the art, Samsung hardware only) marks Wallet/Pay as ❌ ("checks run in TEE, signed trustlets — requires TrustZone exploit"); key sharing from a Galaxy to the Xiaomi fails (recipient is also server-side checked). Sources: github.com/salvogiangri/KnoxPatch (+issue #43), docs.samsungknox.com/dev/knox-attestation, xda-developers.com/samsung-pay-not-working-non-samsung-phones, news.samsung.com (Digital Key = eSE), dolphinbyd t/4976.
- **🆕 "Xiaomi 17 works" signal DEBUNKED (deep research 24/Jul):** the r/BYD post was **hearsay** (Vivo X300 thread, 28/Jun: "I saw a recent post...", no link/model/ROM). There is NO first-hand report of BYD working on any Xiaomi 17 as of 24/Jul — only failures (17 and 17 Ultra with BYD M6, Mar/Apr/2026). The only real success case is **BMW i4 + Xiaomi 17 base via Google Wallet** (May/2026, r/BMWI4 1t5p111): proves that Google's digital key API EXISTS on the 17 family and that Google has already whitelisted the 17 base. **Google's official list (android.com/digital-car-key, verified 24/Jul): "Xiaomi 12&12 Pro, 13…, 15&15 Ultra, 15T&15T Pro, 17 & 17 Ultra, MIX Flip, Poco F7/F8 Ultra" — does NOT include 17 Pro or 17 Pro Max** (popsicle is a China-only model, 2509FPN0BC, no Global variant). Whitelist is per model+manufacturer; BYD and BMW are separate lists. There is no AOSP feature (`android.hardware.digital_key` does not exist) — DCK lives 100% in Play services/Wallet and eligibility is server-side by model identity. Server-side flips happen (15T Pro Uruguay flipped ~08/Jul without update). **Open: test whether PIF spoofing gms to a whitelisted model (17 base or 17 Ultra, not Pixel) moves the Wallet gate** — eligibility reads the identity GMS reports; PIF already injects gms/vending.
**Progress 24/Jul evening (Session 9 — BYD app RE + DCK gating discovered):**
- **BYD app RE** (decompiled sources in `analysis/byd/out2/sources/`): the compatibility check calls `DigitalKeyFramework.getClient(ctx).isCreateDigitalKeyPossible()` — **the verdict comes from GMS (Google's DCK module)**, not a local list. The app only sends `deviceManufacturer` (code: xiaomi=0002, via Build.MANUFACTURER — already correct) to BYD. The "supported models" screen is the `catalogPage` help page opened when the GMS check fails. Main logic obfuscated via JNI (`com.fort.andjni`).
- **DCK gating in GMS (logcat, tag `Dck`, service_id=289):** `[WirelessCapabilitiesFeatures] wccSysProp: 0` (unknown prop, int 0-3, default 0) + `wccOverride: not set` → `hasWccSupport: false` → `downloadAllowed: false` → full DCK module never downloads ("Initializing as WCC1"). **WCC = CCC capability class: 1=NFC, 2=NFC+BLE, 3=NFC+BLE+UWB.**
- **Important new fact (ZN):** the `.perapp_props` match is by **process name** (nice_name), not package — `com.google.android.gms` only covers the main process. Chimera DCK runs in **`com.google.android.gms.persistent`** (needs its own lines in config). `.unstable` is INTENTIONALLY LEFT OUT (DroidGuard/PIF Pixel 10; PI 3/3 maintained).
- **Aurora spoof (14 Ultra: model=24030PN60G/device/name=aurora/marketname)** applied via COW + Build.* (new JNI in module) on: gms, gms.persistent, walletnfcrel (no effect — wallet under umount is not injected), bydautolink (had to **turn off umount in manager UI**). Even with everything spoofed: incompatible → verdict depends on wcc, not model.
- **Phenotype override applied:** `DckFeatureMain__wcc_override=3` inserted into `/data/data/com.google.android.gms/databases/phenotype.db` (`flag_overrides` id 18 + `flag_overrides_to_commit`, config_package_id **231** = `com.google.android.gms.dck`; db backup in `/data/local/tmp/phenotype.db*`). Decoded format of existing overrides (Android Auto/DiLink ones, ids 1-17): type 1=int, 4=string, account_id=0. **Commit does not trigger with force-stop — likely trigger is boot.** Broadcast `com.google.android.gms.phenotype.FLAG_OVERRIDE` exists (--es package/flag/type/value) as an alternative. ⚠️ New GMS **reverts overrides in ≤24h** (signed configs) — may need periodic re-application.
- **BYD app wiped + spoof active: same verdict** (verdict is neither local cache nor reported model).
- Research: no public mention of `wccSysProp`/`wccOverride` — prop name only via GMS decompilation (class `WirelessCapabilitiesFeatures`).
- ⏳ **NEXT:** decide route for `downloadAllowed` (see below — Session 9 part 2).

**Session 9 part 2 (24/Jul evening — DCK rock bottom):**
- **`wccSysProp` = `ro.gms.dck.eligible_wcc`** (int 0-3, default 0) — source: `defpackage/bsst.java` (classes6.dex of gms base, jadx on-device). **SET live via `setprop` (works because the prop didn't exist) and persisted in DeviceID+ `.props_spoof` (service.sh boot_completed+5s)** → `wccSysProp: 3` in log, `hasWccSupport` passed. **Even so, BYD app still blocked.**
- **Remaining gate = `downloadAllowed` = flag `DckStub__full_module_download_allowed`** (bool, default false) — source: `defpackage/jycg.java` (classes15.dex). Module eligibility: `bsog.b()` = `wcc>0 && downloadAllowed`. Stub's other flags: `DckStub__are_flags_synced` (default false!), `DckStub__disable_dck_support`. wcc override = `DckFeatureMain__wcc_override` (long, default -1; jybv.java). gtwx registers package `com.google.android.gms.dck` (config_package_id **231**, params EMPTY — server does not serve DCK for this model).
- **New phenotype schema (db v1033+) decoded** (classes8: fkch/fkee/fjzr): override merge requires link in `experiment_states_to_overrides` with the package's `committed_experiment_state_id` (dck = 4372). Applied: overrides id 18 (`DckFeatureMain__wcc_override=3` type 1), 19 (`DckStub__full_module_download_allowed=1` type 0), 20 (`DckStub__are_flags_synced=1`) + links to 4372 + `flag_overrides_to_commit`. **NOTHING applies** — the read operation is called `getCommittedOverridesPhixit` ("Phixit" = internal debug tool; likely overrides only work in dogfood/debug flow, and production gtwx reads only served config). Broadcast `com.google.android.gms.phenotype.FLAG_OVERRIDE` returns 0 with no effect. XML `gms_chimera_phenotype_flags.xml` is write-only cache (edits ignored). **The old Android Auto overrides (ids 1-17) have NO link in experiment_states_to_overrides — possibly never applied via phenotype.**
- phenotype.db backup in `/data/local/tmp/phenotype.db*`. GMS sources decompiled on-device: `/data/local/tmp/gmsout{,8,15}/`; key classes copied to `analysis/byd/*.java` in repo.
- **Possible routes for downloadAllowed:** (a) **GMS Phixit** (polodarb, root app built for the new schema — calls the official op; revert ≤24h due to signed configs, needs re-apply); (b) call the binder op `SetFlagOverridesOperation` directly; (c) accept there may be an additional server-side gate (model allowlist on module download — `downloadAllowed` may be decided server-side, not just locally).

**Session 9 part 3 (24/Jul ~23:00 — overrides exhausted, empirical verdict):**
- **GMS Phixit tested** (fork jcrutch-design/GMS-Phixit-Android17 v1.5, sha256 `8b0fc972…`, installed as `ua.polodarb.gmsphixit`): wrote the full DCK flag registry (~90 overrides, ids 21-109, incl. `DckStub__full_module_download_allowed=1` and `DckStub__are_flags_synced=1`) into `flag_overrides` + `flag_overrides_to_commit`. **No effect on production read path.**
- Manual complements tested, all without effect: links from ALL dck overrides → `experiment_states_to_overrides` in committed state 4372 (92 links); mirror overrides in **account 1** (user) + links in state 3889. `downloadAllowed: false` persisted in all.
- **Verdict: in production GMS 26.28.60 (262860035), gtwx does NOT apply phenotype overrides through any known local channel** (db, links, Phixit, FLAG_OVERRIDE broadcast, chimera XML). The real gtwx read path (gtvd/gtwx in classes.dex, not yet decompiled) likely reads only the served config — and the server does not serve DCK for this model (empty params).
- **Possible next steps (not executed):** (1) decompile `classes.dex` (gtvd/gtwx client) to find the real read path (there may be a snapshot cache to invalidate); (2) protobuf surgery on the served `experiment_token`/`params`; (3) accept server-side gate.
- Good final state: `ro.gms.dck.eligible_wcc=3` ACTIVE + persisted in `.props_spoof` (DeviceID+ service.sh); wcc=3 read on every boot. dck overrides remain in db (inert) — backup in `/data/local/tmp/phenotype.db*`.

**ROLLBACK (24/Jul ~23:30, at user request — front closed):** `.perapp_props` restored (Petal only); `ro.gms.dck.eligible_wcc` removed from `.props_spoof` and deleted live; ALL dck overrides/links deleted from phenotype.db (0 remaining, backup deleted); `gms_chimera_phenotype_flags.xml` restored from backup; **GMS Phixit uninstalled**; `/data/local/tmp` cleaned of session artifacts; gms restarted and verified WITHOUT injection/spoof; `analysis/byd/` in repo pruned (only the analysis `.java` files kept: bsst/bsog/jy*/fj*/fk*/gtwx). **Manual pending: re-enable "Umount modules" for BYD app in KSU manager** (was turned off for injection). DeviceID+ `.so` with Build.* spoof (v2.2.0-dev) REMAINS (valid, harmless feature). Bump v2.2.0 + native commit still pending.
- BYD app check architecture: `DigitalKeyHelper.t()` → `isCreateDigitalKeyPossible()` (GMS); error displayed in WebView (`catalogPage`). App sends `deviceManufacturer` (0002=xiaomi) to BYD server. Samsung check: `com.samsung.android.dkey` + content provider (not applicable). "China devices should not use Google DCK" check (bmms) PASSES (does not appear in log).

### 2.2 Petal Maps (✅ RESOLVED 24/Jul ~18:20 — COW prop_area field-validated)
**Goal:** run Petal Maps ≥4.7.0.316 — since that version the app requires a Huawei device. Installed: **4.7.0.319** (sideload APKCombo, sha256 `fc1ebc0f…d3ad3`). HMS Core (`com.huawei.hwid`) and AppGallery were already on the device.

**Reverse engineering (facts, own decompilation of 8 APKs — artifacts in `/data/data/com.termux/files/usr/tmp/petal/`):**
- Block introduced in **4.7.0.316** (04/May/2026); **4.7.0.315 is the last without the block**.
- Single, local check, in `SplashActivity` (`onCreate`/`onResume`): `tp2.g()` reads **`ro.product.manufacturer` via reflection on `android.os.SystemProperties.get(String)`** and requires exact `"HUAWEI"` (decompiled source reviewed 24/Jul: `out319/sources/defpackage/tp2.java` — confirmed; log emitted: tag `HmsMapApp_M_EnvironmentUtil`, msg `Get Manufacturer: <value>`). Failure → non-dismissable dialog "Petal Maps is only available for Huawei devices" → `killProcess`.
- No server-side attestation, no HMS Core check at the gate, no brand/model/emui check in the blocking path.

**Path taken on 24/Jul (Session 8 — chronological):**
1. **ROOT BUG #1 found and fixed:** the `my___system_property_read`/`read_callback` hooks checked `__system_property_read` return with `== 0`, but bionic returns the **VALUE LENGTH** (≥ 0) → spoof was never applied on those paths. Fix `>= 0` + return the spoofed len (extended smoke test covers get/read/read_callback).
2. **SIGILL/SIGSEGV in every injected app (2 incidents):** caused by **hot-swapping the module `.so`** — ZN caches the module entry offset in the zygote; the new file has the entry at a different offset → jump into padding. Reboot fixes (and only reboot). **New rule: zygisk `.so` only swapped with reboot.**
3. **USAP pool silently breaks ZN 1.4.3 injection** (`.so` mapped, entry never called — analogous documented case: NeoZygisk#73). Pool disabled (see 1.2) → stable injection.
4. **GOT patching does not reach the check path:** with hooks proven installed (verbose log: write+readback OK at the correct slot of `libandroid_runtime`), the check read `Xiaomi` ~350 ms later. Filtered trace showed incidental queries (`ro.build.version.sdk`, `ro.product.board`) via `get`, but **NO query of `ro.product.manufacturer` by any of the 3 hooked symbols** — MIUI's JNI reads the prop through a path that does not go through the patched slots (AOSP A16 uses `__system_property_find`+`read_callback`; MIUI apparently a different route). Conclusion: GOT hooks are insufficient here.
5. **Solution implemented — prop_area COW (like COPG `:cow`, which is exactly what COPG PRO does):** `prop_cow.cpp` copies the prop_area pages containing the prop to a private anonymous mapping at the SAME address (per-process) and rewrites the value in-place (bionic serial protocol). Covers JNI/native/direct parse/static-linked. Smoke test PASS: raw `__system_property_get` returns `HUAWEI` with no hooks active.
6. ✅ **VALIDATED 24/Jul ~18:20** (post-reboot from v2.1.1 install): log `Get Manufacturer: HUAWEI`, app passes the gate and opens (PrivacyActivity). Temporary Termux config removed from `.perapp_props` (only `com.huawei.maps.app|ro.product.manufacturer=HUAWEI` remains). Pending: bump v2.2.0 + native commit; **open decision: re-enable USAP pool** (fact 19 — re-enabling may reintroduce flaky injection; requires reboot for full effect).
7. **ZN residual flakiness:** even with USAP off, there was a launch (Termux pid 5065) where the module entry was not called (no crash, no log). Investigate if persistent; practical mitigation: force-stop + relaunch.

**Reference sources (research 24/Jul):** AOSP `android_os_SystemProperties.cpp` (android16-release: JNI = find+read_callback, no ART intrinsics); COPG changelog v5.3.0 (COW replaces GOT hooking; v5.4.0: caveat for props >91 chars on A16 — our key is short, OK); KernelSU PR #3470 (static-linked readers bypass hooks); NeoZygisk#73 (USAP doesn't inject); ZN wiki FAQ (safe mode after zygote crash; KSU "Umount modules" = denylist for ZN).

**Available Plans B/C (not executed):**
- **APK patch:** force `tp2.g()` to `return true` in smali + re-sign (java 21/aapt/apksigner ok on Termux; apktool absent; smali/baksmali jar runs). Breaks update chain (re-patch per version) + small risk with HMS. Decompiled sources in `usr/tmp/petal/out319/sources`.
- **COPG PRO** (`:cow` prop spoof) — paid; our open implementation does the equivalent.
- Stay on **4.7.0.315** (no new features).

### 2.3 Install DeviceID+ v2.1.1 on the device — ✅ DONE 24/Jul ~18:17

### 2.5 Bradesco Seguros `br.com.bradseg.bscelular` (✅ RESOLVED 27/Jul ~11:20 — HMA-OSS)
**Symptom:** same block screen as Revolut ("unsecure environment", dialog with OK). App installed 27/Jul 08:27 (v2.85.0).

**Proven facts:**
- **Protector = DexProtector/Licel** (`lib/arm64-v8a/libdexprotector.so` in arm64 split). Process renamed `:p<hex>` (fact 32). APKs and logs in `analysis/bradseguros/`.
- **Was ABSENT from TrickyStore target.txt** (Revolut is present). Added 27/Jul ~09:44 — **no effect on verdict**.
- **After `pm clear`: crash with `android.app.TerminateException$<obfuscated>`** (FATAL EXCEPTION main — deliberate DexProtector kill) — **WITH and WITHOUT the TS daemon running** (spoofed attestation doesn't change the verdict; real TEE likewise).
- **App does NOT create keystore aliases** (nothing from uid 10363 in `/data/misc/keystore/user_0/`) → persistent local attestation unlikely.
- **Visible root packages installed:** `me.weishu.kernelsu` (manager), `com.termux`, `app.morphe.manager`, `app.pwhs.universalinstaller` (enumeration via raw Binder is candidate #1; fact 32).
- APK RE in progress (decompilation `analysis/bradseguros/out/`) + research on package hiding without Xposed.

**RESOLUTION (27/Jul ~11:20, confirmed by user — "It worked"):** **HMA-OSS oss-164** (zip in `tools/hma_oss/`, sha256 `4bf157db…`, id `hma_oss_zygisk`, manager `org.frknkrc44.hma_oss`) filters applist in system_server (covers raw Binder). Config at `/data/misc/hide_my_applist_hmaosspreseedab/config.json` (the service reuses the first `hide_my_applist*` dir it finds in /data/misc; it was pre-seeded and adopted it): `bancos` template = empty whitelist (target app sees NO user apps) + per-app scope. **HMA-OSS lessons:** (1) config only applies LIVE via manager app (ServiceClient — the service reads the file only on boot; editing the JSON on disk without going through the manager has no effect); (2) "Enable" alone creates scope in **empty blacklist (= hides nothing)** — you must turn on "Hide" mode (whitelist) AND apply the template; (3) decoded source format (`JsonConfig.kt`, CONFIG_VERSION=93); (4) log at `<datadir>/log/runtime.log` shows `@shouldFilterApplication: query from <pkg>` — proof of filtering. **Revolut:** same fix applied ~11:35 (user confirmed) — keybox hypothesis disproven (Part 2.4).

**⚠️ TrickyStore lesson (27/Jul):** the TS daemon has **anti-tamper** — checks module file integrity; editing `service.sh` (e.g. DEBUG=true) causes the daemon to die with **silent exit 1**. NEVER edit TS module files. Manual daemon restart: `cd /data/adb/modules/tricky_store && (setsid sh ./service.sh >/data/local/tmp/ts.log 2>&1 </dev/null &)`. NEVER `pkill -f TrickyStore` via adb shell (the shell's own cmdline contains the string — kills the shell).

### 2.4 Documented technical debt
- **Revolut (✅ RESOLVED 27/Jul ~11:35 — Session 11; Session 10 diagnosis below kept as reference):** the real cause was **package enumeration** (not the keybox!) — resolved with HMA-OSS, `bancos` template (Part 2.5). Note: the app was updated to 10.140 that day, but the decisive variable was the applist (filtered query logged + app passed). **Original Session 10 text:** splash "environment is not secure" (LOCAL RASP verdict, DexProtector/Licel confirmed by research). Eliminated as causes: mounts, PI 3/3, TS attestation, `/system/bin/su` (adb root off), frida-server, `/proc/modules`+`/sys/module` ksu (**permanently solved via `.ko` with hide — see 1.2**), `/proc/kallsyms` (clean), prctl (driver does not respond — fact 30), ADB, new SSAID + clear data, frozen manager. **Zygisk injection in the app is INFEASIBLE** (maps scan → instant `MessageGuardException`). **Suspect #1 (research): leaked DroidWin keybox — Revolut rejects popular keyboxes even with PI 3/3** (XDA guide 4773849: PI 3/3 + burned keybox = block; swap keybox = works). Next steps: (1) private/non-leaked keybox; (2) HMA-OSS/HMAL whitelist (DexProtector enumerates packages via raw Binder); (3) TEESimulator (attention: malformed attestation = MessageGuard); (4) attestation↔Build consistency (frankel×Xiaomi) is a plausible but unconfirmed hypothesis. SuSFS = **structurally impossible on this device** (VFS built-in, requires boot.img — fact 31).
- **Keybox treadmill:** swap `/data/adb/tricky_store/keybox.xml` when revoked + reboot
- **PIF print expires 2026-08-19:** run PIF Action to renew before then
- **System app updater:** RESOLVED — user uninstalled the update app (was by design, EU ≠ Xiaomi signature — see Part 6.E)

---

## PART 3 — CAIXA FRONT (RESOLVED 23/Jul ~21:00 — reference)

**Combination that unlocked it:** shared SSAID across the 3 apps + `br.com.gabba.Caixa` in TrickyStore target.txt + persistent spoof `ro.build.host=c3-miui-ota-bd110` (service.sh at boot_completed+5s) + ADB/dev off when using the app (manual).

**Detector identified (reverse engineering):** SDK **CashShield** (`libcashshieldptr-native-lib.so`, present in superapp AND Gabba) — root/xposed paths, props (ro.build.host/tags/debuggable/service.adb.root), Frida/hooks, `which su`, collects MediaDRM/GAID/AndroidID → server-side verdict. App = React Native/Expo (Module Federation). Gabba = security companion app (OpenCV for documents, iProov/Oz for liveness).

**Lessons from the 2 bootloops (fixes in module, commits `67a508b`/`7b961c4`):**
1. `settings_ssaid.xml` has `<namespaceHashes/>` AFTER `</settings>` — any edit that removes/re-adds the closing tag swallows it into `<settings>` → system_server dies → bootloop. Module apply now inserts via awk before the closing tag + automatic backup + post-encode validation with restore.
2. `resetprop` in post-fs-data DOES bootloop (live with the system up it works) — spoof runs in service.sh after boot_completed.

**ADB/Dev watcher:** REMOVED from module at user request (battery + detection window at launch is unbeatable by polling). ADB/dev = manual control. Hysteresis implemented and discarded is in git history (`98315b3`).

---

## PART 4 — OPERATIONAL PLAYBOOK (what works in this setup)

- **OTA flash:** `scripts/flash/windows_install_upgrade_auto.bat` (or the ROM's .bat with prompt answered manually) → stock reboot → `ksud boot-patch -b <ROM_init_boot> -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell -o <dir>` → `fastboot flash init_boot_a <patched>` → reboot. (`ksud boot-patch` runs **without root**, only needs the output dir to exist.)
- **Slot:** `fastboot set_active a|b` (underscore!). Partition verification: `sha256sum /dev/block/by-name/<part>` via `su -mm` (shell su has flaky namespace; `-mm` fixes it).
- **Daemon-context (Magisk) to /data/adb:** micro-module with customize.sh (builders `tools\build_*_module.py` — swap SCRIPT_PROP/CUSTOMIZE_SH and generate KSU/Magisk-compatible zip via TrickyStore or Shamiko donor zip).
- **State commands:** `ksud debug version` (KSU driver), `znctl status` (ZN — shows loaded modules and inject_state), `ksud module list` (JSON), `update_engine_client --help`.
- **Push/pull:** always `/storage/emulated/0/...`.
- **Two devices on adb:** use `adb -s 4d7fc9af` (an emulator-5554 sometimes shows up).
- **Pipes on Windows cmd:** avoid `|` inside `su -c "..."` (breaks) — use script files or separate commands; avoid escaped `\$` (passes literal); `$(...)` without escape works.
- **Git on Termux:** identity already configured in repo; push requires `gh auth setup-git` once (done). If "divergent branches": `git pull --rebase origin main`.
- **Zygisk .so build:** `cd modules/deviceidchanger/native && ./build.sh` (Termux clang 21, outputs to `module/zygisk/arm64-v8a.so`; `./build.sh test` compiles the `test_hook` smoke test — run as root; `./build.sh inspect` generates standalone inspector in /data/local/tmp). Verify DT_NEEDED only with system libs (build.sh already fails if dirty). **Deploying the .so requires reboot** (fact 20). On adb su, export `PATH=/data/data/com.termux/files/usr/bin:$PATH` first.
- **App diagnostics:** `logcat -b all -c` → launch app → `logcat -d -b all > file`. Our .so logs to tag `DeviceIDPlus` (config load + hook summary + COW) and `DIDPTrace` (filtered queries, debug).
- **Own forensic tools** (C, compile on Termux, run as root in `/data/local/tmp/`): `scripts/analysis/memread.c` (process_vm_readv), `memscan.c` (scan maps for pointer-values), `propread_test.c` (prop return semantics), `native/inspect_lar.cpp` + `inspect_preload.cpp` (replay hook engine outside zygisk).
- **Reboot watcher for sessions:** loop reading `/proc/uptime` via adb — reset = real reboot (`sys.boot_completed` alone is misleading if device didn't drop).

---

## PART 5 — FIELD-PROVEN FACTS (not assumptions)

1. **Shamiko 1.2.5 does not load on ZygiskNext 1.4.3** (silent discard by zn_loader). Do not reinstall without checking via `zygiskd status`.
2. **Magisk 30.7 seals /data/adb** (su via adb without r/w access; write/read only via **daemon-context**: micro-module whose customize.sh runs as daemon root — builders in `tools\build_*_module.py`).
3. **Magisk leaks mounts in userspace** (`tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` in mountinfo) — Wallet/Caixa detection vector. **Native ZN whitelist actually hides** (proven clean mountinfo).
4. **popsicle ABL rejects unsigned boot.img** (slot fallback). **`init_boot` is not in the vbmeta chain**; `boot`/`system`/`recovery` are. Stock vbmeta `flags=0`.
5. **Manual byte-patch of vbmeta corrupts and kills both slots** — recovery: `fastboot flash vbmeta_a/b <original ROM vbmeta>` + `set_active=a`.
6. **Slot _b had vendor_boot/dtbo from HyperOS CN** (different hash from _a; stock+stock baseline wouldn't boot on _b). The OTA script (flashes both slots) normalized it.
7. **ABL/fastboot quirks:** generic `--set-active` is NO-OP for re-arming a slot (use **`fastboot set_active a|b`** — resets retry-count); `fastboot wait-for-device` does not exist in this build (fastboot waits on its own when a command is issued); `--disable-verity` broken (AVB_MAGIC false negative).
8. **KSU LKM (backslashxx .ko android16-6.12) loads and responds on stock abogki4639 kernel** (proof: live `insmod` + `debug version` = 32558). Driver features are lean (su_compat/sulog/selinux_hide NOT_SUPPORTED) but **manager `adb root` works** (shell = uid 0, `u:r:ksu:s0`).
9. **znctl** = `/data/adb/modules/zygisksu/bin/zygiskd` (Magisk) or `/data/adb/ksu/bin/znctl` (KSU). On KSU there is **no** `denylist-policy` (hiding is via manager umount + `znctl enforce-denylist`). Binary is shell-flaky → retry wrapper.
10. **`/sdcard` does not resolve in adbd namespace under KSU** — use `/storage/emulated/0/...` for adb push/pull.
11. **Termux googleplay does NOT have RunCommandService** (F-Droid/GitHub version does).
12. **Magisk CLI daemon (`--denylist status`, `--sqlite`) gets intermittent SIGTRAP** ("selfchecker: checker remold.magisk detects sig5") — use retry/daemon-context.
13. **update_engine**: Updater may abort (bad download) without damage (A/B fallback); `--cancel`/`--reset_status` clean up; the UI "restart" button may appear before writing finishes — **always check partition hashes before letting it reboot** (boot_b 315 vs 317 in hashes).
14. **317 did not change the kernel** (same abogki4639 from 315) — that's why the .ko 32558 worked directly.
15. **GSF reset does NOT change per-app ANDROID_ID** (SSAID lives in `/data/system/users/0/settings_ssaid.xml`, ABX since A12; only factory reset clears it). BR banks likely bind there + ADB_ENABLED.
16. **On KSU, apps under "umount global" do NOT receive zygisk injection** (ZN treats them as denylisted; exceptions go in `/data/adb/ksu/.allowlist`, magic binary "KSU"; no CLI — only manager UI, toggle "Umount modules" per app). Proof: Petal Maps only started being injected after disabling its umount (MIUI in the process read the `HWALN` from the COPG profile).
17. **GOT/PLT prop patching works on A16 userspace** (standalone smoke test: direct call spoofed, dlsym control real; and in zygote via ZN: `patched=272 errors=0` in Petal Maps process). `__system_property_get_name` **is not exported** in A16 bionic — derive name via original `__system_property_read`. **`__system_property_read` returns VALUE LENGTH (≥ 0), not 0-on-success** — check with `>= 0`.
18. **`screencap` is not useful for app diagnostics here** (captures Termux in foreground); `uiautomator dump` frequently fails with "could not get idle state" and only sees lockscreen with the screen locked.
19. **ZN 1.4.3 does NOT inject into processes from the USAP pool** (module `.so` stays mapped but the entry is never called; no log, no crash — NeoZygisk#73 documents the analog). MIUI uses the pool aggressively ("boost cold start"). Workaround: `device_config put runtime_native usap_pool_enabled false` (persists) + `persist.sys.usap_pool_enabled=false` + `dalvik.vm.usap_pool_enabled=false`. ZN may also sporadically skip the entry even with pool off (unknown cause; force-stop+relaunch mitigates).
20. **ZN caches the module `.so` entry offset in the zygote** — hot-swapping the `.so` (even with `cp` preserving inode) crashes every injected app on specialize (SIGILL in padding below new `.text` / SIGSEGV in zn_alloc), including with the module "disabled" (the flag only takes effect after reboot). **NEVER swap zygisk `.so` without reboot.**
21. **`liblog` reads props internally** (`log.tag.*`): unconditionally logging inside a `__system_property_get/find` hook recurses until stack overflow. Filter the log by key or use a reentrancy guard.
22. **`dd` on `/proc/<pid>/mem` returns ZEROS in this setup** (toybox/SELinux) — cross-process memory read only reliable via `process_vm_readv` (own tools: `scripts/analysis/memread.c`, `memscan.c`, compiled on-device with Termux clang; in `/data/local/tmp/`).
23. **`wrap.<pkg>` (LD_PRELOAD in app) requires `ro.debuggable=1`** — here it's 0, doesn't work. app_process with LD_PRELOAD from shell WORKS to reproduce ART context (but does NOT reproduce zygote preloaded lib context).
24. **GOT hook does not cover MIUI's JNI path for SystemProperties** (with hooks installed and verified at the correct slot of libandroid_runtime, `SystemProperties.get` via reflection still returns the real value). The path-proof solution is **prop_area COW** (COPG `:cow` does the same; KernelSU PR #3470 confirms static/direct readers bypass dynamic hooks).
25. **ZN/zygisk matching is by PROCESS NAME (nice_name), not package** — `com.google.android.gms` in `.perapp_props` only covers the main process; `.persistent`/`.unstable` need their own lines. Proof via `/proc/<pid>/maps`: module mapped only in the main process until we added the extra lines (Chimera DCK runs in `.persistent`).
26. **`setprop` CREATES non-existent `ro.*` prop** (root, live): `setprop ro.gms.dck.eligible_wcc 3` worked because the prop did not exist (ro.* only becomes immutable after it exists). Persisted via DeviceID+ service.sh (resetprop post-boot_completed).
27. **`android.os.Build.*` is baked in the zygote** — neither COW nor GOT hooks reach it; apps that check the model via `Build.MODEL` (or WebView UA, derived from Build.*) require JNI spoof of the static field in the process (PIF technique; implemented in DeviceID+ v2.2.0-dev). Diagnosis: DIDPTrace showed zero queries for `ro.product.model` in the target app.
28. **Production GMS 26.28.60 does NOT apply phenotype overrides through any local channel** — tested: `flag_overrides` + `flag_overrides_to_commit` + links in `experiment_states_to_overrides` (committed state 4372), accounts 0 and 1, broadcast `com.google.android.gms.phenotype.FLAG_OVERRIDE`, editing `gms_chimera_phenotype_flags.xml`, and the **GMS Phixit** app (wrote ~90 DCK registry flags). Read op = `getCommittedOverridesPhixit` (debug channel). New schema (db v1033+) decoded: merge requires override↔committed_experiment_state link; served configs live in `experiment_states.experiment_token` (params/dynamic_params almost always empty).
29. **Google Digital Car Key (DCK): gating documented via own RE** — `isCreateDigitalKeyPossible()` requires `wcc>0 && downloadAllowed`. wcc = `SystemProperties.getInt("ro.gms.dck.eligible_wcc", 0)` (class `bsst`, classes6.dex of gms) with optional override `DckFeatureMain__wcc_override`; `downloadAllowed` = flag `DckStub__full_module_download_allowed` (default false; jycg.java). WCC: 1=NFC, 2=+BLE, 3=+UWB. Without served config for the model → stub stays at defaults → full DCK module never downloads.
30. **The backslashxx KSU driver does NOT respond to external prctl** (probe `0xDEADBEEF` returns -1 even as root — `scripts/analysis/ksu_probe.c`; manager/ksud use supercall/netlink) — prctl detection vector is harmless in this setup.
31. **SuSFS is impossible in LKM** (patches VFS built-in; requires boot.img which ABL rejects; research 25/Jul: nobody distributes `.ko` with SuSFS because it's structurally impossible; sus_su deprecated in v2). LKM hide route: `kobject_del`+`list_del` in `kernelsu_init` (done — fork `hide-lkm`).
32. **Protected apps (DexProtector) rename the process** (`:p<hex>` — `pidof <pkg>` fails; use `ps -A | grep <pkg>`), **scan `/proc/self/maps`** for extra executable segments (zygisk injection = instant kill via `MessageGuardException`, DP code: 786) and **enumerate packages via raw Binder** (bypass PackageManager). Revolut **performs local key attestation and validates the content** (TEESimulator#41: malformed attestation = crash).

---

## PART 6 — RESEARCH CONDUCTED (condensed, with sources)

### A. yapixel/popsicle_ksu_workflow repo
- **Generic Google GKI common kernel** (android16-6.12, tarball Jun/2025 r58), NOT Xiaomi source. **`xxksu`** variant = fork `backslashxx/KernelSU` (driver 32558) + real **SuSFS v2.2.0** (15 CONFIG_KSU_SUSFS_* flags). Uname string **hardcoded spoof** `abogki444322847` (≠ our stock abogki4639, same android16-5 KMI generation). README outdated (says "SuSFS: N/A" — wrong; promises KMI bypass the current pipeline does not apply). **Zero community validation** (~1 download, 0 issues).

### B. Xiaomi AVB/bootloader (HyperOS 3)
- AVB spec: UNLOCKED should tolerate unsigned ([avb README](https://android.googlesource.com/platform/external/avb/+/refs/heads/main/README.md)); **Xiaomi deviates in practice** — modified boot only boots by disabling verification in vbmeta ([XDA guide](https://xdaforums.com/t/guide-unlocking-bootloader-and-disabling-verifiedboot.4104045/), [OrangeFox MR](https://gitlab.com/OrangeFox/bootable/Recovery/-/merge_requests/36)).
- `fastboot --disable-verity --disable-verification` with error **"Failed to find AVB_MAGIC at offset: 0"** = known fastboot bug (multi-device, even with valid file; [PixelFlasher #346](https://github.com/badabing2005/PixelFlasher/issues/346), [razer-edge-gsi #1](https://github.com/gogopowerjackets/razer-edge-gsi/issues/1)). Workaround: local patch with avbtool or [libxzr/vbmeta-disable-verification](https://github.com/libxzr/vbmeta-disable-verification).
- **Eng ABL**: documented use is unlocking fastboot for unlock ([POCO F7 Ultra gist](https://gist.github.com/maoist2009/370dc89fa5e52bcec792dc95fe94e33b) — ⚠️ bricks Toshiba/Kioxia NAND post-Feb); **no evidence** it disables boot AVB.
- `fastboot flashing unlock_critical`: exists on some Xiaomi; **no evidence** it's needed on modern HyperOS.
- sm8850 ecosystem bypasses AVB via **init_boot** (LKM) — same route we use.

### C. KSU ecosystem (xxksu, KSU-Next, SuSFS, sources)
- **xxksu** = `backslashxx/KernelSU` (upstream-compliant fork; releases have own manager `KernelSU_v3.2.5-34_32559-release.apk` + `.ko` per KMI including `android16-6.12_kernelsu.ko` + `ksuinit`). xxksu↔KSU-Next manager compat: **not documented**. Driver requires manager ≥ 32513.
- **KernelSU-Next** has integrated `susfsd` (SuSFS userspace).
- **Official popsicle kernel source EXISTS:** branch `popsicle-w-oss` in [MiCode/Xiaomi_Kernel_OpenSource](https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/popsicle-w-oss) (Mar/2026, 17/17 Pro/17 Pro Max, Android 16) — enables own build.
- **Codenames:** 17 = pudding, 17 Pro = pandora, **17 Pro Max = popsicle**, 17 Ultra = nezha.
- **KMI frozen per generation** ([android-common](https://source.android.com/docs/core/architecture/kernel/android-common)); real risk outside KMI: Xiaomi patches missing in generic GKI break WiFi/BT ([WildKernels #241](https://github.com/WildKernels/GKI_KernelSU_SUSFS/issues/241)).
- **SuSFS v2.2.0** ([simonpunk/susfs4ksu branch gki-android16-6.12](https://gitlab.com/simonpunk/susfs4ksu/-/raw/gki-android16-6.12/README.md)): userspace tool `ksu_susfs` (same branch/patch version), scripts in stages; since v2 no longer depends on KPROBES.

### D. sm8850 custom kernels (Kokuban/Picters/ReSukiSU)
- **Kokuban Kernel** ([YuzakiKokuban/android_kernel_xiaomi_sm8850](https://github.com/YuzakiKokuban/android_kernel_xiaomi_sm8850)) — supports popsicle (CI config with device_check), near-daily releases, 2 modes: **LKM** (patch init_boot via manager — validates our route) and **ReSukiSU** (+SuSFS+KPM). Install via recovery (TWRP). AK3 with `patch_vbmeta_flag:auto` (⚠️ touches vbmeta).
- **Picters** (fork): NetHunter/WiFi injection focus; claims OK boot on **pudding** with camera/WiFi; no popsicle reports.
- **ReSukiSU** = SukiSU-Ultra fork (KernelSU fork): integrated SuSFS, multi-manager (accepts official KSU/RKSU/MKSU/SukiSU manager), GKI2 tracepoint hooks.
- **TWRP 3.7.1 unofficial functional for popsicle** ([XDA thread](https://xdaforums.com/t/recovery-unofficial-a16-twrp-3-7-1-for-xiaomi-17-series.4784052/), [builds](https://sourceforge.net/projects/twrp-xiaomi-17-series/)) — flash via `fastboot flash recovery` (temporary `fastboot boot` does NOT work).
- Nobody documents the hostile ABL we encountered — **we are in uncharted territory**.

### E. xiaomi.eu system app updater
- Official policy ([FAQ](https://xiaomi.eu/community/threads/frequently-asked-questions.73215/)): never update non-Google system apps — loses EU translations/patches. Staff confirms ([70844](https://xiaomi.eu/community/threads/how-to-install-the-latest-system-apps-in-eu-rom.70844/)). "Downloads but doesn't install" pattern reported: [74800](https://xiaomi.eu/community/threads/system-apps-update-problem.74800/), [67231](https://xiaomi.eu/community/threads/update-system-app.67231/), [XDA 4701181](https://xdaforums.com/t/cant-install-system-applications-from-unofficial-channels-xiaomi-12s-ultra.4701181/) (same `INSTALL_FAILED_UPDATE_INCOMPATIBLE`).
- Documented workarounds: uninstall with root and install as regular app (loses privileges/mods); LSPosed+CorePatch (dead on A16).

### F. Per-app device-ID spoof (the research that unlocked Caixa)
1. ⭐ **[sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger)** — KSU/Magisk/APatch, native KSU WebUI, per-app **ANDROID_ID/SSAID**: dropdown of settings_ssaid.xml packages, Randomize button (new 16-hex), Default (restore), backup to /sdcard; uses abx2xml/xml2abx (A12+ ABX); **requires reboot**. Stated purpose: "banking apps are using your DeviceID/SSAID to ban you". AGPL, 244★, v1.2.1 (Nov/2025), active. **CHOSEN (and forked as DeviceID+).**
2. [yubunus/DeviceSpoofLab-Magisk](https://github.com/yubunus/DeviceSpoofLab-Magisk) — per-app SSAID + global props (model/serial via resetprop); MIT, 105★, young.
3. [AlirezaParsi/COPG](https://github.com/AlirezaParsi/COPG) — most mature (341★, v5.9.0 from 20/Jul/2026), Zygisk (requires ZygiskNext), hot without reboot. **Free tier = Build.*/CPU/device profiles; prop spoof (`:cow`), Android ID, GPU, SIM, GAID per-app = PRO.** Used on Petal Maps front (see 2.2).
- Minor: RezaArbabBot/Android-ID-Changer (standalone root APK, no reboot), A7ALABS/ssaid-changer (A11/12), FuckAPK/FuckSSAID.
- **Nothing off-the-shelf covers per-app MediaDRM/keystore IDs without Xposed.** Agent warning: SSAID alone may not be enough (banks correlate GAID/MediaDRM/tokens).
- LSPosed is not 100% dead: fork **JingMatrix** (framework "Vector", LSPosed v1.11) has initial A16 support via ZygiskNext — future route if we want XPrivacyLua/Android Faker.
- Build-from-zero reference (MIT): `DeviceSpoofLab/common/android_id.sh` — edit settings_ssaid.xml via abx2xml/xml2abx, chmod 600, chown 1000:1000, restorecon, reboot.

### G. Digital car key in Google Wallet (24/Jul — BYD front)
- Google official list: "Pixel 6+, Samsung S21+, some Android 12+" ([answer/12060041](https://support.google.com/wallet/answer/12060041), [answer/13037118](https://support.google.com/wallet/answer/13037118)) — actual category is server-side and unpublished.
- "Smartphone is not compatible" error is documented by Google as a Wallet flow error ([answer/11358016](https://support.google.com/wallet/answer/11358016)).
- **Non-China implementation depends on Google's digital key API embedded in the ROM by the OEM** (HyperOS 3.0.3 enabled it on 15 Ultra; Lineage/Graphene/Huawei don't have it). Rollout happens model by model, via ROM update and/or server-side without update.
- BYD is NFC-only (no UWB) — [Wikipedia list of digital keys](https://en.wikipedia.org/wiki/List_of_digital_keys_in_mobile_wallets). BYD official only mentions iPhone/Samsung/Pixel ([byd.com/eu/ownership/byd-digital-key](https://www.byd.com/eu/ownership/byd-digital-key), [byd.com/br/chave-digital](https://www.byd.com/br/chave-digital)); actual list in the app is larger (Xiaomi 12→14, 13T Pro, OPPO Find X8 — [dolphinbyd t/4976](https://dolphinbyd.com.br/t/novos-modelos-de-smartphones-com-chave-digital-liberada/4976)).
- Silent key revocation on integrity re-checks: BMW + Xiaomi 15 case (Reddit r/BMW).
- Key sharing (iPhone/Samsung → another device) also goes through Google's whitelist (failed on Poco X4 Pro).

### H. Petal Maps block on non-Huawei (24/Jul — own RE)
- Method: decompilation of 8 APKs (jadx) from 4.5.0.303 → 4.7.0.319 (APKCombo/Uptodown). Artifacts in `/data/data/com.termux/files/usr/tmp/petal/`.
- Single check: `ro.product.manufacturer == "HUAWEI"` via reflection on `SystemProperties.get`, in `SplashActivity`; introduced in **4.7.0.316** (04/May/2026); current msg in 4.7.0.319: "Petal Maps is only available for Huawei devices."
- Classes: `com.huawei.maps.app.petalmaps.splash.SplashActivity#A`, `defpackage.tp2` (EnvironmentUtil), resource `restrictions_use_app`.
- No public write-up of this check found (Reddit r/Petal_Maps, 4PDA were inaccessible for scraping) — original analysis.

---

## PART 7 — SESSION ARC

### Session 0 (21/Jul/2026 — inherited context)
- Bootloader unlock completed via CVE-2026-43499 (audited Linuxoid-cn v2.0.0 tool), xiaomi.eu OS3.0.315 installed, Magisk 30.7 + ZygiskNext + PIF + TrickyStore + "Shamiko whitelist" (later proven illusory).

### Session 1 (22/Jul morning — the previous AI)
- Attempted Magisk→KSU/APatch migration: 6 attempts, 0 successes. Concluded (wrongly) that KSU was unfeasible. Restored Magisk. Wallet broke hours later.

### Session 2 (22/Jul afternoon — this line of work)
1. **Wallet diagnosis:** integrity 3/3 ok, keybox ok, props ok. Central finding: **Shamiko never loaded** — Shamiko 1.2.5's armored `.so` is silently discarded by ZygiskNext 1.4.3's `zn_loader` (registry `{"modules":[]}`, no `.tmp/status`, no injection in maps, module description never rewritten). And the `no_mount_znctl` flag that Shamiko creates **suppressed ZN's own mount hiding** → apps read `tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` in `/proc/self/mountinfo`.
2. **Clean rebuild:** backup /data/adb → `magisk --remove-modules` → wipe dirs → reinstall ZN+PIF+TS (+keybox/target restored) → **native ZN whitelist** (`znctl denylist-policy whitelist` + `enforce-denylist enabled`; allowlist termux/gms+unstable/vending) → non-listed app mountinfo **proven clean**. But Wallet stayed flagged: **server-side marking** (the GSF reset was done with the device still leaking mounts).
3. **Decision: KSU.** Autopsy of the 6 attempts: attempt 5 (yapixel GKI-mode kernel) was discarded by false negative (`/sys/module/kernelsu` doesn't exist in GKI-mode; detection is via manager/prctl); attempt 6 mixed GKI+LKM modes (conflicting); LKM on stock kernel died from module signing.
4. **AVB wall discovered:** byte-perfect boot repack with yapixel kernel → **ABL rejects unsigned boot.img** (slot fallback). Stock vbmeta `flags=0`; **`init_boot` OUTSIDE the chain** (descriptor strings-scan: `boot`/`system`/`recovery` present, `init_boot` absent).
5. **KSU LKM victorious:** live proof — `ksud insmod android16-6.12_kernelsu.ko` on stock kernel → `ksu` module LIVE + `Kernel Version: 32558` via prctl. init_boot patch via `ksud boot-patch --allow-shell` → boot with KSU alive. Stack ZN+PIF+TS + **umount global** (manager) + clean Google reset → **Wallet ✅, Caixa ✅, BYD ✅, integrity 3/3**. (BYD: the "PAC crash" was a Magisk environment artifact, not an app bug.)

### Session 3 (22/Jul evening — system app updater)
- Symptom: updater shows 7 real updates, infinite loop; manual install also fails. **Forensic proof:** `INSTALL_FAILED_UPDATE_INCOMPATIBLE` + compared certificates: installed app signed by `ingbrzy@miuios.cz` (Igor Eisberg, xiaomi.eu) vs update by `miui@xiaomi.com` (official). **It's by design** (xiaomi.eu official FAQ). Solution: ignore; apps come with the ROM. Module/magic-mount replacement pilot: magic-mount doesn't cover `/product` on this KSU; manual bind via post-fs-data.sh worked (app registered new signature) but the app didn't open → reverted. Collateral finding: Xiaomi global apps are a family (shared permission `hyperos.permission.READ_AIACTION` between securitycenter and aicr — `INSTALL_FAILED_DUPLICATE_PERMISSION`). **Closed 23/Jul: user uninstalled the update app.**

### Session 4 (23/Jul morning — OTA 315→317)
- Almost used the **PANDORA** (Xiaomi 17 Pro) zip by mistake — caught on name verification. Redownload POPSICLE.
- Built-in updater aborted 2× (1st: download failure; 2nd: showed 22% with misleading "restart" button; engine cancelled via `update_engine_client --cancel` + `--reset_status`).
- **Winning path:** `windows_install_upgrade.bat` script (audited: no wipe, no relock, both slots, `set_active a`). Interactive prompt bypassed with `_auto.bat` variant (`set /p` doesn't accept pipe on cmd.exe). Flash 100% OK (~9 GB, super in 14 parts).
- **Pre-patch:** 317 init_boot patched with `ksud boot-patch --allow-shell` BEFORE flash (same .ko; image `68f996d9…` in `backup\ksu-migration\init_boot-317-ksu.img`). After stock reboot: `fastboot flash init_boot_a` → **317 + KSU 32558 + ZN enforce + PIF + integrity — all preserved**. Bonus: _b lost the factory CN partitions (script flashes both slots).

### Session 5 (23/Jul afternoon/evening — Caixa blocked → RESOLVED)
- Caixa: **server-side bank block** (marked the device in the root+ADB visible era). GSF reset does **not** resolve (SSAID persists). BR banks also **detect ADB on** (`Settings.Global.ADB_ENABLED`) — user keeps ADB off to use bank.
- Off-the-shelf solution research (Part 6.F) → sidex15/deviceidchanger module chosen → **DeviceID+ v2.0.0 fork** created and installed.
- **~21:00: Caixa RESOLVED** (combination and 2 bootloop lessons in Part 3). TWRP flashed as safety net.
- Evening: Pixel 10 PIF experiment for BYD digital key applied (Part 2.1). Commits up to `a2e90c5` (DeviceID+ v2.1.0: per-app prop spoof via own zygisk).

### Session 6 (24/Jul — DeviceID+ v2.1.1 fix + docs unification)
- Bug reported: app enrolled in SSAID that was **uninstalled** blocked any apply ("UID not found" — `uidOf()` only resolves via `pm list packages`) and was invisible in WebUI (list only renders installed packages) → impossible to uncheck, deadlock.
- **Fix (commit `4a9f8f0`, pushed):** automatic prune of UID-less entries in `applySsaid()` (with toast) + uninstalled apps now appear in the list marked as "uninstalled" (can be manually unchecked). Bump v2.1.1 (versionCode 2001001). Zip rebuilt locally (gitignored), **not installed on device**.
- Handover docs unified in this file (before: `SESSION-HANDOVER-2026-07-23.md` + `ESTADO-ATUAL-2026-07-23.md`).

### Session 7 (24/Jul — BYD research + Petal Maps + DeviceID+ per-app zygisk)
1. **BYD digital key — research concluded** (Parts 2.1 and 6.G): block is Google server-side whitelist by model+ROM + "digital key API" that xiaomi.eu may lack; PI 3/3 and PIF spoof not enough; no documented workaround.
2. **Petal Maps — RE + 4 attempts** (Parts 2.2 and 6.H): single check `ro.product.manufacturer=HUAWEI` via reflection; free COPG insufficient; discovery that KSU umount global blocks zygisk injection; DeviceID+ v2.1.0 with own zygisk injects and hooks (proven in logs) **but block persists** — open hypotheses (a/b/c) in Part 2.2.
3. **New user rules:** NEVER reboot on own initiative (always ask — there are other agents running); screencap not useful for diagnostics (grabs Termux); ask user for artifacts when needed.
4. Code house: git identity configured, `gh auth setup-git` done, `.gitignore` covers `native/test_hook`.

### Session 8 (24/Jul afternoon — Petal: full forensics + COW prop_area + manager 32562)
1. **Root bug in per-app spoof:** read/read_callback hooks checked `__system_property_read` with `== 0`; bionic returns value length → spoof was never applied. Fix `>= 0` + extended smoke test (get/read/read_callback/COW).
2. **Two injected app crash incidents** → cause: hot-swap of zygisk `.so` with entry offset cached in zygote by ZN (fact 20). User performed 4 manual reboots that day.
3. **USAP pool disabled** (fact 19) after proving ZN does not inject into pool processes.
4. **KSU manager updated 32559 → 32562 (v3.2.4-36)** via `gh release download` + `pm install -r` (APK in `tools/ksu_apk/`).
5. **GOT patching forensics:** with per-slot verbose logging (build `PERAPP_VERBOSE_PATCH`), proven that the patch lands at the correct slot of libandroid_runtime (readback OK) and MIUI's JNI check **still** reads the real value → path not covered by GOT (fact 24). New tools: `scripts/analysis/memread.c`, `memscan.c`, `propread_test.c` (compile on Termux, live in `/data/local/tmp/`).
6. **prop_area COW implemented** (`native/prop_cow.cpp`) and integrated into module (COW first, GOT hooks as complement). Smoke test full PASS. **Awaiting Petal validation reboot** (`.so` deployed ~17:46).
7. Web research (3 agents): Petal bypass does not exist publicly (our RE is the only one); AOSP A16 JNI = find+read_callback; COPG `:cow` = prop_area COW (validates the approach); NeoZygisk#73 = USAP doesn't inject; ZN has safe mode after zygote crash.

### Session 9 (24/Jul afternoon/evening — Petal RESOLVED, DeviceID+ complete, BYD/DCK rock bottom)
1. **DeviceID+ v2.1.1 installed** (zip rebuilt with COW `.so` — the previous zip lacked it and would have wiped the spoof) + config re-merged. **Petal VALIDATED post-reboot: `Get Manufacturer: HUAWEI`, app opens — Petal front closed.** Temporary Termux config removed; COPG removed by user.
2. **BYD digital key — full journey to verdict:** research debunked the "Xiaomi 17 works" rumor (hearsay; only real success = BMW i4 + Xiaomi 17 base — BYD and BMW have separate whitelists; Google official list has "17 & 17 Ultra", **not** 17 Pro Max). Aurora spoof (14 Ultra) via COW + **new `Build.*` JNI spoof in module** on gms/wallet/BYD app (discovery: match by process name — fact 25; BYD umount turned off to inject) → block persisted. **BYD app + GMS RE:** check = `isCreateDigitalKeyPossible()` (GMS DCK); gates = `ro.gms.dck.eligible_wcc` (**set=3, persisted**) + phenotype flag `DckStub__full_module_download_allowed` (**unbeatable**: production GMS does not apply overrides — fact 28). Tested up to GMS Phixit. **Front CLOSED with full rollback** (config, prop, phenotype, Phixit, tmp) — device back to stable state, PI 3/3, Petal ok.
3. **Legacy:** DCK map in Part 2.1/6.G, facts 25–29, gms sources analyzed in `analysis/byd/*.java`, DeviceID+ with 3rd spoof mechanism (Build.* JNI).
4. Day's quirks: 2nd device appeared on adb (Redmi `f10c4f767d7b`, slot _b) — always check serial/model before commands; on-device jadx lives in `/data/data/com.termux/files/usr/tmp/petal/jadx` (run with `sh .../bin/jadx` + java on PATH).

### Session 11 (27/Jul morning — Bradesco Seguros ✅ RESOLVED via HMA-OSS)
1. **Diagnosis:** app v2.85.0 = DexProtector/Licel (same protector as Revolut); process renamed `:p<hex>`; block screen on 1st run, `TerminateException` crash after `pm clear` (the crash is A16 BAL_BLOCK: the block dialog can't open on cold start).
2. **Root vector:** package enumeration — post-crash digest names `me.weishu.kernelsu`; app telemetry says `isRoot:false` (not classic root nor attestation — app creates no keystore aliases; identical crash with TS up/down). APK RE + subagent research (DexProtector enumerates via raw Binder — Romain Thomas Jan/2026).
3. **Fix:** HMA-OSS oss-164 installed + pre-seeded config (`bancos` template empty whitelist; scope bradseguros) → reboot → **app WORKS (user confirmed ~11:20)**.
4. **TS daemon anti-tamper discovered** (Part 2.5 — lesson): editing module files = daemon dies silently.
5. **Revolut RESOLVED ~11:35** (user confirmed): same `bancos` template applied via HMA-OSS UI → app v10.140 passes. **"Leaked keybox" hypothesis from Session 10 DISPROVEN** — the vector was applist all along (with DroidWin keybox active and PI 3/3, the app works). Lesson: DexProtector checks applist via raw Binder **before** any attestation; HMA-OSS is a prerequisite for Licel apps.

### Session 12 (27/Jul — YT Music Morphe on Android Auto ✅ RESOLVED)
1. **Diagnosis:** `Missing DynamiteApplicationContext` crash in `MediaBrowserService.onGetRoot` — real GMS `GoogleCertificatesImpl` requires uninitialized `DynamiteApplicationContext` (app uses microG). Official Morphe patch (`return true` in `kxo.m`) not enough with real GMS — the crash occurs BEFORE `m()` is evaluated, inside `tcn.c()`. Three builds without the patch: old bundle, patch unchecked in UI, patcher OOM.
2. **Validated fix:** `tcn.c(String) → return false` via dex-only surgery (baksmali/smali 3.0.9). Reports "unsigned" without touching Dynamite; `kxo.g()` passes because `!c() && !m()` = `false` with `m()` patched.
3. **Final build:** CLI Morphe `--exclusive` 37 patches + dex-only fix → `install -r` with manager.keystore (BKS v2, storepass "", alias Morphe, keypass Morphe).
4. **Podcast verdict:** entitlement `khr.e()` already patched, `skip_entitlement_check=true` for gearhead, browse tree comes from server (Innertube) — cutoff is **server-side** by account tier (ReVanced#6185). Music requires YouTube Premium on AA.
5. **Upstream PR:** https://github.com/MorpheApp/morphe-patches/pull/2239 — extends "Bypass certificate checks" with `IsGoogleSignedFingerprint` + `returnEarly(false)`. andersonlucasg3 fork, branch `fix/ytmusic-aa-dynamite-crash`.
6. **Lessons:** patcher OOM → reboot device before patching large APKs; manager keystore reusable (empty storepass!); apktool corrupts resources → prefer dex-only; Frida 17 without java-bridge → bundle with esbuild; always check device serial (Redmi `f10c4f767d7b` confused with popsicle); frida-server is a detection vector → stop after use.

### Session 10 (25/Jul morning — Revolut: full forensics + `.ko` with hide, not resolved)
1. **Diagnosis:** visible vectors confirmed as app uid: `/proc/modules` ksu, `/sys/module/ksu`, `/system/bin/su`. prctl harmless (fact 30). SuSFS ruled out (fact 31).
2. **Tests that failed:** adb root OFF (no su), frida-server removed, manager frozen, new SSAID + clear data, per-app injection with Pixel 10 spoof (detected by maps scan — fact 32).
3. **`.ko` with hide built and flashed:** fork `andersonlucasg3/KernelSU` + patch `9ea26f3` (`kobject_del`+`list_del`), built via GitHub Actions, `ksud boot-patch` on stock 317 init_boot, fastboot flash. Module invisible, KSU 32558 intact, normal boot. Rollback: re-flash `68f996d9`.
4. **Research (verdict):** Revolut = DexProtector/Licel; **suspect #1 = leaked keybox** (PI 3/3 not enough; Revolut rejects popular keyboxes — XDA 4773849); documented paths: private keybox, HMA-OSS whitelist, TEESimulator. Front paused (Part 2.4).

---

## PART 8 — GOLDEN RULES

- NEVER `fastboot flashing lock` · NEVER manually edit vbmeta · always verify **POPSICLE** (not PANDORA) in downloads
- **NEVER reboot the device on your own initiative — always ask the user** (there are other agents/sessions running on the device)
- **NEVER hot-swap the zygisk module `.so`** — ZN caches the entry offset in the zygote; every injected app crashes until reboot. Build → deploy → REBOOT → test (fact 20)
- `fastboot set_active a|b` (underscore) · adb push/pull via `/storage/emulated/0/...` or `/data/local/tmp/`
- Two devices on adb: `adb -s 4d7fc9af` (or IP:5555); Git Bash: `MSYS_NO_PATHCONV=1` for Unix paths
- ADB/dev options OFF when using banking apps (detection via `Settings.Global.ADB_ENABLED`)
- KSU module update overwrites the module dir → re-merge config into staged before reboot
- Cross-process memory read: `process_vm_readv` (memread/memscan), NEVER `dd` on `/proc/<pid>/mem` (returns zeros — fact 22)
- USAP pool: Petal front closed; reactivation is an open decision (leaving it off costs nothing visible)
- adb with a new/unknown device: check `ro.product.model` before any command (a Redmi showed up as `f10c4f767d7b` in Session 9)
- **Patcher OOM:** before patching large APKs (>50 MB) in Morphe Manager, **reboot the device** (clears RAM). The patcher allocates mmap proportional to the APK (~1.3 GB for YT Music 80 MB)
- **Morphe Manager keystore:** `/data/data/app.morphe.manager/app_signing/morphe.keystore` (BKS v2), storepass **empty**, alias `Morphe`, keypass `Morphe` — reusable for CLI builds with `install -r`
- **apktool corrupts resources:** rebuilds via apktool break `resources.arsc` (`Resources$NotFoundException` crash). Prefer **dex-only surgery** (baksmali/smali + ReplaceDex.java) for targeted patches
- **Frida 17:** java-bridge was removed from core → scripts need to be **bundled with esbuild** (`frida-java-bridge`). `enumerate_processes()` may not list certain apps → use `pidof` via adb + attach by PID. **Always stop frida-server after use** (detection vector for banking apps)
