# SESSION HANDOVER — UnlockXiaomi (popsicle)
**Documento vivo de continuidade entre sessões. Última atualização: 25/jul/2026 (pós-Sessão 10 — Revolut: forense completa, `.ko` com hide; suspeito nº1 = keybox). Unifica os antigos `SESSION-HANDOVER-2026-07-23.md` e `ESTADO-ATUAL-2026-07-23.md`. Histórico cronológico detalhado das sessões de 21–22/jul: `docs/relatorio-sessao-2026-07-22.md`.**

---

## PARTE 1 — ESTADO ATUAL (snapshot 24/jul/2026 ~23:45)

### 1.1 Aparelho e ROM
- **Device:** Xiaomi 17 Pro Max (`popsicle`, 2509FPN0BC), SD 8 Elite Gen 5, Android 16 (SDK 36)
- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM**
- **Kernel:** stock `6.12.23-android16-5-...-abogki463945075-4k` (intocado)
- **Bootloader:** desbloqueado (NUNCA re-travar)
- **Recovery:** TWRP 3.7.1 unofficial (variante `fix22ZX_pinwork_partialdecryption`) — mantido como rede de segurança, decripta /data com PIN (foi o que salvou os 2 bootloops da frente Caixa)

### 1.2 Root e stack
- **KernelSU LKM** driver **32558** (fork `andersonlucasg3/KernelSU` branch `hide-lkm`, patch `9ea26f3`: `kobject_del`+`list_del` — módulo **invisível** em `/proc/modules` e `/sys/module`, build via Actions run 30156878115; `.ko` em `tools/ksu_apk/`, imagem em `backup\ksu-migration\init_boot-317-ksu-hide.img` sha `2079b885…`) no `init_boot`; manager `me.weishu.kernelsu` **32562 (v3.2.4-36)** — atualizado em 24/jul via `pm install` do APK do release backslashxx (em `tools/ksu_apk/`), **adb root ON** (funciona também via Wi-Fi: `adb tcpip 5555` → `adb connect <ip>:5555`; não persiste a reboot)
- **ZygiskNext 1.4.3-817** (enforce-denylist; `modules64: deviceidchanger, playintegrityfix`)
- **USAP pool DESATIVADO** (fatos 19/20): `device_config put runtime_native usap_pool_enabled false` (persiste) + `persist.sys.usap_pool_enabled=false` + `setprop dalvik.vm.usap_pool_enabled false` (volátil). **Frente Petal fechada 24/jul — reativação é decisão em aberto** (risco: injeção ZN flaky volta; requer reboot)
- **PlayIntegrityFork v17** com `custom.pif.prop` = **Pixel 10 (frankel, Canary ZP11.260618.005 — expira 2026-08-19, rodar Action do PIF p/ renovar)**
- **TrickyStore v1.4.1** (keybox DroidWin v3.6 + security_patch.txt=2026-07-05; target.txt inclui os 3 pacotes Caixa + `br.com.gabba.Caixa`)
- **Umount global** (exceto gms/vending/termux **e `com.huawei.maps.app`** — ver Parte 5, fato 16). **App BYD:** umount foi desligado na Sessão 9 p/ injeção (frente DCK) — re-ligar no manager se ainda não foi
- **HMA-OSS oss-164** (instalado 27/jul — Parte 2.5): filtra applist no system_server; template `bancos` (whitelist vazio) aplicado a `br.com.bradseg.bscelular` e `com.revolut.revolut`; config em `/data/misc/hide_my_applist_hmaosspreseedab/config.json`
- **COPG 5.9.0** — **REMOVIDO pelo usuário em 24/jul** (DeviceID+ COW cobre o caso Petal; era cinto-e-suspensório)
- **Play Integrity: 3/3 ✅**

### 1.3 Apps
| App | Estado |
|---|---|
| Google Wallet | ✅ |
| BYD | ✅ |
| Caixa / bancos BR | ✅ **RESOLVIDA 23/jul ~21h** (Parte 3) |
| Revolut | ✅ **RESOLVIDO 27/jul ~11:35** — mesmo vetor/fix do bradseguros: HMA-OSS (Parte 2.5). Hipótese keybox vazada DESCARTADA |
| Bradesco Seguros | ✅ **RESOLVIDO 27/jul ~11:20** — vetor: enumeração de pacotes; fix: HMA-OSS (Parte 2.5) |
| **Petal Maps 4.7.0.319** | ✅ **RESOLVIDA 24/jul ~18:20** — COW prop_area validado: `Get Manufacturer: HUAWEI`, app passa do gate (Parte 2.2) |

### 1.4 Módulo DeviceID+ (fork próprio)
- **Local:** `modules/deviceidchanger/` (fork AGPL de sidex15/deviceidchanger, créditos no README/LICENSE). Rebuild do zip: `native/build.sh` (o `.so`) + zip da pasta `module/` (zip gitignored; `build_zip.ps1` no Windows). Targets do build.sh: sem arg = módulo; `test` = smoke test `test_hook`; `inspect` = inspetor standalone em /data/local/tmp
- **Versões:** v2.1.1 **instalada no aparelho 24/jul ~18:17** (zip rebuildado **com o `.so` COW incluído** — o zip antigo não tinha o `.so` e teria removido o spoof no update; o `.so` deployado foi puxado do device p/ `module/zygisk/arm64-v8a.so` antes do rebuild) + config viva re-mesclada no staged. **Native v2.2.0-dev: COW do prop_area VALIDADO 24/jul ~18:20 (Petal) + spoof `Build.*` via JNI adicionado e deployado às ~19:50 (hash `06dcaf4c…`)** — validado em campo no gms/app BYD (frente DCK) e no Petal (23:21). Pendente: bump v2.2.0 + commit do native (prop_cow.cpp, Build.* JNI etc., ainda uncommitted)
- **Features:** SSAID por app (lista todos os pacotes, checkbox enroll, ID global compartilhado OU custom por app, regen de ambos, backup/restore + validação anti-bootloop) · spoof de props persistente global (service.sh pós-boot, `ro.build.host=c3-miui-ota-bd110`) · **spoof de props por app** — três mecanismos complementares: (1) **COW do prop_area** (`prop_cow.cpp`: copia as páginas da prop p/ mapping privado e reescreve o valor in-place com o protocolo de serial da bionic — cobre TODOS os caminhos de leitura: JNI, nativo, parse direto, static-linked); (2) GOT/PLT hook das 3 funções bionic (`perapp_hooks.cpp`, complemento p/ leitores dinâmicos); (3) **spoof de `android.os.Build.*` via JNI** (`deviceid_zygisk.cpp`: reescreve os campos estáticos MODEL/DEVICE/PRODUCT/BRAND/MANUFACTURER/etc. no processo do app em postSpecialize — necessário porque a classe Build é inicializada no zygote com os valores reais e nem COW nem hooks a alcançam; mesma técnica do PIF). Config flat `.perapp_props` linhas `pkg|chave=valor` — **match por NOME DE PROCESSO (nice_name), não pacote** (fato 25); aplica com force-stop do app, sem reboot; stealth unload nos apps não-alvo · editor do target.txt do TrickyStore
- **⚠️ Update de módulo KSU sobrescreve o dir inteiro** (`/data/adb/modules/deviceidchanger/`) → sempre re-mesclar `config.json` + `.props_*` + `.perapp_props` no staged (`/data/adb/modules_update/...`) antes do reboot
- **⚠️ NUNCA trocar o `.so` zygisk a quente** — o ZN cacheia o entry offset no zygote; trocar o arquivo sem reboot faz todo app injetado crashar (fato 20)

### 1.5 Rollback e recuperação
| Item | Caminho | sha256 |
|---|---|---|
| init_boot Magisk 30.7 (rollback completo) | `backup\ksu-migration\init_boot_a_backup.img` | `c951cdf4…` |
| boot stock 315 | `backup\ksu-migration\boot_a_backup.img` | `6c48dd3f…` |
| init_boot KSU 317 (re-flash rápido) | `backup\ksu-migration\init_boot-317-ksu.img` | `68f996d9…` |
| init_boot KSU 317 **+hide** (atual) | `backup\ksu-migration\init_boot-317-ksu-hide.img` | `2079b885…` |
| vbmeta original (flags=0) | `rom\popsicle_eu_3.0.315\images\vbmeta.img` | — |
| SSAID pré-randomize | `backup\settings_ssaid-pre-randomize.xml` | — |

### 1.6 Git/GitHub
- Repo: `github.com/andersonlucasg3/UnlockXiaomi` (branch `main`, push via `gh` autenticado)
- Identidade git local configurada no repo (`user.name`/`user.email` — antes falhava com "Author identity unknown"). Push direto exigia `gh auth setup-git` (feito em 24/jul)
- `tools/` segue untracked (binários grandes). `.gitattributes`: `*.sh` sempre LF. `.gitignore`: `*.so`, `*.zip`, `native/test_hook` (binários regeneráveis ficam fora do git)

---

## PARTE 2 — PENDÊNCIAS

### 2.1 BYD digital key (⏸️ ENCERRADA 24/jul ~23:30 — esgotado client-side; veredito: gate do Google sem workaround conhecido)
**Objetivo:** provisionar a chave digital do BYD (Destroyer 05/King BR) no celular. **Estado final: bloqueio no `downloadAllowed` do DCK do GMS — GMS de produção não aplica overrides de phenotype por nenhum canal (todos testados); servidor não serve config DCK p/ o modelo. Reversão completa feita** (ver fim da Sessão 9). Legado que fica: DeviceID+ com spoof de `Build.*` (JNI), prop `ro.gms.dck.eligible_wcc` documentada, e todo o mapa do DCK abaixo. Se um dia o Google whitelistar o modelo, retomar daqui.

