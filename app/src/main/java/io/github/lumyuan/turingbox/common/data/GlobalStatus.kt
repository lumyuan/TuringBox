package io.github.lumyuan.turingbox.common.data

import android.os.BatteryManager
import io.github.lumyuan.turingbox.common.permissions.CheckRootStatus.Companion.lastCheckResult
import io.github.lumyuan.turingbox.common.shell.BatteryUtils

object GlobalStatus {
    var temperatureCurrent = -1f
        private set
    private var batteryTempTime = 0L
    fun setBatteryTemperature(temperature: Float) {
        batteryTempTime = System.currentTimeMillis()
        temperatureCurrent = temperature
    }

    /**
     * 获取实时温度（如果举例上次更新已过去较长时间，使用ROOT权限重新获取温度）
     */
    fun updateBatteryTemperature(): Float {
        // 将更新频率控制在>5秒
        if (lastCheckResult && System.currentTimeMillis() - 5000 >= batteryTempTime) {
            // 更新电池温度
            val temperature = BatteryUtils.getBatteryTemperature().temperature
            if (temperature > 10 && temperature < 100) {
                setBatteryTemperature(temperature)
            }
        }
        return temperatureCurrent
    }

    var batteryCapacity = -1
    var batteryCurrentNow: Long = -1
    var batteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN
    var lastPackageName = ""
    var homeMessage: String? = null
}