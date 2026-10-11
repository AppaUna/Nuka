package com.appa.recipeapp.navigation

sealed class Routes(val route: String) {
    data object Auth : Routes("auth")

    data object Home : Routes("home")
    data object Favorites : Routes("favorites")
    data object Profile : Routes("profile")

    data object Editor : Routes("editor?recipeId={recipeId}") {
        fun create(recipeId: String? = null): String =
            if (recipeId.isNullOrBlank()) "editor?recipeId="
            else "editor?recipeId=$recipeId"
    }

    data object Detail : Routes("detail/{recipeId}") {
        fun create(recipeId: String): String = "detail/$recipeId"
    }
}