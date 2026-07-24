#!/system/bin/sh
# No-op. The build-prop spoof moved to service.sh (runs after boot_completed):
# calling resetprop at the real post-fs-data stage bootloops this device.
# File kept so customize.sh's set_perm stays valid.
exit 0
