#!/system/bin/sh
# Configura hiding nativo do ZygiskNext com retry (binario flakky em shell context)
Z=/data/adb/modules/zygisksu/bin/zygiskd

try() {
  i=1
  while [ $i -le 6 ]; do
    "$@" && return 0
    sleep 1
    i=$((i+1))
  done
  echo "FALHOU: $*"
  return 1
}

echo "== denylist-policy whitelist =="
try "$Z" denylist-policy whitelist
echo "== enforce-denylist enabled =="
try "$Z" enforce-denylist enabled
echo "== status final =="
try "$Z" status
