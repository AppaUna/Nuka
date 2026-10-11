package com.appa.recipeapp.platform

/**
 * Читает содержимое файла по URI в ByteArray.
 * Нужно для загрузки фото в Supabase Storage.
 *
 * На Android — через ContentResolver.
 * На iOS — через NSData (пока заглушка).
 */
expect suspend fun readFileBytes(uri: String): ByteArray?