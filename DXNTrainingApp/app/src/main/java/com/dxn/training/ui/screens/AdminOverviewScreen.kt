package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.components.CloseButton
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.theme.*

@Composable
fun AdminOverviewScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CloseButton(onClick = onBack)
            Text(
                text = "لوحة الإدارة",
                style = MaterialTheme.typography.bodyLarge,
                color = DXNBlue
            )
            Box(modifier = Modifier.size(40.dp))
        }

        KpiCard(
            title = "المستخدمون النشطون",
            value = "1,240  ↑ 12%",
            containerColor = DXNRed,
            contentColor = CardSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            KpiCard(
                title = "معدل الإكمال",
                value = "68%",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            KpiCard(
                title = "متوسط Streak",
                value = "4.2",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "أكثر المهارات رسوباً",
            style = MaterialTheme.typography.bodyLarge,
            color = DXNBlue,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val skills = listOf("نظام النقاط" to "35%", "الرد على الاعتراضات" to "28%", "سلم الرتب" to "22%")
        skills.forEach { (skill, rate) ->
            DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$rate رسوب", color = DXNRedDeep, style = MaterialTheme.typography.bodyMedium)
                    Text(text = skill, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = CardSurface,
    contentColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor
            )
        }
    }
}
