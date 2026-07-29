# Analysis of YT Music Morphe APK — "Bypass Certificate Checks" Patch

**Date:** 2025-07-25  
**APK:** `app.morphe.android.apps.youtube.music` (YT Music 9.15.51 + Morphe)  
**Device:** `4d7fc9af`

---

## 1. APK Structure

- **Path on device:** `/data/app/~~6MFJRkXXyIUBLfXzdjDSPA==/app.morphe.android.apps.youtube.music-gpl_U_glRKACspURZGG5CQ==/base.apk`
- **Splits:** None — only `base.apk` (80 MB)
- **.dex files:** 11 (classes.dex through classes10.dex)

---

## 2. Morphe Markers Found

The APK contains Morphe branding:

- `MORPHE_BRANDING.TXT`
- `MORPHE_LICENSE.TXT`
- `MORPHE_LICENSE_NOTICE.TXT`
- Graphic resources: `morphe_header_custom_dark.png`, `morphe_adaptive_*`, etc.

**Morphe patches identified** (via `app/morphe/extension/*` classes):

| Category | Patches |
|----------|---------|
| Music | ChangeHeaderPatch, ChangeMiniplayerColorPatch, ChangeStartPagePatch, CrossfadeManager, DisableDislikeRedirectionPatch, DownloadsPatch, EnableForcedMiniplayerPatch, EnableSwipeToDismissMiniplayerPatch, HideAdsPatch, HideButtonsPatch, HideFilterBarPatch, HideFlyoutMenuComponentsPatch, MiniplayerPreviousNextButtonsPatch, NavigationBarPatch, RememberRepeatStatePatch, RememberShuffleStatePatch, ReturnYouTubeDislikePatch, SettingsMenuFilterPatch, VersionCheckPatch, ThemePatch, SpoofVideoStreamsPatch, ScrobblePatch |
| Shared | AppCheckPatch, CheckEnvironmentPatch, CheckWatchHistoryDomainNameResolutionPatch, CustomBrandingPatch, DisableDRCAudioPatch, DisableQUICProtocolPatch, EnableDebuggingPatch, ExperimentalAppNoticePatch, FixRecycledBitmapPatch, ForceOriginalAudioPatch, GmsCoreSupportPatch, HideFullscreenAdsPatch, InitializationPatch, NetworkProxyPatch, SanitizeSharingLinksPatch, SpoofAppVersionPatch, TreeNodeElementPatch |

**NO patch related to "BypassCertificate" or certificate verification bypass was found.**

- `AppCheckPatch` — only detects whether it is YouTube or YouTube Music (not related to certificates)
- `GmsCoreSupportPatch` — manages microG (battery optimization, etc.), does not bypass certificates
- No reference to `BypassCertificate`, `bypassCert`, `certificate bypass` in any decompiled file

---

## 3. Crash Flow (Android Auto → MediaBrowserService)

### Crash stack trace:

```
java.lang.IllegalStateException: Missing DynamiteApplicationContext.
    at com.google.android.gms.common.GoogleCertificatesImpl.a(...)
    at com.google.android.gms.common.GoogleCertificatesImpl.<init>(...)
    at java.lang.Class.newInstance(Native Method)
    at tqd.c(PG:11)          ← Class.forName(...).newInstance()
    at tbz.c(PG:30)          ← tqd.d(...).c("com.google.android.gms.common.GoogleCertificatesImpl")
    at tcn.a(PG:32)          ← signature verification
    at tcn.c(PG:1)           ← return a(str).b
    at kxo.g(PG:85)          ← !this.r.c(avgiVar.b) — caller certificate verification
    at com.google.android.apps.youtube.music.mediabrowser.MusicBrowserService.f(PG:242)
    at bzl.onGetRoot(PG:116) ← MediaBrowserService entry point
```

### Detailed flow:

```
bzl.onGetRoot()
  └─ MusicBrowserService.f(packageName, bundle)    [line 45: caiVar2.f(str, bundle3)]
       └─ kxo.g(avgiVar, avesVar)                   [line 297-319]
            └─ this.r.c(avgiVar.b)                  [line 313]  ← this.r = tcn instance
                 └─ tcn.c(packageName)              [line 75-77]
                      └─ tcn.a(packageName)          ← returns tch (verification result)
                           └─ tbz.c()               [line 62-80]
                                └─ tqd.d(ctx, "com.google.android.gms.googlecertificates")
                                     .c("com.google.android.gms.common.GoogleCertificatesImpl")
                                                      [line 70]
                                     └─ tqd.c(className)  [line 319-325]
                                          └─ classLoader.loadClass(str).newInstance()
                                              ↑ CRASHES HERE ↑
                                              GoogleCertificatesImpl needs
                                              DynamiteApplicationContext which does
                                              not exist (microG does not provide it)
```

---

## 4. Relevant Decompiled Code

### 4.1 `bzl.onGetRoot()` — Entry point (classes7.dex)

```java
// bzl.java, line 45
defpackage.bze f = caiVar2.f(str, bundle3);
```

### 4.2 `kxo.g()` — AllowlistManager, decides whether the caller is authorized (classes.dex)

