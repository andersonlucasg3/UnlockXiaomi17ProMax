#!/system/bin/sh
# Foreground-app watcher: disables ADB/dev options while a watched app is in
# foreground, restores them when it leaves. Reads flat files only (no jq).
MODDIR=${0%/*}
EN="$MODDIR/.watcher_enabled"
LIST="$MODDIR/.watcher_packages"
STATE="$MODDIR/.adb_state"

[ -f "$EN" ] && grep -q 1 "$EN" || exit 0
[ -s "$LIST" ] || exit 0

# Wait for boot to finish so we do not fight system_server during startup.
while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 5; done
sleep 5

while true; do
  fg=$(dumpsys activity activities 2>/dev/null | grep -E 'topResumedActivity|mResumedActivity' | head -n 1 | sed -E 's|^.*[[:space:]]([A-Za-z0-9_.]+)/.*$|\1|')
  if [ -n "$fg" ] && grep -qx "$fg" "$LIST"; then
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
