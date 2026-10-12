package com.appa.recipeapp.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun saveBytesToCache(bytes: ByteArray): String = withContext(Dispatchers.IO) {
    val context = appContext ?: error("appContext не инициализирован")
    val file = File(context.cacheDir, "picked_${System.currentTimeMillis()}.jpg")
    file.writeBytes(bytes)
    file.absolutePath
}