```java
// kxo.java, lines 297-319
public final boolean g(defpackage.avgi avgiVar, defpackage.aves avesVar) {
    boolean contains;
    if (!avgiVar.b()) {
        if (!j(avgiVar)) {
            if ((Build.VERSION.SDK_INT >= 30 && avgiVar.equals(avff.a) && n(avgiVar))
                || avgiVar.a(this.q.getPackageName())) {
                return true;  // self-check OK
            }
        } else {
            return true;  // Android Automotive, self-signed OK
        }
    }
    Set set = this.s;
    synchronized (set) {
        contains = set.contains(avesVar);
    }
    if (contains) {
        // ***** THIS IS THE LINE THAT TRIGGERS THE CRASH *****
        if ((avgiVar.b() || (!ckve.c(avgiVar, avfb.a) && !ckve.c(avgiVar, avfc.a)))
            && !this.r.c(avgiVar.b)    // ← tcn.c(packageName) — Google verification
            && !m(avgiVar)) {          // ← SHA-256 fingerprint verification
            return false;
        }
        return true;
    }
    return false;
}
```

**Critical point:** `!this.r.c(avgiVar.b)` at line 313 calls `tcn.c(packageName)` which triggers the entire Google certificate verification chain.

### 4.3 `tcn.c()` and `tcn.a()` — GoogleSignatureVerifier (classes.dex)

```java
// tcn.java, lines 75-77
public final boolean c(String str) {
    return a(str).b;   // returns true if the package has a valid Google signature
}
```

The `a()` method (not fully decompiled) performs signature verification. It calls `tbz.c()` to get the remote certificate service.

### 4.4 `tbz.c()` — GoogleCertificates, loads the GMS module (classes5.dex)

```java
// tbz.java, lines 62-80
static void c() {
    if (h != null) return;
    Preconditions.checkNotNull(g);
    synchronized (i) {
        if (h == null) {
            // ★ THIS LINE CAUSES THE CRASH ★
            IBinder c2 = tqd.d(g, tqd.d, "com.google.android.gms.googlecertificates")
                .c("com.google.android.gms.common.GoogleCertificatesImpl");
            if (c2 == null) {
                tjfVar = null;
            } else {
                IInterface queryLocalInterface = c2.queryLocalInterface(
                    "com.google.android.gms.common.internal.IGoogleCertificatesApi");
                tjfVar = queryLocalInterface instanceof tjf
                    ? (tjf) queryLocalInterface : new tjf(c2);
            }
            h = tjfVar;
        }
    }
}
```

### 4.5 `tqd.c()` — DynamiteModule, reflection instantiation (classes.dex)

```java
// tqd.java, lines 319-325
public final IBinder c(String str) {
    try {
        // ★ THE CRASH OCCURS HERE ★
        return (IBinder) this.f.getClassLoader().loadClass(str).newInstance();
        // Attempts to instantiate: com.google.android.gms.common.GoogleCertificatesImpl
        // GoogleCertificatesImpl.<init>() calls GoogleCertificatesImpl.a()
        // which requires DynamiteApplicationContext → IllegalStateException
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e2) {
        throw new tpz("Failed to instantiate module class: ".concat(str), e2);
    }
}
```

---

## 5. Conclusion

### **MISSING PATCH — "Bypass certificate checks" is NOT applied.**

**Evidence:**

1. **No `BypassCertificate*` class** exists in the APK — neither in the `app.morphe.extension.*` namespace nor anywhere else.
2. **No reference to "bypass certificate"** in strings, classes, or methods of the APK.
3. **The classes in the verification path (`tcn`, `tbz`, `tqd`, `kxo`) have ZERO references to Morphe** — i.e., they were not modified by any patch.
4. **The certificate verification code is 100% intact** and active:
   - `tbz.c()` still calls `tqd.d().c("GoogleCertificatesImpl")` (line 70)
   - `tqd.c()` still does `classLoader.loadClass(str).newInstance()` (line 321)
   - `kxo.g()` still calls `this.r.c(packageName)` (line 313)
5. **The crash occurs precisely because the verification is active** — `GoogleCertificatesImpl` tries to access `DynamiteApplicationContext` which microG does not properly provide.

### Why should the patch exist?

The Morphe "Bypass certificate checks" patch (with `default: true`) should modify the verification chain so that:
- `kxo.g()` would return `true` without calling `tcn.c()`, OR
- `tcn.c()` would return `true` without calling `tbz.c()`, OR
- `tbz.c()` would return a mock instead of instantiating `GoogleCertificatesImpl`

None of these modifications are present.

---

## 6. Recommendation

**Re-patch the APK with Morphe**, ensuring that the **"Bypass certificate checks"** patch is **enabled** (it is `default: true`, but may have been disabled in the configuration).

The exact point to inject the bypass (for reference for a possible manual fix):

- **Option A (safest):** Modify `kxo.g()` to return `true` before line 313 (`!this.r.c(avgiVar.b)`)
- **Option B:** Modify `tcn.c()` to return `true` directly, without calling `a(str)`
- **Option C:** Modify `tbz.c()` to not instantiate `GoogleCertificatesImpl` — return a stub that always says "valid"

---

## 7. Artifacts

- Original APK: `analysis/ytmusic-morphe/base.apk` (80 MB)
- Extracted dex: `analysis/ytmusic-morphe/classes*.dex` (11 files)
- jadx output: `analysis/ytmusic-morphe/jadx-output/` (36,150 classes)