**Fatos (device):**
- Hardware OK (verificado no device): `nfc.ese` + `nfc.uicc` + OMAPI (`android.hardware.se.omapi.ese.xml`), `android.hardware.uwb`, HAL `secure_element-service.qti` e `com.android.se` rodando
- **Experimento aplicado:** `action.sh` do PIF rodado → gms/vending agora = **Pixel 10 (frankel)** via `/data/adb/modules/playintegrityfix/custom.pif.prop`
- **Rollback do experimento:** apagar `custom.pif.prop` + force-stop gms/vending (PIF volta aos defaults internos que davam PI 3/3)
- ⚠️ Verificar Play Integrity após o reteste (print novo pode alterar o veredito)

**Pesquisa de 24/jul (conclusões — fontes: support.google.com/wallet/answer/12060041, /11358016, /13037118, byd.com/br/chave-digital, byd.com/eu, fórum dolphinbyd.com.br threads 4976/2935, Reddit r/BYD 1on9yem/1uhy20t/1usp25c):**
- **O bloqueio é do Google Wallet/Google, server-side, por modelo+ROM** — NÃO é região (BR é suportada pelo app BYD) nem integridade. A lista pública é curta ("Pixel 6+, S21+, e alguns Android 12+") mas a real é server-side e não publicada; liga/desliga por servidor sem update de app (provas: Xiaomi 15T Pro e 15 Ultra passaram a funcionar de um dia pro outro).
- **Depende de "digital key API" do Google embutida na ROM pelo OEM** — Xiaomi só incluiu em builds HyperOS recentes, modelo a modelo ("The HyperOS update to 3.0.3 enables Google's digital key API"). **Risco real: a xiaomi.eu pode não carregar essa API** (e Lineage/Graphene/Huawei não têm).
- Hardware: NFC+eSE basta; **UWB é opcional** — BYD é NFC-only (todos os BYD na tabela da Wikipedia são "NFC"; Xiaomis homologados 12→14 não têm UWB).
- Lista real da BYD (fórum BR, abr/2025): Samsung S20→S25/Note20/Z/A56/A36, **Xiaomi 12/12 Pro/13/13 Pro/13 Ultra/13T/13T Pro/14/14 Ultra**, OPPO Find X8/Pro. **Nenhum Xiaomi 17** — donos de 17/17 Ultra no Reddit dizem que ainda não funciona (mas há relato solto de 17 funcionando).
- Restrição é **dupla**: aparelho precisa estar na lista do Google E na da BYD. O erro do nosso caso vem da etapa do Wallet = bloqueio do Google.
- **Spoof PIF p/ Pixel 10 não resolve**: injeta Build.* só no DroidGuard/attestation; a elegibilidade de car key é decidida server-side contra o modelo real que o GMS reporta.
- **Nenhum workaround documentado** (LSPosed/spoof de props) com sucesso comprovado para car key — território novo.
- Risco mesmo se provisionar: revogação silenciosa da chave em re-checks de integridade (caso BMW + Xiaomi 15).
- **Via Samsung Wallet DESCARTADA (pesquisa 24/jul, 3 agentes):** Samsung Wallet exige hardware Galaxy + framework One UI + conta Samsung validada server-side (rejeita não-Samsung desde 2022, "ID not valid"); chave digital Samsung vive no eSE Galaxy com attestation Knox (SAK único por aparelho no TrustZone, vinculado a IMEI+serial — NÃO existe keybox de lote estilo TrickyStore p/ Samsung); KnoxPatch (estado da arte, só hardware Samsung) marca Wallet/Pay como ❌ ("checks rodam no TEE, trustlets assinados — só exploit de TrustZone"); compartilhamento de chave de um Galaxy p/ o Xiaomi falha (destinatário também é checado server-side). Fontes: github.com/salvogiangri/KnoxPatch (+issue #43), docs.samsungknox.com/dev/knox-attestation, xda-developers.com/samsung-pay-not-working-non-samsung-phones, news.samsung.com (Digital Key = eSE), dolphinbyd t/4976.
- **🆕 Sinal "Xiaomi 17 funciona" DESMONTADO (pesquisa profunda 24/jul):** o post do r/BYD era **hearsay** (thread do Vivo X300, 28/jun: "I saw a recent post...", sem link/modelo/ROM). Não existe NENHUM relato first-hand de BYD funcionando em qualquer Xiaomi 17 até 24/jul — só fracassos (17 e 17 Ultra com BYD M6, mar/abr/2026). O caso real de sucesso é **BMW i4 + Xiaomi 17 base via Google Wallet** (mai/2026, r/BMWI4 1t5p111): prova que a digital key API do Google EXISTE na família 17 e que o Google já whitelisteou o 17 base. **Lista oficial do Google (android.com/digital-car-key, verificada 24/jul): "Xiaomi 12&12 Pro, 13…, 15&15 Ultra, 15T&15T Pro, 17 & 17 Ultra, MIX Flip, Poco F7/F8 Ultra" — NÃO inclui 17 Pro nem 17 Pro Max** (popsicle é modelo China-only, 2509FPN0BC, sem variante Global). Whitelist é por modelo+montadora; BYD e BMW são listas separadas. Não há feature AOSP (`android.hardware.digital_key` não existe) — o DCK vive 100% no Play services/Wallet e a elegibilidade é server-side por identidade do modelo. Flips server-side acontecem (15T Pro Uruguai virou ~08/jul sem update). **Aberto: testar se PIF spoofando gms p/ modelo whitelistingado (17 base ou 17 Ultra, não Pixel) move o gate do Wallet** — a elegibilidade lê a identidade que o GMS reporta; PIF já injeta gms/vending.
**Progresso 24/jul noite (Sessão 9 — RE do app BYD + gating DCK descoberto):**
- **RE do app BYD** (fontes decompiladas em `analysis/byd/out2/sources/`): o check de compatibilidade chama `DigitalKeyFramework.getClient(ctx).isCreateDigitalKeyPossible()` — **o veredito é do GMS (módulo DCK do Google)**, não de lista local. O app só envia `deviceManufacturer` (código: xiaomi=0002, via Build.MANUFACTURER — já correto) p/ BYD. A tela "modelos suportados" é a página de ajuda `catalogPage` aberta quando o check do GMS falha. Lógica principal ofuscada via JNI (`com.fort.andjni`).
- **Gating do DCK no GMS (logcat, tag `Dck`, service_id=289):** `[WirelessCapabilitiesFeatures] wccSysProp: 0` (prop desconhecida, int 0-3, default 0) + `wccOverride: not set` → `hasWccSupport: false` → `downloadAllowed: false` → módulo DCK completo nem baixa ("Initializing as WCC1"). **WCC = classe de capacidade do CCC: 1=NFC, 2=NFC+BLE, 3=NFC+BLE+UWB.**
- **Fato novo importante (ZN):** o match do `.perapp_props` é por **nome de processo** (nice_name), não de pacote — `com.google.android.gms` só cobre o processo principal. O Chimera DCK roda em **`com.google.android.gms.persistent`** (precisa de linhas próprias na config). `.unstable` fica DE FORA de propósito (DroidGuard/PIF Pixel 10; PI 3/3 mantido).
- **Spoof aurora (14 Ultra: model=24030PN60G/device/name=aurora/marketname)** aplicado via COW + Build.* (JNI novo no módulo) em: gms, gms.persistent, walletnfcrel (sem efeito — wallet sob umount não é injetado), bydautolink (precisou **desligar umount no manager UI**). Mesmo com tudo spoofado: incompatível → veredito depende do wcc, não do modelo.
- **Override phenotype aplicado:** `DckFeatureMain__wcc_override=3` inserido em `/data/data/com.google.android.gms/databases/phenotype.db` (`flag_overrides` id 18 + `flag_overrides_to_commit`, config_package_id **231** = `com.google.android.gms.dck`; backup do db em `/data/local/tmp/phenotype.db*`). Formato decodificado dos overrides existentes (os do Android Auto/DiLink, ids 1-17): type 1=int, 4=string, account_id=0. **Commit não dispara com force-stop — gatilho provável é boot.** Existe broadcast `com.google.android.gms.phenotype.FLAG_OVERRIDE` (--es package/flag/type/value) como alternativa. ⚠️ GMS novo **reverte overrides em ≤24h** (configs assinadas) — pode precisar re-aplicação periódica.
- **App BYD zerado + spoof ativo: mesmo veredito** (veredito não é cache local nem modelo reportado).
- Pesquisa: nenhuma menção pública a `wccSysProp`/`wccOverride` — nome da prop só via decompilação do GMS (classe `WirelessCapabilitiesFeatures`).
- ⏳ **PRÓXIMO:** decidir rota pro `downloadAllowed` (ver abaixo — Sessão 9 parte 2).

**Sessão 9 parte 2 (24/jul noite — fundo do poço do DCK):**
- **`wccSysProp` = `ro.gms.dck.eligible_wcc`** (int 0-3, default 0) — fonte: `defpackage/bsst.java` (classes6.dex do gms base, jadx on-device). **SETADA ao vivo via `setprop` (funciona pq a prop não existia) e persistida em `.props_spoof` do DeviceID+ (service.sh boot_completed+5s)** → `wccSysProp: 3` no log, `hasWccSupport` passou. **Mesmo assim app BYD segue bloqueado.**
- **Gate restante = `downloadAllowed` = flag `DckStub__full_module_download_allowed`** (bool, default false) — fonte: `defpackage/jycg.java` (classes15.dex). Eligibility do módulo: `bsog.b()` = `wcc>0 && downloadAllowed`. Outras flags do stub: `DckStub__are_flags_synced` (default false!), `DckStub__disable_dck_support`. Override wcc = `DckFeatureMain__wcc_override` (long, default -1; jybv.java). gtwx registra pacote `com.google.android.gms.dck` (config_package_id **231**, params VAZIOS — servidor não serve DCK p/ este modelo).
- **Phenotype schema novo (db v1033+) decodificado** (classes8: fkch/fkee/fjzr): merge de overrides exige link em `experiment_states_to_overrides` com o `committed_experiment_state_id` do pacote (dck = 4372). Aplicamos: overrides id 18 (`DckFeatureMain__wcc_override=3` type 1), 19 (`DckStub__full_module_download_allowed=1` type 0), 20 (`DckStub__are_flags_synced=1`) + links p/ 4372 + `flag_overrides_to_commit`. **NADA aplica** — a op de leitura chama-se `getCommittedOverridesPhixit` ("Phixit" = ferramenta de debug interna; provável que overrides só funcionem em fluxo dogfood/debug, e o gtwx em produção leia só config servida). Broadcast `com.google.android.gms.phenotype.FLAG_OVERRIDE` retorna 0 sem efeito. XML `gms_chimera_phenotype_flags.xml` é cache write-only (edição ignorada). **Os overrides antigos do Android Auto (ids 1-17) NÃO têm link em experiment_states_to_overrides — possivelmente nunca aplicaram via phenotype.**
- Backup phenotype.db em `/data/local/tmp/phenotype.db*`. Fontes gms decompiladas on-device: `/data/local/tmp/gmsout{,8,15}/`; classes-chave copiadas p/ `analysis/byd/*.java` no repo.
- **Rotas possíveis pro downloadAllowed:** (a) **GMS Phixit** (polodarb, app root feito p/ o schema novo — chama a op oficial; revert ≤24h por configs assinadas, precisa re-aplicar); (b) chamar a binder op `SetFlagOverridesOperation` direto; (c) aceitar que pode haver gate server-side adicional (allowlist de modelo no download do módulo — `downloadAllowed` pode ser decidido no servidor, não só local).

**Sessão 9 parte 3 (24/jul ~23h — overrides esgotados, veredito empírico):**
- **GMS Phixit testado** (fork jcrutch-design/GMS-Phixit-Android17 v1.5, sha256 `8b0fc972…`, instalado como `ua.polodarb.gmsphixit`): escreveu o registry completo de flags DCK (~90 overrides, ids 21-109, incl. `DckStub__full_module_download_allowed=1` e `DckStub__are_flags_synced=1`) em `flag_overrides` + `flag_overrides_to_commit`. **Sem efeito no read path de produção.**
- Complementos manuais testados, todos sem efeito: links de TODOS os overrides dck → `experiment_states_to_overrides` no estado commitado 4372 (92 links); espelho dos overrides na **conta 1** (usuário) + links no estado 3889. `downloadAllowed: false` persistiu em todas.
- **Veredito: no GMS 26.28.60 (262860035) de produção, o gtwx NÃO aplica overrides de phenotype por nenhum canal local conhecido** (db, links, Phixit, broadcast FLAG_OVERRIDE, XML chimera). O read path real do gtwx (gtvd/gtwx em classes.dex, não decompilado ainda) provavelmente lê só a config servida — e o servidor não serve DCK p/ este modelo (params vazios).
- **Próximos passos possíveis (não executados):** (1) decompilar `classes.dex` (gtvd/gtwx client) p/ achar o read path real (pode haver cache de snapshot p/ invalidar); (2) cirurgia protobuf no `experiment_token`/`params` servido; (3) aceitar gate server-side.
- Estado final bom: `ro.gms.dck.eligible_wcc=3` ATIVA + persistida em `.props_spoof` (DeviceID+ service.sh); wcc=3 lido em todo boot. Overrides dck ficam no db (inertes) — backup em `/data/local/tmp/phenotype.db*`.

**REVERSÃO (24/jul ~23:30, a pedido do usuário — frente encerrada):** `.perapp_props` restaurada (só Petal); `ro.gms.dck.eligible_wcc` removida de `.props_spoof` e deletada ao vivo; TODOS os overrides/links dck apagados do phenotype.db (0 restantes, backup deletado); `gms_chimera_phenotype_flags.xml` restaurado do backup; **GMS Phixit desinstalado**; `/data/local/tmp` limpo dos artefatos da sessão; gms reiniciado e verificado SEM injeção/spoof; `analysis/byd/` no repo podado (ficaram só os `.java` de análise: bsst/bsog/jy*/fj*/fk*/gtwx). **Pendência manual: re-ligar o "Umount modules" do app BYD no KSU manager** (foi desligado pra injeção). DeviceID+ `.so` com Build.* spoof (v2.2.0-dev) PERMANECE (feature válida, inócua). Bump v2.2.0 + commit do native seguem pendentes.
- Arquitetura do check no app BYD: `DigitalKeyHelper.t()` → `isCreateDigitalKeyPossible()` (GMS); erro exibido em WebView (`catalogPage`). App envia `deviceManufacturer` (0002=xiaomi) p/ servidor BYD. Verificação Samsung: `com.samsung.android.dkey` + content provider (não se aplica). Check "China devices should not use Google DCK" (bmms) PASSA (não aparece no log).

### 2.2 Petal Maps (✅ RESOLVIDA 24/jul ~18:20 — COW prop_area validado em campo)
**Objetivo:** rodar Petal Maps ≥4.7.0.316 — desde essa versão o app exige device Huawei. Instalado: **4.7.0.319** (sideload APKCombo, sha256 `fc1ebc0f…d3ad3`). HMS Core (`com.huawei.hwid`) e AppGallery já estavam no device.

**Engenharia reversa (fatos, decompilação própria de 8 APKs — artefatos em `/data/data/com.termux/files/usr/tmp/petal/`):**
- Bloqueio introduzido na **4.7.0.316** (04/mai/2026); **4.7.0.315 é a última sem bloqueio**.
- Check ÚNICO e local, na `SplashActivity` (`onCreate`/`onResume`): `tp2.g()` lê **`ro.product.manufacturer` via reflexão em `android.os.SystemProperties.get(String)`** e exige `"HUAWEI"` exato (fonte decompilada revisada em 24/jul: `out319/sources/defpackage/tp2.java` — confere; log emitido: tag `HmsMapApp_M_EnvironmentUtil`, msg `Get Manufacturer: <valor>`). Falha → diálogo não-cancelável "Petal Maps is only available for Huawei devices" → `killProcess`.
- Sem attestation server-side, sem check de HMS Core no gate, sem check de brand/model/emui no caminho do bloqueio.

**Caminho percorrido em 24/jul (Sessão 8 — cronológico):**
1. **BUG RAIZ #1 encontrado e corrigido:** os hooks `my___system_property_read`/`read_callback` checavam o retorno de `__system_property_read` com `== 0`, mas a bionic retorna o **VALUE LENGTH** (≥ 0) → spoof nunca aplicava nesses caminhos. Fix `>= 0` + retorno do len spoofado (smoke test estendido cobre get/read/read_callback).
2. **SIGILL/SIGSEGV em todo app injetado (2 incidentes):** causados por **trocar o `.so` do módulo a quente** — o ZN cacheia o entry offset do módulo no zygote; o arquivo novo tem entry em outro offset → salto p/ padding. Reboot resolve (e só). **Regra nova: `.so` zygisk só se troca com reboot.**
3. **USAP pool quebra a injeção do ZN 1.4.3 silenciosamente** (`.so` mapeado, entry nunca chamada — caso documentado análogo: NeoZygisk#73). Pool desativado (ver 1.2) → injeção estável.
4. **GOT patching não alcança o caminho do check:** com hooks comprovadamente instalados (verbose log: write+readback OK no slot certo de `libandroid_runtime`), o check leu `Xiaomi` ~350 ms depois. Trace filtrado mostrou queries incidentais (`ro.build.version.sdk`, `ro.product.board`) via `get`, mas **NENHUMA query de `ro.product.manufacturer` por qualquer um dos 3 símbolos hookados** — o JNI da MIUI lê a prop por caminho que não atravessa os slots patcheados (AOSP A16 usa `__system_property_find`+`read_callback`; MIUI aparentemente outra rota). Conclusão: GOT hooks são insuficientes aqui.
5. **Solução implementada — COW do prop_area (estilo COPG `:cow`, que é exatamente o que o COPG PRO faz):** `prop_cow.cpp` copia as páginas do prop_area que contêm a prop p/ mapping privado anônimo no MESMO endereço (per-process) e reescreve o valor in-place (serial protocol da bionic). Cobre JNI/nativo/parse direto/static-linked. Smoke test PASS: `__system_property_get` cru retorna `HUAWEI` sem nenhum hook ativo.
6. ✅ **VALIDADO 24/jul ~18:20** (pós-reboot da instalação da v2.1.1): log `Get Manufacturer: HUAWEI`, app passa do gate e abre (PrivacyActivity). Config temporária do Termux removida de `.perapp_props` (ficou só `com.huawei.maps.app|ro.product.manufacturer=HUAWEI`). Pendente: bump v2.2.0 + commit do native; **decisão em aberto: reativar USAP pool** (fato 19 — reativar pode reintroduzir injeção flaky; requer reboot p/ efeito completo).
7. **Flakiness residual do ZN:** mesmo com USAP off, houve lançamento (Termux pid 5065) em que a entry do módulo não foi chamada (sem crash, sem log). Investigar se persiste; mitigação prática: force-stop + relançar.

**Fontes de referência (pesquisa 24/jul):** AOSP `android_os_SystemProperties.cpp` (android16-release: JNI = find+read_callback, sem intrinsics em ART); COPG changelog v5.3.0 (COW substitui GOT hooking; v5.4.0: caveat de props >91 chars no A16 — nossa chave é curta, OK); KernelSU PR #3470 (leitores static-linked bypassam hooks); NeoZygisk#73 (USAP não injeta); ZN wiki FAQ (safe mode após crash de zygote; "Umount modules" do KSU = denylist p/ ZN).

**Planos B/C disponíveis (não executados):**
- **APK patch:** forçar `tp2.g()` a `return true` em smali + reassinar (java 21/aapt/apksigner ok no Termux; apktool ausente; smali/baksmali jar roda). Quebra cadeia de updates (re-patch por versão) + risco pequeno com HMS. Fontes decompilados em `usr/tmp/petal/out319/sources`.
- **COPG PRO** (`:cow` prop spoof) — pago; nossa implementação open faz o equivalente.
- Ficar na **4.7.0.315** (sem features novas).

### 2.3 Instalar DeviceID+ v2.1.1 no aparelho — ✅ FEITO 24/jul ~18:17

### 2.5 Bradesco Seguros `br.com.bradseg.bscelular` (✅ RESOLVIDO 27/jul ~11:20 — HMA-OSS)
**Sintoma:** mesma tela de bloqueio do Revolut ("ambiente não seguro", diálogo com OK). App instalado 27/jul 08:27 (v2.85.0).

**Fatos provados:**
- **Protetor = DexProtector/Licel** (`lib/arm64-v8a/libdexprotector.so` no split arm64). Processo renomeado `:p<hex>` (fato 32). APKs e logs em `analysis/bradseguros/`.
- **Estava AUSENTE do target.txt do TrickyStore** (Revolut está). Adicionado 27/jul ~09:44 — **sem efeito no veredito**.
- **Após `pm clear`: crash com `android.app.TerminateException$<obfuscado>`** (FATAL EXCEPTION main — kill deliberado do DexProtector) — **com E sem o daemon TS no ar** (attestation spoofada não muda o veredito; TEE real idem).
- **App NÃO cria aliases no keystore** (nada do uid 10363 em `/data/misc/keystore/user_0/`) → attestation local persistente improvável.
- **Pacotes root visíveis instalados:** `me.weishu.kernelsu` (manager), `com.termux`, `app.morphe.manager`, `app.pwhs.universalinstaller` (enumeração via Binder raw é vetor candidato nº1; fato 32).
- RE do APK em andamento (decompilação `analysis/bradseguros/out/`) + pesquisa de hide de pacotes sem Xposed.

**RESOLUÇÃO (27/jul ~11:20, confirmada pelo usuário — "Funcionou"):** **HMA-OSS oss-164** (zip em `tools/hma_oss/`, sha256 `4bf157db…`, id `hma_oss_zygisk`, manager `org.frknkrc44.hma_oss`) filtra a applist no system_server (cobre Binder raw). Config em `/data/misc/hide_my_applist_hmaosspreseedab/config.json` (o serviço reusa o 1º dir `hide_my_applist*` que encontra em /data/misc; foi pré-semeada e ele adotou): template `bancos` = whitelist vazio (app-alvo não vê NENHUM user app) + scope por app. **Lições HMA-OSS:** (1) config só aplica AO VIVO via manager app (ServiceClient — o serviço lê o arquivo só no boot; editar o JSON em disco sem passar pelo manager não tem efeito); (2) "Ativar" sozinho cria scope em **blacklist vazio (= não esconde nada)** — é preciso ligar o modo "Esconder" (whitelist) E aplicar o template; (3) formato decodificado do fonte (`JsonConfig.kt`, CONFIG_VERSION=93); (4) log em `<datadir>/log/runtime.log` mostra `@shouldFilterApplication: query from <pkg>` — prova da filtragem. **Revolut:** mesmo fix aplicado ~11:35 (usuário confirmou) — hipótese keybox descartada (Parte 2.4).

**⚠️ Lição TrickyStore (27/jul):** o daemon TS tem **anti-tamper** — verifica integridade dos arquivos do módulo; editar `service.sh` (ex.: DEBUG=true) faz o daemon morrer com **exit 1 silencioso**. NUNCA editar arquivos do módulo TS. Reinício manual do daemon: `cd /data/adb/modules/tricky_store && (setsid sh ./service.sh >/data/local/tmp/ts.log 2>&1 </dev/null &)`. NUNCA `pkill -f TrickyStore` via adb shell (a cmdline do próprio shell contém a string — mata o shell).

### 2.4 Dívidas técnicas documentadas
- **Revolut (✅ RESOLVIDO 27/jul ~11:35 — Sessão 11; diagnóstico da Sessão 10 abaixo mantido como referência):** a causa real era **enumeração de pacotes** (não a keybox!) — resolvido com HMA-OSS, template `bancos` (Parte 2.5). Nota: o app foi atualizado p/ 10.140 no dia, mas a variável decisiva foi a applist (query filtrada logada + app passou). **Texto original da Sessão 10:** splash "ambiente não é seguro" (veredito LOCAL do RASP, DexProtector/Licel confirmado por pesquisa). Eliminados como causa: mounts, PI 3/3, attestation TS, `/system/bin/su` (adb root off), frida-server, `/proc/modules`+`/sys/module` ksu (**resolvido permanentemente via `.ko` com hide — ver 1.2**), `/proc/kallsyms` (limpo), prctl (driver não responde — fato 30), ADB, SSAID novo + clear data, manager congelado. **Injeção zygisk no app é INVIÁVEL** (maps scan → `MessageGuardException` instantâneo). **Suspeito nº 1 (pesquisa): keybox vazada DroidWin — Revolut rejeita keyboxes populares mesmo com PI 3/3** (guia XDA 4773849: PI 3/3 + keybox queimada = bloqueio; trocar keybox = volta). Próximos passos: (1) keybox privada/não-vazada; (2) HMA-OSS/HMAL whitelist (DexProtector enumera pacotes via Binder raw); (3) TEESimulator (atenção: attestation malformada = MessageGuard); (4) attestation↔Build consistency (frankel×Xiaomi) é hipótese plausível não confirmada. SuSFS = **estruturalmente impossível neste device** (VFS built-in, exige boot.img — fato 31).
- **Keybox treadmill:** trocar `/data/adb/tricky_store/keybox.xml` quando revogar + reboot
- **Print do PIF expira 2026-08-19:** rodar Action do PIF para renovar antes
- **Atualizador de apps de sistema:** RESOLVIDO — usuário desinstalou o app de update (era by design, assinatura EU ≠ Xiaomi — ver Parte 6.E)

---

## PARTE 3 — FRENTE CAIXA (RESOLVIDA 23/jul ~21h — referência)

**Combinação que destravou:** SSAID compartilhado nos 3 apps + `br.com.gabba.Caixa` no target.txt do TrickyStore + spoof persistente `ro.build.host=c3-miui-ota-bd110` (service.sh em boot_completed+5s) + ADB/dev desligado ao usar o app (manual).

**Detector identificado (engenharia reversa):** SDK **CashShield** (`libcashshieldptr-native-lib.so`, presente no superapp E no Gabba) — paths de root/xposed, props (ro.build.host/tags/debuggable/service.adb.root), Frida/hooks, `which su`, coleta MediaDRM/GAID/AndroidID → veredito server-side. App = React Native/Expo (Module Federation). Gabba = app-companheiro de segurança (OpenCV p/ documentos, iProov/Oz p/ liveness).

**Lições dos 2 bootloops (fixes no módulo, commits `67a508b`/`7b961c4`):**
1. `settings_ssaid.xml` tem `<namespaceHashes/>` APÓS `</settings>` — qualquer edição que remova/re-adicione o closing tag o engole pra dentro de `<settings>` → system_server morre → bootloop. Apply do módulo agora insere via awk antes do closing tag + backup automático + validação pós-encode com restore.
2. `resetprop` no post-fs-data REAL bootloopeia (ao vivo com sistema de pé funciona) — spoof roda em service.sh após boot_completed.

**Watcher ADB/Dev:** REMOVIDO do módulo a pedido do usuário (bateria + janela de detecção no lançamento é imbatível por polling). ADB/dev = controle manual. Histerese implementada e descartada está no histórico git (`98315b3`).

---

## PARTE 4 — PLAYBOOK OPERACIONAL (o que funciona neste setup)

- **Flash OTA:** `scripts/flash/windows_install_upgrade_auto.bat` (ou a .bat da ROM com prompt respondido à mão) → reboot stock → `ksud boot-patch -b <init_boot_da_ROM> -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell -o <dir>` → `fastboot flash init_boot_a <patched>` → reboot. (`ksud boot-patch` roda **sem root**, só precisa do dir de saída existente.)
- **Slot:** `fastboot set_active a|b` (underscore!). Verificação de partição: `sha256sum /dev/block/by-name/<part>` via `su -mm` (shell su tem namespace flaky; `-mm` resolve).
- **Daemon-context (Magisk) para /data/adb:** micro-módulo com customize.sh (builders `tools\build_*_module.py` — trocam SCRIPT_PROP/CUSTOMIZE_SH e geram zip KSU/Magisk-compatível via donor TrickyStore ou Shamiko zip).
- **Comandos de estado:** `ksud debug version` (KSU driver), `znctl status` (ZN — mostra módulos carregados e inject_state), `ksud module list` (JSON), `update_engine_client --help`.
- **Push/pull:** sempre `/storage/emulated/0/...`.
- **Dois devices no adb:** usar `adb -s 4d7fc9af` (há um emulator-5554 aparecendo às vezes).
- **Pipes no Windows cmd:** evitar `|` dentro de `su -c "..."` (quebra) — usar scripts em arquivo ou comandos separados; evitar `\$` escapado (passa literal); `$(...)` sem escape funciona.
- **Git no Termux:** identidade já configurada no repo; push exige `gh auth setup-git` uma vez (feito). Se "divergent branches": `git pull --rebase origin main`.
- **Build do .so zygisk:** `cd modules/deviceidchanger/native && ./build.sh` (Termux clang 21, sai em `module/zygisk/arm64-v8a.so`; `./build.sh test` compila o smoke test `test_hook` — rodar como root; `./build.sh inspect` gera o inspetor standalone em /data/local/tmp). Verificar DT_NEEDED só com libs do sistema (build.sh já falha se sujar). **Deploy do .so exige reboot** (fato 20). No su do adb, exportar `PATH=/data/data/com.termux/files/usr/bin:$PATH` antes.
- **Diagnóstico de apps:** `logcat -b all -c` → lançar app → `logcat -d -b all > arquivo`. Nosso .so loga na tag `DeviceIDPlus` (carregamento de config + resumo dos hooks + COW) e `DIDPTrace` (queries filtradas, debug).
- **Ferramentas forenses próprias** (C, compilam no Termux, rodam como root em `/data/local/tmp/`): `scripts/analysis/memread.c` (process_vm_readv), `memscan.c` (varre maps p/ valores-ponteiro), `propread_test.c` (semântica de retorno das props), `native/inspect_lar.cpp` + `inspect_preload.cpp` (replay do motor de hook fora do zygisk).
- **Watcher de reboot p/ sessões:** loop lendo `/proc/uptime` via adb — reset = reboot real (`sys.boot_completed` sozinho engana se o device não cair).

---

## PARTE 5 — FATOS PROVADOS EM CAMPO (não são suposição)

1. **Shamiko 1.2.5 não carrega no ZygiskNext 1.4.3** (descarte silencioso do zn_loader). Não reinstalar sem verificar via `zygiskd status`.
2. **Magisk 30.7 sela /data/adb** (su via adb sem acesso r/w; escrita/leitura só via **daemon-context**: micro-módulo cujo customize.sh roda como root do daemon — builders em `tools\build_*_module.py`).
3. **Magisk vaza mounts em userspace** (`tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` em mountinfo) — vetor de detecção do Wallet/Caixa. **ZN whitelist nativa esconde de fato** (mountinfo limpo provado).
4. **ABL do popsicle rejeita boot.img não-assinado** (fallback de slot). **`init_boot` não está na cadeia vbmeta**; `boot`/`system`/`recovery` estão. vbmeta stock `flags=0`.
5. **Byte-patch manual no vbmeta corrompe e mata os 2 slots** — recuperação: `fastboot flash vbmeta_a/b <vbmeta original da ROM>` + `set_active=a`.
6. **Slot _b tinha vendor_boot/dtbo da HyperOS CN** (hash diferente do _a; baseline stock+stock não bootava no _b). O script OTA (flasheia 2 slots) normalizou.
7. **Quirks do ABL/fastboot:** `--set-active` genérico é NO-OP p/ rearmar slot (usar **`fastboot set_active a|b`** — reseta retry-count); `fastboot wait-for-device` não existe neste build (o fastboot aguarda sozinho quando um comando é emitido); `--disable-verity` quebrado (AVB_MAGIC falso negativo).
8. **KSU LKM (backslashxx .ko android16-6.12) carrega e responde no kernel stock abogki4639** (prova: `insmod` ao vivo + `debug version` = 32558). Features do driver são enxutas (su_compat/sulog/selinux_hide NOT_SUPPORTED) mas **`adb root` do manager funciona** (shell = uid 0, `u:r:ksu:s0`).
9. **znctl** = `/data/adb/modules/zygisksu/bin/zygiskd` (Magisk) ou `/data/adb/ksu/bin/znctl` (KSU). No KSU **não há** `denylist-policy` (hiding é via umount do manager + `znctl enforce-denylist`). Binário flaky do shell → wrapper de retry.
10. **`/sdcard` não resolve no namespace do adbd sob KSU** — usar `/storage/emulated/0/...` no adb push/pull.
11. **Termux googleplay NÃO tem RunCommandService** (versão F-Droid/GitHub tem).
12. **Magisk CLI daemon (`--denylist status`, `--sqlite`) leva SIGTRAP intermitente** ("selfchecker: checker remold.magisk detects sig5") — usar retry/daemon-context.
13. **update_engine**: Updater pode abortar (download ruim) sem dano (fallback A/B); `--cancel`/`--reset_status` limpam; botão "reiniciar" da UI pode aparecer antes da escrita terminar — **sempre verificar hash das partições antes de deixar reiniciar** (boot_b 315 vs 317 nos hashes).
14. **317 não trocou o kernel** (mesmo abogki4639 da 315) — por isso o .ko 32558 serviu direto.
15. **GSF reset NÃO muda ANDROID_ID por app** (SSAID vive em `/data/system/users/0/settings_ssaid.xml`, ABX desde A12; só factory reset zera). Bancos BR provavelmente bindam aí + ADB_ENABLED.
16. **No KSU, apps sob "umount global" NÃO recebem injeção zygisk** (ZN trata como denylisted; exceções ficam em `/data/adb/ksu/.allowlist`, binário magic "KSU"; sem CLI — só UI do manager, toggle "Umount modules" por app). Prova: Petal Maps só passou a ser injetado após desativar o umount dele (MIUI no processo leu o `HWALN` do perfil COPG).
17. **GOT/PLT patching de props funciona no A16 userspace** (smoke test standalone: direct call spoofada, dlsym control real; e no zygote via ZN: `patched=272 errors=0` no processo do Petal Maps). `__system_property_get_name` **não é exportado** na bionic do A16 — derivar nome via `__system_property_read` original. **`__system_property_read` retorna o VALUE LENGTH (≥ 0), não 0-on-success** — checar com `>= 0`.
18. **`screencap` não serve p/ diagnóstico de app aqui** (captura o Termux em foreground); `uiautomator dump` falha com "could not get idle state" com frequência e só vê lockscreen com tela bloqueada.
19. **ZN 1.4.3 NÃO injeta em processos vindos do USAP pool** (`.so` do módulo fica mapeado mas a entry nunca é chamada; sem log, sem crash — NeoZygisk#73 documenta o análogo). MIUI usa o pool agressivamente ("boost cold start"). Workaround: `device_config put runtime_native usap_pool_enabled false` (persiste) + `persist.sys.usap_pool_enabled=false` + `dalvik.vm.usap_pool_enabled=false`. ZN também pode deixar de chamar a entry esporadicamente mesmo com pool off (causa desconhecida; force-stop+relaunch mitiga).
20. **O ZN cacheia o entry offset do `.so` do módulo no zygote** — trocar o `.so` a quente (mesmo com `cp` preservando inode) faz todo app injetado crashar na specialize (SIGILL em padding abaixo do `.text` novo / SIGSEGV em zn_alloc), inclusive com o módulo "disabled" (o flag só vale após reboot). **NUNCA trocar `.so` zygisk sem reboot.**
21. **`liblog` lê props internamente** (`log.tag.*`): logar incondicionalmente dentro de um hook de `__system_property_get/find` recursa até estourar a pilha. Filtrar o log por chave ou usar guard de reentrância.
22. **`dd` em `/proc/<pid>/mem` retorna ZEROS neste setup** (toybox/SELinux) — leitura cruzada de memória só confiável via `process_vm_readv` (ferramentas próprias: `scripts/analysis/memread.c`, `memscan.c`, compiladas no device com clang do Termux; em `/data/local/tmp/`).
23. **`wrap.<pkg>` (LD_PRELOAD em app) exige `ro.debuggable=1`** — aqui é 0, não funciona. app_process com LD_PRELOAD a partir do shell FUNCIONA p/ reproduzir contexto ART (mas NÃO reproduz o contexto de lib preloaded do zygote).
24. **GOT hook não cobre o caminho JNI da MIUI p/ SystemProperties** (com hooks instalados e verificados no slot certo de libandroid_runtime, `SystemProperties.get` via reflexão segue retornando o valor real). A solução à prova de caminho é **COW do prop_area** (COPG `:cow` faz o mesmo; KernelSU PR #3470 confirma que leitores estáticos/diretos bypassam hooks dinâmicos).
25. **O match do ZN/zygisk é por NOME DE PROCESSO (nice_name), não pacote** — `com.google.android.gms` no `.perapp_props` só cobre o processo principal; `.persistent`/`.unstable` precisam de linhas próprias. Prova via `/proc/<pid>/maps`: módulo mapeado só no processo principal até adicionarmos as linhas (o Chimera DCK roda no `.persistent`).
26. **`setprop` CRIA prop `ro.*` inexistente** (root, ao vivo): `setprop ro.gms.dck.eligible_wcc 3` funcionou porque a prop não existia (ro.* só trava depois de existir). Persistência pela service.sh do DeviceID+ (resetprop pós-boot_completed).
27. **`android.os.Build.*` é assado no zygote** — nem COW nem GOT hooks alcançam; apps que checam modelo via `Build.MODEL` (ou WebView UA, derivado de Build.*) exigem spoof JNI do campo estático no processo (técnica do PIF; implementada no DeviceID+ v2.2.0-dev). Diagnóstico: DIDPTrace mostrava zero queries de `ro.product.model` no app alvo.
28. **GMS 26.28.60 produção NÃO aplica overrides de phenotype por nenhum canal local** — testados: `flag_overrides` + `flag_overrides_to_commit` + links em `experiment_states_to_overrides` (estado commitado 4372), contas 0 e 1, broadcast `com.google.android.gms.phenotype.FLAG_OVERRIDE`, edição do `gms_chimera_phenotype_flags.xml`, e o app **GMS Phixit** (escreveu ~90 flags do registry DCK). Op de leitura = `getCommittedOverridesPhixit` (canal debug). Schema novo (db v1033+) decodificado: merge exige link override↔committed_experiment_state; configs servidas ficam em `experiment_states.experiment_token` (params/dynamic_params quase sempre vazios).
29. **Digital Car Key do Google (DCK): gating documentado por RE própria** — `isCreateDigitalKeyPossible()` exige `wcc>0 && downloadAllowed`. wcc = `SystemProperties.getInt("ro.gms.dck.eligible_wcc", 0)` (classe `bsst`, classes6.dex do gms) com override opcional `DckFeatureMain__wcc_override`; `downloadAllowed` = flag `DckStub__full_module_download_allowed` (default false; jycg.java). WCC: 1=NFC, 2=+BLE, 3=+UWB. Sem config servida p/ o modelo → stub fica em defaults → módulo DCK completo nunca baixa.
30. **O driver KSU backslashxx NÃO responde prctl de fora** (probe `0xDEADBEEF` retorna -1 até como root — `scripts/analysis/ksu_probe.c`; manager/ksud usam supercall/netlink) — vetor prctl de detecção é inócuo neste setup.
31. **SuSFS é impossível em LKM** (patcheia VFS built-in; exige boot.img que o ABL rejeita; pesquisa 25/jul: ninguém distribui `.ko` com SuSFS porque é estruturalmente impossível; sus_su deprecated na v2). Rota de hide LKM: `kobject_del`+`list_del` no `kernelsu_init` (feito — fork `hide-lkm`).
32. **Apps protegidos (DexProtector) renomeiam o processo** (`:p<hex>` — `pidof <pkg>` falha; usar `ps -A | grep <pkg>`), **escaneiam `/proc/self/maps`** atrás de segmentos executáveis extras (injeção zygisk = kill instantâneo via `MessageGuardException`, código DP: 786) e **enumeram pacotes via Binder raw** (bypass PackageManager). Revolut **faz key attestation local e valida o conteúdo** (TEESimulator#41: attestation malformada = crash).

---

## PARTE 6 — PESQUISAS REALIZADAS (condensadas, com fontes)

### A. Repo yapixel/popsicle_ksu_workflow
- Kernel **GKI common genérico Google** (android16-6.12, tarball jun/2025 r58), NÃO source Xiaomi. Variante **`xxksu`** = fork `backslashxx/KernelSU` (driver 32558) + **SuSFS v2.2.0** real (15 flags CONFIG_KSU_SUSFS_*). String uname **spoofada hardcoded** `abogki444322847` (≠ nosso stock abogki4639, mesma geração KMI android16-5). README desatualizado (diz "SuSFS: N/A" — errado; promete KMI bypass que o pipeline atual não aplica). **Zero validação comunitária** (~1 download, 0 issues).

### B. AVB/bootloader Xiaomi (HyperOS 3)
- Spec AVB: UNLOCKED deveria tolerar não-assinado ([avb README](https://android.googlesource.com/platform/external/avb/+/refs/heads/main/README.md)); **Xiaomi desvia na prática** — boot modificado só sobe desabilitando verificação no vbmeta ([XDA guide](https://xdaforums.com/t/guide-unlocking-bootloader-and-disabling-verifiedboot.4104045/), [OrangeFox MR](https://gitlab.com/OrangeFox/bootable/Recovery/-/merge_requests/36)).
- `fastboot --disable-verity --disable-verification` com erro **"Failed to find AVB_MAGIC at offset: 0"** = bug conhecido do fastboot (multi-device, mesmo com arquivo válido; [PixelFlasher #346](https://github.com/badabing2005/PixelFlasher/issues/346), [razer-edge-gsi #1](https://github.com/gogopowerjackets/razer-edge-gsi/issues/1)). Workaround: patch local com avbtool ou [libxzr/vbmeta-disable-verification](https://github.com/libxzr/vbmeta-disable-verification).
- **Eng ABL**: uso documentado é destravar fastboot p/ unlock ([POCO F7 Ultra gist](https://gist.github.com/maoist2009/370dc89fa5e52bcec792dc95fe94e33b) — ⚠️ brick em NAND Toshiba/Kioxia pós-fev); **sem evidência** de que desligue AVB de boot.
- `fastboot flashing unlock_critical`: existe em alguns Xiaomi; **sem evidência** de ser necessário em HyperOS moderno.
- Ecossistema sm8850 contorna AVB via **init_boot** (LKM) — mesma rota que usamos.

### C. Ecossistema KSU (xxksu, KSU-Next, SuSFS, sources)
- **xxksu** = `backslashxx/KernelSU` (fork upstream-compliant; releases têm manager próprio `KernelSU_v3.2.5-34_32559-release.apk` + `.ko` por KMI incluindo `android16-6.12_kernelsu.ko` + `ksuinit`). Compat xxksu↔manager KSU-Next: **não documentada**. Driver exige manager ≥ 32513.
- **KernelSU-Next** tem `susfsd` integrado (SuSFS userspace).
- **Kernel source oficial popsicle EXISTE:** branch `popsicle-w-oss` em [MiCode/Xiaomi_Kernel_OpenSource](https://github.com/MiCode/Xiaomi_Kernel_OpenSource/tree/popsicle-w-oss) (mar/2026, 17/17 Pro/17 Pro Max, Android 16) — viabiliza build próprio.
- **Codenames:** 17 = pudding, 17 Pro = pandora, **17 Pro Max = popsicle**, 17 Ultra = nezha.
- **KMI congelado por geração** ([android-common](https://source.android.com/docs/core/architecture/kernel/android-common)); risco real fora do KMI: patches Xiaomi ausentes em GKI genérico quebram WiFi/BT ([WildKernels #241](https://github.com/WildKernels/GKI_KernelSU_SUSFS/issues/241)).
- **SuSFS v2.2.0** ([simonpunk/susfs4ksu branch gki-android16-6.12](https://gitlab.com/simonpunk/susfs4ksu/-/raw/gki-android16-6.12/README.md)): tool userspace `ksu_susfs` (mesmo branch/versão do patch), scripts em stages; desde v2 não depende de KPROBES.

### D. Kernels custom sm8850 (Kokuban/Picters/ReSukiSU)
- **Kokuban Kernel** ([YuzakiKokuban/android_kernel_xiaomi_sm8850](https://github.com/YuzakiKokuban/android_kernel_xiaomi_sm8850)) — suporta popsicle (CI config com device_check), releases quase diárias, 2 modos: **LKM** (patch init_boot via manager — valida nossa rota) e **ReSukiSU** (+SuSFS+KPM). Instalação via recovery (TWRP). AK3 com `patch_vbmeta_flag:auto` (⚠️ toca vbmeta).
- **Picters** (fork): foco NetHunter/WiFi injection; afirma boot OK no **pudding** com câmera/WiFi; sem relato em popsicle.
- **ReSukiSU** = fork do SukiSU-Ultra (fork do KernelSU): SuSFS integrado, multi-manager (aceita manager KSU oficial/RKSU/MKSU/SukiSU), tracepoint hooks GKI2.
- **TWRP 3.7.1 unofficial funcional para popsicle** ([XDA thread](https://xdaforums.com/t/recovery-unofficial-a16-twrp-3-7-1-for-xiaomi-17-series.4784052/), [builds](https://sourceforge.net/projects/twrp-xiaomi-17-series/)) — flash via `fastboot flash recovery` (fastboot boot temporário NÃO funciona).
- Ninguém documenta o ABL hostil que encontramos — **somos território novo**.

### E. Updater de apps de sistema xiaomi.eu
- Política oficial ([FAQ](https://xiaomi.eu/community/threads/frequently-asked-questions.73215/)): nunca atualizar apps de sistema não-Google — perde traduções/patches da EU. Staff confirma ([70844](https://xiaomi.eu/community/threads/how-to-install-the-latest-system-apps-in-eu-rom.70844/)). Padrão "baixa mas não instala" reportado: [74800](https://xiaomi.eu/community/threads/system-apps-update-problem.74800/), [67231](https://xiaomi.eu/community/threads/update-system-app.67231/), [XDA 4701181](https://xdaforums.com/t/cant-install-system-applications-from-unofficial-channels-xiaomi-12s-ultra.4701181/) (mesmo `INSTALL_FAILED_UPDATE_INCOMPATIBLE`).
- Workarounds documentados: desinstalar com root e instalar como app comum (perde privilégios/mods); LSPosed+CorePatch (morto no A16).

### F. Spoof de device-ID por app (a pesquisa que destravou a Caixa)
1. ⭐ **[sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger)** — KSU/Magisk/APatch, WebUI nativa KSU, per-app **ANDROID_ID/SSAID**: dropdown dos pacotes do settings_ssaid.xml, botão Randomize (16-hex novo), Default (restaura), backup em /sdcard; usa abx2xml/xml2abx (ABX do A12+); **exige reboot**. Propósito declarado: "banking apps are using your DeviceID/SSAID to ban you". AGPL, 244★, v1.2.1 (nov/2025), ativo. **ESCOLHIDO (e forkado como DeviceID+).**
2. [yubunus/DeviceSpoofLab-Magisk](https://github.com/yubunus/DeviceSpoofLab-Magisk) — per-app SSAID + props globais (modelo/serial via resetprop); MIT, 105★, jovem.
3. [AlirezaParsi/COPG](https://github.com/AlirezaParsi/COPG) — o mais maduro (341★, v5.9.0 de 20/jul/2026), Zygisk (requer ZygiskNext), hot sem reboot. **Tier free = Build.*/CPU/device profiles; prop spoof (`:cow`), Android ID, GPU, SIM, GAID per-app = PRO.** Usado na frente Petal Maps (ver 2.2).
- Menores: RezaArbabBot/Android-ID-Changer (APK root standalone, sem reboot), A7ALABS/ssaid-changer (A11/12), FuckAPK/FuckSSAID.
- **Nada pronto cobre MediaDRM/keystore IDs por app sem Xposed.** Alerta do agente: SSAID sozinho pode não bastar (bancos correlacionam GAID/MediaDRM/tokens).
- LSPosed não está 100% morto: fork **JingMatrix** (framework "Vector", LSPosed v1.11) tem suporte inicial a A16 via ZygiskNext — porta futura se quisermos XPrivacyLua/Android Faker.
- Referência build-from-zero (MIT): `DeviceSpoofLab/common/android_id.sh` — editar settings_ssaid.xml via abx2xml/xml2abx, chmod 600, chown 1000:1000, restorecon, reboot.

### G. Chave digital de carro no Google Wallet (24/jul — frente BYD)
- Lista oficial Google: "Pixel 6+, Samsung S21+, alguns Android 12+" ([answer/12060041](https://support.google.com/wallet/answer/12060041), [answer/13037118](https://support.google.com/wallet/answer/13037118)) — categoria real é server-side e não publicada.
- Erro "O smartphone não é compatível" é documentado pelo Google como erro do fluxo do Wallet ([answer/11358016](https://support.google.com/wallet/answer/11358016)).
- **A implementação fora da China depende de API do Google embutida na ROM pelo OEM** (HyperOS 3.0.3 habilitou no 15 Ultra; Lineage/Graphene/Huawei não têm). Liberação acontece modelo a modelo, por update de ROM e/ou server-side sem update.
- BYD é NFC-only (sem UWB) — [lista da Wikipedia de digital keys](https://en.wikipedia.org/wiki/List_of_digital_keys_in_mobile_wallets). BYD oficial fala só em iPhone/Samsung/Pixel ([byd.com/eu/ownership/byd-digital-key](https://www.byd.com/eu/ownership/byd-digital-key), [byd.com/br/chave-digital](https://www.byd.com/br/chave-digital)); lista real no app é maior (Xiaomi 12→14, 13T Pro, OPPO Find X8 — [dolphinbyd t/4976](https://dolphinbyd.com.br/t/novos-modelos-de-smartphones-com-chave-digital-liberada/4976)).
- Revogação silenciosa de chave em re-checks de integridade: caso BMW + Xiaomi 15 (Reddit r/BMW).
- Compartilhamento de chave (iPhone/Samsung → outro device) também passa pela whitelist do Google (falhou em Poco X4 Pro).

### H. Bloqueio do Petal Maps em não-Huawei (24/jul — RE própria)
- Método: decompilação de 8 APKs (jadx) de 4.5.0.303 → 4.7.0.319 (APKCombo/Uptodown). Artefatos em `/data/data/com.termux/files/usr/tmp/petal/`.
- Check único: `ro.product.manufacturer == "HUAWEI"` via reflexão em `SystemProperties.get`, na `SplashActivity`; introduzido na **4.7.0.316** (04/mai/2026); msg atual na 4.7.0.319: "Petal Maps is only available for Huawei devices."
- Classes: `com.huawei.maps.app.petalmaps.splash.SplashActivity#A`, `defpackage.tp2` (EnvironmentUtil), resource `restrictions_use_app`.
- Sem write-up público desse check encontrado (Reddit r/Petal_Maps, 4PDA estavam inacessíveis p/ scraping) — análise original.

---

## PARTE 7 — ARCO DAS SESSÕES

### Sessão 0 (21/jul/2026 — contexto herdado)
- Unlock do bootloader concluído via CVE-2026-43499 (tool Linuxoid-cn v2.0.0, auditada), xiaomi.eu OS3.0.315 instalada, Magisk 30.7 + ZygiskNext + PIF + TrickyStore + "Shamiko whitelist" (que depois se provou ilusória).

### Sessão 1 (22/jul manhã — a IA anterior)
- Tentou migrar Magisk→KSU/APatch: 6 tentativas, 0 sucessos. Concluiu (errado) que KSU era inviável. Restaurou Magisk. Wallet quebrou horas depois.

### Sessão 2 (22/jul tarde — esta linha de trabalho)
1. **Diagnóstico Wallet:** integridade 3/3 ok, keybox ok, props ok. Achado central: **Shamiko nunca carregou** — o `.so` blindado do Shamiko 1.2.5 é descartado silenciosamente pelo `zn_loader` do ZygiskNext 1.4.3 (registry `{"modules":[]}`, sem `.tmp/status`, sem injeção no maps, descrição do módulo nunca reescrita). E o flag `no_mount_znctl` que o Shamiko cria **suprimia o hiding de mounts do próprio ZN** → apps liam `tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` em `/proc/self/mountinfo`.
2. **Clean rebuild:** backup de /data/adb → `magisk --remove-modules` → wipe dirs → reinstall ZN+PIF+TS(+keybox/target restaurados) → **whitelist nativa do ZN** (`znctl denylist-policy whitelist` + `enforce-denylist enabled`; allowlist termux/gms+unstable/vending) → mountinfo de app não-listado **comprovadamente limpo**. Mas Wallet seguia flagged: **marcação server-side** (o reset GSF foi feito com o aparelho ainda vazando mounts).
3. **Decisão: KSU.** Autópsia das 6 tentativas: tentativa 5 (kernel yapixel GKI-mode) foi descartada por falso negativo (`/sys/module/kernelsu` não existe em GKI-mode; detecção é via manager/prctl); tentativa 6 misturou modos GKI+LKM (conflitante); LKM no kernel stock morria por assinatura de módulo.
4. **Muro AVB descoberto:** repack byte-perfeito do boot com kernel yapixel → **ABL rejeita boot.img não-assinado** (fallback de slot). vbmeta stock `flags=0`; **`init_boot` FORA da cadeia** (strings-scan dos descriptors: `boot`/`system`/`recovery` presentes, `init_boot` ausente).
5. **KSU LKM vitorioso:** prova ao vivo — `ksud insmod android16-6.12_kernelsu.ko` no kernel stock → módulo `ksu` LIVE + `Kernel Version: 32558` via prctl. Patch do init_boot via `ksud boot-patch --allow-shell` → boot com KSU vivo. Stack ZN+PIF+TS + **umount global** (manager) + reset Google limpo → **Wallet ✅, Caixa ✅, BYD ✅, integridade 3/3**. (BYD: o "crash PAC" era artefato do ambiente Magisk, não bug do app.)

### Sessão 3 (22/jul noite — updater de apps de sistema)
- Sintoma: updater mostra 7 updates reais, loop infinito; instalação manual também falha. **Prova forense:** `INSTALL_FAILED_UPDATE_INCOMPATIBLE` + certificados comparados: app instalado assinado por `ingbrzy@miuios.cz` (Igor Eisberg, xiaomi.eu) vs update por `miui@xiaomi.com` (oficial). **É by design** (FAQ oficial xiaomi.eu). Solução: ignorar; apps chegam na ROM. Piloto de substituição via módulo/magic-mount: magic-mount não cobre `/product` neste KSU; bind manual via post-fs-data.sh funcionou (app registrou nova assinatura) mas o app não abriu → revertido. Achado colateral: apps globais Xiaomi são família (permissão compartilhada `hyperos.permission.READ_AIACTION` entre securitycenter e aicr — `INSTALL_FAILED_DUPLICATE_PERMISSION`). **Encerrado em 23/jul: usuário desinstalou o app de update.**

### Sessão 4 (23/jul manhã — OTA 315→317)
- Quase usamos o zip **PANDORA** (Xiaomi 17 Pro) por engano — pego na verificação de nome. Redownload POPSICLE.
- Updater embutido abortou 2× (1ª: falha de download; 2ª: mostrava 22% com botão "reiniciar" enganoso; engine cancelado via `update_engine_client --cancel` + `--reset_status`).
- **Caminho vencedor:** script `windows_install_upgrade.bat` (auditado: sem wipe, sem relock, ambos slots, `set_active a`). Prompt interativo contornado com variante `_auto.bat` (`set /p` não aceita pipe no cmd.exe). Flash 100% OK (~9 GB, super em 14 partes).
- **Pré-patch:** init_boot da 317 patcheado com `ksud boot-patch --allow-shell` ANTES do flash (mesmo .ko; imagem `68f996d9…` em `backup\ksu-migration\init_boot-317-ksu.img`). Após reboot stock: `fastboot flash init_boot_a` → **317 + KSU 32558 + ZN enforce + PIF + integridade — tudo preservado**. Bônus: _b perdeu as partições CN de fábrica (script flasheia os 2 slots).

### Sessão 5 (23/jul tarde/noite — Caixa bloqueou → RESOLVIDA)
- Caixa: **bloqueio server-side do banco** (marcou o aparelho na era root+ADB visíveis). Reset GSF **não** resolve (SSAID persiste). Bancos BR também **detectam ADB ligado** (`Settings.Global.ADB_ENABLED`) — usuário mantém ADB off para usar banco.
- Pesquisa de soluções prontas (Parte 6.F) → módulo sidex15/deviceidchanger escolhido → **fork DeviceID+ v2.0.0** criado e instalado.
- **~21h: Caixa RESOLVIDA** (combinação e lições dos 2 bootloops na Parte 3). TWRP flasheado como rede de segurança.
- Noite: experimento PIF Pixel 10 para BYD digital key aplicado (Parte 2.1). Commits até `a2e90c5` (DeviceID+ v2.1.0: spoof de props por app via zygisk próprio).

### Sessão 6 (24/jul — fix DeviceID+ v2.1.1 + unificação dos docs)
- Bug reportado: app enrolled no SSAID que foi **desinstalado** bloqueava qualquer apply ("UID não encontrado" — `uidOf()` só resolve via `pm list packages`) e ficava invisível na WebUI (lista só renderiza pacotes instalados) → impossível desmarcar, deadlock.
- **Fix (commit `4a9f8f0`, pushed):** prune automático de entradas sem UID no `applySsaid()` (com toast) + apps desinstalados passam a aparecer na lista marcados como "desinstalado" (dá p/ desmarcar manual). Bump v2.1.1 (versionCode 2001001). Zip rebuildado local (gitignored), **não instalado no aparelho**.
- Docs de handover unificados neste arquivo (antes: `SESSION-HANDOVER-2026-07-23.md` + `ESTADO-ATUAL-2026-07-23.md`).

### Sessão 7 (24/jul — BYD research + Petal Maps + DeviceID+ zygisk per-app)
1. **BYD digital key — pesquisa concluída** (Partes 2.1 e 6.G): bloqueio é whitelist server-side do Google por modelo+ROM + "digital key API" que a xiaomi.eu pode não ter; PI 3/3 e spoof PIF não bastam; nenhum workaround documentado.
2. **Petal Maps — RE + 4 tentativas** (Partes 2.2 e 6.H): check único `ro.product.manufacturer=HUAWEI` via reflexão; COPG free insuficiente; descoberta que umount global do KSU bloqueia injeção zygisk; DeviceID+ v2.1.0 com zygisk próprio injeta e hooka (provado nos logs) **mas o bloqueio persiste** — hipóteses abertas (a/b/c) na Parte 2.2.
3. **Regras novas do usuário:** NUNCA rebootar por conta própria (pedir sempre — há outros agentes rodando); screencap não serve p/ diagnóstico (pega o Termux); pedir artefatos ao usuário quando necessário.
4. Casa de código: identidade git configurada, `gh auth setup-git` feito, `.gitignore` cobre `native/test_hook`.

### Sessão 8 (24/jul tarde — Petal: forense completa + COW prop_area + manager 32562)
1. **Bug raiz do spoof per-app:** hooks read/read_callback checavam `__system_property_read` com `== 0`; bionic retorna value length → spoof nunca aplicava. Fix `>= 0` + smoke test estendido (get/read/read_callback/COW).
2. **Dois incidentes de crash em apps injetados** → causa: troca de `.so` zygisk a quente com entry offset cacheado no zygote pelo ZN (fato 20). Usuário fez 4 reboots manuais no dia.
3. **USAP pool desativado** (fato 19) após provar que o ZN não injeta em processos do pool.
4. **Manager KSU atualizado 32559 → 32562 (v3.2.4-36)** via `gh release download` + `pm install -r` (APK em `tools/ksu_apk/`).
5. **GOT patching forense:** com logging verbose por slot (build `PERAPP_VERBOSE_PATCH`), provado que o patch cai no slot correto de libandroid_runtime (readback OK) e o check JNI da MIUI **mesmo assim** lê o valor real → caminho não coberto por GOT (fato 24). Ferramentas novas: `scripts/analysis/memread.c`, `memscan.c`, `propread_test.c` (compilam no Termux, ficam em `/data/local/tmp/`).
6. **COW do prop_area implementado** (`native/prop_cow.cpp`) e integrado ao módulo (COW primeiro, GOT hooks como complemento). Smoke test PASS completo. **Aguardando reboot de validação no Petal** (`.so` deployado ~17:46).
7. Pesquisa web (3 agentes): bypass do Petal não existe publicamente (nossa RE é a única); AOSP A16 JNI = find+read_callback; COPG `:cow` = COW prop_area (valida a abordagem); NeoZygisk#73 = USAP não injeta; ZN tem safe mode pós-crash de zygote.

### Sessão 9 (24/jul tarde/noite — Petal RESOLVIDA, DeviceID+ completo, BYD/DCK fundo do poço)
1. **DeviceID+ v2.1.1 instalada** (zip rebuildado com o `.so` COW — o zip anterior não o continha e teria apagado o spoof) + config re-mesclada. **Petal VALIDADO pós-reboot: `Get Manufacturer: HUAWEI`, app abre — frente Petal fechada.** Config temporária do Termux removida; COPG removido pelo usuário.
2. **BYD digital key — jornada completa até o veredito:** pesquisa desmontou o boato "Xiaomi 17 funciona" (hearsay; único sucesso real = BMW i4 + Xiaomi 17 base — BYD e BMW têm whitelists separadas; lista oficial do Google tem "17 & 17 Ultra", **não** 17 Pro Max). Spoof aurora (14 Ultra) via COW + **spoof `Build.*` JNI novo no módulo** em gms/wallet/app BYD (descoberta: match por nome de processo — fato 25; umount do BYD desligado p/ injetar) → bloqueio persistiu. **RE do app BYD + GMS:** check = `isCreateDigitalKeyPossible()` (GMS DCK); gates = `ro.gms.dck.eligible_wcc` (**setada=3, persistida**) + flag phenotype `DckStub__full_module_download_allowed` (**imbatível**: GMS produção não aplica overrides — fato 28). Testado até GMS Phixit. **Frente ENCERRADA com reversão completa** (config, prop, phenotype, Phixit, tmp) — aparelho voltou ao estado estável, PI 3/3, Petal ok.
3. **Legado:** mapa do DCK no Parte 2.1/6.G, fatos 25–29, fontes gms analisadas em `analysis/byd/*.java`, DeviceID+ com 3º mecanismo de spoof (Build.* JNI).
4. Quirks do dia: 2º device apareceu no adb (Redmi `f10c4f767d7b`, slot _b) — sempre conferir serial/modelo antes de comandos; jadx on-device fica em `/data/data/com.termux/files/usr/tmp/petal/jadx` (rodar com `sh .../bin/jadx` + java no PATH).

### Sessão 11 (27/jul manhã — Bradesco Seguros ✅ RESOLVIDO via HMA-OSS)
1. **Diagnóstico:** app v2.85.0 = DexProtector/Licel (mesmo protetor do Revolut); processo renomeado `:p<hex>`; tela de bloqueio na 1ª execução, crash `TerminateException` após `pm clear` (o crash é BAL_BLOCK do A16: o diálogo de bloqueio não consegue abrir em cold start).
2. **Vetor raiz:** enumeração de pacotes — digest pós-crash nomeia `me.weishu.kernelsu`; telemetria do app diz `isRoot:false` (não é root clássico nem attestation — app não cria aliases no keystore; crash idêntico com TS up/down). RE do APK + pesquisa por subagentes (DexProtector enumera via Binder raw — Romain Thomas jan/2026).
3. **Fix:** HMA-OSS oss-164 instalado + config pré-semeada (template `bancos` whitelist vazio; scope bradseguros) → reboot → **app FUNCIONA (usuário confirmou ~11:20)**.
4. **Anti-tamper do daemon TS descoberto** (Parte 2.5 — lição): editar arquivos do módulo = daemon morre silencioso.
5. **Revolut RESOLVIDO ~11:35** (usuário confirmou): mesmo template `bancos` aplicado via UI do HMA-OSS → app v10.140 passa. **Hipótese "keybox vazada" da Sessão 10 DESCARTADA** — o vetor era applist o tempo todo (com keybox DroidWin ativa e PI 3/3, o app funciona). Lição: DexProtector checa applist por Binder raw **antes** de qualquer attestation; HMA-OSS é prerequisito p/ apps Licel.

### Sessão 10 (25/jul manhã — Revolut: forense completa + `.ko` com hide, não resolvido)
1. **Diagnóstico:** vetores visíveis confirmados como uid do app: `/proc/modules` ksu, `/sys/module/ksu`, `/system/bin/su`. prctl inócuo (fato 30). SuSFS descartado (fato 31).
2. **Testes que falharam:** adb root OFF (sem su), frida-server removido, manager congelado, SSAID novo + clear data, injeção per-app com spoof Pixel 10 (detectada pelo maps scan — fato 32).
3. **`.ko` com hide buildado e flasheado:** fork `andersonlucasg3/KernelSU` + patch `9ea26f3` (`kobject_del`+`list_del`), build via GitHub Actions, `ksud boot-patch` no init_boot 317 stock, fastboot flash. Módulo invisível, KSU 32558 íntegro, boot normal. Rollback: re-flash `68f996d9`.
4. **Pesquisa (veredito):** Revolut = DexProtector/Licel; **suspeito nº 1 = keybox vazada** (PI 3/3 não basta; Revolut rejeita keyboxes populares — XDA 4773849); caminhos documentados: keybox privada, HMA-OSS whitelist, TEESimulator. Frente pausada (Parte 2.4).

---

## PARTE 8 — REGRAS DE OURO

- NUNCA `fastboot flashing lock` · NUNCA editar vbmeta na mão · sempre conferir **POPSICLE** (não PANDORA) nos downloads
- **NUNCA rebootar o aparelho por conta própria — sempre pedir ao usuário** (há outros agentes/sessões rodando no device)
- **NUNCA trocar o `.so` de módulo zygisk a quente** — o ZN cacheia o entry offset no zygote; todo app injetado crasha até o reboot. Build → deploy → REBOOT → teste (fato 20)
- `fastboot set_active a|b` (underscore) · adb push/pull via `/storage/emulated/0/...` ou `/data/local/tmp/`
- Dois devices no adb: `adb -s 4d7fc9af` (ou IP:5555); Git Bash: `MSYS_NO_PATHCONV=1` para paths Unix
- ADB/dev options DESLIGADOS ao usar app de banco (detecção via `Settings.Global.ADB_ENABLED`)
- Update de módulo KSU sobrescreve o dir do módulo → re-mesclar config no staged antes do reboot
- Leitura de memória de outro processo: `process_vm_readv` (memread/memscan), NUNCA `dd` em `/proc/<pid>/mem` (retorna zeros — fato 22)
- USAP pool: frente Petal fechada; reativação é decisão em aberto (deixar off não custa nada visível)
- adb com device novo/desconhecido: conferir `ro.product.model` antes de qualquer comando (um Redmi apareceu como `f10c4f767d7b` na Sessão 9)
