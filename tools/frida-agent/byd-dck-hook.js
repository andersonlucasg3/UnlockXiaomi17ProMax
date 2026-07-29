// byd-dck-hook.js — Hook DigitalKeyFrameworkClient.isCreateDigitalKeyPossible()
// Target: com.byd.bydautolink. Append to _agent.js bridge.

console.log("[BYD-FINAL] BYD DCK Client Hook starting...");

(function() {
    var hooked = false;
    var attempts = 0;
    var implName = null;
    var DKF = null, Tasks = null, Bool = null, ctx = null;
    
    // Get Android context early (in default ClassLoader)
    try {
        var AT = Java.use("android.app.ActivityThread");
        ctx = AT.currentActivityThread().getApplication().getApplicationContext();
        console.log("[BYD-FINAL] Context: " + ctx.getClass().getName());
    } catch(e) {
        console.log("[BYD-FINAL] Context error: " + e);
    }
    
    function installHook() {
        if (hooked) return;
        attempts++;
        
        if (!ctx) {
            try {
                var AT = Java.use("android.app.ActivityThread");
                ctx = AT.currentActivityThread().getApplication().getApplicationContext();
            } catch(e) {}
            if (!ctx && attempts < 100) { setTimeout(installHook, 250); return; }
        }
        
        // Get DCK classes (usually in default PathClassLoader for BYD app)
        if (!DKF) {
            try {
                DKF = Java.use("com.google.android.gms.dck.DigitalKeyFramework");
                Tasks = Java.use("com.google.android.gms.tasks.Tasks");
                Bool = Java.use("java.lang.Boolean");
            } catch(e) {
                // Try classloader switching
                try {
                    var loaders = Java.enumerateClassLoadersSync();
                    for (var i = 0; i < loaders.length; i++) {
                        try {
                            if (loaders[i].loadClass("com.google.android.gms.dck.DigitalKeyFramework")) {
                                Java.classFactory.loader = loaders[i];
                                DKF = Java.use("com.google.android.gms.dck.DigitalKeyFramework");
                                Tasks = Java.use("com.google.android.gms.tasks.Tasks");
                                Bool = Java.use("java.lang.Boolean");
                                break;
                            }
                        } catch(e2) {}
                    }
                } catch(e3) {}
            }
        }
        
        if (!DKF || !Tasks) {
            if (attempts <= 3 || attempts % 20 === 0)
                console.log("[BYD-FINAL] Waiting for DCK classes (attempt " + attempts + ")");
            if (attempts < 300) { setTimeout(installHook, 250); }
            return;
        }
        
        console.log("[BYD-FINAL] DCK classes ready (attempt " + attempts + ")");
        
        // Get client and hook
        try {
            console.log("[BYD-FINAL] DKF type: " + typeof DKF);
            console.log("[BYD-FINAL] DKF.getClient type: " + typeof DKF.getClient);
            console.log("[BYD-FINAL] Calling DKF.getClient(ctx)...");
            var client = DKF.getClient(ctx);
            console.log("[BYD-FINAL] Client obtained: " + client);
            implName = client.getClass().getName();
            console.log("[BYD-FINAL] Client: " + implName);
            
            // Hook isCreateDigitalKeyPossible on the implementation
            var Impl = Java.use(implName);
            Impl.isCreateDigitalKeyPossible.implementation = function() {
                console.log("[BYD-FINAL] ★★★ isCreateDigitalKeyPossible() INTERCEPTED ★★★");
                console.log("[BYD-FINAL]   Returning Tasks.forResult(Boolean.TRUE)");
                return Tasks.forResult(Bool.TRUE);
            };
            console.log("[BYD-FINAL] ★ Hook installed on " + implName);
            
            // Verify
            var task = client.isCreateDigitalKeyPossible();
            var result = Tasks.await(task);
            console.log("[BYD-FINAL] Verify: " + result + " (" + result.getClass().getName() + ")");
            
            hooked = true;
        } catch(e) {
            console.log("[BYD-FINAL] Hook error: " + e);
            if (attempts < 300) { setTimeout(installHook, 500); }
            return;
        }
        
        // Also hook isDckFeatureAvailable
        try {
            DKF.isDckFeatureAvailable.implementation = function(c) {
                console.log("[BYD-FINAL] ★ isDckFeatureAvailable() -> true");
                return true;
            };
        } catch(e) {}
        
        console.log("[BYD-FINAL] === All hooks ready ===");
    }
    
    rpc.exports = {
        test: function() {
            if (!hooked) return "not hooked (attempt " + attempts + ")";
            try {
                var c = DKF.getClient(ctx);
                var t = c.isCreateDigitalKeyPossible();
                return "Result=" + Tasks.await(t) + " impl=" + implName;
            } catch(e) { return "Error=" + e; }
        },
        status: function() {
            return JSON.stringify({hooked: hooked, attempts: attempts, impl: implName||"null"});
        }
    };
    
    installHook();
})();
