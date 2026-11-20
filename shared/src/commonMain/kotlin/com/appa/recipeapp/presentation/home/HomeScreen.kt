package com.appa.recipeapp.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.data.QuoteRepository
import com.appa.recipeapp.data.WadsworthRepository
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.ui.components.InteractiveSnowfallLayer
import com.appa.recipeapp.ui.components.QuoteCard
import com.appa.recipeapp.ui.components.RecipeCardLarge
import com.appa.recipeapp.ui.components.RecipeCardList
import com.appa.recipeapp.ui.components.RecipeCardMedium
import com.appa.recipeapp.ui.components.ScreenBottomSnowdrift
import com.appa.recipeapp.ui.components.ScreenHeader
import com.appa.recipeapp.ui.components.SectionHeader
import com.appa.recipeapp.ui.components.SkeletonBox
import com.appa.recipeapp.ui.components.SkeletonRecipeCardLarge
import com.appa.recipeapp.ui.components.SkeletonRecipeCardMedium
import com.appa.recipeapp.ui.components.WadsworthDialog
import com.appa.recipeapp.ui.components.WinterCardStyle
import com.appa.recipeapp.ui.components.WinterConservatoryBackdrop
import com.appa.recipeapp.ui.components.arcticGlassCard
import com.appa.recipeapp.ui.components.festiveTapBurst
import com.appa.recipeapp.ui.components.observeSnowPointer
import com.appa.recipeapp.ui.components.rememberSnowSimulationState
import com.appa.recipeapp.ui.components.winterRecipeCard
import com.appa.recipeapp.ui.theme.NewYearTheme
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import com.appa.recipeapp.utils.greetingForNow
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.gusteau
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val DEFAULT_TAGS = listOf(
    "завтрак", "обед", "ужин", "суп", "салат",
    "десерт", "выпечка", "напитки", "закуска",
)

