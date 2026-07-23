#!/system/bin/sh
echo '--- shamiko module ---'
ls /data/adb/modules/zygisk_shamiko/
echo '--- revolut processes ---'
dumpsys package com.revolut.revolut | grep -E 'process=' | head -10
echo '--- shamiko log ---'
logcat -d | grep -i shamiko | tail -5
