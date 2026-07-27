# Análise APK YT Music Morphe 9.28.51 — Patch "Bypass Certificate Checks"

**Data:** 2025-07-25  
**APK:** `app.morphe.android.apps.youtube.music` (YT Music **9.28.51**, versionCode 92851240)  
**Dispositivo:** `4d7fc9af`, instalado 2026-07-27 07:58

---

## 1. Estrutura do APK

- **Caminho:** `/data/app/~~3gwFUH13MPQfR_TZb43Ahw==/app.morphe.android.apps.youtube.music-JLNrw_fjA6y8lfFa1-xZUg==/base.apk`
- **Tamanho:** 74 MB (vs 80 MB da 9.15.51)
- **Splits:** Nenhum — apenas `base.apk`
- **Arquivos .dex:** 11 (classes.dex até classes10.dex)

---

## 2. Mapeamento de Classes (Obfuscação mudou)

A versão 9.28.51 tem nomes obfuscados diferentes da 9.15.51. O mapeamento:

| Papel | 9.15.51 (antigo) | 9.28.51 (novo) | Dex |
|-------|------------------|----------------|-----|
| AllowlistManager | `kxo` | `laj` | classes.dex |
| GoogleSignatureVerifier | `tcn` | `tny` | classes.dex |
| GoogleCertificates | `tbz` | `tnk` | classes5.dex |
| DynamiteModule | `tqd` | `ubo` | classes.dex |
| MediaBrowserService wrapper | `bzl` | nome diferente | classes7.dex |
| MusicBrowserService | (mesmo) | (mesmo) | classes7.dex |

---

## 3. Conclusão Principal

## 🔴 PATCH AUSENTE — "Bypass Certificate Checks" NÃO está aplicado.

**Evidências:**

1. **Nenhuma classe `BypassCertificate*`** ou similar no namespace `app.morphe.extension.*`
2. **Nenhuma string "bypass cert"** em todo o APK (busca nos 11 .dex e nas 37.170 classes decompiladas)
3. **As 4 classes da cadeia de verificação** (`laj`, `tny`, `tnk`, `ubo`) têm **zero referências a Morphe** — não foram modificadas
4. **A chamada a `GoogleCertificatesImpl` continua intacta** em `tnk.java:70`:
   ```java
   android.os.IBinder c2 = ubo.d(g, ubo.d, "com.google.android.gms.googlecertificates")
       .c("com.google.android.gms.common.GoogleCertificatesImpl");
   ```
5. **O método `laj.g()` (AllowlistManager)** continua chamando `this.r.c(packageName)` na linha 309 — a verificação de assinatura Google está ativa

### Por que o crash sumiu mas o loading está infinito?

**Hipótese mais provável:** O `GoogleCertificatesImpl` agora consegue ser instanciado (possivelmente porque o microG/GMS no dispositivo foi atualizado ou o Dynamite module está carregando corretamente). Porém, a verificação retorna `false` porque o app **não é assinado pelo Google** (é um APK re-empacotado pelo Morphe). Isso causa:

```
Android Auto connecta
  → bzl.onGetRoot()
    → MusicBrowserService.f()
      → laj.g(awkpVar, awizVar)        [AllowlistManager]
        → this.r.c(awkpVar.b)            [tny.c() — GoogleSignatureVerifier]
          → tny.a(packageName)
            → tnk.c()                    [GoogleCertificates]
              → ubo.d().c("GoogleCertificatesImpl")
                → ✅ NÃO CRASHA MAIS (GMS carrega)
                → ❌ Retorna "não é Google-signed"
        → laj.g() retorna false
      → MusicBrowserService.f() retorna "__EMPTY_ROOT_ID__"
    → Android Auto recebe root vazio
    → 🔄 Android Auto tenta de novo → LOOP INFINITO
```

**Resumo:** Antes crashava antes de chegar na decisão. Agora chega na decisão "é Google-signed?" e a resposta é NÃO. Sem o bypass, o app nunca autoriza o Android Auto.

---

## 4. Cadeia de Verificação de Certificado (versão 9.28.51)

### 4.1 `laj.g()` — AllowlistManager (ex-`kxo`)

```java
// laj.java, linhas 293-315
public final boolean g(awkp awkpVar, awiz awizVar) {
    boolean contains;
    if (!awkpVar.b()) {
        if (!j(awkpVar)) {
            // Self-check: se é o próprio app OU Android Automotive com mesma assinatura
            if ((Build.VERSION.SDK_INT >= 30 && awkpVar.equals(awjm.a) && n(awkpVar))
                || awkpVar.a(this.q.getPackageName())) {
                return true;  // ✅ self-check passa
            }
        } else {
            return true;  // Android Automotive: self-signed OK
        }
    }
    Set set = this.s;
    synchronized (set) {
        contains = set.contains(awizVar);  // está na lista de permitidos?
    }
    if (contains) {
        // ★ PONTO CRÍTICO — esta condição decide se o caller é aceito ★
        if ((awkpVar.b() || (!cnnf.c(awkpVar, awji.a) && !cnnf.c(awkpVar, awjj.a)))
            && !this.r.c(awkpVar.b)     // ← tny.c(packageName): é Google-signed?
            && !m(awkpVar)) {           // ← fingerprint SHA-256 conhecido?
            return false;  // ❌ NÃO autorizado
        }
        return true;
    }
    return false;
}
```

