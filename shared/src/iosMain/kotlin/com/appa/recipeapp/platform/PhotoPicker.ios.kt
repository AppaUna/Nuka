package com.appa.recipeapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/*
 * iOS-реализация PhotoPicker — заглушка.
 * Полная логика (PHPickerViewController) будет добавлена позже.
 */

@Composable
actual fun rememberPhotoPicker(
    onPhotoPicked: (String) -> Unit,
): PhotoPickerLauncher {
    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {
                // TODO: реализовать PHPicker на iOS
            }
        }
    }
}