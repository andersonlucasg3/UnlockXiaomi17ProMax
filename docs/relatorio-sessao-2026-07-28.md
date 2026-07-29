# Session Report — 2026-07-28

**Objective:** Make the BYD app (`com.byd.bydautolink` v3.4.5) recognize the Xiaomi 17 Pro Max as eligible to enroll a digital car key. Starting point: Session 9 had closed this front when the gate phenotype `DckStub__full_module_download_allowed` proved unbeatable via local overrides.

**Device:** Xiaomi 17 Pro Max (popsicle) | xiaomi.eu OS3.0.317.0.WPBCNXM | Android 16 | KSU LKM 32558

**Duration:** ~21:10 (28/Jul) → ~01:00 (29/Jul)

---

## ✅ CURRENT STATUS (as of session end, ~01:00 29/Jul)

**Front REOPENED — active, not resolved.** DeviceID+ v2.3.1 (native DCK hook, passive install) is deployed and armed; final validation pending. Stack:

| Component | Detail |
|---|---|
| DCK hook | DeviceID+ v2.3.1 — passive `FindClass` + `isCreateDigitalKeyPossible` hook, no forced class load |
| Config | `dck.hook=1` in `.perapp_props` for `com.byd.bydautolink` |
| KSU umount | **OFF** for BYD app (required for Zygisk injection; was re-enabled after bootloop recovery, then turned off again) |
| GMS | Clean (no Frida, no phenotype overrides); `ro.gms.dck.eligible_wcc=3` set via DeviceID+ service.sh |
| MMKV | Backed up in `backup/byd-mmkv-2026-07-28/`; to be deleted before final test |

**Remaining step:** force-stop BYD → open → 30s crash check on DigitalKeyHomeActivity → delete MMKV → "Add digital key" → verify `intercepted` log and `isWCC3=true` in MMKV.

---

## 1. Phase 1 — Frida client-side on BYD app

### 1.1 Hook setup (`tools/frida-agent/byd-dck-hook.js`, 5 layers)

Five hook layers targeting the BYD app's digital key flow:

| Layer | Target | Result |
|---|---|---|
| `isDckFeatureAvailable()` | `DigitalKeyHelper` (app) | ✅ Hooked → returns `true` |
| `isCreateDigitalKeyPossible()` | GMS DCK client via binder proxy | ❌ `FrameworkUnavailableException` — DCK service does not exist (full module never downloaded) |
| `getDigitalKeyFramework()` | Framework acquisition | ❌ Same path, same exception |
| Eligibility check | App-side gate before GMS call | ✅ Intercepted |
| Dialog gate | `isWCC3` / compatibility dialog | ⏳ Not reached (crash) |

### 1.2 Anti-tamper: app dies ~3–5s after Frida attach

The BYD app process crashes reliably within seconds of Frida attaching. The crash is a SIGSEGV — **DexProtector** (see Phase 4) refuses to decrypt its own code under ptrace/Frida. This is not a hookable detector — it is a fundamental incompatibility between Frida's instrumentation and DexProtector's code protection.

**Conclusion:** Frida on the BYD process is not viable.

---

## 2. Phase 2 — Frida GMS-side

### 2.1 Hook setup (`tools/frida-agent/gms-dck-unlock.js`)

Target: `com.google.android.gms` process (⚠️ **NOT** `.persistent` — Chimera DCK classes live in the main GMS process under a DelegateLastClassLoader from Chimera).

7 hooks across 4 classes:

| Class | Method | Purpose |
|---|---|---|
| `bsog` | `a()` | Eligibility check → return `true` |
| `bsog` | `b()` | WCC + download gate → return `true` |
| `bsst` | `a()` | WCC prop read → return `3` |
| `jycg` | (flag reader) | Download allowed flag → return `true` |
| `jycd` | (flag reader) | Auxiliary flag reader |

**Key challenge:** Chimera classes are loaded by a `DelegateLastClassLoader`, not the standard app classloader. The Frida script must perform classloader switching (enumerate loaders, find the one containing `bsog`, set as context before hooking).

### 2.2 Results

