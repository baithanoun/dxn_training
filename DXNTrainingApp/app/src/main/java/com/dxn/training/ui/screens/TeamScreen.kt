package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dxn.training.data.model.MemberStatus
import com.dxn.training.data.model.TeamMember
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.components.DxProgressBar
import com.dxn.training.ui.components.ScreenHeader
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.text.g
import com.dxn.training.ui.viewmodel.TeamViewModel

@Composable
fun TeamScreen(
    viewModel: TeamViewModel,
    onMemberClick: (String) -> Unit
) {
    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var filter by remember { mutableStateOf(MemberStatus.ACTIVE) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "فريقي")

        // Summary — الأرقام من استجابة السيرفر.
        val average = if (members.isEmpty()) 0 else members.sumOf { it.progressPercent } / members.size
        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "ملخص الفريق",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الأعضاء: ${members.size}  |  متوسط الإكمال: $average%",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            DxProgressBar(progress = average / 100f)
        }

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FilterChip(
                label = "نشط",
                selected = filter == MemberStatus.ACTIVE,
                onClick = { filter = MemberStatus.ACTIVE },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                label = "متوقف >3 أيام",
                selected = filter == MemberStatus.INACTIVE,
                onClick = { filter = MemberStatus.INACTIVE },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                label = "مكتمل",
                selected = filter == MemberStatus.COMPLETED,
                onClick = { filter = MemberStatus.COMPLETED },
                modifier = Modifier.weight(1f)
            )
        }

        val filtered = members.filter { it.status == filter }
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = BrandPlum,
                    modifier = Modifier.padding(16.dp)
                )
            }
            error != null || filtered.isEmpty() -> {
                Text(
                    text = "لا يوجد أعضاء بعد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            else -> filtered.forEach { member ->
                MemberRow(member = member, onClick = { onMemberClick(member.id) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        DxButton(
            text = g("شجّع فريقك", "شجّعي فريقك"),
            onClick = { },
            containerColor = Xp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) BrandPlum else Surface,
            contentColor = if (selected) Surface else TextMuted
        )
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MemberRow(member: TeamMember, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Disabled),
                contentAlignment = Alignment.Center
            ) {
                Text(text = member.name.firstOrNull()?.toString().orEmpty(), color = TextMuted)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "إنجاز ${member.progressPercent}%  |  آخر نشاط قبل ${member.lastActiveDays} يوم",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            val color = when (member.status) {
                MemberStatus.ACTIVE -> Leaf
                MemberStatus.INACTIVE -> Error
                MemberStatus.COMPLETED -> Xp
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
