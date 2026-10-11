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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Универсальный компонент фото:
 * - Если [photoUri] не null и не пустой — грузим фото через Coil.
 * - Иначе — показываем цветной градиент (детерминированно от [seed]).
 */
@Composable
fun RecipeImage(
    photoUri: String?,
    seed: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
) {
    if (photoUri.isNullOrBlank()) {
        // Градиент-заглушка
        PlaceholderImage(
            seed = seed,
            modifier = modifier,
            cornerRadius = cornerRadius,
        )
    } else {
        // Реальное фото через Coil
        Box(
            modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        ) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),

            )
        }
    }
}