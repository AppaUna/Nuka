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
import com.appa.recipeapp.platform.bytesToImageBitmap
import com.appa.recipeapp.platform.imageBitmapToJpegBytes
import com.appa.recipeapp.platform.imageBitmapToWebpBytes
import com.appa.recipeapp.platform.resizeImageBitmap
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
    onConfirm: (ByteArray, String?) -> Unit,
    onCancel: () -> Unit,
    aspectRatio: Pair<Int, Int> = 4 to 5,
) {
    val imageCropper = rememberImageCropper()
    var sourceBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var originalBytes by remember { mutableStateOf<ByteArray?>(null) }

    LaunchedEffect(photoFile) {
        val bytes = try {
            photoFile.readBytes().also { originalBytes = it }
        } catch (e: Throwable) {
            e.printStackTrace()
            onCancel()
            return@LaunchedEffect
        }

        val bitmap = bytesToImageBitmap(bytes)
        if (bitmap == null) {
            onConfirm(bytes, null)
            return@LaunchedEffect
        }
        sourceBitmap = bitmap
    }

    LaunchedEffect(sourceBitmap) {
        val bmp = sourceBitmap ?: return@LaunchedEffect
        val result = imageCropper.crop(bmp)
        when (result) {
            is CropResult.Success -> {
                val resizedBitmap = resizeImageBitmap(result.bitmap, 1920)
                val webpBytes = imageBitmapToWebpBytes(resizedBitmap, 80)
                val finalBytes = webpBytes ?: imageBitmapToJpegBytes(resizedBitmap)

                // --- ЛОГИ ---
                println("NUKA_DEBUG: Оригинал весил ${originalBytes?.size ?: 0} байт")
                println("NUKA_DEBUG: После WebP (90%) весит ${webpBytes?.size ?: 0} байт")
                println("NUKA_DEBUG: Итоговый размер (JPEG fallback) ${finalBytes?.size ?: 0} байт")
                // -----------

                if (finalBytes != null) {
                    onConfirm(finalBytes, null)
                } else {
                    val fallback = originalBytes
                    if (fallback != null) onConfirm(fallback, null) else onCancel()
                }
            }
            CropResult.Cancelled -> onCancel()
            CropError.LoadingError, CropError.SavingError -> onCancel()
        }
    }

    imageCropper.cropState?.let { cropState ->
        ImageCropperDialog(
            state = cropState,
            style = cropperStyle(
                aspects = listOf(AspectRatio(aspectRatio.first, aspectRatio.second)),
            )
        )
    }

    if (sourceBitmap == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
    }
}