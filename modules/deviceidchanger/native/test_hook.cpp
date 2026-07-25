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

/* Captures the last name/value delivered by __system_property_read_callback. */
static char cb_name[512];
static char cb_val[PROP_VALUE_MAX];
static void cb_capture(void *cookie, const char *name, const char *value, uint32_t serial) {
    (void) cookie; (void) serial;
    strncpy(cb_name, name, sizeof(cb_name) - 1);
    strncpy(cb_val, value, sizeof(cb_val) - 1);
}

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

    /* Path 2: __system_property_find + __system_property_read (what JNI
     * native_get / property_get use on A16). rc must be the spoofed length. */
    const prop_info *pi = __system_property_find("ro.product.manufacturer");
    char rname[512] = {0};
    static char rval[PROP_VALUE_MAX];
    memset(rval, 0, sizeof(rval));
    int rlen = pi ? __system_property_read(pi, rname, rval) : -1;
    printf("post-hook read  : '%s' (len=%d, name=%s)\n", rval, rlen, rname);

    /* Path 3: __system_property_read_callback. */
    memset(cb_name, 0, sizeof(cb_name));
    memset(cb_val, 0, sizeof(cb_val));
    if (pi) __system_property_read_callback(pi, cb_capture, NULL);
    printf("post-hook rd_cb : '%s' (name=%s)\n", cb_val, cb_name);

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

    int ok_get = strcmp(buf, "HUAWEI") == 0;
    int ok_read = strcmp(rval, "HUAWEI") == 0 && rlen == (int) strlen("HUAWEI");
    int ok_cb = strcmp(cb_val, "HUAWEI") == 0;
    int ok_ctrl = strcmp(control, "HUAWEI") != 0 && strcmp(real, "HUAWEI") != 0;

    /* Path 4 (COW): fresh hooks OFF — reset the table so GOT hooks do
     * nothing, then apply the prop-area COW edit and re-read. The plain
     * bionic __system_property_get must see HUAWEI without any hook. */
    perapp_reset();
    perapp_add("ro.product.manufacturer", "HUAWEI");
    int cow = perapp_cow_apply();
    static char cowbuf[PROP_VALUE_MAX];
    memset(cowbuf, 0, sizeof(cowbuf));
    int cowlen = __system_property_get("ro.product.manufacturer", cowbuf);
    printf("cow get        : '%s' (len=%d, applied=%d)\n", cowbuf, cowlen, cow);
    int ok_cow = cow > 0 && strcmp(cowbuf, "HUAWEI") == 0;

    if (ok_get && ok_read && ok_cb && ok_ctrl && ok_cow) {
        printf("RESULT: PASS (get + read + read_callback + COW all spoofed; dlsym control real)\n");
        rc = 0;
    } else {
        printf("RESULT: FAIL (get=%d read=%d read_cb=%d control=%d cow=%d)\n",
               ok_get, ok_read, ok_cb, ok_ctrl, ok_cow);
    }
    return rc;
}
