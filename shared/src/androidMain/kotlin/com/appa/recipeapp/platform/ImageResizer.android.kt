package com.appa.recipeapp.platform

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun resizeImageBitmap(image: ImageBitmap, maxSize: Int): ImageBitmap {
    val originalBitmap = image.asAndroidBitmap()
    val width = originalBitmap.width
    val height = originalBitmap.height

    if (width <= maxSize && height <= maxSize) return image

    val ratio = width.toFloat() / height.toFloat()
    val (newWidth, newHeight) = if (ratio > 1) {
        maxSize to (maxSize / ratio).toInt()
    } else {
        (maxSize * ratio).toInt() to maxSize
    }

    val resized = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    return resized.asImageBitmap()
}