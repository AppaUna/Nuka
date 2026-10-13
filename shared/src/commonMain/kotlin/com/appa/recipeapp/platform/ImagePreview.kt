package com.appa.recipeapp.platform

/**
 * Превращает ByteArray изображения в URL/строку, пригодную для отображения в AsyncImage.
 * Android: data URL (base64)
 * Web: blob URL
 * iOS: null (заглушка)
 */
expect fun bytesToPreviewUrl(bytes: ByteArray): String?