package defpackage;

/* compiled from: EnvironmentUtil.java */
/* loaded from: classes4.dex */
public class tp2 {
    public static int a = 0;
    public static boolean b = false;
    public static boolean c = false;
    public static boolean d = false;
    public static boolean e = false;

    public static int a() {
        int i = a;
        if (i != 0) {
            return i;
        }
        try {
            java.lang.Class<?> cls = java.lang.Class.forName(com.huawei.hms.framework.common.EmuiUtil.BUILDEX_VERSION);
            java.lang.reflect.Field declaredField = cls.getDeclaredField(com.huawei.hms.framework.common.EmuiUtil.EMUI_SDK_INT);
            java.lang.reflect.AccessibleObject.setAccessible(new java.lang.reflect.Field[]{declaredField}, true);
            java.lang.Object obj = declaredField.get(cls);
            if (obj instanceof java.lang.Integer) {
                a = ((java.lang.Integer) obj).intValue();
            }
        } catch (java.lang.ClassCastException unused) {
            defpackage.b25.h("EnvironmentUtil", "ClassCastException: getEMUIVersionCode is not a number ");
        } catch (java.lang.ClassNotFoundException unused2) {
            defpackage.b25.h("EnvironmentUtil", "ClassNotFoundException: ");
        } catch (java.lang.IllegalAccessException unused3) {
            defpackage.b25.h("EnvironmentUtil", "IllegalAccessException: ");
        } catch (java.lang.NoSuchFieldException unused4) {
            defpackage.b25.h("EnvironmentUtil", "NoSuchFieldException: ");
        }
        defpackage.b25.p("EnvironmentUtil", "emuiVersionCodeValue: " + a);
        return a;
    }

    public static java.lang.String b() {
        try {
            return defpackage.z71.b().getPackageManager().getPackageInfo("com.huawei.hwid", 16384).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException unused) {
            defpackage.b25.h("EnvironmentUtil", "cannot find com.huawei.hwid");
            return "";
        }
    }

    public static int c() {
        try {
            java.lang.String str = defpackage.z71.b().getPackageManager().getPackageInfo("com.huawei.hwid", 16384).versionName;
            defpackage.b25.p("EnvironmentUtil", "hms version is : " + str);
            if (defpackage.i0b.a(str)) {
                return 0;
            }
            java.lang.String[] split = str.split("\\.");
            if (split.length < 3) {
                return 0;
            }
            return java.lang.Integer.parseInt(split[0] + split[1] + split[2]);
        } catch (android.content.pm.PackageManager.NameNotFoundException unused) {
            defpackage.b25.h("EnvironmentUtil", "can not find com.huawei.hwid.");
            return 0;
        } catch (java.lang.NumberFormatException unused2) {
            defpackage.b25.h("EnvironmentUtil", "get hms version number format exception");
            return 0;
        } catch (java.util.regex.PatternSyntaxException unused3) {
            defpackage.b25.h("EnvironmentUtil", "get hms version number split exception");
            return 0;
        }
    }

    public static java.lang.String d(java.lang.String str, java.lang.String str2) {
        android.os.Bundle bundle;
        if (android.text.TextUtils.isEmpty(str)) {
            defpackage.b25.h("EnvironmentUtil", "getMetaData key is empty");
            return str2;
        }
        try {
            bundle = defpackage.z71.c().getPackageManager().getApplicationInfo(defpackage.z71.c().getPackageName(), 128).metaData;
        } catch (android.content.pm.PackageManager.NameNotFoundException unused) {
            defpackage.b25.h("EnvironmentUtil", "getMetaData failed");
        } catch (java.lang.RuntimeException unused2) {
            defpackage.b25.h("EnvironmentUtil", "getMetaData RuntimeException");
        }
        if (bundle != null && bundle.containsKey(str)) {
            return bundle.getString(str);
        }
        defpackage.b25.h("EnvironmentUtil", "getMetaData no key");
        return str2;
    }

