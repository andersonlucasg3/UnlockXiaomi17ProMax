#!/system/bin/sh
# Late-boot tasks: build-prop spoof + foreground-app ADB/Dev watcher.
# Reads flat files only (no jq). The WebUI mirrors config.json into them.
MODDIR=${0%/*}
PEN="$MODDIR/.props_enabled"
PLIST="$MODDIR/.props_spoof"
WEN="$MODDIR/.watcher_enabled"
WLIST="$MODDIR/.watcher_packages"
STATE="$MODDIR/.adb_state"

# Wait for boot_completed FIRST: running resetprop at the real post-fs-data
# stage bootloops this device, while boot_completed+5s was proven safe live.
while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 5; done
sleep 5

# --- build-prop spoof (moved here from post-fs-data.sh) ---
if [ -f "$PEN" ] && grep -q 1 "$PEN" && [ -s "$PLIST" ]; then
  while IFS= read -r line; do
    case "$line" in
      *=*) ;;
      *) continue ;;
    esac
    key=${line%%=*}
    val=${line#*=}
    [ -n "$key" ] || continue
    resetprop "$key" "$val"
  done < "$PLIST"
fi

# --- foreground-app watcher: disables ADB/dev options while a watched app is
# in foreground, restores them when it leaves ---
[ -f "$WEN" ] && grep -q 1 "$WEN" || exit 0
[ -s "$WLIST" ] || exit 0

while true; do
  fg=$(dumpsys activity activities 2>/dev/null | grep -E 'topResumedActivity|mResumedActivity' | head -n 1 | sed -E 's|^.*[[:space:]]([A-Za-z0-9_.]+)/.*$|\1|')
  if [ -n "$fg" ] && grep -qx "$fg" "$WLIST"; then
    if [ ! -f "$STATE" ]; then
      # Save current values so restore does not force-enable ADB the user
      # deliberately kept off.
      printf '%s %s\n' "$(settings get global adb_enabled)" "$(settings get global development_settings_enabled)" > "$STATE"
      settings put global adb_enabled 0
      settings put global development_settings_enabled 0
    fi
  elif [ -f "$STATE" ]; then
    read -r adb_prev dev_prev < "$STATE"
    settings put global adb_enabled "${adb_prev:-1}"
    settings put global development_settings_enabled "${dev_prev:-1}"
    rm -f "$STATE"
  fi
  sleep 2
done
