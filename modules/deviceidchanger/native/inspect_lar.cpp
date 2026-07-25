/* inspect_lar — standalone reproduction of the in-app GOT patching mystery:
 * dlopen's libandroid_runtime.so, installs the perapp hooks with verbose
 * per-slot logging, then reads back the known JUMP_SLOT addresses for
 * __system_property_get (base+0x303fb8) and __system_property_read_callback
 * (base+0x2fe510) from THIS process's own memory and prints their contents.
 *
 * Build: ./build.sh inspect   Run: su -c '/data/local/tmp/inspect_lar' */

#include <dlfcn.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>

#include "perapp_hooks.h"

static uintptr_t base_of(const char *substr) {
    FILE *f = fopen("/proc/self/maps", "r");
    if (!f) return 0;
    char line[512];
    uintptr_t base = 0;
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, substr)) {
            sscanf(line, "%lx-", &base);
            break;
        }
    }
    fclose(f);
    return base;
}

int main(void) {
    void *h = dlopen("/system/lib64/libandroid_runtime.so", RTLD_NOW);
    printf("dlopen libandroid_runtime: %p (err=%s)\n", h, dlerror());

    perapp_reset();
    perapp_add("ro.product.manufacturer", "HUAWEI");
    int n = perapp_install_hooks();
    printf("patched=%d\n", n);

    uintptr_t base = base_of("libandroid_runtime.so");
    printf("lar base from maps: 0x%lx\n", (unsigned long) base);
    if (base) {
        void *get_slot = (void *) (base + 0x303fb8);
        void *cb_slot = (void *) (base + 0x2fe510);
        printf("get slot @%p contains %p\n", get_slot, *(void **) get_slot);
        printf("rcb slot @%p contains %p\n", cb_slot, *(void **) cb_slot);
    }
    return 0;
}
