package com.appa.recipeapp.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class NukaColors(
    val accent: Color,
    val accentSoft: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val error: Color,
)

@Immutable
data class NukaTheme(
    val id: String,
    val displayName: String,
    val colors: NukaColors,
    val isDark: Boolean,
)

val DefaultTheme = NukaTheme(
    id = "default",
    displayName = "Default",
    colors = NukaColors(
        accent = Accent,
        accentSoft = AccentSoft,
        background = Background,
        surface = Surface,
        surfaceVariant = SurfaceVariant,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        divider = DividerColor,
        error = Error,
    ),
    isDark = true,
)

val FalloutTheme = NukaTheme(
    id = "fallout",
    displayName = "Fallout",
    colors = NukaColors(
        accent = Color(0xFFFFB000),
        accentSoft = Color(0xFF3A4A2A),
        background = Color(0xFF1B2B1B),
        surface = Color(0xFF2A3A2A),
        surfaceVariant = Color(0xFF3A4A3A),
        textPrimary = Color(0xFFFFB000),
        textSecondary = Color(0xFF8FB080),
        divider = Color(0xFF4A5A4A),
        error = Color(0xFFFF6B00),
    ),
    isDark = true,
)

val CreamGardenTheme = NukaTheme(
    id = "cream_garden",
    displayName = "Light Beam",
    colors = NukaColors(
        accent = Color(0xFF7CB342),
        accentSoft = Color(0xFFDCE9C9),
        background = Color(0xFFF5F0E1),
        surface = Color(0xFFFFFBF2),
        surfaceVariant = Color(0xFFEDE6D6),
        textPrimary = Color(0xFF2D2A24),
        textSecondary = Color(0xFF6B6558),
        divider = Color(0xFFD4CDB8),
        error = Color(0xFFC62828),
    ),
    isDark = false,
)

val HalloweenThemePlaceholder = NukaTheme(
    id = "halloween",
    displayName = "Halloween",
    colors = DefaultTheme.colors,
    isDark = true,
)

val AllThemes = listOf(DefaultTheme, FalloutTheme, CreamGardenTheme, HalloweenThemePlaceholder)