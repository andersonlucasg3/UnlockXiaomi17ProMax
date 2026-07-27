# Análise do APK YT Music Morphe — Patch "Bypass Certificate Checks"

**Data:** 2025-07-25  
**APK:** `app.morphe.android.apps.youtube.music` (YT Music 9.15.51 + Morphe)  
**Dispositivo:** `4d7fc9af`

---

## 1. Estrutura do APK

- **Caminho no device:** `/data/app/~~6MFJRkXXyIUBLfXzdjDSPA==/app.morphe.android.apps.youtube.music-gpl_U_glRKACspURZGG5CQ==/base.apk`
- **Splits:** Nenhum — apenas `base.apk` (80 MB)
- **Arquivos .dex:** 11 (classes.dex até classes10.dex)

---

## 2. Marcadores Morphe Encontrados

O APK contém branding Morphe:

- `MORPHE_BRANDING.TXT`
- `MORPHE_LICENSE.TXT`
- `MORPHE_LICENSE_NOTICE.TXT`
- Recursos gráficos: `morphe_header_custom_dark.png`, `morphe_adaptive_*`, etc.

**Patches Morphe identificados** (via classes `app/morphe/extension/*`):

| Categoria | Patches |
|-----------|---------|
| Music | ChangeHeaderPatch, ChangeMiniplayerColorPatch, ChangeStartPagePatch, CrossfadeManager, DisableDislikeRedirectionPatch, DownloadsPatch, EnableForcedMiniplayerPatch, EnableSwipeToDismissMiniplayerPatch, HideAdsPatch, HideButtonsPatch, HideFilterBarPatch, HideFlyoutMenuComponentsPatch, MiniplayerPreviousNextButtonsPatch, NavigationBarPatch, RememberRepeatStatePatch, RememberShuffleStatePatch, ReturnYouTubeDislikePatch, SettingsMenuFilterPatch, VersionCheckPatch, ThemePatch, SpoofVideoStreamsPatch, ScrobblePatch |
| Shared | AppCheckPatch, CheckEnvironmentPatch, CheckWatchHistoryDomainNameResolutionPatch, CustomBrandingPatch, DisableDRCAudioPatch, DisableQUICProtocolPatch, EnableDebuggingPatch, ExperimentalAppNoticePatch, FixRecycledBitmapPatch, ForceOriginalAudioPatch, GmsCoreSupportPatch, HideFullscreenAdsPatch, InitializationPatch, NetworkProxyPatch, SanitizeSharingLinksPatch, SpoofAppVersionPatch, TreeNodeElementPatch |

**NÃO foi encontrado nenhum patch relacionado a "BypassCertificate" ou bypass de verificação de certificado.**

- `AppCheckPatch` — apenas detecta se é YouTube ou YouTube Music (não tem relação com certificados)
- `GmsCoreSupportPatch` — gerencia microG (battery optimization, etc.), não bypassa certificados
- Não há referência a `BypassCertificate`, `bypassCert`, `certificate bypass` em nenhum arquivo decompilado

---

## 3. Fluxo do Crash (Android Auto → MediaBrowserService)

### Stack trace do crash:

```
java.lang.IllegalStateException: Missing DynamiteApplicationContext.
    at com.google.android.gms.common.GoogleCertificatesImpl.a(...)
    at com.google.android.gms.common.GoogleCertificatesImpl.<init>(...)
    at java.lang.Class.newInstance(Native Method)
    at tqd.c(PG:11)          ← Class.forName(...).newInstance()
    at tbz.c(PG:30)          ← tqd.d(...).c("com.google.android.gms.common.GoogleCertificatesImpl")
    at tcn.a(PG:32)          ← verificação de assinatura
    at tcn.c(PG:1)           ← return a(str).b
    at kxo.g(PG:85)          ← !this.r.c(avgiVar.b) — verificação de certificate do caller
    at com.google.android.apps.youtube.music.mediabrowser.MusicBrowserService.f(PG:242)
    at bzl.onGetRoot(PG:116) ← ponto de entrada do MediaBrowserService
```

### Fluxo detalhado:

```
bzl.onGetRoot()
  └─ MusicBrowserService.f(packageName, bundle)    [linha 45: caiVar2.f(str, bundle3)]
       └─ kxo.g(avgiVar, avesVar)                   [linha 297-319]
            └─ this.r.c(avgiVar.b)                  [linha 313]  ← this.r = tcn instance
                 └─ tcn.c(packageName)              [linha 75-77]
                      └─ tcn.a(packageName)          ← retorna tch (resultado da verificação)
                           └─ tbz.c()               [linha 62-80]
                                └─ tqd.d(ctx, "com.google.android.gms.googlecertificates")
                                     .c("com.google.android.gms.common.GoogleCertificatesImpl")
                                                      [linha 70]
                                     └─ tqd.c(className)  [linha 319-325]
                                          └─ classLoader.loadClass(str).newInstance()
                                              ↑ CRASHA AQUI ↑
                                              GoogleCertificatesImpl precisa de
                                              DynamiteApplicationContext que não
                                              existe (microG não provê)
```

