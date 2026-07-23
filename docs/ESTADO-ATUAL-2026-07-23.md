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
| **Caixa / bancos BR** | ❌ **em investigação — frente pausada 23/jul ~19h** (usuário avaliando usar outro celular) |
| Revolut | ❌ postergado (alavanca: SuSFS) |

## 4. Frente Caixa — o que foi provado e descartado (23/jul tarde)

**Descartado em campo (não é nada disso):**
- SSAID — os 3 apps Caixa receberam **ID compartilhado** `08c406642b2f299d` (apply manual via abx2xml/xml2abx, persistiu pós-reboot)
- ADB/dev ligado — testado com chave mestra do dev desligada
- Mounts/maps vazando — mountinfo e maps do processo Caixa 100% limpos
- Props clássicas — user/release-keys, ro.debuggable=0, ro.secure=1, SELinux Enforcing
- `/system/bin/su` — **invisível para apps** (KSU virtualiza só para UIDs autorizados; uid 2000 recebe ENOENT)
- Enumeração de pacotes — QUERY_ALL_PACKAGES revogado pelo usuário + testes
- Gabba fora do TrickyStore — adicionado ao target.txt
- **`ro.build.host=xiaomi.eu`** — spoofado nos DOIS níveis (resetprop em memória + bind-mount de `/system/build.prop`, propagado e confirmado visível no processo da Caixa) → **mesma mensagem**

**Engenharia reversa (APKs analisados; relatório descartado, conclusões aqui):**
- Detector = SDK **CashShield** (`libcashshieldptr-native-lib.so`, presente no superapp E no Gabba). Superfície: paths de root/xposed, props (ro.build.host/tags/debuggable/service.adb.root), Frida/hooks, `which su`, coleta MediaDRM ID + GAID + AndroidID → **veredito provavelmente server-side**
- Gabba (`br.com.gabba.Caixa`, instalado hoje 10:13) = app-companheiro de segurança; OpenCV pra captura de documentos; iProov/Oz Forensics = liveness anti-fraude (daí as queries de câmera virtual no manifest)
- App é React Native/Expo com Module Federation (@caixasuperapp/sdk)

**Hipóteses abertas (ordem de retomada):**
1. **Caixa está no umount do KSU = sem injeção Zygisk** → TrickyStore não intercepta key attestation no processo dela → unlocked exposto ao backend. **Teste:** tirar os 3 apps Caixa do umount no manager KSU + retestar
2. Bind server-side em **MediaDRM ID / GAID / Firebase Installation ID**. **Teste:** reset GAID (Config → Google → Anúncios) + clear data dos apps Caixa
3. Marcação na conta no servidor do banco → re-registro/atendimento

**Rollback SSAID:** backup binário em `/data/local/tmp/settings_ssaid.bak` (device) e `backup/settings_ssaid-pre-randomize.xml` (PC).

## 5. Módulo DeviceID+ v2.0.0 (fork — NOVO, hoje)

- **Local:** `modules/deviceidchanger/` (fork AGPL de sidex15/deviceidchanger, créditos no README/LICENSE)
- **Zip:** `modules/deviceidchanger/DeviceID-Plus.zip` (rebuild: `build_zip.ps1`)
- **Features:** SSAID por app (lista todos os pacotes, checkbox enroll, ID global compartilhado OU custom por app, regen de ambos, backup/restore) · spoof de props no boot (`ksud resetprop`, default `ro.build.host=c3-miui-ota-bd110`) · watcher ADB/Dev (desliga ADB/dev com app monitorado em foreground, **restaura estado anterior** ao sair) · editor do target.txt do TrickyStore
- **Instalação pendente:** `ksud module install` — ⚠️ usa o mesmo id `deviceidchanger` do módulo original (instalação **substitui** o sidex15)

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
