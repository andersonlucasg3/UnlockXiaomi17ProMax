#!/system/bin/sh
cp -f /data/adb/tricky_store/keybox.xml /data/adb/tricky_store/keybox.xml.bak
cp -f /data/local/tmp/keybox_new.xml /data/adb/tricky_store/keybox.xml
chmod 0644 /data/adb/tricky_store/keybox.xml
chown 0:0 /data/adb/tricky_store/keybox.xml
rm -f /data/adb/modules/tricky_store/disable
echo '--- tricky_store ---'
ls -la /data/adb/tricky_store/
echo '--- hashes (devem ser iguais) ---'
sha256sum /data/adb/tricky_store/keybox.xml /data/local/tmp/keybox_new.xml
