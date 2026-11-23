package com.dxn.training.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun ChatBattleScreen(
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

    var trust by remember(round.id) { mutableIntStateOf(round.initialTrust) }
    var hearts by remember(levelId) { mutableIntStateOf(3) }
    var answered by remember(round.id) { mutableStateOf(false) }
    var selectedOption by remember(round.id) { mutableStateOf<RoundOption?>(null) }
    val messages = remember(round.id) { mutableStateListOf(round.prompt to false) }
    val answers = remember(levelId) { mutableListOf<AnswerDto>() }
    val roundStartedAt = remember(round.id) { SystemClock.elapsedRealtime() }

    LaunchedEffect(levelId) { EventSender.send("level_started", levelId = levelId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AvatarPlaceholder(size = 40)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("أحمد — صديق مهتم", style = MaterialTheme.typography.bodyLarge, color = BrandPlum700)
                        Text("متردد", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                    CloseButton(onClick = onBack)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DxTrustBar(trust = trust)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    HeartsIndicator(hearts = hearts)
                }
            }
        }

        // Chat
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            messages.forEach { (text, isUser) ->
                ChatBubble(text = text, isUser = isUser)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Options
        if (!answered) {
            Text(
                text = g("اختر ردك التالي:", "اختاري ردكِ التالي:"),
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            round.options.forEach { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { selectedOption = option },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption?.id == option.id) BrandPlum else Surface
                    )
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedOption?.id == option.id) Surface else TextPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (answered) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = selectedOption?.feedback ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedOption?.isCorrect == true) LeafText else Error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DxButton(
            text = if (answered && roundIndex < rounds.lastIndex) "التالي"
                   else if (answered) "متابعة" else "إرسال",
            onClick = {
                if (answered) {
                    val chosen = selectedOption ?: return@DxButton
                    answers.add(AnswerDto(round.id, chosen.id))
                    if (roundIndex < rounds.lastIndex) {
                        roundIndex++
                    } else {
                        onComplete(answers.toList())
                    }
                } else {
                    selectedOption?.let { option ->
                        trust = (trust + option.trustImpact).coerceIn(0, 100)
                        if (!option.isCorrect && !option.isNeutral) hearts--
                        messages.add(option.text to true)
                        answered = true
                        EventSender.send(
                            name = "question_answered",
                            levelId = levelId,
                            payload = mapOf(
                                "roundId" to round.id,
                                "optionId" to option.id,
                                "correct" to option.isCorrect,
                                "timeMs" to (SystemClock.elapsedRealtime() - roundStartedAt)
                            )
                        )
                    }
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun ChatBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 4.dp else 16.dp,
                        bottomEnd = if (isUser) 16.dp else 4.dp
                    )
                )
                .background(if (isUser) BrandPlum else Surface)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) Surface else TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
