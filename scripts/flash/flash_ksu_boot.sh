#!/system/bin/sh
echo '--- flash boot ---'
dd if=/data/local/tmp/new-boot.img of=/dev/block/by-name/boot_a bs=4096
dd if=/data/local/tmp/new-boot.img of=/dev/block/by-name/boot_b bs=4096
echo '--- verify boot ---'
sha256sum /dev/block/by-name/boot_a | head -c 64
echo ''
sha256sum /dev/block/by-name/boot_b | head -c 64
echo ''
echo '--- flash init_boot stock ---'
dd if=/sdcard/Download/init_boot.img of=/dev/block/by-name/init_boot_a bs=4096
dd if=/sdcard/Download/init_boot.img of=/dev/block/by-name/init_boot_b bs=4096
echo 'DONE'
