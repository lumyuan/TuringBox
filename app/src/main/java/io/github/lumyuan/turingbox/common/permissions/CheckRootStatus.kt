package io.github.lumyuan.turingbox.common.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.PermissionChecker
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.TuringBox
import io.github.lumyuan.turingbox.common.shell.KeepShellPublic
import io.github.lumyuan.turingbox.common.util.CommonCmds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 检查获取root权限
 * Created by helloklf on 2017/6/3.
 */

class CheckRootStatus(
    var context: Context,
    private val next: () -> Unit = {},
    private var disableSeLinux: Boolean = false,
    private val onRootRejected: () -> Unit = {},
    private val onRootTimeout: () -> Unit = {}
) {
    fun forceGetRoot() {
        if (lastCheckResult) {
            next.invoke()
        } else {
            var completed = false
            CoroutineScope(Dispatchers.IO).launch {
                setRootStatus(KeepShellPublic.checkRoot())
                if (completed) {
                    return@launch
                }
                completed = true
                if (lastCheckResult) {
                    if (disableSeLinux) {
                        KeepShellPublic.doCmdSync(CommonCmds.DisableSELinux)
                    }
                    next.invoke()
                } else {
                    KeepShellPublic.tryExit()
                    onRootRejected.invoke()
                }
            }
            CoroutineScope(Dispatchers.IO).launch {
                delay(10 * 1000)
                if (!completed) {
                    KeepShellPublic.tryExit()
                    onRootTimeout.invoke()
                }
            }
        }
    }

    companion object {
        private var rootStatus = false

        fun checkRootAsync() {
            CoroutineScope(Dispatchers.IO).launch {
                setRootStatus(KeepShellPublic.checkRoot())
            }
        }

        private fun checkPermission(context: Context, permission: String): Boolean =
            PermissionChecker.checkSelfPermission(
                context,
                permission
            ) == PermissionChecker.PERMISSION_GRANTED

        fun grantPermission(context: Context) {
            val cmds = StringBuilder()
            // 必需的权限
            val requiredPermission = arrayListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CHANGE_CONFIGURATION,
                Manifest.permission.WRITE_SECURE_SETTINGS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                // Manifest.permission.UNINSTALL_SHORTCUT,
                // Manifest.permission.INSTALL_SHORTCUT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requiredPermission.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
            requiredPermission.forEach {
                if (it == Manifest.permission.MANAGE_EXTERNAL_STORAGE) {
                    if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                        cmds.append("appops set --uid ${context.packageName} MANAGE_EXTERNAL_STORAGE allow\n")
                    }
                } else if (it == Manifest.permission.SYSTEM_ALERT_WINDOW) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(context)) {
                            // 未允许悬浮窗
                            try {
                                //启动Activity让用户授权
                                Toast.makeText(
                                    context,
                                    R.string.no_float_window_permission,
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + context.packageName)
                                )
                                context.startActivity(intent);
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        if (!checkPermission(context, it)) {
                            cmds.append("pm grant ${context.packageName} $it\n")
                        }
                    }
                } else {
                    if (!checkPermission(context, it)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val option = it.substring("android.permission.".length)
                            cmds.append("appops set ${context.packageName} $option allow\n")
                        }
                        cmds.append("pm grant ${context.packageName} $it\n")
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!checkPermission(
                        context,
                        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    )
                ) {
                    cmds.append("dumpsys deviceidle whitelist +${context.packageName};\n")
                }
            }

            /*
            // 不支持使用ROOT权限进行设置
            if (!checkPermission(context, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)) {
                cmds.append("pm grant ${context.packageName} android.permission.BIND_NOTIFICATION_LISTENER_SERVICE;\n")
            }
            if (!checkPermission(context, Manifest.permission.WRITE_SETTINGS)) {
                cmds.append("pm grant ${context.packageName} android.permission.WRITE_SETTINGS;\n")
            }
            */
            KeepShellPublic.doCmdSync(cmds.toString())
        }

        // 最后的ROOT检测结果
        val lastCheckResult: Boolean
            get() {
                return rootStatus
            }

        private fun setRootStatus(root: Boolean) {
            rootStatus = root
            TuringBox.setBoolean("root", root)
        }
    }
}
