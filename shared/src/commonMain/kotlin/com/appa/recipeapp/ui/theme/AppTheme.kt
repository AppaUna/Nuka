package com.appa.recipeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(theme: NukaTheme = DefaultTheme, content: @Composable () -> Unit) {
    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            primary = theme.colors.accent,
            onPrimary = theme.colors.background,
            secondary = theme.colors.accent,
            onSecondary = theme.colors.background,
            secondaryContainer = theme.colors.accentSoft,
            onSecondaryContainer = theme.colors.accent,
            background = theme.colors.background,
            onBackground = theme.colors.textPrimary,
            surface = theme.colors.surface,
            onSurface = theme.colors.textPrimary,
            surfaceVariant = theme.colors.surfaceVariant,
            onSurfaceVariant = theme.colors.textSecondary,
            error = theme.colors.error,
            outline = theme.colors.divider,
        )
    } else {
        lightColorScheme(
            primary = theme.colors.accent,
            onPrimary = theme.colors.background,
            secondary = theme.colors.accent,
            onSecondary = theme.colors.background,
            secondaryContainer = theme.colors.accentSoft,
            onSecondaryContainer = theme.colors.accent,
            background = theme.colors.background,
            onBackground = theme.colors.textPrimary,
            surface = theme.colors.surface,
            onSurface = theme.colors.textPrimary,
            surfaceVariant = theme.colors.surfaceVariant,
            onSurfaceVariant = theme.colors.textSecondary,
            error = theme.colors.error,
            outline = theme.colors.divider,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content,
    )
}