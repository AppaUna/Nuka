package com.appa.recipeapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun RecipeImage(
    photoUri: String?,
    seed: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
) {
    if (photoUri.isNullOrBlank()) {
        PlaceholderImage(
            seed = seed,
            modifier = modifier,
            cornerRadius = cornerRadius,
        )
    } else {
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