private val LARGE_CARD_HEIGHT = 275.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRecipeClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val accent = MaterialTheme.colorScheme.primary
    val isNewYear = accent == NewYearTheme.colors.accent

    var showWadsworth by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scrollParallax by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex * 28f + listState.firstVisibleItemScrollOffset * 0.10f
        }
    }

    val snowState = rememberSnowSimulationState(flakeCount = 34)

    val winterTransition = rememberInfiniteTransition(label = "winter_home_loop")
    val lightPhase by winterTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "winter_light_phase",
    )

    LaunchedEffect(Unit) {
        viewModel.refreshIfStale()
    }

    val isFiltering = state.query.isNotBlank() || state.selectedTags.isNotEmpty()

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

            InteractiveSnowfallLayer(
                snowState = snowState,
                topFadeStartFraction = 0.17f,
                modifier = Modifier.fillMaxSize(),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 138.dp),
        ) {
            item(contentType = "top_header_zone") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ScreenHeader(
                        title = "${greetingForNow()}, ${state.greetingName}",
                        subtitle = "Что приготовим сегодня?",
                        onLogoClick = { showWadsworth = true },
                    )
                    SearchBar(
                        query = state.query,
                        onQueryChange = viewModel::onSearchChange,
                        isNewYear = isNewYear,
                        lightPhase = lightPhase,
                    )
                    TagFiltersRow(
                        allTags = mergedTags(state.allTags),
                        selectedTags = state.selectedTags,
                        onTagClick = viewModel::toggleTag,
                        onAllClick = viewModel::clearTags,
                        isNewYear = isNewYear,
                    )
                }
            }

            item(contentType = "divider") {
                WinterSectionDivider(isNewYear = isNewYear)
            }
            item(contentType = "section_header") {
                SectionHeader(title = "Рецепты")
            }
            item(contentType = "main_feed") {
                when {
                    state.mainFeed.isNotEmpty() -> {
                        Box(modifier = Modifier.height(LARGE_CARD_HEIGHT)) {
                            MainFeedRow(
                                mainFeed = state.mainFeed,
                                isLoadingMore = state.isMainFeedLoadingMore,
                                isEndReached = state.isMainFeedEndReached,
                                isFiltering = isFiltering,
                                isNewYear = isNewYear,
                                lightPhase = lightPhase,
                                onRecipeClick = onRecipeClick,
                                onLoadMore = { viewModel.loadMoreMainFeed() },
                            )
                        }
                    }
                    !state.isRecipesLoaded -> {
                        Box(modifier = Modifier.height(LARGE_CARD_HEIGHT)) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(count = 3) { SkeletonRecipeCardLarge() }
                            }
                        }
                    }
                    state.hasNetworkError -> {
                        TextPlaceholder(
                            text = "Проверь интернет-подключение",
                            color = accent,
                            fontSize = 15.sp,
                        )
                    }
                    isFiltering -> {
                        TextPlaceholder(text = "Такого мы пока не готовили...")
                    }
                    else -> {
                        TextPlaceholder(
                            text = "Пока ничего не добавлено",
                            color = TextSecondary,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            item(contentType = "quote") {
                QuoteCard(
                    quote = QuoteRepository.getToday(),
                    gusteauImage = Res.drawable.gusteau,
                )
            }

            item(contentType = "divider") {
                WinterSectionDivider(isNewYear = isNewYear)
            }
            item(contentType = "section_header") {
                SectionHeader(
                    title = "Завтрак",
                    onSeeAllClick = { viewModel.toggleBreakfastExpanded() },
                    isExpanded = state.isBreakfastExpanded,
                )
            }
            item(contentType = "section_strip") {
                when {
                    state.breakfast.isNotEmpty() -> {
                        val recipes = if (state.isBreakfastExpanded) {
                            state.breakfast
                        } else {
                            state.breakfast.take(5)
                        }
                        HorizontalRecipeStrip(
                            recipes = recipes,
                            isNewYear = isNewYear,
                            lightPhase = lightPhase,
                            onRecipeClick = onRecipeClick,
                        )
                    }
                    !state.isStaticSectionsLoaded -> HorizontalSkeletonStrip()
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder()
                }
            }

            item(contentType = "divider") {
                WinterSectionDivider(isNewYear = isNewYear)
            }
            item(contentType = "section_header") {
                SectionHeader(
                    title = "Обед",
                    onSeeAllClick = { viewModel.toggleLunchExpanded() },
                    isExpanded = state.isLunchExpanded,
                )
            }
            item(contentType = "section_strip") {
                when {
                    state.lunch.isNotEmpty() -> {
                        val recipes = if (state.isLunchExpanded) {
                            state.lunch
                        } else {
                            state.lunch.take(5)
                        }
                        HorizontalRecipeStrip(
                            recipes = recipes,
                            isNewYear = isNewYear,
                            lightPhase = lightPhase,
                            onRecipeClick = onRecipeClick,
                        )
                    }
                    !state.isStaticSectionsLoaded -> HorizontalSkeletonStrip()
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder()
                }
            }

            item(contentType = "divider") {
                WinterSectionDivider(isNewYear = isNewYear)
            }
            item(contentType = "section_header") {
                SectionHeader(
                    title = "Ужин",
                    onSeeAllClick = { viewModel.toggleDinnerExpanded() },
                    isExpanded = state.isDinnerExpanded,
                )
            }
            item(contentType = "section_strip") {
                when {
                    state.dinner.isNotEmpty() -> {
                        val recipes = if (state.isDinnerExpanded) {
                            state.dinner
                        } else {
                            state.dinner.take(5)
                        }
                        HorizontalRecipeStrip(
                            recipes = recipes,
                            isNewYear = isNewYear,
                            lightPhase = lightPhase,
                            onRecipeClick = onRecipeClick,
                        )
                    }
                    !state.isStaticSectionsLoaded -> HorizontalSkeletonStrip()
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder()
                }
            }

            item(contentType = "divider") {
                WinterSectionDivider(isNewYear = isNewYear)
            }
            item(contentType = "section_header") { SectionHeader(title = "Популярное на этой неделе") }
            item(contentType = "popular") {
                when {
                    state.topRated.isNotEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            state.topRated.forEach { recipe ->
                                RecipeCardList(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(recipe.id) },
                                )
                            }
                        }
                    }
                    !state.isWeeklyLoaded -> {
                        Column {
                            repeat(3) {
                                SkeletonListCardRow()
                            }
                        }
                    }
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder(text = "Пока нет оценок")
                }
            }
        }

        if (isNewYear) {
            ScreenBottomSnowdrift(
                scrollParallax = scrollParallax,
                lightPhase = lightPhase,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(132.dp),
            )
        }

        if (showWadsworth) {
            val joke = WadsworthRepository.getCurrentJoke()
            WadsworthDialog(
                jokeText = joke.textEn,
                jokeAudioFile = joke.audioFile,
                onClose = { showWadsworth = false },
            )
        }
    }
}

