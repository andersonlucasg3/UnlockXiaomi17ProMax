/* inspect_preload — LD_PRELOAD variant of inspect_lar: constructor dumps
 * dl_iterate_phdr info for libandroid_runtime (dlpi_addr vs maps first line),
 * installs the perapp hooks with verbose slot logging, and reads back the
 * known slots. Run inside app_process to reproduce the app-process context:
 *   LD_PRELOAD=/data/local/tmp/inspect_preload.so \
 *     app_process /system/bin com.android.commands.monkey.Monkey 2>&1 | grep ISP
 */

#include <dlfcn.h>
#include <link.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <android/log.h>

#include "perapp_hooks.h"

#define P(...) __android_log_print(ANDROID_LOG_INFO, "ISPX", __VA_ARGS__)

static uintptr_t maps_base_of(const char *substr) {
    FILE *f = fopen("/proc/self/maps", "r");
    if (!f) return 0;
    char line[512];
    uintptr_t base = 0;
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, substr)) { sscanf(line, "%lx-", &base); break; }
    }
    fclose(f);
    return base;
}

static int phdr_print_cb(struct dl_phdr_info *info, size_t, void *) {
    if (info->dlpi_name && strstr(info->dlpi_name, "libandroid_runtime"))
        P("phdr: name=%s dlpi_addr=%p\n", info->dlpi_name, (void *) info->dlpi_addr);
    return 0;
}

__attribute__((constructor)) static void inspect_main(void) {
    dl_iterate_phdr(phdr_print_cb, NULL);
    uintptr_t mb = maps_base_of("libandroid_runtime.so");
    P("maps first line base: 0x%lx\n", (unsigned long) mb);

    perapp_reset();
    perapp_add("ro.product.manufacturer", "HUAWEI");
    int n = perapp_install_hooks();
    P("patched=%d\n", n);

    if (mb) {
        void *get_slot = (void *) (mb + 0x303fb8);
        void *rcb_slot = (void *) (mb + 0x2fe510);
        P("get slot @%p contains %p\n", get_slot, *(void **) get_slot);
        P("rcb slot @%p contains %p\n", rcb_slot, *(void **) rcb_slot);
    }
}
