# Relatório da Sessão — 2026-07-27

**Objetivo:** Fazer o YT Music Morphe funcionar no Android Auto (MediaBrowserService) no Xiaomi 17 Pro Max com microG + Google Play Services reais.

**Aparelho:** Xiaomi 17 Pro Max (popsicle) | xiaomi.eu OS3.0.317.0.WPBCNXM | Android 16 | KSU LKM 32558

---

## ✅ STATUS ATUAL

**YT Music Morphe 9.15.51 funcionando no Android Auto ✅** — podcasts carregam, música exige YouTube Premium (server-side, ver §Veredito premium/podcasts). Sem crash, sem spinner infinito. Stack:

| Componente | Detalhe |
|---|---|
| APK | YT Music 9.15.51 stock (APKMirror) |
| Patches (37) | seleção do usuário via CLI Morphe (bundle v1.37.0) |
| Fix adicional | `tcn.c(String) → return false` (cirurgia dex-only via baksmali/smali) |
| Assinatura | manager.keystore (BKS v2, storepass "", alias "Morphe", keypass "Morphe") |
| PR upstream | https://github.com/MorpheApp/morphe-patches/pull/2239 |

---

## 1. Diagnóstico — cadeia do crash

### 1.1 Sintoma inicial

YT Music Morphe **crashava** quando o Android Auto conectava ao `MediaBrowserService`:

```
java.lang.IllegalStateException: Missing DynamiteApplicationContext.
    at com.google.android.gms.common.GoogleCertificatesImpl.<init>(...)
    at java.lang.Class.newInstance(Native Method)
    at tqd.c(PG:11)          ← DynamiteModule: Class.forName().newInstance()
    at tbz.c(PG:30)          ← GoogleCertificates: carrega GoogleCertificatesImpl
    at tcn.a(PG:32)          ← GoogleSignatureVerifier: verificação de assinatura
    at tcn.c(PG:1)
    at kxo.g(PG:85)          ← AllowlistManager: "esse caller é Google-signed?"
    at MusicBrowserService.f(PG:242)  ← onGetRoot
    at bzl.onGetRoot(PG:116)
```

### 1.2 Classes envolvidas (9.15.51, ofuscação ProGuard)

| Sigla | Classe | Função |
|-------|--------|--------|
| `kxo` | AllowlistManager | Decide se o caller (ex: gearhead) pode browsear. Método `g(avgi, aves)` = gate principal, `h(aves)` = gate de browsability, `m(avgi)` = fingerprint SHA-256 |
| `tcn` | GoogleSignatureVerifier | Verifica se um pacote é assinado pelo Google. `c(String)` = entry point, chama `a(String)` → `tbz` |
| `tbz` | GoogleCertificates | Carrega `GoogleCertificatesImpl` via Dynamite (GMS). Método `c()` faz `tqd.d("googlecertificates").c("GoogleCertificatesImpl")` |
| `tqd` | DynamiteModule | Carrega classes do GMS via reflexão. `c(String)` = `classLoader.loadClass(str).newInstance()` |

### 1.3 Por que crasha

O `GoogleCertificatesImpl` do GMS **real** (instalado no device, não o microG) exige `DynamiteApplicationContext` no construtor. Esse contexto nunca é inicializado porque o app usa **microG** (redirecionamento do GmsCore support patch). A exceção `IllegalStateException` não é capturada (o código só captura `tpz`/`RemoteException`), então o processo morre.

**Em devices sem GMS real** (só microG), o `tbz.c()` falha gracefulmente com `tpz` → retorna "não-assinado" → `kxo.g()` avalia o resto da condição (incluindo `m()`) → funciona. Por isso o patch oficial do Morphe (`return true` em `kxo.m`) basta para a maioria.

### 1.4 Três builds sem o patch — por quê

| Build | Data | Bundle | Motivo |
|-------|------|--------|--------|
| 9.15.51 (23/jul) | 23/jul | pb-897837162 ("Rushi's Patches") | Bundle antigo **não continha** o patch BypassCertificateChecks |
| 9.28.51 experimental | 26/jul | pb-0.jar (v1.37.0) | Bundle novo **tinha** o patch, mas a seleção salva do manager (da sessão com bundle antigo) **não o incluía** — ficou desmarcado na UI |
| 9.15.51 re-patch | 27/jul 09:43 | pb-0.jar | Processo `app.morphe.manager:Patcher` sofreu **SIGABRT por OOM** (mmap de 1,28 GB falhou no start da VM) ~1 min antes da instalação → APK saiu sem o patch |

---

## 2. Fix validado

### 2.1 Patch via Morphe CLI

O Morphe Manager não conseguiu aplicar o patch (OOM). Usamos o **Morphe CLI** (morphe-desktop-1.12.0-all.jar) no PC com o bundle `pb-0.jar` extraído do próprio manager:

```
java -jar morphe-cli.jar patch --patches pb-0.jar -e "Bypass certificate checks" -o output.apk stock.apk
```

