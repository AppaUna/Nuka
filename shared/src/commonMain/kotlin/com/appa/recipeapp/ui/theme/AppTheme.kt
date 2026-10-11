package com.appa.recipeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Background,

    secondary = Accent,
    onSecondary = Background,

    // Контейнеры выбранных/невыбранных элементов
    secondaryContainer = AccentSoft,
    onSecondaryContainer = Accent,

    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = Error,
    outline = DividerColor,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography(),   // ← ВЫЗОВ функции со скобками
        content = content,
    )
}