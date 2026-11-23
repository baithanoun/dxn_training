package com.dxn.training.ui.screens

import android.os.SystemClock
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
import com.dxn.training.data.remote.AnswerDto
import com.dxn.training.data.remote.EventSender
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.*
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.text.g

@Composable
fun QuizScreen(
    levelId: String,
    onComplete: (List<AnswerDto>) -> Unit,
    onBack: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = remember(levelId) { contentRepository.getLevel(levelId) }
    val rounds = level?.rounds ?: emptyList()
    var roundIndex by remember(levelId) { mutableIntStateOf(0) }
    val round = rounds.getOrNull(roundIndex)
    if (level == null || round == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("المستوى غير متاح", color = Error, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    var selected by remember(round.id) { mutableStateOf<RoundOption?>(null) }
    var hearts by remember(levelId) { mutableIntStateOf(3) }
    var answered by remember(round.id) { mutableStateOf(false) }
    val answers = remember(levelId) { mutableListOf<AnswerDto>() }
    val roundStartedAt = remember(round.id) { SystemClock.elapsedRealtime() }

    LaunchedEffect(levelId) { EventSender.send("level_started", levelId = levelId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
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
            color = BrandPlum700,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        round.options.forEach { option ->
            val isSelected = selected?.id == option.id
            val background = when {
                answered && option.isCorrect -> Leaf
                answered && isSelected && !option.isCorrect -> Error
                isSelected -> BrandPlum
                else -> Surface
            }
            val textColor = if (answered && (option.isCorrect || isSelected)) Surface else TextPrimary

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
                            .background(if (isSelected) Surface else BrandIvory)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(BrandPlum)
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
                color = if (selected?.isCorrect == true) LeafText else Error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DxButton(
            text = if (answered && roundIndex < rounds.lastIndex) "التالي" else if (answered) "متابعة" else g("تحقق", "تحققي"),
            onClick = {
                if (answered) {
                    val chosen = selected ?: return@DxButton
                    answers.add(AnswerDto(round.id, chosen.id))
                    EventSender.send(
                        name = "question_answered",
                        levelId = levelId,
                        payload = mapOf(
                            "roundId" to round.id,
                            "optionId" to chosen.id,
                            "correct" to chosen.isCorrect,
                            "timeMs" to (SystemClock.elapsedRealtime() - roundStartedAt)
                        )
                    )
                    if (roundIndex < rounds.lastIndex) {
                        roundIndex++
                        selected = null
                        answered = false
                    } else {
                        onComplete(answers.toList())
                    }
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
