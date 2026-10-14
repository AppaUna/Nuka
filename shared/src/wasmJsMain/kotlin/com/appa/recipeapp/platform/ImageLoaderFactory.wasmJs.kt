package com.appa.recipeapp.platform

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade

actual fun createImageLoader(): ImageLoader {
    return ImageLoader.Builder(PlatformContext.INSTANCE)
        .components {
            add(KtorNetworkFetcherFactory())
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(PlatformContext.INSTANCE, 0.20)  // 20% памяти браузера
                .build()
        }
        .crossfade(false)   // без анимации — быстрее
        .build()
}