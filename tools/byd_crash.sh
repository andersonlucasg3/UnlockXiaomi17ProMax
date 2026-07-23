#!/system/bin/sh
TOMB=$(ls -t /data/tombstones/tombstone_* 2>/dev/null | head -1)
echo "--- tombstone: $TOMB ---"
head -120 "$TOMB" 2>/dev/null || echo "SEM_TOMBSTONE"
echo "--- recent crash ---"
logcat -d | grep -A 40 'F libc.*SIGSEGV.*bydautolink' | tail -50
