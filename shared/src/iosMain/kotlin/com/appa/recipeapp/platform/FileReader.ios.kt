package com.appa.recipeapp.platform

actual suspend fun readFileBytes(uri: String): ByteArray? {
    // TODO: iOS-реализация через NSData(contentsOfURL:)
    println("DEBUG readFileBytes: iOS не реализовано")
    return null
}