**O que um patch de bypass faria:** Modificaria esta condição para NUNCA executar `!this.r.c(awkpVar.b)`, seja:
- Retornando `true` antes da condição, ou
- Substituindo `!this.r.c(awkpVar.b)` por `false` (já que a condição usa `&&`)

### 4.2 `tny.c()` — GoogleSignatureVerifier (ex-`tcn`)

```java
// tny.java, linhas 78-80
public final boolean c(String str) {
    return a(str).b;   // delega para a(str) que faz a verificação real
}
```

### 4.3 `tnk.c()` — GoogleCertificates (ex-`tbz`)

```java
// tnk.java, linhas 62-80
static void c() {
    tvc tvcVar;
    if (h != null) return;
    Preconditions.checkNotNull(g);
    synchronized (i) {
        if (h == null) {
            // ★ AINDA TENTANDO INSTANCIAR GoogleCertificatesImpl ★
            IBinder c2 = ubo.d(g, ubo.d, "com.google.android.gms.googlecertificates")
                .c("com.google.android.gms.common.GoogleCertificatesImpl");
            // ... queryLocalInterface para IGoogleCertificatesApi
            h = tvcVar;
        }
    }
}
```

### 4.4 `ubo.c()` — DynamiteModule (ex-`tqd`)

```java
// ubo.java, linhas 541-547
public final IBinder c(String str) {
    try {
        return (IBinder) this.f.getClassLoader().loadClass(str).newInstance();
        // Tenta instanciar: com.google.android.gms.common.GoogleCertificatesImpl
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e2) {
        throw new ubk("Failed to instantiate module class: ".concat(str), e2);
    }
}
```

---

## 5. Patches Morphe Presentes (lista completa)

Mesmos patches da versão anterior. Nenhum patch novo relacionado a Android Auto, MediaBrowser ou certificados:

| Categoria | Patches |
|-----------|---------|
| Music | ChangeHeaderPatch, ChangeMiniplayerColorPatch, ChangeStartPagePatch, CrossfadeManager, DisableDislikeRedirectionPatch, DownloadsPatch, EnableForcedMiniplayerPatch, EnableSwipeToDismissMiniplayerPatch, HideAdsPatch, HideButtonsPatch, HideFlyoutMenuComponentsPatch, MiniplayerPreviousNextButtonsPatch, NavigationBarPatch, RememberRepeatStatePatch, RememberShuffleStatePatch, ReturnYouTubeDislikePatch, VersionCheckPatch, ThemePatch, SpoofVideoStreamsPatch, ScrobblePatch |
| Shared | AppCheckPatch, CheckEnvironmentPatch, CheckWatchHistoryDomainNameResolutionPatch, CustomBrandingPatch, DisableDRCAudioPatch, DisableQUICProtocolPatch, EnableDebuggingPatch, ExperimentalAppNoticePatch, FixRecycledBitmapPatch, ForceOriginalAudioPatch, GmsCoreSupportPatch, HideFullscreenAdsPatch, InitializationPatch, NetworkProxyPatch, SanitizeSharingLinksPatch, SpoofAppVersionPatch, TreeNodeElementPatch |

**Nota:** `GmsCoreSupportPatch` gerencia microG (diálogos de battery optimization, etc.) — **não** tem relação com bypass de certificado. `AppCheckPatch` apenas detecta se é YT ou YT Music.

---

## 6. Recomendação

**Re-patchar com "Bypass certificate checks" EXPLICITAMENTE HABILITADO.** O patch não foi aplicado em nenhuma das duas versões testadas.

### Pontos exatos para injeção do bypass:

| Opção | Arquivo | Linha | O que modificar |
|-------|---------|-------|-----------------|
| **A (recomendada)** | `laj.java` | 309 | Remover `!this.r.c(awkpVar.b)` da condição, ou adicionar `if (true) return true;` antes |
| B | `tny.java` | 78-80 | Fazer `c()` retornar `true` sempre |
| C | `tnk.java` | 62-80 | Fazer `c()` pular a instanciação e retornar stub "válido" |

---

## 7. Artefatos

- APK: `analysis/ytmusic-morphe/exp/base.apk` (74 MB)
- Dex: `analysis/ytmusic-morphe/exp/classes*.dex` (11 arquivos)
- jadx: `analysis/ytmusic-morphe/exp/jadx-output/` (37.170 classes)

---

## 8. Investigação do Patch Bundle (pb-0.jar) — Por que o patch não aplicou?

### 8.1 Fontes

- **pb-0.jar** (8.5 MB, 26/jul) — bundle atual do Morphe Manager, **contém** `BypassCertificateChecksPatchKt.class`
- **pb-897837162.jar** (2.6 MB, 23/jul) — bundle antigo, **NÃO contém** o patch
- Patch source decompilado: `analysis/ytmusic-morphe/patch-src/`

### 8.2 Como o patch funciona

#### Fingerprint (`CheckCertificateFingerprint`)

