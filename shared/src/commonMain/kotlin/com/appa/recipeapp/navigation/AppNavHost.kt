@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.appa.recipeapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.savedstate.read
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import com.appa.recipeapp.data.repository.UserPreferencesRepository
import com.appa.recipeapp.presentation.auth.AuthScreen
import com.appa.recipeapp.presentation.auth.AuthViewModel
import com.appa.recipeapp.presentation.detail.DetailScreen
import com.appa.recipeapp.presentation.detail.DetailViewModel
import com.appa.recipeapp.presentation.editor.EditorScreen
import com.appa.recipeapp.presentation.editor.EditorViewModel
import com.appa.recipeapp.presentation.favorites.FavoritesScreen
import com.appa.recipeapp.presentation.favorites.FavoritesViewModel
import com.appa.recipeapp.presentation.home.HomeScreen
import com.appa.recipeapp.presentation.home.HomeViewModel
import com.appa.recipeapp.presentation.profile.ProfileScreen
import com.appa.recipeapp.presentation.profile.ProfileViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun AppNavHost() {
    val authRepository: SupabaseAuthRepository = koinInject()
    val isLoggedIn = authRepository.isLoggedIn()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.Home.route,
        Routes.Favorites.route,
        Routes.Profile.route,
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route.route) {
                            popUpTo(navController.graph.findStartDestination().route ?: Routes.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCreateClick = {
                        navController.navigate(Routes.Editor.create())
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Routes.Home.route else Routes.Auth.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            // ─── Анимации ───
                enterTransition = {
                    fadeIn(animationSpec = tween(150))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(150))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(150))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(150))
                },
        ) {
            composable(Routes.Auth.route) {
                val viewModel = remember {
                    AuthViewModel(
                        authRepository = authRepository,
                        onAuthSuccess = {
                            navController.navigate(Routes.Home.route) {
                                popUpTo(Routes.Auth.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                AuthScreen(viewModel = viewModel)
            }

            // ─── Home — через Koin ───
            composable(Routes.Home.route) {
                val viewModel = koinViewModel<HomeViewModel>()
                HomeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { id ->
                        navController.navigate(Routes.Detail.create(id))
                    },
                )
            }

            // ─── Favorites — через Koin ───
            composable(Routes.Favorites.route) {
                val viewModel = koinViewModel<FavoritesViewModel>()
                FavoritesScreen(
                    viewModel = viewModel,
                    onRecipeClick = { id ->
                        navController.navigate(Routes.Detail.create(id))
                    },
                )
            }

            // ─── Profile — через remember (нужен onLogout) ───
            composable(Routes.Profile.route) {
                val repository: RecipeRepository = koinInject()
                val userPreferences: UserPreferencesRepository = koinInject()
                val viewModel = remember {
                    ProfileViewModel(
                        repository = repository,
                        authRepository = authRepository,
                        userPreferences = userPreferences,
                        onLogout = {
                            navController.navigate(Routes.Auth.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                ProfileScreen(
                    viewModel = viewModel,
                    onEditRecipe = { recipeId ->
                        navController.navigate(Routes.Editor.create(recipeId))
                    },
                    onRecipeClick = { recipeId ->
                        navController.navigate(Routes.Detail.create(recipeId))
                    },
                )
            }

            // ─── Detail — через remember (нужен recipeId) ───
            composable(
                route = Routes.Detail.route,
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.StringType },
                ),
            ) { entry ->
                val recipeId = entry
                    .arguments
                    ?.read { getStringOrNull("recipeId") }
                    ?: ""
                val repository: RecipeRepository = koinInject()
                val viewModel = remember(recipeId) {
                    DetailViewModel(
                        repository = repository,
                        authRepository = authRepository,
                        recipeId = recipeId,
                    )
                }
                DetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate(Routes.Editor.create(id))
                    },
                )
            }

            // ─── Editor — через remember (нужен onSaveSuccess) ───
            composable(
                route = Routes.Editor.route,
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                val recipeId: String? = entry
                    .arguments
                    ?.read { getStringOrNull("recipeId") }
                    ?.takeIf { it.isNotBlank() }

                val repository: RecipeRepository = koinInject()
                val viewModel = remember(recipeId) {
                    EditorViewModel(
                        repository = repository,
                        recipeId = recipeId,
                        onSaveSuccess = { newRecipeId ->
                            navController.navigate(Routes.Detail.create(newRecipeId)) {
                                popUpTo(Routes.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                EditorScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}