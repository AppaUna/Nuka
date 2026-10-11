package com.appa.recipeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.logo

/**
 * Логотип приложения — Nuka-Cola.
 * Используется в шапках Home, Favorites, Profile.
 */
@Composable
fun AppLogo(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "RecipeApp",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size),
    )
}