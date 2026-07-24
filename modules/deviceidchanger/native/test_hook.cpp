/* DeviceID+ smoke test (on-device, run as root):
 * proves that perapp_install_hooks() GOT-patching actually diverts
 * __system_property_get() in this process on this device's userspace.
 *
 * 1. reads ro.product.manufacturer BEFORE hooking (direct call, real value)
 * 2. seeds the spoof table with ro.product.manufacturer=HUAWEI and hooks
 * 3. reads again via a direct call (goes through this binary's PLT/GOT —
 *    must now print HUAWEI)
 * 4. reads via a dlopen+dlsym'd pointer to the real libc function
 *    (bypasses the GOT — control, must still print the real value)
 *
 * Build: ./build.sh test   Run: su -c '/path/to/test_hook' */

#include <dlfcn.h>
#include <stdio.h>
#include <string.h>
#include <sys/system_properties.h>

#include "perapp_hooks.h"

typedef int (*get_fn)(const char *, char *);

int main(void) {
    static char buf[PROP_VALUE_MAX];
    int rc = 1;

    memset(buf, 0, sizeof(buf));
    int len = __system_property_get("ro.product.manufacturer", buf);
    printf("pre-hook direct : '%s' (len=%d)\n", buf, len);
    char real[PROP_VALUE_MAX];
    strncpy(real, buf, sizeof(real) - 1);
    real[sizeof(real) - 1] = '\0';

    perapp_reset();
    if (perapp_add("ro.product.manufacturer", "HUAWEI") != 0) {
        printf("FAIL: perapp_add rejected the entry\n");
        return 1;
    }
    int slots = perapp_install_hooks();
    printf("slots patched   : %d\n", slots);

    memset(buf, 0, sizeof(buf));
    len = __system_property_get("ro.product.manufacturer", buf);
    printf("post-hook direct: '%s' (len=%d)\n", buf, len);

    const char *control = "(dlsym failed)";
    void *h = dlopen("libc.so", RTLD_NOW);
    if (h) {
        get_fn real_get = (get_fn) dlsym(h, "__system_property_get");
        if (real_get) {
            static char cbuf[PROP_VALUE_MAX];
            memset(cbuf, 0, sizeof(cbuf));
            int clen = real_get("ro.product.manufacturer", cbuf);
            printf("dlsym control   : '%s' (len=%d)\n", cbuf, clen);
            control = cbuf;
        }
    }

    if (strcmp(buf, "HUAWEI") == 0 && strcmp(control, "HUAWEI") != 0 &&
        strcmp(real, "HUAWEI") != 0) {
        printf("RESULT: PASS (GOT patch live: direct call spoofed, dlsym control real)\n");
        rc = 0;
    } else {
        printf("RESULT: FAIL\n");
    }
    return rc;
}
