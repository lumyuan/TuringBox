package io.github.lumyuan.turingbox.ui.widget

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.concurrent.LinkedBlockingQueue

@Preview
@Composable
fun PreviewChart() {
    CpuChart(modifier = Modifier.size(120.dp), progress = 85f)
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CpuChart(
    modifier: Modifier = Modifier,
    progress: Float,
    chartWeight: Dp = 10.dp,
    defaultColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = .125f),
    midColor: Color = Color(0xFFFC8A1B),
    highColor: Color = Color(0xFFF9592F),
    content: @Composable (ColumnScope) -> Unit = {}
) {
    val loadHistory by remember {
        mutableStateOf(LinkedBlockingQueue<Int>())
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .drawWithContent {
                val width = drawContext.size.width
                val height = drawContext.size.height
                val chartCount = (width / chartWeight.toPx()).toInt()

                //居中偏移量
                val space = (width - chartCount * chartWeight.toPx()) / 2f
                //初始化集合
                if (loadHistory.size <= 0) {
                    for (i in 0 until chartCount - 1) {
                        loadHistory.add(0)
                    }
                }

                loadHistory.put(progress.toInt())
                if (loadHistory.size > chartCount) {
                    loadHistory.poll()
                }

                var index = 0
                loadHistory.onEach { ratio ->
                    var chartColor = if (ratio > 85) {
                        highColor
                    } else if (ratio > 65) {
                        midColor
                    } else {
                        defaultColor
                    }
                    chartColor = if (ratio > 50) {
                        chartColor.copy(alpha = 1f)
                    } else {
                        chartColor.copy(alpha = 0.5f + (ratio / 100.0f))
                    }
                    val top = if (ratio <= 2) {
                        height - 10f
                    } else if (ratio >= 98) {
                        0f
                    } else {
                        (100 - ratio) * height / 100
                    }
                    drawRoundRect(
                        color = chartColor,
                        topLeft = Offset(x = chartWeight.toPx() * index + space, y = top),
                        size = Size(chartWeight.toPx() * .9f, height - top),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    index++
                }
                drawContent()
            },
        content = { content(this) })
}