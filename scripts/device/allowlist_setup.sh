#!/system/bin/sh
# Migra DenyList -> Allowlist (Shamiko whitelist mode)
# Listados = veem root + recebem injecao Zygisk | Nao-listados = escondidos pelo Shamiko

# Remove todos os apps atualmente escondidos (nivel pacote cobre todos os processos listados)
magisk --denylist rm br.com.flashapp
magisk --denylist rm br.com.gabba.Caixa
magisk --denylist rm br.gov.caixa.cartoes
magisk --denylist rm br.gov.caixa.superapp
magisk --denylist rm com.google.android.apps.walletnfcrel
magisk --denylist rm com.huawei.appmarket
magisk --denylist rm com.huawei.hwid
magisk --denylist rm com.nordpass.android.app.password.manager
magisk --denylist rm com.nu.production
magisk --denylist rm isolated com.byd.bydautolink:rs:com.byd.bydautolink.IsoService

# Allowlist: quem precisa ver root ou receber injecao de modulo
magisk --denylist add com.termux
magisk --denylist add com.google.android.gms
magisk --denylist add com.google.android.gms com.google.android.gms.unstable
magisk --denylist add com.android.vending

echo "=== RESULTADO ==="
magisk --denylist ls
