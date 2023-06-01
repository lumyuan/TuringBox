package io.github.lumyuan.turingbox.common.data.customer

import android.content.Context
import android.os.BatteryManager
import io.github.lumyuan.turingbox.common.data.EventType
import io.github.lumyuan.turingbox.common.data.GlobalStatus
import io.github.lumyuan.turingbox.common.data.IEventReceiver
import io.github.lumyuan.turingbox.common.store.ChargeSpeedStore
import io.github.lumyuan.turingbox.common.store.SpfConfig
import java.util.*

class ChargeCurve(context: Context) : IEventReceiver {
    private val storage = ChargeSpeedStore(context)
    private var timer: Timer? = null
    private var batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private var globalSPF = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)

    override fun eventFilter(eventType: EventType): Boolean {
        return when (eventType) {
            EventType.POWER_CONNECTED,
            EventType.POWER_DISCONNECTED,
            EventType.BATTERY_CHANGED -> {
                true
            }
            else -> false
        }
    }

    override fun onReceive(eventType: EventType, data: HashMap<String, Any>?) {
        when (eventType) {
            EventType.POWER_CONNECTED -> {
                if (GlobalStatus.batteryCapacity != -1 && GlobalStatus.batteryCapacity != storage.lastCapacity()) {
                    storage.clearAll()
                }
            }
            EventType.POWER_DISCONNECTED -> {
                cancelUpdate()
            }
            EventType.BATTERY_CHANGED -> {
                if (timer == null && GlobalStatus.batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                    // storage.handleConflics(GlobalStatus.batteryCapacity)

                    startUpdate()
                }
            }
            else -> {
            }
        }
    }

    override val isAsync: Boolean
        get() = true

    override fun onSubscribe() {

    }

    override fun onUnsubscribe() {

    }

    private fun startUpdate() {
        if (timer == null) {
            timer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        saveLog()
                    }
                }, 15000, 1000)
            }
        }
    }

    private fun saveLog() {
        if (GlobalStatus.batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
            // 电流
            GlobalStatus.batteryCurrentNow = (
                    batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) /
                            globalSPF.getInt(SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT, SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT_DEFAULT)
                    )
            batteryManager.getIntProperty(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE)

            if (Math.abs(GlobalStatus.batteryCurrentNow) > 100) {
                storage.addHistory(
                        GlobalStatus.batteryCurrentNow,
                        GlobalStatus.batteryCapacity,
                        GlobalStatus.updateBatteryTemperature()
                )
            }
        } else {
            cancelUpdate()
        }
    }

    private fun cancelUpdate() {
        timer?.run {
            cancel()
            timer = null
        }
    }
}