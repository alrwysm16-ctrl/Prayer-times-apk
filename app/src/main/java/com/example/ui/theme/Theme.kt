package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldSoft,
    onPrimary = Color.Black,
    primaryContainer = EmeraldMedium,
    onPrimaryContainer = GoldLight,
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = EmeraldDeep,
    onSecondaryContainer = GoldLight,
    tertiary = GoldAccent,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = EmeraldMedium.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldMedium,
    onPrimary = Color.White,
    primaryContainer = EmeraldBgLight,
    onPrimaryContainer = EmeraldDeep,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldMuted,
    onSecondaryContainer = EmeraldDeep,
    tertiary = EmeraldDeep,
    background = EmeraldBgLight,
    onBackground = TextPrimaryLight,
    surface = EmeraldCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE9F2ED),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFD0DDD6)
)

@Composable
fun NoorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
