#!/system/bin/sh
magisk --sqlite "INSERT OR REPLACE INTO settings (key,value) VALUES ('zygisk',1),('denylist',0);"
echo '--- settings ---'
magisk --sqlite "SELECT key,value FROM settings;"
