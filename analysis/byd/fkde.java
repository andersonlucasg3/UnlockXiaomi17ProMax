package defpackage;

import android.content.Context;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.phenotype.Flag;
import com.google.android.gms.phenotype.FlagOverride;
import com.google.android.gms.phenotype.FlagOverrides;
import j$.util.Objects;
import java.util.ArrayList;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes8.dex */
public final class fkde extends fkbs {
    private final fkbb a;
    private final fkdf b;

    public fkde(fkbb fkbbVar, String str, String str2, String str3, String str4) {
        super("ListFlagOverridesOperationCall", jqxk.T);
        blru.s(fkbbVar);
        this.a = fkbbVar;
        this.b = new fkdf(str2, str3, str4, str);
    }

    @Override // defpackage.fkbs
    public final jqwn b() {
        jqwm y = jqwn.a.y();
        String str = this.b.a;
        if (str != null) {
            if (!((jjss) y).b.al()) {
                y.P();
            }
            jqwn jqwnVar = y.b;
            Objects.requireNonNull(str);
            jqwnVar.b |= 1;
            jqwnVar.c = str;
        }
        return y.I();
    }

    @Override // defpackage.fkbs
    public final void i(Context context, fjzr fjzrVar) {
        fjyy f;
        Flag flag;
        fkdf fkdfVar = this.b;
        String str = fkdfVar.c;
        if (str != null && str.endsWith("*")) {
            throw new fjzu(29500, "Prefix searches are no longer supported");
        }
        fjza a = fjzrVar.b().a();
        try {
            fkdfVar.a = fkah.c(fkdfVar.a, fkdfVar.d);
            ArrayList arrayList = new ArrayList();
            if (a.f()) {
                f = ((fjzb) a).b("SELECT\n  COALESCE(flag_overrides.config_package_name, config_packages.name) AS\n    config_package_name,\n  accounts.name AS account_name,\n  flag_overrides.override_id,\n  flag_overrides.name AS override_name,\n  flag_overrides.type AS override_type,\n  flag_overrides.value AS override_value,\n  (\n    EXISTS (\n        SELECT NULL\n        FROM config_packages\n        INNER JOIN experiment_states_to_overrides\n            ON (\n              committed_experiment_state_id IS experiment_state_id\n              AND experiment_states_to_overrides.override_id =\n                flag_overrides.override_id\n            )\n    ) OR EXISTS (\n        SELECT NULL\n        FROM flag_overrides_to_commit\n        WHERE flag_overrides_to_commit.override_id = flag_overrides.override_id\n    )\n  ) AS override_committed\nFROM flag_overrides\nLEFT OUTER JOIN config_packages\n  USING (config_package_id)\nINNER JOIN accounts\n  USING (account_id)\nWHERE\n  (\n    (\n      flag_overrides.config_package_name IS NOT NULL\n      AND IFNULL(flag_overrides.config_package_name = ?1, 1)\n    )\n    OR (\n      config_packages.name IS NOT NULL\n      AND IFNULL(config_packages.name = ?1, 1)\n    )\n  )\n  AND IFNULL(accounts.name = ?2, 1)\n  AND IFNULL(flag_overrides.name = ?3, 1)\n  AND flag_overrides.active IS 1;\n").h(fkdfVar.a, fkdfVar.b, str).d().f();
                while (f.b()) {
                    try {
                        String f2 = f.f(3);
                        boolean z = ((int) f.d(6)) == 1;
                        int d = (int) f.d(4);
                        if (d == 1) {
                            flag = new Flag(f2, f.d(5), 0);
                        } else if (d == 2) {
                            flag = new Flag(f2, f.d(5) == 1, 0);
                        } else if (d == 3) {
                            flag = new Flag(f2, f.c(5), 0);
                        } else if (d == 4) {
                            flag = new Flag(f2, f.f(5), 0);
                        } else {
                            if (d != 5) {
                                throw new IllegalStateException(a.B(f2, "Found flag override with unknown type: "));
                            }
                            flag = new Flag(f2, f.i(5), 0);
                        }
                        arrayList.add(new FlagOverride(f.f(0), f.f(1), flag, z));
                    } finally {
                    }
                }
                f.close();
            } else {
                f = ((fjzb) a).b("SELECT flagType, name, intVal, boolVal, floatVal, stringVal, extensionVal, packageName, user, committed FROM FlagOverrides WHERE IFNULL(packageName = ?, 1) AND IFNULL(user = ?, 1) AND IFNULL(name = ?, 1)").h(fkdfVar.a, fkdfVar.b, fkdfVar.c).d().f();
                while (f.b()) {
                    try {
                        arrayList.add(new FlagOverride(f.f(7), f.f(8), fkcm.d(f), f.d(9) != 0));
                    } finally {
                    }
                }
                f.close();
                a.e();
            }
            FlagOverrides flagOverrides = new FlagOverrides(arrayList);
            a.close();
            this.a.i(Status.b, flagOverrides);
        } catch (Throwable th) {
            try {
                a.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    public final void j(Status status) {
        this.a.i(status, new FlagOverrides(new ArrayList()));
    }
}
