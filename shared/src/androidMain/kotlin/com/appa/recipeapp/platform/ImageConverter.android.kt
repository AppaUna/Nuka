package com.appa.recipeapp.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import java.io.ByteArrayOutputStream

actual suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap? {
    return try {
        val bytes = file.readBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Throwable) {
        null
    }
}

actual fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray? {
    return try {
        val stream = ByteArrayOutputStream()
        image.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.toByteArray()
    } catch (e: Throwable) {
        null
    }
}

actual fun imageBitmapToWebpBytes(image: ImageBitmap, quality: Int): ByteArray? {
    return try {
        val stream = ByteArrayOutputStream()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            image.asAndroidBitmap().compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
        } else {
            @Suppress("DEPRECATION")
            image.asAndroidBitmap().compress(Bitmap.CompressFormat.WEBP, quality, stream)
        }
        stream.toByteArray()
    } catch (e: Throwable) {
        null
    }
}

actual fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Throwable) {
        null
    }
}

actual suspend fun imageBitmapToJpegBytesAsync(image: ImageBitmap): ByteArray? {
    return imageBitmapToJpegBytes(image)
}

actual suspend fun imageBitmapToWebpBytesAsync(image: ImageBitmap, quality: Int): ByteArray? {
    return imageBitmapToWebpBytes(image, quality)
}