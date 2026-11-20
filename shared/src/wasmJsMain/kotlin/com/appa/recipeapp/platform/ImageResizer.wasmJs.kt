package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Rect
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@JsFun("(size) => new Uint8Array(size)")
private external fun jsCreateUint8Array(size: Int): JsAny

@JsFun("(arr, index, value) => { arr[index] = value; }")
private external fun jsSetArrayByte(arr: JsAny, index: Int, value: Int)

@JsFun("(arr) => arr.length")
private external fun jsArrayLength(arr: JsAny): Int

@JsFun("(arr, i) => arr[i]")
private external fun jsGetArrayByte(arr: JsAny, i: Int): Int

@JsFun("""
    (data, maxSize, quality, resolve) => {
        window.nukaCanvas.compressAsync(data, maxSize, quality, (base64) => {
            if (!base64) { resolve(null); return; }
            const bin = atob(base64);
            const arr = new Uint8Array(bin.length);
            for (let i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
            resolve(arr);
        });
    }
""")
private external fun jsCompressAsync(data: JsAny, maxSize: Int, quality: Int, resolve: (JsAny?) -> Unit)

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

actual fun compressImageBytes(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? {
    return null
}

actual suspend fun compressImageBytesAsync(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? {
    val jsArray = jsCreateUint8Array(bytes.size)
    for (i in bytes.indices) {
        jsSetArrayByte(jsArray, i, bytes[i].toInt())
    }

    return suspendCancellableCoroutine { cont ->
        jsCompressAsync(jsArray, maxSize, quality) { result ->
            if (result == null) {
                cont.resume(null)
            } else {
                val size = jsArrayLength(result)
                val out = ByteArray(size)
                for (i in 0 until size) {
                    out[i] = jsGetArrayByte(result, i).toByte()
                }
                cont.resume(out)
            }
        }
    }
}