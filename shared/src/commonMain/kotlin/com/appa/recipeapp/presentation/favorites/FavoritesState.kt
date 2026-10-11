package com.appa.recipeapp.presentation.favorites

import com.appa.recipeapp.data.model.Recipe

data class FavoritesState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
)