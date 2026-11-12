package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Rect

actual fun resizeImageBitmap(image: ImageBitmap, maxSize: Int): ImageBitmap {
    val width = image.width
    val height = image.height
    if (width <= maxSize && height <= maxSize) return image

    val ratio = width.toFloat() / height.toFloat()
    val (newWidth, newHeight) = if (ratio > 1) {
        maxSize to (maxSize / ratio).toInt()
    } else {
        (maxSize * ratio).toInt() to maxSize
    }

    val skiaImage = Image.makeFromBitmap(image.asSkiaBitmap())
    val surface = Surface.makeRasterN32Premul(newWidth, newHeight)
    surface.canvas.drawImageRect(
        skiaImage,
        Rect.makeWH(newWidth.toFloat(), newHeight.toFloat())
    )
    return surface.makeImageSnapshot().toComposeImageBitmap()
}