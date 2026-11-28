package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dxn.training.data.remote.AnswerDto
import com.dxn.training.data.remote.EventSender
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.*
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.text.g

@Composable
fun OrderSequenceScreen(
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

    // ترتيب الخيارات القادم من السيرفر هو الترتيب الصحيح؛ نعرض كل الخيارات بترتيب عشوائي.
    val options = round.options
    var ordered by remember(round.id) { mutableStateOf(options.shuffled()) }
    var checked by remember(round.id) { mutableStateOf(false) }
    var hearts by remember(levelId) { mutableIntStateOf(3) }
    val isCorrectOrder = ordered.map { it.id } == options.map { it.id }
    val answers = remember(levelId) { mutableListOf<AnswerDto>() }

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

        Spacer(modifier = Modifier.height(16.dp))

        ordered.forEachIndexed { index, step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
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
                        text = step.text,
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
                                Text("↑", style = MaterialTheme.typography.titleLarge, color = BrandPlum700)
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
                                Text("↓", style = MaterialTheme.typography.titleLarge, color = BrandPlum700)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        CorrectAnswerFeedback(visible = checked && isCorrectOrder)

        Spacer(modifier = Modifier.weight(1f))

        DxButton(
            text = if (checked && roundIndex < rounds.lastIndex) "التالي"
                   else if (checked) "متابعة" else g("تحقق", "تحققي"),
            onClick = {
                if (checked) {
                    answers.add(AnswerDto(round.id, orderedOptionIds = ordered.map { it.id }))
                    if (roundIndex < rounds.lastIndex) {
                        roundIndex++
                    } else {
                        onComplete(answers.toList())
                    }
                } else {
                    checked = true
                    if (!isCorrectOrder) hearts--
                }
            },
            enabled = ordered.isNotEmpty()
        )
    }
}
