# ESTADO ATUAL DO PROJETO — snapshot 23/jul/2026 ~14:20

> Escrito a pedido do usuário (parada imediata). Retomar qualquer frente a partir daqui.

## 1. Aparelho e ROM

- **Device:** Xiaomi 17 Pro Max (`popsicle`, 2509FPN0BC), SD 8 Elite Gen 5, Android 16 (SDK 36)
- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM** (atualizada hoje de 315 via script fastboot `windows_install_upgrade_auto.bat`, sem wipe, 2 slots)
- **Kernel:** stock `6.12.23-android16-5-g75e9b1c7ae7c-abogki463945075-4k` (intocado, assinado)
- **Bootloader:** desbloqueado (NUNCA re-travar)

## 2. Root e stack (funcionando, verificado hoje)

- **KernelSU LKM** driver **32558** (backslashxx/KernelSU v3.2.5-34) no `init_boot` — `.ko android16-6.12_kernelsu.ko`, patch via `ksud boot-patch --allow-shell`
- Manager: `KernelSU_v3.2.5-34_32559-release.apk`, **adb root ON** (shell = uid 0, `u:r:ksu:s0`)
- **ZygiskNext 1.4.3-817** (`enforce-denylist enabled`), PIF v17 injetado em gms
- **TrickyStore v1.4.1** — keybox DroidWin v3.6 em `/data/adb/tricky_store/keybox.xml` (+ target.txt)
- **Hiding:** KSU "umount modules" GLOBAL, exceto `com.google.android.gms`, `com.android.vending`, `com.termux`
- **Play Integrity: 3/3 ✅**

## 3. Apps

| App | Estado |
|---|---|
| Google Wallet | ✅ funcionando |
| BYD | ✅ funcionando |
| **Caixa / bancos BR** | ❌ **BLOQUEIO SERVER-SIDE** (banco marcou o aparelho na era root+ADB visíveis; reset de GSF NÃO resolve — SSAID persiste) |
| Revolut | ❌ detector mais paranoico (dívida documentada) |

## 4. Frente em andamento AGORA (Caixa)

**Diagnóstico:** bloqueio do banco atrelado a **ANDROID_ID/SSAID** (não GSF; bancos BR também detectam **ADB ligado** — usuário mantém ADB off ao usar banco).

**Solução escolhida:** módulo **`sidex15/deviceidchanger` v1.2.1** (KSU nativo + WebUI; randomiza ANDROID_ID por app sob demanda; FOSS, 244★; trata ABX do Android 12+ corretamente).

**Estado da execução (parada aqui):**
- ✅ Pesquisa feita (agente): deviceidchanger é o mais aderente; alternativas: DeviceSpoofLab-Magisk, COPG (PRO)
- ✅ Módulo baixado no PC: `tools\DeviceID-Changer.zip` (5.074.556 bytes) e **enviado ao aparelho**: `/storage/emulated/0/Download/ksu/DeviceID-Changer.zip`
- ✅ Backup do SSAID original: `backup\settings_ssaid-pre-randomize.xml` (4.929 bytes)
- ✅ UIDs Caixa: superapp `10346`, cartoes `10292`, gabba `10361`
- ⏳ **PRÓXIMO PASSO (não feito):** usuário instala o zip no **KSU manager → Modules**, abre a **WebUI** do módulo, seleciona os 3 pacotes Caixa → **Randomize** → reboot → testar app (com ADB OFF)

**Se SSAID não bastar:** binding é mais fundo (GAID/MediaDRM ID/tokens de conta) — próximo nível seria COPG ou build próprio. (GAID = Google Advertising ID, resetável em Config → Google → Anúncios.)

## 5. Git/GitHub (pendente — NÃO commitado ainda)

- Repo inicializado localmente; **branch ainda é `master`** (renomear para `main`)
- ✅ Escritos: `.gitignore` (ignora rom/, backup/, updates/, platform-tools/, extracted/, binários) e `README.md`
- ⏳ Falta: baseline commit (layout original) → `git mv` para `docs/`, `scripts/{build,analysis,device,flash}`, `config/` → commit → `gh repo create UnlockXiaomi --private --source=. --push`
- Acesso: `gh` CLI autenticado (`andersonlucasg3`, scope repo, **token em keyring — sem 2FA interativo necessário**)

## 6. Artefatos-chave e hashes

| Item | Onde | Hash/nota |
|---|---|---|
| init_boot KSU (atual, 317) | `backup\ksu-migration\init_boot-317-ksu.img` | `68f996d9…d000` |
| init_boot Magisk (rollback) | `backup\ksu-migration\init_boot_a_backup.img` | `c951cdf4…240bcd9` |
| boot stock 315 | `backup\ksu-migration\boot_a_backup.img` | `6c48dd3f…b5893b` |
| ROM 317 zip | `rom\xiaomi.eu_POPSICLE_OS3.0.317.0.WPBCNXM_16.zip` | 9,03 GB (⚠️ PANDORA = 17 Pro, arquivo errado descartado) |
| SSAID original | `backup\settings_ssaid-pre-randomize.xml` | 4.929 bytes |
| ksud/.ko/ksuinit | `tools\` + `/data/local/tmp/ksu_build/` no aparelho | re-push se faltar |

## 7. Regras de ouro do projeto

- NUNCA `fastboot flashing lock` · NUNCA editar vbmeta na mão · sempre conferir **POPSICLE** (não PANDORA)
- Slot `_b` era CN até hoje; script OTA flasheia os 2 slots (normalizado agora)
- `fastboot set_active <a|b>` (underscore) para trocar slot — `--set-active` genérico é NO-OP neste ABL
- adb push para o telefone: usar `/storage/emulated/0/...` (`/sdcard` quebra no namespace do adbd sob KSU)
- Updater de apps do sistema: loop eterno **por design** (assinatura EU `ingbrzy@miuios.cz` ≠ Xiaomi oficial) — ignorar; apps chegam na ROM
