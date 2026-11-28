package com.dxn.training.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dxn.training.data.remote.EventSender
import com.dxn.training.data.repository.ContentRepository
import com.dxn.training.ui.components.*
import com.dxn.training.ui.theme.*
import com.dxn.training.ui.text.g

@Composable
fun LessonScreen(
    levelId: String,
    onStartQuiz: () -> Unit,
    onFinishLesson: () -> Unit,
    onBack: () -> Unit,
    contentRepository: ContentRepository = ContentRepository.getInstance()
) {
    val level = remember(levelId) { contentRepository.getLevel(levelId) }
    val cards = level?.cards ?: emptyList()
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(levelId) { EventSender.send("level_started", levelId = levelId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandIvory)
    ) {
        ScreenHeader(
            title = level?.title ?: "درس",
            onClose = onBack,
            streak = 5
        )

        if (level == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("المستوى غير موجود", color = Error, style = MaterialTheme.typography.bodyLarge)
            }
            return
        }

        if (cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد بطاقات لهذا المستوى", color = TextMuted)
            }
            return
        }

        Text(
            text = "بطاقة ${currentIndex + 1} من ${cards.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        DxProgressBar(
            progress = (currentIndex + 1) / cards.size.toFloat(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val card = cards[currentIndex]
        DxCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            val mediaUrl = card.mediaUrl
            if (mediaUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(BrandRose50),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DXN",
                        style = MaterialTheme.typography.displayMedium,
                        color = BrandRose600
                    )
                }
            } else {
                // الخلفية العاجية تظهر أثناء التحميل وعند فشل الصورة؛ لا يُعرض أي تنبيه خطأ.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(BrandIvory)
                ) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = card.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineMedium,
                color = BrandPlum700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.body,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            card.tip?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "نصيحة: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DxButton(
            text = if (currentIndex < cards.size - 1) {
                "التالي"
            } else if (level.type == "lesson") {
                "إنهاء الدرس"
            } else {
                g("ابدأ الاختبار", "ابدئي الاختبار")
            },
            onClick = {
                if (currentIndex < cards.size - 1) {
                    currentIndex++
                } else if (level.type == "lesson") {
                    // درس بلا إجابات: يُرسل طلب إكمال بقائمة إجابات فارغة.
                    onFinishLesson()
                } else {
                    onStartQuiz()
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}
