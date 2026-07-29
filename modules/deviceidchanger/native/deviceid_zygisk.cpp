/* DeviceID+ zygisk module — per-app system-property spoofing.
 *
 * preAppSpecialize: match the app's package name (nice_name) against the
 * flat config ".perapp_props" in the module dir (lines "pkg|key=value") and
 * load matching entries into the static spoof table. Non-target apps get
 * DLCLOSE_MODULE_LIBRARY so nothing of this module stays mapped.
 * postAppSpecialize (matched apps only): install GOT/PLT hooks that divert
 * the bionic property readers to the spoof table.
 *
 * The library is never dlclose-d in matched processes: the installed hooks
 * point into this .so. */

#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <android/log.h>

#include "zygisk.hpp"
#include "perapp_hooks.h"
#include "dck_hook.h"

#define LOG_TAG "DeviceIDPlus"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define CONFIG_NAME ".perapp_props"
#define CONFIG_FALLBACK_PATH "/data/adb/modules/deviceidchanger/.perapp_props"

class DeviceIDModule : public zygisk::ModuleBase {
public:
    void onLoad(zygisk::Api *api, JNIEnv *env) override {
        this->api = api;
        this->env = env;
    }

    void preAppSpecialize(zygisk::AppSpecializeArgs *args) override {
        bool matched = false;
        const char *pkg = NULL;
        if (args->nice_name) pkg = env->GetStringUTFChars(args->nice_name, NULL);
        if (pkg && pkg[0]) matched = load_config(pkg);
        if (pkg) env->ReleaseStringUTFChars(args->nice_name, pkg);

        if (!matched) {
            /* Not a target app: vanish completely. */
            api->setOption(zygisk::DLCLOSE_MODULE_LIBRARY);
        }
    }

    void postAppSpecialize(const zygisk::AppSpecializeArgs *) override {
        /* Only reached for matched packages (table loaded above).
         * COW first: it covers every read path (JNI, native, direct prop-area
         * parsing) with process-local pages; GOT hooks stay as a complement
         * for dynamically linked native readers. */
        int cow = perapp_cow_apply();
        if (cow > 0) LOGI("cow: %d prop(s) applied", cow);
        perapp_install_hooks();
        spoof_build_fields();
        if (dck_hook_enabled_) {
            JavaVM *vm = NULL;
            if (env->GetJavaVM(&vm) == JNI_OK && vm) {
                LOGI("byd-dck: dck.hook=1 in config, starting lazy hook");
                dck_hook_start(vm);
            }
        }
    }

    void preServerSpecialize(zygisk::ServerSpecializeArgs *) override {
        api->setOption(zygisk::DLCLOSE_MODULE_LIBRARY);
    }

private:
    zygisk::Api *api;
    JNIEnv *env;
    bool dck_hook_enabled_ = false; /* config key "dck.hook=1" (BYD only) */

    /* Rewrite the matching static fields of android.os.Build with the spoof
     * values. The Build class is initialized once in the zygote, so every app
     * inherits the REAL device values in its Java fields — prop-level spoofing
     * (COW/hooks) never reaches them. Apps that check compatibility through
     * Build.MODEL & friends (or a WebView user-agent, which is derived from
     * Build.*) need this JNI rewrite. Runs in postSpecialize, before any app
     * code. Same technique as PlayIntegrityFix. */
    void spoof_build_fields() {
        static const struct { const char *key; const char *field; } kMap[] = {
            { "ro.product.brand",        "BRAND" },
            { "ro.product.manufacturer", "MANUFACTURER" },
            { "ro.product.model",        "MODEL" },
            { "ro.product.device",       "DEVICE" },
            { "ro.product.name",         "PRODUCT" },
            { "ro.product.board",        "BOARD" },
            { "ro.product.hardware",     "HARDWARE" },
            { "ro.build.fingerprint",    "FINGERPRINT" },
            { "ro.build.id",             "ID" },
            { "ro.build.display.id",     "DISPLAY" },
            { "ro.build.host",           "HOST" },
            { "ro.build.user",           "USER" },
            { "ro.build.tags",           "TAGS" },
            { "ro.build.type",           "TYPE" },
        };
        jclass cls = env->FindClass("android/os/Build");
        if (!cls) {
            if (env->ExceptionCheck()) env->ExceptionClear();
            return;
        }
        int applied = 0;
        for (int i = 0; i < perapp_count(); i++) {
            const char *key = perapp_key_at(i);
            const char *val = perapp_value_at(i);
            if (!key || !val) continue;
            for (size_t j = 0; j < sizeof(kMap) / sizeof(kMap[0]); j++) {
                if (strcmp(key, kMap[j].key) != 0) continue;
                jfieldID f = env->GetStaticFieldID(cls, kMap[j].field, "Ljava/lang/String;");
                if (!f) { env->ExceptionClear(); break; }
                jstring js = env->NewStringUTF(val);
                env->SetStaticObjectField(cls, f, js);
                env->DeleteLocalRef(js);
                applied++;
                break;
            }
        }
        if (applied > 0) LOGI("build: %d Build.* field(s) spoofed", applied);
    }

    /* Parse ".perapp_props" and load the entries for pkg into the spoof
     * table. Returns true when at least one entry matched. Malformed lines
     * are skipped; the table caps at PERAPP_MAX_ENTRIES. */
    bool load_config(const char *pkg) {
        int fd = -1;
        int dirfd = api->getModuleDir(); /* pre-specialize only, avoids SELinux path issues */
        if (dirfd >= 0) {
            fd = openat(dirfd, CONFIG_NAME, O_RDONLY);
            close(dirfd);
        }
        if (fd < 0) fd = open(CONFIG_FALLBACK_PATH, O_RDONLY);
        if (fd < 0) return false;

        static char buf[65536]; /* file is WebUI-generated and tiny; cap defensively */
        ssize_t total = 0, n;
        while (total < (ssize_t) sizeof(buf) - 1 &&
               (n = read(fd, buf + total, sizeof(buf) - 1 - (size_t) total)) > 0) {
            total += n;
        }
        close(fd);
        if (total <= 0) return false;
        buf[total] = '\0';

        bool any = false;
        char *line = buf;
        while (line && *line) {
            char *nl = strchr(line, '\n');
            if (nl) *nl = '\0';
            /* tolerate CRLF just in case */
            size_t llen = strlen(line);
            if (llen && line[llen - 1] == '\r') line[llen - 1] = '\0';

            char *bar = strchr(line, '|');
            if (bar) {
                *bar = '\0';
                if (strcmp(line, pkg) == 0) {
                    char *kv = bar + 1;
                    char *eq = strchr(kv, '=');
                    if (eq && eq != kv) { /* need a non-empty key */
                        *eq = '\0';
                        /* Module-control key, not a property spoof: enable the
                         * BYD DCK Java hook for this package only. */
                        if (strcmp(kv, "dck.hook") == 0) {
                            if (strcmp(eq + 1, "1") == 0) {
                                dck_hook_enabled_ = true;
                                any = true;
                            }
                            /* never goes into the prop table */
                        } else if (perapp_add(kv, eq + 1) == 0) {
                            any = true;
                        }
                    }
                }
            }
            line = nl ? nl + 1 : NULL;
        }

        if (any) LOGI("%s: %d spoof entrie(s) loaded", pkg, perapp_count());
        return any;
    }
};

REGISTER_ZYGISK_MODULE(DeviceIDModule)
