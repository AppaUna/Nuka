package com.appa.recipeapp.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Локальные настройки пользователя (multiplatform-settings).
 * Хранит цвет имени, выбранный в профиле.
 */
class UserPreferencesRepository(
    private val settings: Settings,
) {
    companion object {
        private const val KEY_NAME_COLOR = "user_name_color"
        private const val DEFAULT_NAME_COLOR = "#F5A623"
    }

    private val _nameColor = MutableStateFlow(loadNameColor())
    val nameColor: StateFlow<String> = _nameColor.asStateFlow()

    fun setNameColor(hex: String) {
        settings.putString(KEY_NAME_COLOR, hex)
        _nameColor.value = hex
    }

    fun getNameColor(): String = _nameColor.value

    private fun loadNameColor(): String {
        return settings.getStringOrNull(KEY_NAME_COLOR) ?: DEFAULT_NAME_COLOR
    }
}