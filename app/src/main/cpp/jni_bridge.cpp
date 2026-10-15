#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "SHELL_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace camb::media {
     ANativeWindow* g_dummy_window;
}

// 保存h264裸流的真实文件路径
static std::string g_h264_file_path;

extern "C"
JNIEXPORT void JNICALL
Java_com_camb_shell_MainActivity_nativeSetSurfaceAndFilePath(JNIEnv *env, jobject thiz, jobject j_surface, jstring j_file_path)
{
    ANativeWindow* win = ANativeWindow_fromSurface(env, j_surface);
    LOGI("nativeSetSurfaceAndFilePath, ANativeWindow = %p", win);

    namespace cm = camb::media;
    if(cm::g_dummy_window)
    {
        ANativeWindow_release(cm::g_dummy_window);
    }
    cm::g_dummy_window = win;
    if(win)
        ANativeWindow_acquire(win);

    // 从java拿到路径字符串
    const char* path = env->GetStringUTFChars(j_file_path, nullptr);
    g_h264_file_path = path;
    env->ReleaseStringUTFChars(j_file_path, path);

    LOGI("收到H264文件路径 = %s", g_h264_file_path.c_str());
    // ========== 这里直接把 g_h264_file_path.c_str() 传给你的SDK ==========
    // SDK内部直接 fopen(g_h264_file_path.c_str(), "rb");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_camb_shell_MainActivity_nativeStopPlayer(JNIEnv *env, jobject thiz)
{
    LOGI("nativeStopPlayer，停止解码，释放资源");
    namespace cm = camb::media;
    if(cm::g_dummy_window)
    {
        ANativeWindow_release(cm::g_dummy_window);
        cm::g_dummy_window = nullptr;
    }
    g_h264_file_path.clear();
}
