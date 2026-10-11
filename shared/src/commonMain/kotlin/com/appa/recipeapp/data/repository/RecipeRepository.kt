package com.appa.recipeapp.data.repository

import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.data.model.Rating
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    /**
     * Главная лента. Если tags пустой — все рецепты в случайном порядке.
     * Если tags не пустой — рецепты, у которых есть хотя бы один из тегов, в случайном порядке.
     */
    fun getMainFeed(
        tags: Set<String> = emptySet(),
        query: String = "",
    ): Flow<List<Recipe>>

    /** Рецепты для конкретной секции (например, "завтрак"). Сортировка случайная. */
    fun getByTag(tag: String): Flow<List<Recipe>>

    /** Топ рецептов по среднему рейтингу (для "Популярное на этой неделе"). */
    fun getTopRated(limit: Int = 10): Flow<List<Recipe>>

    fun getRecipe(id: String): Flow<Recipe?>

    fun getUserRecipes(userId: String): Flow<List<Recipe>>

    fun getAllTags(): Flow<List<String>>

    /** Список избранных рецептов. */
    fun getFavorites(): Flow<List<Recipe>>

    /** Переключить избранное. */
    suspend fun toggleFavorite(recipeId: String)

    suspend fun addRecipe(recipe: Recipe): String
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: String)
    /**
     * Добавить или обновить оценку от текущего пользователя.
     * Если пользователь уже оценивал — старая запись заменяется.
     */
    suspend fun addOrUpdateRating(recipeId: String, rating: Rating)
}