```java
// CheckCertificateFingerprint.java (descompilado de pb-0.jar)
public final class CheckCertificateFingerprint extends Fingerprint {
    public static final CheckCertificateFingerprint INSTANCE = new CheckCertificateFingerprint();

    private CheckCertificateFingerprint() {
        super(
            null,                                              // customFingerprint
            "Z",                                               // returnType = boolean
            listOf("L"),                                       // parameters = 1 Object param
            null,                                              // opcodes
            listOf(listOf("X509", "isPartnerSHAFingerprint")), // strings a buscar
            null,                                              // customResolver
            41,                                                // mask
            null                                               // marker
        );
    }
}
```

**Mask 41 = 0b101001:** bits 0 (customFingerprint), 3 (opcodes) e 5 (customResolver) são IGNORADOS. Bits 1 (returnType), 2 (parameters) e 4 (strings) são VERIFICADOS.

**O fingerprint procura:** um método que:
- Retorna `boolean` (`"Z"`)
- Recebe 1 parâmetro objeto (`"L"`)
- Contém as strings `"X509"` e `"isPartnerSHAFingerprint"`

#### Ação do patch (`BypassCertificateChecksPatchKt`)

```java
// BypassCertificateChecksPatchKt.java (linha 52)
BytecodeUtilsKt.returnEarly(
    CheckCertificateFingerprint.INSTANCE.getMethod(execute),
    true   // ← injeta "return true" no início do método
);
```

O patch **injeta `return true`** como primeira instrução do método encontrado pelo fingerprint.

#### Configuração no patches-list.json

```json
{
  "name": "Bypass certificate checks",
  "default": true,                    // ← habilitado por padrão
  "compatiblePackages": [{
    "packageName": "com.google.android.apps.youtube.music",
    "targets": [
      { "version": "9.28.51", "isExperimental": true },  // ← compatível com 9.28.51
      { "version": "9.26.51", "isExperimental": true }
    ]
  }]
}
```

### 8.3 Fingerprint × Código Alvo (9.28.51)

O fingerprint casa perfeitamente com `laj.m(awkp)` (AllowlistManager, ex-`kxo`):

| Campo do Fingerprint | Valor esperado | `laj.m(awkp)` na 9.28.51 | Match? |
|---------------------|----------------|--------------------------|--------|
| returnType | `"Z"` (boolean) | `boolean` (linha 61) | ✅ |
| parameters | `["L"]` (1 Object) | `(awkp awkpVar)` — 1 parâmetro | ✅ |
| strings | `"X509"` | Presente (linha 68) | ✅ |
| strings | `"isPartnerSHAFingerprint"` | Presente (linhas 120,134,145,161) | ✅ |

**Conclusão: O fingerprint CASA PERFEITAMENTE com o código da 9.28.51.** O método alvo existe, tem a assinatura correta e contém as strings esperadas.

### 8.4 O que o patch modificaria

Se aplicado, `laj.m()` se tornaria:

```java
private final boolean m(awkp r11) {
    return true;  // ← INJETADO pelo patch (BytecodeUtilsKt.returnEarly)
    // ... todo o código original de verificação SHA-256 fica inacessível
}
```

Isso faria `laj.g()` (linha 309) sempre cair no `return true`:

```java
// laj.g() — condição original:
if (... && !this.r.c(awkpVar.b) && !m(awkpVar)) {
//                                   ↑ m() sempre true → !true = false
//                                   false quebra o && → condição inteira = false
    return false;  // ← NUNCA executa
}
return true;  // ← SEMPRE executa → Android Auto autorizado
```

### 8.5 Por que o patch NÃO está no APK?

**Hipótese confirmada: (b) O patch NÃO FOI SELECIONADO na UI do Morphe Manager.**

Evidência que descarta a hipótese (a) "fingerprint não casa":
- O fingerprint casa **perfeitamente** com `laj.m()` na 9.28.51 (ver tabela acima)
- O método alvo está presente e tem exatamente a mesma estrutura da 9.15.51
- As strings "X509" e "isPartnerSHAFingerprint" estão presentes nos bytes do dex

**Causa provável:** Quando o usuário abriu o Morphe Manager pela primeira vez, usava o bundle `pb-897837162.jar` (que NÃO contém o patch). A seleção de patches foi salva. Ao trocar para `pb-0.jar` (que CONTÉM o patch), o patch "Bypass certificate checks" apareceu como novo item **não selecionado** (porque a config salva não o incluía). O usuário patchou sem notar que o novo patch estava desselecionado.

Fatores que contribuíram:
1. O patch é `isExperimental: true` para 9.28.51 — alguns managers escondem patches experimentais por padrão
2. O patch é novo no bundle — não existia na seleção anterior
3. O Morphe Manager persiste a seleção entre sessões — patches novos não são automaticamente selecionados

### 8.6 Solução

**Re-patchar com o Morphe Manager, garantindo que "Bypass certificate checks" está VISIVELMENTE SELECIONADO (checkbox marcado) antes de aplicar.**

Se o manager estiver escondendo patches experimentais, habilitar a opção "Show experimental patches" primeiro.
