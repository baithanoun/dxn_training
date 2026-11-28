package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.BadgeChips
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.components.ScreenHeader
import com.dxn.training.ui.text.g
import com.dxn.training.ui.theme.BrandIvory
import com.dxn.training.ui.theme.BrandPlum700
import com.dxn.training.ui.theme.Success
import com.dxn.training.ui.theme.TextMuted
import com.dxn.training.ui.theme.TextPrimary
import com.dxn.training.ui.viewmodel.TeamViewModel

@Composable
fun MemberDetailsScreen(
    memberId: String,
    viewModel: TeamViewModel,
    contentRepository: ContentRepository,
    onBack: () -> Unit
) {
    val members by viewModel.members.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val member = members.firstOrNull { it.id == memberId }

    LaunchedEffect(memberId) { viewModel.loadGrades(memberId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = member?.name ?: g("تفاصيل العضو", "تفاصيل العضوة"),
            onClose = onBack
        )

        if (member == null) {
            Text(
                text = g("لم يعد هذا العضو في الفريق", "لم تعد هذه العضوة في الفريق"),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            return
        }

        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = g("إنجاز ${member.progressPercent}%", "إنجاز ${member.progressPercent}%"),
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            DxProgressBar(progress = member.progressPercent / 100f)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = g("آخر نشاط قبل ${member.lastActiveDays} يوم", "آخر نشاط قبل ${member.lastActiveDays} يوم"),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = g("الأوسمة", "الأوسمة"),
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            BadgeChips(badgeIds = member.badges)
        }

        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = g("الأقسام", "الأقسام"),
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (member.sections.isEmpty()) {
                Text(
                    text = g("لا توجد أقسام بعد", "لا توجد أقسام بعد"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                member.sections.forEach { section ->
                    Text(
                        text = "${section.title}: ${section.completedLevels}/${section.totalLevels}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    if (section.certificate) {
                        Text(
                            text = g("🎓 شهادة صادرة", "🎓 شهادة صادرة"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Success
                        )
                    }
                }
            }
        }

        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = g("الدرجات", "الدرجات"),
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (grades.isEmpty()) {
                Text(
                    text = g("لا توجد درجات بعد", "لا توجد درجات بعد"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                grades.forEach { (levelId, progress) ->
                    val levelTitle = contentRepository.getLevel(levelId)?.title
                        ?: g("مستوى", "مستوى")
                    Text(
                        text = "$levelTitle — ${g("الدرجة", "الدرجة")}: ${progress.bestScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
