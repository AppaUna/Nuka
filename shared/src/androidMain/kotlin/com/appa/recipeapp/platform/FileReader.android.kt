package com.appa.recipeapp.platform

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal var appContext: Context? = null

/**
 * Вызывается из MainActivity (или из Platform.android.kt).
 * Сохраняем context для доступа к contentResolver.
 */
fun initFileReader(context: Context) {
    appContext = context.applicationContext
}

actual suspend fun readFileBytes(uri: String): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val context = appContext ?: run {
            println("DEBUG readFileBytes: context не инициализирован")
            return@withContext null
        }

        // Если это file:// или /path — просто читаем через File
        if (!uri.startsWith("content://")) {
            val file = java.io.File(uri.removePrefix("file://"))
            if (file.exists()) return@withContext file.readBytes()
        }

        // content:// — через ContentResolver
        val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
            ?: return@withContext null

        inputStream.use { it.readBytes() }
    } catch (e: Throwable) {
        println("DEBUG readFileBytes EXCEPTION: ${e::class.simpleName} — ${e.message}")
        e.printStackTrace()
        null
    }
}