package com.dxn.training.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DXNRed,
    onPrimary = CardSurface,
    primaryContainer = Color(0xFFFFE4E1),
    onPrimaryContainer = DXNRedDeep,
    secondary = DXNGold,
    onSecondary = CardSurface,
    secondaryContainer = Color(0xFFFFF8E1),
    onSecondaryContainer = Color(0xFF6D5400),
    tertiary = DXNBlue,
    onTertiary = CardSurface,
    tertiaryContainer = Color(0xFFE3EDF3),
    onTertiaryContainer = DXNBlue,
    background = Background,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = CardSurface,
    outline = Disabled
)

private val DarkColorScheme = darkColorScheme(
    primary = DXNRed,
    onPrimary = CardSurface,
    primaryContainer = Color(0xFF5A1208),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = DXNGold,
    onSecondary = CardSurface,
    secondaryContainer = Color(0xFF4A3700),
    onSecondaryContainer = Color(0xFFFFEECB),
    tertiary = DXNBlue,
    onTertiary = CardSurface,
    tertiaryContainer = Color(0xFF06162C),
    onTertiaryContainer = Color(0xFFB8D4E3),
    background = TextPrimary,
    onBackground = CardSurface,
    surface = Color(0xFF2D3748),
    onSurface = CardSurface,
    error = ErrorRed,
    onError = CardSurface,
    outline = Color(0xFF4A5568)
)

@Composable
fun DXNTrainingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
