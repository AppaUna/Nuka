package com.appa.recipeapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import com.appa.recipeapp.data.repository.UserPreferencesRepository
import com.appa.recipeapp.navigation.AppNavHost
import com.appa.recipeapp.platform.createImageLoader
import com.appa.recipeapp.platform.createSettings
import com.appa.recipeapp.presentation.favorites.FavoritesViewModel
import com.appa.recipeapp.presentation.home.HomeViewModel
import com.appa.recipeapp.ui.theme.AppTheme
import com.appa.recipeapp.ui.theme.Background
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import recipeapp.shared.generated.resources.Res
import recipeapp.shared.generated.resources.bg_texture

private val appModule = module {
    single { SupabaseClient() }
    single { SupabaseAuthApi(get()) }
    single { SupabaseAuthRepository(authApi = get(), settings = createSettings()) }
    single { SupabaseRecipeApi(get()) }
    single { SupabaseStorageApi(get()) }
    single { UserPreferencesRepository(settings = createSettings()) }
    single<RecipeRepository> {
        SupabaseRecipeRepository(
            api = get(),
            storageApi = get(),
            authRepository = get(),
        )
    }

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
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        AppTheme {
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
                        .background(Background.copy(alpha = 0.88f)),
                )
                AppNavHost()
            }
        }
    }
}