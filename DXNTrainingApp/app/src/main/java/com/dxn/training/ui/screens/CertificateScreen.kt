package com.dxn.training.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
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
    val savedToGallery = g("تم حفظ شهادتك في الصور", "تم حفظ شهادتكِ في الصور")
    val savedToApp = g("حُفظت شهادتك في مجلد التطبيق", "حُفظت شهادتكِ في مجلد التطبيق")
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
            text = "حفظ في الصور",
            enabled = bitmap != null,
            onClick = {
                val current = bitmap ?: return@DxButton
                scope.launch {
                    val saved = runCatching {
                        withContext(Dispatchers.IO) {
                            // minSdk 24: on API 24-28 there is no MediaStore RELATIVE_PATH
                            // and no storage permission request here. shortcut: keep the PNG
                            // in app-owned files and share it through FileProvider instead.
                            ShareHelper.saveCertificatePng(context, current, safeFileName)
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                                saveToGallery(context, current, safeFileName)
                        }
                    }.getOrDefault(false)
                    message = if (saved) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) savedToGallery else savedToApp
                    } else {
                        saveFailed
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DxButton(
            text = "مشاركة",
            enabled = bitmap != null,
            containerColor = BrandPlum,
            onClick = {
                val current = bitmap ?: return@DxButton
                scope.launch {
                    val shared = runCatching {
                        val file = withContext(Dispatchers.IO) {
                            ShareHelper.saveCertificatePng(context, current, safeFileName)
                        }
                        ShareHelper.shareCertificateFile(context, file)
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

/** Writes the shareable PNG into app-owned storage; FileProvider exposes it to other apps. */
/** API 29+ only: writes to Pictures/HikayaTamayoz without any storage permission. */
private fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HikayaTamayoz")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: return false
    val saved = context.contentResolver.openOutputStream(uri)?.use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    } ?: false
    if (!saved) context.contentResolver.delete(uri, null, null)
    return saved
}
