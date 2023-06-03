package io.github.lumyuan.turingbox.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.CoroutineContext

/**
 * 协程定时器
 */
fun launchTimerJob(scopeContext: CoroutineContext = Dispatchers.IO, intervalMillis: Long = 1500, action: suspend (CoroutineScope) -> Unit): Job =
    CoroutineScope(scopeContext).launch {
        while (isActive) {
            action(this)
            delay(intervalMillis)
        }
    }

class TimerState {
    var timerJob: Job? = null
    var timer: String by mutableStateOf("00:00:00")

    fun updateTimer() {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        timer = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
