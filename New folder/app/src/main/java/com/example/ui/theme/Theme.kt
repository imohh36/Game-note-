package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val customTextColor by ThemePreferences.textColor.collectAsState()
    val customBgColor by ThemePreferences.bgColor.collectAsState()

    val isDarkBackground = ThemePreferences.getContrastingContentColor(customBgColor) == Color(0xFFF8FAFC)
    val surfaceColor = if (isDarkBackground) {
        Color(
            red = (customBgColor.red * 0.9f + 0.04f).coerceIn(0f, 1f),
            green = (customBgColor.green * 0.9f + 0.05f).coerceIn(0f, 1f),
            blue = (customBgColor.blue * 0.9f + 0.07f).coerceIn(0f, 1f)
        )
    } else {
        Color.White
    }

    val surfaceVariantColor = if (isDarkBackground) {
        Color(
            red = (customBgColor.red * 0.8f + 0.10f).coerceIn(0f, 1f),
            green = (customBgColor.green * 0.8f + 0.12f).coerceIn(0f, 1f),
            blue = (customBgColor.blue * 0.8f + 0.16f).coerceIn(0f, 1f)
        )
    } else {
        Color(0xFFF1F5F9)
    }

    val dynamicColorScheme = darkColorScheme(
        primary = customTextColor,
        onPrimary = ThemePreferences.getContrastingContentColor(customTextColor),
        primaryContainer = customTextColor.copy(alpha = 0.22f),
        onPrimaryContainer = customTextColor,
        secondary = DiamondCyan,
        onSecondary = Color(0xFF003544),
        secondaryContainer = Color(0xFF075985),
        onSecondaryContainer = Color(0xFFBAE6FD),
        tertiary = AmberQuest,
        onTertiary = Color(0xFF452B00),
        tertiaryContainer = AmberQuestDark,
        onTertiaryContainer = Color(0xFFFEF3C7),
        background = customBgColor,
        onBackground = customTextColor,
        surface = surfaceColor,
        onSurface = customTextColor,
        surfaceVariant = surfaceVariantColor,
        onSurfaceVariant = customTextColor.copy(alpha = 0.72f),
        error = ErrorRed,
        onError = Color(0xFF450A0A)
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = Typography,
        content = content
    )
}

