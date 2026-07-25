/* DeviceID+ per-app property spoof — GOT/PLT hook engine.
 *
 * Walks every loaded ELF image (dl_iterate_phdr), parses PT_DYNAMIC and
 * patches GOT slots (R_AARCH64_JUMP_SLOT / R_AARCH64_GLOB_DAT) that point to
 * the bionic property readers, diverting them to the replacements below.
 * Pages are temporarily mprotect-ed RW (full RELRO) and their original
 * protection (read from /proc/self/maps) is restored afterwards.
 *
 * Constraints: plain C-style C++ — no STL, no exceptions, no RTTI, no dynamic
 * allocation. The spoof table is a fixed static array; lookups are a tiny
 * linear scan. Everything is bounds-checked: this code runs inside arbitrary
 * app processes and must never crash the host. */

#include "perapp_hooks.h"

#include <elf.h>
#include <link.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/system_properties.h>
#include <unistd.h>
#include <android/log.h>

#define LOG_TAG "DeviceIDPlus"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/* Termux's elf.h does not define the AArch64 relocation types we need. */
#ifndef R_AARCH64_JUMP_SLOT
#define R_AARCH64_JUMP_SLOT 1026
#endif
#ifndef R_AARCH64_GLOB_DAT
#define R_AARCH64_GLOB_DAT 1025
#endif

/* NOTE: __system_property_get_name() is NOT exported by bionic on modern
 * Android, so the replacements below derive prop names via the saved original
 * __system_property_read() instead. */

// ---------- spoof table ----------

struct perapp_entry {
    char key[PERAPP_KEY_MAX];
    char value[PERAPP_VAL_MAX];
};

static struct perapp_entry g_table[PERAPP_MAX_ENTRIES];
static int g_count = 0;

void perapp_reset(void) {
    g_count = 0;
    memset(g_table, 0, sizeof(g_table));
}

int perapp_add(const char *key, const char *value) {
    if (!key || !value || !key[0]) return -1;
    size_t klen = strlen(key);
    size_t vlen = strlen(value);
    if (klen >= PERAPP_KEY_MAX || vlen >= PERAPP_VAL_MAX) return -1;
    if (g_count >= PERAPP_MAX_ENTRIES) return -1;
    memcpy(g_table[g_count].key, key, klen + 1);
    memcpy(g_table[g_count].value, value, vlen + 1);
    g_count++;
    return 0;
}

int perapp_count(void) {
    return g_count;
}

const char *perapp_key_at(int idx) {
    if (idx < 0 || idx >= g_count) return NULL;
    return g_table[idx].key;
}

const char *perapp_value_at(int idx) {
    if (idx < 0 || idx >= g_count) return NULL;
    return g_table[idx].value;
}

/* Tiny linear scan, allocation-free; NULL when the key is not spoofed. */
static const char *perapp_lookup(const char *key) {
    if (!key) return NULL;
    for (int i = 0; i < g_count; i++) {
        if (strcmp(g_table[i].key, key) == 0) return g_table[i].value;
    }
    return NULL;
}

/* Bounded copy into caller-provided bionic buffers (PROP_NAME_MAX /
 * PROP_VALUE_MAX sized). Returns the spoofed value length. */
static size_t copy_value(char *dst, size_t dst_cap, const char *src) {
    size_t len = strlen(src);
    size_t n = len < dst_cap - 1 ? len : dst_cap - 1;
    memcpy(dst, src, n);
    dst[n] = '\0';
    return len;
}

// ---------- saved originals ----------

typedef int (*get_fn)(const char *, char *);
typedef void (*read_cb_fn)(const prop_info *,
                           void (*)(void *, const char *, const char *, uint32_t),
                           void *);
typedef int (*read_fn)(const prop_info *, char *, char *);

static get_fn orig_get = NULL;
static read_cb_fn orig_read_callback = NULL;
static read_fn orig_read = NULL;

