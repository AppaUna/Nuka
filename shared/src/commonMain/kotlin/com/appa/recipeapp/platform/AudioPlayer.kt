package com.appa.recipeapp.platform

/**
 * Простой аудиоплеер.
 * На Android — MediaPlayer.
 * На iOS — AVAudioPlayer.
 * На вход принимает байты файла.
 */
expect class AudioPlayer() {
    fun play(bytes: ByteArray)
    fun stop()
    fun release()
}