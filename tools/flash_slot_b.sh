#!/system/bin/sh
# K3: flash no slot INATIVO (_b) + verificacao pos-dd
set -e
cd /data/local/tmp/ksu_build

echo "=== flash boot_b (yapixel) ==="
dd if=boot-yapixel.img of=/dev/block/by-name/boot_b bs=4m
echo "=== flash init_boot_b (stock ROM) ==="
dd if=init_boot.img of=/dev/block/by-name/init_boot_b bs=4m
sync

echo "=== verify boot_b ==="
head -c 100663296 /dev/block/by-name/boot_b | sha256sum
echo "esperado: 0747e1706b3dcfd204d83aa650a367f270d989099888f9b19a7bb48ee9630513"

echo "=== verify init_boot_b ==="
head -c 8388608 /dev/block/by-name/init_boot_b | sha256sum
sha256sum init_boot.img

echo "=== K3 DONE ==="
