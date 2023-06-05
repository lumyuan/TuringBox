package io.github.lumyuan.turingbox.windows.main

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.GLSurfaceView
import android.os.BatteryManager
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.basic.AppInfoLoader
import io.github.lumyuan.turingbox.common.device.GpuInfoUtil
import io.github.lumyuan.turingbox.common.model.CpuCoreInfo
import io.github.lumyuan.turingbox.common.shell.BatteryUtils
import io.github.lumyuan.turingbox.common.shell.CpuFrequencyUtils
import io.github.lumyuan.turingbox.common.shell.CpuLoadUtils
import io.github.lumyuan.turingbox.common.shell.GpuUtils
import io.github.lumyuan.turingbox.common.shell.KeepShellPublic
import io.github.lumyuan.turingbox.common.shell.KernelProp
import io.github.lumyuan.turingbox.common.shell.MemoryUtils
import io.github.lumyuan.turingbox.common.shell.ProcessInfo
import io.github.lumyuan.turingbox.common.shell.ProcessUtils
import io.github.lumyuan.turingbox.common.shell.RootFile
import io.github.lumyuan.turingbox.ui.compose.launchTimerJob
import io.github.lumyuan.turingbox.ui.widget.CpuChart
import io.github.lumyuan.turingbox.windows.main.device.CpuFreqCard
import io.github.lumyuan.turingbox.windows.main.device.GpuCard
import io.github.lumyuan.turingbox.windows.main.device.MemoryCard
import io.github.lumyuan.turingbox.windows.main.device.OtherInfoCard
import io.github.lumyuan.turingbox.windows.main.device.OtherInfoState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale


@Preview(showBackground = true)
@Composable
private fun PreviewDevicePage() {
    DevicePage()
}

private val memoryUtils by lazy { MemoryUtils() }
private val cpuLoadUtils by lazy { CpuLoadUtils() }
private val cpuFrequencyUtil by lazy { CpuFrequencyUtils() }
private val batteryUtils by lazy { BatteryUtils() }
private lateinit var batteryManager: BatteryManager

private lateinit var pm: PackageManager
private lateinit var activityManager: ActivityManager

private val processUtils by lazy { ProcessUtils() }

@SuppressLint("StaticFieldLeak")
private lateinit var appInfoLoader: AppInfoLoader
private lateinit var androidIcon: Drawable
private lateinit var linuxIcon: Drawable

private var supported: Boolean = false

private var coreCount = -1
private var minFreq = HashMap<Int, String>()
private var maxFreq = HashMap<Int, String>()

@SuppressLint("UseCompatLoadingForDrawables", "MutableCollectionMutableState")
@Composable
fun DevicePage() {

    val memoryState = remember {
        mutableStateOf(MemoryState(0f, 0f, 0f, 0f))
    }

    val ramPercentage = remember {
        mutableStateOf(0f)
    }
    val swapPercentage = remember {
        mutableStateOf(0f)
    }

    val gpuStateMutableState = remember {
        mutableStateOf(GpuState(0f, 0f))
    }

    val cpuState = remember {
        mutableStateOf(CpuState(-1, ArrayList(), HashMap()))
    }

    val processState = remember {
        mutableStateOf(ArrayList<ProcessInfo>())
    }

    val otherInfoState = remember {
        mutableStateOf(OtherInfoState(0f, 0, 0f, 0f, ""))
    }

    val ioState = remember {
        mutableStateOf(1000)
    }

    val context = LocalContext.current as ComponentActivity

    //GL渲染
    AndroidView(
        factory = {
            GLSurfaceView(it)
        },
        modifier = Modifier.size(.5.dp)
    ) { glSurfaceView ->
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        glSurfaceView.setRenderer(GpuInfoUtil())
    }

    supported = processUtils.supported(context)

    appInfoLoader = AppInfoLoader(context, 100)

    pm = context.packageManager

    activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    androidIcon = context.resources.getDrawable(R.drawable.process_android, context.theme)
    linuxIcon = context.resources.getDrawable(R.drawable.process_linux, context.theme)

    batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LaunchedEffect(this) {
            launchTimerJob(Dispatchers.IO, 1500) {
                updateInfo(memoryState, ramPercentage, swapPercentage, gpuStateMutableState, cpuState, processState, otherInfoState, ioState)
            }
        }
        MemoryCard(memoryState, ramPercentage, swapPercentage)
        GpuCard(gpuStateMutableState)
        CpuFreqCard(cpuState, processState)
        OtherInfoCard(otherInfoState, ioState)
    }
}

