package com.appa.recipeapp.platform

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import okio.FileSystem

private var appContextForCoil: Context? = null

fun initImageLoaderFactory(context: Context) {
    appContextForCoil = context.applicationContext
}

actual fun createImageLoader(): ImageLoader {
    val context = appContextForCoil
        ?: error("initImageLoaderFactory(context) не вызван в MainActivity")

    return ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)   // 25% RAM
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizeBytes(100L * 1024 * 1024)   // 100 МБ
                .build()
        }
        .crossfade(false)   // без анимации — быстрее
        .build()
}