**Problema:** A CLI assina com keystore pública "Morphe" → `INSTALL_FAILED_UPDATE_INCOMPATIBLE` (assinatura diferente do APK instalado).

**Solução:** Extrair o keystore do manager do device:
- `/data/data/app.morphe.manager/app_signing/morphe.keystore` (BKS v2, alias "Morphe", store password **vazia**, key password "Morphe")
- Assinar com `SignApk.java` (apksig + BouncyCastle do próprio jar do CLI) → `install -r` preserva dados

### 2.2 O patch oficial não basta com GMS real

Após aplicar o patch Morphe (`return true` em `kxo.m`), o crash **continuou**. Frida provou em runtime:

- `kxo.h()` retornava `false` para gearhead (allowlist `t`)
- O crash Dynamite acontecia **dentro de `tcn.c()`** (= `this.r.c()` em `kxo.g`), **ANTES** de `m()` ser avaliado

Ou seja: o patch neutraliza o terceiro termo da condição (`!m()`), mas `!this.r.c()` é avaliado **antes** e **crasha** antes de chegar em `m()`.

### 2.3 Fix #1 (amplo, validado): `kxo.g` e `kxo.h` → `return true`

Edição via apktool: `g()` e `h()` retornam `true` imediatamente. AA funcionou.

**Problema:** rebuilds via apktool **corrompem resources** — o app crashava na UI do celular com `Resources$NotFoundException res/d9P.xml`. O apktool reempacota resources e quebra referências.

### 2.4 Fix #2 (mínimo, escolhido pro upstream): `tcn.c(String) → return false`

**Método:** cirurgia **dex-only** (baksmali/smali 3.0.9 fat jars):

```smali
# tcn.smali — antes:
.method public final c(Ljava/lang/String;)Z
    .registers 2
    invoke-virtual {p0, p1}, Ltcn;->a(Ljava/lang/String;)Ltch;
    move-result-object p0
    iget-boolean p0, p0, Ltch;->b:Z
    return p0
.end method

# depois:
.method public final c(Ljava/lang/String;)Z
    .registers 2
    const/4 v0, 0x0
    return v0
.end method
```

**Efeito:** `tcn.c()` reporta "não-assinado pelo Google" **sem tocar no Dynamite**. Em `kxo.g()`:

```java
!this.r.c(pkg) && !m(caller)
→ !false && !true        // c() = false, m() = true (patch Morphe)
→ true && false
→ false                   // condição falha → return true (autorizado)
```

**Vantagens sobre o Fix #1:**
- Mantém `kxo.g/h` originais (allowlists intactas — só o Dynamite é bypassado)
- Não corrompe resources (dex-only, sem tocar em resources.arsc/AndroidManifest)
- Comportamento idêntico em devices com/sem GMS real

### 2.5 Build final

```
CLI Morphe (--exclusive, 37 patches) → APK base
  → baksmali classes.dex → editar tcn.smali → smali → classes-patched.dex
  → ReplaceDex.java (preserva compressão DEFLATED, STORED p/ .so)
  → SignApk.java (manager.keystore)
  → adb install -r
```

**APK final:** `analysis/ytmusic-morphe/cli/ytmusic-9.15.51-final-signed.apk`

---

## 3. Veredito "só podcasts no AA"

Com todos os gates client-side abertos (tcn.c, kxo.m, kxo.g, kxo.h), o Android Auto mostra **só podcasts** + upsell "faça upgrade para o YouTube Premium".

**Análise do código:**

- **`khr.e()`** (entitlement/unlimited check) **já é patcheado pra `true`** pelo Morphe — usado em `lbg.java:439` como gate de playback
- **`skip_entitlement_check`** é setado pra `true` para gearhead em `lai.b()`
- A árvore de browse (`lgp.v()`) é construída a partir de **protobufs `buol` retornados pelo servidor** (Innertube API)
- **Não existe filtro client-side** separando música de podcast — o servidor simplesmente retorna conteúdo diferente para Android Auto (free-tier = só podcasts)

**Conclusão: SERVER-SIDE.** Documentado em ReVanced#6185. Nenhum patch local pode adicionar conteúdo que o servidor não envia. Contas YouTube Premium recebem o catálogo completo também no AA.

---

## 4. PR upstream

**https://github.com/MorpheApp/morphe-patches/pull/2239**

Estende o patch "Bypass certificate checks" com:

- **`GoogleCertificatesRemoteFingerprint`**: ancora na string `"Failed to get Google certificates from remote"` (método `tcn.a`)
- **`IsGoogleSignedFingerprint`**: `(String)→boolean` com `classFingerprint = GoogleCertificatesRemoteFingerprint` — isola `tcn.c`
- **`returnEarly(false)`** em `IsGoogleSignedFingerprint.method` no `execute {}` do patch

Fork: `andersonlucasg3/morphe-patches`, branch `fix/ytmusic-aa-dynamite-crash`.

