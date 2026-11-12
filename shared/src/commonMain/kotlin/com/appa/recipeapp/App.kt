package com.appa.recipeapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import com.appa.recipeapp.data.remote.SupabaseAuthApi
import com.appa.recipeapp.data.remote.SupabaseClient
import com.appa.recipeapp.data.remote.SupabaseRecipeApi
import com.appa.recipeapp.data.remote.SupabaseStorageApi
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import com.appa.recipeapp.data.repository.SupabaseRecipeRepository
import com.appa.recipeapp.data.repository.ThemeRepository
import com.appa.recipeapp.data.repository.UserPreferencesRepository
import com.appa.recipeapp.navigation.AppNavHost
import com.appa.recipeapp.platform.createImageLoader
import com.appa.recipeapp.platform.createSettings
import com.appa.recipeapp.platform.saveUserIdToStorage
import com.appa.recipeapp.presentation.favorites.FavoritesViewModel
import com.appa.recipeapp.presentation.home.HomeViewModel
import com.appa.recipeapp.ui.theme.AppTheme
import com.russhwolf.settings.Settings
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.bg_texture

private val appModule = module {
    single<Settings> { createSettings() }

    single { SupabaseClient() }
    single { SupabaseAuthApi(get()) }
    single { SupabaseAuthRepository(authApi = get(), settings = get()) }
    single { SupabaseRecipeApi(get()) }
    single { SupabaseStorageApi(get()) }
    single { UserPreferencesRepository(settings = get()) }
    single { ThemeRepository(settings = get()) }

    single { SupabaseRecipeRepository(api = get(), storageApi = get(), authRepository = get()) }
    single<RecipeRepository> { get<SupabaseRecipeRepository>() }

    single<ImageLoader> { createImageLoader() }

    viewModel {
        HomeViewModel(
            repository = get(),
            authRepository = get(),
        )
    }

    viewModel {
        FavoritesViewModel(
            repository = get(),
        )
    }
}

@Composable
fun App(initialUrl: String = "/") {
    KoinApplication(application = { modules(appModule) }) {
        val themeRepository: ThemeRepository = koinInject()
        val currentTheme by themeRepository.currentTheme.collectAsState()
        val authRepository: SupabaseAuthRepository = koinInject()

        // Наблюдаем за сессией. Как только user_id появляется — сохраняем в localStorage.
        LaunchedEffect(Unit) {
            authRepository.session.collect { session ->
                val userId = session?.userId
                if (userId != null) {
                    saveUserIdToStorage(userId)
                }
            }
        }

        AppTheme(theme = currentTheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(Res.drawable.bg_texture),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentTheme.colors.background.copy(alpha = 0.88f)),
                )
                AppNavHost(initialUrl = initialUrl)
            }
        }
    }
}