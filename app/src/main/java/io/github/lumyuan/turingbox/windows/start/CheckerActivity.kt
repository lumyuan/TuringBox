package io.github.lumyuan.turingbox.windows.start

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.TuringBox
import io.github.lumyuan.turingbox.common.data.publisher.BatteryState
import io.github.lumyuan.turingbox.common.permissions.Busybox
import io.github.lumyuan.turingbox.common.permissions.CheckRootStatus
import io.github.lumyuan.turingbox.common.permissions.WriteSettings
import io.github.lumyuan.turingbox.common.shell.KeepShellPublic
import io.github.lumyuan.turingbox.common.store.SpfConfig
import io.github.lumyuan.turingbox.ui.compose.setContentUI
import io.github.lumyuan.turingbox.windows.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class CheckerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentUI {
            RootUI()
        }
    }
}

private var hasRoot = false

@Composable
fun RootUI() {

    val rootRejectedState = remember {
        mutableStateOf(false)
    }

    val rootTimeoutState = remember {
        mutableStateOf(false)
    }

    val installBusyboxState = remember {
        mutableStateOf(false)
    }

    val privacyState = remember {
        mutableStateOf(false)
    }

    val stringResource = stringResource(id = R.string.text_permission_checking)

    val textState = remember {
        mutableStateOf(stringResource)
    }

    val timerLongState = remember {
        mutableStateOf(11)
    }

    val timer = Timer()
    val timerTask = object : TimerTask() {
        override fun run() {
            if (timerLongState.value > 0) {
                timerLongState.value = timerLongState.value - 1
            } else {
                timer.cancel()
            }
        }
    }

    val context = LocalContext.current as CheckerActivity

    val disableSeLinux = TuringBox.getBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, false)
    val checkRootStatus = CheckRootStatus(
        context = context,
        next = {
            if (TuringBox.getBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, false)) {
                textState.value = "检查并获取必需权限……"
                hasRoot = true
                checkFileWrite(context) {
                    textState.value = "检查Busybox是否安装..."
                    Busybox(context) {
                        installBusyboxState.value = true
                    }.forceInstall {
                        startToFinish(context, textState)
                    }
                }
            } else {
                timer.schedule(timerTask, 0, 1000)
                privacyState.value = true
            }
        },
        disableSeLinux = disableSeLinux,
        onRootRejected = {
            rootRejectedState.value = true
        },
        onRootTimeout = {
            rootTimeoutState.value = true
        }
    )

    LaunchedEffect(installBusyboxState) {
        withContext(Dispatchers.IO) {
            //检测root权限
            delay(1500)
            checkRootStatus.forceGetRoot()
        }
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (progress) = createRefs()
        val (logo) = createRefs()
        val (text) = createRefs()
        Column(
            modifier = Modifier.constrainAs(progress) {
                this.linkTo(
                    start = parent.start,
                    end = parent.end,
                    top = parent.top,
                    bottom = parent.bottom
                )
            },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(75.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_check_permission),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .size(76.dp)
                .constrainAs(logo) {
                    this.linkTo(
                        start = parent.start,
                        end = parent.end,
                        top = parent.top,
                        bottom = parent.bottom
                    )
                }
        )
        Text(
            text = textState.value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(top = 8.dp)
                .constrainAs(text) {
                    this.linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    top.linkTo(logo.bottom)
                }
                .alpha(.5f)
        )
    }

    RootRejectedDialog(textState, rootRejectedState, installBusyboxState, checkRootStatus)
    RootTimeoutDialog(rootTimeoutState, checkRootStatus)
    InstallBusyBoxErrorDialog(installBusyboxState)
    PrivacyDocDialog(timer, timerLongState, privacyState, checkRootStatus)
}

private fun checkPermission(activity: ComponentActivity, permission: String): Boolean =
    PermissionChecker.checkSelfPermission(
        activity.applicationContext,
        permission
    ) == PermissionChecker.PERMISSION_GRANTED

private fun checkFileWrite(activity: ComponentActivity, next: Runnable) {
    CoroutineScope(Dispatchers.IO).launch {
        CheckRootStatus.grantPermission(activity)
        if (!(checkPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) && checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Manifest.permission.WAKE_LOCK
                    ),
                    0x11
                )
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.WAKE_LOCK
                    ),
                    0x11
                )
            }
        }
        val writeSettings = WriteSettings()
        if (!writeSettings.getPermission(activity.applicationContext)) {
            writeSettings.setPermission(activity.applicationContext)
        }
        next.run()
    }
}

/**
 * 启动完成
 */
private fun startToFinish(componentActivity: ComponentActivity, textState: MutableState<String>) {
    textState.value = "启动完成！"
    CoroutineScope(Dispatchers.IO).launch {
        //BatteryState(TuringBox.application).registerReceiver()
    }
    textState.value = componentActivity.getString(R.string.text_permission_pass)
    val intent = Intent(componentActivity, MainActivity::class.java)
    componentActivity.startActivity(intent)
    componentActivity.finish()
}

