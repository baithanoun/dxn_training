package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxn.training.data.remote.AppConfigResponse
import com.dxn.training.ui.components.BrandMark
import com.dxn.training.ui.components.BrandMarkSize
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.theme.*

@Composable
fun OnboardingScreen(page: Int, config: AppConfigResponse, onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(if (page == 1) 32.dp else 48.dp))

        if (page == 1) {
            BrandMark(size = BrandMarkSize.Large)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = config.teamName,
                style = MaterialTheme.typography.headlineMedium,
                color = BrandPlum700,
                textAlign = TextAlign.Center
            )
            config.teamTagline?.takeIf { it.isNotBlank() }?.let { tagline ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrandRose600,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(LeafContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Eco,
                                contentDescription = "ورقة",
                                tint = LeafText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "نبذة الفريق",
                            style = MaterialTheme.typography.titleMedium,
                            color = LeafText
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = config.teamBio.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        textAlign = TextAlign.Start
                    )
                }
            }
        } else {
            val neutral = if (page == 2) {
                Pair("اجمع XP وأوسمة", "أكمل الدروس والاختبارات لتكسب نقاط الخبرة والأوسمة، وتابع سلسلة نجاحك اليومية.")
            } else {
                Pair("ابنِ فريقك", "تابع أعضاء فريقك وشجّعهم للوصول للنجاح معاً.")
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (page == 2) XpContainer else LeafContainer),
                contentAlignment = Alignment.Center
            ) {
                if (page == 2) {
                    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                        Text(text = "★", style = MaterialTheme.typography.displayLarge, color = Xp)
                    }
                } else {
                    Row {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(LeafText)
                                    .padding(4.dp)
                            )
                            if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = neutral.first,
                style = MaterialTheme.typography.headlineMedium,
                color = BrandPlum700,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = neutral.second,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Dots
        Row {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index + 1 == page) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (index + 1 == page) BrandPlum else Disabled)
                )
                if (index < 2) Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (page == 3) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPlum,
                    contentColor = Surface
                )
            ) {
                Text(
                    text = config.teamWelcome?.takeIf { it.isNotBlank() } ?: "ابدأ",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    softWrap = true
                )
            }
        } else {
            DxButton(
                text = if (page == 1) "ابدأ" else "التالي",
                onClick = onNext
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
