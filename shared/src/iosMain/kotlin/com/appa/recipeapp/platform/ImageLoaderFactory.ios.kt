package com.appa.recipeapp.platform

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import okio.FileSystem
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

actual fun createImageLoader(): ImageLoader {
    val cacheDir = NSTemporaryDirectory() + "image_cache"

    return ImageLoader.Builder()
        .memoryCache {
            MemoryCache.Builder()
                .maxSizeBytes(100L * 1024 * 1024)   // 100 МБ
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.toPath())
                .maxSizeBytes(100L * 1024 * 1024)
                .build()
        }
        .crossfade(false)
        .build()
}

private fun String.toPath(): okio.Path {
    return okio.Path.Companion.toPath(this)
}