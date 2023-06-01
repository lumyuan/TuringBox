package io.github.lumyuan.turingbox.common.pojo

open class AppInfo {
    var appName: String = ""
    var packageName: String = ""

    // 是否未找到此应用
    var notFound: Boolean = false
    var selected: Boolean = false
}