package defpackage;

import android.os.Trace;
import com.google.android.gms.phenotype.Configuration;
import com.google.android.gms.phenotype.Configurations;
import com.google.android.gms.phenotype.Flag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes8.dex */
public final class fkch {
    public String a;
    public final String b;

    static {
        bmnk.b("GetCommittedConfigOp", bmbu.aI);
    }

    public fkch(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v10 */
    /* JADX WARN: Type inference failed for: r4v18, types: [fjze] */
    /* JADX WARN: Type inference failed for: r4v19, types: [fjze] */
    /* JADX WARN: Type inference failed for: r4v21 */
    /* JADX WARN: Type inference failed for: r4v22 */
    /* JADX WARN: Type inference failed for: r4v24, types: [fkcl] */
    /* JADX WARN: Type inference failed for: r4v25 */
    /* JADX WARN: Type inference failed for: r4v26 */
    /* JADX WARN: Type inference failed for: r4v27 */
    /* JADX WARN: Type inference failed for: r4v7 */
    public static Configurations a(fjza fjzaVar, String str, boolean z) {
        String str2;
        fjzb fjzbVar;
        Flag[] flagArr;
        fjzb fjzbVar2;
        String str3;
        ?? r4;
        fkai.b(fkch.class, "getCommittedOverridesPhixit", str);
        try {
            if (z) {
                str2 = "SELECT flag_overrides.name, flag_overrides.type, flag_overrides.value\nFROM flag_overrides\nINNER JOIN config_packages\n  USING (config_package_id)\nWHERE config_packages.name = ?1;\n";
                fjzbVar = (fjzb) fjzaVar;
            } else {
                str2 = "WITH CommittedOverrides AS (\n  SELECT flag_overrides.name, flag_overrides.type, flag_overrides.value\n  FROM flag_overrides\n  INNER JOIN experiment_states_to_overrides\n    USING (override_id)\n  INNER JOIN config_packages\n    ON experiment_state_id IS committed_experiment_state_id\n  WHERE config_packages.name = ?1\n), OverridesToCommit AS (\n  /* Only consider overrides_to_commit when there is no committed state */\n  SELECT flag_overrides.name, flag_overrides.type, flag_overrides.value\n  FROM flag_overrides\n  INNER JOIN experiment_states_to_overrides\n    USING (override_id)\n  INNER JOIN config_packages\n    USING (config_package_id)\n  INNER JOIN flag_overrides_to_commit\n    ON flag_overrides.config_package_id = flag_overrides_to_commit.config_package_id\n  WHERE\n    config_packages.name = ?1\n    AND config_packages.committed_experiment_state_id IS NULL\n)\nSELECT * FROM CommittedOverrides\nUNION ALL\nSELECT * FROM OverridesToCommit;\n";
                fjzbVar = (fjzb) fjzaVar;
            }
            fjyy f = fjzbVar.b(str2).h(str).f();
            try {
                if (f.b()) {
                    ArrayList arrayList = new ArrayList();
                    do {
                        arrayList.add(fkcm.e(f));
                    } while (f.b());
                    flagArr = (Flag[]) arrayList.toArray(new Flag[0]);
                    f.close();
                    Trace.endSection();
                } else {
                    f.close();
                    Trace.endSection();
                    flagArr = null;
                }
                if (flagArr != null) {
                    int i = 0;
                    while (true) {
                        if (i < flagArr.length) {
                            Flag flag = flagArr[i];
                            if (flag.b.equals("__phenotype_server_token") && flag.h == 4) {
                                r4 = new fkcl(null, flag.d(), 0L);
                                break;
                            }
                            i++;
                        } else {
                            r4 = ((fjzb) fjzaVar).b("SELECT server_token\nFROM experiment_states\nINNER JOIN config_packages\n  ON experiment_states.experiment_state_id IS committed_experiment_state_id\nINNER JOIN experiment_states_to_overrides\n  ON experiment_states_to_overrides.experiment_state_id IS committed_experiment_state_id\nINNER JOIN flag_overrides\n  USING (override_id)\nWHERE config_packages.name = ?1 AND flag_overrides.active IS 1\nLIMIT 1;\n").h(str).i();
                            try {
                                if (r4 != 0) {
                                    fkcl fkclVar = new fkcl(null, r4.f(0), 0L);
                                    r4.close();
                                    r4 = fkclVar;
                                } else {
                                    r4 = fkcl.a;
                                }
                            } finally {
                            }
                        }
                    }
                } else {
                    if (z) {
                        fjzbVar2 = (fjzb) fjzaVar;
                        str3 = "SELECT experiment_token, server_token, serving_version\nFROM config_packages\nWHERE config_packages.name = ?1;\n";
                    } else {
                        fjzbVar2 = (fjzb) fjzaVar;
                        str3 = "SELECT experiment_token, server_token, serving_version\nFROM experiment_states\nINNER JOIN config_packages\n  ON experiment_state_id IS committed_experiment_state_id\nWHERE config_packages.name = ?1;\n";
                    }
                    fjze i2 = fjzbVar2.b(str3).h(str).i();
                    try {
                        if (i2 != null) {
                            fkcl fkclVar2 = new fkcl(i2.h(0), i2.f(1), i2.d(2));
                            i2.close();
                            r4 = fkclVar2;
                        } else {
                            r4 = fkcl.a;
                        }
                    } finally {
                    }
                }
                try {
                    gtsj gtsjVar = new gtsj();
                    try {
                        HashMap hashMap = new HashMap();
                        if (flagArr != null) {
                            for (Flag flag2 : flagArr) {
                                hashMap.put(flag2.b, flag2);
                            }
                        }
                        fjyx b = z ? ((fjzb) fjzaVar).b("SELECT flags_content\nFROM config_packages\nWHERE config_packages.name = ?1 AND flags_content IS NOT NULL;\n") : ((fjzb) fjzaVar).b("SELECT flags_content\nFROM param_partitions\nINNER JOIN experiment_states_to_partitions\n  USING (param_partition_id)\nINNER JOIN config_packages\n  ON experiment_state_id IS committed_experiment_state_id\nWHERE config_packages.name = ?1;\n");
                        ArrayList arrayList2 = new ArrayList();
                        fjyy f2 = b.h(str).f();
                        while (f2.b()) {
                            try {
                                arrayList2.add((gtsm) gtsjVar.a(f2.i(0), new gtsi() { // from class: fkcg
                                    public final Object a(jjrn jjrnVar) {
                                        return gtsm.d(jjrnVar);
                                    }
                                }));
                            } finally {
                            }
                        }
                        f2.close();
                        for (Flag flag3 : fkcm.f(gtsm.c(arrayList2))) {
                            String str4 = flag3.b;
                            if (!hashMap.containsKey(str4)) {
                                hashMap.put(str4, flag3);
                            }
                        }
                        Configuration[] configurationArr = new Configuration[0];
                        if (!hashMap.isEmpty()) {
                            configurationArr = new Configuration[]{new Configuration(0, (Flag[]) hashMap.values().toArray(new Flag[0]), new String[0])};
                        }
                        fkcl fkclVar3 = r4;
                        Configurations configurations = new Configurations("", fkclVar3.c, configurationArr, false, fkclVar3.b, fkclVar3.d);
                        gtsjVar.close();
                        return configurations;
                    } finally {
                    }
                } catch (IOException e) {
                    throw new fjzu(29518, "Failed to parse flags content.", e);
                }
            } finally {
            }
        } finally {
        }
    }

    public static Configurations b(fjza fjzaVar, String str, fkcl fkclVar, Flag[] flagArr) {
        TreeSet treeSet = new TreeSet(Flag.a);
        fjyy f = ((fjzb) fjzaVar).b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM Flags WHERE packageName = ? AND committed = 1").h(str).f();
        while (f.b()) {
            try {
                treeSet.add(fkcm.d(f));
            } catch (Throwable th) {
                try {
                    f.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
        f.close();
        if (flagArr != null) {
            for (Flag flag : flagArr) {
                treeSet.remove(flag);
                treeSet.add(flag);
            }
        }
        return fkcm.b(treeSet, Collections.EMPTY_SET, "", fkclVar, false);
    }

    public static fkcl c(fjza fjzaVar, String str, Flag[] flagArr) {
        fjze i;
        if (flagArr == null) {
            i = ((fjzb) fjzaVar).b("SELECT experimentToken, serverToken, servingVersion FROM ExperimentTokens WHERE packageName = ? AND isCommitted = 1 LIMIT 1").h(str).i();
            try {
                if (i == null) {
                    return fkcl.a;
                }
                fkcl fkclVar = new fkcl(i.i(0), i.f(1), i.d(2));
                i.close();
                return fkclVar;
            } finally {
            }
        }
        for (Flag flag : flagArr) {
            if (flag.b.equals("__phenotype_server_token") && flag.h == 4) {
                return new fkcl(null, flag.d(), 0L);
            }
        }
        i = ((fjzb) fjzaVar).b("SELECT serverToken FROM ExperimentTokens WHERE packageName = ? AND isCommitted = 1 LIMIT 1").h(str).i();
        try {
            if (i == null) {
                return fkcl.a;
            }
            fkcl fkclVar2 = new fkcl(null, i.f(0), 0L);
            i.close();
            return fkclVar2;
        } finally {
        }
    }

    public static Flag[] d(fjza fjzaVar, String str) {
        Trace.beginSection(fkai.a("fkch", "getCommittedOverrides", str));
        try {
            fjyy f = fjzaVar.b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal FROM FlagOverrides WHERE packageName = ? AND committed = 1").h(str).f();
            try {
                if (!f.b()) {
                    f.close();
                    Trace.endSection();
                    return null;
                }
                ArrayList arrayList = new ArrayList();
                do {
                    arrayList.add(fkcm.d(f));
                } while (f.b());
                Flag[] flagArr = (Flag[]) arrayList.toArray(new Flag[0]);
                f.close();
                Trace.endSection();
                return flagArr;
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
    }
}
