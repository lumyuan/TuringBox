package io.github.lumyuan.turingbox.common.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.PermissionChecker
import io.github.lumyuan.turingbox.R
import io.github.lumyuan.turingbox.common.shell.KeepShellPublic


/**
 * Created by SYSTEM on 2018/07/21.
 */
class WriteSettings {
    private fun checkPermission(context: Context, permission: String): Boolean = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
    fun getPermission(context: Context): Boolean =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.System.canWrite(context)
            } else {
                // TODO("VERSION.SDK_INT < M")
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    fun setPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // val selfPackageUri = Uri.parse("package:" + context.packageName)
            // val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, selfPackageUri)
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // context.startActivity(intent)
            try {
                Toast.makeText(context, R.string.please_arrow_update_system_permission, Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.getPackageName(), null)
                intent.setData(uri)
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            KeepShellPublic.doCmdSync("pm grant ${context.packageName} android.permission.WRITE_SETTINGS")
        }
    }
}
