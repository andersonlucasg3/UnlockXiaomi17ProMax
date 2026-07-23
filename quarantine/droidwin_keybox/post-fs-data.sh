#!/system/bin/sh
MODDIR=${0%/*}

# Debug log
LOGFILE=/data/adb/keybox_debug.log
echo "[post-fs-data] Running at boot..." >> $LOGFILE

# Call keybox.sh to place keybox.xml
sh $MODDIR/keybox.sh >> $LOGFILE 2>&1