/* Temporary diagnostics (petal maps investigation): trace prop queries whose
 * key looks device-identity related, so we can see exactly which keys the app
 * reads and via which bionic entry point. The filter keeps log volume low AND
 * avoids the liblog reentrancy trap: __android_log_print internally reads
 * log.tag.* props, which come back through our hooks — logging every query
 * recurses until the stack blows (observed). log.tag.* never matches the
 * filter below, so no recursion is possible.
 *
 * NOTE: do NOT add __system_property_find as a hook target and do NOT use
 * __thread/TLS here — both variants SIGILL under ZygiskNext's custom loader
 * (jump into the padding below .text), though they run fine under the system
 * linker (test_hook). */
#define PERAPP_TRACE 1
static int trace_key_matches(const char *name) {
    if (!PERAPP_TRACE || !name) return 0;
    return strstr(name, "manufacturer") || strstr(name, "brand") ||
           strstr(name, "huawei") || strstr(name, "HUAWEI") ||
           strstr(name, "ro.product") || strstr(name, "ro.build");
}
#define LOGT(...) do { __android_log_print(ANDROID_LOG_INFO, "DIDPTrace", \
    __VA_ARGS__); } while (0)

// ---------- replacements ----------

static int my___system_property_get(const char *name, char *value) {
    const char *spoof = perapp_lookup(name);
    if (trace_key_matches(name))
        LOGT("get: %s%s", name, spoof ? " [SPOOF]" : "");
    if (spoof) {
        LOGI("spoof applied (get): %s=%s", name, spoof);
        return (int) copy_value(value, PROP_VALUE_MAX, spoof);
    }
    if (orig_get) return orig_get(name, value);
    if (value) value[0] = '\0';
    return 0;
}

static void my___system_property_read_callback(
        const prop_info *pi,
        void (*callback)(void *cookie, const char *name, const char *value, uint32_t serial),
        void *cookie) {
    if (pi && orig_read && orig_read_callback) {
        /* Derive the prop name via the saved original __system_property_read
         * (__system_property_get_name is not exported on modern bionic).
         * NOTE: __system_property_read returns the VALUE LENGTH (>= 0), not
         * 0-on-success — an `== 0` check here silently skipped every spoof
         * for props with a non-empty real value (bug found on A16). */
        char name[512];
        char val[PROP_VALUE_MAX];
        if (orig_read(pi, name, val) >= 0) {
            const char *spoof = perapp_lookup(name);
            if (trace_key_matches(name))
                LOGT("read_callback: %s%s", name, spoof ? " [SPOOF]" : "");
            if (spoof) {
                LOGI("spoof applied (read_callback): %s=%s", name, spoof);
                if (callback) callback(cookie, name, spoof, 0);
                return;
            }
        }
    }
    if (orig_read_callback) orig_read_callback(pi, callback, cookie);
}

static int my___system_property_read(const prop_info *pi, char *name, char *value) {
    if (!orig_read) return 0;
    /* Fill the real data first, then overwrite the value when spoofed.
     * rc is the VALUE LENGTH (>= 0 on success). */
    char tmpname[512];
    int rc = orig_read(pi, tmpname, value);
    if (rc >= 0) {
        if (name) copy_value(name, PROP_NAME_MAX, tmpname);
        const char *spoof = perapp_lookup(tmpname);
        if (trace_key_matches(tmpname))
            LOGT("read: %s%s", tmpname, spoof ? " [SPOOF]" : "");
        if (spoof && value) {
            size_t slen = copy_value(value, PROP_VALUE_MAX, spoof);
            LOGI("spoof applied (read): %s=%s", tmpname, spoof);
            return (int) slen; /* callers use the return as the value length */
        }
    }
    return rc;
}

// ---------- GOT/PLT patching ----------

struct hook_target {
    const char *sym;
    void *replacement;
    void **orig_slot; /* where the first-seen original is saved */
};

struct hook_stats {
    int images;
    int relocs_scanned;
    int patched;      /* slots actually written */
    int already;      /* slots already pointing at our replacement */
    int errors;
};

static struct hook_stats g_stats;

