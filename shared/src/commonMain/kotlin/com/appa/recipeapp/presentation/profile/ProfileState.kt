package com.appa.recipeapp.presentation.profile

import com.appa.recipeapp.data.model.Recipe

data class ProfileState(
    val login: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val myRecipes: List<Recipe> = emptyList(),
    val recipesCount: Int = 0,
    val ratingsCount: Int = 0,
    val favoritesCount: Int = 0,
    val isMyRecipesExpanded: Boolean = false,
    val isPersonalizationExpanded: Boolean = false,
    val selectedNameColor: String = "#F5A623",
    val isEditingName: Boolean = false,
    val nameDraft: String = "",
    val isLoading: Boolean = true,
)