/* memscan <pid> <lo_hex> <hi_hex> — scan all readable private/file-backed
 * mappings of a process for 8-byte pointer values in [lo, hi).
 * Prints each hit: mapping line + address + value. Root only. */
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <sys/uio.h>

int main(int argc, char **argv) {
    if (argc != 4) { fprintf(stderr, "usage: %s <pid> <lo_hex> <hi_hex>\n", argv[0]); return 1; }
    pid_t pid = (pid_t) atoi(argv[1]);
    uint64_t lo = strtoull(argv[2], NULL, 16);
    uint64_t hi = strtoull(argv[3], NULL, 16);

    char maps_path[64];
    snprintf(maps_path, sizeof(maps_path), "/proc/%d/maps", pid);
    FILE *f = fopen(maps_path, "r");
    if (!f) { perror("fopen maps"); return 1; }

    static unsigned char buf[1 << 20]; /* 1 MiB chunks */
    char line[512];
    int hits = 0;
    while (fgets(line, sizeof(line), f)) {
        uint64_t start, end;
        char perms[8] = {0};
        if (sscanf(line, "%lx-%lx %7s", &start, &end, perms) != 3) continue;
        if (perms[0] != 'r') continue;
        uint64_t size = end - start;
        if (size > 256UL << 20) continue; /* skip giant regions (dalvik spaces) */
        for (uint64_t off = 0; off < size; off += sizeof(buf)) {
            size_t want = sizeof(buf);
            if (off + want > size) want = (size_t) (size - off);
            struct iovec lv = { buf, want };
            struct iovec rv = { (void *) (uintptr_t) (start + off), want };
            ssize_t n = process_vm_readv(pid, &lv, 1, &rv, 1, 0);
            if (n <= 0) break;
            for (ssize_t i = 0; i + 8 <= n; i += 8) {
                uint64_t v;
                memcpy(&v, buf + i, 8);
                if (v >= lo && v < hi) {
                    printf("HIT @0x%lx -> 0x%lx  [map: %lx-%lx %s]\n",
                           (unsigned long) (start + off + (uint64_t) i),
                           (unsigned long) v, (unsigned long) start,
                           (unsigned long) end, perms);
                    hits++;
                    if (hits > 200) { printf("...truncated\n"); fclose(f); return 0; }
                }
            }
        }
    }
    fclose(f);
    printf("total hits: %d\n", hits);
    return 0;
}
