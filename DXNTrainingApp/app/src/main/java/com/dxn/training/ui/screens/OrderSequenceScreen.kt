package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dxn.training.data.model.RoundOption
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.*
import com.dxn.training.ui.theme.*

@Composable
fun OrderSequenceScreen(
    levelId: String,
    onComplete: (Int, Int) -> Unit,
    onBack: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = remember(levelId) { contentRepository.getLevel(levelId) }
    val round = level?.rounds?.firstOrNull()

    // استخراج الخطوات من خيارات الجولة (الخيارات "الصحيحة" بالترتيب)
    val steps = remember(levelId) {
        round?.options
            ?.filter { it.isCorrect }
            ?.sortedBy { it.id }
            ?.map { it.text }
            ?: emptyList()
    }

    // إذا لم تتوفر خطوات من البيانات، استخدم قائمة افتراضية
    val defaultSteps = remember { listOf("الدعوة والتشويق", "عرض المنتج", "شرح الخطة", "طلب القرار") }
    val actualSteps = if (steps.isNotEmpty()) steps else defaultSteps

    var ordered by remember { mutableStateOf(actualSteps.shuffled()) }
    var checked by remember { mutableStateOf(false) }
    var hearts by remember { mutableIntStateOf(3) }

    if (level == null || round == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("المستوى غير متاح", color = DXNRedDeep, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ScreenHeader(title = level.title, onClose = onBack, streak = 5)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            HeartsIndicator(hearts = hearts)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = round.prompt,
            style = MaterialTheme.typography.headlineMedium,
            color = DXNBlue,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ordered.forEachIndexed { index, step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    // أزرار إعادة الترتيب
                    if (!checked) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        ordered = ordered.toMutableList().apply {
                                            val item = removeAt(index)
                                            add(index - 1, item)
                                        }
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Text("↑", style = MaterialTheme.typography.titleLarge, color = DXNBlue)
                            }
                            IconButton(
                                onClick = {
                                    if (index < ordered.size - 1) {
                                        ordered = ordered.toMutableList().apply {
                                            val item = removeAt(index)
                                            add(index + 1, item)
                                        }
                                    }
                                },
                                enabled = index < ordered.size - 1
                            ) {
                                Text("↓", style = MaterialTheme.typography.titleLarge, color = DXNBlue)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        DxButton(
            text = if (checked) "متابعة" else "تحقق",
            onClick = {
                if (checked) {
                    val correct = ordered == actualSteps
                    val score = if (correct) 100 else 0
                    val xp = if (correct) level.xpReward else 0
                    val passed = score >= level.passThreshold
                    contentRepository.updateLevelProgress(levelId, score, passed)
                    onComplete(score, xp)
                } else {
                    checked = true
                    if (ordered != actualSteps) hearts--
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}
