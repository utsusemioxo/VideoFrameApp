#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "VideoProcessor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_example_videoframeapp_VideoProcessor_processVideo(
    JNIEnv* env,
    jobject /* this */,
    jstring path) {

  const char* cPath = env->GetStringUTFChars(path, nullptr);
  LOGD("ProcessVideo called with path: %s", cPath);

  // 底层录像c++ api的空实现，留着做插帧逻辑
}