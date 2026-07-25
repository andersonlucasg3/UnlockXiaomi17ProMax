/* memread <pid> <addr_hex> <len> — dump another process's memory via
 * process_vm_readv (root). Prints hex+ascii. */
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/uio.h>

int main(int argc, char **argv) {
    if (argc != 4) { fprintf(stderr, "usage: %s <pid> <addr_hex> <len>\n", argv[0]); return 1; }
    pid_t pid = (pid_t) atoi(argv[1]);
    unsigned long long addr = strtoull(argv[2], NULL, 16);
    size_t len = (size_t) atoi(argv[3]);
    if (len > 4096) len = 4096;
    unsigned char buf[4096];
    memset(buf, 0, sizeof(buf));
    struct iovec local = { buf, len };
    struct iovec remote = { (void *) (uintptr_t) addr, len };
    ssize_t n = process_vm_readv(pid, &local, 1, &remote, 1, 0);
    if (n < 0) { perror("process_vm_readv"); return 1; }
    printf("read %zd bytes at 0x%llx\n", n, addr);
    for (ssize_t i = 0; i < n; i++) {
        if (i % 16 == 0) printf("%016llx: ", addr + (unsigned long long) i);
        printf("%02x ", buf[i]);
        if (i % 16 == 15 || i == n - 1) {
            for (ssize_t p = i % 16; p < 15; p++) printf("   ");
            printf(" ");
            for (ssize_t p = i - i % 16; p <= i; p++)
                putchar(buf[p] >= 32 && buf[p] < 127 ? buf[p] : '.');
            printf("\n");
        }
    }
    return 0;
}
