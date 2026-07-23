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
  ksud resetprop "$key" "$val" 2>/dev/null || resetprop "$key" "$val" 2>/dev/null
done < "$LIST"
