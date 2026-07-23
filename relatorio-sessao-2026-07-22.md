# Relatório da Sessão — 2026-07-22

**Objetivo:** Migrar o root de Magisk para KernelSU (ou APatch) no Xiaomi 17 Pro Max para resolver detecção de root pelos apps Caixa, BYD e Revolut.

**Aparelho:** Xiaomi 17 Pro Max (popsicle) | xiaomi.eu OS3.0.315.0.WPBCNXM | Android 16 | kernel GKI `6.12.23-android16-*-abogki`

---

# ✅ STATUS ATUAL — definitivo (23/jul/2026, após OTA 317)

## Resumo executivo

**Upgrade OS3.0.315 → OS3.0.317 concluído com KSU preservado.** Stack idêntica à de 22/jul (abaixo), agora sobre a ROM **OS3.0.317.0.WPBCNXM** — mesmo kernel `abogki4639` (a 317 não trocou kernel, o `.ko` 32558 serviu direto). Verificado: KSU vivo, ZN enforce, PIF, integridade mantida.

**Procedimento OTA que funcionou (via script fastboot):**
1. Download da ROM POPSICLE correta (atenção: PANDORA = 17 Pro — quase usamos o zip errado; sempre conferir o nome)
2. Extração + auditoria do `windows_install_upgrade.bat` (sem wipe, sem relock, ambos slots)
3. **Pré-patch** do init_boot da ROM nova com `ksud boot-patch --allow-shell` (mesmo .ko)
4. Script de flash (prompt interativo contornado com variante `_auto.bat` — `set /p` não aceita pipe no cmd)
5. Reboot stock → `fastboot flash init_boot_a <patched>` → reboot → 317+KSU

**Notas:** (a) o Updater embutido abortou 2× (falha de download + engine cancelado) — o caminho script é o confiável; (b) aborto de OTA não danifica nada (fallback A/B); (c) o script flasheia os 2 slots — _b finalmente perdeu as partições CN de fábrica.

---

# ✅ STATUS — 22/jul/2026 (KSU LKM — referência da stack)

## Resumo executivo

**Migração Magisk → KernelSU LKM CONCLUÍDA com sucesso total nos apps prioritários.** O root agora vive dentro do kernel (módulo LKM) sobre o kernel **stock intacto**, com hiding em nível de kernel. Nenhuma modificação em `boot.img` ou `vbmeta`.

| App | Antes (Magisk) | **Agora (KSU LKM)** |
|---|---|---|
| **Google Wallet** | ❌ aviso de segurança | **✅ FUNCIONANDO** |
| **Caixa / bancos BR** | ❌ "desative root" (Magic Mount) | **✅ FUNCIONANDO** |
| **BYD** | ❌ crash (SIGSEGV/PAC) | **✅ FUNCIONANDO** |
| **Play Integrity** | 3/3 (instável) | **✅ 3/3 estável** |
| Revolut | ❌ | ❌ detector mais paranoico — dívida documentada (§ Itens abertos) |

## Stack de produção (todas as camadas)

| Camada | Componente | Detalhe |
|---|---|---|
| Kernel | **stock** `6.12.23-android16-5-g75e9b1c7ae7c-abogki463945075-4k` | `boot.img` original assinado, **zero modificação** |
| init_boot | **KSU LKM patch** | `ksuinit` + `kernelsu.ko` (driver **32558**) embarcados; gerado via `ksud boot-patch --allow-shell`; sha256 `7a9c1fb4…fc904` |
| Root | **KernelSU backslashxx** (fork upstream-compliant) | modo **LKM** — módulo de kernel carregado no boot pelo init do ramdisk; driver 32558 |
| Manager | `KernelSU_v3.2.5-34_32559-release.apk` | **adb root ON** (shell adb = uid 0, contexto `u:r:ksu:s0`); umount configurado nos perfis |
| Zygisk | **ZygiskNext 1.4.3-817** | `enforce-denylist enabled`; injeta PIF no `gms.unstable`; status: `root_status ✅KernelSU (32558)`, `inject_state 1` |
| Integridade | **PlayIntegrityFork v17** + **TrickyStore v1.4.1** | keybox **DroidWin v3.6** (1 ECDSA + 1 RSA, válido até 2029) + target.txt (gms, vending, caixa, byd, revolut, test apps) |
| Hiding | **KSU "umount modules" GLOBAL** | TODOS os apps com umount **exceto** `com.google.android.gms`, `com.android.vending`, `com.termux` |

