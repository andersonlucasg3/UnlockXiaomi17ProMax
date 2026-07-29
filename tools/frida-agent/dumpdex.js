const Java = require("frida-java-bridge").default;

let dumped = 0;

function untag(q) {
    return q.and(uint64("0x00FFFFFFFFFFFFFF"));
}

function dumpDexAt(dexObjPtr, tag) {
    try {
        const beginQ = untag(dexObjPtr.add(8).readU64());
        const size = dexObjPtr.add(0x20).readU32();
        if (size < 0x70 || size > 100 * 1024 * 1024) { console.log("[" + tag + "] bad size " + size); return; }
        const begin = ptr("0x" + beginQ.toString(16));
        let fsize = -1;
        try { fsize = begin.add(0x20).readU32(); } catch (e) { console.log("[" + tag + "] unreadable begin " + begin); return; }
        if (fsize !== size) { console.log("[" + tag + "] fsize mismatch " + fsize + " != " + size); return; }
        const magic = new Uint8Array(begin.readByteArray(8));
        const magicStr = String.fromCharCode(magic[0], magic[1], magic[2], magic[3]);
        const bytes = begin.readByteArray(size);
        dumped++;
        console.log("[DEX#" + dumped + "] " + tag + " size=" + size + " magic=" + JSON.stringify(magicStr));
        send({ t: "dex", idx: dumped, size: size, tag: tag }, bytes);
    } catch (e) { console.log("[" + tag + "] err " + e); }
}

Java.perform(function () {
    const Arrays = Java.use("java.util.Arrays");
    const loaders = Java.enumerateClassLoadersSync();
    console.log("[*] loaders: " + loaders.length);
    loaders.forEach(function (loader, li) {
        let elements;
        try {
            const casted = Java.cast(loader, Java.use("dalvik.system.BaseDexClassLoader"));
            const plCast = Java.cast(casted.pathList.value, Java.use("dalvik.system.DexPathList"));
            elements = plCast.dexElements.value;
        } catch (e) { return; }
        for (let i = 0; i < elements.length; i++) {
            let df, path = "?";
            try {
                const el = Java.cast(elements[i], Java.use("dalvik.system.DexPathList$Element"));
                df = el.dexFile.value;
                try { path = String(el.path.value); } catch (e) { }
            } catch (e) { continue; }
            if (df === null) continue;
            const cookie = Java.cast(df, Java.use("dalvik.system.DexFile")).mCookie.value;
            let s;
            try { s = Arrays.toString.overload("[J").call(Arrays, cookie); }
            catch (e) { console.log("[L" + li + " el" + i + "] cookieErr " + e); continue; }
            console.log("[L" + li + " el" + i + "] " + path + " cookie=" + s);
            const nums = s.replace(/[\[\]\s]/g, "").split(",");
            for (let j = 0; j < nums.length; j++) {
                if (!nums[j] || nums[j] === "0") continue;
                try {
                    const u = BigInt(nums[j]) & 0xFFFFFFFFFFFFFFFFn;
                    const addr = u & 0x00FFFFFFFFFFFFFFn; // strip MTE tag
                    if (addr === 0n) continue;
                    dumpDexAt(ptr("0x" + addr.toString(16)), "L" + li + "e" + i + "c" + j);
                } catch (e) { console.log("[L" + li + "e" + i + "c" + j + "] parse " + e); }
            }
        }
    });
    console.log("[*] total dumped: " + dumped);
});
