# Bradesco Seguros + Revolut (DexProtector) — RESOLVED 27/Jul/2026

Summary of the winning approach. Full operational details: `docs/SESSION-HANDOVER.md` (Parts 2.4, 2.5, Session 11).

## Diagnosis

- **Apps:** `br.com.bradseg.bscelular` v2.85.0 and `com.revolut.revolut` v10.140 — both protected by **DexProtector/Licel** (`lib/arm64-v8a/libdexprotector.so`).
- **Symptom:** "unsafe environment" screen (OK dialog) on 1st run; after `pm clear`, `android.app.TerminateException$<obf>` crash (the dialog does not open on cold start due to A16/targetSdk 36 BAL_BLOCK → unhandled exception kills the process).
- **Root vector (both):** **installed package enumeration** — DexProtector lists packages via **raw Binder** (bypassing Java PackageManager hooks) and detects root apps (`me.weishu.kernelsu` named in the post-crash digest — `launch-log5.txt`). App's own telemetry: `isRoot:false` (not classic root).
- **Ruled out:** key attestation (bradseguros does not create keystore aliases; identical crash with TrickyStore daemon up/down), mounts, PI 3/3, su, frida, leaked keybox (Session 10 hypothesis for Revolut — **ruled out**: works with DroidWin keybox).

## Solution (permanent)

**HMA-OSS oss-164** (Zygisk fork of Hide My Applist, no LSPosed): filters the applist at the **system_server** level — covers even raw Binder enumeration.

- Zip: `tools/hma_oss/HMA-OSS-ZYGISK-oss-164-release.zip` (sha256 `4bf157db64f0daa59137436fef8eafeb3180d0fd545ca2e1196b47aa8abef9fa`), id `hma_oss_zygisk`, manager `org.frknkrc44.hma_oss`.
- Template `bancos` = **empty whitelist** (target app does not see ANY user app) applied to both packages.
- Config: `/data/misc/hide_my_applist_hmaosspreseedab/config.json` (the service reuses the 1st `hide_my_applist*` dir under /data/misc; this one was pre-seeded and adopted).
- Config format reference (CONFIG_VERSION=93) in `hma-oss/*.kt`.

## Operational lessons

1. **HMA-OSS: config only applies LIVE via manager app** (ServiceClient, verified signature). Editing the JSON on disk only takes effect on the next boot (service reads it once at init).
2. **"Enable" alone = empty blacklist (hides nothing).** Required: "Hide" mode (whitelist) + template applied.
3. Proof of filtering in the log: `<datadir>/log/runtime.log` → `@shouldFilterApplication: query from <pkg>`.
4. **TrickyStore daemon anti-tamper:** verifies module file integrity; any edit (e.g. `DEBUG=true` in service.sh) = daemon dies with silent exit 1. Manual restart: `cd /data/adb/modules/tricky_store && (setsid sh ./service.sh >/dev/null 2>&1 </dev/null &)`. NEVER `pkill -f TrickyStore` via adb shell (kills the shell itself — the cmdline contains the string).
5. New banking app blocking with this screen: HMA-OSS → "Manage apps" → app → "Enable" + "Hide" + template `bancos`. No reboot.

## Artifacts

- `launch-log1..5.txt` — launch logs (log5 = the `me.weishu.kernelsu have been installed` digest + TerminateException).
- `ui-dump1.xml` — the blocking screen (dialog titled "Bradesco Seguros" + OK button).
- `hma-oss/` — pre-seeded config + format sources (JsonConfig/ConfigManager/HMAService/Constants, repo frknkrc44/HMA-OSS).
- APKs and native libs live on disk (gitignored). Regeneratable jadx decompilation: `tools/jadx-pull/jadx`.
