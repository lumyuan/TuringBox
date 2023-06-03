package io.github.lumyuan.turingbox.windows.main.device

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.shell.ProcessInfo
import io.github.lumyuan.turingbox.ui.widget.CpuChart
import io.github.lumyuan.turingbox.windows.main.CpuItem
import io.github.lumyuan.turingbox.windows.main.CpuState
import io.github.lumyuan.turingbox.windows.main.ProcessItem

@SuppressLint("MutableCollectionMutableState")
@Composable
fun CpuFreqCard(
    cpuState: MutableState<CpuState>,
    processState: MutableState<ArrayList<ProcessInfo>>
) {

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ProcessLayout(processState)
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
                    CpuTempText(text, cpuState)
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
                        CpuTotalLayout(cpuState)
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
            CorsList(cpuState)
        }
    }
}

@Composable
fun CorsList(cpuState: MutableState<CpuState>) {
    val cpuLineCount: () -> Int = { cpuState.value.coreCount / 2 }
    for (row in 1..2) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            CpuCoreRow(row, cpuLineCount, cpuState)
        }
    }
}

@Composable
fun RowScope.CpuCoreRow(row: Int, cpuLineCount: () -> Int, cpuState: MutableState<CpuState>) {
    val count by remember {
        derivedStateOf {
            cpuLineCount()
        }
    }
    for (column in count * (row - 1) until count * row) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CpuItem(column, cpuState)
        }
    }
}

@Composable
fun ColumnScope.CpuTotalLayout(cpuState: MutableState<CpuState>) {
    CpuChart(
        modifier = Modifier
            .padding(start = 28.dp, end = 28.dp)
            .fillMaxSize()
            .weight(1f)
            .alpha(.5f),
        progress = cpuState.value.loads[-1]?.toFloat() ?: 0f
    ) {
        Text(text = "CPU")
    }
    Text(
        text = cpuState.value.socType ?: "",
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = String.format(
            stringResource(id = R.string.text_gpu_used),
            cpuState.value.loads[-1]?.toInt() ?: 0,
            "%"
        ),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .alpha(.5f)
    )
}

@Composable
fun ConstraintLayoutScope.CpuTempText(text: ConstrainedLayoutReference, cpuState: MutableState<CpuState>) {
    Text(
        text = cpuState.value.cpuTemp ?: "",
        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.ExtraLight),
        modifier = Modifier.constrainAs(text) {
            this.top.linkTo(parent.top)
            this.end.linkTo(parent.end)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.ProcessLayout(processState: MutableState<ArrayList<ProcessInfo>>) {
    val isShow by remember {
        derivedStateOf {
            processState.value.size > 0
        }
    }

    AnimatedVisibility(visible = !isShow) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp)
            )
        }
    }
    AnimatedVisibility(visible = isShow) {
        LazyColumn {
            items(
                items = processState.value,
                /*key = {
                    it.name
                }*/
            ) {
                ProcessItem(processInfo = it)
            }
        }
    }
}
