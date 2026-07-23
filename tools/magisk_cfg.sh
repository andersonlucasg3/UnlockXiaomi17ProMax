#!/system/bin/sh
magisk --sqlite "INSERT OR REPLACE INTO settings (key,value) VALUES ('zygisk',1);"
echo "--- settings ---"
magisk --sqlite "SELECT key,value FROM settings;"
