const Java = require("frida-java-bridge").default;

// BYD AutoLink — runtime tracing for digital-key compatibility check
const TARGET_ID = 0x7f1206c9; // string_nfc_your_device_does_not_supports_adding

function jstack() {
    return Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Exception").$new());
}

function safe(name, fn) {
    try { fn(); console.log("[+] hooked: " + name); }
    catch (e) { console.log("[-] FAIL " + name + ": " + e); }
}

Java.perform(function () {
    safe("Resources.getString/getText", function () {
        const Resources = Java.use("android.content.res.Resources");
        ["getString", "getText"].forEach(function (m) {
            Resources[m].overloads.forEach(function (ov) {
                ov.implementation = function () {
                    const id = arguments[0];
                    const ret = ov.apply(this, arguments);
                    if (id === TARGET_ID) {
                        console.log("\n[!!!] " + m + "(0x" + id.toString(16) + ") => " + ret);
                        console.log(jstack());
                    }
                    return ret;
                };
            });
        });
    });

    safe("WebView.loadUrl", function () {
        const WebView = Java.use("android.webkit.WebView");
        WebView.loadUrl.overloads.forEach(function (ov) {
            ov.implementation = function () {
                console.log("\n[WV.loadUrl] " + arguments[0]);
                if (arguments.length > 1 && arguments[1]) console.log("  headers: " + arguments[1]);
                return ov.apply(this, arguments);
            };
        });
    });

    safe("WebView.postUrl", function () {
        const WebView = Java.use("android.webkit.WebView");
        WebView.postUrl.implementation = function (url, data) {
            console.log("\n[WV.postUrl] " + url + " data=" + (data ? data.length + "b" : "null"));
            return this.postUrl(url, data);
        };
    });

    safe("WebView.loadDataWithBaseURL", function () {
        const WebView = Java.use("android.webkit.WebView");
        WebView.loadDataWithBaseURL.implementation = function (base, data, mime, enc, hist) {
            const s = data ? (data.length > 300 ? data.substring(0, 300) + "..." : data) : "null";
            console.log("\n[WV.loadDataWithBaseURL] base=" + base + " data=" + s);
            return this.loadDataWithBaseURL(base, data, mime, enc, hist);
        };
    });

    safe("WebView.evaluateJavascript", function () {
        const WebView = Java.use("android.webkit.WebView");
        WebView.evaluateJavascript.implementation = function (js, cb) {
            const s = js.length > 400 ? js.substring(0, 400) + "..." : js;
            console.log("[WV.evalJS] " + s);
            return this.evaluateJavascript(js, cb);
        };
    });

    safe("WebView.addJavascriptInterface", function () {
        const WebView = Java.use("android.webkit.WebView");
        WebView.addJavascriptInterface.implementation = function (obj, name) {
            console.log("\n[WV.addJsInterface] name=" + name + " class=" + obj.getClass().getName());
            return this.addJavascriptInterface(obj, name);
        };
    });

    function dumpIntent(intent, tag) {
        try {
            let comp = intent.getComponent();
            console.log("\n[" + tag + "] -> " + (comp ? comp.toString() : intent.getAction()));
            const extras = intent.getExtras();
            if (extras) {
                const it = extras.keySet().iterator();
                while (it.hasNext()) {
                    const k = it.next();
                    const v = extras.get(k);
                    let vs = v === null ? "null" : v.toString();
                    if (vs.length > 400) vs = vs.substring(0, 400) + "...";
                    console.log("   extra[" + k + "] = " + vs);
                }
            }
            const data = intent.getData();
            if (data) console.log("   data = " + data.toString());
        } catch (e) { console.log("dumpIntent err: " + e); }
    }

    safe("Activity.startActivity", function () {
        const Activity = Java.use("android.app.Activity");
        Activity.startActivity.overloads.forEach(function (ov) {
            ov.implementation = function () {
                dumpIntent(arguments[0], "startActivity");
                return ov.apply(this, arguments);
            };
        });
    });

    safe("Activity.startActivityForResult", function () {
        const Activity = Java.use("android.app.Activity");
        Activity.startActivityForResult.overloads.forEach(function (ov) {
            ov.implementation = function () {
                dumpIntent(arguments[0], "startActivityForResult");
                return ov.apply(this, arguments);
            };
        });
    });

    safe("Instrumentation.execStartActivity", function () {
        const Instr = Java.use("android.app.Instrumentation");
        Instr.execStartActivity.overloads.forEach(function (ov) {
            ov.implementation = function () {
                // args: who, contextThread, token, target, intent, ...
                for (let i = 0; i < arguments.length; i++) {
                    const a = arguments[i];
                    if (a !== null && a !== undefined && a.$className === "android.content.Intent") {
                        dumpIntent(a, "execStartActivity[" + i + "]");
                    }
                }
                return ov.apply(this, arguments);
            };
        });
    });

    console.log("[*] hooks installed");
});
