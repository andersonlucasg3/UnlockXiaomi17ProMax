// gms-dck-unlock.js — Hook GMS DCK Stub eligibility to trigger full module download
// =============================================================================
// Target process: com.google.android.gms.persistent (Chimera DCK stub runs here)
// GMS version: 26.28.60 (262860035)
//
// How to load:
//   Same scheme as byd-dck-hook.js — MUST be concatenated with _agent.js
//   (which bundles the frida-java-bridge) and loaded via session.create_script().
//   frida-compile is broken on Windows, so raw JS without the bridge won't work.
//
//   Python example:
//     agent = open("_agent.js").read()
//     dck   = open("gms-dck-unlock.js").read()
//     script = session.create_script(agent + "\n" + dck)
//
// Planned execution sequence:
//   1. adb shell "su -c 'setprop ro.gms.dck.eligible_wcc 3'"   (wcc prop)
//   2. frida -U -f com.google.android.gms.persistent -l <compiled_script>
//      OR attach to running gms.persistent + force-stop gms to re-init stub
//   3. Watch logcat: adb logcat | grep -iE 'Dck|DCK|eligib|download'
//   4. Expected: "isDckModuleEligible: true", "downloadAllowed: true",
//      module download starts (Chimera downloads DCK full module)
//
// Architecture (from RE of GMS 26.28.60, see analysis/byd/*.java):
//   bsog.a()           — outer eligibility entry (public static boolean a())
//     bsog.b()         — core check: wcc>0 && downloadAllowed
//       bsst.a()       — reads wcc from sysprop + phenotype override
//       jycd.a.mc().g()— reads downloadAllowed flag (DckStub__full_module_download_allowed)
//   jycg.g()           — flag getter: downloadAllowed (boolean, default false)
//   jycg.d()           — flag getter: are_flags_synced (boolean, default false)
//   jycd.c()           — static: disable_dck_support (boolean, default false)
//
// Classes are in package "defpackage" (= no package in source, obfuscated names).
// They live in Chimera dexes (classes6.dex, classes15.dex) loaded by a container
// ClassLoader after GMS process start — need deferred/polling approach.
// =============================================================================

console.log("[GMS-DCK] === GMS DCK Stub Unlock ===");
console.log("[GMS-DCK] Target: com.google.android.gms (main process, NOT persistent)");