@Composable
private fun WinterSectionDivider(
    isNewYear: Boolean,
) {
    if (!isNewYear) {
        Spacer(Modifier.height(20.dp))
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF38BDF8).copy(alpha = 0.22f),
                            Color(0xFF818CF8).copy(alpha = 0.28f),
                            Color(0xFF38BDF8).copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun MainFeedRow(
    mainFeed: List<Recipe>,
    isLoadingMore: Boolean,
    isEndReached: Boolean,
    isFiltering: Boolean,
    isNewYear: Boolean,
    lightPhase: Float,
    onRecipeClick: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, mainFeed.size, isEndReached) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val isScrolling = listState.isScrollInProgress
            Triple(lastVisible, totalItems, isScrolling)
        }.collect { (lastVisible, totalItems, isScrolling) ->
            if (!isScrolling && !isEndReached && !isLoadingMore && !isFiltering && totalItems > 0) {
                if (lastVisible >= totalItems - 3) {
                    onLoadMore()
                }
            }
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(mainFeed, key = { it.id }) { recipe ->
            val cardTransform by remember(listState, recipe.id, isNewYear) {
                derivedStateOf {
                    if (!isNewYear) return@derivedStateOf Triple(1f, 0f, false)
                    val info = listState.layoutInfo.visibleItemsInfo.find { it.key == recipe.id }
                    if (info == null) {
                        Triple(1f, 0f, false)
                    } else {
                        val viewportCenter = listState.layoutInfo.viewportEndOffset * 0.5f
                        val itemCenter = info.offset + info.size * 0.5f
                        val normDist = ((itemCenter - viewportCenter) / viewportCenter.coerceAtLeast(1f))
                            .coerceIn(-1f, 1f)
                        val scale = 1f - 0.032f * abs(normDist)
                        val isCenterFocus = abs(normDist) < 0.35f
                        Triple(scale, normDist, isCenterFocus)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = cardTransform.first
                        scaleY = cardTransform.first
                        rotationY = -cardTransform.second * 4.5f
                        cameraDistance = 14f * density
                    }
                    .winterRecipeCard(
                        enabled = isNewYear,
                        style = WinterCardStyle.Large,
                        cornerRadius = 16.dp,
                        highlightPhase = lightPhase + cardTransform.second,
                        isFocused = cardTransform.third,
                    ),
            ) {
                RecipeCardLarge(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                )
            }
        }

        if (isLoadingMore) {
            item(key = "loading_more") {
                SkeletonRecipeCardLarge()
            }
        }

        if (isEndReached && !isFiltering && mainFeed.isNotEmpty()) {
            item(key = "end_reached") {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(275.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Это всё",
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun TextPlaceholder(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HorizontalSkeletonStrip() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(count = 3) { SkeletonRecipeCardMedium() }
    }
}

@Composable
private fun SkeletonListCardRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(
            modifier = Modifier.size(80.dp),
            cornerRadius = 12.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp),
                cornerRadius = 4.dp,
            )
            Spacer(Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp),
                cornerRadius = 4.dp,
            )
        }
    }
}

private fun mergedTags(dynamic: List<String>): List<String> {
    val defaults = DEFAULT_TAGS
    val extras = dynamic.filter { it.lowercase() !in defaults.map { d -> d.lowercase() } }
    return defaults + extras
}

@Composable
private fun EmptySectionPlaceholder(text: String = "Пока ничего не добавлено") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun HorizontalRecipeStrip(
    recipes: List<Recipe>,
    isNewYear: Boolean,
    lightPhase: Float,
    onRecipeClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(recipes, key = { it.id }) { recipe ->
            Box(
                modifier = Modifier.winterRecipeCard(
                    enabled = isNewYear,
                    style = WinterCardStyle.Medium,
                    cornerRadius = 14.dp,
                    highlightPhase = lightPhase,
                ),
            ) {
                RecipeCardMedium(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) },
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isNewYear: Boolean = false,
    lightPhase: Float = 0f,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isNewYear) Color(0xFF0B1B30).copy(alpha = 0.90f) else Surface,
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isNewYear) Color(0xFF67E8F9) else TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(if (isNewYear) Color(0xFF67E8F9) else MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Поиск рецептов...",
                            color = TextSecondary,
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Очистить",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                )
            }
        }

        if (isNewYear) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@Canvas

                val corner = h * 0.5f
                val shimmer = 0.5f + 0.5f * sin(lightPhase)
                val sweepX = ((lightPhase / (2f * PI.toFloat())) * 1.6f - 0.3f) * w

                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF67E8F9).copy(alpha = 0.44f),
                            Color(0xFF818CF8).copy(alpha = 0.28f),
                            Color(0xFF38BDF8).copy(alpha = 0.34f),
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h),
                    ),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 1.4f),
                )

                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFE0F2FE).copy(alpha = 0.62f),
                            Color.Transparent,
                        ),
                        start = Offset(sweepX - 80f, 0f),
                        end = Offset(sweepX + 80f, 0f),
                    ),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 1.6f),
                )

                val iconCenter = Offset(26.dp.toPx(), h * 0.5f)
                val haloRadius = 18.dp.toPx().coerceAtLeast(1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF67E8F9).copy(alpha = 0.18f + 0.08f * shimmer),
                            Color.Transparent,
                        ),
                        center = iconCenter,
                        radius = haloRadius,
                    ),
                    radius = haloRadius,
                    center = iconCenter,
                )
            }
        }
    }
}

@Composable
private fun TagFiltersRow(
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    onAllClick: () -> Unit,
    isNewYear: Boolean = false,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChipCustom(
                label = "Все",
                selected = selectedTags.isEmpty(),
                isNewYear = isNewYear,
                onClick = onAllClick,
            )
        }
        items(allTags, key = { it }) { tag ->
            FilterChipCustom(
                label = tag.replaceFirstChar { it.uppercase() },
                selected = tag in selectedTags,
                isNewYear = isNewYear,
                onClick = { onTagClick(tag) },
            )
        }
    }
}

@Composable
private fun FilterChipCustom(
    label: String,
    selected: Boolean,
    isNewYear: Boolean = false,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .festiveTapBurst(enabled = isNewYear, cornerRadius = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                when {
                    selected -> accent
                    isNewYear -> Color(0xFF0C1C2E).copy(alpha = 0.88f)
                    else -> SurfaceVariant
                },
            )
            .arcticGlassCard(
                enabled = isNewYear && !selected,
                cornerRadius = 20.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.background else TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}