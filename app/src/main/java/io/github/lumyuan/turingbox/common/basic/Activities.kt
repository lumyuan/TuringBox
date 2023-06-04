package io.github.lumyuan.turingbox.common.basic

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.Display

fun Activity.startBrowser(url: String?){
    try {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse(url)
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        toast(e.toString())
    }
}

fun Activity.startIntentView(uri: String?){
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(uri)
            )
        )
    }catch (e: Exception){
        e.printStackTrace()
        toast(e.toString())
    }
}

fun Activity.startApp(packageName: String?) {
    try {
        val packageManager: PackageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName!!)!!
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }catch (e: Exception){
        e.printStackTrace()
        toast(e.toString())
    }
}

fun Activity.getRefreshRate(): Float {
    val display: Display = windowManager.defaultDisplay
    return display.refreshRate
}