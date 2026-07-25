package defpackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteAccessPermException;
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.database.sqlite.SQLiteBlobTooBigException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteDatatypeMismatchException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteMisuseException;
import android.database.sqlite.SQLiteOutOfMemoryException;
import android.database.sqlite.SQLiteReadOnlyDatabaseException;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTableLockedException;
import android.system.ErrnoException;
import android.system.Os;
import j$.util.Objects;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: :com.google.android.gms@262860035@26.28.60 (260400-948038001) */
/* loaded from: /data/local/tmp/gmsdex/classes8.dex */
public final class fjzr implements Closeable {
    static final bmnk a = bmnk.b("PhenotypeDbHelper", bmbu.aI);
    static final Object b = new Object();
    static volatile fjzr c;
    static volatile fjzr d;
    public static final /* synthetic */ int l = 0;
    public final int e;
    public final Context f;
    public final SQLiteDatabase.OpenParams.Builder h;
    public SQLiteDatabase j;
    private final fkaf m;
    private boolean n;
    private final fkag o;
    public final AtomicInteger i = new AtomicInteger(0);
    int k = 0;
    public final String g = "phenotype.db";

    public fjzr(Context context, int i, fkaf fkafVar, fkag fkagVar) {
        this.f = context;
        SQLiteDatabase.OpenParams.Builder builder = new SQLiteDatabase.OpenParams.Builder();
        this.h = builder;
        builder.addOpenFlags(805306368);
        this.e = i;
        this.m = fkafVar;
        this.o = fkagVar;
    }

    public static int a(SQLiteException sQLiteException) {
        if (sQLiteException instanceof SQLiteAbortException) {
            return 29520;
        }
        if (sQLiteException instanceof SQLiteAccessPermException) {
            return 29521;
        }
        if (sQLiteException instanceof SQLiteBindOrColumnIndexOutOfRangeException) {
            return 29522;
        }
        if (sQLiteException instanceof SQLiteBlobTooBigException) {
            return 29523;
        }
        if (sQLiteException instanceof SQLiteCantOpenDatabaseException) {
            return 29524;
        }
        if (sQLiteException instanceof SQLiteConstraintException) {
            return 29525;
        }
        if (sQLiteException instanceof SQLiteDatabaseCorruptException) {
            return 29526;
        }
        if (sQLiteException instanceof SQLiteDatabaseLockedException) {
            return 29527;
        }
        if (sQLiteException instanceof SQLiteDatatypeMismatchException) {
            return 29528;
        }
        if (sQLiteException instanceof SQLiteDiskIOException) {
            return 29529;
        }
        if (sQLiteException instanceof SQLiteDoneException) {
            return 29530;
        }
        if (sQLiteException instanceof SQLiteFullException) {
            return 29531;
        }
        if (sQLiteException instanceof SQLiteMisuseException) {
            return 29532;
        }
        if (sQLiteException instanceof SQLiteOutOfMemoryException) {
            return 29533;
        }
        if (sQLiteException instanceof SQLiteReadOnlyDatabaseException) {
            return 29534;
        }
        return sQLiteException instanceof SQLiteTableLockedException ? 29535 : 29519;
    }

    static String c(String str, String str2, String... strArr) {
        StringBuilder sb = new StringBuilder("CREATE INDEX IF NOT EXISTS ");
        sb.append(str2);
        sb.append(" ON ");
        sb.append(str);
        sb.append(" (");
        g(sb, strArr);
        sb.append(")");
        return sb.toString();
    }

