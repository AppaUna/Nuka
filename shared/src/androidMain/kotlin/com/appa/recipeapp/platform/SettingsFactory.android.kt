package com.appa.recipeapp.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private var settingsContext: Context? = null

fun initSettings(context: Context) {
    settingsContext = context.applicationContext
}

actual fun createSettings(): Settings {
    val context = settingsContext
        ?: error("initSettings(context) не был вызван. Проверьте MainActivity.")
    val prefs = context.getSharedPreferences("recipe_app_settings", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}