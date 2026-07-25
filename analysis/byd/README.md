# Análise BYD Digital Key / GMS DCK (24/jul/2026)

Fontes decompiladas (jadx, on-device) das classes-chave do mecanismo de elegibilidade
de chave digital de carro do Google, coletadas na Sessão 9 do projeto UnlockXiaomi.

Contexto completo: `docs/SESSION-HANDOVER.md`, Parte 2.1 (frente BYD) e Parte 5 fatos 25–29.

## Do app BYD (`com.byd.bydautolink`, decompilado completo não versionado)

- O check de compatibilidade chama `DigitalKeyFramework.getClient(ctx).isCreateDigitalKeyPossible()`
  — o veredito vem do GMS, não de lista local. Erro exibido em WebView (página `catalogPage`).
- App envia `deviceManufacturer` (código: xiaomi=0002) ao servidor BYD. Lógica principal
  ofuscada via JNI (`com.fort.andjni`).

## Do GMS 26.28.60 (`com.google.android.gms@262860035`)

| Arquivo | Origem | Conteúdo |
|---|---|---|
| `bsst.java` | classes6.dex | `WirelessCapabilitiesFeatures` — lê `SystemProperties.getInt("ro.gms.dck.eligible_wcc", 0)` e o override `DckFeatureMain__wcc_override` |
| `bsog.java` | classes6.dex | Eligibility do módulo DCK: `wcc>0 && downloadAllowed`; checks Samsung/China/debug |
| `jycg.java`, `jyce.java`, `jycd.java` | classes15.dex | Registry de flags do stub: `DckStub__full_module_download_allowed` (= `downloadAllowed`), `are_flags_synced`, `disable_dck_support` |
| `jybv.java`, `jybt.java`, `jybl.java` | classes15.dex | `DckFeatureMain__wcc_override` (long, default -1); registro do pacote `com.google.android.gms.dck` |
| `fjzr.java`, `fkde.java`, `fkee.java`, `fkch.java`, `fkcm.java` | classes8.dex | Serviço phenotype (schema novo v1033+): `SetFlagOverridesOperation`, merge de overrides (`getCommittedOverridesPhixit`), tabelas `flag_overrides{,_to_commit}` / `experiment_states{,_to_overrides}` |

## Veredito da análise

O GMS de produção **não aplica overrides de phenotype** por nenhum canal local
(db, links, broadcast, GMS Phixit) — verificado empiricamente. Sem config servida
para o modelo, o stub DCK fica nos defaults e o módulo completo nunca é baixado.
