package com.appa.recipeapp.data.repository

import com.appa.recipeapp.ui.theme.CreamGardenTheme
import com.appa.recipeapp.ui.theme.DefaultTheme
import com.appa.recipeapp.ui.theme.FalloutTheme
import com.appa.recipeapp.ui.theme.NukaTheme
import com.appa.recipeapp.ui.theme.getThemeById
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository(private val settings: Settings) {

    private val _currentTheme = MutableStateFlow(loadTheme())
    val currentTheme: StateFlow<NukaTheme> = _currentTheme.asStateFlow()

    fun setTheme(themeId: String): Boolean {
        if (themeId == FalloutTheme.id || themeId == CreamGardenTheme.id) return false
        val theme = getThemeById(themeId)
        _currentTheme.value = theme
        settings.putString(KEY_THEME, theme.id)
        return true
    }

    private fun loadTheme(): NukaTheme {
        val savedId = settings.getStringOrNull(KEY_THEME) ?: return DefaultTheme
        if (savedId == FalloutTheme.id || savedId == CreamGardenTheme.id) return DefaultTheme
        return getThemeById(savedId)
    }

    companion object {
        private const val KEY_THEME = "nuka_theme"
    }
}