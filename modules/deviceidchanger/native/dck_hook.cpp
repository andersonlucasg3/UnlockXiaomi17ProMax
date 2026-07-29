/* DeviceID+ BYD DCK hook (com.byd.bydautolink only).
 *
 * Goal: make DigitalKeyFrameworkClient.isCreateDigitalKeyPossible() (GMS
 * Digital Car Key client SDK embedded in the BYD app) return
 * Tasks.forResult(Boolean.TRUE) instead of a Task failed with
 * FrameworkUnavailableException (no DCK service in this device's GMS).
 *
 * Technique — "flip-to-native", no inline patching:
 *   1. The concrete implementation class is known from runtime recon:
 *      com.google.android.gms.dck.internal.zzfa (invoke-interface at the call
 *      site dispatches to it, so hooking it covers the app). We NEVER call
 *      DigitalKeyFramework.getClient() ourselves: instantiating the DCK
 *      client early (service bind, chimera init) destabilizes the
 *      DexProtector-protected flow (SIGSEGV in nterp, v2.3.0 A/B-proven).
 *   2. PASSIVE timing: wait for the Application context (cheap, no DCK class
 *      touch), then a grace delay, then poll FindClass(zzfa). No loadClass
 *      forcing, no instantiation, no other DCK classes touched.
 *   3. Set kAccNative (0x0100) in ArtMethod::access_flags_ of the target
 *      method, then RegisterNatives() to install our C entry point.
 *      access_flags_ sits at offset 4 of ArtMethod (GcRoot declaring_class_
 *      is a 4-byte compressed reference at offset 0) — stable since
 *      Android 12, verified target: Android 16 (API 36), aarch64.
 *   4. The native stub builds Tasks.forResult(Boolean.TRUE) via JNI and
 *      returns it. ART then always enters the method through the native
 *      entry point (no JIT/AOT dependence).
 *
 * Every JNI call that can throw is followed by ExceptionCheck/Clear; any
 * failure leaves the app untouched (no-op, never crash the host). */

#include <jni.h>
#include <pthread.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <android/log.h>

#include "dck_hook.h"

#define LOG_TAG "DeviceIDPlus"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/* ArtMethod layout assumptions (see file header). */
#define ARTMETHOD_ACCESS_FLAGS_OFF 4
#define KACC_NATIVE 0x0100u

#define DCK_IMPL_CLASS "com.google.android.gms.dck.internal.zzfa"
#define DCK_METHOD "isCreateDigitalKeyPossible"
#define DCK_METHOD_SIG "()Lcom/google/android/gms/tasks/Task;"

#define GRACE_US 1500000            /* let the app settle before touching
                                     * anything DCK-related */
#define POLL_US 250000              /* 250 ms between attempts */
#define MAX_ATTEMPTS 1000           /* ~4 min, then give up silently */

static JavaVM *g_vm;

/* Clear a pending JNI exception, return true when one was pending. */
static bool clr_exc(JNIEnv *env) {
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        return true;
    }
    return false;
}

/* Tasks.forResult(Boolean.TRUE) — fresh local ref, NULL on any failure. */
static jobject new_true_task(JNIEnv *env) {
    jclass bcls = env->FindClass("java/lang/Boolean");
    if (!bcls) { clr_exc(env); return NULL; }
    jfieldID tf = env->GetStaticFieldID(bcls, "TRUE", "Ljava/lang/Boolean;");
    if (!tf) { clr_exc(env); env->DeleteLocalRef(bcls); return NULL; }
    jobject btrue = env->GetStaticObjectField(bcls, tf);
    env->DeleteLocalRef(bcls);
    if (!btrue) { clr_exc(env); return NULL; }

    jclass tcls = env->FindClass("com/google/android/gms/tasks/Tasks");
    if (!tcls) { clr_exc(env); env->DeleteLocalRef(btrue); return NULL; }
    jmethodID fr = env->GetStaticMethodID(tcls, "forResult",
        "(Ljava/lang/Object;)Lcom/google/android/gms/tasks/Task;");
    if (!fr) { clr_exc(env); env->DeleteLocalRef(tcls); env->DeleteLocalRef(btrue); return NULL; }
    jobject task = env->CallStaticObjectMethod(tcls, fr, btrue);
    env->DeleteLocalRef(tcls);
    env->DeleteLocalRef(btrue);
    if (clr_exc(env) || !task) return NULL;
    return task;
}

/* Native replacement for DigitalKeyFrameworkClient.isCreateDigitalKeyPossible(). */
static jobject dck_stub_isCreatePossible(JNIEnv *env, jobject /*thiz*/) {
    LOGI("byd-dck: isCreateDigitalKeyPossible() intercepted -> Task(TRUE)");
    jobject task = new_true_task(env);
    if (!task) LOGE("byd-dck: stub failed to build Task; returning null");
    return task;
}

/* Flip the ArtMethod to native and register fn as its implementation.
 * Returns true on success. */
static bool flip_and_register(JNIEnv *env, jclass cls, jmethodID mid,
                              const char *name, const char *sig, void *fn) {
    if (!mid) return false;
    uint32_t *flags = (uint32_t *) ((char *) mid + ARTMETHOD_ACCESS_FLAGS_OFF);
    *flags |= KACC_NATIVE;
    JNINativeMethod nm;
    nm.name = (char *) name;
    nm.signature = (char *) sig;
    nm.fnPtr = fn;
    if (env->RegisterNatives(cls, &nm, 1) != JNI_OK) {
        clr_exc(env);
        return false;
    }
    return true;
}