Build local não compilou (plugin `app.morphe.patches:1.3.3` requer GitHub Packages auth — CI do Morphe tem as credenciais).

---

## 5. Lições operacionais

### Patcher OOM → reiniciar antes de patchear
O processo `app.morphe.manager:Patcher` alocou 1,28 GB de mmap e morreu por OOM no start da VM. O YT Music tem ~80 MB; o patcher precisa de RAM proporcional. **Sempre reiniciar o device (limpa RAM) antes de patchear APKs grandes no manager.**

### Keystore do manager reutilizável
Credenciais: `/data/data/app.morphe.manager/app_signing/morphe.keystore` (BKS v2), storepass **vazia**, alias `Morphe`, keypass `Morphe`. Permite assinar builds do CLI com a mesma identidade do manager → `install -r` sem desinstalar.

### Dex-only > apktool
apktool corrompeu resources em 100% dos rebuilds testados (3 tentativas). A cirurgia dex-only (baksmali/smali + ReplaceDex.java) é **confiável** e não toca em resources.arsc, AndroidManifest.xml, ou libs nativas. **Sempre preferir dex-only para patches pontuais.**

### Sempre conferir serial do device
O device `f10c4f767d7b` (Redmi) apareceu no adb durante a sessão. Um `install` foi disparado nele por engano → `INSTALL_FAILED_USER_RESTRICTED`. **Sempre verificar `adb devices` e usar `-s <serial>` explícito.** O serial do popsicle pode mudar entre conexões (era `4d7fc9af`, virou `f10c4f767d7b` em uma das reconexões).

### Frida 17 sem java-bridge + quirks do device
- Frida 17 removeu a java-bridge do core → scripts precisam ser bundlados (esbuild com `frida-java-bridge` do `tools/frida-agent`)
- `enumerate_processes()` **não lista** o YT Music neste device (razão desconhecida; `ps -A` lista) → usar `pidof` via adb + attach por PID
- Processo do YT Music morre quando o gearhead é force-stopped → `Java.perform` nunca dispara em spawn-gating
- **Solução:** runner Python com **re-attach loop** (`analysis/ytmusic-morphe/frida/run.py` + `tools/frida-agent/mbs-hook.ts`)

### Frida-server no device é vetor de detecção
O frida-server 17.9.3 foi deixado rodando no device após a investigação. **Sempre parar após usar** (apps bancários detectam o processo `frida-server`). O binário permanece em `analysis/ytmusic-morphe/frida-server` (fora do git).

---

## 6. Linha do tempo (síntese do dia)

1. **Manhã:** Diagnóstico do crash original (9.15.51 de 23/jul). Descoberta: patch BypassCertificateChecks ausente no bundle antigo. Análise de 3 builds sem patch.
2. **Tarde:** Patch via CLI → problema de assinatura → extração do keystore do manager → `install -r` OK. Crash persistente (Dynamite).
3. **Frida:** prova em runtime que `tcn.c` crasha antes de `m()`. Fix #1 (g/h → true) via apktool → AA funciona, mas apktool corrompe resources.
4. **Fix #2:** tcn.c → false via dex-only → AA funciona, celular OK, resources íntegros.
5. **Veredito podcasts:** análise do código confirma que o corte é server-side (Innertube API).
6. **PR upstream:** #2239 no MorpheApp/morphe-patches com o fix mínimo.
7. **Build final:** CLI --exclusive 37 patches + fix dex-only → APK assinado e validado.
8. **Limpeza:** frida-server parado no device.

---

## 7. Artefatos da sessão

```
analysis/ytmusic-morphe/
├── REPORT.md                          # Análise inicial (9.15.51 sem patch)
├── exp/REPORT.md                      # Análise 9.28.51 + investigação do bundle
├── morphe-patches.json                # Config dos patches (extraído do manager)
├── pb-0.jar                           # Bundle v1.37.0 (contém BypassCertificateChecks)
├── pb-897837162.jar                   # Bundle antigo (sem o patch)
├── patch-src/                         # Decompilação do pb-0.jar
├── cli/
│   ├── morphe-cli.jar                 # morphe-desktop-1.12.0-all.jar
│   ├── apktool.jar                    # apktool 3.0.3
│   ├── baksmali.jar / smali.jar       # v3.0.9 fat jars
│   ├── SignApk.java                   # Assinador com manager.keystore
│   ├── manager.keystore               # ⚠️ SENSÍVEL — chave de assinatura
│   ├── stock.apk                      # YT Music 9.15.51 original
│   ├── ytmusic-9.15.51-morphe-signed.apk   # Build CLI inicial
│   ├── ytmusic-9.15.51-final-signed.apk    # Build final (37 patches + tcn fix)
│   └── final-build.log                # Log completo do CLI
├── frida-server                       # v17.9.3 (fora do git)
└── frida/
    ├── run.py                         # Runner com re-attach loop
    └── hook.js                        # Script Frida (mbs-hook.ts bundlado)
```
