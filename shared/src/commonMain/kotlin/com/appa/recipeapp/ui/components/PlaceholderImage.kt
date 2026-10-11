package com.appa.recipeapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Временная заглушка вместо фото.
 * Позже заменим на AsyncImage (Coil 3).
 */
@Composable
fun PlaceholderImage(
    seed: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
) {
    val (start, end) = gradientFor(seed)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(listOf(start, end))
            ),
    )
}

/**
 * Детерминированный градиент по строке-сиду (обычно название рецепта).
 * Одинаковый seed всегда даёт одинаковые цвета.
 */
private fun gradientFor(seed: String): Pair<Color, Color> {
    val palettes = listOf(
        Color(0xFFE07A5F) to Color(0xFF8B3A2E), // терракота
        Color(0xFFF2A65A) to Color(0xFFC97B2E), // оранжевый
        Color(0xFF81B29A) to Color(0xFF3E6B57), // зелёный
        Color(0xFF6A8EAF) to Color(0xFF335066), // синий
        Color(0xFFB5838D) to Color(0xFF6B4A52), // розово-винный
        Color(0xFF9A8C98) to Color(0xFF4A414D), // серо-фиолетовый
        Color(0xFFE29578) to Color(0xFFA75B3F), // персиковый
        Color(0xFF83C5BE) to Color(0xFF3B7A73), // бирюзовый
    )
    val index = (seed.hashCode() and 0x7FFFFFFF) % palettes.size
    return palettes[index]
}