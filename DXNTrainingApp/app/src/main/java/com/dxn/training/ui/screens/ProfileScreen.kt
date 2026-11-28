package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dxn.training.data.model.UserRole
import com.dxn.training.ui.components.AvatarPlaceholder
import com.dxn.training.ui.components.BadgeChips
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.components.DxCard
import com.dxn.training.ui.components.ScreenHeader
import com.dxn.training.ui.components.dxTextFieldColors
import com.dxn.training.ui.share.ShareHelper
import com.dxn.training.ui.text.g
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.viewmodel.ProfileViewModel
import com.dxn.training.ui.viewmodel.TeamViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    teamViewModel: TeamViewModel,
    isLeader: Boolean = false,
    onAdminClick: () -> Unit,
    onCertificateClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val teamMembers by teamViewModel.members.collectAsState()
    val teamLoading by teamViewModel.isLoading.collectAsState()
    val teamError by teamViewModel.error.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(isLeader) {
        if (isLeader) teamViewModel.load()
    }

    // Refresh certificates each time the screen comes back to the foreground
    // (e.g. after completing a section and receiving a certificate).
    // shortcut: manual resume observer because Compose UI is 1.6; switch back to LifecycleResumeEffect after moving the Compose BOM to >= 2024.09
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) viewModel.refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
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
                    text = user.fullName.ifBlank { "..." },
                    style = MaterialTheme.typography.headlineMedium,
                    color = BrandPlum700
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
            StatBox(value = certificates.size.toString(), label = "شهادة", icon = "▮")
        }

        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = g("الأوسمة", "الأوسمة"),
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            BadgeChips(badgeIds = user.badges)
        }

        if (isLeader && user.dxnId.isNotBlank()) {
            val inviteLink = "https://dxn-train.dockhosting.cloud/join/${user.dxnId}"
            DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "رابط دعوتي",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrandPlum700
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = inviteLink,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { clipboardManager.setText(AnnotatedString(inviteLink)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("نسخ")
                    }
                    DxButton(
                        text = "مشاركة",
                        onClick = { ShareHelper.shareText(context, inviteLink) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "المسجّلون عبر رابطك",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrandPlum700
                )
                Spacer(modifier = Modifier.height(4.dp))
                when {
                    teamLoading -> {
                        CircularProgressIndicator(
                            color = BrandPlum,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    teamError != null && teamMembers.isEmpty() -> {
                        Text(
                            text = "تعذر تحميل المسجّلين عبر رابطك",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    teamMembers.isEmpty() -> {
                        Text(
                            text = "لا يوجد مسجّلون بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    else -> {
                        Text(
                            text = "العدد: ${teamMembers.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        teamMembers.forEach { member ->
                            Text(
                                text = "• ${member.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // الشهادات من تقدّم الأقسام على السيرفر.
        DxCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "الشهادات",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (certificates.isEmpty()) {
                Text(
                    text = "لا توجد شهادات بعد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                certificates.forEach { certificate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onCertificateClick(
                                    certificate.sectionId.ifBlank { certificate.id }
                                )
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎓 ${certificate.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (certificate.issuedAt.isNotBlank()) {
                                Text(
                                    text = certificate.issuedAt.take(10),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "عرض الشهادة",
                                tint = TextMuted
                            )
                        }
                    }
                }
            }
        }

        val menuItems = listOf("حقيبة المتدرب", "الإعدادات")
        menuItems.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onLogout() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تسجيل الخروج",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Error
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Error
                )
            }
        }

        var showDeleteDialog by remember { mutableStateOf(false) }
        var deletePassword by remember { mutableStateOf("") }
        var deleteError by remember { mutableStateOf<String?>(null) }
        var isDeleting by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
                title = { Text("حذف الحساب") },
                text = {
                    Column {
                        Text("أدخل كلمة المرور لتأكيد حذف الحساب. لا يمكن التراجع.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deletePassword,
                            onValueChange = { deletePassword = it },
                            label = { Text("كلمة المرور") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = dxTextFieldColors()
                        )
                        deleteError?.let { Text(it, color = Error, style = MaterialTheme.typography.bodySmall) }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting && deletePassword.isNotBlank(),
                        onClick = {
                            isDeleting = true
                            deleteError = null
                            scope.launch {
                                val ok = viewModel.deleteAccount(deletePassword)
                                isDeleting = false
                                if (ok) {
                                    showDeleteDialog = false
                                    onLogout()
                                } else {
                                    deleteError = "تعذر حذف الحساب. تحقق من كلمة المرور."
                                }
                            }
                        }
                    ) { Text("حذف", color = Error) }
                },
                dismissButton = {
                    TextButton(enabled = !isDeleting, onClick = { showDeleteDialog = false }) { Text("إلغاء") }
                }
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { showDeleteDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "حذف الحساب",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Error
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Error
                )
            }
        }

        if (user.role == UserRole.ADMIN || user.role == UserRole.LEADER) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAdminClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPlum900)
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
        colors = CardDefaults.cardColors(containerColor = BrandIvory)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                color = Xp,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = BrandPlum700
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}
