#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_pratik_bluetoothadhoc_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello World";
    return env->NewStringUTF(hello.c_str());
}
