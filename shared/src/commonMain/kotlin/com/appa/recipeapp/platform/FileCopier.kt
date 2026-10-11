package com.appa.recipeapp.platform

/**
 * Копирует файл из URI (content://) во внутреннее хранилище приложения.
 * Возвращает абсолютный путь к скопированному файлу.
 * На iOS — копирует из URI в Documents.
 */
expect suspend fun copyUriToCache(uri: String): String