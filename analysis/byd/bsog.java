package defpackage;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import com.google.android.gms.chimera.modules.dck.AppContextProvider;
import j$.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes6.dex */
public final class bsog {
    public static final /* synthetic */ int a = 0;
    private static final bmnk b = bmnk.b("Dck", bmbu.fg);

    public static boolean a() {
        if (!jybp.d()) {
            if (d()) {
                a.R(b.j(), "Disable DCK module - Android debug build without fullaccess", (char) 2206);
                return false;
            }
            if (e()) {
                try {
                    AppContextProvider.a().getPackageManager().getPackageInfo("com.samsung.android.dkey", 0);
                } catch (PackageManager.NameNotFoundException e) {
                    a.at(b.h(), "Package is not installed on device: %s", "com.samsung.android.dkey", (char) 2205, e);
                    return false;
                }
            }
            bmnk bmnkVar = b;
            a.au(bmnkVar.h(), "disableDckSupport: %s", new gsxa(jycd.c()), (char) 2203);
            if (!jycd.c() || c()) {
                return b();
            }
            a.R(bmnkVar.h(), "DCK support overridden on device with no keys", (char) 2204);
            return false;
        }
        Context a2 = AppContextProvider.a();
        if (d()) {
            a.R(b.j(), "Disable DCK module - Android debug build without fullaccess", (char) 2200);
            return false;
        }
        if (bssw.a(a2)) {
            a.R(b.j(), "Disable DCK module - unsupported profile", (char) 2199);
            return false;
        }
        if (bmms.i(a2) || bmms.d(a2)) {
            a.R(b.j(), "Disable DCK module - China devices should not use Google DCK Framework", (char) 2194);
            return false;
        }
        if (e()) {
            try {
                a2.getPackageManager().getPackageInfo("com.samsung.android.dkey", 0);
            } catch (PackageManager.NameNotFoundException e2) {
                a.at(b.h(), "Disable DCK module - %s package is not installed on the Samsung device", "com.samsung.android.dkey", (char) 2198, e2);
                return false;
            }
        }
        bmnk bmnkVar2 = b;
        a.au(bmnkVar2.h(), "disableDckSupport: %s", Boolean.valueOf(jycd.c()), (char) 2195);
        if (!jycd.c() || c()) {
            return b();
        }
        a.R(bmnkVar2.h(), "Disable DCK module - DCK support overridden on device with no keys", (char) 2197);
        return false;
    }

    private static boolean b() {
        int a2 = bsst.a();
        boolean g = jycd.a.b().g();
        boolean z = a2 > 0;
        if (!z) {
            a.R(b.h(), "Dck module condition - hasWccSupport: false", (char) 2192);
        }
        if (!g) {
            a.R(b.h(), "Dck module condition - downloadAllowed: false", (char) 2191);
        }
        if (!z || !g) {
            return false;
        }
        a.R(b.h(), "Dck module condition - isDckModuleEligible: true", (char) 2190);
        return true;
    }

    private static boolean c() {
        new bsoo().b(AppContextProvider.a());
        try {
            bsnv bsnvVar = (bsnv) new bsor().b().u(3000L, TimeUnit.MILLISECONDS);
            if (bsnvVar != null) {
                b.h().an(2202).y("hasCarKeyConditionResult: %s", bsnvVar.name());
            }
            return !Objects.equals(bsnvVar, bsnv.HAS_CAR_KEY_FALSE);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            a.aB(b.j(), "Unable to read car key condition.", (char) 2201, e);
            return true;
        }
    }

    private static boolean d() {
        try {
            Class.forName("org.carconnectivity.android.digitalkey.secureelement.CccDkSe");
            return false;
        } catch (ClassNotFoundException unused) {
            return (Build.TYPE.equals("userdebug") || Build.TYPE.equals("eng")) && !SystemProperties.get("persist.service.seek", SystemProperties.get("service.seek", "")).contains("fullaccess");
        }
    }

    private static boolean e() {
        return jybp.c().equals("com.samsung.android.dkey");
    }
}
