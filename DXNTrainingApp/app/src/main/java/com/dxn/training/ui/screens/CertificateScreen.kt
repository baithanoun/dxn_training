package com.dxn.training.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.certificate.CertificatePdfWriter
import com.dxn.training.ui.certificate.CertificateRenderer
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxOutlinedButton
import com.dxn.training.ui.components.ScreenHeader
import com.dxn.training.ui.share.ShareHelper
import com.dxn.training.ui.text.g
import com.dxn.training.ui.theme.BrandIvory
import com.dxn.training.ui.theme.BrandPlum
import com.dxn.training.ui.theme.LeafText
import com.dxn.training.ui.theme.TextMuted
import com.dxn.training.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CertificateScreen(
    sectionId: String,
    profileViewModel: ProfileViewModel,
    contentRepository: ContentRepository = ContentRepository.getInstance(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by profileViewModel.user.collectAsState()
    val certificates by profileViewModel.certificates.collectAsState()
    val certificate = certificates.find { it.sectionId == sectionId || it.id == sectionId }
    val section = contentRepository.getSections().find { it.id == sectionId }
    val progress = contentRepository.getSectionProgress(sectionId)

    val name = user.fullName.ifBlank { g("متدرب DXN", "متدربة DXN") }
    val title = certificate?.title?.takeIf { it.isNotBlank() }
        ?: section?.certificateTitle?.takeIf { it.isNotBlank() }
        ?: "شهادة إتمام"
    val code = certificate?.id?.takeIf { it.isNotBlank() }
        ?: progress.certificateId?.takeIf { it.isNotBlank() }
        ?: sectionId
    val issuedAt = certificate?.issuedAt?.takeIf { it.isNotBlank() }
        ?: progress.completedAt.orEmpty()

    val bitmap by produceState<Bitmap?>(initialValue = null, context, name, title, code, issuedAt) {
        value = withContext(Dispatchers.Default) {
            CertificateRenderer.render(context.applicationContext, name, title, code, issuedAt)
        }
    }
    val preview = remember(bitmap) { bitmap?.asImageBitmap() }
    val safeFileName = remember(code) {
        code.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
            .take(64)
            .ifBlank { "certificate" }
    }

    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    val savedToDownloads = g("تم حفظ شهادة PDF في التنزيلات", "تم حفظ شهادة PDF في التنزيلات")
    val savedToApp = g("حُفظت شهادة PDF في مجلد التطبيق", "حُفظت شهادة PDF في مجلد التطبيق")
    val saveFailed = g("تعذّر حفظ شهادتك", "تعذّر حفظ شهادتكِ")
    val shareFailed = g("تعذّر تجهيز شهادتك للمشاركة", "تعذّر تجهيز شهادتكِ للمشاركة")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(title = "الشهادة", onClose = onBack)

        Spacer(modifier = Modifier.height(8.dp))

        if (preview != null) {
            Image(
                bitmap = preview,
                contentDescription = g("معاينة شهادتك", "معاينة شهادتكِ"),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            )
        } else {
            CircularProgressIndicator(color = BrandPlum)
        }

        Spacer(modifier = Modifier.height(20.dp))

        DxButton(
            text = "حفظ PDF",
            enabled = bitmap != null,
            onClick = {
                val current = bitmap ?: return@DxButton
                scope.launch {
                    val saved = runCatching {
                        withContext(Dispatchers.IO) {
                            // shortcut: on API 24-28 there is no MediaStore RELATIVE_PATH and no
                            // storage permission request here; keep the PDF in app-owned files.
                            CertificatePdfWriter.write(context.applicationContext, current, safeFileName)
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                                savePdfToDownloads(context, current, safeFileName)
                        }
                    }.getOrDefault(false)
                    message = if (saved) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) savedToDownloads else savedToApp
                    } else {
                        saveFailed
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DxButton(
            text = "مشاركة PDF",
            enabled = bitmap != null,
            containerColor = BrandPlum,
            onClick = {
                val current = bitmap ?: return@DxButton
                scope.launch {
                    val shared = runCatching {
                        val file = withContext(Dispatchers.IO) {
                            CertificatePdfWriter.write(context.applicationContext, current, safeFileName)
                        }
                        ShareHelper.shareCertificatePdf(context, file)
                    }.isSuccess
                    if (!shared) message = shareFailed
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DxOutlinedButton(text = "العودة", onClick = onBack)

        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = LeafText
            )
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "على هذا الإصدار تُحفظ الشهادة داخل التطبيق ويمكن مشاركتها من هنا.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

/** API 29+ only: writes the PDF to Downloads/HikayaTamayoz without any storage permission. */
private fun savePdfToDownloads(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "$fileName.pdf")
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/HikayaTamayoz")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
    val saved = resolver.openOutputStream(uri)?.use { output ->
        CertificatePdfWriter.writeTo(bitmap, output)
        true
    } ?: false
    if (!saved) {
        resolver.delete(uri, null, null)
        return false
    }
    values.clear()
    values.put(MediaStore.Downloads.IS_PENDING, 0)
    resolver.update(uri, values, null, null)
    return true
}
