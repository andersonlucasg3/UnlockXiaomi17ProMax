#!/system/bin/sh
echo '--- kernel ---'
uname -r
echo '--- ksu checks ---'
ls /sys/module/kernelsu 2>/dev/null || echo 'no /sys/module/kernelsu'
ls /data/adb/ksud 2>/dev/null || echo 'no ksud'
getprop ro.boot.kernelsu 2>/dev/null || echo 'no prop'
cat /proc/version | grep -i ksu || echo 'no ksu in proc/version'
echo '--- magisk vestigios ---'
ls /data/adb/magisk 2>/dev/null || echo 'no magisk dir'
echo '--- modules moribundas ---'
ls /data/adb/modules/ 2>/dev/null
