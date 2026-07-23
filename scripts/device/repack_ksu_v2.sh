#!/system/bin/sh
# Repack v2 com magiskboot oficial do Magisk 30.7
set -e
cd /data/local/tmp/ksu_build
MB=/data/local/tmp/magiskboot_oficial

echo "=== v2: unpack boot stock ==="
rm -f kernel ramdisk.cpio dtb second
$MB unpack boot.img
ls -l kernel

echo "=== v2: troca kernel + repack ==="
cp Image kernel
$MB repack boot.img boot-yapixel-v2.img
ls -l boot-yapixel-v2.img

echo "=== v2: verificacao (unpack do repack) ==="
rm -rf v2check
mkdir -p v2check
cd v2check
cp ../boot-yapixel-v2.img .
$MB unpack boot-yapixel-v2.img
echo "kernel extraido vs Image original:"
sha256sum kernel ../Image
cd ..

echo "=== v2 DONE ==="