- `ro.gms.dck.eligible_wcc=3` set live via `setprop` — **works** (prop didn't exist; `ro.*` only becomes immutable after it exists; see fact 26 from handover).
- Eligibility of the stub intercepted: `bsog.a() → true`, `bsst.a() → 3`, negative logs suppressed.
- **BUT the DCK module download does NOT fire.** The decision to download is made by Chimera's `ModuleInstallService`, which reads the flag through a different path (registry `gtwx`/`gtvd`).
- `bsog.b()` is **AOT-inlined** by `dex2oat` — Java hooks on it do not fire during init. The real gate is executed as native code, invisible to Frida's Java bridge.

### 2.3 GMS selfchecker crash — Frida route ABANDONED

Seconds after attaching Frida to the GMS process, the device experienced:

1. **SIGSEGV** in GMS — GMS has an internal **anti-Frida selfchecker** that detects ptrace/tamper and deliberately crashes.
2. **Crash loop** — GMS restarted, Frida re-attached, crashed again.
3. **Full device reboot ~00:07** — the crash loop escalated to a system-level reboot.
4. **Play Protect flagged "risky app"** — post-reboot, Play Protect warned about an app on the device (Frida-server or the hook artifacts). **Monitor PI** — risk of attestation impact.

**Verdict: Frida-on-GMS ABANDONED as too destructive.** The selfchecker + AOT inlining make this route infeasible and dangerous to device stability.

---

## 3. Phase 3 — Web research (3 sub-agents)

Comprehensive search for public bypasses of digital car key eligibility:

| Source | Finding |
|---|---|
| XDA Developers | Zero cases of forced DCK on unsupported device |
| Reddit (r/BYD, r/Android, r/GoogleWallet) | Only reports of failure on non-whitelisted models |
| dolphinbyd.com.br | BR community — no workaround documented |
| GitHub | No projects targeting DCK module download bypass |
| APKMirror / APK dumps | **DCK module APK (`dl-Dck.optional_*.apk`) does not exist on the internet** |

### 3.1 DCK module APK — why it's unfindable

The DCK module is classified as **"optional"** in Chimera's module registry:
- Not included in Pixel factory images (optional modules are downloaded on demand).
- Not in APKMirror bundles (APKMirror only archives modules that ship in factory images).
- No public dump of `app_chimera` from a DCK-enabled device.

**The only source would be extracting it from a device with DCK already activated.** User has a Galaxy S24 (unrooted, Knox intact) — but Samsung's DCK path goes through **Samsung Wallet** / `com.samsung.android.dkey`, not Google's generic DCK module. Route discarded.

### 3.2 BYD root detection history

The BYD app has had root detection since **v2.9.1** (documented in Niek/BYD-re repository). The recommended stack is **Shamiko + HMA** (Hide My Applist). This explains the utility of HMA-OSS (present in `tools/hma_oss/` but **not configured for BYD in this session** — Shamiko does not load on ZygiskNext 1.4.3; fact 1 in handover).

### 3.3 Full provisioning flow (mapped)

```
BYD app → backend BYD (dilinkappoversea-eu.byd.auto, dynamic model list)
  → Google Wallet (eSE secure element)
  → CCC R3 provisioning with RKP/key attestation
  → NFC pairing with vehicle
```

**Known walls beyond the local check:**
1. **DCK module** — Google server-side (never downloads without served config)
2. **Backend BYD** — serves model list dynamically; even if the app passes the local gate, the server may reject
3. **Wallet attestation** — hardware-backed key attestation via eSE; TrickyStore covers this but RKP adds complexity

---

## 4. Phase 4 — BYD app forensics (key discoveries)

### 4.1 DexProtector confirmed

The BYD app is protected by **DexProtector** (Licel). Evidence:
- Native library with DexProtector signature behavior (refuses to decrypt bytecode under ptrace).
- Frida attach → SIGSEGV within seconds (Phase 1.2).
- Process behavior matches the Bradseguros/Revolut DexProtector pattern (fact 32 in handover).

**Implication:** any Frida-based approach on the BYD process is dead. The only viable hook vector is **native Zygisk** (injected before the app process starts, before DexProtector initializes).

### 4.2 MMKV eligibility cache — not encrypted, locally decisive

**Path:** `/data/data/com.byd.bydautolink/files/mmkv/NFC_CACHE_FILE<VIN>` (+ `.crc`)

**Format:** MMKV (Tencent's mmap-based KV store), **not encrypted.**

**Keys observed:**
- `isWCC3` — the verdict from the "device is not compatible" dialog
- `isHaveLocalKey` — whether a digital key is already provisioned
- `isShowEnter` — whether to show the entry flow

**Behavior:** pure memoization. When the MMKV file is deleted, the app **re-evaluates** eligibility on next entry into the digital key flow. The re-evaluation calls GMS DCK (`wccSysProp` / `hasWccSupport` / `downloadAllowed`).

**Strategic implication:** the "incompatible" dialog verdict is **local** (GMS DCK stub), not from the BYD backend. This means:
- We do NOT need to defeat the BYD server to make the app *recognize* eligibility.
- If we can make `isCreateDigitalKeyPossible()` return `true`, the app will cache `isWCC3=true` and proceed.
- The backend wall (model list served dynamically) is a *subsequent* gate, not the one blocking us now.

### 4.3 Concrete DCK client class

The GMS DCK client used by BYD is: **`com.google.android.gms.dck.internal.zzfa`**

This is the concrete class behind the `DigitalKeyFramework.getClient()` call. Knowing the exact class name enables targeted native hooking without loading/decompiling the full DCK module.

---

## 5. Phase 5 — DeviceID+ v2.3.x (Zygisk native hook — the remaining route)

### 5.1 v2.3.0 — aggressive hook, crashed the flow

**File:** `modules/deviceidchanger/native/dck_hook.cpp`

**Strategy:** lazy thread fast-polling, flip-to-native (`kAccNative` @ offset +4 in ArtMethod + `RegisterNatives`), returning `Tasks.forResult(Boolean.TRUE)`.

**Behavior:**
- Installed successfully and **won the race** (fast-poll detected the class before the app called it).
- **BUT crashed the flow with SIGSEGV** at `ExecuteNterpImpl+324`.

**Root cause (A/B proven):** the v2.3.0 hook eagerly called `getClient()` / loaded DCK classes ahead of time. This forced DexProtector to decrypt and initialize the DCK classes out of their normal order, destabilizing the protector and causing the crash. **The hook itself was the cause** — confirmed by A/B test (no hook = no crash).

### 5.2 v2.3.1 — passive install (current, deployed)

**Strategy:** **no** `loadClass`, **no** `getClient`. Only `FindClass("com/google/android/gms/dck/internal/zzfa")` with `ExceptionClear` in a 250ms poll loop, after a **1.5s grace delay** (lets the app fully initialize, DexProtector settle, and the normal classload sequence play out). The only hook: `isCreateDigitalKeyPossible` → returns `true`.

**Key design decisions:**
- `FindClass` with `ExceptionClear` — if the class isn't loaded yet, silently skip; do NOT force loading.
- Grace delay of 1.5s before polling starts — avoids the early-init window where DexProtector is most sensitive.
- Only one method hooked — minimal surface area.

**Configuration:** controlled by key `dck.hook=1` in `.perapp_props` (per-app, matched by process name). The hook is **completely inert** when the key is absent or `0`.

**Artifacts:**
| File | Detail |
|---|---|
| `native/dck_hook.cpp` | Core hook logic (v2.3.1 passive strategy) |
| `native/dck_hook.h` | Header with class/method signatures |
| `deviceid_zygisk.cpp` | Integration: reads `dck.hook` key, conditionally activates DCK hooks |
| `build.sh` | Added dck_hook.{cpp,h} to the build |
| `module.prop` | v2.3.1, versionCode 2003001 |
| `module/zygisk/arm64-v8a.so` | 17880 bytes, includes dck_hook.o |

### 5.3 Validation state (pending)

At session end (~01:00):

| Condition | State |
|---|---|
| DeviceID+ v2.3.1 deployed | ✅ `.so` built and flashed, reboot completed |
| `dck.hook=1` armed in `.perapp_props` | ✅ Set for `com.byd.bydautolink` |
| KSU "Umount modules" for BYD | ✅ OFF (re-enabled after bootloop, then turned off again by user) |
| BYD app force-stopped + reopened | ⏳ Not yet executed |
| 30s crash check on DigitalKeyHomeActivity | ⏳ Pending |
| MMKV deleted + "Add digital key" flow | ⏳ Pending |
| Log verification (`intercepted` + `isWCC3`) | ⏳ Pending |

---

## 6. Incidents

### 6.1 Bootloop during GMS Frida crash loop (~00:07)

**Symptom:** device rebooted during the GMS Frida crash loop. Boot was stable at ~00:07 with the **old** DeviceID+ `.so` (zygote had not yet loaded v2.3.1).

**Likely cause:** residual crash loop from GMS selfchecker + Frida. The continuous SIGSEGV-SIGSEGV-restart cycle on a critical system process (`com.google.android.gms`) escalated to a zygote/system_server watchdog reboot.

**Mitigation:** `dck.hook` key was temporarily removed from `.perapp_props` as a precaution (re-armed later for the final test). KSU umount for BYD was re-enabled by the recovery (the bootloop/recovery reverts KSU per-app settings — see §7.3). User manually turned umount OFF again.

### 6.2 Play Protect "risky app" flag

Play Protect flagged an app as risky during the Frida-GMS session. This is a **server-side marker** on the Google account/device. Monitor Play Integrity — if PI drops, the flag may be the cause.

---

## 7. Lessons learned

### 7.1 The BYD "incompatible" verdict is local + memoized
The dialog "device is not compatible" is decided by the **GMS DCK stub** (locally), not the BYD backend. The verdict is cached in an unencrypted MMKV file. Deleting the cache forces re-evaluation. This changes the strategy: we don't need to defeat the BYD server for the app to *recognize* eligibility — only the GMS DCK gate.

### 7.2 DexProtector (BYD) and selfchecker (GMS) make Frida inviable on both processes
- **BYD app:** DexProtector kills the process within seconds of ptrace attachment. Not a hookable detector — fundamental code protection.
- **GMS:** internal anti-Frida selfchecker → SIGSEGV → crash loop → device reboot. Destructive.
- **Only viable route:** native Zygisk (injected at process birth, before protectors initialize).

### 7.3 AOT inlining and ModuleInstallService make DCK module download unbeatable via Java hooks
- `bsog.b()` (the eligibility decision) is AOT-inlined by `dex2oat` — Java hooks do not fire.
- `ModuleInstallService` reads flags through `gtwx`/`gtvd` registry, NOT through the phenotype path we exhaustively tested in Session 9.
- The full DCK module will never download without Google server-side config for this model.

### 7.4 KSU "Umount modules" is the Zygisk injection toggle — and it can revert
Per-app umount in KSU manager controls whether ZygiskNext injects into the process. This setting lives in `/data/adb/ksu/.allowlist` (binary format, flag at offset `f272` with stride `784`). **Bootloop/recovery can revert this setting** — always verify after incidents.

### 7.5 DCK module APK does not exist publicly
`dl-Dck.optional_*.apk` is classified "optional" by Chimera — not in factory images, not on APKMirror, no public dumps. The only source would be extraction from a device with DCK already active.

---

## 8. Timeline (day summary)

1. **Phase 1 — Frida on BYD app:** `isDckFeatureAvailable()` hooked successfully, but `isCreateDigitalKeyPossible()` throws `FrameworkUnavailableException` (no DCK service). App dies ~3–5s after Frida attach (DexProtector).
2. **Phase 2 — Frida on GMS:** 7 hooks across `bsog`/`bsst`/`jycg`/`jycd`. Eligibility intercepted, but DCK module download does not fire (`bsog.b()` AOT-inlined, `ModuleInstallService` reads flags independently). GMS selfchecker detects Frida → SIGSEGV crash loop → device reboot ~00:07. Play Protect flags "risky app". **Frida-on-GMS abandoned.**
3. **Phase 3 — Web research (3 agents):** zero public cases of DCK bypass. DCK module APK unfindable. BYD root detection documented since v2.9.1.
4. **Phase 4 — BYD app forensics:** DexProtector confirmed. MMKV eligibility cache discovered (unencrypted, locally decisive). Concrete DCK client class identified (`zzfa`).
5. **Phase 5 — DeviceID+ v2.3.x:** v2.3.0 (aggressive hook) crashed the flow (SIGSEGV from forced class load + DexProtector destabilization). v2.3.1 (passive `FindClass` with grace delay) deployed and armed. Final validation pending.
6. **Incident:** bootloop during GMS Frida crash loop; device recovered. Umount for BYD re-verified. dck.hook re-armed.

---

## 9. Session artifacts

```
modules/deviceidchanger/
├── native/
│   ├── dck_hook.cpp              # v2.3.1 passive DCK hook
│   ├── dck_hook.h                # Class/method signatures
│   ├── deviceid_zygisk.cpp       # Modified: dck.hook key integration
│   └── build.sh                  # Modified: dck_hook in build
├── module/
│   ├── zygisk/arm64-v8a.so       # 17880 bytes, v2.3.1
│   └── module.prop               # v2.3.1, versionCode 2003001

tools/frida-agent/
├── byd-dck-hook.js               # 5-layer hooks on BYD app
├── gms-dck-unlock.js             # 7 hooks on GMS DCK stub
├── byd-antitamper.js             # Anti-tamper investigation
├── run_gms_dck.py                # GMS attach runner
├── run_gms_dck_spawn.py          # Spawn-mode variant
├── _agent.js                     # Base agent template
└── ...                           # Other frida-agent scripts

analysis/byd/
└── gms_dck_run.log               # Frida GMS session log

backup/byd-mmkv-2026-07-28/       # MMKV backup (NFC_CACHE_FILE + .crc)
                                   # ⚠️ outside git (backup/ is gitignored)

tools/venv-frida16/               # Frida 16 venv (stealth attempt)
                                   # ⚠️ do not commit if untracked + large
```

---

## 10. Next steps (for the next session)

1. **Execute final v2.3.1 validation:** force-stop BYD → open → 30s crash check → delete MMKV → "Add digital key" → verify `intercepted` in `DeviceIDPlus` log and `isWCC3=true` in new MMKV.
2. **If v2.3.1 passes:** the app will show the "supported models" screen (next gate is BYD backend model list, server-side).
3. **If v2.3.1 fails:** investigate whether `FindClass` ever resolves `zzfa` (add diagnostic logging); consider delaying the poll further or hooking at a different lifecycle point.
4. **Monitor Play Integrity** — the Play Protect flag from the GMS Frida incident may have server-side consequences.
5. **HMA-OSS for BYD:** if root detection becomes an issue (BYD has had detectors since v2.9.1), configure HMA-OSS `bancos` template for `com.byd.bydautolink`.
