/* DeviceID+ per-app property spoof — shared hook engine.
 * Plain C-style C++: no STL, no exceptions, no RTTI, no dynamic allocation.
 * Used by the zygisk library (deviceid_zygisk.cpp) and by the on-device
 * smoke test (test_hook.cpp). */
#ifndef PERAPP_HOOKS_H
#define PERAPP_HOOKS_H

#define PERAPP_MAX_ENTRIES 32
#define PERAPP_KEY_MAX 92
#define PERAPP_VAL_MAX 92 /* PROP_VALUE_MAX */

/* Reset the spoof table (also used by tests). */
void perapp_reset(void);

/* Add one key=value spoof entry. Returns 0 on success, -1 when the table is
 * full or the entry is invalid (empty key, oversized key/value). */
int perapp_add(const char *key, const char *value);

/* Number of live entries in the table. */
int perapp_count(void);

/* Install GOT/PLT hooks for the bionic property readers in every ELF image
 * currently loaded in this process. No-op when the table is empty.
 * Returns the number of GOT slots patched. */
int perapp_install_hooks(void);

#endif /* PERAPP_HOOKS_H */
