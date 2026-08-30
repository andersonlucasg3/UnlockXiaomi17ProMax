#!/system/bin/sh
# PIF Auto-Renew: renova custom.pif.prop quando faltam <= 7 dias.

MODDIR=/data/adb/modules/pif-autorenew
PIF_DIR=/data/adb/modules/playintegrityfix
PIF_PROP=$PIF_DIR/custom.pif.prop
AUTOPIF=$PIF_DIR/autopif4.sh
LOG=$MODDIR/autorenew.log

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" >> "$LOG"
}

truncate_log() {
    if [ -f "$LOG" ]; then
        lines=$(wc -l < "$LOG" | tr -d ' ')
        if [ "$lines" -gt 100 ] 2>/dev/null; then
            tail -n 100 "$LOG" > "$LOG.tmp"
            mv "$LOG.tmp" "$LOG"
        fi
    fi
}

# Converte YYYY-MM-DD para epoch, com fallback manual puro sh.
date_to_epoch() {
    dt=$1
    ep=$(date -d "$dt" +%s 2>/dev/null)
    if [ -n "$ep" ]; then
        echo "$ep"
        return
    fi

    y=${dt%%-*}
    rest=${dt#*-}
    m=${rest%%-*}
    d=${rest##*-}

    a=$(( (14 - m) / 12 ))
    yy=$(( y + 4800 - a ))
    mm=$(( m + 12*a - 3 ))
    jd=$(( d + (153*mm + 2)/5 + 365*yy + yy/4 - yy/100 + yy/400 - 32045 ))
    echo $(( (jd - 2440588) * 86400 ))
}

truncate_log
log "Iniciando verificacao PIF"

if [ ! -f "$PIF_PROP" ]; then
    log "ERRO: $PIF_PROP nao encontrado. Abortando."
    exit 1
fi

# Aguarda conectividade (24 x 10s = ate 240s)
i=0
while [ "$i" -lt 24 ]; do
    if ping -c 1 -W 3 connectivitycheck.gstatic.com >/dev/null 2>&1 || \
       curl -I --connect-timeout 3 https://connectivitycheck.gstatic.com >/dev/null 2>&1; then
        log "Rede OK"
        break
    fi
    i=$((i + 1))
    sleep 10
done

if [ "$i" -eq 24 ]; then
    log "ERRO: rede nao disponivel apos 240s. Abortando."
    exit 1
fi

# Extrai data de expiracao
expiry=$(grep -i '^# Estimated Expiry:' "$PIF_PROP" 2>/dev/null | head -n 1 | sed 's/.*: *//;s/ *$//')

if [ -z "$expiry" ]; then
    log "Aviso: linha Estimated Expiry nao encontrada. Renovando."
else
    exp_epoch=$(date_to_epoch "$expiry")
    today_epoch=$(date_to_epoch "$(date +%Y-%m-%d)")
    days_left=$(( (exp_epoch - today_epoch) / 86400 ))
    log "Expiracao: $expiry (faltam $days_left dias)"
    if [ "$days_left" -gt 7 ]; then
        log "Nao renovado: ainda faltam mais de 7 dias."
        exit 0
    fi
fi

if [ ! -f "$AUTOPIF" ]; then
    log "ERRO: $AUTOPIF nao encontrado. Abortando."
    exit 1
fi

log "Executando $AUTOPIF -m"
if sh "$AUTOPIF" -m >> "$LOG" 2>&1; then
    log "Renovacao concluida com sucesso."
else
    log "ERRO: renovacao falhou (codigo $?)."
fi
