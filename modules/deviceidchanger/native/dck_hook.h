/* DeviceID+ BYD DCK hook — lazy Java-method hook inside the target process.
 * Plain C-style C++: no STL, no exceptions, no RTTI, no dynamic allocation. */
#ifndef DCK_HOOK_H
#define DCK_HOOK_H

#include <jni.h>

/* Spawn the detached installer thread. It polls until the BYD app context and
 * the GMS DCK client SDK classes exist, then flips
 * DigitalKeyFrameworkClient.isCreateDigitalKeyPossible() (on the concrete
 * implementation class) to native and points it at a stub returning
 * Tasks.forResult(Boolean.TRUE). Any failure is a silent no-op: the host app
 * must never crash because of this module. Call once, in postAppSpecialize. */
void dck_hook_start(JavaVM *vm);

#endif /* DCK_HOOK_H */
