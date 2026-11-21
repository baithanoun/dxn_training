package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun MatchPairsScreen(
    levelId: String,
    onComplete: (Int, Int) -> Unit,
    onBack: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = remember(levelId) { contentRepository.getLevel(levelId) }
    val round = level?.rounds?.firstOrNull()
    val options = round?.options ?: emptyList()
    var selected by remember { mutableStateOf<RoundOption?>(null) }
    var answered by remember { mutableStateOf(false) }
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
            Spacer(modifier = Modifier.width(12.dp))
            Text("00:45", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = round.prompt,
            style = MaterialTheme.typography.headlineMedium,
            color = DXNBlue,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { option ->
            val isSelected = selected?.id == option.id
            val isCorrect = answered && option.isCorrect
            val isWrong = answered && isSelected && !option.isCorrect
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(enabled = !answered) { selected = option },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCorrect -> DXNGreen
                        isWrong -> DXNRedDeep
                        isSelected -> DXNRed
                        else -> CardSurface
                    }
                )
            ) {
                Text(
                    text = option.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected || isCorrect || isWrong) CardSurface else TextPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (answered) {
            Text(
                text = if (selected?.isCorrect == true) "صحيح!" else "اختيار خاطئ",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected?.isCorrect == true) DXNGreen else DXNRedDeep,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DxButton(
            text = if (answered) "متابعة" else "تحقق",
            onClick = {
                if (answered) {
                    val score = if (selected?.isCorrect == true) 100 else 0
                    val xp = if (score >= level.passThreshold) level.xpReward else 0
                    val passed = score >= level.passThreshold
                    contentRepository.updateLevelProgress(levelId, score, passed)
                    onComplete(score, xp)
                } else {
                    answered = true
                    if (selected?.isCorrect == false) hearts--
                }
            },
            enabled = selected != null,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}
