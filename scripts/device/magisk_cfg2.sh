#!/system/bin/sh
magisk --sqlite "INSERT OR REPLACE INTO settings (key,value) VALUES ('denylist',0);"
echo '--- settings ---'
magisk --sqlite "SELECT key,value FROM settings;"