private suspend fun updateInfo(
    memoryState: MutableState<MemoryState>,
    ramPercentage: MutableState<Float>,
    swapPercentage: MutableState<Float>,
    gpuStateMutableState: MutableState<GpuState>,
    cpuState: MutableState<CpuState>,
    processState: MutableState<ArrayList<ProcessInfo>>,
    otherInfoState: MutableState<OtherInfoState>,
    ioState: MutableState<Int>
) {
    try {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        val totalMem = (info.totalMem / 1024 / 1024f).toInt()
        val availMem = (info.availMem / 1024 / 1024f).toInt()

        val swapInfo = KeepShellPublic.doCmdSync("free -m | grep Swap")
        var swapTotal = 0
        var swapUse = 0
        if (swapInfo.contains("Swap")) {
            try {
                val swap =
                    swapInfo.substring(swapInfo.indexOf(" "), swapInfo.lastIndexOf(" ")).trim()
                if (Regex("[\\d]+[\\s]{1,}[\\d]{1,}").matches(swap)) {
                    swapTotal = swap.substring(0, swap.indexOf(" ")).trim().toInt()
                    swapUse = swap.substring(swap.indexOf(" ")).trim().toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val memInfo = memoryUtils.memoryInfo
        val state = MemoryState(
            ramTotalSize = totalMem.toFloat(),
            ramUsedSize = availMem.toFloat(),
            swapTotalSize = swapTotal.toFloat(),
            swapUsedSize = swapUse.toFloat(),
            swapCached = "${(memInfo.swapCached / 1024)}MB",
            dirty = "${(memInfo.dirty / 1024)}MB"
        )

        memoryState.value = state

        ramPercentage.value =
            (memoryState.value.ramTotalSize - memoryState.value.ramUsedSize) / memoryState.value.ramTotalSize * 100f
        swapPercentage.value =
            (memoryState.value.swapTotalSize - memoryState.value.swapUsedSize) / memoryState.value.swapTotalSize * 100f

        val gpuFreq = GpuUtils.getGpuFreq() + "Mhz"
        val gpuLoad = GpuUtils.getGpuLoad()

        val gpuState = GpuState(
            total = 100f,
            used = gpuLoad.toFloat(),
            freq = gpuFreq,
            kernel = "${GpuInfoUtil.glVendor} ${GpuInfoUtil.glRenderer}\n${GpuInfoUtil.glVersion}"
        )

        gpuStateMutableState.value = gpuState

        coreCount = cpuFrequencyUtil.coreCount

        val cores = java.util.ArrayList<CpuCoreInfo>()
        for (coreIndex in 0 until coreCount) {
            val core = CpuCoreInfo(coreIndex)

            core.currentFreq = cpuFrequencyUtil.getCurrentFrequency("cpu$coreIndex")
            if (!maxFreq.containsKey(coreIndex) || (core.currentFreq != "" && maxFreq[coreIndex].isNullOrEmpty())) {
                maxFreq[coreIndex] = cpuFrequencyUtil.getCurrentMaxFrequency("cpu$coreIndex")
            }
            core.maxFreq = maxFreq[coreIndex]

            if (!minFreq.containsKey(coreIndex) || (core.currentFreq != "" && minFreq[coreIndex].isNullOrEmpty())) {
                minFreq.put(coreIndex, cpuFrequencyUtil.getCurrentMinFrequency("cpu$coreIndex"))
            }
            core.minFreq = minFreq[coreIndex]
            cores.add(core)
        }

        val loads = cpuLoadUtils.cpuLoad
        for (core in cores) {
            if (loads.containsKey(core.coreIndex)) {
                core.loadRatio = loads[core.coreIndex]!!
            }
        }

        val map = HashMap<String, Int>()
        cores.onEach {
            val i = map[it.maxFreq]
            if (i == null) {
                map[it.maxFreq] = 1
            } else {
                map[it.maxFreq] = (map[it.maxFreq] ?: 1) + 1
            }
        }

        val list = arrayListOf<Int>()
        map.onEach {
            list.add(it.value)
        }
        list.sort()
        list.reverse()

        val stringBuilder = StringBuilder("${cores.size} Cores")
        stringBuilder.append("(")
        list.indices.onEach {
            stringBuilder.append("${list[it]}")
            if (it < list.size - 1) {
                stringBuilder.append("+")
            }
        }
        stringBuilder.append(")")

        val temp = try {
            val cmdSync = KeepShellPublic.doCmdSync("cat /sys/class/thermal/thermal_zone0/temp")
            String.format("%.2f%s", cmdSync.toLong().toFloat() / 1000f, "℃")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

        cpuState.value =
            CpuState(
                cpuFrequencyUtil.coreCount,
                cores,
                loads,
                temp,
                stringBuilder.toString()
            )

        withContext(Dispatchers.IO) {
            val processList = processUtils.allProcess
            loadLabel(processList)
            val filterAppList = filterAppList(processList)
            processState.value = filterAppList
        }

        val bms = "/sys/class/power_supply/bms/uevent"
        val battery = "/sys/class/power_supply/battery/uevent"
        val path = (if (RootFile.fileExists(bms)) {
            bms
        } else if (RootFile.fileExists(battery)) {
            battery
        } else {
            ""
        })
        if (path.isNotEmpty()) {
            val batteryInfos = KernelProp.getProp(path)
            val infos =
                batteryInfos.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var io = 0
            var level = 0
            var voltage = 0f
            for (item in infos) {
                try {
                    if (item.startsWith("POWER_SUPPLY_VOLTAGE_NOW=")) {
                        val keyword = "POWER_SUPPLY_VOLTAGE_NOW="
                        voltage = str2voltage(item.substring(keyword.length, item.length), "").toFloat()
                    }else if (item.startsWith("POWER_SUPPLY_CURRENT_NOW=")) {
                        val keyword = "POWER_SUPPLY_CURRENT_NOW="
                        val substring = item.substring(keyword.length, item.length)
                        io = substring.toInt() / ioState.value
                        continue
                    }else if (item.startsWith("POWER_SUPPLY_CAPACITY=")) {
                        val keyword = "POWER_SUPPLY_CAPACITY="
                        level = item.substring(keyword.length, item.length).toInt()
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val power = (voltage * io * 100).toInt() / 100f / 1000f * -1f
            otherInfoState.value = OtherInfoState(
                power,
                level,
                voltage,
                BatteryUtils.getBatteryTemperature().temperature,
                elapsedRealtimeStr()
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun elapsedRealtimeStr(): String {
    val timer = SystemClock.elapsedRealtime() / 1000
    return String.format("%02d:%02d:%02d", timer / 3600, timer % 3600 / 60, timer % 60)
}


private fun str2voltage(str: String, tag: String = "v"): String {
    val value = str.substring(0, if (str.length > 4) 4 else str.length).toDouble()

    return (if (value > 3000) {
        value / 1000
    } else if (value > 300) {
        value / 100
    } else if (value > 30) {
        value / 10
    } else {
        value
    }).toString() + tag
}

@Stable
data class MemoryState(
    val ramTotalSize: Float,
    val ramUsedSize: Float,
    val swapTotalSize: Float,
    val swapUsedSize: Float,
    val swapCached: String? = "N/A",
    val dirty: String? = "N/A"
)

@Stable
data class GpuState(
    val total: Float,
    val used: Float,
    val freq: String? = null,
    val kernel: String? = null
)

@Stable
data class CpuState(
    val coreCount: Int = -1,
    val cores: ArrayList<CpuCoreInfo>,
    val loads: HashMap<Int, Double>,
    val cpuTemp: String? = null,
    val socType: String? = null
)

@Composable
fun ProcessItem(processInfo: ProcessInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        if (isAndroidProcess(processInfo)) {
            ProgressIcon(processInfo)
            Spacer(modifier = Modifier.size(4.dp))
        }
        Text(
            text = processInfo.friendlyName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "${processInfo.getCpu()}%",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            modifier = Modifier.alpha(.5f)
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
fun ProgressIcon(processInfo: ProcessInfo) {

    var iconState by remember {
        mutableStateOf((androidIcon as BitmapDrawable).bitmap)
    }

    LaunchedEffect(processInfo) {
        withContext(Dispatchers.IO) {
            var icon: Drawable? = null
            try {
                val name = if (processInfo.name.contains(":")) processInfo.name.substring(
                    0,
                    processInfo.name.indexOf(":")
                ) else processInfo.name
                val installInfo = pm.getPackageInfo(name, 0)
                icon = installInfo.applicationInfo.loadIcon(pm)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (icon != null) {
                    iconState = drawableToBitmap(icon)
                } else {
                    iconState = (androidIcon as BitmapDrawable).bitmap
                }
            }
        }
    }

    Image(
        bitmap = iconState.asImageBitmap(),
        contentDescription = "",
        modifier = Modifier.size(20.dp)
    )
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    //声明将要创建的bitmap
    val bitmap: Bitmap
    //获取图片宽度
    val width = drawable.intrinsicWidth
    //获取图片高度
    val height = drawable.intrinsicHeight
    //图片位深，PixelFormat.OPAQUE代表没有透明度，RGB_565就是没有透明度的位深，否则就用ARGB_8888。详细见下面图片编码知识。
    val config =
        if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    //创建一个空的Bitmap
    bitmap = Bitmap.createBitmap(width, height, config)
    //在bitmap上创建一个画布
    val canvas = Canvas(bitmap)
    //设置画布的范围
    drawable.setBounds(0, 0, width, height)
    //将drawable绘制在canvas上
    drawable.draw(canvas)
    return bitmap
}


private val nameCache = HashMap<String, String>()
private fun loadLabel(processes: ArrayList<ProcessInfo>) {
    for (item in processes) {
        if (isAndroidProcess(item)) {
            if (nameCache.containsKey(item.name)) {
                item.friendlyName = nameCache.get(item.name)
            } else {
                val name = if (item.name.contains(":")) item.name.substring(
                    0,
                    item.name.indexOf(":")
                ) else item.name
                try {
                    val app = pm.getApplicationInfo(name, 0)
                    item.friendlyName = "" + app.loadLabel(pm)
                } catch (ex: java.lang.Exception) {
                    item.friendlyName = name
                } finally {
                    nameCache[item.name] = item.friendlyName
                }
            }
        } else {
            item.friendlyName = item.name
        }
    }
}

val SORT_MODE_DEFAULT = 1
val SORT_MODE_CPU = 4
val SORT_MODE_RES = 8
val SORT_MODE_PID = 16

val FILTER_ALL = 1
val FILTER_OTHER = 4
val FILTER_ANDROID_USER = 8
val FILTER_ANDROID_SYSTEM = 16
val FILTER_ANDROID = 32
private fun keywordSearch(item: ProcessInfo, text: String): Boolean {
    return item.friendlyName.toString().lowercase(Locale.ROOT)
        .contains(text) || item.name.toString().lowercase().contains(text) || item.user.toString()
        .lowercase().contains(text) || item.command.toString().lowercase()
        .contains(text) || item.cmdline.toString().lowercase().contains(text)
}

private var sortMode: Int = SORT_MODE_CPU
private var keywords = ""
private var filterMode: Int = FILTER_ANDROID_USER
private fun filterAppList(processes: ArrayList<ProcessInfo>): ArrayList<ProcessInfo> {
    val text = keywords.lowercase()
    val keywordsEmpty = text.isEmpty()
    return ArrayList(processes.filter { it ->
        (keywordsEmpty || keywordSearch(it, text)) && (
                when (filterMode) {
                    FILTER_ALL -> true
                    FILTER_ANDROID_USER -> isAndroidUserProcess(it)
                    FILTER_ANDROID_SYSTEM -> isSystemProcess(it)
                    FILTER_ANDROID -> isAndroidProcess(it)
                    FILTER_OTHER -> !isAndroidProcess(it)
                    else -> true
                })
    }.sortedBy {
        when (sortMode) {
            SORT_MODE_DEFAULT -> it.pid
            SORT_MODE_CPU -> -(it.getCpu() * 10).toInt()
            SORT_MODE_RES -> -(it.res * 100).toInt()
            SORT_MODE_PID -> -it.pid
            else -> it.pid
        }
    })
}

private val regexUser = Regex("u[0-9]+_.*")
private val regexPackageName = Regex(".*\\..*")
private fun isAndroidProcess(processInfo: ProcessInfo): Boolean {
    return (processInfo.command.contains("app_process") && processInfo.name.matches(regexPackageName))
}

private fun isSystemProcess(processInfo: ProcessInfo): Boolean {
    return isAndroidProcess(processInfo) && !processInfo.user.matches(regexUser)
}

private fun isAndroidUserProcess(processInfo: ProcessInfo): Boolean {
    return isAndroidProcess(processInfo) && processInfo.user.matches(regexUser)
}

@Composable
fun CpuItem(column: Int, cpuState: MutableState<CpuState>) {
    Column(
        modifier = Modifier
            .padding(top = 6.dp, end = 6.dp)
            .fillMaxWidth()
            .height(75.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cpuCoreInfo = cpuState.value.cores[column]
        CpuChart(
            progress = cpuCoreInfo.loadRatio.toFloat(),
            modifier = Modifier
                .padding(bottom = 4.dp, start = 6.dp, end = 6.dp)
                .fillMaxSize()
                .weight(1f)
                .alpha(.5f)
        ) {
            Text(
                text = String.format("%d%s", cpuCoreInfo.loadRatio.toInt(), "%"),
                style = MaterialTheme.typography.labelSmall
            )
        }
        var currentFreq by remember {
            mutableStateOf("")
        }

        var min by remember {
            mutableStateOf("")
        }

        LaunchedEffect(currentFreq) {
            withContext(Dispatchers.IO) {
                currentFreq = try {
                    "${cpuCoreInfo.currentFreq.toLong() / 1000}MHz"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "N/A"
                }

                min = try {
                    "${cpuCoreInfo.minFreq.toLong() / 1000}~${cpuCoreInfo.maxFreq.toLong() / 1000}MHz"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "N/A"
                }
            }
        }

        Text(text = currentFreq, style = MaterialTheme.typography.bodySmall)
        Text(
            text = min,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            modifier = Modifier.alpha(.5f)
        )
    }
}