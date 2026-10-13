package com.appa.recipeapp.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes

/**
 * Создать Uint8Array в JS и вернуть ссылку.
 */
@JsFun("(size) => new Uint8Array(size)")
private external fun jsCreateUint8Array(size: Int): JsAny

/**
 * Записать байт в Uint8Array по индексу.
 */
@JsFun("(arr, index, value) => { arr[index] = value; }")
private external fun jsSetArrayByte(arr: JsAny, index: Int, value: Int)

/**
 * Синхронно закодировать RGBA-байты в JPEG data URL.
 * Возвращает строку 'data:image/jpeg;base64,...' или ''.
 */
@JsFun("""
    (rgbaArray, width, height, quality) => {
        return window.nukaCanvas.encodeJpegSync(rgbaArray, width, height, quality);
    }
""")
private external fun jsEncodeJpegSync(
    rgbaArray: JsAny,
    width: Int,
    height: Int,
    quality: Double,
): String

/**
 * Загрузить PlatformFile как ImageBitmap.
 */
actual suspend fun loadImageBitmap(file: PlatformFile): ImageBitmap? {
    return try {
        val bytes = file.readBytes()
        println("DEBUG loadImageBitmap (Web): прочитано ${bytes.size} байт")
        bytes.decodeToImageBitmap()
    } catch (e: Throwable) {
        println("DEBUG loadImageBitmap (Web) EXCEPTION: ${e::class.simpleName} — ${e.message}")
        e.printStackTrace()
        null
    }
}

/**
 * Конвертировать ImageBitmap в JPEG-байты через JS canvas.
 */
actual fun imageBitmapToJpegBytes(image: ImageBitmap): ByteArray? {
    return try {
        val w = image.width
        val h = image.height
        println("DEBUG imageBitmapToJpegBytes (Web): размер $w x $h")

        // 1. Получаем ARGB-пиксели из ImageBitmap
        val argbPixels = IntArray(w * h)
        image.readPixels(argbPixels)

        // 2. Создаём Uint8Array для RGBA (4 байта на пиксель)
        val rgbaSize = w * h * 4
        val rgbaArray = jsCreateUint8Array(rgbaSize)

        // 3. ARGB (int) → RGBA (4 байта)
        for (i in argbPixels.indices) {
            val argb = argbPixels[i]
            val a = (argb shr 24) and 0xFF
            val r = (argb shr 16) and 0xFF
            val g = (argb shr 8) and 0xFF
            val b = argb and 0xFF
            val baseIdx = i * 4
            jsSetArrayByte(rgbaArray, baseIdx, r)
            jsSetArrayByte(rgbaArray, baseIdx + 1, g)
            jsSetArrayByte(rgbaArray, baseIdx + 2, b)
            jsSetArrayByte(rgbaArray, baseIdx + 3, a)
        }

        // 4. Синхронно получаем data URL
        val dataUrl = jsEncodeJpegSync(rgbaArray, w, h, 0.9)
        println("DEBUG imageBitmapToJpegBytes (Web): dataUrl length = ${dataUrl.length}")

        if (dataUrl.isEmpty()) {
            println("DEBUG imageBitmapToJpegBytes (Web): dataUrl пустой")
            return null
        }

        // 5. Извлекаем base64-часть
        val prefix = "data:image/jpeg;base64,"
        if (!dataUrl.startsWith(prefix)) {
            println("DEBUG imageBitmapToJpegBytes (Web): неожиданный префикс — ${dataUrl.take(50)}")
            return null
        }
        val base64 = dataUrl.substring(prefix.length)

        // 6. Декодируем base64 → ByteArray
        val result = decodeBase64ToBytes(base64)
        println("DEBUG imageBitmapToJpegBytes (Web): декодировано ${result.size} байт")
        result
    } catch (e: Throwable) {
        println("DEBUG imageBitmapToJpegBytes (Web) EXCEPTION: ${e::class.simpleName} — ${e.message}")
        e.printStackTrace()
        null
    }
}

/**
 * Конвертировать ByteArray в ImageBitmap для отображения.
 */
actual fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        bytes.decodeToImageBitmap()
    } catch (e: Throwable) {
        println("DEBUG bytesToImageBitmap (Web) EXCEPTION: ${e::class.simpleName} — ${e.message}")
        null
    }
}

/**
 * Декодирование base64 → ByteArray.
 * Стандартная реализация для wasmJs (в commonMain нет Base64 decoder).
 */
private fun decodeBase64ToBytes(base64: String): ByteArray {
    // Убираем возможные переносы строк
    val cleanInput = base64.replace("\n", "").replace("\r", "")

    // Считаем padding
    var padding = 0
    if (cleanInput.endsWith("==")) padding = 2
    else if (cleanInput.endsWith("=")) padding = 1

    val outputLength = (cleanInput.length / 4) * 3 - padding
    val output = ByteArray(outputLength)

    var outIdx = 0
    var buffer = 0
    var bitsCollected = 0

    for (c in cleanInput) {
        val value = when (c) {
            in 'A'..'Z' -> c - 'A'
            in 'a'..'z' -> c - 'a' + 26
            in '0'..'9' -> c - '0' + 52
            '+' -> 62
            '/' -> 63
            '=' -> break
            else -> continue
        }
        buffer = (buffer shl 6) or value
        bitsCollected += 6
        if (bitsCollected >= 8) {
            bitsCollected -= 8
            outIdx += 1
            if (outIdx <= output.size) {
                output[outIdx - 1] = ((buffer shr bitsCollected) and 0xFF).toByte()
            }
        }
    }
    return output
}