# DeviceID+ (fork of sidex15/deviceidchanger)

KernelSU/Magisk module with WebUI to manage device identifiers per app.
Fork of [sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger), licensed under **AGPL-3.0** (see `LICENSE`).

## Features

- **SSAID manager**: lists all installed packages (3rd-party/all filter + search), shows the current SSAID for each app, allows enrolling apps for spoofing with a global ID or per-app custom ID (16 hex chars). Edits `/data/system/users/0/settings_ssaid.xml` via `abx2xml`/`xml2abx`, inserting new entries when the app has no SSAID yet. Backup/restore to `/storage/emulated/0/settings_ssaid.backup.xml`.
- **Build props**: editable list of key=value pairs applied with `ksud resetprop` (immediate via button and on boot via `service.sh`, after `boot_completed` — `post-fs-data.sh` is a no-op: resetprop at that stage boot-loops the device).
- **Per-app props**: system property spoofing visible only to chosen apps, via a custom Zygisk library (`zygisk/arm64-v8a.so`, source in `native/`). The lib loads, in `preAppSpecialize`, entries from the flat file `.perapp_props` (lines `pkg|key=value`, mirrored from `config.json` by the WebUI — **matches by process name**, e.g. `com.google.android.gms.persistent` is a distinct target from `com.google.android.gms`) and, only in configured apps, applies three mechanisms: (1) **prop_area COW** (`prop_cow.cpp`) — copies prop pages to a private mapping and rewrites the value in-place, covering any read path (JNI, native, direct parse, static-linked); (2) GOT/PLT hooks of the bionic functions `__system_property_get`, `__system_property_read` and `__system_property_read_callback` (`perapp_hooks.cpp`); (3) **`android.os.Build.*` spoof via JNI** (`deviceid_zygisk.cpp`) — rewrites the static fields (MODEL, DEVICE, PRODUCT, BRAND, MANUFACTURER…) in the app process, covering Java reads and WebView UA that the prop mechanisms don't reach (the Build class is initialized in the zygote). Non-configured apps receive `DLCLOSE_MODULE_LIBRARY` (nothing stays mapped). Takes effect on reopening the app — no reboot needed. **Note**: the target app MUST NOT be checked under "umount modules" in KernelSU, otherwise the spoof won't reach it.
- **TrickyStore**: views/edits `/data/adb/tricky_store/target.txt` (add/remove packages), when present.
- **Java DCK (BYD) hook**: control key `com.byd.bydautolink|dck.hook=1` in `.perapp_props` (not a prop — a module flag). In `postAppSpecialize`, only in that app, a lazy thread (`dck_hook.cpp`) waits for the app context and the GMS DCK SDK classes to exist, resolves the concrete class of `DigitalKeyFrameworkClient` via `getClient(ctx)` + `GetObjectClass`, marks `isCreateDigitalKeyPossible()` as `kAccNative` (flip of `ArtMethod::access_flags_`, offset 4 — stable layout since Android 12, validated on target Android 16) and registers a stub via `RegisterNatives` that returns `Tasks.forResult(Boolean.TRUE)`. Also hooks `DigitalKeyFramework.isDckFeatureAvailable(Context)` → `true`. No inline patch/Dobby. Any failure is a silent no-op. `.so` deployment requires reboot (ZygiskNext caches the lib in the zygote).

## Structure

```
module/
├── module.prop
├── customize.sh
├── post-fs-data.sh   # no-op (prop spoof lives in service.sh)
├── service.sh        # applies spoofed props after boot_completed
├── config.json       # configuration persisted by the WebUI
├── zygisk/
│   └── arm64-v8a.so  # per-app prop spoof (source in ../native/)
└── webroot/
    ├── index.html
    └── app.js
```

The WebUI mirrors `config.json` into flat files (`.props_enabled`, `.props_spoof`,
`.perapp_props`) so that boot scripts and the zygisk lib don't need `jq`.

## Native lib build

Requires Termux clang (aarch64). Generates `module/zygisk/arm64-v8a.so`:

```sh
cd native && ./build.sh
```

Smoke test on-device (proves GOT patching in the process itself):

```sh
cd native && ./build.sh test && su -c "$PWD/test_hook"
```

## Build

Zip the **contents** of `module/` (not the folder itself) into a zip:

```sh
cd module && zip -r9 ../deviceidplus.zip .
```

Install via the KernelSU manager (KernelSU / KernelSU Next / APatch / Magisk with WebUI).

## Requirements

- Android 12+ (SSAID in ABX format; plain XML is also supported)
- `abx2xml`/`xml2abx` on the system (present in AOSP 12+)
- Root with KernelSU or equivalent with WebUI support (`ksu.exec`, `ksu.toast`)

## Disclaimer

Changing SSAID and build props may violate app terms of service and trigger
integrity detection. Use at your own risk.

## Credits

- Original project: [sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger) (AGPL-3.0)
