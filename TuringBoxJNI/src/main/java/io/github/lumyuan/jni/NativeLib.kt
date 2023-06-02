package io.github.lumyuan.jni

class NativeLib {
    external fun getKernelPropLong(path: String): Long

    companion object {
        init {
            System.loadLibrary("jni")
        }
    }
}