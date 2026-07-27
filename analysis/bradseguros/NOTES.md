# Bradesco Seguros + Revolut (DexProtector) — RESOLVIDO 27/jul/2026

Resumo da frente vencedora. Detalhes operacionais completos: `docs/SESSION-HANDOVER.md` (Partes 2.4, 2.5, Sessão 11).

## Diagnóstico

- **Apps:** `br.com.bradseg.bscelular` v2.85.0 e `com.revolut.revolut` v10.140 — ambos protegidos por **DexProtector/Licel** (`lib/arm64-v8a/libdexprotector.so`).
- **Sintoma:** tela "ambiente não seguro" (diálogo com OK) na 1ª execução; após `pm clear`, crash `android.app.TerminateException$<obf>` (o diálogo não abre em cold start por BAL_BLOCK do A16/targetSdk 36 → exceção não tratada mata o processo).
- **Vetor raiz (ambos):** **enumeração de pacotes instalados** — o DexProtector lista pacotes via **Binder raw** (bypass de hooks Java do PackageManager) e flagra apps de root (`me.weishu.kernelsu` nomeado no digest pós-crash — `launch-log5.txt`). Telemetria do próprio app: `isRoot:false` (não é root clássico).
- **Descartados:** key attestation (bradseguros não cria aliases no keystore; crash idêntico com daemon TrickyStore up/down), mounts, PI 3/3, su, frida, keybox vazada (hipótese da Sessão 10 p/ Revolut — **descartada**: funciona com keybox DroidWin).

## Solução (permanente)

**HMA-OSS oss-164** (fork Zygisk do Hide My Applist, sem LSPosed): filtra a applist no **system_server** — cobre inclusive a enumeração por Binder raw.

- Zip: `tools/hma_oss/HMA-OSS-ZYGISK-oss-164-release.zip` (sha256 `4bf157db64f0daa59137436fef8eafeb3180d0fd545ca2e1196b47aa8abef9fa`), id `hma_oss_zygisk`, manager `org.frknkrc44.hma_oss`.
- Template `bancos` = **whitelist vazia** (app-alvo não vê NENHUM user app) aplicado aos 2 pacotes.
- Config: `/data/misc/hide_my_applist_hmaosspreseedab/config.json` (o serviço reusa o 1º dir `hide_my_applist*` de /data/misc; esta foi pré-semeada e adotada).
- Fonte do formato de config (CONFIG_VERSION=93) em `hma-oss/*.kt` (referência).

## Lições operacionais

1. **HMA-OSS: config só aplica AO VIVO via manager app** (ServiceClient, assinatura verificada). Editar o JSON em disco só vale no boot seguinte (serviço lê uma vez no init).
2. **"Ativar" sozinho = blacklist vazio (não esconde nada).** Precisa: modo "Esconder" (whitelist) + template aplicado.
3. Prova da filtragem no log: `<datadir>/log/runtime.log` → `@shouldFilterApplication: query from <pkg>`.
4. **Anti-tamper do daemon TrickyStore:** verifica integridade dos arquivos do módulo; qualquer edição (ex.: `DEBUG=true` no service.sh) = daemon morre com exit 1 silencioso. Reinício manual: `cd /data/adb/modules/tricky_store && (setsid sh ./service.sh >/dev/null 2>&1 </dev/null &)`. NUNCA `pkill -f TrickyStore` via adb shell (mata o próprio shell — a cmdline contém a string).
5. Novo app bancário bloqueando com essa tela: HMA-OSS → "Gerenciar apps" → app → "Ativar" + "Esconder" + template `bancos`. Sem reboot.

## Artefatos

- `launch-log1..5.txt` — logs de lançamento (log5 = o digest `me.weishu.kernelsu have been installed` + TerminateException).
- `ui-dump1.xml` — a tela de bloqueio (diálogo título "Bradesco Seguros" + botão OK).
- `hma-oss/` — config pré-semeada + fontes do formato (JsonConfig/ConfigManager/HMAService/Constants, repo frknkrc44/HMA-OSS).
- APKs e libs nativas ficam no disco (gitignored). Decompilação jadx regenerável: `tools/jadx-pull/jadx`.
