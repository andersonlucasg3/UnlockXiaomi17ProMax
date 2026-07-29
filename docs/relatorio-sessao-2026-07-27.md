# Session Report — 2026-07-27

**Objective:** Get YT Music Morphe working on Android Auto (MediaBrowserService) on the Xiaomi 17 Pro Max with microG + real Google Play Services.

**Device:** Xiaomi 17 Pro Max (popsicle) | xiaomi.eu OS3.0.317.0.WPBCNXM | Android 16 | KSU LKM 32558

---

## ✅ CURRENT STATUS

**YT Music Morphe 9.15.51 working on Android Auto ✅** — podcasts load, music requires YouTube Premium (server-side, see §Verdict premium/podcasts). No crash, no infinite spinner. Stack:

| Component | Detail |
|---|---|
| APK | YT Music 9.15.51 stock (APKMirror) |
| Patches (37) | user selection via Morphe CLI (bundle v1.37.0) |
| Additional fix | `tcn.c(String) → return false` (dex-only surgery via baksmali/smali) |
| Signature | manager.keystore (BKS v2, storepass "", alias "Morphe", keypass "Morphe") |
| Upstream PR | https://github.com/MorpheApp/morphe-patches/pull/2239 |

---

## 1. Diagnosis — crash chain

### 1.1 Initial symptom

YT Music Morphe **crashed** when Android Auto connected to `MediaBrowserService`:

```
java.lang.IllegalStateException: Missing DynamiteApplicationContext.
    at com.google.android.gms.common.GoogleCertificatesImpl.<init>(...)
    at java.lang.Class.newInstance(Native Method)
    at tqd.c(PG:11)          ← DynamiteModule: Class.forName().newInstance()
    at tbz.c(PG:30)          ← GoogleCertificates: loads GoogleCertificatesImpl
    at tcn.a(PG:32)          ← GoogleSignatureVerifier: signature verification
    at tcn.c(PG:1)
    at kxo.g(PG:85)          ← AllowlistManager: "is this caller Google-signed?"
    at MusicBrowserService.f(PG:242)  ← onGetRoot
    at bzl.onGetRoot(PG:116)
```

### 1.2 Classes involved (9.15.51, ProGuard obfuscation)

| Abbrev | Class | Function |
|--------|-------|----------|
| `kxo` | AllowlistManager | Decides whether the caller (e.g. gearhead) can browse. Method `g(avgi, aves)` = main gate, `h(aves)` = browsability gate, `m(avgi)` = SHA-256 fingerprint |
| `tcn` | GoogleSignatureVerifier | Verifies whether a package is Google-signed. `c(String)` = entry point, calls `a(String)` → `tbz` |
| `tbz` | GoogleCertificates | Loads `GoogleCertificatesImpl` via Dynamite (GMS). Method `c()` does `tqd.d("googlecertificates").c("GoogleCertificatesImpl")` |
| `tqd` | DynamiteModule | Loads GMS classes via reflection. `c(String)` = `classLoader.loadClass(str).newInstance()` |

### 1.3 Why it crashes

The **real** GMS `GoogleCertificatesImpl` (installed on the device, not microG) requires `DynamiteApplicationContext` in its constructor. This context is never initialized because the app uses **microG** (GmsCore support patch redirection). The `IllegalStateException` is not caught (the code only catches `tpz`/`RemoteException`), so the process dies.

**On devices without real GMS** (microG only), `tbz.c()` fails gracefully with `tpz` → returns "not signed" → `kxo.g()` evaluates the rest of the condition (including `m()`) → works. That's why the official Morphe patch (`return true` in `kxo.m`) is enough for most.

### 1.4 Three builds without the patch — why

| Build | Date | Bundle | Reason |
|-------|------|--------|--------|
| 9.15.51 (23/Jul) | 23/Jul | pb-897837162 ("Rushi's Patches") | Old bundle **did not contain** the BypassCertificateChecks patch |
| 9.28.51 experimental | 26/Jul | pb-0.jar (v1.37.0) | New bundle **had** the patch, but the manager's saved selection (from the session with the old bundle) **did not include it** — was left unchecked in the UI |
| 9.15.51 re-patch | 27/Jul 09:43 | pb-0.jar | Process `app.morphe.manager:Patcher` suffered **SIGABRT due to OOM** (1.28 GB mmap failed at VM startup) ~1 min before installation → APK came out without the patch |

---

## 2. Validated fix

### 2.1 Patch via Morphe CLI

Morphe Manager could not apply the patch (OOM). We used **Morphe CLI** (morphe-desktop-1.12.0-all.jar) on the PC with the `pb-0.jar` bundle extracted from the manager itself:

