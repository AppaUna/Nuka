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
import com.appa.recipeapp.platform.compressImageBytesAsync
import com.appa.recipeapp.platform.imageBitmapToJpegBytesAsync
import com.appa.recipeapp.platform.imageBitmapToWebpBytesAsync
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
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(photoFile) {
        val rawBytes = try {
            photoFile.readBytes()
        } catch (e: Throwable) {
            e.printStackTrace()
            onCancel()
            return@LaunchedEffect
        }

        val compressedBytes = compressImageBytesAsync(rawBytes, 1280, 80)
        val bitmapBytes = compressedBytes ?: rawBytes

        val bitmap = bytesToImageBitmap(bitmapBytes)
        if (bitmap == null) {
            onCancel()
            return@LaunchedEffect
        }

        sourceBitmap = bitmap
        isReady = true
    }

    LaunchedEffect(sourceBitmap, isReady) {
        val bmp = sourceBitmap ?: return@LaunchedEffect
        if (!isReady) return@LaunchedEffect

        val result = imageCropper.crop(bmp)
        when (result) {
            is CropResult.Success -> {
                // Кодируем кропнутый bitmap через Skia encodeToData (быстро, без зависаний)
                val webpBytes = imageBitmapToWebpBytesAsync(result.bitmap, 80)
                val finalBytes = webpBytes ?: imageBitmapToJpegBytesAsync(result.bitmap)

                if (finalBytes != null) {
                    onConfirm(finalBytes, null)
                } else {
                    onCancel()
                }
            }
            CropResult.Cancelled -> onCancel()
            is CropError -> onCancel()
        }
        sourceBitmap = null
    }

    if (isReady) {
        imageCropper.cropState?.let { cropState ->
            ImageCropperDialog(
                state = cropState,
                style = cropperStyle(
                    aspects = listOf(AspectRatio(aspectRatio.first, aspectRatio.second)),
                ),
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
    }
}