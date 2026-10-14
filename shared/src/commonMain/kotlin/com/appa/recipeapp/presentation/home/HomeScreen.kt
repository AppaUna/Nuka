package com.appa.recipeapp.presentation.home

import com.appa.recipeapp.ui.components.ScreenHeader
import com.appa.recipeapp.utils.greetingForNow
import com.appa.recipeapp.data.QuoteRepository
import com.appa.recipeapp.data.WadsworthRepository
import com.appa.recipeapp.ui.components.QuoteCard
import com.appa.recipeapp.ui.components.SkeletonRecipeCardLarge
import com.appa.recipeapp.ui.components.SkeletonRecipeCardMedium
import com.appa.recipeapp.ui.components.WadsworthDialog
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.gusteau
import recipeapp.shared.generated.resources.decor_popular
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.ui.components.RecipeCardLarge
import com.appa.recipeapp.ui.components.RecipeCardList
import com.appa.recipeapp.ui.components.RecipeCardMedium
import com.appa.recipeapp.ui.components.SectionHeader
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.Surface
import com.appa.recipeapp.ui.theme.SurfaceVariant
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource

private val DEFAULT_TAGS = listOf(
    "завтрак", "обед", "ужин", "суп", "салат",
    "десерт", "выпечка", "напитки", "закуска",
)

/** Высота большой карточки рецепта — используется для фиксации высоты LazyRow. */
private val LARGE_CARD_HEIGHT = 275.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRecipeClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showWadsworth by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshIfStale()
    }

    val isFiltering = state.query.isNotBlank() || state.selectedTags.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item(contentType = "header") {
                ScreenHeader(
                    title = "${greetingForNow()}, ${state.greetingName}",
                    subtitle = "Что приготовим сегодня?",
                    onLogoClick = { showWadsworth = true },
                )
            }

            item(contentType = "search") {
                SearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onSearchChange,
                )
            }

            item(contentType = "tag_filters") {
                TagFiltersRow(
                    allTags = mergedTags(state.allTags),
                    selectedTags = state.selectedTags,
                    onTagClick = viewModel::toggleTag,
                    onAllClick = viewModel::clearTags,
                )
            }

            // ─── Рецепты ───
            item(contentType = "divider") { SectionDivider() }
            item(contentType = "section_header") { SectionHeader(title = "Рецепты") }
            item(contentType = "main_feed") {
                when {
                    state.mainFeed.isNotEmpty() -> {
                        Box(modifier = Modifier.height(LARGE_CARD_HEIGHT)) {
                            MainFeedRow(
                                mainFeed = state.mainFeed,
                                isLoadingMore = state.isMainFeedLoadingMore,
                                isEndReached = state.isMainFeedEndReached,
                                isFiltering = isFiltering,
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
                            color = Accent,
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

            // ─── Завтрак ───
            item(contentType = "divider") { SectionDivider() }
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
                            onRecipeClick = onRecipeClick,
                        )
                    }
                    !state.isStaticSectionsLoaded -> HorizontalSkeletonStrip()
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder()
                }
            }

            // ─── Обед ───
            item(contentType = "divider") { SectionDivider() }
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
                            onRecipeClick = onRecipeClick,
                        )
                    }
                    !state.isStaticSectionsLoaded -> HorizontalSkeletonStrip()
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder()
                }
            }

            // ─── Ужин ───
            item(contentType = "divider") { SectionDivider() }
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
                            onRecipeClick = onRecipeClick,
                        )
                    }
                    !state.isStaticSectionsLoaded -> HorizontalSkeletonStrip()
                    state.hasNetworkError -> EmptySectionPlaceholder(text = "Нет соединения")
                    else -> EmptySectionPlaceholder()
                }
            }

            // ─── Популярное ───
            item(contentType = "divider") { SectionDivider() }
            item(contentType = "section_header") { SectionHeader(title = "Популярное на этой неделе") }
            item(contentType = "popular") {
                when {
                    state.topRated.isNotEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(520.dp),
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.decor_popular),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                alpha = 0.12f,
                                colorFilter = ColorFilter.colorMatrix(
                                    ColorMatrix().apply { setToSaturation(0f) }
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(280.dp)
                                    .offset(x = 60.dp, y = 60.dp),
                            )

                            // ─── Column вместо LazyColumn ───
                            // Вложенный LazyColumn перехватывал fling у родительского
                            // LazyColumn. Для 6 элементов LazyColumn не нужен — Column
                            // проще и не конфликтует с родительским скроллом.
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter),
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

// ═══════════════════════════════════════════
// MAIN FEED
// ═══════════════════════════════════════════

@Composable
private fun MainFeedRow(
    mainFeed: List<Recipe>,
    isLoadingMore: Boolean,
    isEndReached: Boolean,
    isFiltering: Boolean,
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
            // Загружаем только когда скролл ПОЛНОСТЬЮ остановился.
            // Иначе onLoadMore добавляет элементы во время fling → fling сбрасывается.
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
            RecipeCardLarge(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) },
            )
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

// ─── Вспомогательные ───

@Composable
private fun TextPlaceholder(
    text: String,
    color: androidx.compose.ui.graphics.Color = TextPrimary,
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
        com.appa.recipeapp.ui.components.SkeletonBox(
            modifier = Modifier.size(80.dp),
            cornerRadius = 12.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            com.appa.recipeapp.ui.components.SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp),
                cornerRadius = 4.dp,
            )
            Spacer(Modifier.height(8.dp))
            com.appa.recipeapp.ui.components.SkeletonBox(
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
    onRecipeClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeCardMedium(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) },
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(Accent),
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
        }
    }
}

@Composable
private fun TagFiltersRow(
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagClick: (String) -> Unit,
    onAllClick: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChipCustom(
                label = "Все",
                selected = selectedTags.isEmpty(),
                onClick = onAllClick,
            )
        }
        items(allTags, key = { it }) { tag ->
            FilterChipCustom(
                label = tag.replaceFirstChar { it.uppercase() },
                selected = tag in selectedTags,
                onClick = { onTagClick(tag) },
            )
        }
    }
}

@Composable
private fun FilterChipCustom(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Accent else SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = if (selected) Background else TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(20.dp))
}