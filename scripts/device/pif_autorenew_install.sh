#!/system/bin/sh
# Instala o modulo pif-autorenew.zip no KernelSU.
# Do PC, faca primeiro:
#   adb push modules/pif-autorenew.zip /sdcard/
# Depois execute no dispositivo:
#   adb shell su -c 'sh /sdcard/pif_autorenew_install.sh'

ZIP=/sdcard/pif-autorenew.zip

if [ ! -f "$ZIP" ]; then
    echo "ZIP nao encontrado: $ZIP"
    echo "Faca no PC: adb push modules/pif-autorenew.zip /sdcard/"
    exit 1
fi

echo "Instalando $ZIP ..."
if su -c "ksud module install $ZIP"; then
    echo "Instalacao OK. Reinicie o dispositivo."
else
    echo "ksud indisponivel ou falhou."
    echo "Instale manualmente pelo KSU Manager: Modules -> Install -> $ZIP"
    exit 1
fi