    public static boolean e() {
        boolean equals = a() < 25 ? android.text.TextUtils.equals("HONOR", android.os.Build.BRAND) : false;
        defpackage.b25.p("EnvironmentUtil", "isHonorPhone : " + equals);
        return equals;
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x005e  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0061  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean f() {
        /*
            java.lang.String r0 = "EnvironmentUtil"
            boolean r1 = defpackage.tp2.d
            if (r1 == 0) goto L9
            boolean r0 = defpackage.tp2.e
            return r0
        L9:
            java.lang.String r1 = "android.os.SystemProperties"
            java.lang.Class r1 = java.lang.Class.forName(r1)     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.String r2 = "get"
            java.lang.Class<java.lang.String> r3 = java.lang.String.class
            java.lang.Class[] r3 = new java.lang.Class[]{r3}     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.reflect.Method r2 = r1.getDeclaredMethod(r2, r3)     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.String r3 = "ro.product.manufacturer"
            java.lang.Object[] r3 = new java.lang.Object[]{r3}     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.Object r1 = r2.invoke(r1, r3)     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.String r1 = (java.lang.String) r1     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            goto L41
        L28:
            java.lang.String r1 = "InvocationTargetException"
            defpackage.b25.h(r0, r1)
            goto L3f
        L2e:
            java.lang.String r1 = "IllegalAccessException"
            defpackage.b25.h(r0, r1)
            goto L3f
        L34:
            java.lang.String r1 = "NoSuchMethodException"
            defpackage.b25.h(r0, r1)
            goto L3f
        L3a:
            java.lang.String r1 = "ClassNotFoundException"
            defpackage.b25.h(r0, r1)
        L3f:
            java.lang.String r1 = ""
        L41:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Get Manufacturer: "
            r2.append(r3)
            r2.append(r1)
            java.lang.String r2 = r2.toString()
            defpackage.b25.p(r0, r2)
            java.lang.String r0 = "HONOR"
            boolean r0 = r0.equals(r1)
            r1 = 1
            if (r0 == 0) goto L61
            defpackage.tp2.e = r1
            goto L64
        L61:
            r0 = 0
            defpackage.tp2.e = r0
        L64:
            defpackage.tp2.d = r1
            boolean r0 = defpackage.tp2.e
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: defpackage.tp2.f():boolean");
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x005e  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x0061  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean g(android.content.Context r3) {
        /*
            java.lang.String r3 = "EnvironmentUtil"
            boolean r0 = defpackage.tp2.b
            if (r0 == 0) goto L9
            boolean r3 = defpackage.tp2.c
            return r3
        L9:
            java.lang.String r0 = "android.os.SystemProperties"
            java.lang.Class r0 = java.lang.Class.forName(r0)     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.String r1 = "get"
            java.lang.Class<java.lang.String> r2 = java.lang.String.class
            java.lang.Class[] r2 = new java.lang.Class[]{r2}     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.reflect.Method r1 = r0.getDeclaredMethod(r1, r2)     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.String r2 = "ro.product.manufacturer"
            java.lang.Object[] r2 = new java.lang.Object[]{r2}     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.Object r0 = r1.invoke(r0, r2)     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            java.lang.String r0 = (java.lang.String) r0     // Catch: java.lang.reflect.InvocationTargetException -> L28 java.lang.IllegalAccessException -> L2e java.lang.NoSuchMethodException -> L34 java.lang.ClassNotFoundException -> L3a
            goto L41
        L28:
            java.lang.String r0 = "isHuaweiPhone, InvocationTargetException"
            defpackage.b25.h(r3, r0)
            goto L3f
        L2e:
            java.lang.String r0 = "isHuaweiPhone, IllegalAccessException"
            defpackage.b25.h(r3, r0)
            goto L3f
        L34:
            java.lang.String r0 = "isHuaweiPhone, NoSuchMethodException"
            defpackage.b25.h(r3, r0)
            goto L3f
        L3a:
            java.lang.String r0 = "isHuaweiPhone, ClassNotFoundException"
            defpackage.b25.h(r3, r0)
        L3f:
            java.lang.String r0 = ""
        L41:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Get Manufacturer: "
            r1.append(r2)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            defpackage.b25.p(r3, r1)
            java.lang.String r3 = "HUAWEI"
            boolean r3 = r3.equals(r0)
            r0 = 1
            if (r3 == 0) goto L61
            defpackage.tp2.c = r0
            goto L64
        L61:
            r3 = 0
            defpackage.tp2.c = r3
        L64:
            defpackage.tp2.b = r0
            boolean r3 = defpackage.tp2.c
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: defpackage.tp2.g(android.content.Context):boolean");
    }
}
