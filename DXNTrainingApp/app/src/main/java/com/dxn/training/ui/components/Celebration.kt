package com.dxn.training.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.theme.BrandPlum
import com.dxn.training.ui.theme.BrandRose
import com.dxn.training.ui.theme.Heart
import com.dxn.training.ui.theme.Leaf
import com.dxn.training.ui.theme.LeafText
import com.dxn.training.ui.theme.Xp
import com.dxn.training.ui.text.g
import kotlin.math.cos
import kotlin.math.sin

private const val CelebrationDurationMs = 1200
private const val ConfettiPieces = 16
private val ConfettiColors = listOf(BrandPlum, BrandRose, Xp, Leaf, Heart)

internal fun confettiEnabled(animatorDurationScale: Float): Boolean = animatorDurationScale != 0f

/** Immediate feedback shared by every game screen after a correct answer. */
@Composable
fun CorrectAnswerFeedback(visible: Boolean) {
    if (!visible) return
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        CorrectAnswerSound.play(context)
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = g("أحسنتَ!", "أحسنتِ!"),
            style = MaterialTheme.typography.titleLarge,
            color = LeafText
        )
        Celebration()
    }
}

@Composable
fun Celebration() {
    val context = LocalContext.current
    val showConfetti = remember {
        confettiEnabled(
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        )
    }
    if (!showConfetti) return

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(CelebrationDurationMs, easing = LinearEasing))
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(72.dp)) {
        val centerX = size.width / 2f
        val traveled = progress.value
        val alpha = (1f - traveled).coerceIn(0f, 1f)
        val pieceWidth = 8.dp.toPx()
        val pieceHeight = 5.dp.toPx()
        repeat(ConfettiPieces) { index ->
            val angle = index * (2f * Math.PI.toFloat() / ConfettiPieces)
            val distance = traveled * size.width * 0.4f
            val x = centerX + cos(angle) * distance
            val y = size.height * 0.15f + traveled * size.height * 0.7f + sin(angle) * distance * 0.3f
            translate(x, y) {
                rotate(degrees = index * 37f + traveled * 360f) {
                    drawRect(
                        color = ConfettiColors[index % ConfettiColors.size].copy(alpha = alpha),
                        topLeft = Offset(-pieceWidth / 2f, -pieceHeight / 2f),
                        size = Size(pieceWidth, pieceHeight)
                    )
                }
            }
        }
    }
}
