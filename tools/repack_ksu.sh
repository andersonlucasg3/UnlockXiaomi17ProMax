#!/system/bin/sh
# K1: repack boot com kernel yapixel | K2: backup quente das particoes ativas
set -e
cd /data/local/tmp/ksu_build
chmod 755 magiskboot

echo "=== unpack boot stock ==="
./magiskboot unpack boot.img
ls -l

echo "=== trocando kernel pelo yapixel ==="
cp Image kernel

echo "=== repack ==="
./magiskboot repack boot.img boot-yapixel.img
ls -l boot-yapixel.img

echo "=== backup quente (slot ativo _a) ==="
dd if=/dev/block/by-name/boot_a of=boot_a_backup.img bs=4m
dd if=/dev/block/by-name/init_boot_a of=init_boot_a_backup.img bs=4m
ls -l boot_a_backup.img init_boot_a_backup.img

echo "=== sha256 para conferencia ==="
sha256sum boot-yapixel.img boot_a_backup.img init_boot_a_backup.img
echo "=== K1+K2 DONE ==="
