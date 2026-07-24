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
        /* Only reached for matched packages (table loaded above). */
        perapp_install_hooks();
    }

    void preServerSpecialize(zygisk::ServerSpecializeArgs *) override {
        api->setOption(zygisk::DLCLOSE_MODULE_LIBRARY);
    }

private:
    zygisk::Api *api;
    JNIEnv *env;

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
                        if (perapp_add(kv, eq + 1) == 0) any = true;
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
