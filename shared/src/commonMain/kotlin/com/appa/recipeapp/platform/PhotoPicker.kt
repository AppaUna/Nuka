package com.appa.recipeapp.platform

import androidx.compose.runtime.Composable

/**
 * Возвращает функцию pickPhoto(), которую можно вызвать из UI.
 * При успешном выборе фото вызывает [onPhotoPicked] с URI/путём к файлу.
 */
@Composable
expect fun rememberPhotoPicker(
    onPhotoPicked: (String) -> Unit,
): PhotoPickerLauncher

/**
 * Обёртка над платформенным пикером фото.
 * На Android — PickVisualMedia.
 * На iOS — PHPickerViewController.
 */
interface PhotoPickerLauncher {
    fun launch()
}