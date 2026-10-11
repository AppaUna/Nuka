package com.appa.recipeapp.presentation.detail

import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.model.Recipe

data class DetailState(
    val recipe: Recipe? = null,
    val ratings: List<Rating> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val draftStars: Int = 0,
    val draftComment: String = "",
    val isSubmitting: Boolean = false,
    val isMyRecipe: Boolean = false,
)