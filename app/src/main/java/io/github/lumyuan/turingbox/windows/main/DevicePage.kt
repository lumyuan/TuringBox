package io.github.lumyuan.turingbox.windows.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.ui.compose.launchTimerJob
import io.github.lumyuan.turingbox.ui.widget.IndicatorComponent
import io.github.lumyuan.turingbox.ui.widget.ProgressBar
import kotlinx.coroutines.withContext

@Preview(showBackground = true)
@Composable
private fun PreviewDevicePage() {
    DevicePage()
}

@Composable
fun DevicePage() {

    val memoryState = remember {
        mutableStateOf(MemoryState(0f, 0f, 0f, 0f))
    }

    val random = java.util.Random()
    val coroutineContext = LocalLifecycleOwner.current.lifecycleScope.coroutineContext
    launchTimerJob(2000) {
        withContext(coroutineContext) {
            memoryState.value = MemoryState(10000f, random.nextInt(10001).toFloat(), 10000f, random.nextInt(10001).toFloat())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        MemoryCard(memoryState)
        SocCard()
        CpuFreqCard()
        OtherInfoCard()
    }
}

@Stable
data class MemoryState(
    val ramTotalSize: Float,
    val ramUsedSize: Float,
    val swapTotalSize: Float,
    val swapUsedSize: Float
)

@Composable
fun MemoryCard(memoryState: MutableState<MemoryState>) {
    val ramPercentage = memoryState.value.ramUsedSize / memoryState.value.ramTotalSize * 100f
    val swapPercentage = memoryState.value.swapUsedSize / memoryState.value.swapTotalSize * 100f

    val ramPercentageAnimation = remember {
        Animatable(initialValue = 0f)
    }

    LaunchedEffect(ramPercentage){
        if (ramPercentage in 0f .. 100f){
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

    LaunchedEffect(swapPercentage){
        if (swapPercentage in 0f .. 100f){
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
                                text = "${String.format("%.2f", ramPercentageAnimation.value)}%(16GB)",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .alpha(.8f)
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
                                text = "${String.format("%.2f", swapPercentageAnimation.value)}%(6GB)",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .alpha(.8f)
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
            }
        }
    }
}

@Composable
fun SocCard() {

}

@Composable
fun CpuFreqCard() {

}

@Composable
fun OtherInfoCard() {

}
