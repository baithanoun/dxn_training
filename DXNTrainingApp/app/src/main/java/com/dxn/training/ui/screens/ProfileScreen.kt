package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dxn.training.data.model.UserRole
import com.dxn.training.ui.components.AvatarPlaceholder
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.components.ScreenHeader
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onAdminClick: () -> Unit
) {
    val user by viewModel.user.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = "ملفي")

        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarPlaceholder(size = 90)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DXNBlue
                )
                Text(
                    text = user.dxnId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBox(value = user.currentStreak.toString(), label = "Streak", icon = "★")
            StatBox(value = user.totalXp.toString(), label = "XP", icon = "●")
            StatBox(value = user.certificates.size.toString(), label = "شهادة", icon = "▮")
        }

        Spacer(modifier = Modifier.height(8.dp))

        val menuItems = listOf("حقيبة المتدرب", "الأوسمة", "الشهادات", "الإعدادات", "تسجيل الخروج")
        menuItems.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = TextMuted
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
            }
        }

        if (user.role == UserRole.ADMIN || user.role == UserRole.LEADER) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAdminClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DXNBlue)
            ) {
                Text("لوحة الإدارة")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun StatBox(value: String, label: String, icon: String) {
    Card(
        modifier = Modifier.size(100.dp, 90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                color = DXNGold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = DXNBlue
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}
