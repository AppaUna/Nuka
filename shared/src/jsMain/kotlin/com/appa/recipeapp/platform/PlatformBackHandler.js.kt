package com.appa.recipeapp.platform

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // На Web системной кнопки "Назад" в интерфейсе нет, оставляем пустым
}