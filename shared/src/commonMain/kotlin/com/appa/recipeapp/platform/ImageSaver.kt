package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Сохраняет ImageBitmap в файл приложения и возвращает путь к файлу.
 * На Android — filesDir, на iOS — Documents.
 */
expect suspend fun saveImageToFile(image: ImageBitmap): String