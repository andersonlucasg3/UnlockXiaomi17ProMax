# BYD Digital Key / GMS DCK Analysis (24/jul/2026)

Decompiled sources (jadx, on-device) of key classes from Google's digital car key eligibility mechanism, collected during Session 9 of the UnlockXiaomi project.

Full context: `docs/SESSION-HANDOVER.md`, Part 2.1 (BYD front) and Part 5 facts 25–29.

## From the BYD app (`com.byd.bydautolink`, full decompile not versioned)

- The compatibility check calls `DigitalKeyFramework.getClient(ctx).isCreateDigitalKeyPossible()`
  — the verdict comes from GMS, not from a local list. Error displayed in WebView (`catalogPage` page).
- The app sends `deviceManufacturer` (code: xiaomi=0002) to the BYD server. Main logic
  obfuscated via JNI (`com.fort.andjni`).

## From GMS 26.28.60 (`com.google.android.gms@262860035`)

| File | Source | Content |
|---|---|---|
| `bsst.java` | classes6.dex | `WirelessCapabilitiesFeatures` — reads `SystemProperties.getInt("ro.gms.dck.eligible_wcc", 0)` and the `DckFeatureMain__wcc_override` override |
| `bsog.java` | classes6.dex | DCK module eligibility: `wcc>0 && downloadAllowed`; checks Samsung/China/debug |
| `jycg.java`, `jyce.java`, `jycd.java` | classes15.dex | Stub flag registry: `DckStub__full_module_download_allowed` (= `downloadAllowed`), `are_flags_synced`, `disable_dck_support` |
| `jybv.java`, `jybt.java`, `jybl.java` | classes15.dex | `DckFeatureMain__wcc_override` (long, default -1); registration of the `com.google.android.gms.dck` package |
| `fjzr.java`, `fkde.java`, `fkee.java`, `fkch.java`, `fkcm.java` | classes8.dex | Phenotype service (new schema v1033+): `SetFlagOverridesOperation`, override merge (`getCommittedOverridesPhixit`), tables `flag_overrides{,_to_commit}` / `experiment_states{,_to_overrides}` |

## Analysis verdict

Production GMS **does not apply phenotype overrides** through any local channel
(db, links, broadcast, GMS Phixit) — verified empirically. Without a served config
for the model, the DCK stub stays at defaults and the full module is never downloaded.
