# ESTADO ATUAL DO PROJETO — snapshot 23/jul/2026 ~22h (3ª atualização do dia)

> Substitui os snapshots anteriores. Retomar qualquer frente a partir daqui.

## 1. Aparelho e ROM

- **Device:** Xiaomi 17 Pro Max (`popsicle`, 2509FPN0BC), SD 8 Elite Gen 5, Android 16 (SDK 36)
- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM**
- **Kernel:** stock `6.12.23-android16-5-...-abogki463945075-4k` (intocado)
- **Bootloader:** desbloqueado (NUNCA re-travar)

## 2. Root e stack

- **KernelSU LKM** driver **32558** (backslashxx v3.2.5-34) no `init_boot`; manager `me.weishu.kernelsu` 32559, **adb root ON** (funciona também via Wi-Fi: `adb tcpip 5555` → `adb connect <ip>:5555`; não persiste a reboot)
- **ZygiskNext 1.4.3-817** (enforce-denylist), PIF v17 **com `custom.pif.prop` = Pixel 10 (frankel, Canary ZP11.260618.005, expira 2026-08-19 — rodar Action do PIF p/ renovar)**, **TrickyStore v1.4.1** (keybox DroidWin v3.6 + security_patch.txt=2026-07-05; target.txt inclui os 3 pacotes Caixa + `br.com.gabba.Caixa`)
- **Umount global** (exceto gms/vending/termux)
- **Play Integrity: 3/3 ✅** (verificado hoje)

## 3. Apps

| App | Estado |
|---|---|
| Google Wallet | ✅ |
| BYD | ✅ |
| **Caixa / bancos BR** | ✅ **RESOLVIDA 23/jul ~21h** (ver seção 4) |
| Revolut | ❌ postergado (alavanca: SuSFS) |

## 4. Frente Caixa — RESOLVIDA (23/jul ~21h)

**Combinação que destravou:** SSAID compartilhado nos 3 apps + `br.com.gabba.Caixa` no target.txt do TrickyStore + spoof persistente `ro.build.host=c3-miui-ota-bd110` (service.sh em boot_completed+5s) + ADB/dev desligado ao usar o app (manual).

**Detector identificado (engenharia reversa):** SDK **CashShield** (`libcashshieldptr-native-lib.so`, presente no superapp E no Gabba) — paths de root/xposed, props (ro.build.host/tags/debuggable/service.adb.root), Frida/hooks, `which su`, coleta MediaDRM/GAID/AndroidID → veredito server-side. App = React Native/Expo (Module Federation). Gabba = app-companheiro de segurança (OpenCV p/ documentos, iProov/Oz p/ liveness).

**Licões dos 2 bootloops (fixes no módulo, commits `67a508b`/`7b961c4`):**
1. `settings_ssaid.xml` tem `<namespaceHashes/>` APÓS `</settings>` — qualquer edição que remova/re-adicione o closing tag o engole pra dentro de `<settings>` → system_server morre → bootloop. Apply do módulo agora insere via awk antes do closing tag + backup automático + validação pós-encode com restore.
2. `resetprop` no post-fs-data REAL bootloopeia (ao vivo com sistema de pé funciona) — spoof roda em service.sh após boot_completed.

**Watcher ADB/Dev:** REMOVIDO do módulo a pedido do usuário (bateria + janela de detecção no lançamento é imbatível por polling). ADB/dev = controle manual. Histerese implementada e descartada está no histórico git (`98315b3`).

**Recovery:** TWRP 3.7.1 (variante pinwork_partialdecryption) **mantido** como rede de segurança — foi o que salvou os 2 bootloops.

- **Recovery:** TWRP 3.7.1 unofficial (variante `fix22ZX_pinwork_partialdecryption`, flasheado hoje) — **mantido como rede de segurança**, decripta /data com PIN

## 5. Frente BYD digital key (EM ANDAMENTO — parada aqui)

**Objetivo:** provisionar a chave digital do BYD (Destroyer 05/King BR) no celular. Erro: "celular não tem o necessário" — **na Wallet/OS, não no app BYD** (confirmado pelo usuário).

**Fatos:**
- Hardware OK (verificado no device): `nfc.ese` + `nfc.uicc` + OMAPI (`android.hardware.se.omapi.ese.xml`), `android.hardware.uwb`, HAL `secure_element-service.qti` e `com.android.se` rodando
- BYD Digital Key provisiona via **Google Wallet (Pixel 6+)** ou **Samsung Wallet (S20+)** — Xiaomi não está na lista (fontes: BYD Europe/HK)
- **Experimento aplicado:** `action.sh` do PIF rodado → gms/vending agora = **Pixel 10 (frankel)** via `/data/adb/modules/playintegrityfix/custom.pif.prop`
- ⏳ **PRÓXIMO PASSO (usuário):** retentar o provisionamento no carro
- **Se falhar:** o app Google Wallet (com.google.android.apps.walletnfcrel) **não é injetado pelo PIF** (escopo = gms.unstable + vending) e ainda vê "popsicle" → próximo nível seria injetar o Wallet app (LSPosed JingMatrix ou similar) ou gate server-side de região/conta BYD BR
- **Rollback do experimento:** apagar `custom.pif.prop` + force-stop gms/vending (PIF volta aos defaults internos que davam PI 3/3)
- ⚠️ Verificar Play Integrity após o reteste (print novo pode alterar o veredito)

## 6. Módulo DeviceID+ v2.0.0 (fork — instalado e ativo)

- **Local:** `modules/deviceidchanger/` (fork AGPL de sidex15/deviceidchanger, créditos no README/LICENSE)
- **Zip:** `modules/deviceidchanger/DeviceID-Plus.zip` (rebuild: `build_zip.ps1`)
- **Features:** SSAID por app (lista todos os pacotes, checkbox enroll, ID global compartilhado OU custom por app, regen de ambos, backup/restore + validação anti-bootloop) · spoof de props persistente (service.sh pós-boot, default `ro.build.host=c3-miui-ota-bd110`) · editor do target.txt do TrickyStore
- **Instalado e ativo no aparelho** (substituiu o sidex15 original)

## 7. Git/GitHub

- Repo público-privado: `github.com/andersonlucasg3/UnlockXiaomi` (branch `main`, push automático via `gh` autenticado)
- Últimos commits: `11f01c2` (watcher removido + Caixa resolvida) — ver `git log`
- `.gitattributes`: `*.sh` sempre LF

## 8. Resolvido hoje (não reabrir)

- Updater de apps de sistema: usuário **desinstalou o app de update** (era by design, assinatura EU ≠ Xiaomi)

## 9. Regras de ouro (inalteradas)

- NUNCA `fastboot flashing lock` · NUNCA editar vbmeta na mão · sempre conferir **POPSICLE** (não PANDORA)
- `fastboot set_active a|b` (underscore) · adb push/pull via `/storage/emulated/0/...` ou `/data/local/tmp/`
- Dois devices no adb: `adb -s 4d7fc9af` (ou IP:5555); Git Bash: `MSYS_NO_PATHCONV=1` para paths Unix
