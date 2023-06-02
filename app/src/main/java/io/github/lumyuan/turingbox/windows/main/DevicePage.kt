package io.github.lumyuan.turingbox.windows.main

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.BatteryManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.device.GpuInfoUtil
import io.github.lumyuan.turingbox.common.model.CpuCoreInfo
import io.github.lumyuan.turingbox.common.shell.CpuFrequencyUtils
import io.github.lumyuan.turingbox.common.shell.CpuLoadUtils
import io.github.lumyuan.turingbox.common.shell.GpuUtils
import io.github.lumyuan.turingbox.common.shell.KeepShellPublic
import io.github.lumyuan.turingbox.common.shell.MemoryUtils
import io.github.lumyuan.turingbox.ui.compose.launchTimerJob
import io.github.lumyuan.turingbox.ui.widget.CpuChart
import io.github.lumyuan.turingbox.ui.widget.IndicatorComponent
import io.github.lumyuan.turingbox.ui.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import java.util.Collections

@Preview(showBackground = true)
@Composable
private fun PreviewDevicePage() {
    DevicePage()
}

private val memoryUtils by lazy { MemoryUtils() }
private val cpuLoadUtils by lazy { CpuLoadUtils() }
private val cpuFrequencyUtil by lazy { CpuFrequencyUtils() }
private lateinit var batteryManager: BatteryManager
private lateinit var activityManager: ActivityManager

private var coreCount = -1
private var minFreq = HashMap<Int, String>()
private var maxFreq = HashMap<Int, String>()

@Composable
fun DevicePage() {


    val memoryState = remember {
        mutableStateOf(MemoryState(0f, 0f, 0f, 0f))
    }

    val gpuStateMutableState = remember {
        mutableStateOf(GpuState(0f, 0f))
    }

    val cpuState = remember {
        mutableStateOf(CpuState(-1, ArrayList(), HashMap()))
    }

    val context = LocalContext.current

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

    activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        LaunchedEffect(this) {
            launchTimerJob(Dispatchers.IO, 2000) {
                updateInfo(memoryState, gpuStateMutableState, cpuState)
            }
        }
        MemoryCard(memoryState)
        SocCard(gpuStateMutableState)
        CpuFreqCard(cpuState)
        OtherInfoCard()
    }
}

private fun updateInfo(
    memoryState: MutableState<MemoryState>,
    gpuStateMutableState: MutableState<GpuState>,
    cpuState: MutableState<CpuState>
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
            CpuState(cpuFrequencyUtil.coreCount, cores, loads, temp, stringBuilder.toString())
    } catch (e: Exception) {
        e.printStackTrace()
    }
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

@Composable
fun MemoryCard(memoryState: MutableState<MemoryState>) {
    val ramPercentage =
        (memoryState.value.ramTotalSize - memoryState.value.ramUsedSize) / memoryState.value.ramTotalSize * 100f
    val swapPercentage =
        (memoryState.value.swapTotalSize - memoryState.value.swapUsedSize) / memoryState.value.swapTotalSize * 100f

    val ramPercentageAnimation = remember {
        Animatable(initialValue = 0f)
    }

    LaunchedEffect(ramPercentage) {
        if (ramPercentage in 0f..100f) {
            ramPercentageAnimation.animateTo(
                targetValue = ramPercentage, animationSpec = tween(
                    durationMillis = 750, easing = FastOutSlowInEasing
                )
            )
        }
    }

    val swapPercentageAnimation = remember {
        Animatable(initialValue = 0f)
    }

    LaunchedEffect(swapPercentage) {
        if (swapPercentage in 0f..100f) {
            swapPercentageAnimation.animateTo(
                targetValue = swapPercentage, animationSpec = tween(
                    durationMillis = 750, easing = FastOutSlowInEasing
                )
            )
        }
    }

    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            IndicatorComponent(
                componentSize = 100.dp,
                backgroundIndicatorStrokeWidth = 12.dp,
                foregroundSweepAngle = ramPercentage
            ) {
                Text(
                    text = stringResource(id = R.string.text_memory),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = String.format("%.2f%s", ramPercentageAnimation.value, "%"),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ProgressBar(
                            modifier = Modifier.fillMaxWidth(),
                            progress = ramPercentage
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.text_ram),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${
                                    String.format(
                                        "%.2f",
                                        ramPercentageAnimation.value
                                    )
                                }%(${memoryState.value.ramTotalSize.toInt() / 1024 + 1}GB)",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .alpha(.6f)
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {

                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.app_options_clear2),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ProgressBar(
                            modifier = Modifier.fillMaxWidth(),
                            progress = swapPercentage
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.text_swap),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${
                                    String.format(
                                        "%.2f",
                                        swapPercentageAnimation.value
                                    )
                                }%(${memoryState.value.swapTotalSize.toInt() / 1024 + 1}GB)",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .alpha(.6f)
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {

                            }
                            .padding(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.app_options_clear),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    AnimatedVisibility(visible = memoryState.value.swapCached != null) {
                        Row {
                            Text(
                                text = "SwapCached",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = memoryState.value.swapCached.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .alpha(.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    AnimatedVisibility(visible = memoryState.value.swapCached != null) {
                        Row {
                            Text(
                                text = "Dirty",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = memoryState.value.dirty.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .alpha(.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Stable
data class GpuState(
    val total: Float,
    val used: Float,
    val freq: String? = null,
    val kernel: String? = null
)

@Composable
fun SocCard(gpuState: MutableState<GpuState>) {
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            IndicatorComponent(
                componentSize = 100.dp,
                backgroundIndicatorStrokeWidth = 12.dp,
                foregroundSweepAngle = gpuState.value.used
            ) {
                Text(
                    text = stringResource(id = R.string.text_gpu),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                val freqText = gpuState.value.freq
                AnimatedVisibility(visible = freqText != null) {
                    Text(text = freqText.toString(), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = String.format(
                        stringResource(id = R.string.text_gpu_used),
                        gpuState.value.used.toInt(),
                        "%"
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.size(4.dp))
                val kernel = gpuState.value.kernel
                AnimatedVisibility(visible = kernel != null) {
                    Text(
                        text = kernel.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Light
                        )
                    )
                }
            }
        }
    }
}

@Stable
data class CpuState(
    val coreCount: Int = -1,
    val cores: ArrayList<CpuCoreInfo>,
    val loads: HashMap<Int, Double>,
    val cpuTemp: String? = null,
    val socType: String? = null
)

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CpuFreqCard(cpuState: MutableState<CpuState>) {

    var cpuInfoState by remember {
        mutableStateOf(cpuState.value.cores)
    }

    cpuInfoState = cpuState.value.cores

    val totalFreq = cpuState.value.loads[-1]?.toInt() ?: 0
    val totalFreqText = String.format(
        stringResource(id = R.string.text_gpu_used),
        totalFreq,
        "%"
    )

    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                }
                Spacer(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp)
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
                )
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    val (text) = createRefs()
                    val (lay) = createRefs()
                    Text(
                        text = cpuState.value.cpuTemp ?: "",
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.ExtraLight),
                        modifier = Modifier.constrainAs(text) {
                            this.top.linkTo(parent.top)
                            this.end.linkTo(parent.end)
                        }
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.constrainAs(lay) {
                            this.linkTo(
                                top = parent.top,
                                bottom = parent.bottom,
                                start = parent.start,
                                end = parent.end
                            )
                        }
                    ) {
                        CpuChart(
                            modifier = Modifier
                                .padding(start = 28.dp, end = 28.dp)
                                .fillMaxSize()
                                .weight(1f)
                                .alpha(.5f),
                            progress = totalFreq.toFloat()
                        ) {
                            Text(text = "CPU")
                        }
                        Text(
                            text = cpuState.value.socType ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = totalFreqText,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .alpha(.5f)
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
            )
            val cpuLineCount = cpuState.value.coreCount / 2
            for (row in 1..2) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (column in cpuLineCount * (row - 1) until cpuLineCount * row) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            CpuItem(cpuInfoState[column])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CpuItem(cpuCoreInfo: CpuCoreInfo) {
    Column(
        modifier = Modifier
            .padding(top = 6.dp, end = 6.dp)
            .fillMaxWidth()
            .height(75.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        val currentFreq = try {
            "${cpuCoreInfo.currentFreq.toLong() / 1000}MHz"
        } catch (e: Exception) {
            e.printStackTrace()
            "N/A"
        }

        val min = try {
            "${cpuCoreInfo.minFreq.toLong() / 1000}~${cpuCoreInfo.maxFreq.toLong() / 1000}MHz"
        } catch (e: Exception) {
            e.printStackTrace()
            "N/A"
        }

        Text(text = currentFreq, style = MaterialTheme.typography.bodySmall)
        Text(
            text = min,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            modifier = Modifier.alpha(.5f)
        )
    }
}

@Composable
fun OtherInfoCard() {

}
