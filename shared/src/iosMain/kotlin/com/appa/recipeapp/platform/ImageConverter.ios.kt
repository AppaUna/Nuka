package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap? = null
actual fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray? = null

actual fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? = null