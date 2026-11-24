package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dxn.training.data.model.Level
import com.dxn.training.data.model.LevelStatus
import com.dxn.training.data.model.SectionStatus
import com.dxn.training.data.model.UserProgress
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.text.g
import com.dxn.training.ui.viewmodel.HomeViewModel

@Composable
fun LearnScreen(viewModel: HomeViewModel, onLevelClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLevel = findCurrentOrNextLevel(uiState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "متابعة التعلم",
            style = MaterialTheme.typography.headlineMedium,
            color = BrandPlum700
        )
        Spacer(modifier = Modifier.height(16.dp))

        currentLevel?.let { level ->
            CurrentLevelCard(
                level = level,
                progress = uiState.progress[level.id],
                onContinue = { onLevelClick(level.id) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "كل المستويات",
            style = MaterialTheme.typography.titleLarge,
            color = BrandPlum700
        )
        Spacer(modifier = Modifier.height(8.dp))

        uiState.sections.forEach { section ->
            val sectionStatus = uiState.sectionProgress[section.id]?.status ?: SectionStatus.LOCKED
            if (sectionStatus != SectionStatus.LOCKED) {
                section.skills.forEach { skill ->
                    skill.levels.forEach { level ->
                        val p = uiState.progress[level.id]
                        LevelRow(
                            level = level,
                            skillTitle = skill.title,
                            progress = p,
                            onClick = {
                                if (p?.status != LevelStatus.LOCKED) onLevelClick(level.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLevelCard(
    level: Level,
    progress: UserProgress?,
    onContinue: () -> Unit
) {
    DxCard {
        Text(
            text = g("استمر من حيث توقفتَ", "استمري من حيث توقفتِ"),
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = level.title,
            style = MaterialTheme.typography.titleLarge,
            color = BrandPlum700
        )
        Spacer(modifier = Modifier.height(12.dp))
        DxProgressBar(progress = (progress?.score ?: 0) / 100f, color = Leaf)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPlum)
        ) {
            Text(g("تابع", "تابعي"), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LevelRow(
    level: Level,
    skillTitle: String,
    progress: UserProgress?,
    onClick: () -> Unit
) {
    val status = progress?.status ?: LevelStatus.LOCKED
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (status == LevelStatus.LOCKED) Locked else TextPrimary
                )
                Text(
                    text = skillTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            when (status) {
                LevelStatus.COMPLETED -> Text("✓", color = LeafText)
                LevelStatus.UNLOCKED -> Button(
                    onClick = onClick,
                    modifier = Modifier.defaultMinSize(minWidth = 88.dp, minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPlum)
                ) {
                    Text(g("ابدأ", "ابدئي"), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                }
                LevelStatus.LOCKED -> Text("L", color = Locked)
            }
        }
    }
}

private fun findCurrentOrNextLevel(uiState: HomeViewModel.HomeUiState): Level? {
    return uiState.sections
        .filter { uiState.sectionProgress[it.id]?.status != SectionStatus.LOCKED }
        .flatMap { it.skills }
        .flatMap { it.levels }
        .firstOrNull { level ->
            val p = uiState.progress[level.id]
            p?.status == LevelStatus.UNLOCKED || p?.status == LevelStatus.COMPLETED
        }
}
