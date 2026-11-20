package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.coroutines.resume

@JsFun("""
    (data, maxSize, quality, resolve) => {
        const blob = new Blob([data]);
        createImageBitmap(blob).then(img => {
            let width = img.width;
            let height = img.height;

            if (width <= maxSize && height <= maxSize) {
                const canvas = new OffscreenCanvas(width, height);
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0);
                canvas.convertToBlob({ type: 'image/jpeg', quality: quality / 100 })
                    .then(b => b.arrayBuffer())
                    .then(buf => resolve(new Uint8Array(buf)))
                    .catch(() => resolve(null));
                return;
            }

            const ratio = width / height;
            if (ratio > 1) { width = maxSize; height = Math.round(maxSize / ratio); }
            else { height = maxSize; width = Math.round(maxSize * ratio); }

            const canvas = new OffscreenCanvas(width, height);
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);
            canvas.convertToBlob({ type: 'image/jpeg', quality: quality / 100 })
                .then(b => b.arrayBuffer())
                .then(buf => resolve(new Uint8Array(buf)))
                .catch(() => resolve(null));
        }).catch(() => resolve(null));
    }
""")
private external fun jsCompressViaCanvas(
    data: JsAny,
    maxSize: Int,
    quality: Int,
    resolve: (JsAny?) -> Unit,
)

@JsFun("(size) => new Uint8Array(size)")
private external fun jsCreateUint8Array(size: Int): JsAny

@JsFun("(arr, index, value) => { arr[index] = value; }")
private external fun jsSetArrayByte(arr: JsAny, index: Int, value: Int)

@JsFun("(arr) => arr.length")
private external fun jsArrayLength(arr: JsAny): Int

@JsFun("(arr, i) => arr[i]")
private external fun jsGetArrayByte(arr: JsAny, i: Int): Int

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
    val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(newWidth, newHeight)
    surface.canvas.drawImageRect(
        skiaImage,
        org.jetbrains.skia.Rect.makeWH(newWidth.toFloat(), newHeight.toFloat())
    )
    return surface.makeImageSnapshot().toComposeImageBitmap()
}

actual fun compressImageBytes(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? = null

actual suspend fun compressImageBytesAsync(
    bytes: ByteArray,
    maxSize: Int,
    quality: Int,
): ByteArray? {
    val jsArray = jsCreateUint8Array(bytes.size)
    for (i in bytes.indices) {
        jsSetArrayByte(jsArray, i, bytes[i].toInt())
    }

    return suspendCancellableCoroutine { cont ->
        jsCompressViaCanvas(jsArray, maxSize, quality) { result ->
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

actual suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap? {
    return try {
        val bytes = file.readBytes()
        bytes.decodeToImageBitmap()
    } catch (e: Throwable) {
        null
    }
}

actual fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray? {
    return try {
        val skiaBitmap = image.asSkiaBitmap()
        val skiaImage = Image.makeFromBitmap(skiaBitmap)
        val data = skiaImage.encodeToData(EncodedImageFormat.JPEG, 80)
        data?.bytes
    } catch (e: Throwable) {
        null
    }
}

actual suspend fun imageBitmapToJpegBytesAsync(image: ImageBitmap): ByteArray? {
    return imageBitmapToJpegBytes(image)
}

actual fun imageBitmapToWebpBytes(image: ImageBitmap, quality: Int): ByteArray? {
    return try {
        val skiaBitmap = image.asSkiaBitmap()
        val skiaImage = Image.makeFromBitmap(skiaBitmap)
        val data = skiaImage.encodeToData(EncodedImageFormat.WEBP, quality)
        data?.bytes
    } catch (e: Throwable) {
        return try {
            val skiaBitmap = image.asSkiaBitmap()
            val skiaImage = Image.makeFromBitmap(skiaBitmap)
            skiaImage.encodeToData(EncodedImageFormat.JPEG, quality)?.bytes
        } catch (e2: Throwable) {
            null
        }
    }
}

actual suspend fun imageBitmapToWebpBytesAsync(
    image: ImageBitmap,
    quality: Int,
): ByteArray? {
    return imageBitmapToWebpBytes(image, quality)
}

actual fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        bytes.decodeToImageBitmap()
    } catch (e: Throwable) {
        null
    }
}