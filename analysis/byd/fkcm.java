package defpackage;

import android.content.Context;
import android.os.Trace;
import android.util.Pair;
import com.google.android.gms.phenotype.Configuration;
import com.google.android.gms.phenotype.Configurations;
import com.google.android.gms.phenotype.Flag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes8.dex */
public final class fkcm {
    public static final /* synthetic */ int b = 0;
    private static final bmnk c = bmnk.b("GetConfigSnapshotOp", bmbu.aI);
    private static final Flag[] d = new Flag[0];
    private static final String[] e = new String[0];
    private static final Configurations f = new Configurations("", "", new Configuration[0], false, (byte[]) null, 0);
    public final String a;
    private final String g;
    private final String h;
    private fjza i;

    public fkcm(String str, String str2, String str3, fjza fjzaVar) {
        this.i = null;
        this.a = fkah.c(str, str3);
        this.g = str2;
        this.h = null;
        this.i = fjzaVar;
    }

    public static int a(fjza fjzaVar, String str, String str2) {
        Trace.beginSection(fkai.a("fkcm", "getVersion_packageVersion", str));
        try {
            fjze i = fjzaVar.b("SELECT version FROM Packages WHERE packageName = ?").h(str).i();
            try {
                if (i == null) {
                    throw new fjzu(29503);
                }
                int d2 = (int) i.d(0);
                i.close();
                Trace.endSection();
                Trace.beginSection(fkai.a("fkcm", "getVersion_tokenVersion", str));
                try {
                    fjze i2 = fjzaVar.b("SELECT version FROM ExperimentTokens WHERE packageName = ? AND version <= ? AND user = ? AND isCommitted = 0 ORDER BY version DESC LIMIT 1").h(str, Integer.valueOf(d2), str2).i();
                    if (i2 == null) {
                        Trace.endSection();
                        return d2;
                    }
                    try {
                        int d3 = (int) i2.d(0);
                        i2.close();
                        Trace.endSection();
                        return d3;
                    } finally {
                    }
                } catch (Throwable th) {
                    try {
                        Trace.endSection();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } finally {
            }
        } catch (Throwable th3) {
            try {
                Trace.endSection();
            } catch (Throwable th4) {
                th3.addSuppressed(th4);
            }
            throw th3;
        }
    }

    static Configurations b(Set set, Set set2, String str, fkcl fkclVar, boolean z) {
        HashMap hashMap = new HashMap();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Flag flag = (Flag) it.next();
            fkbt.a(hashMap, Integer.valueOf(flag.i), flag);
        }
        HashMap hashMap2 = new HashMap();
        Iterator it2 = set2.iterator();
        while (it2.hasNext()) {
            Flag flag2 = (Flag) it2.next();
            fkbt.a(hashMap2, Integer.valueOf(flag2.i), flag2);
        }
        ArrayList arrayList = new ArrayList();
        HashSet<Integer> hashSet = new HashSet();
        hashSet.addAll(hashMap.keySet());
        hashSet.addAll(hashMap2.keySet());
        for (Integer num : hashSet) {
            Collection collection = (Collection) hashMap.get(num);
            Collection collection2 = (Collection) hashMap2.get(num);
            Flag[] flagArr = d;
            if (collection != null) {
                flagArr = (Flag[]) collection.toArray(new Flag[collection.size()]);
            }
            String[] strArr = e;
            if (collection2 != null) {
                strArr = new String[collection2.size()];
                Iterator it3 = collection2.iterator();
                int i = 0;
                while (it3.hasNext()) {
                    strArr[i] = ((Flag) it3.next()).b;
                    i++;
                }
            }
            arrayList.add(new Configuration(num.intValue(), flagArr, strArr));
        }
        return new Configurations(str, fkclVar.c, (Configuration[]) arrayList.toArray(new Configuration[arrayList.size()]), z, fkclVar.b, fkclVar.d);
    }

    static Flag d(fjzc fjzcVar) {
        int d2 = (int) fjzcVar.d(0);
        String f2 = fjzcVar.f(1);
        if (!fjzcVar.g(2)) {
            return new Flag(f2, fjzcVar.d(2), d2);
        }
        if (!fjzcVar.g(3)) {
            return new Flag(f2, fjzcVar.d(3) != 0, d2);
        }
        if (!fjzcVar.g(4)) {
            return new Flag(f2, fjzcVar.c(4), d2);
        }
        if (!fjzcVar.g(5)) {
            return new Flag(f2, fjzcVar.f(5), d2);
        }
        if (fjzcVar.g(6)) {
            throw new IllegalStateException("Flag without value: ".concat(String.valueOf(f2)));
        }
        return new Flag(f2, fjzcVar.i(6), d2);
    }

    static Flag e(fjyy fjyyVar) {
        String f2 = fjyyVar.f(0);
        int d2 = (int) fjyyVar.d(1);
        if (d2 == 1) {
            return new Flag(fjyyVar.f(0), fjyyVar.d(2), 0);
        }
        if (d2 == 2) {
            return new Flag(f2, fjyyVar.d(2) == 1, 0);
        }
        if (d2 == 3) {
            return new Flag(fjyyVar.f(0), fjyyVar.c(2), 0);
        }
        if (d2 == 4) {
            return new Flag(fjyyVar.f(0), fjyyVar.f(2), 0);
        }
        if (d2 == 5) {
            return new Flag(fjyyVar.f(0), fjyyVar.i(2), 0);
        }
        throw new IllegalStateException("Unrecognized flag value type " + fjyyVar.d(1) + " for flag name " + fjyyVar.e(0));
    }

    public static List f(gtsm gtsmVar) {
        ArrayList arrayList = new ArrayList();
        hots og = gtsmVar.b.og();
        while (og.hasNext()) {
            gtsk gtskVar = (gtsk) og.next();
            int i = gtskVar.c;
            if (i == 0) {
                arrayList.add(new Flag(gtskVar.h(), false, 0));
            } else if (i == 1) {
                arrayList.add(new Flag(gtskVar.h(), true, 0));
            } else if (i == 2) {
                arrayList.add(new Flag(gtskVar.h(), gtskVar.c(), 0));
            } else if (i == 3) {
                arrayList.add(new Flag(gtskVar.h(), gtskVar.a(), 0));
            } else if (i == 4) {
                arrayList.add(new Flag(gtskVar.h(), gtskVar.g(), 0));
            } else {
                if (i != 5) {
                    throw new IllegalStateException(a.k(i, "Unrecognized flag value type "));
                }
                if (gtskVar.e() instanceof byte[]) {
                    arrayList.add(new Flag(gtskVar.h(), (byte[]) gtskVar.e(), 0));
                } else {
                    arrayList.add(new Flag(gtskVar.h(), ((jjrj) gtskVar.e()).N(), 0));
                }
            }
        }
        return arrayList;
    }

    public static Map g(fjza fjzaVar, String str, long j) {
        if (j == -1) {
            return new HashMap();
        }
        HashMap hashMap = new HashMap();
        fkai.b(fkcm.class, "getActiveOverridesPhixit", str);
        try {
            fjyy f2 = fjzaVar.b("WITH UserOverrides AS (\n  SELECT flag_overrides.name, flag_overrides.type, flag_overrides.value\n  FROM flag_overrides\n  INNER JOIN experiment_states_to_overrides\n    USING (override_id)\n  INNER JOIN accounts\n    USING (account_id)\n  WHERE\n    experiment_state_id = ?1\n    AND active IS 1\n    AND accounts.name <> '*'\n), StarOverrides AS (\n  SELECT flag_overrides.name, flag_overrides.type, flag_overrides.value\n  FROM flag_overrides\n  INNER JOIN experiment_states_to_overrides\n    USING (override_id)\n  INNER JOIN accounts\n    USING (account_id)\n  WHERE\n    experiment_state_id = ?1\n    AND active IS 1\n    AND accounts.name = '*'\n    /* Exclude star override if there is a user override with the same name. */\n    AND NOT EXISTS (\n      SELECT NULL FROM UserOverrides WHERE UserOverrides.name = flag_overrides.name)\n)\nSELECT * FROM StarOverrides\nUNION ALL\nSELECT * FROM UserOverrides;\n").h(Long.valueOf(j)).e("UserOverrides").f();
            while (f2.b()) {
                try {
                    Flag e2 = e(f2);
                    hashMap.put(e2.b, e2);
                } finally {
                }
            }
            f2.close();
            Trace.endSection();
            return hashMap;
        } catch (Throwable th) {
            try {
                Trace.endSection();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    public static boolean h(String str) {
        int i = gmhv.a;
        return kdtt.a.d().a().b.contains(str);
    }

    static Flag[] i(fjza fjzaVar, String str, String str2) {
        Trace.beginSection(fkai.a("fkcm", "getUncommittedOverrides_allOverrides", str));
        try {
            if (!fjzaVar.b("SELECT EXISTS(SELECT NULL FROM FlagOverrides)").d().g()) {
                Trace.endSection();
                return null;
            }
            Trace.endSection();
            Trace.beginSection(fkai.a("fkcm", "getUncommittedOverrides_uncommittedOverridesAllUsers", str));
            try {
                fjyy f2 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM FlagOverrides WHERE packageName = ? AND user = '*' AND committed = 0").h(str).f();
                try {
                    Trace.beginSection(fkai.a("fkcm", "getUncommittedOverrides_uncommittedOverridesForUser", str));
                    try {
                        fjyy f3 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM FlagOverrides WHERE packageName = ? AND user = ? AND committed = 0").h(str, str2).f();
                        try {
                            boolean b2 = f2.b();
                            boolean b3 = f3.b();
                            if (!b2) {
                                if (!b3) {
                                    f3.close();
                                    Trace.endSection();
                                    f2.close();
                                    Trace.endSection();
                                    return null;
                                }
                                b3 = true;
                            }
                            ArrayList arrayList = new ArrayList();
                            if (b2) {
                                do {
                                    arrayList.add(d(f2));
                                } while (f2.b());
                            }
                            if (b3) {
                                do {
                                    arrayList.add(d(f3));
                                } while (f3.b());
                            }
                            Flag[] flagArr = (Flag[]) arrayList.toArray(new Flag[0]);
                            f3.close();
                            Trace.endSection();
                            f2.close();
                            Trace.endSection();
                            return flagArr;
                        } finally {
                        }
                    } finally {
                    }
                } finally {
                }
            } catch (Throwable th) {
                try {
                    Trace.endSection();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                Trace.endSection();
            } catch (Throwable th4) {
                th3.addSuppressed(th4);
            }
            throw th3;
        }
    }

    private final Configurations j(fjza fjzaVar) {
        fjze i;
        fkcl fkclVar;
        fkcl fkclVar2;
        Pair pair;
        String str;
        boolean z;
        hloe e2;
        fjyy f2;
        int i2;
        String str2 = this.a;
        String str3 = this.g;
        int a = a(fjzaVar, str2, str3);
        Flag[] i3 = i(fjzaVar, str2, str3);
        if (i3 != null) {
            int i4 = 0;
            while (true) {
                if (i4 < i3.length) {
                    Flag flag = i3[i4];
                    if (flag.b.equals("__phenotype_server_token") && flag.h == 4) {
                        fkclVar = new fkcl(null, flag.d(), 0L);
                        break;
                    }
                    i4++;
                } else {
                    Trace.beginSection(fkai.a("fkcm", "getUncommittedTokens_serverToken", str2));
                    try {
                        i = fjzaVar.b("SELECT serverToken FROM ExperimentTokens WHERE packageName = ? AND version = ? AND user = ? AND isCommitted = 0").h(str2, Integer.valueOf(a), str3).i();
                        try {
                            if (i != null) {
                                fkclVar2 = new fkcl(null, i.f(0), 0L);
                                i.close();
                                Trace.endSection();
                            } else {
                                fkclVar = fkcl.a;
                                Trace.endSection();
                            }
                        } finally {
                        }
                    } finally {
                    }
                }
            }
        } else {
            String str4 = this.a;
            Trace.beginSection(fkai.a("fkcm", "getUncommittedTokens_tokens", str4));
            try {
                i = fjzaVar.b("SELECT experimentToken,serverToken,servingVersion FROM ExperimentTokens WHERE packageName = ? AND version = ? AND user = ? AND isCommitted = 0").h(str4, Integer.valueOf(a), this.g).i();
                try {
                    if (i != null) {
                        fkclVar2 = new fkcl(i.h(0), i.f(1), i.d(2));
                        i.close();
                        Trace.endSection();
                        fkclVar = fkclVar2;
                    } else {
                        fkclVar = fkcl.a;
                        Trace.endSection();
                    }
                } finally {
                    if (i == null) {
                        throw th;
                    }
                    try {
                        i.close();
                        throw th;
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
            } finally {
                try {
                    Trace.endSection();
                    throw th;
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
        }
        String str5 = this.a;
        Trace.beginSection(fkai.a("fkcm", "getCommittedUserAndVersion", str5));
        try {
            fjze i5 = fjzaVar.b("SELECT user, version FROM ApplicationStates WHERE packageName = ?").h(str5).i();
            if (i5 != null) {
                try {
                    Pair create = Pair.create(i5.f(0), Integer.valueOf((int) i5.d(1)));
                    i5.close();
                    Trace.endSection();
                    pair = create;
                } finally {
                }
            } else {
                Trace.endSection();
                pair = null;
            }
            String str6 = this.a;
            int i6 = fkej.a;
            String c2 = pair != null ? fkej.c(fjzaVar, str6, ((Integer) pair.second).intValue(), (String) pair.first, true) : null;
            String str7 = this.h;
            fkei b2 = fkej.b(str7, fjzaVar.f());
            boolean equals = (b2 == null ? "!" : b2.e).equals(c2);
            if (i3 == null && equals) {
                str = c2;
                z = true;
            } else {
                str = c2;
                z = false;
            }
            String str8 = this.g;
            String str9 = str;
            String c3 = fkej.c(fjzaVar, str6, a, str8, false);
            if (str9 == null) {
                str9 = "";
            }
            String d2 = fkej.d(str8, str6, str9, a, c3, fjyq.a(fjzaVar, str6), z);
            if (z) {
                pair.getClass();
                e2 = hlqu.e(297, "Get delta configuration", hlqv.a, hloi.a, true);
                try {
                    String str10 = (String) pair.first;
                    Integer num = (Integer) pair.second;
                    num.intValue();
                    TreeSet treeSet = new TreeSet();
                    Comparator comparator = Flag.a;
                    TreeSet treeSet2 = new TreeSet(comparator);
                    Flag[] d3 = fkch.d(fjzaVar, str6);
                    if (d3 != null) {
                        int length = d3.length;
                        i2 = a;
                        int i7 = 0;
                        while (i7 < length) {
                            int i8 = i7;
                            Flag flag2 = d3[i8];
                            treeSet.add(flag2);
                            treeSet2.add(flag2);
                            i7 = i8 + 1;
                        }
                    } else {
                        i2 = a;
                    }
                    Trace.beginSection(fkai.a("fkcm", "getDeltaConfigurations_committedFlags", str6));
                    try {
                        fjyy f3 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM Flags WHERE packageName = ? AND version = ? AND user = ? AND committed = 1 ORDER BY name").h(str6, num, str10).f();
                        while (f3.b()) {
                            try {
                                Flag d4 = d(f3);
                                if (!treeSet2.contains(d4)) {
                                    treeSet.add(d4);
                                    treeSet2.add(d4);
                                }
                            } finally {
                                try {
                                    f3.close();
                                    throw th;
                                } catch (Throwable th3) {
                                    th.addSuppressed(th3);
                                }
                            }
                        }
                        f3.close();
                        Trace.endSection();
                        TreeSet treeSet3 = new TreeSet();
                        TreeSet treeSet4 = new TreeSet(comparator);
                        Trace.beginSection(fkai.a("fkcm", "getDeltaConfigurations_pendingFlags", str6));
                        try {
                            f2 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM Flags WHERE packageName = ? AND version = ? AND user = ? AND committed = 0 ORDER BY name").h(str6, Integer.valueOf(i2), str8).f();
                            while (f2.b()) {
                                try {
                                    Flag d5 = d(f2);
                                    treeSet3.add(d5);
                                    treeSet4.add(d5);
                                } finally {
                                }
                            }
                            f2.close();
                            Trace.endSection();
                            treeSet3.removeAll(treeSet);
                            treeSet2.removeAll(treeSet4);
                            Configurations b3 = b(treeSet3, treeSet2, d2, fkclVar, true);
                            e2.close();
                            return b3;
                        } finally {
                        }
                    } finally {
                    }
                } finally {
                }
            } else {
                if (str7 != null) {
                    e2 = hlqu.e(302, "Get delta config with unmatched base", hlqv.a, hloi.a, true);
                    try {
                        Comparator comparator2 = Flag.a;
                        TreeSet treeSet5 = new TreeSet(comparator2);
                        String str11 = this.a;
                        Trace.beginSection(fkai.a("fkcm", "getFullConfigurations_flags", str11));
                        try {
                            f2 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM Flags WHERE packageName = ?").h(str11).f();
                            while (f2.b()) {
                                try {
                                    treeSet5.add(d(f2));
                                } finally {
                                    try {
                                        f2.close();
                                        throw th;
                                    } catch (Throwable th4) {
                                        th.addSuppressed(th4);
                                    }
                                }
                            }
                            f2.close();
                            Trace.endSection();
                            TreeSet treeSet6 = new TreeSet(comparator2);
                            Trace.beginSection(fkai.a("fkcm", "getFullConfigurations_userFlags", str11));
                            try {
                                fjyy f4 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM Flags WHERE packageName = ? AND version = ? AND user = ? AND committed = 0 ORDER BY name").h(str11, Integer.valueOf(a), this.g).f();
                                while (f4.b()) {
                                    try {
                                        Flag d6 = d(f4);
                                        treeSet6.add(d6);
                                        treeSet5.remove(d6);
                                    } finally {
                                        try {
                                            f4.close();
                                            throw th;
                                        } catch (Throwable th5) {
                                            th.addSuppressed(th5);
                                        }
                                    }
                                }
                                f4.close();
                                Trace.endSection();
                                if (i3 != null) {
                                    for (Flag flag3 : i3) {
                                        treeSet6.remove(flag3);
                                        treeSet6.add(flag3);
                                        treeSet5.remove(flag3);
                                    }
                                }
                                Configurations b4 = b(treeSet6, treeSet5, d2, fkclVar, false);
                                e2.close();
                                return b4;
                            } finally {
                                try {
                                    Trace.endSection();
                                    throw th;
                                } catch (Throwable th6) {
                                    th.addSuppressed(th6);
                                }
                            }
                        } finally {
                            try {
                                Trace.endSection();
                                throw th;
                            } catch (Throwable th7) {
                                th.addSuppressed(th7);
                            }
                        }
                    } finally {
                        try {
                            e2.close();
                            throw th;
                        } catch (Throwable th8) {
                            th.addSuppressed(th8);
                        }
                    }
                }
                e2 = hlqu.e(300, "Get full configuration", hlqv.a, hloi.a, true);
                try {
                    TreeSet<Flag> treeSet7 = new TreeSet(Flag.a);
                    String str12 = this.a;
                    Trace.beginSection(fkai.a("fkcm", "getFullConfigurations_userFlags", str12));
                    try {
                        fjyy f5 = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM Flags WHERE packageName = ? AND version = ? AND user = ? AND committed = 0 ORDER BY name").h(str12, Integer.valueOf(a), this.g).f();
                        while (f5.b()) {
                            try {
                                treeSet7.add(d(f5));
                            } finally {
                                try {
                                    f5.close();
                                    throw th;
                                } catch (Throwable th9) {
                                    th.addSuppressed(th9);
                                }
                            }
                        }
                        f5.close();
                        Trace.endSection();
                        if (i3 != null) {
                            for (Flag flag4 : i3) {
                                treeSet7.remove(flag4);
                                treeSet7.add(flag4);
                            }
                        }
                        HashMap hashMap = new HashMap();
                        for (Flag flag5 : treeSet7) {
                            fkbt.a(hashMap, Integer.valueOf(flag5.i), flag5);
                        }
                        ArrayList arrayList = new ArrayList();
                        for (Integer num2 : hashMap.keySet()) {
                            List list = (List) hashMap.get(num2);
                            Flag[] flagArr = d;
                            if (list != null) {
                                flagArr = (Flag[]) list.toArray(new Flag[0]);
                            }
                            arrayList.add(new Configuration(num2.intValue(), flagArr, e));
                        }
                        Configurations configurations = new Configurations(d2, fkclVar.c, (Configuration[]) arrayList.toArray(new Configuration[arrayList.size()]), false, fkclVar.b, fkclVar.d);
                        e2.close();
                        return configurations;
                    } finally {
                        try {
                            Trace.endSection();
                            throw th;
                        } catch (Throwable th10) {
                            th.addSuppressed(th10);
                        }
                    }
                } finally {
                }
            }
        } finally {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x0077  */
    /* JADX WARN: Removed duplicated region for block: B:176:0x0109 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:180:0x00f5 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:483:0x0793  */
    /* JADX WARN: Removed duplicated region for block: B:62:0x0305 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:66:0x02f1 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final com.google.android.gms.phenotype.Configurations k(defpackage.fjza r25) {
        /*
            Method dump skipped, instructions count: 1951
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: defpackage.fkcm.k(fjza):com.google.android.gms.phenotype.Configurations");
    }

    /* JADX WARN: Code restructure failed: missing block: B:38:0x007a, code lost:
    
        r6 = r7.b("SELECT server_token\nFROM experiment_states\nWHERE experiment_state_id = ?1;\n").h(java.lang.Long.valueOf(r8)).i();
     */
    /* JADX WARN: Code restructure failed: missing block: B:40:0x0090, code lost:
    
        if (r6 == null) goto L32;
     */
    /* JADX WARN: Code restructure failed: missing block: B:41:0x0092, code lost:
    
        r7 = new defpackage.fkcl(null, r6.f(0), 0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:42:0x009b, code lost:
    
        r6.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x009f, code lost:
    
        r7 = defpackage.fkcl.a;
     */
    /* JADX WARN: Code restructure failed: missing block: B:46:0x00a5, code lost:
    
        r7 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:48:0x00b0, code lost:
    
        throw r7;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final defpackage.fkcl l(defpackage.fjza r7, long r8, java.util.List r10) {
        /*
            r6 = this;
            java.lang.String r0 = "getUncommittedTokensPhixit"
            java.lang.String r6 = r6.a
            java.lang.Class<fkcm> r1 = defpackage.fkcm.class
            defpackage.fkai.b(r1, r0, r6)
            boolean r6 = r10.isEmpty()     // Catch: java.lang.Throwable -> Lb1
            r0 = 0
            if (r6 == 0) goto L4e
            java.lang.String r6 = "SELECT experiment_token, server_token, serving_version\nFROM experiment_states\nWHERE experiment_state_id = ?1;\n"
            fjyx r6 = r7.b(r6)     // Catch: java.lang.Throwable -> Lb1
            java.lang.Long r7 = java.lang.Long.valueOf(r8)     // Catch: java.lang.Throwable -> Lb1
            java.lang.Object[] r7 = new java.lang.Object[]{r7}     // Catch: java.lang.Throwable -> Lb1
            fjyx r6 = r6.h(r7)     // Catch: java.lang.Throwable -> Lb1
            fjze r6 = r6.i()     // Catch: java.lang.Throwable -> Lb1
            if (r6 == 0) goto L3f
            fkcl r7 = new fkcl     // Catch: java.lang.Throwable -> L42
            byte[] r8 = r6.h(r0)     // Catch: java.lang.Throwable -> L42
            r9 = 1
            java.lang.String r9 = r6.f(r9)     // Catch: java.lang.Throwable -> L42
            r10 = 2
            long r0 = r6.d(r10)     // Catch: java.lang.Throwable -> L42
            r7.<init>(r8, r9, r0)     // Catch: java.lang.Throwable -> L42
            r6.close()     // Catch: java.lang.Throwable -> Lb1
            goto La1
        L3f:
            fkcl r7 = defpackage.fkcl.a     // Catch: java.lang.Throwable -> L42
            goto La1
        L42:
            r7 = move-exception
            if (r6 == 0) goto L4d
            r6.close()     // Catch: java.lang.Throwable -> L49
            goto L4d
        L49:
            r6 = move-exception
            r7.addSuppressed(r6)     // Catch: java.lang.Throwable -> Lb1
        L4d:
            throw r7     // Catch: java.lang.Throwable -> Lb1
        L4e:
            java.util.Iterator r6 = r10.iterator()     // Catch: java.lang.Throwable -> Lb1
        L52:
            boolean r10 = r6.hasNext()     // Catch: java.lang.Throwable -> Lb1
            r1 = 0
            r3 = 0
            if (r10 == 0) goto L7a
            java.lang.Object r10 = r6.next()     // Catch: java.lang.Throwable -> Lb1
            com.google.android.gms.phenotype.Flag r10 = (com.google.android.gms.phenotype.Flag) r10     // Catch: java.lang.Throwable -> Lb1
            java.lang.String r4 = r10.b     // Catch: java.lang.Throwable -> Lb1
            java.lang.String r5 = "__phenotype_server_token"
            boolean r4 = r4.equals(r5)     // Catch: java.lang.Throwable -> Lb1
            if (r4 == 0) goto L52
            int r4 = r10.h     // Catch: java.lang.Throwable -> Lb1
            r5 = 4
            if (r4 != r5) goto L52
            fkcl r7 = new fkcl     // Catch: java.lang.Throwable -> Lb1
            java.lang.String r6 = r10.d()     // Catch: java.lang.Throwable -> Lb1
            r7.<init>(r3, r6, r1)     // Catch: java.lang.Throwable -> Lb1
            goto La1
        L7a:
            java.lang.String r6 = "SELECT server_token\nFROM experiment_states\nWHERE experiment_state_id = ?1;\n"
            fjyx r6 = r7.b(r6)     // Catch: java.lang.Throwable -> Lb1
            java.lang.Long r7 = java.lang.Long.valueOf(r8)     // Catch: java.lang.Throwable -> Lb1
            java.lang.Object[] r7 = new java.lang.Object[]{r7}     // Catch: java.lang.Throwable -> Lb1
            fjyx r6 = r6.h(r7)     // Catch: java.lang.Throwable -> Lb1
            fjze r6 = r6.i()     // Catch: java.lang.Throwable -> Lb1
            if (r6 == 0) goto L9f
            fkcl r7 = new fkcl     // Catch: java.lang.Throwable -> La5
            java.lang.String r8 = r6.f(r0)     // Catch: java.lang.Throwable -> La5
            r7.<init>(r3, r8, r1)     // Catch: java.lang.Throwable -> La5
            r6.close()     // Catch: java.lang.Throwable -> Lb1
            goto La1
        L9f:
            fkcl r7 = defpackage.fkcl.a     // Catch: java.lang.Throwable -> La5
        La1:
            android.os.Trace.endSection()
            return r7
        La5:
            r7 = move-exception
            if (r6 == 0) goto Lb0
            r6.close()     // Catch: java.lang.Throwable -> Lac
            goto Lb0
        Lac:
            r6 = move-exception
            r7.addSuppressed(r6)     // Catch: java.lang.Throwable -> Lb1
        Lb0:
            throw r7     // Catch: java.lang.Throwable -> Lb1
        Lb1:
            r6 = move-exception
            android.os.Trace.endSection()     // Catch: java.lang.Throwable -> Lb6
            goto Lba
        Lb6:
            r7 = move-exception
            r6.addSuppressed(r7)
        Lba:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: defpackage.fkcm.l(fjza, long, java.util.List):fkcl");
    }

    public final Configurations c(Context context, fjzr fjzrVar) {
        String str = this.a;
        if (str == null) {
            throw new fjzu(29500, "No source package");
        }
        String str2 = this.g;
        if (str2 == null) {
            throw new fjzu(29500, "No user");
        }
        if (!str2.equals("") && !fkan.a(str2, context)) {
            throw new fjzu(29500, "Invalid user");
        }
        if (h(str) && !str2.equals("")) {
            throw new fjzu(29500, a.j(str, str2, "Can't commit to ", " for direct boot aware package "));
        }
        try {
            fjza fjzaVar = this.i;
            if (fjzaVar != null) {
                boolean f2 = fjzaVar.f();
                fjza fjzaVar2 = this.i;
                return f2 ? k(fjzaVar2) : j(fjzaVar2);
            }
            Trace.beginSection(fkai.a("fkcm", "transaction", str));
            try {
                fjzl b2 = fjzrVar.b();
                b2.c.beginTransactionReadOnly();
                fjzb fjzbVar = new fjzb(b2, false, false);
                try {
                    Configurations k = fjzbVar.f() ? k(fjzbVar) : j(fjzbVar);
                    fjzbVar.e();
                    fjzbVar.close();
                    Trace.endSection();
                    return k;
                } finally {
                }
            } finally {
            }
        } catch (fjzu e2) {
            if (e2.a != 29503) {
                throw e2;
            }
            c.h().y("Succeeded but not registered: %s", new hyof(hyoe.b, this.a));
            return f;
        }
    }

    public fkcm(String str, String str2, String str3, String str4) {
        this.i = null;
        this.a = fkah.c(str, str4);
        this.g = str2;
        this.h = str3;
    }
}
