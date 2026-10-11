package com.appa.recipeapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: Routes,
    val label: String,
    val icon: ImageVector,
)

/**
 * Табы нижнего бара. "Добавить" здесь НЕ входит — это отдельная кнопка,
 * которая открывает полноэкранный Editor.
 */
val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(Routes.Home, "Главная", Icons.Default.Home),
    BottomNavItem(Routes.Favorites, "Избранное", Icons.Default.Favorite),
    BottomNavItem(Routes.Profile, "Профиль", Icons.Default.Person),
)