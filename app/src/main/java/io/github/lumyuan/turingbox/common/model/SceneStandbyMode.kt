package io.github.lumyuan.turingbox.common.model

import android.content.Context
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.shell.KeepShell
import io.github.lumyuan.turingbox.common.util.AppListHelper

class SceneStandbyMode(private val context: Context, private val keepShell: KeepShell) {
    companion object {
        public val configSpfName = "SceneStandbyList"
    }

    private val stateProp = "persist.turingbox.suspend"

    public fun getCmds(on: Boolean): String {
        val cmds = StringBuffer()
        if (on) {
            val apps = AppListHelper(context).getAll()
            val command = if (on) "suspend" else "unsuspend"

            val blackListConfig = context.getSharedPreferences(configSpfName, Context.MODE_PRIVATE)
            val whiteList = context.resources.getStringArray(R.array.scene_standby_white_list)
            for (app in apps) {
                if (!whiteList.contains(app.packageName)) {
                    if (((app.appType == AppInfo.AppType.SYSTEM || app.updated) && blackListConfig.getBoolean(
                            app.packageName,
                            false
                        )) || (app.appType == AppInfo.AppType.USER && (!app.updated) && blackListConfig.getBoolean(
                            app.packageName,
                            true
                        ))
                    ) {
                        cmds.append("pm ")
                        cmds.append(command)
                        cmds.append(" \"")
                        cmds.append(app.packageName)
                        cmds.append("\"\n")
                        if (on) {
                            cmds.append("am force-stop ")
                            cmds.append(" \"")
                            cmds.append(app.packageName)
                            cmds.append("\"\n")
                            // TODO:真的要这么做吗？
                            // if (app.packageName.equals("com.google.android.gsf")) {
                            //     cmds.append("pm disable com.google.android.gsf 2> /dev/null\n")
                            // }
                        }
                    }
                }
            }
            cmds.append("\n")
            cmds.append("sync\n")
            cmds.append("echo 3 > /proc/sys/vm/drop_caches\n")
            cmds.append("setprop ")
            cmds.append(stateProp)
            cmds.append(" 1")
            cmds.append("\n")
        } else {
            cmds.append(
                "for app in `pm list package | cut -f2 -d ':'`; do\n" +
                        "      pm unsuspend \$app 1 > /dev/null\n" +
                        "    done\n"
            )
            cmds.append("setprop ")
            cmds.append(stateProp)
            cmds.append(" 0")
            cmds.append("\n")
        }
        return cmds.toString()
    }

    public fun on() {
        if (keepShell.doCmdSync("getprop $stateProp").equals("1")) {
            return
        }
        keepShell.doCmdSync(getCmds(true))
    }

    public fun off() {
        if (keepShell.doCmdSync("getprop $stateProp").equals("0")) {
            return
        }
        keepShell.doCmdSync(getCmds(false))
    }
}
