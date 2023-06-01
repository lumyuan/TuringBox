package io.github.lumyuan.turingbox

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import io.github.lumyuan.turingbox.common.data.customer.ChargeCurve
import io.github.lumyuan.turingbox.common.data.customer.ScreenOffCleanup
import io.github.lumyuan.turingbox.common.data.EventBus
import io.github.lumyuan.turingbox.common.data.publisher.BatteryState
import io.github.lumyuan.turingbox.common.data.publisher.ScreenState
import io.github.lumyuan.turingbox.common.model.TriggerIEventMonitor
import io.github.lumyuan.turingbox.common.permissions.Busybox
import io.github.lumyuan.turingbox.common.permissions.CheckRootStatus
import io.github.lumyuan.turingbox.common.shared.FileWrite
import io.github.lumyuan.turingbox.common.shell.ShellExecutor
import io.github.lumyuan.turingbox.common.store.SpfConfig

class TuringBox: Application() {

    companion object {
        lateinit var application: Application

        val globalConfig: SharedPreferences by lazy {
            application.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
        }

        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return globalConfig.getBoolean(key, defaultValue)
        }

        fun setBoolean(key: String, value: Boolean) {
            globalConfig.edit().putBoolean(key, value).apply()
        }

        fun getString(key: String, defaultValue: String): String? {
            return globalConfig.getString(key, defaultValue)
        }
    }

    // 锁屏状态监听
    private lateinit var screenState: ScreenState


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this

        // 安装busybox
        if (!Busybox.systemBusyboxInstalled()) {
            ShellExecutor.setExtraEnvPath(
                FileWrite.getPrivateFilePath(this, getString(R.string.toolkit_install_path))
            )
        }

        //锁屏状态检测
        screenState = ScreenState(this)
        screenState.autoRegister()

        // 事件任务
        EventBus.subscribe(TriggerIEventMonitor(this))

        // 充电曲线
        EventBus.subscribe(ChargeCurve(this))

        // 息屏自动关闭悬浮窗
        EventBus.subscribe(ScreenOffCleanup(this))

        // 如果上次打开应用成功获得root，触发一下root权限申请
        if (getBoolean("root", false)) {
            CheckRootStatus.checkRootAsync()
        }
    }

}