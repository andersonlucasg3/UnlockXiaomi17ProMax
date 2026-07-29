const Java = require("frida-java-bridge").default;
Java.perform(function () {
    console.log("[*] Java.available=" + Java.available);
    const WV = Java.use("android.webkit.WebView");
    console.log("[*] WebView class OK, loadUrl overloads=" + WV.loadUrl.overloads.length);
    const Res = Java.use("android.content.res.Resources");
    console.log("[*] Resources getString overloads=" + Res.getString.overloads.length);
});
