#!/bin/sh
# Builds the DeviceID+ zygisk library with the Termux clang toolchain.
#   ./build.sh        -> ../module/zygisk/arm64-v8a.so
#   ./build.sh test   -> also builds ./test_hook (on-device smoke test)
#
# The module must link ONLY against Android system libs (libc/libm/libdl/
# liblog): -nostdlib++ keeps Termux's libc++_shared out, and no STL is used
# anywhere. Verified with readelf after every build.
set -e
cd "$(dirname "$0")"

CXXFLAGS="-O2 -fno-exceptions -fno-rtti -fvisibility=hidden -std=c++17 -Wall -Wextra"
LIBS="-llog -ldl -lm -lc"
OUT=../module/zygisk/arm64-v8a.so

mkdir -p ../module/zygisk
# shellcheck disable=SC2086
clang++ -shared -fPIC $CXXFLAGS -nostdlib++ \
    -o "$OUT" deviceid_zygisk.cpp perapp_hooks.cpp \
    $LIBS -Wl,-s

echo "== DT_NEEDED of $OUT =="
readelf -d "$OUT" | grep NEEDED
if readelf -d "$OUT" | grep NEEDED | grep -Eq 'c\+\+|android-support|com\.termux'; then
    echo "ERROR: non-system library linked into the module" >&2
    exit 1
fi
echo "== exported zygisk entry symbols =="
readelf -sW "$OUT" | grep -E 'zygisk_module_entry|zygisk_companion_entry' || true

if [ "$1" = "test" ]; then
    # shellcheck disable=SC2086
    clang++ $CXXFLAGS -nostdlib++ \
        -o test_hook test_hook.cpp perapp_hooks.cpp \
        $LIBS -Wl,-s
    echo "== DT_NEEDED of test_hook =="
    readelf -d test_hook | grep NEEDED
fi
