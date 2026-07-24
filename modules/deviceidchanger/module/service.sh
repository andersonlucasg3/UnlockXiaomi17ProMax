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

watched_hits=0
other_hits=0
while true; do
  fg=$(dumpsys window 2>/dev/null | grep -m1 mCurrentFocus | sed -nE 's|^.*u0 ([^/ ]+)/.*$|\1|p')
  if [ -z "$fg" ]; then
    # A parse failure must never count as "app left": treating it as one makes
    # adb_enabled flap 0->1->0 while the watched app is open, which banking
    # apps detect. Skip without touching either hysteresis counter.
    sleep 2
    continue
  fi
  if grep -qx "$fg" "$WLIST"; then
    other_hits=0
    watched_hits=$((watched_hits + 1))
    if [ "$watched_hits" -ge 2 ] && [ ! -f "$STATE" ]; then
      # Save current values so restore does not force-enable ADB the user
      # deliberately kept off.
      printf '%s %s\n' "$(settings get global adb_enabled)" "$(settings get global development_settings_enabled)" > "$STATE"
      settings put global adb_enabled 0
      settings put global development_settings_enabled 0
    fi
  else
    watched_hits=0
    other_hits=$((other_hits + 1))
    if [ "$other_hits" -ge 10 ] && [ -f "$STATE" ]; then
      read -r adb_prev dev_prev < "$STATE"
      settings put global adb_enabled "${adb_prev:-1}"
      settings put global development_settings_enabled "${dev_prev:-1}"
      rm -f "$STATE"
    fi
  fi
  sleep 2
done
