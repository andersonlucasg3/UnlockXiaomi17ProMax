package defpackage;

import com.google.android.gms.phenotype.Flag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes8.dex */
public final class fkee {
    String a;
    public final String d;
    public final Flag[] e;
    public final String f;
    public final fkak g;
    boolean h = false;
    public final ArrayList b = new ArrayList();
    public final ArrayList c = new ArrayList();

    static {
        bmnk.b("SetFlagOverridesOperation", bmbu.aI);
    }

    public fkee(String str, String str2, Flag[] flagArr, String str3, fkak fkakVar) {
        this.a = str;
        this.d = str2;
        this.e = flagArr;
        this.f = str3;
        this.g = fkakVar;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static boolean a(fjzj fjzjVar, String str, String str2, Flag[] flagArr, boolean z) {
        int i;
        int i2;
        boolean z2;
        Object valueOf;
        int i3;
        boolean z3;
        long j;
        long j2;
        Map.Entry entry;
        long j3;
        long j4;
        Map.Entry entry2;
        fjzj fjzjVar2 = fjzjVar;
        Flag[] flagArr2 = flagArr;
        fjzb fjzbVar = (fjzb) fjzjVar2;
        long b = fjzbVar.b("SELECT IFNULL(MAX(config_package_id), -1) FROM config_packages WHERE name = ?1;").h(str).b();
        long b2 = fjzbVar.b("SELECT IFNULL(MAX(account_id), -1) FROM accounts WHERE name = ?1;").h(str2).b();
        long j5 = -1;
        if (b2 == -1) {
            b2 = fjzjVar2.c("INSERT INTO accounts (name) VALUES (?1);").g(str2).c();
        }
        long j6 = b2;
        HashMap hashMap = new HashMap();
        fjyy f = fjzbVar.b("WITH UncommittedStates AS (\n  SELECT account_id, MAX(experiment_state_id) AS experiment_state_id\n  FROM experiment_states\n  INNER JOIN config_packages\n    ON (experiment_states.config_package_id = config_packages.config_package_id)\n  WHERE\n    config_packages.name = ?1\n    AND experiment_states.experiment_state_id IS NOT committed_experiment_state_id\n  GROUP BY account_id\n), CommittedState AS (\n  SELECT account_id, committed_experiment_state_id AS experiment_state_id\n  FROM experiment_states\n  INNER JOIN config_packages\n      ON (experiment_state_id IS committed_experiment_state_id)\n  WHERE config_packages.name = ?1\n)\nSELECT\n    account_id,\n    IFNULL(UncommittedStates.experiment_state_id, -1) AS uncommitted_experiment_state_id,\n    IFNULL(CommittedState.experiment_state_id, -1) AS committed_experiment_state_id,\n    accounts.name AS account_name\nFROM accounts\nLEFT OUTER JOIN UncommittedStates\n    USING (account_id)\nLEFT OUTER JOIN CommittedState\n    USING (account_id)\nWHERE accounts.name <> '*';\n").h(str).d().f();
        while (true) {
            try {
                i = 3;
                i2 = 2;
                z2 = false;
                if (!f.b()) {
                    break;
                }
                long j7 = j5;
                long j8 = b;
                hashMap.put(Long.valueOf(f.d(0)), new fked(f.d(1), f.d(2)));
                f.e(3);
                f.d(1);
                f.d(2);
                j5 = j7;
                b = j8;
            } finally {
            }
        }
        long j9 = b;
        long j10 = j5;
        f.close();
        int length = flagArr2.length;
        int i4 = 0;
        boolean z4 = false;
        fjzj fjzjVar3 = fjzjVar2;
        while (i4 < length) {
            Flag flag = flagArr2[i4];
            int i5 = flag.h;
            if (i5 == 1) {
                valueOf = Long.valueOf(flag.b());
            } else if (i5 == i2) {
                valueOf = Long.valueOf(true != flag.f() ? 0L : 1L);
            } else if (i5 == i) {
                valueOf = Double.valueOf(flag.a());
            } else if (i5 == 4) {
                valueOf = flag.d();
            } else {
                if (i5 != 5) {
                    throw new fjzu(29500, "Unrecognized flag type");
                }
                valueOf = flag.g();
            }
            Object obj = valueOf;
            int i6 = flag.j;
            if (i6 != 1) {
                i3 = i6;
                z3 = z2;
            } else if (fjzjVar3.a() < 1035 || z) {
                z3 = z2;
                i3 = 1;
            } else {
                z3 = true;
                i3 = 1;
            }
            fjyx b3 = fjzbVar.b("    SELECT EXISTS (\n      SELECT NULL\n      FROM flag_overrides\n      WHERE\n        (\n          config_package_id = ?1\n          OR config_package_name IS ?2\n        )\n        AND account_id = ?3\n        AND name = ?4\n        AND type = ?5\n        AND value = ?6\n        AND active IS 1\n    )\n");
            Long valueOf2 = Long.valueOf(j9);
            int i7 = i;
            Long valueOf3 = Long.valueOf(j6);
            int i8 = i2;
            String str3 = flag.b;
            Integer valueOf4 = Integer.valueOf(i5);
            boolean z5 = z2;
            if (!b3.h(valueOf2, str, valueOf3, str3, valueOf4, obj).g() && (!z3 || !fjzbVar.b("    SELECT EXISTS (\n      SELECT NULL\n      FROM flag_overrides\n      WHERE\n        (\n          config_package_id = ?1\n          OR config_package_name IS ?2\n        )\n        AND account_id = ?3\n        AND name = ?4\n        AND type = ?5\n        AND source = 0\n        AND active IS 1\n    )\n").h(valueOf2, str, valueOf3, str3, valueOf4).g())) {
                fjzjVar3.c("    UPDATE flag_overrides\n    SET active = NULL\n    WHERE\n      (\n        config_package_id = ?1\n        OR config_package_name IS ?2\n      )\n      AND account_id = ?3\n      AND name = ?4\n      AND active IS 1;\n").g(valueOf2, str, valueOf3, str3).d();
                if (j9 != j10) {
                    long c = (fjzjVar3.a() < 1035 || z) ? fjzjVar3.c("    INSERT INTO flag_overrides (config_package_id, account_id, name, value, type)\n    VALUES (?1, ?2, ?3, ?4, ?5);\n").g(valueOf2, valueOf3, str3, obj, valueOf4).c() : fjzjVar3.c("    INSERT INTO flag_overrides (config_package_id, account_id, name, value, type, source)\n    VALUES (?1, ?2, ?3, ?4, ?5, ?6);\n").g(valueOf2, valueOf3, str3, obj, valueOf4, Integer.valueOf(i3)).c();
                    if (flag.i == -1000) {
                        fjzjVar3 = fjzjVar3;
                        for (Map.Entry entry3 : hashMap.entrySet()) {
                            if (((fked) entry3.getValue()).b() == j10 || ((fked) entry3.getValue()).b() == ((fked) entry3.getValue()).a()) {
                                entry = entry3;
                                j3 = j9;
                                j4 = c;
                            } else {
                                ((fked) entry3.getValue()).b();
                                entry3.getKey();
                                long b4 = ((fked) entry3.getValue()).b();
                                long longValue = ((Long) entry3.getKey()).longValue();
                                entry = entry3;
                                j4 = c;
                                j3 = j9;
                                b(fjzjVar3, b4, j4, j3, longValue);
                            }
                            ((fked) entry.getValue()).a();
                            entry.getKey();
                            fjzj fjzjVar4 = fjzjVar;
                            long b5 = b(fjzjVar4, ((fked) entry.getValue()).a(), j4, j3, ((Long) entry.getKey()).longValue());
                            long j11 = j4;
                            if (b5 != j10) {
                                j9 = j3;
                                fked fkedVar = new fked(b5, ((fked) entry.getValue()).a);
                                entry2 = entry;
                                entry2.setValue(fkedVar);
                            } else {
                                j9 = j3;
                                entry2 = entry;
                            }
                            if (((fked) entry2.getValue()).a == j10) {
                                fjzjVar4.c("  INSERT INTO flag_overrides_to_commit (override_id, config_package_id,\n    account_id)\n  VALUES (?1, ?2, ?3);\n").g(Long.valueOf(j11), Long.valueOf(j9), entry2.getKey()).d();
                            }
                            c = j11;
                            fjzjVar3 = fjzjVar4;
                        }
                        j = j6;
                        j2 = j9;
                        z4 = true;
                    } else {
                        long j12 = c;
                        if (str2.equals("*")) {
                            for (Map.Entry entry4 : hashMap.entrySet()) {
                                long j13 = j9;
                                long j14 = j12;
                                long b6 = b(fjzjVar3, ((fked) entry4.getValue()).b(), j14, j13, ((Long) entry4.getKey()).longValue());
                                j12 = j14;
                                if (b6 != j10) {
                                    entry4.setValue(new fked(b6, ((fked) entry4.getValue()).a));
                                    c(fjzjVar3, ((fked) entry4.getValue()).a, b6);
                                    j9 = j13;
                                } else {
                                    j9 = j13;
                                }
                            }
                        } else {
                            long j15 = j9;
                            fked fkedVar2 = (fked) hashMap.get(valueOf3);
                            if (fkedVar2 != null) {
                                j2 = j15;
                                j = j6;
                                long b7 = b(fjzjVar3, fkedVar2.b(), j12, j2, j);
                                if (b7 != j10) {
                                    long j16 = fkedVar2.a;
                                    hashMap.put(valueOf3, new fked(b7, j16));
                                    c(fjzjVar3, j16, b7);
                                }
                            } else {
                                j = j6;
                                j2 = j15;
                            }
                        }
                    }
                    i4++;
                    flagArr2 = flagArr;
                    j9 = j2;
                    j6 = j;
                    i = i7;
                    i2 = i8;
                    z2 = z5;
                    fjzjVar3 = fjzjVar3;
                } else if (fjzjVar3.a() < 1035 || z) {
                    fjzjVar3.c("      INSERT INTO flag_overrides (config_package_name, account_id, name, value,\n        type)\n      VALUES (?1, ?2, ?3, ?4, ?5);\n").g(str, valueOf3, str3, obj, valueOf4).d();
                } else {
                    fjzjVar3.c("      INSERT INTO flag_overrides (config_package_name, account_id, name, value,\n        type, source)\n      VALUES (?1, ?2, ?3, ?4, ?5, ?6);\n").g(str, valueOf3, str3, obj, valueOf4, Integer.valueOf(i3)).d();
                }
            }
            j = j6;
            j2 = j9;
            i4++;
            flagArr2 = flagArr;
            j9 = j2;
            j6 = j;
            i = i7;
            i2 = i8;
            z2 = z5;
            fjzjVar3 = fjzjVar3;
        }
        return z4;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static long b(fjzj fjzjVar, long j, long j2, long j3, long j4) {
        long j5 = -1;
        if (j == -1) {
            fjzf c = fjzjVar.c("INSERT INTO experiment_states (experiment_token, server_token,\n  serving_version, tokens_tag, config_hash, registration_generation,\n  account_id, config_package_id)\nVALUES (NULL, '', 0, NULL, 0, 0, ?1, ?2);\n");
            Long valueOf = Long.valueOf(j4);
            Long valueOf2 = Long.valueOf(j3);
            long c2 = c.g(valueOf, valueOf2).c();
            fjyx b = ((fjzb) fjzjVar).b("SELECT experiment_state_id\nFROM experiment_states\nWHERE\n  config_package_id = ?1\n  AND account_id = ?2\n  AND experiment_state_id IS NOT ?3\nORDER BY experiment_state_id DESC\nLIMIT 1;\n");
            Long valueOf3 = Long.valueOf(c2);
            fjze i = b.h(valueOf2, valueOf, valueOf3).i();
            if (i != null) {
                try {
                    i.d(0);
                    fjzjVar.c("  INSERT INTO experiment_states_to_partitions\n    (experiment_state_id, ordinal, param_partition_id)\n  SELECT ?1, ordinal, param_partition_id\n  FROM experiment_states_to_partitions\n  WHERE experiment_state_id = ?2;\n").g(valueOf3, Long.valueOf(i.d(0))).d();
                    i.d(0);
                    fjzjVar.c("WITH source AS (\n  SELECT experiment_token, server_token, serving_version, tokens_tag, config_hash,\n    registration_generation\n  FROM experiment_states\n  WHERE experiment_state_id = ?1\n)\nUPDATE experiment_states\nSET experiment_token = (SELECT experiment_token FROM source),\n    server_token = (SELECT server_token FROM source),\n    serving_version = (SELECT serving_version FROM source),\n    tokens_tag = (SELECT tokens_tag FROM source),\n    config_hash = (SELECT config_hash FROM source),\n    registration_generation = (SELECT registration_generation FROM source)\nWHERE experiment_state_id = ?2;\n").g(Long.valueOf(i.d(0)), valueOf3).d();
                } catch (Throwable th) {
                    try {
                        i.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            }
            if (i != null) {
                i.close();
            }
            j = c2;
            j5 = j;
        }
        Long valueOf4 = Long.valueOf(j2);
        fjzjVar.c("    INSERT INTO experiment_states_to_overrides (experiment_state_id, override_id)\n    VALUES (?1, ?2);\n").g(Long.valueOf(j), valueOf4).d();
        return j5;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static void c(fjzj fjzjVar, long j, long j2) {
        if (j == -1 || j2 == -1) {
            return;
        }
        fjyy f = ((fjzb) fjzjVar).b("SELECT override_id\nFROM experiment_states_to_overrides\nINNER JOIN flag_overrides\n  USING (override_id)\nWHERE\n  experiment_state_id = ?1\n  AND active IS 1;\n").h(Long.valueOf(j)).f();
        while (f.b()) {
            try {
                f.d(0);
                fjzjVar.c("    INSERT INTO experiment_states_to_overrides (experiment_state_id, override_id)\n    VALUES (?1, ?2);\n").g(Long.valueOf(j2), Long.valueOf(f.d(0))).d();
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
    }
}
