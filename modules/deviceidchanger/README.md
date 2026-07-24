# DeviceID+ (fork de sidex15/deviceidchanger)

Módulo KernelSU/Magisk com WebUI para gerenciar identificadores de dispositivo por app.
Fork de [sidex15/deviceidchanger](https://github.com/sidex15/deviceidchanger), licenciado sob **AGPL-3.0** (ver `LICENSE`).

## Recursos

- **SSAID manager**: lista todos os pacotes instalados (filtro 3rd-party/todos + busca), mostra o SSAID atual de cada app, permite enrolar apps para spoof com ID global ou ID custom por app (16 hex). Edita `/data/system/users/0/settings_ssaid.xml` via `abx2xml`/`xml2abx`, inserindo entradas novas quando o app ainda não tem SSAID. Backup/restauração em `/storage/emulated/0/settings_ssaid.backup.xml`.
- **Build props**: lista editável de pares chave=valor aplicados com `ksud resetprop` (imediato via botão e no boot via `post-fs-data.sh`).
- **Props por app**: spoof de propriedades de sistema visível apenas para apps escolhidos, via biblioteca Zygisk própria (`zygisk/arm64-v8a.so`, fonte em `native/`). A lib carrega em `preAppSpecialize` as entradas do arquivo flat `.perapp_props` (linhas `pkg|chave=valor`, espelhado de `config.json` pela WebUI) e, apenas nos apps configurados, instala hooks de GOT/PLT nas funções bionic `__system_property_get`, `__system_property_read` e `__system_property_read_callback` de todas as imagens ELF carregadas. Apps não configurados recebem `DLCLOSE_MODULE_LIBRARY` (nada fica mapeado). Vale ao reabrir o app — sem reboot. **Atenção**: o app alvo NÃO pode estar marcado em "umount modules" no KernelSU, senão o spoof não chega até ele.
- **TrickyStore**: visualiza/edita `/data/adb/tricky_store/target.txt` (adicionar/remover pacotes), quando presente.

## Estrutura

```
module/
├── module.prop
├── customize.sh
├── post-fs-data.sh   # aplica props spoofadas no boot
├── service.sh        # aplica props spoofadas após boot_completed
├── config.json       # configuração persistida pela WebUI
├── zygisk/
│   └── arm64-v8a.so  # spoof de props por app (fonte em ../native/)
└── webroot/
    ├── index.html
    └── app.js
```

A WebUI espelha `config.json` em arquivos flat (`.props_enabled`, `.props_spoof`,
`.perapp_props`) para que os scripts de boot e a lib zygisk não precisem de `jq`.

## Build da lib nativa

Requer o clang do Termux (aarch64). Gera `module/zygisk/arm64-v8a.so`:

```sh
cd native && ./build.sh
```

Teste de fumaça on-device (prova o GOT patching no próprio processo):

```sh
cd native && ./build.sh test && su -c "$PWD/test_hook"
```

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
