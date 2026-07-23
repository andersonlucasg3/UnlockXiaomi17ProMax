#!/system/bin/sh
# Applies persisted build-prop spoofs as early as possible (post-fs-data).
# The WebUI mirrors config.json into flat files because busybox may lack jq.
MODDIR=${0%/*}
EN="$MODDIR/.props_enabled"
LIST="$MODDIR/.props_spoof"

[ -f "$EN" ] && grep -q 1 "$EN" || exit 0
[ -s "$LIST" ] || exit 0

while IFS= read -r line; do
  case "$line" in
    *=*) ;;
    *) continue ;;
  esac
  key=${line%%=*}
  val=${line#*=}
  [ -n "$key" ] || continue
  # Standalone resetprop only: calling "ksud resetprop" from a ksud-run boot
  # stage re-enters ksud and hangs the stage (bootloop in the field).
  resetprop "$key" "$val"
done < "$LIST"
