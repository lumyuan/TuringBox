#include <jni.h>
#include <string>

char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_lumyuan_jni_NativeLib_getKernelPropLong(JNIEnv *env, jobject thiz, jstring path) {
// std::string hello = "Hello from C++";
    // return env->NewStringUTF(hello.c_str());

    // 读取路径 /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq
    char* charData = jstringToChar(env, path);

    FILE *kernelProp = fopen(charData, "r");
    if (kernelProp == nullptr)
        return -1;
    long freq;
    fscanf(kernelProp,"%ld",&freq);
    fclose(kernelProp);
    return freq;
}