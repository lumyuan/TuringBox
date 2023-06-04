package io.github.lumyuan.turingbox.windows.main.device

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.basic.startBrowser

@Stable
data class OtherInfoState(val power: Float, val capacity: Int, val level: Float, val temperature: Float, val elapsedRealtime: String)

@Composable
fun OtherInfoCard(otherInfoState: MutableState<OtherInfoState>, ioState: MutableState<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        LeftCard(otherInfoState)
        RightCard(otherInfoState)
    }
}

@Composable
fun RowScope.LeftCard(otherInfoState: MutableState<OtherInfoState>) {
    Card(
        modifier = Modifier
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 16.dp)
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BatteryPower(otherInfoState)
            BatteryStatus(otherInfoState)
            BatteryThermal(otherInfoState)
        }
    }
}

@Composable
fun BatteryPower(otherInfoState: MutableState<OtherInfoState>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_battery_power),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(20.dp)
                .alpha(.6f)
        )
        Text(
            text = "${String.format("%.2f", otherInfoState.value.power)}W",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .alpha(.6f)
        )
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {

                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_electric_current),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun BatteryStatus(otherInfoState: MutableState<OtherInfoState>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val value = otherInfoState.value

        Icon(
            painter = painterResource(
                id = when (value.capacity) {
                    in 0 until 25 -> R.drawable.icon_battery_1
                    in 25 until 50 -> R.drawable.icon_battery_2
                    in 50 until 75 -> R.drawable.icon_battery_3
                    in 75 .. 100 -> R.drawable.icon_battery_4
                    else -> R.drawable.icon_battery_4
                }
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(20.dp)
                .alpha(.6f)
                .rotate(-90f)
        )
        Text(
            text = "${value.capacity}%\t${String.format("%.2f", value.level)}v",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .alpha(.6f)
        )
    }
}

@Composable
fun BatteryThermal(otherInfoState: MutableState<OtherInfoState>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_temperature),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(20.dp)
                .alpha(.6f)
        )
        Text(
            text = String.format("%.2f℃", otherInfoState.value.temperature),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .alpha(.6f)
        )
    }
}

@Composable
fun RowScope.RightCard(otherInfoState: MutableState<OtherInfoState>) {
    Card(
        modifier = Modifier
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AndroidVersion()
            RunningTime(otherInfoState)
            TuringBoxWebsite()
        }
    }
}

@Composable
fun AndroidVersion() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon_android),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(20.dp)
                .alpha(.6f)
        )
        Text(
            text = "Android ${Build.VERSION.RELEASE}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .alpha(.6f)
        )
    }
}

@Composable
fun RunningTime(otherInfoState: MutableState<OtherInfoState>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_clock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(20.dp)
                .alpha(.6f)
        )
        Text(
            text = String.format("%s %s", stringResource(id = R.string.text_system_running_time), otherInfoState.value.elapsedRealtime),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .alpha(.6f)
        )
    }
}

@Composable
fun TuringBoxWebsite() {
    val context = LocalContext.current as ComponentActivity
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.startBrowser("https://github.com/lumyuan/TuringBox")
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon_global),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(20.dp)
                .alpha(.6f)
        )
        Text(
            text = stringResource(id = R.string.view_to_websit),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 12.dp)
                .fillMaxWidth()
                .weight(1f)
                .alpha(.6f)
        )
    }
}
