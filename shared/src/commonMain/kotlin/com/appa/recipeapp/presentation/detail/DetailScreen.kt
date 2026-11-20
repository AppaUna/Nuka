package com.appa.recipeapp.presentation.detail

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.appa.recipeapp.data.model.Comment
import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.platform.openExternalUrl
import com.appa.recipeapp.ui.components.AuthorAvatar
import com.appa.recipeapp.ui.components.InteractiveSnowfallLayer
import com.appa.recipeapp.ui.components.PlaceholderImage
import com.appa.recipeapp.ui.components.SkeletonDetailContent
import com.appa.recipeapp.ui.components.WinterCardStyle
import com.appa.recipeapp.ui.components.WinterConservatoryBackdrop
import com.appa.recipeapp.ui.components.arcticGlassCard
import com.appa.recipeapp.ui.components.festiveTapBurst
import com.appa.recipeapp.ui.components.observeSnowPointer
import com.appa.recipeapp.ui.components.rememberSnowSimulationState
import com.appa.recipeapp.ui.components.winterRecipeCard
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.DividerColor
import com.appa.recipeapp.ui.theme.Error
import com.appa.recipeapp.ui.theme.NewYearTheme
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.ic_calendar
import recipeapp.shared.generated.resources.ic_timer
import recipeapp.shared.generated.resources.nuka_cap
import kotlin.math.PI

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isNewYear = MaterialTheme.colorScheme.primary == NewYearTheme.colors.accent

    val listState = rememberLazyListState()
    val scrollParallax by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex * 28f + listState.firstVisibleItemScrollOffset * 0.10f
        }
    }
    val snowState = rememberSnowSimulationState(flakeCount = 28)
    val winterTransition = rememberInfiniteTransition(label = "winter_detail_loop")
    val lightPhase by winterTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "detail_light_phase",
    )

    when {
        state.isLoading -> {
            SkeletonDetailContent()
        }
        state.recipe == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isNewYear) Color(0xFF060F1B) else Background),
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isNewYear) {
                            Modifier
                                .background(Color(0xFF060F1B))
                                .observeSnowPointer(snowState = snowState, enabled = true)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                if (isNewYear) {
                    WinterConservatoryBackdrop(
                        scrollParallax = scrollParallax,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Снежинки плавно появляются ниже блока названия и описания, не перекрывая фото блюда
                    InteractiveSnowfallLayer(
                        snowState = snowState,
                        topFadeStartFraction = 0.44f,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    item {
                        TopPhotoBlock(
                            recipe = recipe,
                            isMyRecipe = state.isMyRecipe,
                            isNewYear = isNewYear,
                            lightPhase = lightPhase,
                            onBackClick = onBackClick,
                            onEditClick = { onEditClick(recipe.id) },
                            onFavoriteClick = { viewModel.toggleFavorite() },
                        )
                    }

                    item {
                        TitleCard(
                            recipe = recipe,
                            isNewYear = isNewYear,
                            lightPhase = lightPhase,
                        )
                    }

                    item {
                        SectionBlock(
                            title = "Ингредиенты",
                            isNewYear = isNewYear,
                        ) {
                            IngredientsList(recipe = recipe, isNewYear = isNewYear)
                        }
                    }

                    item {
                        SectionBlock(
                            title = "Приготовление",
                            isNewYear = isNewYear,
                        ) {
                            StepsList(
                                recipe = recipe,
                                isNewYear = isNewYear,
                                lightPhase = lightPhase,
                            )
                        }
                    }

                    item {
                        if (recipe.musicNote.isNotBlank() || recipe.musicTracks.isNotEmpty()) {
                            SectionBlock(
                                title = "Музыка для готовки",
                                isNewYear = isNewYear,
                            ) {
                                MusicBlock(
                                    recipe = recipe,
                                    isNewYear = isNewYear,
                                    lightPhase = lightPhase,
                                )
                            }
                        } else {
                            Spacer(Modifier.height(0.dp))
                        }
                    }

                    if (recipe.notes.isNotBlank()) {
                        item {
                            SectionBlock(
                                title = "Заметки",
                                isNewYear = isNewYear,
                            ) {
                                Text(
                                    text = recipe.notes,
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    }

                    item {
                        SectionBlock(
                            title = "Оценки",
                            showDivider = true,
                            isNewYear = isNewYear,
                        ) {
                            RatingsBlock(
                                ratings = state.ratings,
                                draftStars = state.draftStars,
                                isNewYear = isNewYear,
                                onStarsChange = viewModel::onStarsChange,
                                onSubmit = viewModel::submitRating,
                                onDeleteMyRating = viewModel::deleteMyRating,
                            )
                        }
                    }

                    item {
                        SectionBlock(
                            title = "Комментарии",
                            showDivider = false,
                            isNewYear = isNewYear,
                        ) {
                            CommentsBlock(
                                comments = state.comments,
                                draftComment = state.draftComment,
                                isNewYear = isNewYear,
                                onCommentChange = viewModel::onCommentChange,
                                onSubmit = viewModel::submitComment,
                                currentUserId = state.currentUserID,
                                onDeleteComment = viewModel::deleteComment,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopPhotoBlock(
    recipe: Recipe,
    isMyRecipe: Boolean,
    isNewYear: Boolean = false,
    lightPhase: Float = 0f,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    val activeAccent = if (isNewYear) NewYearTheme.colors.accent else Accent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
    ) {
        val detailPhoto = recipe.photoUrls.firstOrNull()
        if (!detailPhoto.isNullOrBlank()) {
            AsyncImage(
                model = detailPhoto,
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

        // Лёгкий плавный переход самой нижней кромки фото в фон карточки названия
        if (isNewYear) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF060F1B).copy(alpha = 0.75f),
                            ),
                        ),
                    ),
            )
        }

        CircleButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            isNewYear = isNewYear,
            lightPhase = lightPhase,
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
            if (isMyRecipe) {
                CircleButton(
                    isNewYear = isNewYear,
                    lightPhase = lightPhase,
                    onClick = onEditClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            CircleButton(
                isNewYear = isNewYear,
                lightPhase = lightPhase,
                isHighlighted = isNewYear && recipe.isFavorite,
                onClick = onFavoriteClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "В избранное",
                    tint = if (recipe.isFavorite) activeAccent else TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TitleCard(
    recipe: Recipe,
    isNewYear: Boolean = false,
    lightPhase: Float = 0f,
) {
    val activeAccent = if (isNewYear) NewYearTheme.colors.accent else Accent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-32).dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .then(
                if (isNewYear) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A1930),
                                Color(0xFF071222).copy(alpha = 0.94f),
                                Color.Transparent,
                            ),
                        ),
                    )
                } else {
                    Modifier.background(Background)
                },
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        if (isNewYear) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF67E8F9).copy(alpha = 0.45f),
                                Color(0xFF818CF8).copy(alpha = 0.35f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        Text(
            text = recipe.title,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.nuka_cap),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatRating(recipe),
                color = if (isNewYear) activeAccent else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

            MetricDivider()

            AuthorAvatar(
                avatarUrl = recipe.authorAvatarUrl,
                size = 16.dp,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = recipe.authorName,
                color = TextSecondary,
                fontSize = 13.sp,
            )

            MetricDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_timer),
                    contentDescription = null,
                    tint = if (isNewYear) Color(0xFF67E8F9).copy(alpha = 0.85f) else TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${recipe.cookingTimeMinutes} мин",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }

            MetricDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_calendar),
                    contentDescription = null,
                    tint = if (isNewYear) Color(0xFF67E8F9).copy(alpha = 0.85f) else TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = formatDate(recipe.createdAt),
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
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
                            .background(
                                if (isNewYear) Color(0xFF0E213A).copy(alpha = 0.85f)
                                else Surface,
                            )
                            .arcticGlassCard(
                                enabled = isNewYear,
                                cornerRadius = 8.dp,
                                highlightPhase = lightPhase,
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = tag,
                            color = if (isNewYear) Color(0xFFBAE6FD) else TextSecondary,
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

@Composable
private fun SectionBlock(
    title: String,
    showDivider: Boolean = true,
    isNewYear: Boolean = false,
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
            if (isNewYear) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF38BDF8).copy(alpha = 0.24f),
                                    Color(0xFF818CF8).copy(alpha = 0.28f),
                                    Color(0xFF38BDF8).copy(alpha = 0.24f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DividerColor.copy(alpha = 0.5f)),
                )
            }
        }
    }
}

