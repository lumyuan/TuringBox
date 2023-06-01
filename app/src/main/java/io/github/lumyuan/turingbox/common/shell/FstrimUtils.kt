package io.github.lumyuan.turingbox.common.shell

class FstrimUtils(private val keepShell: KeepShell) {
    fun run() {
        keepShell.doCmdSync("" +
                "fstrim /data\n" +
                "fstrim /data\n" +
                "fstrim /system\n" +
                "fstrim /system\n" +
                "fstrim /cache\n" +
                "fstrim /cache\n" +
                "sm fstrim\n")
    }
}
