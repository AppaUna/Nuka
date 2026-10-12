package com.appa.recipeapp.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import com.appa.recipeapp.platform.imageBitmapToJpegBytes
import com.appa.recipeapp.platform.loadImageBitmap
import com.appa.recipeapp.ui.theme.Accent
import com.appa.recipeapp.ui.theme.Background
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.PlatformFile
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.cropperStyle

@Composable
fun CropScreen(
    photoFile: PlatformFile,
    onConfirm: (ByteArray) -> Unit,
    onCancel: () -> Unit,
) {
    val imageCropper = rememberImageCropper()
    var sourceBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Загружаем изображение
    LaunchedEffect(photoFile) {
        val bitmap = loadImageBitmap(photoFile)
        if (bitmap == null) {
            println("DEBUG CropScreen: не удалось загрузить фото")
            onCancel()
            return@LaunchedEffect
        }
        sourceBitmap = bitmap
    }

    // Когда изображение загружено — запускаем кроп
    LaunchedEffect(sourceBitmap) {
        val bmp = sourceBitmap ?: return@LaunchedEffect
        val result = imageCropper.crop(bmp)
        when (result) {
            is CropResult.Success -> {
                val bytes = imageBitmapToJpegBytes(result.bitmap)
                if (bytes != null) {
                    onConfirm(bytes)
                } else {
                    println("DEBUG CropScreen: не удалось конвертировать в байты")
                    onCancel()
                }
            }
            CropResult.Cancelled -> onCancel()
            CropError.LoadingError, CropError.SavingError -> {
                println("DEBUG CropScreen: ошибка $result")
                onCancel()
            }
        }
    }

    // Показываем диалог кропа, если он активен
    imageCropper.cropState?.let { cropState ->
        ImageCropperDialog(
            state = cropState,
            style = cropperStyle(
                aspects = listOf(AspectRatio(4, 5)),
            )
        )
    }

    // Пока грузится — показываем спиннер
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Accent)
    }
}