(function() {
    var hooksInstalled = false;
    var attempts = 0;
    var maxAttempts = 300; // 300 * 250ms = 75s

    // List of classes we need, with their expected methods
    var targetClasses = {
        "bsog": {
            methods: {
                "a": { sig: "()Z", desc: "outer eligibility (public static)" },
                "b": { sig: "()Z", desc: "core: wcc>0 && downloadAllowed (private static)" }
            }
        },
        "bsst": {
            methods: {
                "a": { sig: "()I", desc: "wcc reader (public static)" }
            }
        },
        "jycg": {
            methods: {
                "g": { sig: "()Z", desc: "DckStub__full_module_download_allowed" },
                "d": { sig: "()Z", desc: "DckStub__are_flags_synced" },
                "e": { sig: "()Z", desc: "DckStub__disable_dck_support" }
            }
        },
        "jycd": {
            methods: {
                "c": { sig: "()Z", desc: "disable_dck_support (static)" }
            }
        }
    };

    // Kill switch — set via adb shell "su -c 'setprop debug.gms.dck.unlock 0'"
    function isKillSwitchActive() {
        try {
            var SysProp = Java.use("android.os.SystemProperties");
            var val = SysProp.get("debug.gms.dck.unlock", "1");
            return val === "0";
        } catch(e) {
            return false;
        }
    }

    function installHooks() {
        if (hooksInstalled) return;
        attempts++;

        // Check kill switch
        if (isKillSwitchActive()) {
            if (attempts === 1) {
                console.log("[GMS-DCK] Kill switch active (debug.gms.dck.unlock=0) — ABORTING");
            }
            hooksInstalled = true; // don't retry
            return;
        }

        // Try to resolve all target classes
        var resolved = {};
        var allResolved = true;

        for (var className in targetClasses) {
            try {
                resolved[className] = Java.use(className);
            } catch(e) {
                allResolved = false;
                break;
            }
        }

        // If standard Java.use fails, try finding the Chimera DelegateLastClassLoader
        if (!allResolved) {
            var foundLoader = false;
            try {
                var loaders = Java.enumerateClassLoadersSync();
                for (var i = 0; i < loaders.length; i++) {
                    try {
                        var clz = loaders[i].loadClass("bsog");
                        if (clz) {
                            console.log("[GMS-DCK] Found bsog in ClassLoader[" + i + "], switching...");
                            Java.classFactory.loader = loaders[i];
                            foundLoader = true;
                            break;
                        }
                    } catch(e2) {}
                }
            } catch(e3) {
                console.log("[GMS-DCK] ClassLoader enumeration failed: " + e3);
            }

            if (foundLoader) {
                // Retry with the new classloader
                allResolved = true;
                for (var className in targetClasses) {
                    try {
                        resolved[className] = Java.use(className);
                    } catch(e) {
                        allResolved = false;
                        break;
                    }
                }
            }
        }

        if (!allResolved) {
            if (attempts <= 3 || attempts % 30 === 0) {
                console.log("[GMS-DCK] Classes not ready (attempt " + attempts + ")" +
                    " — waiting for Chimera container to load DCK dexes...");
            }
            if (attempts < maxAttempts) {
                setTimeout(installHooks, 250);
            } else {
                console.log("[GMS-DCK] FAILED after " + maxAttempts + " attempts. Classes not found:");
                for (var cn in targetClasses) {
                    if (!resolved[cn]) {
                        console.log("[GMS-DCK]   MISSING: " + cn);
                    }
                }
                console.log("[GMS-DCK] This may mean:");
                console.log("[GMS-DCK]   - Chimera container not loaded DCK dexes yet");
                console.log("[GMS-DCK]   - Class names differ from RE (GMS version mismatch?)");
                console.log("[GMS-DCK]   - DelegateLastClassLoader not accessible");
                console.log("[GMS-DCK] Try: setprop ro.gms.dck.eligible_wcc 3 && force-stop gms");
            }
            return;
        }

        console.log("[GMS-DCK] All DCK classes found (attempt " + attempts + ")! Installing hooks...");

        var bsogCls = resolved["bsog"];
        var bsstCls = resolved["bsst"];
        var jycgCls = resolved["jycg"];
        var jycdCls = resolved["jycd"];

        // ---- HELPER: safe hook with diagnostic logging ----
        function safeHook(className, methodName, overloadArg, implFn) {
            try {
                var clz = Java.use(className);
                var method;
                if (overloadArg) {
                    method = clz[methodName].overload(overloadArg);
                } else {
                    method = clz[methodName];
                }
                method.implementation = implFn;
                console.log("[GMS-DCK]   ✓ " + className + "." + methodName + " hooked");
                return true;
            } catch(e) {
                console.log("[GMS-DCK]   ✗ " + className + "." + methodName + " FAILED: " + e);
                // Try to enumerate available methods for diagnostics
                try {
                    var clz = Java.use(className);
                    var methods = clz.class.getDeclaredMethods();
                    console.log("[GMS-DCK]     Available methods on " + className + ":");
                    for (var i = 0; i < methods.length; i++) {
                        var m = methods[i];
                        var ret = m.getReturnType().getName();
                        var params = [];
                        var paramTypes = m.getParameterTypes();
                        for (var j = 0; j < paramTypes.length; j++) {
                            params.push(paramTypes[j].getName());
                        }
                        console.log("[GMS-DCK]       " + m.getName() + "(" + params.join(",") + ") -> " + ret);
                    }
                } catch(e2) {
                    console.log("[GMS-DCK]     Cannot enumerate: " + e2);
                }
                return false;
            }
        }

        var hookedCount = 0;

        // ================================================================
        // HOOK 1: bsog.a() — OUTER ELIGIBILITY (most important)
        // public static boolean a()
        // This bypasses ALL checks: China, Samsung, debug, car key condition
        // ================================================================
        if (safeHook("bsog", "a", null, function() {
            console.log("[GMS-DCK] ★★★ bsog.a() [outer eligibility] INTERCEPTED → returning TRUE ★★★");
            // Log stack trace once for diagnostics
            if (!this._dckLogged) {
                this._dckLogged = true;
                try {
                    var Log = Java.use("android.util.Log");
                    var stack = Log.getStackTraceString(Java.use("java.lang.Exception").$new());
                    console.log("[GMS-DCK]   Called from:\n" + stack.split('\n').slice(1, 8).join('\n'));
                } catch(e) {}
            }
            return true;
        })) {
            hookedCount++;
        }

        // ================================================================
        // HOOK 2: bsog.b() — CORE ELIGIBILITY (wcc>0 && downloadAllowed)
        // private static boolean b()
        // Redundancy: if a() is bypassed, this may still be called elsewhere
        // ================================================================
        if (safeHook("bsog", "b", null, function() {
            console.log("[GMS-DCK] ★★★ bsog.b() [core eligibility] INTERCEPTED → returning TRUE ★★★");
            return true;
        })) {
            hookedCount++;
        }

        // ================================================================
        // HOOK 3: bsst.a() — WCC READER
        // public static int a()
        // Returns wcc value (0=no support, 1=NFC, 2=NFC+BLE, 3=NFC+BLE+UWB)
        // Force return 3 for maximum support
        // ================================================================
        if (safeHook("bsst", "a", null, function() {
            var originalResult = this.a();
            console.log("[GMS-DCK] ★ bsst.a() [wcc reader] original=" + originalResult + " → forcing 3");
            return 3;
        })) {
            hookedCount++;
        }

        // ================================================================
        // HOOK 4: jycg.g() — DOWNLOAD ALLOWED FLAG
        // public final boolean g() — DckStub__full_module_download_allowed
        // This is the flag that controls whether the full DCK module downloads
        // ================================================================
        try {
            var jycgInstance = null;
            // jycg is NOT a singleton — it's instantiated inside jycd
            // But hooks on Java.use("jycg") apply class-wide to ALL instances
            // So hooking the class method directly should work
            safeHook("jycg", "g", null, function() {
                console.log("[GMS-DCK] ★★★ jycg.g() [downloadAllowed] INTERCEPTED → returning TRUE ★★★");
                return true;
            });
            hookedCount++;
        } catch(e) {
            console.log("[GMS-DCK] jycg.g() hook error: " + e);
        }

        // ================================================================
        // HOOK 5: jycg.d() — ARE FLAGS SYNCED
        // public final boolean d()
        // If false, might prevent download; force true
        // ================================================================
        try {
            safeHook("jycg", "d", null, function() {
                console.log("[GMS-DCK] ★ jycg.d() [are_flags_synced] → returning TRUE");
                return true;
            });
            hookedCount++;
        } catch(e) {
            console.log("[GMS-DCK] jycg.d() hook error: " + e);
        }

        // ================================================================
        // HOOK 6: jycg.e() — DISABLE DCK SUPPORT FLAG
        // public final boolean e()
        // ================================================================
        try {
            safeHook("jycg", "e", null, function() {
                console.log("[GMS-DCK] ★ jycg.e() [disable_dck_support] → returning FALSE");
                return false;
            });
            hookedCount++;
        } catch(e) {
            console.log("[GMS-DCK] jycg.e() hook error: " + e);
        }

        // ================================================================
        // HOOK 7: jycd.c() — DISABLE DCK SUPPORT (static convenience)
        // public static boolean c()
        // ================================================================
        if (safeHook("jycd", "c", null, function() {
            console.log("[GMS-DCK] ★ jycd.c() [disable_dck_support static] → returning FALSE");
            return false;
        })) {
            hookedCount++;
        }

        hooksInstalled = true;
        console.log("[GMS-DCK] ========================================");
        console.log("[GMS-DCK] === ALL HOOKS INSTALLED (" + hookedCount + "/7) ===");
        console.log("[GMS-DCK] === DCK module should become eligible ===");
        console.log("[GMS-DCK] === Watch logcat tag 'Dck' for:       ===");
        console.log("[GMS-DCK] ===   'isDckModuleEligible: true'     ===");
        console.log("[GMS-DCK] ===   'downloadAllowed: true'         ===");
        console.log("[GMS-DCK] ===   module download starting...     ===");
        console.log("[GMS-DCK] ========================================");

        // Log current state for diagnostics
        try {
            console.log("[GMS-DCK] Current DCK state snapshot:");
            var bsogCls = Java.use("bsog");
            var bsstCls = Java.use("bsst");
            console.log("[GMS-DCK]   bsog.a() = " + bsogCls.a());
            console.log("[GMS-DCK]   bsst.a() (wcc) = " + bsstCls.a());
            // Try to read the flag via jycd chain
            try {
                var jycdCls = Java.use("jycd");
                var flag = jycdCls.c();
                console.log("[GMS-DCK]   jycd.c() (disable) = " + flag);
            } catch(e) {}
        } catch(e) {
            console.log("[GMS-DCK]   State snapshot failed: " + e);
        }
    }

    // ---- RPC for manual verification ----
    rpc.exports = {
        status: function() {
            return JSON.stringify({
                hooksInstalled: hooksInstalled,
                attempts: attempts,
                processName: Java.use("android.os.Process").myPid()
            });
        },
        checkEligibility: function() {
            if (!hooksInstalled) return "Hooks not installed (attempt " + attempts + ")";
            try {
                var bsogCls = Java.use("bsog");
                var bsstCls = Java.use("bsst");
                return "bsog.a()=" + bsogCls.a() + " bsog.b()=" + bsogCls.b() + " bsst.a()=" + bsstCls.a();
            } catch(e) {
                return "Error: " + e;
            }
        },
        enumerateDcxClasses: function() {
            var found = [];
            Java.enumerateLoadedClassesSync().forEach(function(cn) {
                if (cn === "bsog" || cn === "bsst" || cn === "jycg" || cn === "jycd" ||
                    cn === "jyce" || cn === "jybt" || cn === "jybv" || cn === "jybl" ||
                    cn === "bsss" || cn === "bssw" || cn === "bsnv" || cn === "bsor" ||
                    cn === "bsoo" || cn === "bsnw") {
                    found.push(cn);
                }
            });
            return found.join(",");
        }
    };

    // ---- Start polling ----
    console.log("[GMS-DCK] Polling for DCK classes (Chimera container)...");
    console.log("[GMS-DCK] Kill switch: debug.gms.dck.unlock=0 to disable");
    installHooks();
})();
