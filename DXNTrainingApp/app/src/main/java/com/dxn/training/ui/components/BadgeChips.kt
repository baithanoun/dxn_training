package com.dxn.training.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dxn.training.ui.text.g
import com.dxn.training.ui.theme.BrandPlum50
import com.dxn.training.ui.theme.BrandPlum700
import com.dxn.training.ui.theme.TextMuted
import com.dxn.training.ui.theme.XpContainer
import com.dxn.training.ui.theme.XpText

internal data class BadgePresentation(
    val icon: String,
    val maleLabel: String,
    val femaleLabel: String
)

/** Single source of truth for the three server-computed badge ids (D5 option أ). */
internal fun badgePresentation(id: String): BadgePresentation? = when (id) {
    "first_section" -> BadgePresentation("🎓", "أول قسم", "أول قسم")
    "streak_7" -> BadgePresentation("🔥", "سلسلة ٧ أيام", "سلسلة ٧ أيام")
    "all_complete" -> BadgePresentation("★", "إكمال كامل", "إكمال كامل")
    else -> null
}

@Composable
fun BadgeChips(badgeIds: List<String>, modifier: Modifier = Modifier) {
    val badges = badgeIds.mapNotNull(::badgePresentation)
    if (badges.isEmpty()) {
        Text(
            text = g("لا توجد أوسمة بعد", "لا توجد أوسمة بعد"),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = modifier
        )
        return
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        badges.forEach { badge ->
            val isCompletion = badge.icon == "★"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCompletion) XpContainer else BrandPlum50
            ) {
                Text(
                    text = "${badge.icon} ${g(badge.maleLabel, badge.femaleLabel)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCompletion) XpText else BrandPlum700,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
