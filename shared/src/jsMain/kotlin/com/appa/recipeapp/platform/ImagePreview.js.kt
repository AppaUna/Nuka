package com.appa.recipeapp.platform

@JsFun("(size) => window.nukaImage.init(size)")
private external fun jsInitImageBuffer(size: Int)

@JsFun("(index, value) => window.nukaImage.set(index, value)")
private external fun jsSetImageByte(index: Int, value: Int)

@JsFun("() => window.nukaImage.toBlobUrl()")
private external fun jsGetImageBlobUrl(): String

actual fun bytesToPreviewUrl(bytes: ByteArray): String? {
    return try {
        jsInitImageBuffer(bytes.size)
        for (i in bytes.indices) {
            jsSetImageByte(i, bytes[i].toInt() and 0xFF)
        }
        val url = jsGetImageBlobUrl()
        println("DEBUG bytesToPreviewUrl: создан blob URL для ${bytes.size} байт")
        url
    } catch (e: Throwable) {
        println("DEBUG bytesToPreviewUrl: ${e.message}")
        null
    }
}