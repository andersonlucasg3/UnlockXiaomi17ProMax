// byd-antitamper.js — Frida Interceptor to detect and block native app kill
console.log("[NATIVE] Starting...");

// Hook _exit
var _exit_addr = Module.getGlobalExportByName("_exit");
if (_exit_addr) {
    Interceptor.attach(_exit_addr, {
        onEnter: function(args) {
            console.log("[NATIVE] ★★★ _exit(" + args[0].toInt32() + ") called ★★★");
            try {
                var bt = Thread.backtrace(this.context, Backtracer.ACCURATE);
                console.log("[NATIVE] BT: " + bt.map(DebugSymbol.fromAddress).join(" | "));
            } catch(e) {}
        }
    });
    console.log("[NATIVE] _exit hooked");
}

// Hook abort
var abort_addr = Module.getGlobalExportByName("abort");
if (abort_addr) {
    Interceptor.attach(abort_addr, {
        onEnter: function(args) {
            console.log("[NATIVE] ★★★ abort() called ★★★");
            try {
                var bt = Thread.backtrace(this.context, Backtracer.ACCURATE);
                console.log("[NATIVE] BT: " + bt.map(DebugSymbol.fromAddress).join(" | "));
            } catch(e) {}
        }
    });
    console.log("[NATIVE] abort hooked");
}

// Hook raise
var raise_addr = Module.getGlobalExportByName("raise");
if (raise_addr) {
    Interceptor.attach(raise_addr, {
        onEnter: function(args) {
            var sig = args[0].toInt32();
            console.log("[NATIVE] raise(sig=" + sig + ")");
            if (sig === 6 || sig === 9) {
                console.log("[NATIVE] ★★★ raise fatal signal BLOCKED ★★★");
                args[0] = ptr(0);
            }
        }
    });
    console.log("[NATIVE] raise hooked");
}

// Hook kill — block fatal signals to self
var kill_addr = Module.getGlobalExportByName("kill");
if (kill_addr) {
    Interceptor.attach(kill_addr, {
        onEnter: function(args) {
            var target = args[0].toInt32();
            var sig = args[1].toInt32();
            console.log("[NATIVE] kill(pid=" + target + ", sig=" + sig + ")");
            if (target === Process.id || target === 0) {
                if (sig === 6 || sig === 9 || sig === 15) {
                    console.log("[NATIVE] ★★★ kill(self, " + sig + ") BLOCKED ★★★");
                    args[1] = ptr(0);
                }
            }
        }
    });
    console.log("[NATIVE] kill hooked");
}

// Hook tgkill
var tgkill_addr = Module.getGlobalExportByName("tgkill");
if (tgkill_addr) {
    Interceptor.attach(tgkill_addr, {
        onEnter: function(args) {
            var sig = args[2].toInt32();
            if (sig === 6 || sig === 9 || sig === 15) {
                console.log("[NATIVE] tgkill(sig=" + sig + ") BLOCKED");
                args[2] = ptr(0);
            }
        }
    });
    console.log("[NATIVE] tgkill hooked");
}

// Hook exit_group (may not exist as global export)
try {
    var exit_group_addr = Module.getGlobalExportByName("exit_group");
    if (exit_group_addr) {
        Interceptor.attach(exit_group_addr, {
            onEnter: function(args) {
                console.log("[NATIVE] ★★★ exit_group(" + args[0].toInt32() + ") ★★★");
            }
        });
        console.log("[NATIVE] exit_group hooked");
    }
} catch(e) { console.log("[NATIVE] exit_group: " + e.message.split('\n')[0]); }

// Hook _Exit (C++ standard)
var _Exit_addr = Module.getGlobalExportByName("_Exit");
if (_Exit_addr) {
    Interceptor.attach(_Exit_addr, {
        onEnter: function(args) {
            console.log("[NATIVE] ★★★ _Exit(" + args[0].toInt32() + ") BLOCKED ★★★");
        }
    });
    console.log("[NATIVE] _Exit hooked");
}

// Hook pthread_exit
var pthread_exit_addr = Module.getGlobalExportByName("pthread_exit");
if (pthread_exit_addr) {
    Interceptor.attach(pthread_exit_addr, {
        onEnter: function(args) {
            console.log("[NATIVE] ★★★ pthread_exit() BLOCKED ★★★");
        }
    });
    console.log("[NATIVE] pthread_exit hooked");
}

// Catch signals via Process.setExceptionHandler
Process.setExceptionHandler(function(details) {
    console.log("[NATIVE] ★★★ Process exception: type=" + details.type + " addr=" + details.address + " ★★★");
    if (details.type === 'system') {
        console.log("[NATIVE]   System error");
    }
    return false; // Don't handle, let it crash but log
});
console.log("[NATIVE] Exception handler set");
var open_addr = Module.getGlobalExportByName("open");
if (open_addr) {
    var openCount = 0;
    Interceptor.attach(open_addr, {
        onEnter: function(args) {
            try {
                var path = args[0].readCString();
                if (path && path.indexOf("/proc/self/") !== -1 && openCount < 15) {
                    console.log("[NATIVE] open('" + path + "')");
                    openCount++;
                }
            } catch(e) {}
        }
    });
    console.log("[NATIVE] open hooked");
}

console.log("[NATIVE] All hooks installed");
setInterval(function() { console.log("[NATIVE] Alive"); }, 5000);
