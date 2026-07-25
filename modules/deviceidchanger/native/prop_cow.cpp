/* DeviceID+ per-app prop-area COW spoof.
 *
 * Copies the page(s) of the system property area that hold a spoofed
 * property into a PRIVATE anonymous mapping at the same address (per-process
 * copy-on-write), then writes the spoofed value in place following bionic's
 * serial protocol. Every read path in this process — JNI SystemProperties,
 * bionic readers, direct prop-area parsing, statically linked readers — sees
 * the spoofed value, because they all read the same pages. Other processes
 * keep the real value. No hooks, no GOT patching, nothing executable left
 * behind beyond the module itself.
 *
 * Layout reference (bionic libc/bionic/system_properties.cpp):
 *   struct prop_info { atomic<uint32_t> serial; char value[92]; char name[]; }
 *   serial: low bit = dirty while writing; (len << 24) | (count & 0xffffff)
 *
 * Constraints: plain C-style C++, no STL/exceptions/RTTI/dynamic allocation.
 * Runs inside arbitrary app processes — must never crash the host. */

#include "perapp_hooks.h"

#include <stdint.h>
#include <atomic>
#include <string.h>
#include <sys/mman.h>
#include <sys/system_properties.h>
#include <unistd.h>
#include <android/log.h>

#define LOG_TAG "DeviceIDPlus"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/* bionic prop_info layout (see bionic system_properties.cpp). */
#define DIDP_PROP_VALUE_MAX 92
#define DIDP_SERIAL_DIRTY   1u
#define DIDP_SERIAL_LEN_SHIFT 24
#define DIDP_SERIAL_COUNT_MASK 0xFFFFFFu

struct didp_prop_info {
    std::atomic<uint32_t> serial;
    char value[DIDP_PROP_VALUE_MAX];
    char name[0];
};

/* Replace the pages covering [addr, addr+len) with private anonymous copies
 * whose contents match the originals. Returns 0 on success. */
static int privatize_pages(uintptr_t addr, size_t len, long page_size) {
    uintptr_t start = addr & ~(uintptr_t) (page_size - 1);
    uintptr_t end = (addr + len + page_size - 1) & ~(uintptr_t) (page_size - 1);
    static unsigned char backup[65536]; /* covers up to 4 pages of 16 KiB */
    size_t total = end - start;
    if (total > sizeof(backup)) return -1;
    memcpy(backup, (const void *) start, total);
    void *r = mmap((void *) start, total, PROT_READ | PROT_WRITE,
                   MAP_PRIVATE | MAP_ANONYMOUS | MAP_FIXED, -1, 0);
    if (r != (void *) start) return -1;
    memcpy((void *) start, backup, total);
    return 0;
}

/* Write value into our private prop_info copy, following bionic's serial
 * protocol so concurrent readers see a consistent value. */
static void write_prop_value(struct didp_prop_info *pi, const char *value) {
    size_t len = strlen(value);
    if (len >= DIDP_PROP_VALUE_MAX) len = DIDP_PROP_VALUE_MAX - 1;
    uint32_t serial = pi->serial.load(std::memory_order_relaxed);
    pi->serial.store(serial | DIDP_SERIAL_DIRTY, std::memory_order_relaxed);
    memcpy(pi->value, value, len + 1);
    std::atomic_thread_fence(std::memory_order_release);
    uint32_t news = (uint32_t) ((len << DIDP_SERIAL_LEN_SHIFT) |
                                ((serial + 1) & DIDP_SERIAL_COUNT_MASK));
    pi->serial.store(news, std::memory_order_relaxed);
    std::atomic_thread_fence(std::memory_order_seq_cst);
}

/* Apply the whole spoof table via prop-area COW. Returns number applied. */
int perapp_cow_apply(void) {
    long page_size = sysconf(_SC_PAGESIZE);
    if (page_size <= 0) page_size = 4096;

    int applied = 0;
    for (int i = 0; i < perapp_count(); i++) {
        const char *key = perapp_key_at(i);
        const char *val = perapp_value_at(i);
        if (!key || !val) continue;

        const prop_info *pi = __system_property_find(key);
        if (!pi) {
            LOGE("cow: prop not found: %s", key);
            continue;
        }
        struct didp_prop_info *dpi = (struct didp_prop_info *) pi;
        /* Cover serial + value + name (worst case a page-straddling entry). */
        size_t span = sizeof(uint32_t) + DIDP_PROP_VALUE_MAX + strlen(dpi->name) + 1;
        if (privatize_pages((uintptr_t) pi, span, page_size) != 0) {
            LOGE("cow: privatize failed for %s", key);
            continue;
        }
        write_prop_value(dpi, val);
        LOGI("cow spoof applied: %s=%s", key, val);
        applied++;
    }
    return applied;
}
