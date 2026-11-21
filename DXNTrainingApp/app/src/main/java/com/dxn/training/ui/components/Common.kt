package com.dxn.training.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.theme.*

@Composable
fun DxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = DXNRed,
    contentColor: Color = CardSurface
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = Disabled,
            contentColor = contentColor,
            disabledContentColor = CardSurface
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DxOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DXNRed
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DxCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun DxProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = DXNGreen,
    trackColor: Color = Disabled
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
    }
}

@Composable
fun DxTrustBar(trust: Int, modifier: Modifier = Modifier) {
    val progress = trust.coerceIn(0, 100) / 100f
    val color = when {
        progress < 0.4f -> DXNRedDeep
        progress < 0.7f -> WarningAmber
        else -> DXNGreen
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "الثقة",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted,
            modifier = Modifier.padding(end = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Disabled)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        Text(
            text = "$trust%",
            style = MaterialTheme.typography.labelMedium,
            color = DXNBlue,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun HeartsIndicator(hearts: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        repeat(3) { index ->
            val active = index < hearts
            Text(
                text = "♥",
                color = if (active) DXNRedDeep else Disabled,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "إغلاق",
            tint = TextMuted
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    streak: Int? = null,
    onClose: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onClose != null) {
            CloseButton(onClick = onClose)
        } else {
            Box(modifier = Modifier.size(40.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = DXNBlue
        )

        if (streak != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥",
                    color = DXNRedDeep,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "$streak أيام",
                    color = DXNRedDeep,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Box(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun AvatarPlaceholder(modifier: Modifier = Modifier, size: Int = 48) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Disabled),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((size * 0.5).dp)
                .clip(CircleShape)
                .background(TextMuted)
        )
    }
}
