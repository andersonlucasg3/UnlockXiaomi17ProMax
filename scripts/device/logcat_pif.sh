#!/system/bin/sh
logcat -d | grep -i -E 'PIF|playintegrity|DroidGuard' | tail -30
