package com.dxn.training.ui.screens

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
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.*
import com.dxn.training.ui.theme.*

@Composable
fun ChatBattleScreen(
    levelId: String,
    onComplete: (Int, Int) -> Unit,
    onBack: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = remember(levelId) { contentRepository.getLevel(levelId) }
    val round = level?.rounds?.firstOrNull()
    var trust by remember { mutableIntStateOf(round?.initialTrust ?: 50) }
    var hearts by remember { mutableIntStateOf(3) }
    var answered by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<RoundOption?>(null) }
    val messages = remember { mutableStateListOf<Pair<String, Boolean>>() }
    LaunchedEffect(round) {
        if (round != null && messages.isEmpty()) {
            messages.add(round.prompt to false)
        }
    }

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
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AvatarPlaceholder(size = 40)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("أحمد — صديق مهتم", style = MaterialTheme.typography.bodyLarge, color = DXNBlue)
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
            // Ensure prompt is shown when level changes
            LaunchedEffect(levelId) {
                if (messages.isEmpty() || messages.firstOrNull()?.first != round.prompt) {
                    messages.clear()
                    messages.add(round.prompt to false)
                    trust = round.initialTrust
                    hearts = 3
                    answered = false
                    selectedOption = null
                }
            }
        }

        // Options
        if (!answered) {
            Text(
                text = "اختر ردك التالي:",
                style = MaterialTheme.typography.bodyLarge,
                color = DXNBlue,
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
                        containerColor = if (selectedOption?.id == option.id) DXNRed else CardSurface
                    )
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedOption?.id == option.id) CardSurface else TextPrimary,
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
                color = if (selectedOption?.isCorrect == true) DXNGreen else DXNRedDeep,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DxButton(
            text = if (answered) "متابعة" else "إرسال",
            onClick = {
                if (answered) {
                    val passed = trust >= level.passThreshold && hearts > 0
                    val score = if (passed) 100 else 0
                    val xp = if (passed) level.xpReward else 0
                    contentRepository.updateLevelProgress(levelId, score, passed)
                    onComplete(score, xp)
                } else {
                    selectedOption?.let { option ->
                        trust = (trust + option.trustImpact).coerceIn(0, 100)
                        if (!option.isCorrect && !option.isNeutral) hearts--
                        messages.add(option.text to true)
                        answered = true
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
                .background(if (isUser) DXNRed else CardSurface)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) CardSurface else TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
