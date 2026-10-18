package com.appa.recipeapp.presentation.favorites

import com.appa.recipeapp.ui.components.ScreenHeader
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.ui.components.RecipeCardList
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.empty_favorites
import recipeapp.shared.generated.resources.favorites_decor
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onRecipeClick: (String) -> Unit,
) {
    // ─── ДИАГНОСТИКА ───
    val compositionTime = remember { Clock.System.now().toEpochMilliseconds() }
    LaunchedEffect(Unit) {
        println("DEBUG FavoritesScreen: composition+firstFrame ${Clock.System.now().toEpochMilliseconds() - compositionTime} мс, vmHash=${viewModel.hashCode()}")
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Автообновление при заходе на экран (не чаще раза в 30 сек)
    LaunchedEffect(Unit) {
        viewModel.refreshIfStale()
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Декор
        if (state.recipes.isNotEmpty()) {
            Image(
                painter = painterResource(Res.drawable.favorites_decor),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = 0.12f,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) }
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(280.dp)
                    .offset(x = 60.dp, y = (15).dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
        ) {
            item {
                ScreenHeader(
                    title = "Избранное",
                    subtitle = "Твои любимые рецепты",
                )
            }

            if (state.recipes.isEmpty()) {
                item { EmptyFavoritesContent() }
            } else {
                items(state.recipes, key = { it.id }) { recipe ->
                    RecipeCardList(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(recipe.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.empty_favorites),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(180.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Пока здесь ничего нет...",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Нажми на сердечко у любого рецепта,\nчтобы добавить его сюда",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )
    }
}