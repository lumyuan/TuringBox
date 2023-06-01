package io.github.lumyuan.turingbox.common.shell

/**
 * Created by Hello on 2017/8/8.
 */

object PropsUtils {
    /**
     * 获取属性
     *
     * @param propName 属性名称
     * @return 内容
     */
    fun getProp(propName: String): String {
        return KeepShellPublic.doCmdSync("getprop \"$propName\"")
    }

    fun setProp(propName: String, value: String): Boolean {
        return KeepShellPublic.doCmdSync("setprop \"$propName\" \"$value\"") != "error"
    }
}
