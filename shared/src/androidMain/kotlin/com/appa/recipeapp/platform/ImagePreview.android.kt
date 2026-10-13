package com.appa.recipeapp.platform

import android.util.Base64

actual fun bytesToPreviewUrl(bytes: ByteArray): String? {
    return try {
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    } catch (e: Throwable) {
        println("DEBUG bytesToPreviewUrl: ${e.message}")
        null
    }
}