package com.appa.recipeapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.onest_bold
import recipeapp.shared.generated.resources.onest_medium
import recipeapp.shared.generated.resources.onest_regular
import recipeapp.shared.generated.resources.onest_semibold

/**
 * Семейство шрифтов Onest.
 * Regular — для основного текста.
 * Medium — для метаданных.
 * SemiBold — для названий и подзаголовков.
 * Bold — для крупных заголовков.
 */
@Composable
fun AppTypography(): Typography {
    val onestFamily = FontFamily(
        Font(Res.font.onest_regular, FontWeight.Normal),
        Font(Res.font.onest_medium, FontWeight.Medium),
        Font(Res.font.onest_semibold, FontWeight.SemiBold),
        Font(Res.font.onest_bold, FontWeight.Bold),
    )

    return Typography(
        headlineMedium = TextStyle(
            fontFamily = onestFamily,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineSmall = TextStyle(
            fontFamily = onestFamily,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleMedium = TextStyle(
            fontFamily = onestFamily,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleSmall = TextStyle(
            fontFamily = onestFamily,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = TextStyle(
            fontFamily = onestFamily,
            color = TextPrimary,
            fontSize = 15.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = onestFamily,
            color = TextSecondary,
            fontSize = 13.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = onestFamily,
            color = TextTertiary,
            fontSize = 12.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = onestFamily,
            color = TextSecondary,
            fontSize = 14.sp,
        ),
    )
}