**Prova objetiva do hiding:** `/proc/<app>/mountinfo` de app com umount contém **zero** ocorrências de `magisk|kernelsu|ksu|zygisk|debug_ramdisk`. `/proc/modules`: `ksu 172032 Live`.

## Por que o KSU venceu (e o Magisk não tinha como)

1. **Shamiko nunca esteve ativo** (descoberta da sessão): o `.so` blindado do Shamiko 1.2.5 é descartado silenciosamente pelo `zn_loader` do ZygiskNext 1.4.3 — a "stack Shamiko whitelist de 21/07" era uma ilusão; o hiding real não existia.
2. **Magisk vaza mounts em userspace**: `tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` visíveis em `/proc/self/mountinfo` de qualquer app — vetor de detecção do Wallet/Caixa.
3. **KSU umount é feito pelo próprio kernel** no namespace de cada app — sem artefatos em userspace para scanners encontrarem.
4. **O crash do BYD (PAC) era artefato do ambiente Magisk**, não bug do app: com o processo 100% limpo de injeção/mounts, o código nativo roda estável.

## A trava AVB e a porta do init_boot (conhecimento-chave do dia)

- O **ABL do popsicle rejeita `boot.img` não-assinado** mesmo desbloqueado (prova direta: repack byte-perfeito → fallback de slot). vbmeta stock `flags=0`.
- Descriptors do vbmeta: `boot`, `system`, `recovery` na cadeia — **`init_boot` FORA** (strings-scan). É por isso que Magisk (init_boot) sempre funcionou, e é a porta usada pelo **KSU LKM**.
- **Nunca** editar vbmeta na mão (byte-patch corrompeu e derrubou os 2 slots; restaurado via flash do original). Se um dia precisar: regenerar com `avbtool`.
- **Slot _b é inutilizável para testes**: `vendor_boot`/`dtbo` da HyperOS CN de fábrica (xiaomi.eu só populou o _a) — nem stock puro boota lá.
- Quirk do ABL: `--set-active` genérico é NO-OP para rearmar slot (retry não reseta) — usar **`fastboot set_active <a|b>`** (underscore).

## Rollback e recuperação (ensaïdos hoje)

| Item | Caminho | sha256 |
|---|---|---|
| init_boot Magisk 30.7 (rollback completo) | `backup\ksu-migration\init_boot_a_backup.img` | `c951cdf4…240bcd9` |
| boot stock | `backup\ksu-migration\boot_a_backup.img` | `6c48dd3f…b5893b` |
| vbmeta original (flags=0) | `rom\popsicle_eu\images\vbmeta.img` | — |
| init_boot KSU LKM atual (re-flash rápido) | `backup\ksu-migration\init_boot-lkm-allowshell.img` | `7a9c1fb4…fc904` |

Rollback para Magisk: `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img` (5 min, estado Magisk completo restaurado).

## Manutenção

- **OTA xiaomi.eu:** ANTES de reiniciar, re-patch do init_boot do slot inativo: `ksud boot-patch -b <init_boot_novo> -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell` + flash.
- **Keybox treadmill:** Wallet/integridade quebrarem → trocar `/data/adb/tricky_store/keybox.xml` por keybox fresco + reboot. Rollback de keybox ruim: reverter o arquivo + reboot.
- **Módulos:** instalar via UI do manager ou `ksud module install <zip>` (root).
- **NUNCA** `fastboot flashing lock`. **Nunca** editar vbmeta na mão.
- **adb push para o telefone:** usar `/storage/emulated/0/...` (o `/sdcard` não resolve no namespace do adbd sob KSU).

