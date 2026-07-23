#!/system/bin/sh

TARGET_DIR=/data/adb/tricky_store
SOURCE_FILE=${0%/*}/keybox.xml
TARGET_FILE=$TARGET_DIR/keybox.xml
LOGFILE=/data/adb/keybox_debug.log

mkdir -p "$TARGET_DIR"

if [ -f "$SOURCE_FILE" ]; then
    cp -f "$SOURCE_FILE" "$TARGET_FILE"
    chmod 0644 "$TARGET_FILE"
    chown 0:0 "$TARGET_FILE"
    echo "[keybox.sh] keybox.xml successfully placed in $TARGET_DIR" >> $LOGFILE
else
    echo "[keybox.sh] ERROR: keybox.xml not found at $SOURCE_FILE" >> $LOGFILE
fi
