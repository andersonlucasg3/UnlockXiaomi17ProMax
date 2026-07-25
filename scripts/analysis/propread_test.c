/* Probe: what does __system_property_read return on A16 bionic?
 * Compile on-device (Termux clang), run as shell. */
#include <stdio.h>
#include <sys/system_properties.h>

int main(void) {
    const prop_info *pi = __system_property_find("ro.product.manufacturer");
    if (!pi) { printf("find failed\n"); return 1; }
    char name[512] = {0};
    char val[PROP_VALUE_MAX] = {0};
    int rc = __system_property_read(pi, name, val);
    printf("read rc=%d name=%s val=%s\n", rc, name, val);

    char val2[PROP_VALUE_MAX] = {0};
    int rc2 = __system_property_get("ro.product.manufacturer", val2);
    printf("get rc=%d val=%s\n", rc2, val2);

    /* read_callback: does the callback get name+value, and what's the flow */
    return 0;
}
