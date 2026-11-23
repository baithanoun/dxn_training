package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.components.DxButton
import com.dxn.training.ui.theme.*

@Composable
fun OnboardingScreen(page: Int, onNext: () -> Unit) {
    val data = when (page) {
        1 -> Triple("تعلم بأسلوب تفاعلي", "كبسولات معرفية قصيرة ومحفزة يومياً.", BrandPlum50)
        2 -> Triple("اكسب XP وأوسمة", "تابع تقدمك واحتفظ بسلسلة النجاح اليومي.", XpContainer)
        else -> Triple("ابنِ فريقك", "تابع أعضاء فريقك وشجعهم للوصول للنجاح معاً.", BrandPlum100)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(data.third),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                1 -> Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DXN",
                        style = MaterialTheme.typography.headlineMedium,
                        color = BrandPlum
                    )
                }
                2 -> Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "★", style = MaterialTheme.typography.displayLarge, color = Xp)
                }
                else -> Row {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BrandPlum900)
                                .padding(4.dp)
                        )
                        if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = data.first,
            style = MaterialTheme.typography.headlineMedium,
            color = BrandPlum700,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = data.second,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

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
        DxButton(
            text = when (page) {
                1 -> "ابدأ"
                3 -> "تسجيل الدخول"
                else -> "التالي"
            },
            onClick = onNext
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