/* Current application context, NULL when not ready yet. Used ONLY as a
 * process-readiness signal (app framework up) — no DCK class is loaded
 * through it. */
static jobject get_app_context(JNIEnv *env) {
    jclass at = env->FindClass("android/app/ActivityThread");
    if (!at) { clr_exc(env); return NULL; }
    jmethodID cat = env->GetStaticMethodID(at, "currentActivityThread",
        "()Landroid/app/ActivityThread;");
    if (!cat) { clr_exc(env); env->DeleteLocalRef(at); return NULL; }
    jobject th = env->CallStaticObjectMethod(at, cat);
    if (clr_exc(env) || !th) { env->DeleteLocalRef(at); return NULL; }
    jmethodID getapp = env->GetMethodID(at, "getApplication", "()Landroid/app/Application;");
    env->DeleteLocalRef(at);
    if (!getapp) { clr_exc(env); env->DeleteLocalRef(th); return NULL; }
    jobject app = env->CallObjectMethod(th, getapp);
    env->DeleteLocalRef(th);
    if (clr_exc(env) || !app) return NULL;
    return app;
}

/* Resolve the impl class through the app's own ClassLoader. FindClass from a
 * bare native thread uses the boot loader and never sees app classes (v2.3.1
 * lesson); loadClass() only DEFINES the class (no clinit, no instantiation,
 * no service bind — the getClient() instantiation was the DexProtector
 * trigger, not class definition). Returns local ref, NULL while not
 * loadable yet (ClassNotFoundException cleared by caller's clr_exc). */
static jclass resolve_impl(JNIEnv *env, jobject context) {
    jclass ccls = env->FindClass("android/content/Context");
    if (!ccls) { clr_exc(env); return NULL; }
    jmethodID getcl = env->GetMethodID(ccls, "getClassLoader", "()Ljava/lang/ClassLoader;");
    env->DeleteLocalRef(ccls);
    if (!getcl) { clr_exc(env); return NULL; }
    jobject loader = env->CallObjectMethod(context, getcl);
    if (clr_exc(env) || !loader) return NULL;

    jclass lcls = env->FindClass("java/lang/ClassLoader");
    if (!lcls) { clr_exc(env); env->DeleteLocalRef(loader); return NULL; }
    jmethodID loadc = env->GetMethodID(lcls, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    env->DeleteLocalRef(lcls);
    if (!loadc) { clr_exc(env); env->DeleteLocalRef(loader); return NULL; }

    jstring jname = env->NewStringUTF(DCK_IMPL_CLASS);
    jobject cls = env->CallObjectMethod(loader, loadc, jname);
    env->DeleteLocalRef(jname);
    env->DeleteLocalRef(loader);
    if (clr_exc(env) || !cls) return NULL;
    return (jclass) cls;
}

/* One install attempt. Returns true when the hook is fully installed. */
static bool try_install(JNIEnv *env, jobject ctx) {
    jclass impl = resolve_impl(env, ctx);
    if (!impl) return false;

    jmethodID mid = env->GetMethodID(impl, DCK_METHOD, DCK_METHOD_SIG);
    if (!mid) {
        clr_exc(env);
        env->DeleteLocalRef(impl);
        return false;
    }
    bool ok = flip_and_register(env, impl, mid, DCK_METHOD, DCK_METHOD_SIG,
                                (void *) dck_stub_isCreatePossible);
    env->DeleteLocalRef(impl);

    if (ok) LOGI("byd-dck: hook installed on zzfa (flip-to-native, access_flags@+%d, passive)",
                 ARTMETHOD_ACCESS_FLAGS_OFF);
    return ok;
}

static void *installer_thread(void *) {
    JNIEnv *env = NULL;
    if (g_vm->AttachCurrentThread(&env, NULL) != JNI_OK || !env) return NULL;

    /* Phase 1: wait for the app framework (Application context). */
    jobject ctx = NULL;
    for (int i = 0; i < 100 && !ctx; i++) { /* ~10 s */
        ctx = get_app_context(env);
        if (!ctx) usleep(100000);
    }
    if (ctx) env->DeleteLocalRef(ctx);

    /* Phase 2: grace delay — do not touch DCK classes while the app is
     * still in early init (DexProtector decrypts lazily; touching its
     * classes too early corrupted the flow in v2.3.0). */
    usleep(GRACE_US);

    /* Phase 3: passive poll for the already-known impl class. */
    bool done = false;
    for (int i = 0; i < MAX_ATTEMPTS && !done; i++) {
        done = try_install(env);
        if (!done) usleep(POLL_US);
    }
    if (!done) LOGI("byd-dck: gave up after %d attempts (zzfa never loadable)", MAX_ATTEMPTS);
    g_vm->DetachCurrentThread();
    return NULL;
}

void dck_hook_start(JavaVM *vm) {
    g_vm = vm;
    pthread_t t;
    pthread_attr_t attr;
    if (pthread_attr_init(&attr) != 0) return;
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    if (pthread_create(&t, &attr, installer_thread, NULL) != 0) {
        LOGE("byd-dck: failed to spawn installer thread");
    } else {
        LOGI("byd-dck: installer thread started (passive)");
    }
    pthread_attr_destroy(&attr);
}
