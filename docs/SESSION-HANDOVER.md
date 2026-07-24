# SESSION HANDOVER — UnlockXiaomi (popsicle)
**Documento vivo de continuidade entre sessões. Última atualização: 24/jul/2026 (pós-Sessão 6). Unifica os antigos `SESSION-HANDOVER-2026-07-23.md` e `ESTADO-ATUAL-2026-07-23.md`. Histórico cronológico detalhado das sessões de 21–22/jul: `docs/relatorio-sessao-2026-07-22.md`.**

---

## PARTE 1 — ESTADO ATUAL (snapshot 24/jul/2026)

### 1.1 Aparelho e ROM
- **Device:** Xiaomi 17 Pro Max (`popsicle`, 2509FPN0BC), SD 8 Elite Gen 5, Android 16 (SDK 36)
- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM**
- **Kernel:** stock `6.12.23-android16-5-...-abogki463945075-4k` (intocado)
- **Bootloader:** desbloqueado (NUNCA re-travar)
- **Recovery:** TWRP 3.7.1 unofficial (variante `fix22ZX_pinwork_partialdecryption`) — mantido como rede de segurança, decripta /data com PIN (foi o que salvou os 2 bootloops da frente Caixa)

### 1.2 Root e stack
- **KernelSU LKM** driver **32558** (backslashxx v3.2.5-34) no `init_boot`; manager `me.weishu.kernelsu` 32559, **adb root ON** (funciona também via Wi-Fi: `adb tcpip 5555` → `adb connect <ip>:5555`; não persiste a reboot)
- **ZygiskNext 1.4.3-817** (enforce-denylist)
- **PlayIntegrityFork v17** com `custom.pif.prop` = **Pixel 10 (frankel, Canary ZP11.260618.005 — expira 2026-08-19, rodar Action do PIF p/ renovar)**
- **TrickyStore v1.4.1** (keybox DroidWin v3.6 + security_patch.txt=2026-07-05; target.txt inclui os 3 pacotes Caixa + `br.com.gabba.Caixa`)
- **Umount global** (exceto gms/vending/termux)
- **Play Integrity: 3/3 ✅**

### 1.3 Apps
| App | Estado |
|---|---|
| Google Wallet | ✅ |
| BYD | ✅ |
| Caixa / bancos BR | ✅ **RESOLVIDA 23/jul ~21h** (Parte 3) |
| Revolut | ❌ postergado (alavanca: SuSFS — Parte 2.3) |

### 1.4 Módulo DeviceID+ (fork próprio)
- **Local:** `modules/deviceidchanger/` (fork AGPL de sidex15/deviceidchanger, créditos no README/LICENSE). Rebuild do zip: `build_zip.ps1`
- **Versões:** v2.1.0 **instalada no aparelho**; **v2.1.1 no repo** (fix: app desinstalado bloqueava todo apply de SSAID — prune automático + listagem de desinstalados; commit `4a9f8f0`), zip rebuildado em `modules/deviceidchanger/DeviceID-Plus.zip` (gitignored), **pendente de instalação**
- **Features:** SSAID por app (lista todos os pacotes, checkbox enroll, ID global compartilhado OU custom por app, regen de ambos, backup/restore + validação anti-bootloop) · spoof de props persistente (service.sh pós-boot, default `ro.build.host=c3-miui-ota-bd110`) · spoof de props **por app** via lib zygisk própria (hook GOT de `__system_property_get/read/read_callback`, config em `.perapp_props`, aplica com force-stop, sem reboot) · editor do target.txt do TrickyStore

### 1.5 Rollback e recuperação
| Item | Caminho | sha256 |
|---|---|---|
| init_boot Magisk 30.7 (rollback completo) | `backup\ksu-migration\init_boot_a_backup.img` | `c951cdf4…` |
| boot stock 315 | `backup\ksu-migration\boot_a_backup.img` | `6c48dd3f…` |
| init_boot KSU 317 (re-flash rápido) | `backup\ksu-migration\init_boot-317-ksu.img` | `68f996d9…` |
| vbmeta original (flags=0) | `rom\popsicle_eu_3.0.315\images\vbmeta.img` | — |
| SSAID pré-randomize | `backup\settings_ssaid-pre-randomize.xml` | — |

### 1.6 Git/GitHub
- Repo: `github.com/andersonlucasg3/UnlockXiaomi` (branch `main`, push via `gh` autenticado). Último commit: `4a9f8f0` (DeviceID+ v2.1.1)
- `tools/` segue untracked (binários grandes). `.gitattributes`: `*.sh` sempre LF

