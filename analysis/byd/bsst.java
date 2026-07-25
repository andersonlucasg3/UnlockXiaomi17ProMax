package defpackage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import com.google.android.gms.chimera.modules.dck.AppContextProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes6.dex */
public final class bsst {
    public static final bmnk a = bmnk.c("Dck", bmbu.fg, "WirelessCapabilitiesFeatures");

    public static int a() {
        hnsk hnskVar;
        long j;
        bmnk bmnkVar;
        byte[] bArr;
        bmnk bmnkVar2 = bsss.a;
        Context a2 = AppContextProvider.a();
        if (jybp.f()) {
            Bundle bundle = null;
            if (!jybp.a.b().k()) {
                ProviderInfo resolveContentProvider = a2.getPackageManager().resolveContentProvider("com.samsung.android.digitalkey.provider.migrationinfo", 0);
                if (resolveContentProvider == null) {
                    a.R(bsss.a.i(), "Could not get ProviderInfo when resolving the content provider authority.", (char) 2253);
                } else {
                    String str = resolveContentProvider.packageName;
                    try {
                        bArr = bmlv.k(a2, str, "SHA-256");
                    } catch (PackageManager.NameNotFoundException unused) {
                        bArr = null;
                    }
                    bsof a3 = bArr == null ? bsof.a(str, "") : bsof.a(str, bmoc.e(bArr));
                    jjtr jjtrVar = jybp.a.b().a().b;
                    HashSet hashSet = new HashSet();
                    Iterator it = jjtrVar.iterator();
                    while (it.hasNext()) {
                        List o = hnwa.e(':').o((String) it.next());
                        hashSet.add(bsof.a((String) o.get(0), (String) o.get(1)));
                    }
                    if (hashSet.contains(a3)) {
                        bsss.a.h().an(2252).y("Verified package %s as content provider app", resolveContentProvider.packageName);
                    } else {
                        bsss.a.i().an(2251).y("Failed to verify package %s as content provider app", resolveContentProvider.packageName);
                    }
                }
                a.R(bsss.a.h(), "Samsung Content Provider failed app verification", (char) 2248);
                hnskVar = hnsk.a;
            }
            ContentResolver contentResolver = a2.getContentResolver();
            if (contentResolver == null) {
                a.R(bsss.a.j(), "Could not get content resolver to call Samsung Content Provider.", (char) 2250);
            } else {
                try {
                    bundle = contentResolver.call(new Uri.Builder().authority("com.samsung.android.digitalkey.provider.migrationinfo").scheme("content").build(), jybp.a.b().d(), (String) null, (Bundle) null);
                } catch (IllegalArgumentException e) {
                    a.aB(bsss.a.j(), "Failed to call Samsung Content Provider due to IllegalArgumentException.", (char) 2249, e);
                }
            }
            if (bundle == null) {
                a.R(bsss.a.j(), "Could not query Samsung Migration Content Provider.", (char) 2247);
                hnskVar = hnsk.a;
            } else {
                bmnk bmnkVar3 = bsss.a;
                a.au(bmnkVar3.h(), "migrationBundle: %s", bundle, (char) 2244);
                if (bundle.containsKey("is_r3_supported")) {
                    boolean z = bundle.getBoolean("is_r3_supported");
                    bmnkVar3.h().an(2245).S("%s: %s", "is_r3_supported", z);
                    hnskVar = hnuu.m(Boolean.valueOf(z));
                } else {
                    a.au(bmnkVar3.j(), "Could not find %s key in Samsung Migration Content Provider.", "is_r3_supported", (char) 2246);
                    hnskVar = hnsk.a;
                }
            }
        } else {
            hnskVar = hnsk.a;
        }
        long j2 = SystemProperties.getInt("ro.gms.dck.eligible_wcc", 0);
        long a4 = jybt.a.b().a();
        long j3 = a4 != -1 ? a4 : j2;
        if (j2 == 0) {
            bmnkVar = a;
            j = 0;
            a.R(bmnkVar.h(), "wccSysProp: 0", (char) 2269);
        } else {
            j = 0;
            if (j2 == 1) {
                bmnkVar = a;
                a.R(bmnkVar.h(), "wccSysProp: 1", (char) 2268);
            } else if (j2 == 2) {
                bmnkVar = a;
                a.R(bmnkVar.h(), "wccSysProp: 2", (char) 2267);
            } else if (j2 == 3) {
                bmnkVar = a;
                a.R(bmnkVar.h(), "wccSysProp: 3", (char) 2266);
            } else {
                bmnkVar = a;
                a.R(bmnkVar.j(), "wccSysProp: unexpected value", (char) 2265);
            }
        }
        if (a4 == -1) {
            a.R(bmnkVar.h(), "wccOverride: not set", (char) 2264);
        } else if (a4 == j) {
            a.R(bmnkVar.h(), "wccOverride: 0", (char) 2263);
        } else if (a4 == 1) {
            a.R(bmnkVar.h(), "wccOverride: 1", (char) 2262);
        } else if (a4 == 2) {
            a.R(bmnkVar.h(), "wccOverride: 2", (char) 2261);
        } else if (a4 == 3) {
            a.R(bmnkVar.h(), "wccOverride: 3", (char) 2260);
        } else {
            a.R(bmnkVar.j(), "wccOverride: unexpected value", (char) 2259);
        }
        if (bssx.c()) {
            if (jybp.f()) {
                if (!hnskVar.i()) {
                    a.R(bmnkVar.h(), "Found Samsung native app without content provider. This indicates that the app is too old to support the signal. Override WCC to 1.", (char) 2257);
                } else if (((Boolean) hnskVar.d()).booleanValue()) {
                    a.R(bmnkVar.h(), "Found Samsung native app with content provider. Release 3 supported. Will not override WCC to 1.", (char) 2255);
                } else {
                    a.R(bmnkVar.h(), "Found Samsung native app with content provider. Release 3 not supported. Overriding WCC to 1.", (char) 2256);
                }
                j3 = 1;
            } else {
                a.R(bmnkVar.h(), "Found Samsung native app but will not use content provider since flag is disabled.", (char) 2258);
            }
        }
        a.au(bmnkVar.h(), "returned wcc: %s", new gswz(j3), (char) 2254);
        return (int) j3;
    }
}
