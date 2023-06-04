package io.github.lumyuan.turingbox.windows.main.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.ui.widget.IndicatorComponent
import io.github.lumyuan.turingbox.windows.main.GpuState

@Composable
fun GpuCard(gpuState: MutableState<GpuState>) {
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            GpuProgress(gpuState)
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                FreqText(gpuState)
                Spacer(modifier = Modifier.size(4.dp))
                UsedText(gpuState)
                Spacer(modifier = Modifier.size(4.dp))
                KernelText(gpuState)
            }
        }
    }
}

@Composable
fun ColumnScope.KernelText(gpuState: MutableState<GpuState>) {
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

@Composable
fun UsedText(gpuState: MutableState<GpuState>) {
    Text(
        text = String.format(
            stringResource(id = R.string.text_gpu_used),
            gpuState.value.used.toInt(),
            "%"
        ),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun ColumnScope.FreqText(gpuState: MutableState<GpuState>) {
    val freqText = gpuState.value.freq
    AnimatedVisibility(visible = freqText != null) {
        Text(text = freqText.toString(), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun GpuProgress(gpuState: MutableState<GpuState>) {
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
}