@Composable
private fun IngredientsList(
    recipe: Recipe,
    isNewYear: Boolean = false,
) {
    val dotColor = if (isNewYear) Color(0xFF67E8F9).copy(alpha = 0.35f) else Color(0xFF9E9E9E).copy(alpha = 0.4f)
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
                                    color = dotColor,
                                    radius = dotRadius,
                                    center = Offset(x + dotRadius, y),
                                )
                                x += spacing
                            }
                        },
                )
                Text(
                    text = "${ingredient.amount} ${ingredient.unit}".trim(),
                    color = if (isNewYear) Color(0xFFBAE6FD) else TextSecondary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun StepsList(
    recipe: Recipe,
    isNewYear: Boolean = false,
    lightPhase: Float = 0f,
) {
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
                            .then(
                                if (isNewYear) {
                                    Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF67E8F9),
                                                Color(0xFF818CF8),
                                            ),
                                        ),
                                    )
                                } else {
                                    Modifier.background(Accent)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (isNewYear) Color(0xFF060F1B) else Background,
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .winterRecipeCard(
                                        enabled = isNewYear,
                                        style = WinterCardStyle.Medium,
                                        cornerRadius = 12.dp,
                                        highlightPhase = lightPhase,
                                    ),
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
                            .background(
                                if (isNewYear) Color(0xFF38BDF8).copy(alpha = 0.16f)
                                else DividerColor.copy(alpha = 0.3f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicBlock(
    recipe: Recipe,
    isNewYear: Boolean = false,
    lightPhase: Float = 0f,
) {
    val activeAccent = if (isNewYear) NewYearTheme.colors.accent else Accent
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
                        .festiveTapBurst(enabled = isNewYear, cornerRadius = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isNewYear) Color(0xFF0B1A2E).copy(alpha = 0.82f)
                            else Surface,
                        )
                        .arcticGlassCard(
                            enabled = isNewYear,
                            cornerRadius = 12.dp,
                            highlightPhase = lightPhase,
                        )
                        .clickable {
                            if (track.url.isNotBlank()) {
                                openExternalUrl(track.url)
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                                .background(
                                    if (isNewYear) Color(0xFF132942) else SurfaceVariant,
                                ),
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
                            color = activeAccent,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingsBlock(
    ratings: List<Rating>,
    draftStars: Int,
    isNewYear: Boolean = false,
    onStarsChange: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDeleteMyRating: () -> Unit,
) {
    val activeAccent = if (isNewYear) NewYearTheme.colors.accent else Accent
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(5) { index ->
                val starIndex = index + 1
                val isSelected = starIndex <= draftStars

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onStarsChange(starIndex) })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.nuka_cap),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = if (isSelected) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    )
                }
            }
        }

        if (draftStars > 0) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .festiveTapBurst(enabled = isNewYear, cornerRadius = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeAccent.copy(alpha = 0.2f))
                        .clickable { onSubmit() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Оценить", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDeleteMyRating() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Удалить", color = Error, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (ratings.isEmpty()) {
            Text("Пока никто не оценил", color = TextSecondary, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ratings.forEach { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AuthorAvatar(avatarUrl = rating.userAvatarUrl, size = 48.dp)

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = rating.userName,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(Modifier.width(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(rating.stars) {
                                Image(
                                    painter = painterResource(Res.drawable.nuka_cap),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsBlock(
    comments: List<Comment>,
    draftComment: String,
    isNewYear: Boolean = false,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    currentUserId: String,
    onDeleteComment: (String) -> Unit,
) {
    val activeAccent = if (isNewYear) NewYearTheme.colors.accent else Accent
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isNewYear) Color(0xFF0B1A2E).copy(alpha = 0.82f)
                    else Surface,
                )
                .then(
                    if (isNewYear) {
                        Modifier.border(
                            width = 1.dp,
                            color = Color(0xFF38BDF8).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(12.dp),
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(14.dp),
        ) {
            if (draftComment.isEmpty()) {
                Text("Оставь свой комментарий...", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp)
            }
            BasicTextField(
                value = draftComment,
                onValueChange = onCommentChange,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(activeAccent),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .align(Alignment.End)
                .festiveTapBurst(enabled = isNewYear && draftComment.isNotBlank(), cornerRadius = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (draftComment.isNotBlank()) activeAccent
                    else if (isNewYear) Color(0xFF132942) else SurfaceVariant,
                )
                .clickable(enabled = draftComment.isNotBlank()) { onSubmit() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Отправить",
                color = if (draftComment.isNotBlank()) {
                    if (isNewYear) Color(0xFF060F1B) else Background
                } else {
                    TextSecondary
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(24.dp))

        if (comments.isEmpty()) {
            Text("Пока нет комментариев", color = TextSecondary, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                comments.forEach { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AuthorAvatar(avatarUrl = comment.userAvatarUrl, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comment.userName.ifBlank { "Гость" }, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(comment.text, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                        }

                        if (comment.userId == currentUserId) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteComment(comment.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Удалить",
                                    tint = Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleButton(
    modifier: Modifier = Modifier,
    isNewYear: Boolean = false,
    lightPhase: Float = 0f,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .festiveTapBurst(enabled = isNewYear, cornerRadius = 22.dp)
            .clip(CircleShape)
            .then(
                if (isNewYear) {
                    Modifier
                        .background(Color(0xFF060F1B).copy(alpha = 0.62f))
                        .arcticGlassCard(
                            enabled = true,
                            cornerRadius = 22.dp,
                            highlightPhase = lightPhase,
                            auroraAccent = isHighlighted,
                        )
                } else {
                    Modifier
                },
            )
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

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val instant = kotlin.time.Instant.fromEpochMilliseconds(timestamp)
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val month = dt.monthNumber.toString().padStart(2, '0')
    val year = dt.year
    return "$day.$month.$year"
}