## Itens abertos / dívida técnica

- **Revolut:** detecta além de mounts/integridade (installer names, /proc/net, keymaster direto). Alavancas futuras, em ordem de preferência: **SuSFS** (hiding dentro do VFS — `.ko` SUSFS do WildKernels para KMI android16-6.12, ou kernel Kokuban **ReSukiSU** p/ sm8850), kernel próprio (source oficial `popsicle-w-oss` existe no MiCode).
- **Opcionais na toolbox:** TWRP 3.7.1 unofficial funcional p/ popsicle (flash recovery); Termux F-Droid (a versão Google Play instalada não tem RunCommandService).

## Linha do tempo (síntese do dia)

1. **Manhã:** Wallet quebrado no Magisk. Diagnóstico: integridade 3/3 ok, mas Shamiko morto e `no_mount_znctl` suprimindo o hiding do ZN → mounts Magisk vazando. Clean rebuild + whitelist nativa do ZN → hiding provado limpo, mas Wallet seguia flagged (marcação server-side registrada com o aparelho ainda vazando).
2. **Tarde:** decisão de ir para KSU. Research (yapixel/xxksu/AVB/Kokuban/eng ABL) → muro AVB no boot.img → a porta `init_boot` (fora da cadeia) → prova de vida do `.ko` no kernel stock → flash LKM no _a → **KSU vivo** → stack ZN+PIF+TS → umount global + enforce → reset Google com aparelho limpo → **Wallet, Caixa, BYD funcionando, integridade 3/3.**

---
---

# HISTÓRICO DETALHADO (cronológico)

**Resultado da Sessão 1 (manhã, superado):** KSU e APatch considerados inviáveis no kernel `abogki`. Stack Magisk restaurada. *(Conclusão revista na Sessão 2 — ver STATUS ATUAL acima.)*

## 1. Estado inicial

Stack funcional (herdada da sessão 2026-07-21):
- Magisk v30.7 (init_boot patcheado, 2 slots)
- ZygiskNext v1.4.3 (substitui Zygisk built-in quebrado no Android 16)
- PlayIntegrityFork v17 + TrickyStore v1.4.1 + keybox DroidWin v3.6
- Shamiko v1.2.5 em modo whitelist (DenyList invertida)
- Play Integrity: 3/3 ✓ | Wallet: ✓

| App | Status |
|---|---|
| Wallet | ✅ |
| Caixa | ❌ "desative root" (Magic Mount do Magisk) |
| BYD | ❌ crash (SIGSEGV, corrupção de PAC) |
| Revolut | ❌ "ambiente não é seguro" (Magic Mount) |

---

## 2. Investigação KernelSU — 6 tentativas, 0 sucessos

### Tentativa 1 — KSU padrão v3.2.5 (boot)
- **Método:** App KSU patch do `boot.img` stock → flash boot_a/b + init_boot stock
- **Resultado:** ❌ `/sys/module/kernelsu` ausente, `su` não encontrado
- **Causa:** KSU padrão não suporta kernel 6.12

### Tentativa 2 — KSU-Next v3.3.0 (boot)
- **Método:** App KSU-Next patch do `boot.img` stock → flash boot_a/b + init_boot stock
- **Resultado:** ❌ idêntico
- **Causa:** Patch de arquivo não instrumenta o kernel corretamente

### Tentativa 3 — KSU-Next direct install (via Magisk root)
- **Método:** Magisk root ativo → KSU-Next "Direct Install" → flash init_boot stock
- **Resultado:** ❌
- **Causa:** KSU-Next escreveu no `init_boot` (correto), mas foi anulado pelo flash do init_boot stock logo em seguida (**erro meu**)

### Tentativa 4 — KSU-Next LKM (init_boot)
- **Método:** KSU-Next patch do `init_boot.img` → flash init_boot KSU + boot stock
- **Resultado:** ❌
- **Causa:** Boot stock (sem hooks KSU) + init_boot KSU (sem kernel com suporte)

