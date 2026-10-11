package com.appa.recipeapp.presentation.home

import com.appa.recipeapp.data.model.Recipe

data class HomeState(
    val greetingName: String = "",
    val query: String = "",
    val selectedTags: Set<String> = emptySet(),
    val allTags: List<String> = emptyList(),
    val mainFeed: List<Recipe> = emptyList(),
    val breakfast: List<Recipe> = emptyList(),
    val lunch: List<Recipe> = emptyList(),
    val dinner: List<Recipe> = emptyList(),
    val topRated: List<Recipe> = emptyList(),

    val isRecipesLoaded: Boolean = false,
    val isWeeklyLoaded: Boolean = false,
    val isStaticSectionsLoaded: Boolean = false,
    val hasNetworkError: Boolean = false,

    /** Раскрыта ли вторая лента "Завтрак". */
    val isBreakfastExpanded: Boolean = false,
    /** Раскрыта ли вторая лента "Обед". */
    val isLunchExpanded: Boolean = false,
    /** Раскрыта ли вторая лента "Ужин". */
    val isDinnerExpanded: Boolean = false,

    /** Идёт догрузка следующей страницы mainFeed. */
    val isMainFeedLoadingMore: Boolean = false,
    /** Все страницы mainFeed загружены. */
    val isMainFeedEndReached: Boolean = false,
) {
    val greeting: String
        get() = "Добрый день, $greetingName"
}