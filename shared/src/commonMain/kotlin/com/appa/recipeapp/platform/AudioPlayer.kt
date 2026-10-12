package com.appa.recipeapp.platform

/**
 * Простой аудиоплеер.
 * На вход принимает байты файла (mp4).
 * На Android — MediaPlayer, на iOS — AVAudioPlayer, на Web — HTMLAudioElement через Blob URL.
 */
expect class AudioPlayer() {
    fun play(bytes: ByteArray)
    fun stop()
    fun release()
}