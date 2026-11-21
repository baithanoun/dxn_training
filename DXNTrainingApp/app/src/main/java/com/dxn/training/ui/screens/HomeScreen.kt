package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dxn.training.data.model.*
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.components.ScreenHeader
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLevelClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
        ScreenHeader(title = "مرحباً ${uiState.userName}", streak = uiState.streak)

        // عرض رسالة الخطأ عند فشل الاتصال بالخادم
        uiState.error?.let { message ->
            LaunchedEffect(message) {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }

        DailyProgressCard(uiState)

        Text(
            text = "مسار التدريب",
            style = MaterialTheme.typography.headlineMedium,
            color = DXNBlue,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (uiState.isLoading && uiState.sections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DXNRed)
            }
        }

        var lockedSectionMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(lockedSectionMessage) {
            lockedSectionMessage?.let {
                snackbarHostState.showSnackbar(it)
                lockedSectionMessage = null
            }
        }

        uiState.sections.forEachIndexed { index, section ->
            SectionCard(
                section = section,
                index = index,
                sectionProgress = uiState.sectionProgress[section.id],
                onContinue = { shouldOpen ->
                    if (!shouldOpen) {
                        val required = section.requiredSectionId
                        val requiredTitle = uiState.sections.find { it.id == required }?.title ?: "القسم السابق"
                        lockedSectionMessage = "يجب إكمال $requiredTitle والحصول على شهادته لفتح هذا القسم"
                        return@SectionCard
                    }

                    // فتح أول مستوى في القسم إذا لم يُفتح بعد
                    viewModel.unlockFirstLevelOfSection(section.id)

                    val firstLevel = section.skills
                        .flatMap { it.levels }
                        .firstOrNull { l ->
                            uiState.progress[l.id]?.status != LevelStatus.LOCKED
                        }
                        ?: section.skills.firstOrNull()?.levels?.firstOrNull()
                    firstLevel?.let { onLevelClick(it.id) }
                }
            )

            val sectionStatus = uiState.sectionProgress[section.id]?.status ?: SectionStatus.LOCKED
            if (sectionStatus != SectionStatus.LOCKED) {
                section.skills.forEach { skill ->
                    skill.levels.forEach { level ->
                        SkillNode(
                            skill = skill,
                            level = level,
                            progress = uiState.progress,
                            onLevelClick = onLevelClick
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DailyProgressCard(uiState: HomeViewModel.HomeUiState) {
    DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "تقدمك اليومي",
            style = MaterialTheme.typography.bodyLarge,
            color = DXNBlue
        )
        Spacer(modifier = Modifier.height(12.dp))
        DxProgressBar(progress = uiState.dailyProgress, color = DXNGreen)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${uiState.streak} أيام متتالية  |  XP ${uiState.totalXp}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SectionCard(
    section: Section,
    index: Int,
    sectionProgress: SectionProgress?,
    onContinue: (Boolean) -> Unit
) {
    val status = sectionProgress?.status ?: SectionStatus.LOCKED
    val isLocked = status == SectionStatus.LOCKED
    val isCompleted = status == SectionStatus.COMPLETED
    val isCertificateIssued = sectionProgress?.certificateIssued == true

    val (statusColor, statusText) = when (status) {
        SectionStatus.COMPLETED -> DXNGreen to "مكتمل"
        SectionStatus.UNLOCKED -> DXNGold to "متاح"
        SectionStatus.LOCKED -> Locked to "مقفل"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = true) { onContinue(!isLocked) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) CardSurface else statusColor.copy(alpha = 0.08f)
        ),
        border = if (isCompleted) null else androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(statusColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "مقفل",
                                tint = CardSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (isCompleted) {
                            Text("✓", color = CardSurface, style = MaterialTheme.typography.titleLarge)
                        } else {
                            Text("${index + 1}", color = CardSurface, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isLocked) Locked else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        section.bagId?.let {
                            Text(
                                text = "الحقيبة $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (section.description.isNotBlank() && !isLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = section.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            if (isCompleted && isCertificateIssued) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎓 ${section.certificateTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DXNGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SkillNode(
    skill: Skill,
    level: Level,
    progress: Map<String, UserProgress>,
    onLevelClick: (String) -> Unit
) {
    val userProgress = progress[level.id]
    val status = userProgress?.status ?: LevelStatus.LOCKED

    val (iconColor, textColor) = when (status) {
        LevelStatus.COMPLETED -> DXNGreen to TextPrimary
        LevelStatus.UNLOCKED -> DXNGreen to TextPrimary
        LevelStatus.LOCKED -> Locked to Locked
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
            .clickable(enabled = status != LevelStatus.LOCKED) {
                if (status == LevelStatus.UNLOCKED || status == LevelStatus.COMPLETED) {
                    onLevelClick(level.id)
                }
            },
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (status) {
                    LevelStatus.COMPLETED -> "✓"
                    LevelStatus.UNLOCKED -> ">"
                    LevelStatus.LOCKED -> "L"
                }
                Text(
                    text = icon,
                    color = CardSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = skill.title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
