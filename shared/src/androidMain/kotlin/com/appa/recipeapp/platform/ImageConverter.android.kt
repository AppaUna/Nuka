package com.appa.recipeapp.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap? = withContext(Dispatchers.IO) {
    try {
        val bytes = file.readBytes()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
        bitmap.asImageBitmap()
    } catch (e: Throwable) {
        println("DEBUG loadImageBitmap error: ${e.message}")
        null
    }
}

actual fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray? {
    return try {
        val bitmap = image.asAndroidBitmap()
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.toByteArray()
    } catch (e: Throwable) {
        println("DEBUG imageBitmapToJpegBytes error: ${e.message}")
        null
    }
}

actual fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Throwable) {
        println("DEBUG bytesToImageBitmap error: ${e.message}")
        null
    }
}