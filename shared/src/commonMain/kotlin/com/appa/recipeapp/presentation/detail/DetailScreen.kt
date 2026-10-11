package com.appa.recipeapp.presentation.detail

import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.ui.components.PlaceholderImage
import com.appa.recipeapp.ui.components.RecipeImage
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.appa.recipeapp.ui.components.SkeletonDetailContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock


@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
) {
    // ─── ДИАГНОСТИКА ───
    val compositionTime = remember { Clock.System.now().toEpochMilliseconds() }
    LaunchedEffect(Unit) {
        println("DEBUG DetailScreen: composition+firstFrame ${Clock.System.now().toEpochMilliseconds() - compositionTime} мс, vmHash=${viewModel.hashCode()}")
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> {
            SkeletonDetailContent()
        }
        state.recipe == null -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Background),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.errorMessage ?: "Рецепт не найден",
                    color = TextPrimary,
                    fontSize = 16.sp,
                )
            }
        }
        else -> {
            val recipe = state.recipe!!

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                item {
                    TopPhotoBlock(
                        recipe = recipe,
                        isMyRecipe = state.isMyRecipe,
                        onBackClick = onBackClick,
                        onEditClick = { onEditClick(recipe.id) },
                        onFavoriteClick = { viewModel.toggleFavorite() },
                    )
                }

                item {
                    TitleCard(recipe = recipe)
                }

                item {
                    SectionBlock(title = "Ингредиенты") {
                        IngredientsList(recipe)
                    }
                }

                item {
                    SectionBlock(title = "Приготовление") {
                        StepsList(recipe)
                    }
                }

                item {
                    if (recipe.musicNote.isNotBlank() || recipe.musicTracks.isNotEmpty()) {
                        SectionBlock(title = "Музыка для готовки") {
                            MusicBlock(recipe)
                        }
                    } else {
                        Spacer(Modifier.height(0.dp))
                    }
                }

                item {
                    SectionBlock(title = "Критика", showDivider = false) {
                        ReviewsBlock(
                            ratings = state.ratings,
                            draftStars = state.draftStars,
                            draftComment = state.draftComment,
                            onStarsChange = viewModel::onStarsChange,
                            onCommentChange = viewModel::onCommentChange,
                            onSubmit = viewModel::submitRating,
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// ВЕРХНИЙ БЛОК
// ═══════════════════════════════════════════

@Composable
private fun TopPhotoBlock(
    recipe: Recipe,
    isMyRecipe: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
    ) {
        if (!recipe.photoUrls.firstOrNull().isNullOrBlank()) {
            AsyncImage(
                model = recipe.photoUrls.firstOrNull(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            PlaceholderImage(
                seed = recipe.title,
                modifier = Modifier.fillMaxSize(),
            )
        }

        CircleButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            onClick = onBackClick,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Кнопка «Редактировать» — только для своих рецептов
            if (isMyRecipe) {
                CircleButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            CircleButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "В избранное",
                    tint = if (recipe.isFavorite) Accent else TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// КАРТОЧКА С НАЗВАНИЕМ
// ═══════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TitleCard(recipe: Recipe) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-32).dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = recipe.title,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatRating(recipe),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

            MetricDivider()

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = recipe.authorName,
                color = TextSecondary,
                fontSize = 13.sp,
            )

            MetricDivider()

            Text(
                text = "${recipe.cookingTimeMinutes} мин",
                color = TextSecondary,
                fontSize = 13.sp,
            )
        }

        if (recipe.tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                recipe.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = tag,
                            color = TextSecondary,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        if (recipe.description.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = recipe.description,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

// ═══════════════════════════════════════════
// СЕКЦИЯ
// ═══════════════════════════════════════════

@Composable
private fun SectionBlock(
    title: String,
    showDivider: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 5.dp, bottom = 20.dp),
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(36.dp))
        content()
        if (showDivider) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerColor.copy(alpha = 0.5f)),
            )
        }
    }
}

// ─── Ингредиенты ───

@Composable
private fun IngredientsList(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        recipe.ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = ingredient.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(14.dp)
                        .drawBehind {
                            val dotRadius = 1.dp.toPx()
                            val spacing = 5.dp.toPx()
                            val y = size.height - dotRadius * 2
                            var x = 0f
                            while (x < size.width) {
                                drawCircle(
                                    color = Color(0xFF9E9E9E).copy(alpha = 0.4f),
                                    radius = dotRadius,
                                    center = Offset(x + dotRadius, y),
                                )
                                x += spacing
                            }
                        },
                )
                Text(
                    text = "${ingredient.amount} ${ingredient.unit}".trim(),
                    color = TextSecondary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

// ─── Шаги ───

@Composable
private fun StepsList(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        recipe.steps.forEachIndexed { index, step ->
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Background,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (!step.photoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(225.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                AsyncImage(
                                    model = step.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        Text(
                            text = step.text,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }

                if (index < recipe.steps.size - 1) {
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 38.dp)
                            .height(1.dp)
                            .background(DividerColor.copy(alpha = 0.3f)),
                    )
                }
            }
        }
    }
}

// ─── Музыка ───

@Composable
private fun MusicBlock(recipe: Recipe) {
    val uriHandler = LocalUriHandler.current

    Column {
        if (recipe.musicNote.isNotBlank()) {
            Text(
                text = recipe.musicNote,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontStyle = FontStyle.Italic,
            )
            Spacer(Modifier.height(12.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recipe.musicTracks.forEach { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                        .clickable {
                            if (track.url.isNotBlank()) {
                                uriHandler.openUri(track.url)
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Превью плейлиста (если есть) — иначе иконка ноты
                    if (track.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = track.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = "🎵", fontSize = 22.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title.ifBlank { "Трек" },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val source = com.appa.recipeapp.utils.MusicSourceDetector.detect(track.url)
                        Text(
                            text = "${source.displayName} →",
                            color = Accent,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

// ─── Критика ───

@Composable
private fun ReviewsBlock(
    ratings: List<Rating>,
    draftStars: Int,
    draftComment: String,
    onStarsChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column {
        Text(
            text = "Твоя оценка",
            color = TextSecondary,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(5) { index ->
                val starIndex = index + 1
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (starIndex <= draftStars) Accent
                    else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            onStarsChange(starIndex)
                        },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(14.dp),
        ) {
            if (draftComment.isEmpty()) {
                Text(
                    text = "Оставь свой комментарий...",
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                )
            }
            BasicTextField(
                value = draftComment,
                onValueChange = onCommentChange,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(10.dp))

        val canSubmit = draftStars > 0

        Box(
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(10.dp))
                .background(if (canSubmit) Accent else SurfaceVariant)
                .clickable(enabled = canSubmit) { onSubmit() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Сохранить",
                color = if (canSubmit) Background else TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Отзывы",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))

        if (ratings.isEmpty()) {
            Text(
                text = "Пока никто не оценил",
                color = TextSecondary,
                fontSize = 13.sp,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ratings.forEach { rating ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = rating.userName.ifBlank { "Гость" },
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "★".repeat(rating.stars),
                                    color = Accent,
                                    fontSize = 11.sp,
                                )
                            }
                            if (rating.comment.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = rating.comment,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// ВСПОМОГАТЕЛЬНЫЕ
// ═══════════════════════════════════════════

@Composable
private fun CircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .size(width = 1.dp, height = 14.dp)
            .background(TextSecondary.copy(alpha = 0.4f)),
    )
}

private fun formatRating(recipe: Recipe): String {
    if (recipe.ratingsCount == 0) return "—"
    val rounded = (recipe.averageRating * 10).toInt() / 10.0
    return "$rounded"
}