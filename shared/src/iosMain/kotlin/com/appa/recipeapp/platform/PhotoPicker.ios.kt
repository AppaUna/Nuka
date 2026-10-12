package com.appa.recipeapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberPhotoPicker(
    onPhotoPicked: (PlatformFile) -> Unit,
): PhotoPickerLauncher {
    return remember {
        object : PhotoPickerLauncher {
            override fun launch() {
                // TODO: нативный iOS (не критично — используем PWA)
            }
        }
    }
}