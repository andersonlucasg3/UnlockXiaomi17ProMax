const Java = require("frida-java-bridge").default;

Java.perform(function () {
    const Arrays = Java.use("java.util.Arrays");
    const loaders = Java.enumerateClassLoadersSync();
    loaders.forEach(function (loader, li) {
        if (li !== 7) return; // only BYD loader
        let elements;
        try {
            const casted = Java.cast(loader, Java.use("dalvik.system.BaseDexClassLoader"));
            const plCast = Java.cast(casted.pathList.value, Java.use("dalvik.system.DexPathList"));
            elements = plCast.dexElements.value;
        } catch (e) { return; }
        for (let i = 0; i < elements.length; i++) {
            let df;
            try {
                df = Java.cast(elements[i], Java.use("dalvik.system.DexPathList$Element")).dexFile.value;
            } catch (e) { continue; }
            if (df === null) continue;
            const cookie = Java.cast(df, Java.use("dalvik.system.DexFile")).mCookie.value;
            const s = Arrays.toString.overload("[J").call(Arrays, cookie);
            const nums = s.replace(/[\[\]\s]/g, "").split(",");
            for (let j = 0; j < nums.length && j < 4; j++) {
                if (!nums[j] || nums[j] === "0") continue;
                const u = BigInt(nums[j]) & 0xFFFFFFFFFFFFFFFFn;
                const addr = u & 0x00FFFFFFFFFFFFFFn;
                if (addr === 0n) continue;
                const p = ptr("0x" + addr.toString(16));
                try {
                    console.log("[c" + j + "] @0x" + addr.toString(16));
                    console.log(hexdump(p, { length: 96, ansi: false }));
                    // try to follow first pointer-looking qwords
                    for (let k = 0; k < 12; k++) {
                        try {
                            const q = p.add(k * 8).readU64();
                            const qu = q.and(uint64("0x00FFFFFFFFFFFFFF"));
                            if (qu.compare(uint64("0x10000")) > 0 && qu.compare(uint64("0x0000FFFFFFFFFFFF")) < 0) {
                                const bp = ptr("0x" + qu.toString(16));
                                let b0 = 0;
                                try { b0 = bp.readU8(); } catch (e) { continue; }
                                console.log("  qword[" + k + "]=0x" + q.toString(16) + " -> first byte 0x" + b0.toString(16));
                            }
                        } catch (e) { }
                    }
                } catch (e) { console.log("[c" + j + "] read err " + e); }
            }
        }
    });
    console.log("[*] done");
});
