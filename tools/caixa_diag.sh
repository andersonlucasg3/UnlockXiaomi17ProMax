#!/system/bin/sh
# Captura evidencia do que a Caixa enxerga: maps, mountinfo e logcat por PID
OUT=/data/local/tmp/caixa_diag
mkdir -p $OUT
logcat -c
monkey -p br.gov.caixa.superapp -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
PID=""
i=0
while [ $i -lt 20 ]; do
  PID=$(pidof br.gov.caixa.superapp)
  [ -n "$PID" ] && break
  sleep 1
  i=$((i+1))
done
if [ -z "$PID" ]; then echo "NO_PID"; exit 1; fi
echo "PID=$PID"
cat /proc/$PID/maps > $OUT/maps.txt
cat /proc/$PID/mountinfo > $OUT/mountinfo.txt
logcat --pid=$PID -v threadtime > $OUT/logcat.txt 2>&1 &
LOGPID=$!
sleep 45
kill $LOGPID 2>/dev/null
pidof br.gov.caixa.superapp > $OUT/pid_after.txt
echo "DONE"
