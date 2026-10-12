package com.appa.recipeapp.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appa.recipeapp.ui.theme.Background
import com.appa.recipeapp.ui.theme.TextPrimary
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

@Composable
fun CropScreen(
    photoFile: PlatformFile,
    onConfirm: (ByteArray) -> Unit,
    onCancel: () -> Unit,
) {
    // ЗАГЛУШКА: кроп через Krop добавим позже
    LaunchedEffect(photoFile) {
        try {
            val bytes = photoFile.readBytes()
            onConfirm(bytes)
        } catch (e: Throwable) {
            println("DEBUG CropScreen: ошибка чтения — ${e.message}")
            onCancel()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Обработка фото...", color = TextPrimary)
    }
}