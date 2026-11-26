package com.dxn.training.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.dxn.training.BuildConfig
import com.dxn.training.data.remote.AppConfigResponse

/** True when the server publishes a newer build than the one installed and says where to get it. */
fun isUpdateAvailable(installedVersionCode: Int, latestVersionCode: Int?, downloadUrl: String?): Boolean =
    latestVersionCode != null && latestVersionCode > installedVersionCode && !downloadUrl.isNullOrBlank()

/**
 * Asks the user to install a newer APK when one is published. Android does not allow a sideloaded
 * app to replace itself silently, so the download opens in the browser and the system installer
 * replaces the current version (same signing key required).
 */
@Composable
fun UpdatePrompt(config: AppConfigResponse?) {
    val url = config?.appDownloadUrl
    var dismissed by rememberSaveable { mutableStateOf(false) }
    if (dismissed || !isUpdateAvailable(BuildConfig.VERSION_CODE, config?.appVersionCode, url)) return

    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = { dismissed = true },
        title = { Text("يتوفر تحديث جديد") },
        text = {
            Text(
                "الإصدار ${config?.appVersion ?: ""} متاح الآن. حمّل التحديث ثم ثبّته، " +
                    "وسيحلّ محل النسخة الحالية مع الاحتفاظ بحسابك وتقدّمك."
            )
        },
        confirmButton = {
            TextButton(onClick = {
                dismissed = true
                uriHandler.openUri(url!!)
            }) { Text("تحديث الآن") }
        },
        dismissButton = {
            TextButton(onClick = { dismissed = true }) { Text("لاحقاً") }
        }
    )
}
