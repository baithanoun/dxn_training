package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun QuizScreen(
    levelId: String,
    onComplete: (Int, Int) -> Unit,
    onBack: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = remember(levelId) { contentRepository.getLevel(levelId) }
    val round = level?.rounds?.firstOrNull()
    if (level == null || round == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("المستوى غير متاح", color = DXNRedDeep, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    var selected by remember { mutableStateOf<RoundOption?>(null) }
    var hearts by remember { mutableIntStateOf(3) }
    var answered by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(24.dp))

        round.options.forEach { option ->
            val isSelected = selected?.id == option.id
            val background = when {
                answered && option.isCorrect -> DXNGreen
                answered && isSelected && !option.isCorrect -> DXNRedDeep
                isSelected -> DXNRed
                else -> CardSurface
            }
            val textColor = if (answered && (option.isCorrect || isSelected)) CardSurface else TextPrimary

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(enabled = !answered) { selected = option },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = background)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CardSurface else Background)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(DXNRed)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (answered) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = selected?.feedback ?: "",
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
                    if (selected?.isCorrect == false) {
                        hearts--
                    }
                }
            },
            enabled = selected != null,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}
