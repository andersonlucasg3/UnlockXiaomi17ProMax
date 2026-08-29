# Session Report — 2026-08-29

**Objective:** retomar a frente BYD DCK (validar DeviceID+ v2.3.1 e o fluxo "Add digital key").

**Device:** Xiaomi 17 Pro Max (popsicle) | xiaomi.eu OS3.0.318.0.WPBCNXM | Android 16 | KSU LKM 32558 | ZygiskNext **1.5.0-843** (atualizado de 1.4.3)

---

## ✅ Status ao fim da sessão

**Front ATIVA — avanço grande.** A injeção Zygisk no BYD foi consertada; o hook app-side (`zzfa`) é obsoleto e foi desbancado por descobertas novas; o gate real é o flag Phenotype `DckStub__full_module_download_allowed` **dentro do GMS**. Tentativas de override local do flag ainda não funcionaram.

| Componente | Estado |
|---|---|
| Injeção Zygisk no BYD | ✅ FUNCIONA (causa raiz: perfil KSU com `use_default=1` → umount efetivo ON) |
| `dck.hook=1` em `.perapp_props` | ✅ armado (mas alvo `zzfa` não existe mais — hook nunca instala) |
| `ro.gms.dck.eligible_wcc=3` | ✅ funciona (`wccSysProp: 3`, `hasWccSupport` passa) — **volátil**, não persiste reboot |
| Gate atual | ❌ `Dck module condition - downloadAllowed: false` → "reporting API failure since DCK module is disabled" |
| `isWCC3` no MMKV | ❌ `false` (decidido pelo GMS, não pelo app) |

---

## 1. Causa raiz da não-injeção (resolvida)

O byte `+0xF2` do `.allowlist` (handover anterior) **não é** o flag de umount. Layout real (stride 784, `kernel/policy/allowlist.c`):

- `+0x108` → `allow_su`
- `+0x110` → `use_default`
- `+0x111` → `umount_modules`

O BYD tinha `use_default=1` → kernel aplicava o padrão `umount_modules=true` → ZygiskNext (que usa a decisão de umount do KSU como denylist, `enforce_denylist=1`) descarregava todos os módulos no processo. **Fix (UI, KSU Manager):** App Profile → BYD → toggle "Umount modules" liga/desliga para gravar `use_default=0, umount=0`. Verificado: `+0x110=0, +0x111=0` e `.so` do DeviceID+ mapeado no processo + logs `DeviceIDPlus`.

## 2. Descobertas de forensics (agentes)

1. **App BYD atualizado: v3.4.5 → v3.5.1 (353).** O `base.apk` tem só um `classes.dex` de 90 KB (stub DexProtector); lógica real em Flutter (`libapp.so`) + dex criptografado. Zero strings DCK no dex — análise estática do app é inútil para DCK.
2. **`com.google.android.gms.dck.internal.zzfa` NÃO EXISTE MAIS** (GMS 26.34.31). Nem no app, nem no GMS base, nem no módulo.
3. **O módulo DCK Chimera EXISTE no aparelho:** `/data/user_de/0/com.google.android.gms/app_chimera/m/000000a1/dl-Dck.optional_263431150400.apk` (na sessão de julho não existia — baixou em algum momento). Classes internas obfuscadas sob pacote `m20`.
4. **Cadeia do veredito (GMS-side, módulo DCK):**
   - `m20.mdv.b()` → `mdv.e()`: `hasWccSupport` (`tpy.a()` — lê sysprop) **AND** `downloadAllowed` (`altd.e()` → `altg.e()`).
   - `altg.e()` = FilePhenotypeFlags flag **`DckStub__full_module_download_allowed`** (pacote `com.google.android.gms.dck`, default `false`).
   - A operação `IsCreateDigitalKeyPossible` é `m20.tuc.a(Context)` (verifica WCC, NFC, Secure NFC, rede, screen lock).
   - `DckStub__are_flags_synced` e `DckStub__disable_dck_support` também existem; `disable_dck_support` está false e não bloqueia.
5. **O veredito "incompatível" do app é 100% GMS-side:** o diálogo aparece porque o GMS responde falha ("reporting API failure since DCK module is disabled").

## 3. Tentativas de override do flag (falharam até agora)