@Composable
fun PrivacyDocDialog(
    timer: Timer,
    timerLongState: MutableState<Int>,
    privacyState: MutableState<Boolean>,
    checkRootStatus: CheckRootStatus
) {

    var agreement by remember {
        mutableStateOf(false)
    }

    val stringResource = stringResource(id = R.string.continue_to_use)

    var agreementTextState by remember {
        mutableStateOf(stringResource)
    }

    agreementTextState = "$stringResource${ if (timerLongState.value <= 0) "" else "(${timerLongState.value}S)" }"

    val context = LocalContext.current
    if (privacyState.value) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                privacyState.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.turing_box_contract_title))
            },
            text = {
                Column {
                    Text(text = stringResource(id = R.string.turing_box_contract))
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                agreement = !agreement
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    color = Color(0xFFFF5252),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                painter = painterResource(id = R.drawable.ic_danger),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.background)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.agreen_danger),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = stringResource(id = R.string.agreen_danger_tips),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.alpha(.5f)
                            )
                        }
                        Switch(checked = agreement, onCheckedChange = null)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (timerLongState.value > 0) {
                        timerLongState.value = timerLongState.value - 1
                        return@Button
                    }
                    if (!agreement) {
                        Toast.makeText(context, context.getString(R.string.please_arrow_danger), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        //检测root权限
                        timer.cancel()
                        TuringBox.setBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, true)
                        checkRootStatus.forceGetRoot()
                        privacyState.value = false
                    }
                },
                    modifier = Modifier.alpha(if (timerLongState.value <= 0) 1f else .5f)) {
                    Text(text = agreementTextState)
                }
            },
            dismissButton = {
                FilledTonalButton(onClick = {
                    privacyState.value = false
                    (context as ComponentActivity).finish()
                }) {
                    Text(text = stringResource(id = R.string.cancel_use))
                }
            }
        )
    }
}


@Composable
fun InstallBusyBoxErrorDialog(installBusyboxState: MutableState<Boolean>) {
    if (installBusyboxState.value) {
        AlertDialog(
            onDismissRequest = { installBusyboxState.value = false },
            text = {
                Text(text = stringResource(id = R.string.busybox_nonsupport))
            },
            confirmButton = {
                Button(onClick = { Process.killProcess(Process.myPid()) }) {
                    Text(text = stringResource(id = R.string.kill_self))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}

@Composable
fun RootRejectedDialog(
    textState: MutableState<String>,
    rootRejectedState: MutableState<Boolean>,
    installBusyboxState: MutableState<Boolean>,
    checkRootStatus: CheckRootStatus
) {
    val context = LocalContext.current as ComponentActivity
    if (rootRejectedState.value) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                rootRejectedState.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.text_root_fail))
            },
            text = {
                Text(text = stringResource(id = R.string.text_root_fail_tips))
            },
            confirmButton = {
                Button(onClick = {
                    rootRejectedState.value = false
                    CoroutineScope(Dispatchers.IO).launch {
                        KeepShellPublic.tryExit()
                        checkRootStatus.forceGetRoot()
                    }
                }) {
                    Text(text = stringResource(id = R.string.btn_retry))
                }
            },
            dismissButton = {
                FilledTonalButton(onClick = {
                    rootRejectedState.value = false
                    textState.value = "检查Busybox是否安装..."
                    Busybox(context){
                        installBusyboxState.value = true
                    }.forceInstall {
                        startToFinish(context, textState)
                    }
                }) {
                    Text(text = stringResource(id = R.string.btn_skip))
                }
            }
        )
    }
}

@Composable
fun RootTimeoutDialog(rootTimeoutState: MutableState<Boolean>, checkRootStatus: CheckRootStatus) {
    val context = LocalContext.current
    if (rootTimeoutState.value) {
        AlertDialog(
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = {
                rootTimeoutState.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.error_root))
            },
            text = {
                Text(text = stringResource(id = R.string.error_su_timeout))
            },
            confirmButton = {
                Button(onClick = {
                    rootTimeoutState.value = false
                    CoroutineScope(Dispatchers.IO).launch {
                        checkRootStatus.forceGetRoot()
                    }
                }) {
                    Text(text = stringResource(id = R.string.btn_retry))
                }
            },
            dismissButton = {
                FilledTonalButton(onClick = {
                    rootTimeoutState.value = false
                    (context as ComponentActivity).finish()
                }) {
                    Text(text = stringResource(id = R.string.btn_exit))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRootUi() {
    RootUI()
}

@Preview
@Composable
fun PreviewRootRejectDialog() {
    val state = remember {
        mutableStateOf(true)
    }
    val textState = remember {
        mutableStateOf("")
    }
    RootRejectedDialog(
        textState = textState,
        rootRejectedState = state,
        installBusyboxState = state,
        checkRootStatus = CheckRootStatus(LocalContext.current)
    )
}

@Preview
@Composable
fun PreviewRootTimeoutDialog() {
    val state = remember {
        mutableStateOf(true)
    }
    RootTimeoutDialog(
        rootTimeoutState = state,
        checkRootStatus = CheckRootStatus(LocalContext.current)
    )
}

@Preview
@Composable
fun PreviewInstallBusyboxErrorDialog() {
    val state = remember {
        mutableStateOf(true)
    }
    InstallBusyBoxErrorDialog(installBusyboxState = state)
}

@Preview
@Composable
fun PreviewPrivacyDialog() {
    val state = remember {
        mutableStateOf(true)
    }
    val timerState = remember {
        mutableStateOf(10)
    }
    PrivacyDocDialog(
        timer = Timer(),
        timerLongState = timerState,
        privacyState = state,
        checkRootStatus = CheckRootStatus(LocalContext.current)
    )
}
