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
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

@Composable
fun CropScreen(
    photoFile: PlatformFile,
    onConfirm: (ByteArray) -> Unit,
    onCancel: () -> Unit,
) {
    val imageCropper = rememberImageCropper()
    var sourceBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    // Исходные байты — нужны для fallback (если кроп отработал, но не удалось закодировать в JPEG)
    var originalBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Загружаем изображение
    LaunchedEffect(photoFile) {
        println("DEBUG CropScreen: старт обработки фото")

        // Читаем байты сразу — пригодятся и для загрузки, и для fallback
        val bytes = try {
            photoFile.readBytes().also { originalBytes = it }
        } catch (e: Throwable) {
            println("DEBUG CropScreen readBytes EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            onCancel()
            return@LaunchedEffect
        }

        val bitmap = loadImageBitmap(photoFile)
        println("DEBUG CropScreen: loadImageBitmap вернул ${if (bitmap == null) "null" else "bitmap"}")

        if (bitmap == null) {
            // Web: loadImageBitmap — заглушка (Krop не работает).
            // Читаем байты напрямую — фото сохранится БЕЗ обрезки 4:5.
            println("DEBUG CropScreen: bitmap null → передаём ${bytes.size} байт без кропа")
            onConfirm(bytes)
            return@LaunchedEffect
        }
        sourceBitmap = bitmap
    }

    // Когда изображение загружено — запускаем кроп
    LaunchedEffect(sourceBitmap) {
        val bmp = sourceBitmap ?: return@LaunchedEffect
        println("DEBUG CropScreen: запускаем Krop")
        val result = imageCropper.crop(bmp)
        when (result) {
            is CropResult.Success -> {
                val bytes = imageBitmapToJpegBytes(result.bitmap)
                if (bytes != null) {
                    println("DEBUG CropScreen: кроп готов, ${bytes.size} байт → onConfirm")
                    onConfirm(bytes)
                } else {
                    // Fallback: не удалось закодировать в JPEG — отдаём исходные байты
                    val fallback = originalBytes
                    if (fallback != null) {
                        println("DEBUG CropScreen: fallback — передаём исходные ${fallback.size} байт")
                        onConfirm(fallback)
                    } else {
                        println("DEBUG CropScreen: fallback невозможен — отмена")
                        onCancel()
                    }
                }
            }
            CropResult.Cancelled -> {
                println("DEBUG CropScreen: пользователь отменил кроп")
                onCancel()
            }
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