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

/**
 * Дополнительные праздничные оттенки для световых бликов, фасок и акцентов очага.
 */
object WinterPalette {
    val MidnightBase = Color(0xFF060F1B)
    val MidnightElevated = Color(0xFF0C1C2E)
    val CryoSlate = Color(0xFF132942)
    val GlacialCyan = Color(0xFF67E8F9)
    val FrostHighlight = Color(0xFFE0F2FE)
    val IceBlue = Color(0xFFBAE6FD)
    val ChampagneGold = Color(0xFFFDE68A)
    val HearthAmber = Color(0xFFF59E0B)
    val BerryGlow = Color(0xFFFF5C77)
}

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

val NewYearTheme = NukaTheme(
    id = "new_year",
    displayName = "New Year",
    colors = NukaColors(
        accent = Color(0xFF67E8F9),
        accentSoft = Color(0x2E67E8F9),
        background = Color(0xFF060F1B),
        surface = Color(0xFF0C1C2E),
        surfaceVariant = Color(0xFF132942),
        textPrimary = Color(0xFFF4F9FF),
        textSecondary = Color(0xFF93B4D4),
        divider = Color(0xFF1D3856),
        error = Color(0xFFFF5C77),
    ),
    isDark = true,
)

val AllThemes = listOf(
    DefaultTheme,
    FalloutTheme,
    CreamGardenTheme,
    NewYearTheme,
)

fun getThemeById(id: String): NukaTheme {
    return AllThemes.firstOrNull { it.id == id } ?: DefaultTheme
}