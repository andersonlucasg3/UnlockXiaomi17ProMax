#!/system/bin/sh
# Bind-mount do SecurityCenter atualizado sobre o da ROM (magic mount nao cobre /product aqui)
MODDIR=${0%/*}
SRC=$MODDIR/system/product/priv-app/SecurityCenter/SecurityCenter.apk
DST=/product/priv-app/SecurityCenter/SecurityCenter.apk
chcon --reference="$DST" "$SRC" 2>/dev/null
mount --bind "$SRC" "$DST"
