// signal_handler.c
#include <jni.h>
#include <signal.h>
#include <android/log.h>

#define LOG_TAG "SignalHandler"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

void sigsys_handler(int signum, siginfo_t *info, void *context) {
    LOGI("SIGSYS signal received and ignored.");
    // Ignore the signal to prevent the app from crashing
}

void setup_sigsys_handler() {
    struct sigaction sa;
    sa.sa_flags = SA_SIGINFO;
    sa.sa_sigaction = sigsys_handler;
    sigemptyset(&sa.sa_mask);
    if (sigaction(SIGSYS, &sa, NULL) == -1) {
        LOGI("Failed to set up SIGSYS handler");
    } else {
        LOGI("SIGSYS handler set up successfully");
    }
}

// Correctly named JNI function
JNIEXPORT void JNICALL
Java_com_anytypeio_anytype_app_AndroidApplication_00024SignalHandler_initSignalHandlerNative(JNIEnv *env, jobject thiz) {
    LOGI("Native signal handler method called - library loaded successfully");
    setup_sigsys_handler();
}