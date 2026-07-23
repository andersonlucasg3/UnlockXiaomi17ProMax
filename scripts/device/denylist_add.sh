#!/system/bin/sh
magisk --sqlite "INSERT OR REPLACE INTO settings (key,value) VALUES ('denylist',1);"
magisk --sqlite "INSERT OR REPLACE INTO denylist (package_name, process) VALUES ('com.revolut.revolut','com.revolut.revolut');"
magisk --sqlite "INSERT OR REPLACE INTO denylist (package_name, process) VALUES ('com.google.android.apps.walletnfcrel','com.google.android.apps.walletnfcrel');"
pm clear com.google.android.gms
pm clear com.android.vending
echo '--- settings ---'
magisk --sqlite "SELECT key,value FROM settings;"
echo '--- denylist ---'
magisk --sqlite "SELECT package_name,process FROM denylist;"
