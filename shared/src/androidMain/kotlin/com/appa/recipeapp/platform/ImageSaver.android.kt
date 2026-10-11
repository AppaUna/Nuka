package com.appa.recipeapp.platform

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Контекст приложения. Инициализируется через [initImageSaver] из MainActivity.
 * Не используем lateinit — это ломает сборку на iOS.
 */
private var appContext: Context? = null

/**
 * Вызывается один раз из MainActivity.onCreate().
 */
fun initImageSaver(context: Context) {
    appContext = context.applicationContext
}

actual suspend fun saveImageToFile(image: ImageBitmap): String = withContext(Dispatchers.IO) {
    val context = appContext
        ?: error("initImageSaver(context) не был вызван. Проверьте MainActivity.")
    val bitmap: Bitmap = image.asAndroidBitmap()
    val file = File(context.filesDir, "cropped_${UUID.randomUUID()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    file.absolutePath
}