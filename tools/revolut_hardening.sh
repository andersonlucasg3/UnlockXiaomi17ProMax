#!/system/bin/sh
echo '--- 1. target.txt TrickyStore ---'
grep -q com.revolut.revolut /data/adb/tricky_store/target.txt || echo com.revolut.revolut >> /data/adb/tricky_store/target.txt
cat /data/adb/tricky_store/target.txt
echo '--- 2. Shamiko whitelist mode ---'
mkdir -p /data/adb/shamiko
touch /data/adb/shamiko/whitelist
ls -la /data/adb/shamiko/
echo '--- 3. props spoof ---'
mkdir -p /data/adb/service.d
cat > /data/adb/service.d/propspoof.sh <<'EOF'
#!/system/bin/sh
resetprop ro.boot.verifiedbootstate green
resetprop ro.boot.flash.locked 1
resetprop ro.boot.vbmeta.device_state locked
resetprop ro.secure 1
resetprop ro.debuggable 0
resetprop sys.oem_unlock_allowed 0
EOF
chmod 755 /data/adb/service.d/propspoof.sh
sh /data/adb/service.d/propspoof.sh
getprop ro.boot.verifiedbootstate
getprop ro.boot.flash.locked
getprop ro.boot.vbmeta.device_state
getprop ro.debuggable
echo '--- fim ---'
