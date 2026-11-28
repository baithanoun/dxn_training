package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dxn.training.data.remote.EventSender
import com.dxn.training.data.repository.CompletionResult
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.certificate.CertificatePdfWriter
import com.dxn.training.ui.certificate.CertificateRenderer
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxOutlinedButton
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.share.ShareHelper
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.text.g
import com.dxn.training.ui.viewmodel.CompletionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CompletionScreen(
    levelId: String,
    completionViewModel: CompletionViewModel,
    onNextLevel: () -> Unit,
    onReview: () -> Unit,
    onShowCertificate: (String) -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance(),
    appDownloadUrl: String? = null,
    userName: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by completionViewModel.state.collectAsState()
    val result = state.result?.takeIf { state.levelId == levelId }
    val section = contentRepository.findSectionForLevel(levelId)
    val level = contentRepository.getLevel(levelId)
    val levelName = level?.title.orEmpty()
    val passThreshold = level?.passThreshold ?: 80
    val sharePrefix = g("أكملتُ", "أكملتُ")
    val traineeNameFallback = g("متدرب DXN", "متدربة DXN")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("اكتمل المستوى!")

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    state.isSubmitting || result == null -> {
                        CircularProgressIndicator(color = BrandPlum)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري حفظ النتيجة...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }

                    result is CompletionResult.Ok -> {
                        val response = result.response
                        ResultBadge(response.passed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (response.passed) g("أحسنتَ!", "أحسنتِ!") else g("حاول مرة أخرى", "حاولي مرة أخرى"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = BrandPlum700
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+${response.xpAwarded} XP",
                            style = MaterialTheme.typography.displayMedium,
                            color = XpText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (response.passed) "تم حفظ النتيجة" else g("حاول مرة أخرى لتحسين النتيجة", "حاولي مرة أخرى لتحسين النتيجة"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (response.passed) TextMuted else Error
                        )

                        if (response.certificateIssued) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = LeafContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎓 ${response.certificateTitle ?: section?.certificateTitle ?: "شهادة إتمام"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = LeafText,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = g("تم منحكَ الشهادة بنجاح", "تم منحكِ الشهادة بنجاح"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                    section?.let {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        DxOutlinedButton(
                                            text = "عرض الشهادة",
                                            onClick = { onShowCertificate(it.id) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        val scoreColor = if (response.passed) LeafText else Error
                        Text(
                            text = "${response.score} / 100",
                            style = MaterialTheme.typography.displayMedium,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${response.score}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (response.passed) {
                                g("اجتزتَ المستوى", "اجتزتِ المستوى")
                            } else {
                                "${g("تحتاج", "تحتاجين")} $passThreshold% للنجاح"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DxProgressBar(
                            progress = response.score / 100f,
                            color = if (response.passed) Leaf else Error
                        )
                    }

                    result is CompletionResult.Locked -> {
                        ResultBadge(false)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Error
                        )
                    }

                    else -> {
                        ResultBadge(false)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = g("لم تُحفظ النتيجة — تحقق من الاتصال", "لم تُحفظ النتيجة — تحققي من الاتصال"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا يتم احتساب XP أو فتح مستويات قبل حفظ السيرفر للنتيجة.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        when (result) {
            is CompletionResult.Ok -> {
                DxButton(
                    text = if (result.response.passed) "المستوى التالي" else g("أعد المحاولة", "أعيدي المحاولة"),
                    onClick = if (result.response.passed) onNextLevel else onReview
                )
            }
            is CompletionResult.Offline, is CompletionResult.Failed -> {
                DxButton(text = g("أعد المحاولة", "أعيدي المحاولة"), onClick = { completionViewModel.retry() })
            }
            is CompletionResult.Locked -> {
                DxButton(text = "العودة", onClick = onReview)
            }
            else -> Unit
        }
        Spacer(modifier = Modifier.height(12.dp))
        DxOutlinedButton(text = "مراجعة البطاقات", onClick = onReview)
        Spacer(modifier = Modifier.height(12.dp))
        DxButton(
            text = "مشاركة الإنجاز",
            onClick = {
                val ok = result as? CompletionResult.Ok
                val shareText = ShareHelper.buildAchievementText(
                    prefix = sharePrefix,
                    levelName = levelName,
                    scorePercent = ok?.response?.score,
                    appDownloadUrl = appDownloadUrl
                )
                if (ok != null && ok.response.certificateIssued) {
                    scope.launch {
                        val shared = runCatching {
                            val file = withContext(Dispatchers.Default) {
                                val certTitle = ok.response.certificateTitle
                                    ?: section?.certificateTitle?.takeIf { it.isNotBlank() }
                                    ?: "شهادة إتمام"
                                val code = ok.response.certificateId?.takeIf { it.isNotBlank() }
                                    ?: section?.id
                                    ?: levelId
                                val issuedAt = section?.let {
                                    contentRepository.getSectionProgress(it.id).completedAt
                                }.orEmpty()
                                val safeName = code.filter { c -> c.isLetterOrDigit() || c == '-' || c == '_' }
                                    .take(64)
                                    .ifBlank { "certificate" }
                                val bitmap = CertificateRenderer.render(
                                    context.applicationContext,
                                    userName.ifBlank { traineeNameFallback },
                                    certTitle,
                                    code,
                                    issuedAt
                                )
                                try {
                                    CertificatePdfWriter.write(context.applicationContext, bitmap, safeName)
                                } finally {
                                    bitmap.recycle()
                                }
                            }
                            ShareHelper.shareCertificatePdf(context, file, text = shareText)
                        }.isSuccess
                        if (shared) {
                            EventSender.send("share_achievement", levelId = levelId)
                        }
                    }
                } else {
                    ShareHelper.shareText(context, shareText)
                    EventSender.send("share_achievement", levelId = levelId)
                }
            },
            containerColor = BrandPlum100,
            contentColor = BrandPlum700
        )
    }
}

@Composable
private fun ResultBadge(passed: Boolean) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(if (passed) LeafContainer else ErrorContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "★",
            style = MaterialTheme.typography.displayLarge,
            color = if (passed) LeafText else Error
        )
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = BrandPlum700,
        modifier = Modifier.fillMaxWidth()
    )
}
