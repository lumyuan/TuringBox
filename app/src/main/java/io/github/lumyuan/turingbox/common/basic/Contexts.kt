package io.github.lumyuan.turingbox.common.basic

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import java.io.IOException
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


fun Context.width(): Int {
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val outPoint = Point()
    // 可能有虚拟按键的情况
    display.getRealSize(outPoint)
    return outPoint.x
}

fun Context.height(): Int {
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val outPoint = Point()
    // 可能有虚拟按键的情况
    display.getRealSize(outPoint)
    return outPoint.y
}

fun Context.px2dip(pxValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

fun Context.dip2px(dipValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

fun Context.px2sp(pxValue: Float): Int {
    val fontScale = this.resources.displayMetrics.scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}

fun Context.sp2px(spValue: Float): Int {
    val fontScale = this.resources.displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

@Throws(IOException::class)
fun Context.readByte(fileName: String) : ByteArray {
    val open = assets.open(fileName)
    return open.readBytes()
}

fun Context.clip(content: CharSequence){
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, content))
}

fun Context.computeContrastBetweenColors(bg: Int, fg: Int): Float {
    var bgR = android.graphics.Color.red(bg) / 255f
    var bgG = android.graphics.Color.green(bg) / 255f
    var bgB = android.graphics.Color.blue(bg) / 255f
    bgR = if (bgR < 0.03928f) bgR / 12.92f else ((bgR + 0.055f) / 1.055f).toDouble().pow(2.4)
        .toFloat()
    bgG = if (bgG < 0.03928f) bgG / 12.92f else ((bgG + 0.055f) / 1.055f).toDouble().pow(2.4)
        .toFloat()
    bgB = if (bgB < 0.03928f) bgB / 12.92f else ((bgB + 0.055f) / 1.055f).toDouble().pow(2.4)
        .toFloat()
    val bgL = 0.2126f * bgR + 0.7152f * bgG + 0.0722f * bgB
    var fgR = android.graphics.Color.red(fg) / 255f
    var fgG = android.graphics.Color.green(fg) / 255f
    var fgB = android.graphics.Color.blue(fg) / 255f
    fgR = if (fgR < 0.03928f) fgR / 12.92f else ((fgR + 0.055f) / 1.055f).toDouble().pow(2.4)
        .toFloat()
    fgG = if (fgG < 0.03928f) fgG / 12.92f else ((fgG + 0.055f) / 1.055f).toDouble().pow(2.4)
        .toFloat()
    fgB = if (fgB < 0.03928f) fgB / 12.92f else ((fgB + 0.055f) / 1.055f).toDouble().pow(2.4)
        .toFloat()
    val fgL = 0.2126f * fgR + 0.7152f * fgG + 0.0722f * fgB
    return abs((fgL + 0.05f) / (bgL + 0.05f))
}

fun Context.toast(charSequence: CharSequence?) {
    toast(charSequence, Toast.LENGTH_SHORT)
}

fun Context.toast(@StringRes id: Int) {
    toast(id, Toast.LENGTH_SHORT)
}

fun Context.toast(charSequence: CharSequence?, time: Int) {
    Toast.makeText(this, charSequence, time).show()
}

fun Context.toast(@StringRes id: Int, time: Int) {
    Toast.makeText(this, id, time).show()
}

fun Context.isDebugMode(): Boolean {
    return try {
        val info: ApplicationInfo = this.applicationInfo
        info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Context.getVersionCode() = try {
    packageManager.getPackageInfo(packageName, 0).versionCode
} catch (e: PackageManager.NameNotFoundException) {
    e.printStackTrace()
    -1
}

fun Context.getVersionName() = try {
    packageManager.getPackageInfo(packageName, 0).versionName
} catch (e: PackageManager.NameNotFoundException) {
    e.printStackTrace()
    null
}

fun Context.signature(): String? {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        packageInfo.signatures[0].toCharsString()
    }catch (e: Exception){
        e.toString()
        null
    }
}

fun Context.getDiveSize(): Double {
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val dm = DisplayMetrics()
    display.getRealMetrics(dm)
    val x = Math.pow((dm.widthPixels / dm.xdpi).toDouble(), 2.0)
    val y = Math.pow((dm.heightPixels / dm.ydpi).toDouble(), 2.0)
    // 屏幕尺寸
    var decimal = BigDecimal.valueOf(sqrt(x + y))
    decimal = decimal.setScale(2, BigDecimal.ROUND_UP)
    return decimal.toDouble()
}

private val memoryMap = HashMap<String, Long>()
fun Context.memoryInfo(): Map<String, Long> {
    val mi = ActivityManager.MemoryInfo()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(mi)
    return memoryMap.apply {
        this["avail"] = mi.availMem
        this["total"] = mi.totalMem
    }
}

private val externalMemoryMap = HashMap<String, Long>()
fun Context.externalMemoryInfo(): Map<String, Long> {
    val file = Environment.getDataDirectory()
    val statFs = StatFs(file.path)
    val blockSizeLong = statFs.blockSizeLong
    val blockCountLong = statFs.blockCountLong
    val size = blockCountLong * blockSizeLong

    val availableBlocksLong = statFs.availableBlocksLong
    val availSize = availableBlocksLong * blockSizeLong

    return  externalMemoryMap.apply {
        this["avail"] = availSize
        this["total"] = size
    }
}

@SuppressLint("PrivateApi")
fun Context.getBatterySize(): Double {
    return try {
        val powerProfileName = "com.android.internal.os.PowerProfile"
        val powerProfile = Class.forName(powerProfileName).getConstructor(Context::class.java).newInstance(this)
        Class.forName(powerProfileName).getMethod("getBatteryCapacity").invoke(powerProfile) as Double
    }catch (e: Exception) {
        e.toString()
        0.0
    }
}

/**
 * 获取状态栏高度
 */
@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun Context.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}
