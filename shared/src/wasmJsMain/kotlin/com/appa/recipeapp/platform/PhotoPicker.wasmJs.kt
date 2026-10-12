package com.appa.recipeapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
actual fun rememberPhotoPicker(
    onPhotoPicked: (PlatformFile) -> Unit,
): PhotoPickerLauncher {
    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {
                MainScope().launch {
                    val file = FileKit.openFilePicker(type = FileKitType.Image)
                    file?.let { onPhotoPicked(it) }
                }
            }
        }
    }
}