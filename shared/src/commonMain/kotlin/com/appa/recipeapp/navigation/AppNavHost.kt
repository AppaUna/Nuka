@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.appa.recipeapp.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import com.appa.recipeapp.data.repository.ThemeRepository
import com.appa.recipeapp.data.repository.UserPreferencesRepository
import com.appa.recipeapp.platform.PlatformBackHandler
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
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.NewYearTheme
import com.appa.recipeapp.ui.theme.TextPrimary
import com.appa.recipeapp.ui.theme.TextSecondary
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(initialUrl: String = "/") {
    val authRepository: SupabaseAuthRepository = koinInject()
    val settings: Settings = koinInject()
    val themeRepository: ThemeRepository = koinInject()
    val isNewYear = themeRepository.currentTheme.collectAsState().value.id == NewYearTheme.id

    var showDraftDialog by remember { mutableStateOf(false) }

    val isLoggedIn = authRepository.isLoggedIn()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(initialUrl) {
        if (initialUrl.startsWith("/detail/")) {
            val recipeId = initialUrl.removePrefix("/detail/")
            if (recipeId.isNotBlank()) {
                navController.navigate(Routes.Detail.create(recipeId))
            }
        }
    }

    val showBottomBar = currentRoute in listOf(
        Routes.Home.route,
        Routes.Favorites.route,
        Routes.Profile.route,
    )

    PlatformBackHandler(enabled = showDraftDialog) {
        showDraftDialog = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    isNewYear = isNewYear,
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
                        if (settings.getStringOrNull("draft_recipe") != null) {
                            showDraftDialog = true
                        } else {
                            navController.navigate(Routes.Editor.create())
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) Routes.Home.route else Routes.Auth.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding()),
                enterTransition = { fadeIn(animationSpec = tween(150)) },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(150)) },
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

                composable(Routes.Home.route) {
                    val viewModel = koinViewModel<HomeViewModel>()
                    HomeScreen(
                        viewModel = viewModel,
                        onRecipeClick = { id ->
                            navController.navigate(Routes.Detail.create(id))
                        },
                    )
                }

                composable(Routes.Favorites.route) {
                    val viewModel = koinViewModel<FavoritesViewModel>()
                    FavoritesScreen(
                        viewModel = viewModel,
                        onRecipeClick = { id ->
                            navController.navigate(Routes.Detail.create(id))
                        },
                    )
                }

                composable(Routes.Profile.route) {
                    val repository: RecipeRepository = koinInject()
                    val userPreferences: UserPreferencesRepository = koinInject()
                    val profileThemeRepository: ThemeRepository = koinInject()
                    val viewModel = remember {
                        ProfileViewModel(
                            repository = repository,
                            authRepository = authRepository,
                            userPreferences = userPreferences,
                            themeRepository = profileThemeRepository,
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

                composable(
                    route = Routes.Detail.route,
                    arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
                ) { entry ->
                    val recipeId = entry.arguments?.read { getStringOrNull("recipeId") } ?: ""
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
                            settings = settings,
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

    if (showDraftDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { showDraftDialog = false })
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Background)
                    .clickable(enabled = false) { },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "У тебя есть черновик",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Хочешь продолжить редактирование или начать новый рецепт?",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val inter1 = remember { MutableInteractionSource() }
                        val pressed1 by inter1.collectIsPressedAsState()
                        val scale1 by animateFloatAsState(if (pressed1) 0.95f else 1f, tween(100))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .graphicsLayer(scaleX = scale1, scaleY = scale1)
                                .clip(RoundedCornerShape(12.dp))
                                .border(0.7.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = inter1,
                                    indication = null,
                                ) {
                                    showDraftDialog = false
                                    settings.remove("draft_recipe")
                                    settings.remove("draft_time")
                                    navController.navigate(Routes.Editor.create())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Новый", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        val inter2 = remember { MutableInteractionSource() }
                        val pressed2 by inter2.collectIsPressedAsState()
                        val scale2 by animateFloatAsState(if (pressed2) 0.95f else 1f, tween(100))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .graphicsLayer(scaleX = scale2, scaleY = scale2)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Accent)
                                .clickable(
                                    interactionSource = inter2,
                                    indication = null,
                                ) {
                                    showDraftDialog = false
                                    navController.navigate(Routes.Editor.create())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Продолжить", color = Background, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}