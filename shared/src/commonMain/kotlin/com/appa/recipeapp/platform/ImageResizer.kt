package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap

expect fun resizeImageBitmap(image: ImageBitmap, maxSize: Int): ImageBitmap
expect fun compressImageBytes(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray?
expect suspend fun compressImageBytesAsync(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray?