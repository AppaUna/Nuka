package com.appa.recipeapp.platform

/**
 * Сохраняет байты во временное хранилище и возвращает URI/путь.
 *
 * На Android — временный файл в cacheDir.
 * На Web — blob: URL.
 * На iOS — временный файл в Documents (заглушка).
 */
expect suspend fun saveBytesToCache(bytes: ByteArray): String