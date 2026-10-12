package com.appa.recipeapp.platform

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual fun createSettings(): Settings = StorageSettings()