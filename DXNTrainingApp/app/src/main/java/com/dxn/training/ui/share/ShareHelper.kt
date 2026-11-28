package com.dxn.training.ui.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    fun buildAchievementText(
        prefix: String,
        levelName: String,
        scorePercent: Int?,
        appDownloadUrl: String?
    ): String = buildString {
        append(prefix)
        if (levelName.isNotBlank()) append(" ").append(levelName)
        if (scorePercent != null) append("\nالدرجة: $scorePercent%")
        append("\nمع فريق حكاية تميّز")
        val download = appDownloadUrl?.takeIf { it.isNotBlank() }
        if (download != null) append("\n").append(download)
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الإنجاز"))
    }

    fun shareCertificatePdf(
        context: Context,
        file: File,
        chooserTitle: String = "مشاركة الشهادة",
        text: String? = null
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (!text.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
