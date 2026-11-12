package com.appa.recipeapp.data.repository

import com.appa.recipeapp.ui.theme.*
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository(private val settings: Settings) {

    private val _currentTheme = MutableStateFlow(loadTheme())
    val currentTheme: StateFlow<NukaTheme> = _currentTheme.asStateFlow()

    fun setTheme(themeId: String) {
        // Пока что смена темы отключена, всегда используем DefaultTheme
        // if (themeId == HalloweenThemePlaceholder.id) return
        // val theme = getThemeById(themeId)
        // _currentTheme.value = theme
        // settings.putString(KEY_THEME, themeId)
    }

    private fun loadTheme(): NukaTheme {
        // Всегда возвращаем DefaultTheme
        return DefaultTheme
    }

    companion object {
        private const val KEY_THEME = "nuka_theme"
    }
}