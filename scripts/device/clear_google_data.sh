#!/system/bin/sh
# Limpa dados do nucleo Google para forcar nova identidade do dispositivo (GSF ID)
# Efeito: servidor Google re-avalia o dispositivo do zero (limpa flag sticky do Wallet)
for pkg in com.google.android.apps.walletnfcrel com.android.vending com.google.android.gms com.google.android.gsf; do
  am force-stop "$pkg"
  pm clear "$pkg"
  echo "cleared: $pkg"
done
echo "=== DONE - reboot necessario ==="
