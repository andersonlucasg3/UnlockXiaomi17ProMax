# Standard KSU/Magisk install script (donor zip ships none; defaults plus exec perms).
SKIPUNZIP=0

ui_print "- DeviceID+ (fork sidex15)"

set_perm_recursive "$MODPATH/webroot" 0 0 0755 0644
set_perm "$MODPATH/post-fs-data.sh" 0 0 0755
set_perm "$MODPATH/service.sh" 0 0 0755
set_perm "$MODPATH/config.json" 0 0 0644
