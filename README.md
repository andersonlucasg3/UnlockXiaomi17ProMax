# UnlockXiaomi — Xiaomi 17 Pro Max (popsicle)

Projeto de desbloqueio, ROM xiaomi.eu e root (KernelSU LKM) do **Xiaomi 17 Pro Max** (codinome `popsicle`, Snapdragon 8 Elite Gen 5, HyperOS 3 / Android 16).

## Estado atual (24/jul/2026)

- **ROM:** xiaomi.eu **OS3.0.317.0.WPBCNXM** (pt-BR + GMS), bootloader desbloqueado
- **Root:** KernelSU **LKM** (driver backslashxx 32558) no `init_boot` — kernel **stock intacto**
- **Stack:** ZygiskNext 1.4.3 (enforce) + PlayIntegrityFork v17 + TrickyStore v1.4.1 (keybox DroidWin) + umount global (exceto gms/vending/Termux)
- **Resultado:** Play Integrity 3/3, Google Wallet ✅, BYD ✅, Caixa ✅, **Petal Maps 4.7.0.319 ✅** (spoof prop_area COW), Revolut ❌ (postergado) — ver `docs/SESSION-HANDOVER.md`
- **Módulo próprio:** DeviceID+ v2.1.1 instalada (`modules/deviceidchanger/`, fork AGPL de sidex15) — SSAID por app, spoof de props global e por app (COW prop_area + GOT hooks + **`android.os.Build.*` via JNI**, v2.2.0-dev validado), editor TrickyStore
- **BYD digital key:** encerrada (24/jul) — gate `downloadAllowed` do GMS DCK sem workaround conhecido; mapa completo do mecanismo no handover (Parte 2.1) e `analysis/byd/`

## Estrutura do repositório

| Pasta | Conteúdo | No git? |
|---|---|---|
| `docs/` | Relatórios de sessão (histórico completo: unlock, KSU, OTA) | ✅ |
| `scripts/build/` | Geradores .py (módulos Magisk/KSU, patches) | ✅ |
| `scripts/analysis/` | Análise .py (certs, vbmeta, kernel, keybox) | ✅ |
| `scripts/device/` | Shell que roda no aparelho (.sh) | ✅ |
| `scripts/flash/` | `windows_install_upgrade_auto.bat` (flash ROM sem prompt) | ✅ |
| `config/` | `keybox.xml` (DroidWin — referência pública) | ✅ |
| `backup/` | Imagens de partição, dumps, backups | ❌ (grande) |
| `rom/` | ROMs xiaomi.eu + extraídas | ❌ (~18 GB) |
| `tools/` | Binários: platform-tools, APKs, módulos zip, magiskboot, ksud | ❌ (públicos) |
| `quarantine/` | Ferramenta CVE do unlock + extraídos | parcial |
| `updates/` | APKs/módulos de apps de sistema | ❌ |

Binários grandes/públicos ficam no disco mas fora do histórico (ver `.gitignore`).

## Procedimentos-chave (documentados em `docs/`)

- **OTA xiaomi.eu:** flash via `scripts/flash/windows_install_upgrade_auto.bat` → re-patch do `init_boot` com `ksud boot-patch -m android16-6.12_kernelsu.ko --partition init_boot --allow-shell` → `fastboot flash init_boot_a`
- **Rollback para Magisk:** `fastboot flash init_boot_a backup\ksu-migration\init_boot_a_backup.img`
- **Keybox treadmill:** trocar `/data/adb/tricky_store/keybox.xml` quando revogado + reboot
- **NUNCA** `fastboot flashing lock` · **NUNCA** editar vbmeta na mão · conferir sempre POPSICLE (não PANDORA) nos downloads
