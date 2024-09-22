#include <jni.h>
#include <string>
#include <unistd.h>
#include <sys/wait.h>
#include <android/log.h>

#define LOG_TAG "NativeExecution"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// 执行二进制文件的 native 方法
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_erjinzzhi2_MainActivity_runNativeBinary(JNIEnv *env, jobject obj, jstring binaryPath) {
    const char *binary_path = env->GetStringUTFChars(binaryPath, 0);

    pid_t pid = fork();  // 创建子进程
    if (pid == 0) {
        // 在子进程中执行二进制文件
        execl(binary_path, binary_path, (char *) NULL);
        _exit(EXIT_FAILURE);  // execl 失败，退出子进程
    } else if (pid > 0) {
        // 父进程等待子进程执行完成
        int status;
        waitpid(pid, &status, 0);
        env->ReleaseStringUTFChars(binaryPath, binary_path);

        if (WIFEXITED(status)) {
            int exitCode = WEXITSTATUS(status);
            LOGD("Binary executed with exit code: %d", exitCode);
            return exitCode;
        } else {
            LOGE("Binary did not exit successfully");
            return -1;
        }
    } else {
        // fork 失败
        LOGE("Failed to fork process");
        return -1;
    }
}