    static String d(String str, String... strArr) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(str);
        sb.append(" (");
        g(sb, strArr);
        sb.append(")");
        return sb.toString();
    }

    static String f(String... strArr) {
        StringBuilder sb = new StringBuilder(", PRIMARY KEY(");
        g(sb, strArr);
        sb.append(")");
        return sb.toString();
    }

    static void g(StringBuilder sb, String... strArr) {
        boolean z = true;
        int i = 0;
        while (i < strArr.length) {
            String str = strArr[i];
            if (!z) {
                sb.append(", ");
            }
            sb.append(str);
            i++;
            z = false;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x013f A[Catch: all -> 0x0172, DONT_GENERATE, TRY_ENTER, TryCatch #1 {, blocks: (B:4:0x0005, B:14:0x013f, B:15:0x0142, B:120:0x016e, B:121:0x0171, B:117:0x0010, B:7:0x0013, B:9:0x0018, B:11:0x001e, B:12:0x0021, B:18:0x0025, B:20:0x0029, B:84:0x012f, B:86:0x0133, B:88:0x0139, B:93:0x014c, B:95:0x0150, B:97:0x0154, B:99:0x0159, B:101:0x015f, B:102:0x0162, B:114:0x0163, B:115:0x016a), top: B:3:0x0005, inners: #6 }] */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0096 A[Catch: all -> 0x0144, TryCatch #2 {all -> 0x0144, blocks: (B:46:0x008b, B:51:0x0096, B:53:0x009c, B:56:0x00aa, B:59:0x00af, B:60:0x00b2, B:61:0x00b3, B:62:0x00d0, B:63:0x00d1, B:65:0x00d7, B:55:0x009f), top: B:45:0x008b, inners: #7 }] */
    /* JADX WARN: Removed duplicated region for block: B:73:0x00f0 A[Catch: all -> 0x0148, TRY_LEAVE, TryCatch #4 {all -> 0x0148, blocks: (B:25:0x004a, B:29:0x0054, B:33:0x0065, B:38:0x0070, B:44:0x0081, B:71:0x00ea, B:73:0x00f0, B:76:0x00fc, B:79:0x0101, B:80:0x0104, B:81:0x0105, B:82:0x012c, B:83:0x012d, B:106:0x0086, B:107:0x0089, B:75:0x00f3, B:40:0x0073, B:42:0x0077, B:43:0x007e, B:104:0x007b), top: B:24:0x004a, inners: #0, #3 }] */
    /* JADX WARN: Removed duplicated region for block: B:81:0x0105 A[Catch: all -> 0x0148, TryCatch #4 {all -> 0x0148, blocks: (B:25:0x004a, B:29:0x0054, B:33:0x0065, B:38:0x0070, B:44:0x0081, B:71:0x00ea, B:73:0x00f0, B:76:0x00fc, B:79:0x0101, B:80:0x0104, B:81:0x0105, B:82:0x012c, B:83:0x012d, B:106:0x0086, B:107:0x0089, B:75:0x00f3, B:40:0x0073, B:42:0x0077, B:43:0x007e, B:104:0x007b), top: B:24:0x004a, inners: #0, #3 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final android.database.sqlite.SQLiteDatabase k() {
        /*
            Method dump skipped, instructions count: 373
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: defpackage.fjzr.k():android.database.sqlite.SQLiteDatabase");
    }

    private final void l(SQLiteDatabase sQLiteDatabase) {
        j();
        hloe c2 = hlqu.c(285, "PhenotypeDbHelper.onCreate()");
        try {
            m(sQLiteDatabase);
            boolean j = j();
            int i = this.e;
            if (j) {
                if (i >= 1001) {
                    sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS android_packages(\n        android_package_id INTEGER PRIMARY KEY,\n        name TEXT UNIQUE NOT NULL\n    );\n");
                    sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS config_packages(\n      config_package_id INTEGER PRIMARY KEY,\n      name TEXT UNIQUE NOT NULL,\n      android_package_id INTEGER NOT NULL REFERENCES android_packages ON DELETE CASCADE,\n      experiment_state_id INTEGER NOT NULL,\n      flags_content BLOB,\n      experiment_token BLOB,\n      server_token TEXT NOT NULL DEFAULT '',\n      serving_version INTEGER NOT NULL\n    );\n");
                    sQLiteDatabase.execSQL("    CREATE INDEX IF NOT EXISTS config_packages_by_android_package_id ON\n      config_packages(android_package_id);\n");
                    sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS log_sources(\n      log_source_id INTEGER PRIMARY KEY,\n      name TEXT UNIQUE NOT NULL\n    );\n");
                    sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS config_packages_to_log_sources(\n      config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n      log_source_id INTEGER NOT NULL REFERENCES log_sources ON DELETE RESTRICT,\n      PRIMARY KEY (log_source_id, config_package_id),\n      UNIQUE(config_package_id, log_source_id)\n    ) WITHOUT ROWID;\n");
                    sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS cross_logged_tokens(\n      from_config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n      to_config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n      token NOT NULL,\n      diversion_provenance INTEGER NOT NULL\n    );\n");
                    sQLiteDatabase.execSQL("    CREATE INDEX IF NOT EXISTS cross_logged_tokens_by_to_config_package_id ON\n      cross_logged_tokens(to_config_package_id);\n");
                    sQLiteDatabase.execSQL("    CREATE INDEX IF NOT EXISTS cross_logged_tokens_by_from_config_package_id ON\n      cross_logged_tokens(from_config_package_id);\n");
                    sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS flag_overrides(\n      config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n      name TEXT NOT NULL,\n      value NOT NULL,\n      type INTEGER NOT NULL,\n      PRIMARY KEY (config_package_id, name)\n    ) WITHOUT ROWID;\n");
                }
            } else if (i >= 1001) {
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS accounts(\n    account_id INTEGER PRIMARY KEY,\n    name TEXT UNIQUE NOT NULL,\n    CONSTRAINT reservedIds CHECK ((account_id = 0) = (name = ''))\n  );\n");
                sQLiteDatabase.execSQL("INSERT INTO accounts (account_id, name) VALUES (0, '');");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS android_packages(\n    android_package_id INTEGER PRIMARY KEY,\n    name TEXT UNIQUE NOT NULL,\n    last_syncafter_serving_version INTEGER NOT NULL DEFAULT 0,\n    secret BLOB NOT NULL,\n    device_encrypted_secret BLOB NOT NULL,\n    app_wide_properties BLOB\n  );\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS static_config_packages(\n    static_config_package_id INTEGER PRIMARY KEY,\n    name TEXT UNIQUE NOT NULL,\n    CONSTRAINT nameNotEmpty CHECK (name <> '')\n  );\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS config_packages(\n    config_package_id INTEGER PRIMARY KEY,\n    name TEXT UNIQUE NOT NULL,\n    static_config_package_id INTEGER NOT NULL REFERENCES static_config_packages,\n    android_package_id INTEGER NOT NULL REFERENCES android_packages ON DELETE CASCADE,\n    params BLOB,\n    dynamic_params BLOB,\n    declarative_registration_info BLOB,\n    declarative_registration_info_from_client BLOB,\n    heterodyne_info BLOB,\n    runtime_properties BLOB,\n    version INTEGER NOT NULL,\n    baseline_cl INTEGER NOT NULL DEFAULT 0,\n    registration_generation INTEGER NOT NULL DEFAULT 0,\n    last_syncafter_serving_version INTEGER NOT NULL DEFAULT 0,\n    weak_packed_experiment_ids BLOB\n  );\n");
                if (i >= 1034) {
                    sQLiteDatabase.execSQL("CREATE TRIGGER increment_registration_generation\nAFTER UPDATE OF\n  params,\n  dynamic_params,\n  declarative_registration_info,\n  declarative_registration_info_from_client,\n  heterodyne_info,\n  runtime_properties,\n  version,\n  baseline_cl\nON config_packages\nWHEN NOT (\n  OLD.params IS NEW.params\n  AND OLD.dynamic_params IS NEW.dynamic_params\n  AND OLD.declarative_registration_info IS NEW.declarative_registration_info\n  AND OLD.declarative_registration_info_from_client IS\n    NEW.declarative_registration_info_from_client\n  AND OLD.heterodyne_info IS NEW.heterodyne_info\n  AND OLD.runtime_properties IS NEW.runtime_properties\n  AND OLD.version IS NEW.version\n  AND OLD.baseline_cl = NEW.baseline_cl\n) AND OLD.registration_generation = NEW.registration_generation\nBEGIN\n  UPDATE config_packages\n  SET registration_generation = NEW.registration_generation + 1\n  WHERE config_package_id = NEW.config_package_id;\nEND;\n");
                } else {
                    sQLiteDatabase.execSQL("CREATE TRIGGER increment_registration_generation\nAFTER UPDATE OF\n  params,\n  declarative_registration_info,\n  declarative_registration_info_from_client,\n  heterodyne_info,\n  runtime_properties,\n  version,\n  baseline_cl\nON config_packages\nWHEN NOT (\n  OLD.params IS NEW.params\n  AND OLD.dynamic_params IS NEW.dynamic_params\n  AND OLD.declarative_registration_info IS NEW.declarative_registration_info\n  AND OLD.declarative_registration_info_from_client IS\n    NEW.declarative_registration_info_from_client\n  AND OLD.heterodyne_info IS NEW.heterodyne_info\n  AND OLD.runtime_properties IS NEW.runtime_properties\n  AND OLD.version IS NEW.version\n  AND OLD.baseline_cl = NEW.baseline_cl\n) AND OLD.registration_generation = NEW.registration_generation\nBEGIN\n  UPDATE config_packages\n  SET registration_generation = NEW.registration_generation + 1\n  WHERE config_package_id = NEW.config_package_id;\nEND;\n");
                }
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS config_packages_by_android_package_id ON\n    config_packages(android_package_id);\n");
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS config_packages_by_static_config_package_id ON\n    config_packages(static_config_package_id);\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS experiment_states(\n    experiment_state_id INTEGER PRIMARY KEY AUTOINCREMENT,\n    experiment_token BLOB,\n    server_token TEXT NOT NULL DEFAULT '',\n    serving_version INTEGER NOT NULL,\n    tokens_tag BLOB,\n    config_hash INTEGER DEFAULT 0,\n    registration_generation INTEGER NOT NULL,\n    account_id INTEGER NOT NULL REFERENCES accounts ON DELETE CASCADE,\n    config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE\n  );\n");
                sQLiteDatabase.execSQL("CREATE INDEX experiment_states_ids ON experiment_states(\n  config_package_id,\n  account_id,\n  experiment_state_id DESC\n);\n");
                sQLiteDatabase.execSQL("  ALTER TABLE config_packages ADD COLUMN committed_experiment_state_id INTEGER REFERENCES\n      experiment_states ON DELETE SET NULL;\n");
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS config_packages_by_committed_experiment_state_id\n    ON config_packages(committed_experiment_state_id);\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS param_partitions(\n    param_partition_id INTEGER PRIMARY KEY,\n    static_config_package_id INTEGER NOT NULL REFERENCES static_config_packages ON\n      DELETE CASCADE,\n    tag BLOB NOT NULL,\n    flags_content BLOB NOT NULL\n  );\n");
                sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS param_partitions_by_static_config_package_id_tag ON\n  param_partitions(static_config_package_id, tag);\n");
                sQLiteDatabase.execSQL("  CREATE TRIGGER param_partition_unique BEFORE INSERT ON param_partitions\n    WHEN EXISTS(\n        SELECT NULL\n        FROM param_partitions AS existing\n        WHERE\n          existing.static_config_package_id = NEW.static_config_package_id\n          AND existing.tag = NEW.tag\n          AND existing.flags_content = NEW.flags_content\n    )\n    BEGIN\n      SELECT RAISE(FAIL,\n        'Unique constraint on (static_config_package_id, tag, flags_content) failed)');\n    END;\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS experiment_states_to_partitions(\n    experiment_state_id INTEGER NOT NULL REFERENCES experiment_states ON DELETE CASCADE,\n    ordinal INTEGER NOT NULL,\n    param_partition_id INTEGER NOT NULL REFERENCES param_partitions,\n    PRIMARY KEY(experiment_state_id, param_partition_id)\n  ) WITHOUT ROWID;\n");
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS experiment_states_to_partitions_by_param_partition_id ON\n    experiment_states_to_partitions(param_partition_id);\n");
                sQLiteDatabase.execSQL("CREATE TRIGGER delete_unused_param_partitions_for_experiment_states\nAFTER DELETE ON experiment_states_to_partitions\nWHEN OLD.param_partition_id NOT IN (\n  SELECT param_partition_id FROM experiment_states_to_partitions\n)\nBEGIN\n  DELETE FROM param_partitions WHERE param_partition_id = OLD.param_partition_id;\nEND;\n");
                sQLiteDatabase.execSQL("      CREATE TABLE IF NOT EXISTS log_sources(\n        log_source_id INTEGER PRIMARY KEY,\n        name TEXT UNIQUE NOT NULL\n      );\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS config_packages_to_log_sources(\n    config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n    log_source_id INTEGER NOT NULL REFERENCES log_sources ON DELETE RESTRICT,\n    PRIMARY KEY (log_source_id, config_package_id),\n    UNIQUE(config_package_id, log_source_id)\n  ) WITHOUT ROWID;\n");
                sQLiteDatabase.execSQL("        CREATE TRIGGER delete_unused_log_sources_for_config_packages\n        AFTER DELETE ON config_packages_to_log_sources\n        WHEN\n          OLD.log_source_id NOT IN (SELECT log_source_id FROM config_packages_to_log_sources)\n          AND OLD.log_source_id NOT IN (\n              SELECT log_source_id FROM external_experiments_to_log_sources\n          )\n        BEGIN\n          DELETE FROM log_sources WHERE log_source_id = OLD.log_source_id;\n        END;\n");
                sQLiteDatabase.execSQL("      CREATE TABLE IF NOT EXISTS cross_logged_tokens(\n        experiment_state_id INTEGER NOT NULL REFERENCES experiment_states ON DELETE CASCADE,\n        to_config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n        token NOT NULL,\n        diversion_provenance INTEGER NOT NULL\n      );\n");
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS cross_logged_tokens_by_experiment_state_id ON\n    cross_logged_tokens(experiment_state_id);\n");
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS cross_logged_tokens_by_to_config_package_id ON\n    cross_logged_tokens(to_config_package_id);\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS external_experiments(\n    namespace_id INTEGER PRIMARY KEY,\n    namespace TEXT NOT NULL UNIQUE,\n    android_package_id INTEGER NOT NULL REFERENCES android_packages ON DELETE CASCADE,\n    packed_experiments BLOB,\n    external_token BLOB\n  );\n");
                sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS external_experiments_by_android_package_id ON\n    external_experiments(android_package_id);\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS external_experiments_to_log_sources(\n    namespace_id INTEGER NOT NULL REFERENCES external_experiments ON DELETE CASCADE,\n    log_source_id INTEGER NOT NULL REFERENCES log_sources ON DELETE RESTRICT,\n    PRIMARY KEY (log_source_id, namespace_id),\n    UNIQUE(namespace_id, log_source_id)\n  ) WITHOUT ROWID;\n");
                sQLiteDatabase.execSQL("        CREATE TRIGGER delete_unused_log_sources_for_external_experiments\n        AFTER DELETE ON external_experiments_to_log_sources\n        WHEN\n          OLD.log_source_id NOT IN (SELECT log_source_id FROM config_packages_to_log_sources)\n          AND OLD.log_source_id NOT IN (\n            SELECT log_source_id FROM external_experiments_to_log_sources\n          )\n        BEGIN\n          DELETE FROM log_sources WHERE log_source_id = OLD.log_source_id;\n        END;\n");
                if (i >= 1035) {
                    sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS flag_overrides(\n    override_id INTEGER PRIMARY KEY,\n    config_package_id INTEGER REFERENCES config_packages ON DELETE CASCADE,\n    config_package_name TEXT,\n    account_id INTEGER NOT NULL REFERENCES accounts ON DELETE CASCADE,\n    active INTEGER DEFAULT 1,\n    name TEXT NOT NULL,\n    value NOT NULL,\n    type INTEGER NOT NULL,\n    source INTEGER DEFAULT 0,\n    UNIQUE (config_package_id, account_id, active, name),\n    UNIQUE (config_package_name, account_id, active, name),\n    CHECK ((active IS NULL) OR (active IS 1)),\n    CHECK ((config_package_id IS NULL) != (config_package_name IS NULL))\n  );\n");
                } else {
                    sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS flag_overrides(\n    override_id INTEGER PRIMARY KEY,\n    config_package_id INTEGER REFERENCES config_packages ON DELETE CASCADE,\n    config_package_name TEXT,\n    account_id INTEGER NOT NULL REFERENCES accounts ON DELETE CASCADE,\n    active INTEGER DEFAULT 1,\n    name TEXT NOT NULL,\n    value NOT NULL,\n    type INTEGER NOT NULL,\n    UNIQUE (config_package_id, account_id, active, name),\n    UNIQUE (config_package_name, account_id, active, name),\n    CHECK ((active IS NULL) OR (active IS 1)),\n    CHECK ((config_package_id IS NULL) != (config_package_name IS NULL))\n  );\n");
                }
                sQLiteDatabase.execSQL("        CREATE TABLE IF NOT EXISTS experiment_states_to_overrides(\n          experiment_state_id INTEGER NOT NULL REFERENCES experiment_states ON DELETE CASCADE,\n          override_id INTEGER NOT NULL REFERENCES flag_overrides,\n          PRIMARY KEY(experiment_state_id, override_id),\n          UNIQUE(override_id, experiment_state_id)\n        ) WITHOUT ROWID;\n");
                sQLiteDatabase.execSQL("        CREATE TRIGGER delete_unused_flag_overrides_for_experiment_states\n        AFTER DELETE ON experiment_states_to_overrides\n        WHEN\n          OLD.override_id NOT IN (SELECT override_id FROM experiment_states_to_overrides)\n          AND OLD.override_id NOT IN (\n            SELECT override_id FROM flag_overrides WHERE active IS 1\n          )\n        BEGIN\n          DELETE FROM flag_overrides WHERE override_id = OLD.override_id;\n        END;\n");
                sQLiteDatabase.execSQL("    CREATE TABLE IF NOT EXISTS flag_overrides_to_commit(\n      override_id INTEGER NOT NULL REFERENCES flag_overrides ON DELETE CASCADE,\n      config_package_id INTEGER NOT NULL REFERENCES config_packages ON DELETE CASCADE,\n      account_id INTEGER NOT NULL REFERENCES accounts ON DELETE CASCADE,\n      PRIMARY KEY(override_id, config_package_id, account_id),\n      UNIQUE(config_package_id, account_id, override_id)\n    ) WITHOUT ROWID;\n");
                sQLiteDatabase.execSQL("        CREATE TABLE IF NOT EXISTS last_fetch(\n          type INTEGER PRIMARY KEY,\n          serving_version INTEGER NOT NULL\n        );\n");
                sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS dogfood_token(\n    token_key INTEGER PRIMARY KEY CHECK (token_key = 0),\n    token BLOB\n  );\n");
                sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS config_packages_to_experiment_states_for_migration(\n  config_package_id INTEGER NOT NULL PRIMARY KEY REFERENCES config_packages ON DELETE\n    CASCADE,\n  experiment_state_id INTEGER NOT NULL REFERENCES experiment_states ON DELETE CASCADE,\n  UNIQUE(experiment_state_id, config_package_id)\n);\n");
            }
            c2.close();
        } catch (Throwable th) {
            try {
                c2.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private final void m(SQLiteDatabase sQLiteDatabase) {
        if (this.e < 1036) {
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS Packages(\n    packageName TEXT NOT NULL PRIMARY KEY,\n    version INTEGER NOT NULL,\n    params BLOB,\n    dynamicParams BLOB,\n    weak INTEGER NOT NULL,\n    androidPackageName TEXT NOT NULL,\n    isSynced INTEGER,\n    serializedDeclarativeRegInfo BLOB DEFAULT NULL,\n    configTier INTEGER DEFAULT NULL,\n    baselineCl INTEGER DEFAULT NULL,\n    heterodyneInfo BLOB DEFAULT NULL,\n    runtimeProperties BLOB DEFAULT NULL,\n    declarativeRegistrationInfo BLOB DEFAULT NULL\n  )\n");
            sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS androidPackageName ON Packages (androidPackageName)");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS ApplicationStates(\n    packageName TEXT NOT NULL PRIMARY KEY,\n    user TEXT NOT NULL,\n    version INTEGER NOT NULL,\n    patchable INTEGER\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS MultiCommitApplicationStates(\n    packageName TEXT NOT NULL,\n    user TEXT NOT NULL,\n    version INTEGER NOT NULL,\n    PRIMARY KEY(packageName, user)\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS LogSources(\n    logSourceName TEXT NOT NULL,\n    packageName TEXT NOT NULL,\n    PRIMARY KEY(logSourceName, packageName)\n    )\n");
            sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS packageName ON LogSources(packageName)");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS WeakExperimentIds(\n    packageName TEXT NOT NULL,\n    experimentId INTEGER NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS ExperimentTokens(\n    packageName TEXT NOT NULL,\n    version INTEGER NOT NULL,\n    user TEXT NOT NULL,\n    isCommitted INTEGER NOT NULL,\n    experimentToken BLOB NOT NULL,\n    serverToken TEXT NOT NULL,\n    configHash TEXT NOT NULL DEFAULT '',\n    servingVersion INTEGER NOT NULL DEFAULT 0,\n    tokensTag BLOB DEFAULT NULL,\n    flagsHash INTEGER DEFAULT NULL,\n    PRIMARY KEY(packageName, version, user, isCommitted)\n  )\n");
            sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS committed ON ExperimentTokens(packageName, version, user, isCommitted)");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS ExternalExperimentTokens(\n    packageName TEXT NOT NULL PRIMARY KEY,\n    experimentToken BLOB NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS Flags(\n    packageName TEXT NOT NULL,\n    version INTEGER NOT NULL,\n    flagType INTEGER NOT NULL,\n    partitionId INTEGER NOT NULL,\n    user TEXT NOT NULL,\n    name TEXT NOT NULL,\n    intVal INTEGER,\n    boolVal INTEGER,\n    floatVal REAL,\n    stringVal TEXT,\n    extensionVal BLOB,\n    committed INTEGER NOT NULL,\n    PRIMARY KEY(packageName, version, flagType, partitionId, user, name, committed)\n  );\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS RequestTags(\n    user TEXT NOT NULL PRIMARY KEY,\n    bytesTag BLOB NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS ApplicationTags(\n    packageName TEXT NOT NULL,\n    version INTEGER NOT NULL,\n    partitionId INTEGER NOT NULL,\n    user TEXT NOT NULL,\n    tag BLOB NOT NULL,\n    PRIMARY KEY(packageName, version, partitionId, user)\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS CrossLoggedExperimentTokens(\n    fromPackageName TEXT NOT NULL,\n    fromVersion INTEGER NOT NULL,\n    fromUser TEXT NOT NULL,\n    toPackageName TEXT NOT NULL,\n    toVersion INTEGER NOT NULL,\n    isCommitted INTEGER NOT NULL,\n    token BLOB NOT NULL,\n    provenance INTEGER NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE INDEX IF NOT EXISTS apply ON CrossLoggedExperimentTokens(\n    fromPackageName,\n    fromVersion,\n    fromUser,\n    toPackageName,\n    toVersion,\n    isCommitted\n  )\n");
            sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS remove ON CrossLoggedExperimentTokens(toPackageName)");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS ChangeCounts(\n    packageName TEXT NOT NULL PRIMARY KEY,\n    count INTEGER NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS DogfoodsToken(\n    key INTEGER NOT NULL PRIMARY KEY,\n    token BLOB\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS LastFetch(\n    key INTEGER NOT NULL PRIMARY KEY,\n    servertimestamp INTEGER NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS FlagOverrides(\n    packageName TEXT NOT NULL,\n    user TEXT NOT NULL,\n    name TEXT NOT NULL,\n    flagType INTEGER NOT NULL,\n    intVal INTEGER,\n    boolVal INTEGER,\n    floatVal REAL,\n    stringVal TEXT,\n    extensionVal BLOB,\n    committed,\n    PRIMARY KEY(packageName, user, name, committed)\n  );\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS LastSyncAfterRequest(\n    packageName TEXT NOT NULL PRIMARY KEY,\n    servingVersion INTEGER NOT NULL DEFAULT 0,\n    androidPackageName TEXT DEFAULT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE IF NOT EXISTS StorageInfos (\n    androidPackageName TEXT UNIQUE NOT NULL,\n    secret BLOB NOT NULL,\n    deviceEncryptedSecret BLOB NOT NULL\n  )\n");
            sQLiteDatabase.execSQL("  CREATE TABLE AppWideProperties (\n    androidPackageName TEXT UNIQUE NOT NULL,\n    appWideProperties BLOB NOT NULL\n  );\n");
        }
    }

    private final void n(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.beginTransaction();
        try {
            hloe c2 = hlqu.c(286, "PhenotypeDbHelper.onDowngrade()");
            try {
                a.h().E("onDowngrade %d to %d", i, i2);
                if (i2 < 1036 && i >= 1036) {
                    m(sQLiteDatabase);
                }
                if (!j()) {
                    if (i2 < 1034 && i >= 1034) {
                        sQLiteDatabase.execSQL("DROP TRIGGER IF EXISTS increment_registration_generation;");
                        sQLiteDatabase.execSQL("CREATE TRIGGER increment_registration_generation\nAFTER UPDATE OF\n  params,\n  declarative_registration_info,\n  declarative_registration_info_from_client,\n  heterodyne_info,\n  runtime_properties,\n  version,\n  baseline_cl\nON config_packages\nWHEN NOT (\n  OLD.params IS NEW.params\n  AND OLD.dynamic_params IS NEW.dynamic_params\n  AND OLD.declarative_registration_info IS NEW.declarative_registration_info\n  AND OLD.declarative_registration_info_from_client IS\n    NEW.declarative_registration_info_from_client\n  AND OLD.heterodyne_info IS NEW.heterodyne_info\n  AND OLD.runtime_properties IS NEW.runtime_properties\n  AND OLD.version IS NEW.version\n  AND OLD.baseline_cl = NEW.baseline_cl\n) AND OLD.registration_generation = NEW.registration_generation\nBEGIN\n  UPDATE config_packages\n  SET registration_generation = NEW.registration_generation + 1\n  WHERE config_package_id = NEW.config_package_id;\nEND;\n");
                    }
                    if (i2 < 1035 && i >= 1035) {
                        sQLiteDatabase.execSQL("CREATE TEMP TABLE IF NOT EXISTS flag_overrides_temp(\n  override_id INTEGER PRIMARY KEY,\n  config_package_id,\n  config_package_name TEXT,\n  account_id INTEGER NOT NULL,\n  active INTEGER DEFAULT 1,\n  name TEXT NOT NULL,\n  value NOT NULL,\n  type INTEGER NOT NULL,\n  UNIQUE (config_package_id, account_id, active, name),\n  UNIQUE (config_package_name, account_id, active, name),\n  CHECK ((active IS NULL) OR (active IS 1)),\n  CHECK ((config_package_id IS NULL) != (config_package_name IS NULL))\n);\n");
                        sQLiteDatabase.execSQL("CREATE TEMP TABLE IF NOT EXISTS experiment_states_to_overrides_temp(\n  experiment_state_id INTEGER NOT NULL,\n  override_id INTEGER NOT NULL,\n  PRIMARY KEY(experiment_state_id, override_id),\n  UNIQUE(override_id, experiment_state_id)\n) WITHOUT ROWID;\n");
                        sQLiteDatabase.execSQL("CREATE TEMP TABLE IF NOT EXISTS flag_overrides_to_commit_temp(\n  override_id INTEGER NOT NULL,\n  config_package_id INTEGER NOT NULL,\n  account_id INTEGER NOT NULL,\n  PRIMARY KEY(override_id, config_package_id, account_id),\n  UNIQUE(config_package_id, account_id, override_id)\n) WITHOUT ROWID;\n");
                        sQLiteDatabase.execSQL("INSERT INTO flag_overrides_temp\nSELECT override_id,\n  config_package_id,\n  config_package_name,\n  account_id,\n  active,\n  name,\n  value,\n  type\nFROM flag_overrides;\n");
                        sQLiteDatabase.execSQL("INSERT INTO experiment_states_to_overrides_temp\nSELECT experiment_state_id,\n  override_id\nFROM experiment_states_to_overrides;\n");
                        sQLiteDatabase.execSQL("INSERT INTO flag_overrides_to_commit_temp\nSELECT override_id,\n  config_package_id,\n  account_id\nFROM flag_overrides_to_commit;\n");
                        sQLiteDatabase.execSQL("DELETE FROM experiment_states_to_overrides;");
                        sQLiteDatabase.execSQL("DELETE FROM flag_overrides_to_commit;");
                        sQLiteDatabase.execSQL("DELETE FROM flag_overrides;");
                        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS flag_overrides;");
                        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS flag_overrides(\n  override_id INTEGER PRIMARY KEY,\n  config_package_id INTEGER REFERENCES config_packages ON DELETE CASCADE,\n  config_package_name TEXT,\n  account_id INTEGER NOT NULL REFERENCES accounts ON DELETE CASCADE,\n  active INTEGER DEFAULT 1,\n  name TEXT NOT NULL,\n  value NOT NULL,\n  type INTEGER NOT NULL,\n  UNIQUE (config_package_id, account_id, active, name),\n  UNIQUE (config_package_name, account_id, active, name),\n  CHECK ((active IS NULL) OR (active IS 1)),\n  CHECK ((config_package_id IS NULL) != (config_package_name IS NULL))\n);\n");
                        sQLiteDatabase.execSQL("INSERT INTO flag_overrides\nSELECT override_id,\n  config_package_id,\n  config_package_name,\n  account_id,\n  active,\n  name,\n  value,\n  type\nFROM flag_overrides_temp;\n");
                        sQLiteDatabase.execSQL("INSERT INTO experiment_states_to_overrides\nSELECT experiment_state_id,\n  override_id\nFROM experiment_states_to_overrides_temp;\n");
                        sQLiteDatabase.execSQL("INSERT INTO flag_overrides_to_commit\nSELECT override_id,\n  config_package_id,\n  account_id\nFROM flag_overrides_to_commit_temp;\n");
                        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS experiment_states_to_overrides_temp;");
                        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS flag_overrides_to_commit_temp;");
                        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS flag_overrides_temp;");
                    }
                    if (i2 < 1001 && i >= 1001) {
                        r(sQLiteDatabase);
                    }
                } else if (i2 < 1001 && i >= 1001) {
                    s(sQLiteDatabase);
                }
                if (i2 >= 1036) {
                    Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT name\nFROM sqlite_master\nWHERE\n  type = 'table'\n  AND name <> 'android_metadata'\n  AND name NOT LIKE 'sqlite_%';\n", null);
                    while (rawQuery.moveToNext()) {
                        try {
                            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + rawQuery.getString(0));
                        } finally {
                        }
                    }
                    if (rawQuery != null) {
                        rawQuery.close();
                    }
                    l(sQLiteDatabase);
                }
                sQLiteDatabase.setVersion(i2);
                sQLiteDatabase.setTransactionSuccessful();
                c2.close();
            } finally {
            }
        } finally {
            sQLiteDatabase.endTransaction();
            if (i >= 1001 && i2 < 1001) {
                this.i.set(14904026);
            }
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:106:0x05e5, code lost:
    
        if (r2 == 1033) goto L114;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private final void o(android.database.sqlite.SQLiteDatabase r46, int r47, int r48) {
        /*
            Method dump skipped, instructions count: 1958
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: defpackage.fjzr.o(android.database.sqlite.SQLiteDatabase, int, int):void");
    }

    private static void p(String str) {
        try {
            Os.chmod(str, 432);
        } catch (ErrnoException e) {
            a.e(a.j(), "Failed to chmod(%s): ", str, e);
        }
    }

    private final void q(SQLiteDatabase sQLiteDatabase, int i) {
        if (sQLiteDatabase.getVersion() >= 1001) {
            this.i.set(14904024);
        } else if (i >= 1001) {
            this.i.set(14904025);
        }
    }

    private static final void r(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS schema_upgrade_attempts;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS dogfood_token;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS last_fetch;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS config_packages_to_experiment_states_for_migration;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS cross_logged_tokens;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS experiment_states_to_overrides;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS experiment_states_to_partitions;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS flag_overrides_to_commit;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS config_packages_to_log_sources;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS external_experiments_to_log_sources;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS external_experiments;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS log_sources;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS flag_overrides;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS param_partitions;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS experiment_states;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS accounts;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS config_packages;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS static_config_packages;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS android_packages;");
    }

    private static final void s(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS schema_upgrade_attempts;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS config_packages_to_log_sources;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS cross_logged_tokens;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS flag_overrides;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS config_packages;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS log_sources;");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS android_packages;");
    }

    public final fjzl b() {
        return new fjzl(k(), this.m, this.o);
    }

    public final String e() {
        SQLiteStatement compileStatement = k().compileStatement("PRAGMA integrity_check;");
        try {
            String simpleQueryForString = compileStatement.simpleQueryForString();
            if (compileStatement != null) {
                compileStatement.close();
            }
            return simpleQueryForString;
        } catch (Throwable th) {
            if (compileStatement != null) {
                try {
                    compileStatement.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    public final void h() {
        SQLiteDatabase k = k();
        if (k == null) {
            return;
        }
        int version = k.getVersion();
        if (k.isReadOnly()) {
            a.j().E("Can't downgrade read-only database from version %d to %d.", version, this.e);
            return;
        }
        int i = this.e;
        if (version <= i || version < 1001) {
            return;
        }
        n(k, version, i);
    }

    public final void i() {
        SQLiteDatabase k = k();
        try {
            if (k.isReadOnly()) {
                howt j = a.j();
                int version = k.getVersion();
                int i = this.e;
                j.E("Can't upgrade read-only database from version %d to %d.", version, i);
                q(k, i);
                return;
            }
            k.beginTransaction();
            try {
                int i2 = this.e;
                boolean z = false;
                if (i2 > k.getVersion()) {
                    if (k.getVersion() < 1001 && i2 >= 1001) {
                        k.execSQL("CREATE TABLE IF NOT EXISTS schema_upgrade_attempts (\n    target_schema_version INTEGER PRIMARY KEY,\n    attempts INTEGER NOT NULL\n);\n");
                        Cursor rawQuery = k.rawQuery("SELECT attempts FROM schema_upgrade_attempts WHERE target_schema_version = ?;", new String[]{String.valueOf(i2)});
                        try {
                            long j2 = 0;
                            if (rawQuery.moveToFirst() && !rawQuery.isNull(0)) {
                                j2 = rawQuery.getLong(0);
                            }
                            if (rawQuery != null) {
                                rawQuery.close();
                            }
                            if (j2 < kdtm.b()) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("target_schema_version", Integer.valueOf(this.e));
                                contentValues.put("attempts", Long.valueOf(j2 + 1));
                                k.insertWithOnConflict("schema_upgrade_attempts", null, contentValues, 5);
                            } else {
                                a.i().I("Phixit migration attempt limit (%d) has been reached error. EventCode: %s", kdtm.b(), 48);
                                jjss y = bmcv.a.y();
                                bmdd bmddVar = bmdd.kq;
                                if (!y.b.al()) {
                                    y.P();
                                }
                                y.b.b = bmddVar.a();
                                if (!y.b.al()) {
                                    y.P();
                                }
                                bmcv bmcvVar = y.b;
                                bmcvVar.c = 1;
                                bmcz bmczVar = bmcz.c;
                                if (!bmcvVar.al()) {
                                    y.P();
                                }
                                y.b.d = bmczVar.a();
                                bmcv I = y.I();
                                Context context = this.f;
                                List list = bjla.n;
                                bjkz i3 = new bjkq(context, "PHENOTYPE").a().i(jqxl.a, gmgi.b(context, new kjwf()));
                                i3.n(48);
                                ((bjku) i3).m = I;
                                i3.d();
                                k.setTransactionSuccessful();
                            }
                        } finally {
                        }
                    }
                    z = true;
                    k.setTransactionSuccessful();
                }
                if (z) {
                    k.beginTransaction();
                    int version2 = k.getVersion();
                    try {
                        int i4 = this.e;
                        o(k, version2, i4);
                        k.setVersion(i4);
                        k.setTransactionSuccessful();
                        k.endTransaction();
                        if (version2 < 1036 && this.e >= 1036) {
                            try {
                                hloe c2 = hlqu.c(284, "PhenotypeDbHelper.compactAndVacuum()");
                                try {
                                    Cursor rawQuery2 = k.rawQuery("PRAGMA wal_checkpoint=TRUNCATE", null);
                                    if (rawQuery2 != null) {
                                        try {
                                            if (rawQuery2.moveToNext()) {
                                                rawQuery2.close();
                                                k.execSQL("VACUUM");
                                                rawQuery2 = k.rawQuery("PRAGMA wal_checkpoint=TRUNCATE", null);
                                                if (rawQuery2 != null) {
                                                    try {
                                                        if (rawQuery2.moveToNext()) {
                                                            rawQuery2.close();
                                                            c2.close();
                                                        }
                                                    } finally {
                                                    }
                                                }
                                                throw new SQLiteException("wal_checkpoint=TRUNCATE failed");
                                            }
                                        } finally {
                                        }
                                    }
                                    throw new SQLiteException("wal_checkpoint=TRUNCATE failed");
                                } catch (Throwable th) {
                                    try {
                                        c2.close();
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                    }
                                    throw th;
                                }
                            } catch (SQLiteException e) {
                                a.i().q(e).y("Failed to compact and vacuum %s database", new gsxe(true != j() ? "CE" : "DE"));
                                jjss y2 = jqxh.a.y();
                                String name = e.getClass().getName();
                                if (!y2.b.al()) {
                                    y2.P();
                                }
                                jqxh jqxhVar = y2.b;
                                Objects.requireNonNull(name);
                                jqxhVar.b |= 1;
                                jqxhVar.c = name;
                                int a2 = a(e);
                                if (!y2.b.al()) {
                                    y2.P();
                                }
                                jqxh jqxhVar2 = y2.b;
                                jqxhVar2.b |= 2;
                                jqxhVar2.d = a2;
                                String message = e.getMessage();
                                if (message != null) {
                                    if (!y2.b.al()) {
                                        y2.P();
                                    }
                                    jqxh jqxhVar3 = y2.b;
                                    Objects.requireNonNull(message);
                                    jqxhVar3.b |= 4;
                                    jqxhVar3.e = message;
                                }
                                jjss y3 = jqxg.a.y();
                                if (!y3.b.al()) {
                                    y3.P();
                                }
                                jqxg jqxgVar = y3.b;
                                jqxg jqxgVar2 = jqxgVar;
                                jqxgVar2.b |= 1;
                                jqxgVar2.c = version2;
                                if (!jqxgVar.al()) {
                                    y3.P();
                                }
                                jqxg jqxgVar3 = y3.b;
                                jqxgVar3.b |= 2;
                                jqxgVar3.d = 1036L;
                                jqxg I2 = y3.I();
                                if (j()) {
                                    if (!y2.b.al()) {
                                        y2.P();
                                    }
                                    jqxh jqxhVar4 = y2.b;
                                    Objects.requireNonNull(I2);
                                    jqxhVar4.i = I2;
                                    jqxhVar4.b |= 64;
                                } else {
                                    if (!y2.b.al()) {
                                        y2.P();
                                    }
                                    jqxh jqxhVar5 = y2.b;
                                    Objects.requireNonNull(I2);
                                    jqxhVar5.h = I2;
                                    jqxhVar5.b |= 32;
                                }
                                jjss y4 = bmcv.a.y();
                                bmdd bmddVar2 = bmdd.kq;
                                if (!y4.b.al()) {
                                    y4.P();
                                }
                                y4.b.b = bmddVar2.a();
                                if (!y4.b.al()) {
                                    y4.P();
                                }
                                bmcv bmcvVar2 = y4.b;
                                bmcvVar2.c = 1;
                                bmcz bmczVar2 = bmcz.c;
                                if (!bmcvVar2.al()) {
                                    y4.P();
                                }
                                y4.b.d = bmczVar2.a();
                                bmcv I3 = y4.I();
                                Context context2 = this.f;
                                List list2 = bjla.n;
                                bjla a3 = new bjkq(context2, "PHENOTYPE").a();
                                jqxi y5 = jqxl.a.y();
                                if (!((jjss) y5).b.al()) {
                                    y5.P();
                                }
                                jqxl jqxlVar = y5.b;
                                jqxh I4 = y2.I();
                                Objects.requireNonNull(I4);
                                jqxlVar.e = I4;
                                jqxlVar.b |= 8;
                                bjkz i5 = a3.i(y5.I(), gmgi.b(context2, new kjwf()));
                                i5.n(31);
                                ((bjku) i5).m = I3;
                                i5.d();
                            }
                        }
                    } finally {
                    }
                }
            } finally {
            }
        } finally {
            q(k, this.e);
        }
    }

    public final boolean j() {
        return this == c;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public final void close() {
    }
}
