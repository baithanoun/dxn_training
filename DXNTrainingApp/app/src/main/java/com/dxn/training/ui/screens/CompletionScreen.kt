package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dxn.training.data.remote.EventSender
import com.dxn.training.data.repository.CompletionResult
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxOutlinedButton
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.viewmodel.CompletionViewModel

@Composable
fun CompletionScreen(
    levelId: String,
    completionViewModel: CompletionViewModel,
    onNextLevel: () -> Unit,
    onReview: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val state by completionViewModel.state.collectAsState()
    val result = state.result?.takeIf { state.levelId == levelId }
    val section = contentRepository.findSectionForLevel(levelId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("اكتمل المستوى!")

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    state.isSubmitting || result == null -> {
                        CircularProgressIndicator(color = DXNRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري حفظ النتيجة...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                    }

                    result is CompletionResult.Ok -> {
                        val response = result.response
                        ResultBadge()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (response.passed) "ممتاز!" else "حاول مرة أخرى",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DXNBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+${response.xpAwarded} XP",
                            style = MaterialTheme.typography.displayMedium,
                            color = DXNGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (response.passed) "تم حفظ النتيجة" else "حاول مرة أخرى لتحسين النتيجة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (response.passed) TextMuted else DXNRedDeep
                        )

                        if (response.certificateIssued) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎓 ${response.certificateTitle ?: section?.certificateTitle ?: "شهادة إتمام"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DXNGold,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = "تم منحك الشهادة بنجاح",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        DxProgressBar(progress = response.score / 100f, color = DXNGold)
                    }

                    result is CompletionResult.Locked -> {
                        ResultBadge()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.headlineMedium,
                            color = DXNRedDeep
                        )
                    }

                    else -> {
                        ResultBadge()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لم تُحفظ النتيجة — تحقق من الاتصال",
                            style = MaterialTheme.typography.headlineMedium,
                            color = DXNRedDeep
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
                    text = if (result.response.passed) "المستوى التالي" else "إعادة المحاولة",
                    onClick = onNextLevel,
                    enabled = result.response.passed
                )
            }
            is CompletionResult.Offline, is CompletionResult.Failed -> {
                DxButton(text = "إعادة المحاولة", onClick = { completionViewModel.retry() })
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
            onClick = { EventSender.send("share_achievement", levelId = levelId) },
            containerColor = Color(0xFFE3EDF3),
            contentColor = DXNBlue
        )
    }
}

@Composable
private fun ResultBadge() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFF8E1)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "★",
            style = MaterialTheme.typography.displayLarge,
            color = DXNGold
        )
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        color = DXNBlue,
        modifier = Modifier.fillMaxWidth()
    )
}
