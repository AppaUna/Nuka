package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

/** Загрузить PlatformFile как ImageBitmap (для Krop). */
expect suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap?

/** Конвертировать ImageBitmap в JPEG-байты (для Supabase). */
expect fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray?

/** Конвертировать ByteArray в ImageBitmap для отображения в Compose. */
expect fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap?