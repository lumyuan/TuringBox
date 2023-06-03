package io.github.lumyuan.turingbox.windows.main.device

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.shell.KeepShellPublic
import io.github.lumyuan.turingbox.common.shell.MemoryUtils
import io.github.lumyuan.turingbox.ui.compose.launchTimerJob
import io.github.lumyuan.turingbox.ui.widget.IndicatorComponent
import io.github.lumyuan.turingbox.ui.widget.ProgressBar
import io.github.lumyuan.turingbox.windows.main.MemoryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MemoryCard(
    memoryState: MutableState<MemoryState>,
    ramPercentage: MutableState<Float>,
    swapPercentage: MutableState<Float>
) {
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
            IndicatorBar(ramPercentage)
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
                        RamProgressBar(ramPercentage)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.text_ram),
                                style = MaterialTheme.typography.labelSmall
                            )
                            RamText(ramPercentage, memoryState)
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
                        SwapProgress(swapPercentage)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.text_swap),
                                style = MaterialTheme.typography.labelSmall
                            )
                            SwapText(swapPercentage, memoryState)
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
                    SwapCached(memoryState)
                    Spacer(modifier = Modifier.size(16.dp))
                    Dirty(memoryState)
                }
            }
        }
    }
}

@Composable
fun RowScope.Dirty(memoryState: MutableState<MemoryState>) {
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

@Composable
fun RowScope.SwapCached(memoryState: MutableState<MemoryState>) {
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
}

@Composable
fun SwapText(
    swapPercentage: MutableState<Float>,
    memoryState: MutableState<MemoryState>
) {


    var total by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(total) {
        withContext(Dispatchers.IO) {
            total = memoryState.value.swapTotalSize.toInt() / 1024 + 1
        }
    }

    Text(
        text = "${
            String.format(
                "%.2f",
                swapPercentage.value
            )
        }%(${total}GB)",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .padding(start = 16.dp)
            .alpha(.6f)
    )
}

@Composable
fun SwapProgress(swapPercentage: MutableState<Float>) {
    ProgressBar(
        modifier = Modifier.fillMaxWidth(),
        progress = swapPercentage.value
    )
}

@Composable
fun RamText(
    ramPercentage: MutableState<Float>,
    memoryState: MutableState<MemoryState>
) {

    var total by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(total) {
        withContext(Dispatchers.IO) {
            total = memoryState.value.ramTotalSize.toInt() / 1024 + 1
        }
    }

    Text(
        text = "${
            String.format(
                "%.2f",
                ramPercentage.value
            )
        }%(${total}GB)",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .padding(start = 16.dp)
            .alpha(.6f)
    )
}

@Composable
fun RamProgressBar(ramPercentage: MutableState<Float>) {
    ProgressBar(
        modifier = Modifier.fillMaxWidth(),
        progress = ramPercentage.value
    )
}

@Composable
fun IndicatorBar(
    ramPercentage: MutableState<Float>
) {

    val value = ramPercentage.value

    IndicatorComponent(
        componentSize = 100.dp,
        backgroundIndicatorStrokeWidth = 12.dp,
        foregroundSweepAngle = value
    ) {
        Text(
            text = stringResource(id = R.string.text_memory),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = String.format("%.2f%s", value, "%"),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