```
java -jar morphe-cli.jar patch --patches pb-0.jar -e "Bypass certificate checks" -o output.apk stock.apk
```

**Problem:** The CLI signs with the public "Morphe" keystore → `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (different signature from the installed APK).

**Solution:** Extract the manager's keystore from the device:
- `/data/data/app.morphe.manager/app_signing/morphe.keystore` (BKS v2, alias "Morphe", store password **empty**, key password "Morphe")
- Sign with `SignApk.java` (apksig + BouncyCastle from the CLI jar itself) → `install -r` preserves data

### 2.2 The official patch is not enough with real GMS

After applying the Morphe patch (`return true` in `kxo.m`), the crash **persisted**. Frida proved at runtime:

- `kxo.h()` returned `false` for gearhead (allowlist `t`)
- The Dynamite crash happened **inside `tcn.c()`** (= `this.r.c()` in `kxo.g`), **BEFORE** `m()` was evaluated

That is: the patch neutralizes the third term of the condition (`!m()`), but `!this.r.c()` is evaluated **first** and **crashes** before reaching `m()`.

### 2.3 Fix #1 (broad, validated): `kxo.g` and `kxo.h` → `return true`

Edit via apktool: `g()` and `h()` return `true` immediately. AA worked.

**Problem:** apktool rebuilds **corrupt resources** — the app crashed on the phone UI with `Resources$NotFoundException res/d9P.xml`. apktool repacks resources and breaks references.

### 2.4 Fix #2 (minimal, chosen for upstream): `tcn.c(String) → return false`

**Method:** **dex-only** surgery (baksmali/smali 3.0.9 fat jars):

```smali
# tcn.smali — before:
.method public final c(Ljava/lang/String;)Z
    .registers 2
    invoke-virtual {p0, p1}, Ltcn;->a(Ljava/lang/String;)Ltch;
    move-result-object p0
    iget-boolean p0, p0, Ltch;->b:Z
    return p0
.end method

# after:
.method public final c(Ljava/lang/String;)Z
    .registers 2
    const/4 v0, 0x0
    return v0
.end method
```

**Effect:** `tcn.c()` reports "not Google-signed" **without touching Dynamite**. In `kxo.g()`:

```java
!this.r.c(pkg) && !m(caller)
→ !false && !true        // c() = false, m() = true (Morphe patch)
→ true && false
→ false                   // condition fails → return true (authorized)
```

**Advantages over Fix #1:**
- Keeps original `kxo.g/h` (allowlists intact — only Dynamite is bypassed)
- Does not corrupt resources (dex-only, without touching resources.arsc/AndroidManifest)
- Identical behavior on devices with/without real GMS

### 2.5 Final build

```
Morphe CLI (--exclusive, 37 patches) → base APK
  → baksmali classes.dex → edit tcn.smali → smali → classes-patched.dex
  → ReplaceDex.java (preserves DEFLATED compression, STORED for .so)
  → SignApk.java (manager.keystore)
  → adb install -r
