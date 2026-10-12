package com.appa.recipeapp.platform

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/**
 * Возвращает функцию pickPhoto(), которую можно вызвать из UI.
 * При успешном выборе фото вызывает [onPhotoPicked] с PlatformFile.
 */
@Composable
expect fun rememberPhotoPicker(
    onPhotoPicked: (PlatformFile) -> Unit,
): PhotoPickerLauncher

interface PhotoPickerLauncher {
    fun launch()
}