/* Original memory protection of the page holding addr, parsed from
 * /proc/self/maps. Returns -1 when not found. */
static int page_prot_of(uintptr_t addr) {
    FILE *f = fopen("/proc/self/maps", "r");
    if (!f) return -1;
    char line[512];
    int prot = -1;
    while (fgets(line, sizeof(line), f)) {
        uintptr_t lo, hi;
        char perms[8] = {0};
        if (sscanf(line, "%lx-%lx %7s", &lo, &hi, perms) != 3) continue;
        if (addr >= lo && addr < hi) {
            prot = 0;
            if (perms[0] == 'r') prot |= PROT_READ;
            if (perms[1] == 'w') prot |= PROT_WRITE;
            if (perms[2] == 'x') prot |= PROT_EXEC;
            break;
        }
    }
    fclose(f);
    return prot;
}

static void patch_slot(uintptr_t slot_addr, const struct hook_target *t, long page_size) {
    void **slot = (void **) slot_addr;
    void *cur = *slot;
    if (cur == t->replacement) {
        g_stats.already++;
        return;
    }
    if (!cur) {
        g_stats.errors++;
        return;
    }
    /* Save the first original seen; every slot for a given symbol should
     * point at the same libc implementation. */
    if (*t->orig_slot == NULL) *t->orig_slot = cur;

    uintptr_t page_start = slot_addr & ~(uintptr_t) (page_size - 1);
    uintptr_t page_end =
            (slot_addr + sizeof(void *) + page_size - 1) & ~(uintptr_t) (page_size - 1);
    size_t len = page_end - page_start;

    int orig_prot = page_prot_of(slot_addr);
    if (orig_prot < 0) orig_prot = PROT_READ; /* full-RELRO GOT is the common case */

    if (mprotect((void *) page_start, len, PROT_READ | PROT_WRITE) != 0) {
        g_stats.errors++;
        return;
    }
    *slot = t->replacement;
    mprotect((void *) page_start, len, orig_prot);
#ifdef PERAPP_VERBOSE_PATCH
    __android_log_print(ANDROID_LOG_INFO, "ISPX", "patch_slot: %s @%p old=%p new=%p readback=%p",
                        t->sym, (void *) slot_addr, cur, t->replacement, *slot);
#endif
    g_stats.patched++;
}

static void scan_reloc_table(uintptr_t bias, uintptr_t rela_addr, size_t rela_size,
                             const ElfW(Sym) *symtab, const char *strtab, size_t strsz,
                             const struct hook_target *targets, int ntargets,
                             long page_size) {
    if (!rela_addr || !rela_size || !symtab || !strtab) return;
    size_t n = rela_size / sizeof(ElfW(Rela));
    const ElfW(Rela) *rela = (const ElfW(Rela) *) rela_addr;
    for (size_t i = 0; i < n; i++) {
        uint32_t type = ELF64_R_TYPE(rela[i].r_info);
        if (type != R_AARCH64_JUMP_SLOT && type != R_AARCH64_GLOB_DAT) continue;
        g_stats.relocs_scanned++;
        size_t sym_idx = ELF64_R_SYM(rela[i].r_info);
        const ElfW(Sym) *sym = &symtab[sym_idx];
        if (sym->st_name >= strsz) continue; /* defensive: bogus string offset */
        const char *name = strtab + sym->st_name;
        for (int t = 0; t < ntargets; t++) {
            if (strcmp(name, targets[t].sym) == 0) {
                patch_slot(bias + rela[i].r_offset, &targets[t], page_size);
                break;
            }
        }
    }
}

