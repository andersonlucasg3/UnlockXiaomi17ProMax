#!/system/bin/sh

# 1. Desativar Shamiko (DenyList padrão do Magisk basta)
touch /data/adb/modules/zygisk_shamiko/disable

# 2. Habilitar enforce DenyList
magisk --sqlite "UPDATE settings SET value=1 WHERE key='denylist';"

# 3. Adicionar apps à DenyList (sem injeção/sem root pra eles)
for pkg in com.google.android.apps.walletnfcrel com.revolut.revolut br.gov.caixa.superapp br.gov.caixa.cartoes com.byd.bydautolink; do
    magisk --sqlite "INSERT OR REPLACE INTO denylist (package_name, process) VALUES ('$pkg','$pkg');"
done

# 4. Keybox + target
mkdir -p /data/adb/tricky_store
cp -f /data/local/tmp/keybox_new.xml /data/adb/tricky_store/keybox.xml 2>/dev/null
cp -f /data/local/tmp/target_new.txt /data/adb/tricky_store/target.txt 2>/dev/null
chmod 0644 /data/adb/tricky_store/keybox.xml /data/adb/tricky_store/target.txt 2>/dev/null

# 5. Props spoof de bootloader
mkdir -p /data/adb/service.d
cat > /data/adb/service.d/propspoof.sh <<'EOF'
#!/system/bin/sh
resetprop ro.boot.verifiedbootstate green
resetprop ro.boot.flash.locked 1
resetprop ro.boot.vbmeta.device_state locked
EOF
chmod 755 /data/adb/service.d/propspoof.sh
sh /data/adb/service.d/propspoof.sh

echo '--- modules ---'
ls /data/adb/modules/
echo '--- settings ---'
magisk --sqlite "SELECT key,value FROM settings;"
echo '--- denylist ---'
magisk --sqlite "SELECT package_name FROM denylist;"
echo '--- target.txt ---'
cat /data/adb/tricky_store/target.txt 2>/dev/null
echo 'DONE'
