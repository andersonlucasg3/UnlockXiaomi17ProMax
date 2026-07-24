# DeviceID+ (fork de sidex15/deviceidchanger)

Módulo KernelSU/Magisk com WebUI para gerenciar identificadores de dispositivo por app.
Fork de [sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger), licenciado sob **AGPL-3.0** (ver `LICENSE`).

## Recursos

- **SSAID manager**: lista todos os pacotes instalados (filtro 3rd-party/todos + busca), mostra o SSAID atual de cada app, permite enrolar apps para spoof com ID global ou ID custom por app (16 hex). Edita `/data/system/users/0/settings_ssaid.xml` via `abx2xml`/`xml2abx`, inserindo entradas novas quando o app ainda não tem SSAID. Backup/restauração em `/storage/emulated/0/settings_ssaid.backup.xml`.
- **Build props**: lista editável de pares chave=valor aplicados com `ksud resetprop` (imediato via botão e no boot via `post-fs-data.sh`).
- **TrickyStore**: visualiza/edita `/data/adb/tricky_store/target.txt` (adicionar/remover pacotes), quando presente.

## Estrutura

```
module/
├── module.prop
├── customize.sh
├── post-fs-data.sh   # aplica props spoofadas no boot
├── service.sh        # aplica props spoofadas após boot_completed
├── config.json       # configuração persistida pela WebUI
└── webroot/
    ├── index.html
    └── app.js
```

A WebUI espelha `config.json` em arquivos flat (`.props_enabled`, `.props_spoof`)
para que os scripts de boot não precisem de `jq`.

## Build

Compacte o **conteúdo** de `module/` (não a pasta) em um zip:

```sh
cd module && zip -r9 ../deviceidplus.zip .
```

Instale pelo gerenciador KernelSU (KernelSU / KernelSU Next / APatch / Magisk com WebUI).

## Requisitos

- Android 12+ (SSAID em formato ABX; XML plano também é suportado)
- `abx2xml`/`xml2abx` no sistema (presentes no AOSP 12+)
- Root com KernelSU ou equivalente com suporte a WebUI (`ksu.exec`, `ksu.toast`)

## Aviso

Alterar SSAID e props de build pode violar termos de serviço de apps e disparar
detecção de integridade. Use por sua conta e risco.

## Créditos

- Projeto original: [sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger) (AGPL-3.0)