### Tentativa 5 — yapixel KSU+SuSFS kernel (boot)
- **Fonte:** [yapixel/popsicle_ksu_workflow](https://github.com/yapixel/popsicle_ksu_workflow) — repo específico para popsicle, kernel 6.12.23, atualizado **hoje** (260722)
- **Arquivo:** `xxksu_32558_6.12.23-260722-SuSFS_v2.2.0.zip` (AnyKernel3, 18.7 MB, SHA256 `d85333edd…`)
- **Método:** Extração manual do kernel `Image` + `magiskboot` no aparelho → unpack boot stock → substituir kernel → repack → dd boot_a/b
- **Resultado:** ❌ kernel trocado com sucesso (versão mudou de `abogki4639` para `abogki4443`) mas KSU não carregou
- **Causa:** init_boot estava stock — sem o userspace do KSU para ativar os hooks do kernel

### Tentativa 6 — yapixel kernel + KSU-Next init_boot
- **Método:** Kernel yapixel KSU+SuSFS (boot) + init_boot KSU-Next LKM (userspace)
- **Resultado:** ❌
- **Causa:** Combinação completa kernel+userspace ainda não funciona — incompatibilidade fundamental com o build GKI `abogki`

### Conclusão KSU
O kernel GKI `abogki` 6.12.23 **não expõe os hooks que o KSU precisa**, em nenhuma combinação de kernel + userspace testada. O repo yapixel atualiza diariamente — um build futuro pode resolver.

---

## 3. Investigação APatch

- **Último release:** v11142 (12/nov/2025) — 8 meses parado
- **Suporte:** kernels 3.18 até 6.1 (6.12 não suportado)
- **Issue #1169** (Android 16/kernel 6.12): fechado sem resposta do mantenedor
- **Conclusão:** APatch não suporta Android 16. Sem forks ativos encontrados.

---

## 4. Diagnósticos adicionais

### 4.1 BYD — crash nativo (SIGSEGV por PAC)
- **Tombstone (`tombstone_11`):** `sp=0, lr=0, pc=0x1f8` — stack completamente corrompido
- **PAC enabled:** `pac_enabled_keys: 000000000000000f` — ARMv8.3 PAC ativo
- **Causa:** Código nativo do BYD incompatível com PAC enforcement do Android 16, **independente de root ou injeção** (confirmado com e sem ZygiskNext/DenyList)
- **Solução:** Aguardar atualização do app pela BYD

### 4.2 Caixa — Magic Mount
- Com integridade 3/3 e DenyList ativo (sem injeção no app), mensagem mudou de "sistema inseguro" para **"desative o modo root"**
- A integridade passou, mas o app detecta artefatos do Magisk no sistema de arquivos (Magic Mount)
- **Solução:** Só KSU/APatch resolveria — inviáveis hoje

### 4.3 DenyList vs AllowList
- **DenyList ON (enforce):** apps na lista **não veem root** e **não recebem injeção** do ZygiskNext
- **AllowList/Whitelist (Shamiko):** apps na lista **veem root**; todos os outros são escondidos (usado para isolar Termux)
- Estratégia final: **DenyList padrão** (sem Shamiko), Termux fora da lista (vê root), apps bancários dentro (escondidos + sem injeção)

---

## 5. Estado final da stack

### Partições
| Partição | Conteúdo |
|---|---|
| boot_a / boot_b | Stock GKI kernel (ROM original) |
| init_boot_a / init_boot_b | Magisk v30.7 patcheado |

### Módulos (3)
| Módulo | Versão | Função |
|---|---|---|
| ZygiskNext (zygisksu) | v1.4.3-817 | Substitui Zygisk built-in (quebrado no Android 16) |
| PlayIntegrityFork | v17 | Spoof de fingerprint + campos de build |
| TrickyStore | v1.4.1 | Intercepta key attestation (hardware-backed) |

### Configurações
| Config | Valor |
|---|---|
| Zygisk (Magisk built-in) | ON (bootstrap do ZygiskNext) |
| Enforce DenyList | ON |
| DenyList | Wallet, Revolut, Caixa (superapp + cartoes), BYD |
| keybox.xml | DroidWin v3.6 (sha256 `f6d0b41c…cc78d`) — 1 ECDSA + 1 RSA, válido até 2029 |
| target.txt | gms, vending + test apps + caixa + byd + revolut |
| propspoof | `/data/adb/service.d/propspoof.sh` — verifiedbootstate=green, flash.locked=1 |

### Resultado dos apps
| App | Status | Nota |
|---|---|---|
| Play Integrity | ✅ 3/3 | BASIC + DEVICE + STRONG |
| Wallet | ✅ Funcionando | Tap-to-pay OK |
| Caixa | ❌ "desative root" | Magic Mount — gerente do banco |
| BYD | ❌ crash (PAC) | App incompatível com Android 16 — aguardar update |
| Revolut | ⏸️ Via PC/web | Migração KSU parked |

---

## 6. Lições aprendidas

- ❌ **NÃO testar KSU/APatch neste kernel `abogki`** até o repo [yapixel/popsicle_ksu_workflow](https://github.com/yapixel/popsicle_ksu_workflow) ter relatos de sucesso da comunidade
- ❌ **NÃO usar Shamiko whitelist** com DenyList padrão do Magisk — lógica invertida causa confusão
- ❌ **NÃO modificar props `ro.build.host`, `ro.product.mod_device`, `ro.xiaomi.eu.*`** → bootloop garantido
- ❌ **NÃO instalar Zygisk Assistant** → bootloop no Android 16
- ✅ **ZygiskNext v1.4.3** é obrigatório no Android 16 — Zygisk built-in do Magisk 30.7 não carrega módulos
- ✅ **Zygisk do Magisk deve ficar LIGADO** mesmo com ZygiskNext (hook de bootstrap)
- ✅ **Sempre verificar partição correta** antes de flash: KSU vai no `boot` (kernel), Magisk no `init_boot` (ramdisk). KSU-Next LKM vai no `init_boot`
- ✅ **Hash do arquivo antes do flash** — evita gravar patch inválido

---

## 7. Arquivos do projeto

```
UnlockXiaomi\
├── backup\
│   ├── 2026-07-21\              # Dumps pré-unlock (5 partições)
│   ├── ksu-migration\           # Tentativas KSU (boots, init_boots, anykernel)
│   └── 22072026_092522.zip      # Backup do usuário (4,59 GB)
├── rom\
│   ├── popsicle_eu\              # ROM xiaomi.eu extraída
│   └── magisk_patched-30700_ChHUL.img  # init_boot Magisk funcional
├── tools\
│   ├── platform-tools\          # adb/fastboot oficiais (hash verificado)
│   ├── Magisk-v30.7.apk
│   ├── ZygiskNext-v1.4.3.zip
│   ├── PlayIntegrityFork-v17.zip
│   ├── TrickyStore-v1.4.1.zip
│   ├── Shamiko-v1.2.5-414.zip   # Não instalado na stack final
│   ├── KernelSU_Next_v3.3.0.apk
│   ├── popsicle-ksu.zip         # yapixel KSU+SuSFS (AnyKernel3)
│   └── *.sh                     # Scripts utilitários
└── quarantine\
    ├── droidwin_keybox\          # keybox.xml DroidWin v3.6
    └── extracted\               # Tool CVE v2.0.0/v114514
```

## 8. Manutenção futura

- **Keybox treadmill:** ~1 semana–meses. Wallet quebrar → trocar `/data/adb/tricky_store/keybox.xml` + reboot
- **Keybox ruim:** `touch /data/adb/modules/tricky_store/disable` + reboot reverte
- **OTA xiaomi.eu:** Magisk → "Install to Inactive Slot" ANTES de reiniciar
- **NUNCA** `fastboot flashing lock`
- **Repo canário KSU:** [yapixel/popsicle_ksu_workflow](https://github.com/yapixel/popsicle_ksu_workflow) — verificar diariamente
- **Caixa/BYD:** retestar quando (a) KSU funcionar no `abogki` OU (b) gerente liberar whitelist OU (c) BYD atualizar app

---

# Adendo — Sessão 2 (22/jul/2026, tarde) — Wallet quebrou + clean rebuild + descoberta Shamiko

## Diagnóstico da regressão do Wallet

- Wallet parou horas após a Sessão 1. Verificado: keybox íntegro (`teeBroken=false`), integridade **3/3**, props ok, `/proc` com `hidepid=invisible` (ROM protege)
- **Achado 1:** a stack "Shamiko whitelist de 21/07" **nunca esteve ativa** — o `.so` do Shamiko 1.2.5 é descartado silenciosamente pelo `zn_loader` do ZygiskNext 1.4.3 (registry `{"modules":[]}`, sem `.tmp/status`, sem injeção no maps). Wallet funcionou *apesar* do Shamiko morto
- **Achado 2:** com Shamiko morto, o flag `no_mount_znctl` (criado pelo post-fs-data dele) **suprimia o hiding de mounts do próprio ZN** → apps liam `tmpfs magisk`, `/product/bin/magisk`, `/debug_ramdisk/.magisk` em `/proc/self/mountinfo` → vetor de detecção do Wallet

## Clean rebuild executado

1. Backup completo de `/data/adb` → `backup\adb-state-2026-07-22\` (keybox, target.txt, key_db, configs)
2. `magisk --remove-modules` + wipe de `/data/adb/{zygisksu,shamiko,tricky_store}` + drop do `propspoof.sh` (redundante com service.sh do Shamiko)
3. Reinstalação limpa: ZygiskNext 1.4.3 → PIF v17 → TrickyStore v1.4.1 (+keybox/target restaurados)
4. Zygisk built-in do Magisk **desligado** (README do ZN exige OFF; a lição "bootstrap ON" da Sessão 1 estava errada — ZN injeta normal sem ele)
5. **Rota B — hiding nativo do ZN** (Shamiko abandonado, v1.2.5 é o último release e não carrega no ZN 1.4.3): removido Shamiko + `no_mount_znctl` → `znctl denylist-policy whitelist` + `znctl enforce-denylist enabled` → allowlist: termux, gms, gms.unstable, vending

## Estado após clean rebuild

| Item | Status |
|---|---|
| Integridade | ✅ 3/3 |
| Mountinfo de app não-listado | ✅ Limpo (verificado via /proc — zero mounts magisk) |
| Termux root | ✅ |
| Magisk app | ✅ Normal |
| Wallet | ❌ Aviso de segurança (flag server-side — GSF reset anterior foi feito com aparelho ainda vazando) |

## Fatos operacionais (Magisk 30.7 + Android 16)

- `/data/adb` **selado pelo próprio Magisk** — leitura/escrita só via daemon-context (micro-módulo cujo customize.sh executa como root do daemon; builders em `tools\build_*_module.py`)
- `znctl` = `/data/adb/modules/zygisksu/bin/zygiskd`; flaky do shell → wrapper de retry (`tools\zn_whitelist_setup.sh`)
- `magisk --denylist status` / `--sqlite` levam SIGTRAP intermitente ("selfchecker remold.magisk sig5") — usar retry/daemon-context
- TrickyStore é daemon-based — **não aparece** em `zygiskd status` (normal)

## Fase 2 — Re-investigação KSU (em andamento)

- Autópsia das 6 tentativas da Sessão 1: tentativa 5 (kernel yapixel GKI-mode) foi descartada por **falso negativo** (`/sys/module/kernelsu` não existe em build GKI-mode; detecção correta é via Manager app). Tentativa 6 misturou modos GKI+LKM (conflitante)
- Kernel yapixel verificado localmente: `6.12.23-android16-5-...-abogki444322847-4k` — **mesma geração KMI** do stock (`android16-5`) → vendor modules devem casar. KSU ×360 + susfs ×185 no binário (compilados de fato)
- Plano: flash GKI-mode puro (boot yapixel + init_boot **stock** + KSU-Next Manager), rollback = `dd` das imagens stock da ROM (`rom\popsicle_eu\images\`)

---

# Adendo 2 — Sessão 2 continuação (22/jul/2026, tarde) — VITÓRIA KSU

## O caminho vencedor: KSU LKM via init_boot

**Descobertas que desbloquearam o caminho:**
1. **ABL rejeita boot.img não-assinado** (prova direta: repack byte-perfeito → fallback de slot). vbmeta stock `flags=0`; `boot`/`system`/`recovery` na cadeia, **`init_boot` FORA da cadeia** (strings-scan dos descriptors) — por isso Magisk funciona e KSU LKM também
2. **`init_boot` é a porta**: KSU como módulo de kernel (.ko) carregado no boot pelo init do ramdisk — kernel stock assinado permanece intacto, zero AVB
3. **Prova de vida ao vivo**: `ksud insmod android16-6.12_kernelsu.ko` no kernel stock rodando → módulo `ksu` LIVE + `Kernel Version: 32558` via prctl
4. **Slot _b inutilizável p/ testes**: partições `vendor_boot`/`dtbo` da HyperOS CN de fábrica (xiaomi.eu só populou o _a) — nem stock puro boota no _b. Testes no _a com recuperação via fastboot
5. **Quirks do ABL Xiaomi**: `--set-active` genérico é NO-OP para rearmar slot (retry-count não reseta) — usar `fastboot set_active <a|b>` (underscore)
6. **vbmeta não se edita na mão** (byte-patch corrompeu e matou ambos os slots; restaurado via flash do original) — regenerar com avbtool se necessário

## Stack final em produção

| Camada | Conteúdo |
|---|---|
| boot.img | **Stock** abogki4639 (intocado) |
| init_boot | **KSU LKM 32558** (backslashxx .ko + ksuinit, `ksud boot-patch --allow-shell`) |
| Root | KernelSU driver 32558 + manager backslashxx v3.2.5-34 (32559), **adb root ON** |
| Zygisk | ZygiskNext 1.4.3-817, `enforce-denylist enabled` |
| Integridade | PlayIntegrityFork v17 + TrickyStore v1.4.1 (keybox DroidWin v3.6) |
| Hiding | **umount modules p/ TODOS os apps** exceto gms/vending/Termux (perfis KSU) |

## Resultado dos apps (FINAL)

| App | Magisk (ontem) | **KSU (hoje)** |
|---|---|---|
| Play Integrity | 3/3 | **3/3 ✅** |
| Wallet | ❌ aviso segurança | **✅ FUNCIONANDO** |
| Caixa/bancos | ❌ Magic Mount | **✅ FUNCIONANDO** |
| BYD | ❌ crash PAC | **✅ FUNCIONANDO** |
| Revolut | ❌ ambiente inseguro | ❌ (detector mais paranoico; dívida documentada — alavanca futura: SuSFS) |

**Lição BYD:** o "crash PAC independente de root" era na verdade artefato do ambiente Magisk (resíduos de injeção/mount no processo). Com processo 100% limpo (umount global + ZN enforce), o código nativo roda estável.

## Operação KSU — referência rápida

- **Rollback p/ Magisk:** `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img` (c951cdf4)
- **Root adb:** ativado no manager (shell = uid 0, `u:r:ksu:s0`); `su` em /system/bin
- **Módulos:** UI do manager OU `ksud module install <zip>` (root)
- **znctl:** `/data/adb/ksu/bin/znctl` (status, enforce-denylist — sem denylist-policy no KSU)
- **adb push p/ telefone:** usar `/storage/emulated/0/...` (o `/sdcard` quebra no namespace do adbd sob KSU)
- **OTA xiaomi.eu:** re-patch init_boot no slot inativo via `ksud boot-patch` antes de reiniciar
- **Keybox treadmill:** trocar `/data/adb/tricky_store/keybox.xml` quando revogado
