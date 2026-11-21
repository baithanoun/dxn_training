package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dxn.training.data.model.SectionStatus
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxOutlinedButton
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.theme.*

@Composable
fun CompletionScreen(
    levelId: String,
    score: Int,
    xp: Int,
    onNextLevel: () -> Unit,
    onReview: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = contentRepository.getLevel(levelId)
    val section = contentRepository.findSectionForLevel(levelId)
    val sectionProgress = section?.let { contentRepository.getSectionProgress(it.id) }
    val isSectionCompleted = sectionProgress?.status == SectionStatus.COMPLETED
    val certificateIssued = sectionProgress?.certificateIssued == true
    val passed = score >= (level?.passThreshold ?: 80)

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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (score >= 80) "ممتاز!" else "حاول مرة أخرى",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DXNBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+${xp} XP",
                    style = MaterialTheme.typography.displayMedium,
                    color = DXNGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (passed) "إتقان من أول محاولة ×2" else "حاول مرة أخرى لتحسين النتيجة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (passed) TextMuted else DXNRedDeep
                )

                if (isSectionCompleted && certificateIssued) {
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
                                text = "🎓 ${section?.certificateTitle ?: "شهادة إتمام"}",
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
                DxProgressBar(progress = score / 100f, color = DXNGold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DxButton(
            text = if (passed) "المستوى التالي" else "إعادة المحاولة",
            onClick = onNextLevel,
            enabled = passed
        )
        Spacer(modifier = Modifier.height(12.dp))
        DxOutlinedButton(text = "مراجعة البطاقات", onClick = onReview)
        Spacer(modifier = Modifier.height(12.dp))
        DxButton(
            text = "مشاركة الإنجاز",
            onClick = { },
            containerColor = Color(0xFFE3EDF3),
            contentColor = DXNBlue
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
