# ESTADO ATUAL DO PROJETO — snapshot 23/jul/2026 ~20h (2ª atualização do dia)

> Substitui o snapshot de ~14:20. Retomar qualquer frente a partir daqui.

## 1. Aparelho e ROM

- **Device:** Xiaomi 17 Pro Max (`popsicle`, 2509FPN0BC), SD 8 Elite Gen 5, Android 16 (SDK 36)
- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM**
- **Kernel:** stock `6.12.23-android16-5-...-abogki463945075-4k` (intocado)
- **Bootloader:** desbloqueado (NUNCA re-travar)

## 2. Root e stack

- **KernelSU LKM** driver **32558** (backslashxx v3.2.5-34) no `init_boot`; manager `me.weishu.kernelsu` 32559, **adb root ON** (funciona também via Wi-Fi: `adb tcpip 5555` → `adb connect <ip>:5555`; não persiste a reboot)
- **ZygiskNext 1.4.3-817** (enforce-denylist), PIF v17, **TrickyStore v1.4.1** (keybox DroidWin v3.6; target.txt inclui os 3 pacotes Caixa + `br.com.gabba.Caixa` desde hoje)
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

## 5. Módulo DeviceID+ v2.0.0 (fork — NOVO, hoje)

- **Local:** `modules/deviceidchanger/` (fork AGPL de sidex15/deviceidchanger, créditos no README/LICENSE)
- **Zip:** `modules/deviceidchanger/DeviceID-Plus.zip` (rebuild: `build_zip.ps1`)
- **Features:** SSAID por app (lista todos os pacotes, checkbox enroll, ID global compartilhado OU custom por app, regen de ambos, backup/restore + validação anti-bootloop) · spoof de props persistente (service.sh pós-boot, default `ro.build.host=c3-miui-ota-bd110`) · editor do target.txt do TrickyStore
- **Instalado e ativo no aparelho** (substituiu o sidex15 original)

## 6. Git/GitHub

- Branch `main`. Commits: `8c231b3` (baseline), `dcd35aa` (fork do módulo), + reorg (`docs/`, `scripts/{build,analysis,device,flash}`, `config/keybox.xml`)
- `.gitattributes`: `*.sh` sempre LF
- Pendente: `gh repo create UnlockXiaomi --private --source=. --push` (gh autenticado: andersonlucasg3)

## 7. Resolvido hoje (não reabrir)

- Updater de apps de sistema: usuário **desinstalou o app de update** (era by design, assinatura EU ≠ Xiaomi)

## 8. Regras de ouro (inalteradas)

- NUNCA `fastboot flashing lock` · NUNCA editar vbmeta na mão · sempre conferir **POPSICLE** (não PANDORA)
- `fastboot set_active a|b` (underscore) · adb push/pull via `/storage/emulated/0/...` ou `/data/local/tmp/`
- Dois devices no adb: `adb -s 4d7fc9af` (ou IP:5555); Git Bash: `MSYS_NO_PATHCONV=1` para paths Unix