static int phdr_cb(struct dl_phdr_info *info, size_t, void *data) {
    struct {
        const struct hook_target *targets;
        int ntargets;
        long page_size;
    } *ctx = (decltype(ctx)) data;

    if (!info->dlpi_phdr || info->dlpi_phnum == 0) return 0;

    const ElfW(Dyn) *dyn = NULL;
    size_t dyn_count = 0;
    for (int i = 0; i < info->dlpi_phnum; i++) {
        if (info->dlpi_phdr[i].p_type == PT_DYNAMIC) {
            dyn = (const ElfW(Dyn) *) (info->dlpi_addr + info->dlpi_phdr[i].p_vaddr);
            dyn_count = info->dlpi_phdr[i].p_filesz / sizeof(ElfW(Dyn));
            break;
        }
    }
    if (!dyn || !dyn_count) return 0; /* image without .dynamic: nothing to do */
    g_stats.images++;

    uintptr_t jmprel = 0, rela = 0;
    size_t pltrelsz = 0, relasz = 0, strsz = 0;
    long pltrel = DT_RELA;
    const ElfW(Sym) *symtab = NULL;
    const char *strtab = NULL;

    for (size_t i = 0; i < dyn_count; i++) {
        const ElfW(Dyn) *d = &dyn[i];
        if (d->d_tag == DT_NULL) break;
        switch (d->d_tag) {
            case DT_JMPREL:   jmprel   = info->dlpi_addr + d->d_un.d_ptr; break;
            case DT_PLTRELSZ: pltrelsz = d->d_un.d_val; break;
            case DT_PLTREL:   pltrel   = (long) d->d_un.d_val; break;
            case DT_RELA:     rela     = info->dlpi_addr + d->d_un.d_ptr; break;
            case DT_RELASZ:   relasz   = d->d_un.d_val; break;
            case DT_SYMTAB:   symtab   = (const ElfW(Sym) *) (info->dlpi_addr + d->d_un.d_ptr); break;
            case DT_STRTAB:   strtab   = (const char *) (info->dlpi_addr + d->d_un.d_ptr); break;
            case DT_STRSZ:    strsz    = d->d_un.d_val; break;
            default: break;
        }
    }
    if (!symtab || !strtab || !strsz) return 0;

    int before = g_stats.patched;
    /* .rela.plt (expect DT_RELA entries; bail out on unexpected DT_REL) */
    if (jmprel && pltrelsz && pltrel == DT_RELA) {
        scan_reloc_table(info->dlpi_addr, jmprel, pltrelsz, symtab, strtab, strsz,
                         ctx->targets, ctx->ntargets, ctx->page_size);
    }
    /* .rela.dyn carries the GLOB_DAT slots */
    if (rela && relasz) {
        scan_reloc_table(info->dlpi_addr, rela, relasz, symtab, strtab, strsz,
                         ctx->targets, ctx->ntargets, ctx->page_size);
    }
    if (g_stats.patched > before)
        LOGI("patched %d slot(s) in %s", g_stats.patched - before,
             info->dlpi_name && info->dlpi_name[0] ? info->dlpi_name : "(main)");
    return 0;
}

int perapp_install_hooks(void) {
    if (g_count == 0) return 0;

    static const struct hook_target targets[] = {
        { "__system_property_get",           (void *) my___system_property_get,
          (void **) &orig_get },
        { "__system_property_read_callback", (void *) my___system_property_read_callback,
          (void **) &orig_read_callback },
        { "__system_property_read",          (void *) my___system_property_read,
          (void **) &orig_read },
    };

    long page_size = sysconf(_SC_PAGESIZE);
    if (page_size <= 0) page_size = 4096;

    memset(&g_stats, 0, sizeof(g_stats));
    struct {
        const struct hook_target *targets;
        int ntargets;
        long page_size;
    } ctx = { targets, 3, page_size };

    dl_iterate_phdr(phdr_cb, &ctx);

    LOGI("hooks installed: images=%d relocs=%d patched=%d already=%d errors=%d "
         "orig get=%p read_cb=%p read=%p",
         g_stats.images, g_stats.relocs_scanned, g_stats.patched, g_stats.already,
         g_stats.errors, (void *) orig_get, (void *) orig_read_callback,
         (void *) orig_read);
    if (g_stats.errors) LOGE("hook install had %d error(s)", g_stats.errors);
    if (!orig_get && !orig_read_callback && !orig_read)
        LOGE("no property reader symbols found in any loaded image");
    return g_stats.patched;
}
