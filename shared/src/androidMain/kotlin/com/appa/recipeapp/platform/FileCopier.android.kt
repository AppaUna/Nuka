package com.appa.recipeapp.platform

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private var fileCopierContext: Context? = null

fun initFileCopier(context: Context) {
    fileCopierContext = context.applicationContext
}

actual suspend fun copyUriToCache(uri: String): String = withContext(Dispatchers.IO) {
    val context = fileCopierContext
        ?: error("initFileCopier(context) не был вызван. Проверьте MainActivity.")
    val inputStream = context.contentResolver.openInputStream(Uri.parse(uri))
        ?: error("Не удалось открыть URI: $uri")
    val file = File(context.cacheDir, "cropping_input_${UUID.randomUUID()}.jpg")
    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }
    inputStream.close()
    file.absolutePath
}