package io.github.lumyuan.turingbox.common.shell

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.locks.ReentrantLock


class KeepShell(private var rootMode: Boolean = true) {
    private var p: Process? = null
    private var out: OutputStream? = null
    private var reader: BufferedReader? = null
    private var currentIsIdle = true // 是否处于闲置状态
    val isIdle: Boolean
        get() {
            return currentIsIdle
        }

    //尝试退出命令行程序
     fun tryExit() {
        try {
            out?.close()
            reader?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            p?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        enterLockTime = 0L
        out = null
        reader = null
        p = null
        currentIsIdle = true
    }

    //获取ROOT超时时间
    private val mLock = ReentrantLock()
    private val LOCK_TIMEOUT = 10000L
    private var enterLockTime = 0L

    private var checkRootState =
            "if [[ \$(id -u 2>&1) == '0' ]] || [[ \$(\$UID) == '0' ]] || [[ \$(whoami 2>&1) == 'root' ]] || [[ \$(set | grep 'USER_ID=0') == 'USER_ID=0' ]]; then\n" +
                    "  echo 'success'\n" +
                    "else\n" +
                    "if [[ -d /cache ]]; then\n" +
                    "  echo 1 > /cache/turing_root\n" +
                    "  if [[ -f /cache/turing_root ]] && [[ \$(cat /cache/turing_root) == '1' ]]; then\n" +
                    "    echo 'success'\n" +
                    "    rm -rf /cache/turing_root\n" +
                    "    return\n" +
                    "  fi\n" +
                    "fi\n" +
                    "exit 1\n" +
                    "exit 1\n" +
                    "fi\n"

    fun checkRoot(): Boolean {
        val r = doCmdSync(checkRootState).lowercase()
        return if (r == "error" || r.contains("permission denied") || r.contains("not allowed") || r == "not found") {
            if (rootMode) {
                tryExit()
            }
            false
        } else if (r.contains("success")) {
            true
        } else {
            if (rootMode) {
                tryExit()
            }
            false
        }
    }

    private fun getRuntimeShell() {
        if (p != null) return
        val getSu = Thread {
            try {
                mLock.lockInterruptibly()
                enterLockTime = System.currentTimeMillis()
                p = if (rootMode) ShellExecutor.getSuperUserRuntime() else ShellExecutor.getRuntime()
                out = p?.outputStream
                reader = p?.inputStream?.bufferedReader()
                if (rootMode) {
                    out?.run {
                        write(checkRootState.toByteArray(Charset.defaultCharset()))
                        flush()
                    }
                }
                Thread {
                    try {
                        val errorReader = p?.errorStream?.bufferedReader()
                        while (true) {
                            Log.e("KeepShellPublic", errorReader?.readLine() ?: "")
                        }
                    } catch (ex: Exception) {
                        Log.e("c", "" + ex.message)
                    }
                }.start()
            } catch (ex: Exception) {
                Log.e("getRuntime", "" + ex.message)
            } finally {
                enterLockTime = 0L
                mLock.unlock()
            }
        }
        getSu.start()
        getSu.join(10000)
        if (p == null && getSu.state != Thread.State.TERMINATED) {
            enterLockTime = 0L
            getSu.interrupt()
        }
    }

    private var br = "\n\n".toByteArray(Charset.defaultCharset())

    private val shellOutputCache = StringBuilder()
    private val startTag = "|SH>>|"
    private val endTag = "|<<SH|"
    private val startTagBytes = "\necho '$startTag'\n".toByteArray(Charset.defaultCharset())
    private val endTagBytes = "\necho '$endTag'\n".toByteArray(Charset.defaultCharset())

    //执行脚本
    fun doCmdSync(cmd: String): String {
        if (mLock.isLocked && enterLockTime > 0 && System.currentTimeMillis() - enterLockTime > LOCK_TIMEOUT) {
            tryExit()
            Log.e("doCmdSync-Lock", "线程等待超时${System.currentTimeMillis()} - $enterLockTime > $LOCK_TIMEOUT")
        }
        getRuntimeShell()


        try {
            mLock.lockInterruptibly()
            currentIsIdle = false

            out?.run {
                CoroutineScope(Dispatchers.IO).launch {
                    write(startTagBytes)
                    write(cmd.toByteArray(Charset.defaultCharset()))
                    write(endTagBytes)
                    flush()
                }
            }

            var upstart = true
            while (reader != null) {
                val line = reader?.readLine()
                if (line == null) {
                    break
                } else if (line.contains(endTag)) {
                    shellOutputCache.append(line.substring(0, line.indexOf(endTag)))
                    break
                } else if (line.contains(startTag)) {
                    shellOutputCache.clear()
                    shellOutputCache.append(line.substring(line.indexOf(startTag) + startTag.length))
                    upstart = false
                } else if (!upstart) {
                    shellOutputCache.append(line)
                    shellOutputCache.append("\n")
                }
            }
            return shellOutputCache.toString().trim()
        }
        catch (e: Exception) {
            tryExit()
            Log.e("KeepShellAsync", "" + e.message)
            return "error"
        } finally {
            enterLockTime = 0L
            mLock.unlock()

            currentIsIdle = true
        }
    }
}
