# SESSION HANDOVER — UnlockXiaomi (popsicle)
**Escrito em 23/jul/2026 ~14:30 para continuidade na próxima sessão. Contém: arco completo, todas as pesquisas (com fontes), fatos provados em campo, estado atual, pendências e playbook operacional.**

---

## PARTE 1 — ARCO DAS SESSÕES

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
- Sintoma: updater mostra 7 updates reais, loop infinito; instalação manual também falha. **Prova forense:** `INSTALL_FAILED_UPDATE_INCOMPATIBLE` + certificados comparados: app instalado assinado por `ingbrzy@miuios.cz` (Igor Eisberg, xiaomi.eu) vs update por `miui@xiaomi.com` (oficial). **É by design** (FAQ oficial xiaomi.eu: "never update non-Google system apps on our ROMs... you'll lose translations and modifications"). Solução: ignorar; apps chegam na ROM. Piloto de substituição via módulo/magic-mount: magic-mount não cobre `/product` neste KSU; bind manual via post-fs-data.sh funcionou (app registrou nova assinatura) mas o app não abriu → revertido. Achado colateral: apps globais Xiaomi são família (permissão compartilhada `hyperos.permission.READ_AIACTION` entre securitycenter e aicr — `INSTALL_FAILED_DUPLICATE_PERMISSION`).

### Sessão 4 (23/jul manhã — OTA 315→317)
- Quase usamos o zip **PANDORA** (Xiaomi 17 Pro) por engano — pego na verificação de nome. Redownload POPSICLE.
- Updater embutido abortou 2× (1ª: falha de download; 2ª: mostrava 22% com botão "reiniciar" enganoso; engine cancelado via `update_engine_client --cancel` + `--reset_status`).
- **Caminho vencedor:** script `windows_install_upgrade.bat` (auditado: sem wipe, sem relock, ambos slots, `set_active a`). Prompt interativo contornado com variante `_auto.bat` (`set /p` não aceita pipe no cmd.exe). Flash 100% OK (~9 GB, super em 14 partes).
- **Pré-patch:** init_boot da 317 patcheado com `ksud boot-patch --allow-shell` ANTES do flash (mesmo .ko; imagem `68f996d9…` em `backup\ksu-migration\init_boot-317-ksu.img`). Após reboot stock: `fastboot flash init_boot_a` → **317 + KSU 32558 + ZN enforce + PIF + integridade — tudo preservado**. Bônus: _b perdeu as partições CN de fábrica (script flasheia os 2 slots).

### Sessão 5 (23/jul tarde — Caixa bloqueou + handover)
- Caixa: **bloqueio server-side do banco** (marcou o aparelho na era root+ADB visíveis). Reset GSF **não** resolve (SSAID persiste). Bancos BR também **detectam ADB ligado** (`Settings.Global.ADB_ENABLED`) — usuário mantém ADB off para usar banco.
- Pesquisa de soluções prontas concluída → ver Parte 2.F.
- **Parado em:** módulo `deviceidchanger` baixado e enviado ao aparelho; backup do SSAID feito; falta só o usuário instalar + randomizar + reboot + testar.

---

