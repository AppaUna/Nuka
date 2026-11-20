package com.appa.recipeapp.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

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

actual fun compressImageBytes(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    var scale = 1
    while (options.outWidth / scale > maxSize || options.outHeight / scale > maxSize) {
        scale *= 2
    }

    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = scale }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

actual suspend fun compressImageBytesAsync(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? {
    return compressImageBytes(bytes, maxSize, quality)
}