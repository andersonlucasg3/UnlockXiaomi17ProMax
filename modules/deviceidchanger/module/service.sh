#!/system/bin/sh
# Late-boot tasks: build-prop spoof.
# Reads flat files only (no jq). The WebUI mirrors config.json into them.
MODDIR=${0%/*}
PEN="$MODDIR/.props_enabled"
PLIST="$MODDIR/.props_spoof"

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

exit 0
