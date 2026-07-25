/* Probe: does KernelSU answer prctl from an unprivileged process? */
#include <stdio.h>
#include <sys/prctl.h>
#include <stdint.h>
#include <unistd.h>

#define KERNEL_SU_OPTION 0xDEADBEEF
#define CMD_GET_VERSION 2
#define CMD_GET_DENYLIST 7
#define CMD_UID_GRANTED_ROOT 11
#define CMD_UID_SHOULD_UMOUNT 12

int main(void) {
    int32_t version = 0, reply = 0;
    long r = prctl(KERNEL_SU_OPTION, CMD_GET_VERSION, &version, 0, &reply);
    printf("get_version: ret=%ld version=%d reply=%d\n", r, version, reply);

    int32_t granted = 0; reply = 0;
    r = prctl(KERNEL_SU_OPTION, CMD_UID_GRANTED_ROOT, getuid(), &granted, &reply);
    printf("uid_granted_root(%d): ret=%ld granted=%d reply=%d\n", getuid(), r, granted, reply);

    int32_t umount = 0; reply = 0;
    r = prctl(KERNEL_SU_OPTION, CMD_UID_SHOULD_UMOUNT, getuid(), &umount, &reply);
    printf("uid_should_umount(%d): ret=%ld umount=%d reply=%d\n", getuid(), r, umount, reply);
    return 0;
}
