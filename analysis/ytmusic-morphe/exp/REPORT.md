# APK Analysis YT Music Morphe 9.28.51 — "Bypass Certificate Checks" Patch

**Date:** 2025-07-25  
**APK:** `app.morphe.android.apps.youtube.music` (YT Music **9.28.51**, versionCode 92851240)  
**Device:** `4d7fc9af`, installed 2026-07-27 07:58

---

## 1. APK Structure

- **Path:** `/data/app/~~3gwFUH13MPQfR_TZb43Ahw==/app.morphe.android.apps.youtube.music-JLNrw_fjA6y8lfFa1-xZUg==/base.apk`
- **Size:** 74 MB (vs 80 MB in 9.15.51)
- **Splits:** None — only `base.apk`
- **.dex files:** 11 (classes.dex through classes10.dex)

---

## 2. Class Mapping (Obfuscation changed)

Version 9.28.51 has different obfuscated names from 9.15.51. Mapping:

| Role | 9.15.51 (old) | 9.28.51 (new) | Dex |
|-------|------------------|----------------|-----|
| AllowlistManager | `kxo` | `laj` | classes.dex |
| GoogleSignatureVerifier | `tcn` | `tny` | classes.dex |
| GoogleCertificates | `tbz` | `tnk` | classes5.dex |
| DynamiteModule | `tqd` | `ubo` | classes.dex |
| MediaBrowserService wrapper | `bzl` | different name | classes7.dex |
| MusicBrowserService | (same) | (same) | classes7.dex |

---

## 3. Main Conclusion

## 🔴 PATCH MISSING — "Bypass Certificate Checks" is NOT applied.

**Evidence:**

1. **No `BypassCertificate*` class** or similar in the `app.morphe.extension.*` namespace
2. **No "bypass cert" string** anywhere in the APK (searched across 11 .dex files and 37,170 decompiled classes)
3. **The 4 classes in the verification chain** (`laj`, `tny`, `tnk`, `ubo`) have **zero references to Morphe** — they were not modified
4. **The call to `GoogleCertificatesImpl` remains intact** in `tnk.java:70`:
   ```java
   android.os.IBinder c2 = ubo.d(g, ubo.d, "com.google.android.gms.googlecertificates")
       .c("com.google.android.gms.common.GoogleCertificatesImpl");
   ```
5. **The method `laj.g()` (AllowlistManager)** still calls `this.r.c(packageName)` at line 309 — Google signature verification is active

### Why did the crash disappear but loading is infinite?

