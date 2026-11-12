package com.appa.recipeapp.platform

actual suspend fun saveBytesToCache(bytes: ByteArray): String {
    // TODO: на Web кроп всё равно не работает (Crayfish не поддерживает wasmJs)
    // Пока возвращаем пустую строку — EditorScreen будет использовать байты напрямую
    return ""
}