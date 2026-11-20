package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

expect suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap?

expect fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray?

expect suspend fun imageBitmapToJpegBytesAsync(image: ImageBitmap): ByteArray?
expect suspend fun imageBitmapToWebpBytesAsync(image: ImageBitmap, quality: Int): ByteArray?

expect fun imageBitmapToWebpBytes(image: ImageBitmap, quality: Int = 90): ByteArray?

expect fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap?