package io.github.lumyuan.turingbox

import android.app.Application

class TuringBox: Application() {

    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }

}