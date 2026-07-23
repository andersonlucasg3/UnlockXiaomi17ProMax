#!/system/bin/sh
# LKM install: boot-patch do init_boot stock com nosso .ko + prepara flash no slot _b
set -e
cd /data/local/tmp/ksu_build

echo "=== boot-patch init_boot (LKM, .ko backslashxx 32558) ==="
mkdir -p lkm_out
./ksud boot-patch -b init_boot.img -m android16-6.12_kernelsu.ko --partition init_boot -o lkm_out
ls -l lkm_out/

echo "=== restaura boot_b para stock (remove yapixel) ==="
dd if=boot.img of=/dev/block/by-name/boot_b bs=4m
sync

echo "=== flash init_boot-lkm -> init_boot_b ==="
OUTIMG=$(ls lkm_out/*.img | head -1)
echo "imagem: $OUTIMG"
dd if="$OUTIMG" of=/dev/block/by-name/init_boot_b bs=4m
sync

echo "=== verificacao hash ==="
head -c 100663296 /dev/block/by-name/boot_b | sha256sum
sha256sum boot.img
head -c 8388608 /dev/block/by-name/init_boot_b | sha256sum
sha256sum "$OUTIMG"

echo "=== LKM FLASH DONE ==="