---

## PARTE 2 — PENDÊNCIAS

### 2.1 BYD digital key (EM ANDAMENTO — frente ativa)
**Objetivo:** provisionar a chave digital do BYD (Destroyer 05/King BR) no celular. Erro: "celular não tem o necessário" — **na Wallet/OS, não no app BYD** (confirmado pelo usuário).

**Fatos:**
- Hardware OK (verificado no device): `nfc.ese` + `nfc.uicc` + OMAPI (`android.hardware.se.omapi.ese.xml`), `android.hardware.uwb`, HAL `secure_element-service.qti` e `com.android.se` rodando
- BYD Digital Key provisiona via **Google Wallet (Pixel 6+)** ou **Samsung Wallet (S20+)** — Xiaomi não está na lista (fontes: BYD Europe/HK)
- **Experimento aplicado:** `action.sh` do PIF rodado → gms/vending agora = **Pixel 10 (frankel)** via `/data/adb/modules/playintegrityfix/custom.pif.prop`
- ⏳ **PRÓXIMO PASSO (usuário):** retentar o provisionamento no carro
- **Se falhar:** o app Google Wallet (com.google.android.apps.walletnfcrel) **não é injetado pelo PIF** (escopo = gms.unstable + vending) e ainda vê "popsicle" → próximo nível seria injetar o Wallet app (LSPosed JingMatrix ou similar) ou gate server-side de região/conta BYD BR
- **Rollback do experimento:** apagar `custom.pif.prop` + force-stop gms/vending (PIF volta aos defaults internos que davam PI 3/3)
- ⚠️ Verificar Play Integrity após o reteste (print novo pode alterar o veredito)

### 2.2 Instalar DeviceID+ v2.1.1 no aparelho (quando conveniente)
- Zip rebuildado em `modules/deviceidchanger/DeviceID-Plus.zip`; instalar via KSU manager. Só é necessário quando precisar mexer no SSAID de novo (o bug da v2.1.0 só manifesta no apply).

### 2.3 Dívidas técnicas documentadas
- **Revolut:** alavanca futura = SuSFS (WildKernels `.ko` p/ android16-6.12, ou kernel Kokuban ReSukiSU, ou build próprio via `popsicle-w-oss`)
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
- **Comandos de estado:** `ksud debug version` (KSU driver), `znctl status` (ZN), `ksud module list` (JSON), `update_engine_client --help`.
- **Push/pull:** sempre `/storage/emulated/0/...`.
- **Dois devices no adb:** usar `adb -s 4d7fc9af` (há um emulator-5554 aparecendo às vezes).
- **Pipes no Windows cmd:** evitar `|` dentro de `su -c "..."` (quebra) — usar scripts em arquivo ou comandos separados; evitar `\$` escapado (passa literal); `$(...)` sem escape funciona.

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
3. [AlirezaParsi/COPG](https://github.com/AlirezaParsi/COPG) — o mais maduro (341★, v5.9.0 de 20/jul/2026), Zygisk (requer ZygiskNext), hot sem reboot, mas **per-app Android ID é PRO (pago)**.
- Menores: RezaArbabBot/Android-ID-Changer (APK root standalone, sem reboot), A7ALABS/ssaid-changer (A11/12), FuckAPK/FuckSSAID.
- **Nada pronto cobre MediaDRM/keystore IDs por app sem Xposed.** Alerta do agente: SSAID sozinho pode não bastar (bancos correlacionam GAID/MediaDRM/tokens).
- LSPosed não está 100% morto: fork **JingMatrix** (framework "Vector", LSPosed v1.11) tem suporte inicial a A16 via ZygiskNext — porta futura se quisermos XPrivacyLua/Android Faker.
- Referência build-from-zero (MIT): `DeviceSpoofLab/common/android_id.sh` — editar settings_ssaid.xml via abx2xml/xml2abx, chmod 600, chown 1000:1000, restorecon, reboot.

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

---

## PARTE 8 — REGRAS DE OURO

- NUNCA `fastboot flashing lock` · NUNCA editar vbmeta na mão · sempre conferir **POPSICLE** (não PANDORA) nos downloads
- `fastboot set_active a|b` (underscore) · adb push/pull via `/storage/emulated/0/...` ou `/data/local/tmp/`
- Dois devices no adb: `adb -s 4d7fc9af` (ou IP:5555); Git Bash: `MSYS_NO_PATHCONV=1` para paths Unix
- ADB/dev options DESLIGADOS ao usar app de banco (detecção via `Settings.Global.ADB_ENABLED`)