## PARTE 2 — PESQUISAS REALIZADAS (condensadas, com fontes)

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
1. ⭐ **[sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger)** — KSU/Magisk/APatch, WebUI nativa KSU, per-app **ANDROID_ID/SSAID**: dropdown dos pacotes do settings_ssaid.xml, botão Randomize (16-hex novo), Default (restaura), backup em /sdcard; usa abx2xml/xml2abx (ABX do A12+); **exige reboot**. Propósito declarado: "banking apps are using your DeviceID/SSAID to ban you". AGPL, 244★, v1.2.1 (nov/2025), ativo. **ESCOLHIDO.**
2. [yubunus/DeviceSpoofLab-Magisk](https://github.com/yubunus/DeviceSpoofLab-Magisk) — per-app SSAID + props globais (modelo/serial via resetprop); MIT, 105★, jovem.
3. [AlirezaParsi/COPG](https://github.com/AlirezaParsi/COPG) — o mais maduro (341★, v5.9.0 de 20/jul/2026), Zygisk (requer ZygiskNext), hot sem reboot, mas **per-app Android ID é PRO (pago)**.
- Menores: RezaArbabBot/Android-ID-Changer (APK root standalone, sem reboot), A7ALABS/ssaid-changer (A11/12), FuckAPK/FuckSSAID.
- **Nada pronto cobre MediaDRM/keystore IDs por app sem Xposed.** Alerta do agente: SSAID sozinho pode não bastar (bancos correlacionam GAID/MediaDRM/tokens).
- LSPosed não está 100% morto: fork **JingMatrix** (framework "Vector", LSPosed v1.11) tem suporte inicial a A16 via ZygiskNext — porta futura se quisermos XPrivacyLua/Android Faker.
- Referência build-from-zero (MIT): `DeviceSpoofLab/common/android_id.sh` — editar settings_ssaid.xml via abx2xml/xml2abx, chmod 600, chown 1000:1000, restorecon, reboot.

---

## PARTE 3 — FATOS PROVADOS EM CAMPO (não são suposição)

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

## PARTE 4 — ESTADO ATUAL (snapshot)

**Dispositivo:** popsicle, xiaomi.eu **OS3.0.317.0.WPBCNXM**, kernel stock abogki4639 (intocado).
**Root:** KernelSU LKM 32558 (`backup\ksu-migration\init_boot-317-ksu.img` `68f996d9…`) + manager 32559 (adb root ON).
**Stack:** ZygiskNext 1.4.3-817 (enforce-denylist enabled) + PlayIntegrityFork v17 + TrickyStore v1.4.1 (keybox DroidWin v3.6 + target.txt) + **umount global** (exceto com.google.android.gms, com.android.vending, com.termux).
**Integridade:** 3/3 ✅. **Apps:** Wallet ✅, BYD ✅, **Caixa ❌ (bloqueio server-side — frente aberta, Parte 5)**, Revolut ❌ (documentado).

**Rollback Magisk:** `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img` (`c951cdf4…`).
**Boot stock 315:** `backup\ksu-migration\boot_a_backup.img` (`6c48dd3f…`).

---

## PARTE 5 — PENDÊNCIAS (ordem de execução sugerida)

### 5.1 Caixa (em andamento — próximo passo do USUÁRIO)
1. No aparelho: **KSU manager → Modules → instalar `DeviceID-Changer.zip`** (está em `/storage/emulated/0/Download/ksu/`; origem `tools\DeviceID-Changer.zip`, v1.2.1).
2. Abrir a **WebUI** do módulo → selecionar os 3 pacotes Caixa (superapp uid 10346, cartoes 10292, gabba 10361) → **Randomize** → reboot.
3. Testar Caixa **com ADB DESLIGADO** (bancos BR detectam ADB on).
4. Backup de segurança do SSAID original: `backup\settings_ssaid-pre-randomize.xml` (4.929 bytes) — restaurar via cp com root se algo quebrar.
5. **Se não bastar:** resetar GAID (Config → Google → Anúncios → redefinir ID de publicidade) e retestar; se ainda falhar → binding é MediaDRM/conta → avaliar COPG (PRO) ou build próprio (técnica `DeviceSpoofLab/common/android_id.sh` como referência).

### 5.2 Git/GitHub (config pronta, nada commitado)
1. `git branch -m master main`
2. Baseline commit (layout original): add `.gitignore`, `README.md`, `ESTADO-ATUAL-2026-07-23.md`, `SESSION-HANDOVER-2026-07-23.md`, `relatorio-sessao*.md`, `tools/*.py`, `tools/*.sh`, `quarantine/*.py`, `quarantine/droidwin_keybox/`
3. `git mv` → `docs/`, `scripts/{build,analysis,device,flash}`, `config/keybox.xml` + `scripts/flash/windows_install_upgrade_auto.bat` (novo)
4. `gh repo create UnlockXiaomi --private --source=. --push` (gh autenticado: andersonlucasg3, token keyring, scope repo — sem 2FA interativo)
- `.gitignore` e `README.md` JÁ ESCRITOS na raiz.

### 5.3 Dívidas técnicas documentadas
- Revolut: alavanca futura = SuSFS (WildKernels `.ko` p/ android16-6.12, ou kernel Kokuban ReSukiSU, ou build próprio via `popsicle-w-oss`).
- Atualizador de apps de sistema: ignorar (by design) ou módulo de substituição em bloco (apps Xiaomi globais são família — atualizar juntos por causa de permissões compartilhadas).
- Keybox treadmill: trocar `/data/adb/tricky_store/keybox.xml` quando revogar + reboot.

---

## PARTE 6 — PLAYBOOK OPERACIONAL (o que funciona neste setup)

- **Flash OTA:** `scripts/flash/windows_install_upgrade_auto.bat` (ou a .bat da ROM com prompt respondido à mão) → reboot stock → `ksud boot-patch -b <init_boot_da_ROM> -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell -o <dir>` → `fastboot flash init_boot_a <patched>` → reboot. (`ksud boot-patch` roda **sem root**, só precisa do dir de saída existente.)
- **Slot:** `fastboot set_active a|b` (underscore!). Verificação de partição: `sha256sum /dev/block/by-name/<part>` via `su -mm` (shell su tem namespace flaky; `-mm` resolve).
- **Daemon-context (Magisk) para /data/adb:** micro-módulo com customize.sh (builders `tools\build_*_module.py` — trocam SCRIPT_PROP/CUSTOMIZE_SH e geram zip KSU/Magisk-compatível via donor TrickyStore ou Shamiko zip).
- **Comandos de estado:** `ksud debug version` (KSU driver), `znctl status` (ZN), `ksud module list` (JSON), `update_engine_client --help`.
- **Push/pull:** sempre `/storage/emulated/0/...`.
- **Dois devices no adb:** usar `adb -s 4d7fc9af` (há um emulator-5554 aparecendo às vezes).
- **Pipes no Windows cmd:** evitar `|` dentro de `su -c "..."` (quebra) — usar scripts em arquivo ou comandos separados; evitar `\$` escapado (passa literal); `$(...)` sem escape funciona.
