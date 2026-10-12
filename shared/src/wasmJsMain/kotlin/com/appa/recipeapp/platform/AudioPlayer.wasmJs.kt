package com.appa.recipeapp.platform

actual class AudioPlayer actual constructor() {
    actual fun play(bytes: ByteArray) { /* no-op на Web */ }
    actual fun stop() { /* no-op */ }
    actual fun release() { /* no-op */ }
}