```

**Final APK:** `analysis/ytmusic-morphe/cli/ytmusic-9.15.51-final-signed.apk`

---

## 3. Verdict "podcasts only on AA"

With all client-side gates open (tcn.c, kxo.m, kxo.g, kxo.h), Android Auto shows **only podcasts** + "upgrade to YouTube Premium" upsell.

**Code analysis:**

- **`khr.e()`** (entitlement/unlimited check) **is already patched to `true`** by Morphe — used in `lbg.java:439` as playback gate
- **`skip_entitlement_check`** is set to `true` for gearhead in `lai.b()`
- The browse tree (`lgp.v()`) is built from **`buol` protobufs returned by the server** (Innertube API)
- **There is no client-side filter** separating music from podcast — the server simply returns different content for Android Auto (free-tier = only podcasts)

**Conclusion: SERVER-SIDE.** Documented at ReVanced#6185. No local patch can add content the server does not send. YouTube Premium accounts get the full catalog on AA too.

---

## 4. Upstream PR

**https://github.com/MorpheApp/morphe-patches/pull/2239**

Extends the "Bypass certificate checks" patch with:

- **`GoogleCertificatesRemoteFingerprint`**: anchors on the string `"Failed to get Google certificates from remote"` (method `tcn.a`)
- **`IsGoogleSignedFingerprint`**: `(String)→boolean` with `classFingerprint = GoogleCertificatesRemoteFingerprint` — isolates `tcn.c`
- **`returnEarly(false)`** in `IsGoogleSignedFingerprint.method` in the patch's `execute {}`

Fork: `andersonlucasg3/morphe-patches`, branch `fix/ytmusic-aa-dynamite-crash`.

Local build did not compile (plugin `app.morphe.patches:1.3.3` requires GitHub Packages auth — Morphe CI has the credentials).

---

## 5. Operational lessons

### Patcher OOM → reboot before patching
The `app.morphe.manager:Patcher` process allocated 1.28 GB of mmap and died from OOM at VM startup. YT Music is ~80 MB; the patcher needs proportional RAM. **Always reboot the device (clears RAM) before patching large APKs in the manager.**

### Reusable manager keystore
Credentials: `/data/data/app.morphe.manager/app_signing/morphe.keystore` (BKS v2), storepass **empty**, alias `Morphe`, keypass `Morphe`. Allows signing CLI builds with the same identity as the manager → `install -r` without uninstalling.

### Dex-only > apktool
apktool corrupted resources in 100% of tested rebuilds (3 attempts). Dex-only surgery (baksmali/smali + ReplaceDex.java) is **reliable** and does not touch resources.arsc, AndroidManifest.xml, or native libs. **Always prefer dex-only for targeted patches.**

### Always check device serial
Device `f10c4f767d7b` (Redmi) showed up on adb during the session. An `install` was accidentally fired at it → `INSTALL_FAILED_USER_RESTRICTED`. **Always verify `adb devices` and use explicit `-s <serial>`.** The popsicle serial can change between connections (was `4d7fc9af`, became `f10c4f767d7b` on one of the reconnections).

### Frida 17 without java-bridge + device quirks
- Frida 17 removed java-bridge from core → scripts need to be bundled (esbuild with `frida-java-bridge` from `tools/frida-agent`)
- `enumerate_processes()` **does not list** YT Music on this device (unknown reason; `ps -A` lists it) → use `pidof` via adb + attach by PID
- YT Music process dies when gearhead is force-stopped → `Java.perform` never fires in spawn-gating
- **Solution:** Python runner with **re-attach loop** (`analysis/ytmusic-morphe/frida/run.py` + `tools/frida-agent/mbs-hook.ts`)

### Frida-server on device is a detection vector
frida-server 17.9.3 was left running on the device after investigation. **Always stop after use** (banking apps detect the `frida-server` process). The binary remains at `analysis/ytmusic-morphe/frida-server` (outside git).

---

## 6. Timeline (day summary)

1. **Morning:** Diagnosis of original crash (9.15.51 from 23/Jul). Discovery: BypassCertificateChecks patch missing from old bundle. Analysis of 3 builds without patch.
2. **Afternoon:** CLI patch → signature issue → manager keystore extraction → `install -r` OK. Persistent crash (Dynamite).
3. **Frida:** runtime proof that `tcn.c` crashes before `m()`. Fix #1 (g/h → true) via apktool → AA works, but apktool corrupts resources.
4. **Fix #2:** tcn.c → false via dex-only → AA works, phone OK, resources intact.
5. **Podcast verdict:** code analysis confirms the cutoff is server-side (Innertube API).
6. **Upstream PR:** #2239 on MorpheApp/morphe-patches with the minimal fix.
7. **Final build:** CLI --exclusive 37 patches + dex-only fix → APK signed and validated.
8. **Cleanup:** frida-server stopped on device.

---

## 7. Session artifacts

```
analysis/ytmusic-morphe/
├── REPORT.md                          # Initial analysis (9.15.51 without patch)
├── exp/REPORT.md                      # 9.28.51 analysis + bundle investigation
├── morphe-patches.json                # Patch config (extracted from manager)
├── pb-0.jar                           # Bundle v1.37.0 (contains BypassCertificateChecks)
├── pb-897837162.jar                   # Old bundle (without the patch)
├── patch-src/                         # Decompilation of pb-0.jar
├── cli/
│   ├── morphe-cli.jar                 # morphe-desktop-1.12.0-all.jar
│   ├── apktool.jar                    # apktool 3.0.3
│   ├── baksmali.jar / smali.jar       # v3.0.9 fat jars
│   ├── SignApk.java                   # Signer with manager.keystore
│   ├── manager.keystore               # ⚠️ SENSITIVE — signing key
│   ├── stock.apk                      # YT Music 9.15.51 original
│   ├── ytmusic-9.15.51-morphe-signed.apk   # Initial CLI build
│   ├── ytmusic-9.15.51-final-signed.apk    # Final build (37 patches + tcn fix)
│   └── final-build.log                # Full CLI log
├── frida-server                       # v17.9.3 (outside git)
└── frida/
    ├── run.py                         # Runner with re-attach loop
    └── hook.js                        # Frida script (bundled mbs-hook.ts)
```
