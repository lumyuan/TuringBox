package io.github.lumyuan.turingbox.common.util

import android.content.Context
import io.github.lumyuan.turingbox.TuringBox

object SharedPreferencesUtil {

    private val sharedPreferences by lazy {
        TuringBox.application.getSharedPreferences(TuringBox.application.packageName, Context.MODE_PRIVATE)
    }

    fun save(key: Any?, value: String?): Boolean{
        return sharedPreferences.edit().putString(key.toString(), value).commit()
    }

    fun load(key: Any?): String?{
        return sharedPreferences.getString(key?.toString(), null)
    }

}