**Most likely hypothesis:** `GoogleCertificatesImpl` can now be instantiated (possibly because microG/GMS on the device was updated or the Dynamite module is loading correctly). However, the verification returns `false` because the app **is not Google-signed** (it's a Morphe re-packaged APK). This causes:

```
Android Auto connects
  → bzl.onGetRoot()
    → MusicBrowserService.f()
      → laj.g(awkpVar, awizVar)        [AllowlistManager]
        → this.r.c(awkpVar.b)            [tny.c() — GoogleSignatureVerifier]
          → tny.a(packageName)
            → tnk.c()                    [GoogleCertificates]
              → ubo.d().c("GoogleCertificatesImpl")
                → ✅ NO LONGER CRASHES (GMS loads)
                → ❌ Returns "not Google-signed"
        → laj.g() returns false
      → MusicBrowserService.f() returns "__EMPTY_ROOT_ID__"
    → Android Auto receives empty root
    → 🔄 Android Auto tries again → INFINITE LOOP
```

**Summary:** Before, it crashed before reaching the decision. Now it reaches the "is Google-signed?" decision and the answer is NO. Without the bypass, the app never authorizes Android Auto.

---

## 4. Certificate Verification Chain (version 9.28.51)

### 4.1 `laj.g()` — AllowlistManager (ex-`kxo`)

```java
// laj.java, lines 293-315
public final boolean g(awkp awkpVar, awiz awizVar) {
    boolean contains;
    if (!awkpVar.b()) {
        if (!j(awkpVar)) {
            // Self-check: if it's the app itself OR Android Automotive with same signature
            if ((Build.VERSION.SDK_INT >= 30 && awkpVar.equals(awjm.a) && n(awkpVar))
                || awkpVar.a(this.q.getPackageName())) {
                return true;  // ✅ self-check passes
            }
        } else {
            return true;  // Android Automotive: self-signed OK
        }
    }
    Set set = this.s;
    synchronized (set) {
        contains = set.contains(awizVar);  // is it on the allowlist?
    }
    if (contains) {
        // ★ CRITICAL POINT — this condition decides whether the caller is accepted ★
        if ((awkpVar.b() || (!cnnf.c(awkpVar, awji.a) && !cnnf.c(awkpVar, awjj.a)))
            && !this.r.c(awkpVar.b)     // ← tny.c(packageName): is it Google-signed?
            && !m(awkpVar)) {           // ← known SHA-256 fingerprint?
            return false;  // ❌ NOT authorized
        }
        return true;
    }
    return false;
}
```

**What a bypass patch would do:** Would modify this condition to NEVER execute `!this.r.c(awkpVar.b)`, either by:
- Returning `true` before the condition, or
- Replacing `!this.r.c(awkpVar.b)` with `false` (since the condition uses `&&`)

### 4.2 `tny.c()` — GoogleSignatureVerifier (ex-`tcn`)

```java
// tny.java, lines 78-80
public final boolean c(String str) {
    return a(str).b;   // delegates to a(str) which does the actual verification
}
```

### 4.3 `tnk.c()` — GoogleCertificates (ex-`tbz`)

```java
// tnk.java, lines 62-80
static void c() {
    tvc tvcVar;
    if (h != null) return;
    Preconditions.checkNotNull(g);
    synchronized (i) {
        if (h == null) {
            // ★ STILL TRYING TO INSTANTIATE GoogleCertificatesImpl ★
            IBinder c2 = ubo.d(g, ubo.d, "com.google.android.gms.googlecertificates")
                .c("com.google.android.gms.common.GoogleCertificatesImpl");
            // ... queryLocalInterface for IGoogleCertificatesApi
            h = tvcVar;
        }
    }
}
```

### 4.4 `ubo.c()` — DynamiteModule (ex-`tqd`)

```java
// ubo.java, lines 541-547
public final IBinder c(String str) {
    try {
        return (IBinder) this.f.getClassLoader().loadClass(str).newInstance();
        // Tries to instantiate: com.google.android.gms.common.GoogleCertificatesImpl
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e2) {
        throw new ubk("Failed to instantiate module class: ".concat(str), e2);
    }
}
```

---

## 5. Morphe Patches Present (full list)

Same patches as the previous version. No new patches related to Android Auto, MediaBrowser, or certificates:

| Category | Patches |
|-----------|---------|
| Music | ChangeHeaderPatch, ChangeMiniplayerColorPatch, ChangeStartPagePatch, CrossfadeManager, DisableDislikeRedirectionPatch, DownloadsPatch, EnableForcedMiniplayerPatch, EnableSwipeToDismissMiniplayerPatch, HideAdsPatch, HideButtonsPatch, HideFlyoutMenuComponentsPatch, MiniplayerPreviousNextButtonsPatch, NavigationBarPatch, RememberRepeatStatePatch, RememberShuffleStatePatch, ReturnYouTubeDislikePatch, VersionCheckPatch, ThemePatch, SpoofVideoStreamsPatch, ScrobblePatch |
| Shared | AppCheckPatch, CheckEnvironmentPatch, CheckWatchHistoryDomainNameResolutionPatch, CustomBrandingPatch, DisableDRCAudioPatch, DisableQUICProtocolPatch, EnableDebuggingPatch, ExperimentalAppNoticePatch, FixRecycledBitmapPatch, ForceOriginalAudioPatch, GmsCoreSupportPatch, HideFullscreenAdsPatch, InitializationPatch, NetworkProxyPatch, SanitizeSharingLinksPatch, SpoofAppVersionPatch, TreeNodeElementPatch |

**Note:** `GmsCoreSupportPatch` manages microG (battery optimization dialogs, etc.) — **not** related to certificate bypass. `AppCheckPatch` only detects whether it's YT or YT Music.

---

## 6. Recommendation

**Re-patch with "Bypass certificate checks" EXPLICITLY ENABLED.** The patch was not applied in either of the two tested versions.

### Exact injection points for the bypass:

| Option | File | Line | What to modify |
|-------|---------|-------|-----------------|
| **A (recommended)** | `laj.java` | 309 | Remove `!this.r.c(awkpVar.b)` from the condition, or add `if (true) return true;` before |
| B | `tny.java` | 78-80 | Make `c()` always return `true` |
| C | `tnk.java` | 62-80 | Make `c()` skip instantiation and return a "valid" stub |

---

## 7. Artifacts

- APK: `analysis/ytmusic-morphe/exp/base.apk` (74 MB)
- Dex: `analysis/ytmusic-morphe/exp/classes*.dex` (11 files)
- jadx: `analysis/ytmusic-morphe/exp/jadx-output/` (37,170 classes)

---

## 8. Patch Bundle (pb-0.jar) Investigation — Why didn't the patch apply?

### 8.1 Sources

- **pb-0.jar** (8.5 MB, Jul 26) — current Morphe Manager bundle, **contains** `BypassCertificateChecksPatchKt.class`
- **pb-897837162.jar** (2.6 MB, Jul 23) — old bundle, **does NOT contain** the patch
- Decompiled patch source: `analysis/ytmusic-morphe/patch-src/`

### 8.2 How the patch works

#### Fingerprint (`CheckCertificateFingerprint`)

```java
// CheckCertificateFingerprint.java (decompiled from pb-0.jar)
public final class CheckCertificateFingerprint extends Fingerprint {
    public static final CheckCertificateFingerprint INSTANCE = new CheckCertificateFingerprint();

    private CheckCertificateFingerprint() {
        super(
            null,                                              // customFingerprint
            "Z",                                               // returnType = boolean
            listOf("L"),                                       // parameters = 1 Object param
            null,                                              // opcodes
            listOf(listOf("X509", "isPartnerSHAFingerprint")), // strings to search for
            null,                                              // customResolver
            41,                                                // mask
            null                                               // marker
        );
    }
}
```

**Mask 41 = 0b101001:** bits 0 (customFingerprint), 3 (opcodes) and 5 (customResolver) are IGNORED. Bits 1 (returnType), 2 (parameters) and 4 (strings) are CHECKED.

**The fingerprint searches for:** a method that:
- Returns `boolean` (`"Z"`)
- Takes 1 object parameter (`"L"`)
- Contains the strings `"X509"` and `"isPartnerSHAFingerprint"`

#### Patch action (`BypassCertificateChecksPatchKt`)

```java
// BypassCertificateChecksPatchKt.java (line 52)
BytecodeUtilsKt.returnEarly(
    CheckCertificateFingerprint.INSTANCE.getMethod(execute),
    true   // ← injects "return true" at the start of the method
);
```

The patch **injects `return true`** as the first instruction of the method found by the fingerprint.

#### Configuration in patches-list.json

```json
{
  "name": "Bypass certificate checks",
  "default": true,                    // ← enabled by default
  "compatiblePackages": [{
    "packageName": "com.google.android.apps.youtube.music",
    "targets": [
      { "version": "9.28.51", "isExperimental": true },  // ← compatible with 9.28.51
      { "version": "9.26.51", "isExperimental": true }
    ]
  }]
}
```

### 8.3 Fingerprint × Target Code (9.28.51)

The fingerprint matches perfectly with `laj.m(awkp)` (AllowlistManager, ex-`kxo`):

| Fingerprint Field | Expected value | `laj.m(awkp)` in 9.28.51 | Match? |
|---------------------|----------------|--------------------------|--------|
| returnType | `"Z"` (boolean) | `boolean` (line 61) | ✅ |
| parameters | `["L"]` (1 Object) | `(awkp awkpVar)` — 1 parameter | ✅ |
| strings | `"X509"` | Present (line 68) | ✅ |
| strings | `"isPartnerSHAFingerprint"` | Present (lines 120,134,145,161) | ✅ |

**Conclusion: The fingerprint MATCHES PERFECTLY with the 9.28.51 code.** The target method exists, has the correct signature, and contains the expected strings.

### 8.4 What the patch would modify

If applied, `laj.m()` would become:

```java
private final boolean m(awkp r11) {
    return true;  // ← INJECTED by the patch (BytecodeUtilsKt.returnEarly)
    // ... all original SHA-256 verification code becomes unreachable
}
```

This would make `laj.g()` (line 309) always fall through to `return true`:

```java
// laj.g() — original condition:
if (... && !this.r.c(awkpVar.b) && !m(awkpVar)) {
//                                   ↑ m() always true → !true = false
//                                   false breaks the && → entire condition = false
    return false;  // ← NEVER executes
}
return true;  // ← ALWAYS executes → Android Auto authorized
```

### 8.5 Why is the patch NOT in the APK?

**Confirmed hypothesis: (b) The patch WAS NOT SELECTED in the Morphe Manager UI.**

Evidence ruling out hypothesis (a) "fingerprint doesn't match":
- The fingerprint matches **perfectly** with `laj.m()` in 9.28.51 (see table above)
- The target method is present and has exactly the same structure as in 9.15.51
- The strings "X509" and "isPartnerSHAFingerprint" are present in the dex bytes

**Likely cause:** When the user first opened Morphe Manager, they were using the `pb-897837162.jar` bundle (which does NOT contain the patch). The patch selection was saved. When switching to `pb-0.jar` (which CONTAINS the patch), the "Bypass certificate checks" patch appeared as a new **unselected** item (because the saved config did not include it). The user patched without noticing the new patch was deselected.

Contributing factors:
1. The patch is `isExperimental: true` for 9.28.51 — some managers hide experimental patches by default
2. The patch is new in the bundle — it didn't exist in the previous selection
3. Morphe Manager persists selection between sessions — new patches are not automatically selected

### 8.6 Solution

**Re-patch with Morphe Manager, ensuring "Bypass certificate checks" is VISIBLY CHECKED (checkbox ticked) before applying.**

If the manager is hiding experimental patches, enable the "Show experimental patches" option first.