---

## 4. Código Decompilado Relevante

### 4.1 `bzl.onGetRoot()` — Ponto de entrada (classes7.dex)

```java
// bzl.java, linha 45
defpackage.bze f = caiVar2.f(str, bundle3);
```

### 4.2 `kxo.g()` — AllowlistManager, decide se o caller é autorizado (classes.dex)

```java
// kxo.java, linhas 297-319
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
        // ***** ESTA É A LINHA QUE DISPARA O CRASH *****
        if ((avgiVar.b() || (!ckve.c(avgiVar, avfb.a) && !ckve.c(avgiVar, avfc.a)))
            && !this.r.c(avgiVar.b)    // ← tcn.c(packageName) — verificação Google
            && !m(avgiVar)) {          // ← verificação SHA-256 fingerprint
            return false;
        }
        return true;
    }
    return false;
}
```

**Ponto crítico:** `!this.r.c(avgiVar.b)` na linha 313 chama `tcn.c(packageName)` que dispara toda a cadeia de verificação de certificado Google.

### 4.3 `tcn.c()` e `tcn.a()` — GoogleSignatureVerifier (classes.dex)

```java
// tcn.java, linha 75-77
public final boolean c(String str) {
    return a(str).b;   // retorna true se o pacote tem assinatura Google válida
}
```

O método `a()` (não completamente decompilado) faz a verificação de assinatura. Ele chama `tbz.c()` para obter o serviço remoto de certificados.

### 4.4 `tbz.c()` — GoogleCertificates, carrega o módulo GMS (classes5.dex)

```java
// tbz.java, linhas 62-80
static void c() {
    if (h != null) return;
    Preconditions.checkNotNull(g);
    synchronized (i) {
        if (h == null) {
            // ★ ESTA LINHA CAUSA O CRASH ★
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

### 4.5 `tqd.c()` — DynamiteModule, instanciação por reflexão (classes.dex)

```java
// tqd.java, linhas 319-325
public final IBinder c(String str) {
    try {
        // ★ O CRASH OCORRE AQUI ★
        return (IBinder) this.f.getClassLoader().loadClass(str).newInstance();
        // Tenta instanciar: com.google.android.gms.common.GoogleCertificatesImpl
        // GoogleCertificatesImpl.<init>() chama GoogleCertificatesImpl.a()
        // que exige DynamiteApplicationContext → IllegalStateException
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e2) {
        throw new tpz("Failed to instantiate module class: ".concat(str), e2);
    }
}
```

---

## 5. Conclusão

### **PATCH AUSENTE — "Bypass certificate checks" NÃO está aplicado.**

**Evidências:**

1. **Nenhuma classe `BypassCertificate*`** existe no APK — nem no namespace `app.morphe.extension.*` nem em lugar algum.
2. **Nenhuma referência a "bypass certificate"** em strings, classes ou métodos do APK.
3. **As classes do caminho de verificação (`tcn`, `tbz`, `tqd`, `kxo`) têm ZERO referências a Morphe** — ou seja, não foram modificadas por patch algum.
4. **O código de verificação de certificado está 100% intacto** e ativo:
   - `tbz.c()` ainda chama `tqd.d().c("GoogleCertificatesImpl")` (linha 70)
   - `tqd.c()` ainda faz `classLoader.loadClass(str).newInstance()` (linha 321)
   - `kxo.g()` ainda chama `this.r.c(packageName)` (linha 313)
5. **O crash ocorre exatamente porque a verificação está ativa** — o `GoogleCertificatesImpl` tenta acessar `DynamiteApplicationContext` que o microG não provê corretamente.

### Por que o patch deveria existir?

O patch "Bypass certificate checks" do Morphe (com `default: true`) deveria modificar a cadeia de verificação para que:
- `kxo.g()` retornasse `true` sem chamar `tcn.c()`, OU
- `tcn.c()` retornasse `true` sem chamar `tbz.c()`, OU
- `tbz.c()` retornasse um mock ao invés de instanciar `GoogleCertificatesImpl`

Nenhuma dessas modificações está presente.

---

## 6. Recomendação

**Re-patchar o APK com o Morphe**, certificando-se de que o patch **"Bypass certificate checks"** está **habilitado** (ele é `default: true`, mas pode ter sido desabilitado na configuração).

O ponto exato onde injetar o bypass (para referência de um possível fix manual):

- **Opção A (mais segura):** Modificar `kxo.g()` para retornar `true` antes da linha 313 (`!this.r.c(avgiVar.b)`)
- **Opção B:** Modificar `tcn.c()` para retornar `true` diretamente, sem chamar `a(str)`
- **Opção C:** Modificar `tbz.c()` para não instanciar `GoogleCertificatesImpl` — retornar um stub que sempre diz "válido"

---

## 7. Artefatos

- APK original: `analysis/ytmusic-morphe/base.apk` (80 MB)
- Dex extraídos: `analysis/ytmusic-morphe/classes*.dex` (11 arquivos)
- Output do jadx: `analysis/ytmusic-morphe/jadx-output/` (36.150 classes)
