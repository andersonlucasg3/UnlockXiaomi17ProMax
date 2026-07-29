import Java from "frida-java-bridge";

// BYD AutoLink — runtime tracing for digital-key compatibility check
const TARGET_ID = 0x7f1206c9; // string_nfc_your_device_does_not_supports_adding

function jstack(): string {
    return Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Exception").$new());
}

Java.perform(function () {
    // 1) Catch usage of the "not compatible" string resource
    const Resources = Java.use("android.content.res.Resources");
    (["getString", "getText"] as const).forEach(function (m) {
        (Resources as any)[m].overloads.forEach(function (ov: any) {
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

    // 2) WebView loads
    const WebView = Java.use("android.webkit.WebView");
    (WebView.loadUrl as any).overloads.forEach(function (ov: any) {
        ov.implementation = function () {
            console.log("\n[WV.loadUrl] " + arguments[0]);
            if (arguments.length > 1 && arguments[1]) console.log("  headers: " + arguments[1]);
            return ov.apply(this, arguments);
        };
    });
    (WebView.postUrl as any).implementation = function (url: string, data: any) {
        console.log("\n[WV.postUrl] " + url + " data=" + (data ? data.length + "b" : "null"));
        return this.postUrl(url, data);
    };
    (WebView.loadDataWithBaseURL as any).implementation = function (base: string, data: string, mime: any, enc: any, hist: any) {
        const s = data ? (data.length > 300 ? data.substring(0, 300) + "..." : data) : "null";
        console.log("\n[WV.loadDataWithBaseURL] base=" + base + " data=" + s);
        return this.loadDataWithBaseURL(base, data, mime, enc, hist);
    };
    (WebView.evaluateJavascript as any).implementation = function (js: string, cb: any) {
        const s = js.length > 400 ? js.substring(0, 400) + "..." : js;
        console.log("[WV.evalJS] " + s);
        return this.evaluateJavascript(js, cb);
    };
    (WebView.addJavascriptInterface as any).implementation = function (obj: any, name: string) {
        console.log("\n[WV.addJsInterface] name=" + name + " class=" + obj.getClass().getName());
        return this.addJavascriptInterface(obj, name);
    };

    // 3) Activity navigation (intent extras reveal URLs passed to DefaultWebViewActivity)
    const Activity = Java.use("android.app.Activity");
    function dumpIntent(intent: any, tag: string) {
        try {
            console.log("\n[" + tag + "] -> " + intent.getComponent());
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
    (Activity.startActivity as any).overloads.forEach(function (ov: any) {
        ov.implementation = function () {
            dumpIntent(arguments[0], "startActivity");
            return ov.apply(this, arguments);
        };
    });
    (Activity.startActivityForResult as any).overloads.forEach(function (ov: any) {
        ov.implementation = function () {
            dumpIntent(arguments[0], "startActivityForResult");
            return ov.apply(this, arguments);
        };
    });

    console.log("[*] hooks installed");
});