| Tentativa | Resultado |
|---|---|
| Insert em `phenotype.db`: `flag_overrides` (id 532, config_package_id 775 = `com.google.android.gms.dck`, value `'1'`) + `flag_overrides_to_commit` | Linhas persistem, mas **commit nunca processado** pelo GMS |
| `.pb` forjado em `/data/user_de/0/.../phenotype/shared/com.google.android.gms.dck.pb` | Diretório errado (o real é o CE `/data/data/...`); apagado |
| Append do flag no `.pb` **real** (`/data/data/com.google.android.gms/files/phenotype/shared/com.google.android.gms.dck.pb`, 15701 B) | GMS **regenerou o arquivo** (05:31) a partir do served config, apagando nosso flag |

**Backup no device:** `/data/data/com.google.android.gms/files/phenotype/shared/dck.pb.bak` (original 15701 B).

### Por que o append no .pb não basta (análise do código `m20`)
- `aamh.cQ()`: para flags bool com `i()==false` (classe `aamu`, usada por `altg`), o valor do arquivo só é honrado se o nome do flag estiver em `aapz.d` — conjunto de **nomes com override comitado**, que vem do snapshot `aalz` (formato comprimido, InflaterInputStream), não do `.pb` plano (construtor `aapz(aaqb)` seta `d = vazio`).
- O snapshot/overrides comitados são gerados quando o GMS processa `flag_overrides_to_commit` — **gatilho de commit não encontrado ainda** (jobscheduler não mostra job phenotype do GMS; AA-Tweaker usa schema antigo incompatível).

## 4. O que o agente (morto na pausa) estava rastreando

Artefatos em `analysis/byd/` sugerem progresso na engenharia reversa do **snapshot comprimido**: `snapshot-candidates/`, `snapshot-decompress.log`, `murmur3-attempts*.log`, `hash-attempts.log`, `storage-info.pb`, `dck-snapshot-dec.pb`, `dck-snapshot-flags.log`, `dck-hashed-path.log`, `phenotype-device.db`. **Retomar com `resume` do agente de análise** (contexto: investigação do FilePhenotypeFlags/snapshot path hashing).

## 5. Paredes além desta (vistas no .pb real)

O served config do pacote dck contém allowlists server-side que serão os próximos gates:
- `DckFeature__device_oem_allowlist` (bytes 32 35 36 32 5b...)
- `DckFeatureMain__supported_vbrand_ids` / `supported_voem_and_vbrand_ids` / `supported_voem_ids2` ("00AA 00AB 00AC", "0002 0004"...)
- `DckFeatureMain__d_vs_brand_versions_*`, `DckUi__nfc_unsupported_vehicle_brand_ids`

## 6. Estado do device (persiste após a sessão)

- KSU App Profile BYD: `use_default=0, umount=0` ✅ (não reverta)
- `.perapp_props`: `com.byd.bydautolink|dck.hook=1` (hook zzfa inerte/obsoleto — remover ou apontar para alvo novo na próxima sessão)
- `ro.gms.dck.eligible_wcc=3`: setado ao vivo; **não persiste reboot** — candidato a entrar no `service.sh`/`.props_spoof` do DeviceID+ (estava vazio)
- `phenotype.db`: contém nosso override pendente (flag_overrides id 532 + to_commit) — inócuo se nunca commitado; para reverter: `delete from flag_overrides where override_id=532; delete from flag_overrides_to_commit where override_id=532;`
- MMKV do BYD: NFC_CACHE_FILE deletado (app reavalia no próximo uso)
- `dck.pb.bak` no device = .pb original do dck

## 7. Próximos passos

1. **Retomar agente de análise do snapshot** (resume agent-41): achar onde mora o snapshot `aalz` comprimido do pacote `com.google.android.gms.dck` e como o override map `f` entra nele; ou achar o gatilho que processa `flag_overrides_to_commit`.
2. Alternativa: forjar o **snapshot comprimido** (não o .pb) com o override map contendo o flag.
3. Alternativa B: hook Zygisk no processo GMS mirando `m20.altg.e()` / `m20.mdv.b()` (frágil: nomes mudam a cada update do módulo; risco PI).
4. Persistir `ro.gms.dck.eligible_wcc=3` via DeviceID+ service.sh.
5. Se `downloadAllowed` passar: próximos gates serão as allowlists `DckFeature__*` (item 5) e o backend BYD.

## 8. Artefatos (locais, NÃO commitados — ~800 MB)

`analysis/byd/`: base.apk, dex/, jadx-out/, gms-dex/, gms-dck-module{,-src,-smali,-smali2}, split-*, *.pb (dck-real, dck-patched, dck-device, gcm, storage-info), phenotype*.db, logs diversos, validation-2026-08-29.log.
