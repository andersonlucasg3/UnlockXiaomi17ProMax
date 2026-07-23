#!/system/bin/sh
logcat -d '*:E' | grep -E 'byd|BYD|AutoLink|AndroidRuntime|FATAL' | tail -30
