package com.appa.recipeapp.platform

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import java.io.FileOutputStream

private var appContextForAudio: Context? = null

fun initAudioPlayer(context: Context) {
    appContextForAudio = context.applicationContext
}

actual class AudioPlayer actual constructor() {

    private var player: MediaPlayer? = null
    private var tempFile: File? = null

    actual fun play(bytes: ByteArray) {
        stop()

        val context = appContextForAudio
            ?: error("initAudioPlayer(context) не был вызван. Проверьте MainActivity.")

        val file = File.createTempFile("joke_", ".mp4", context.cacheDir)
        FileOutputStream(file).use { it.write(bytes) }
        tempFile = file

        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
            setOnCompletionListener {
                it.release()
                player = null
                tempFile?.delete()
                tempFile = null
            }
        }
    }

    actual fun stop() {
        try {
            player?.stop()
            player?.release()
        } catch (e: Throwable) {
            println("DEBUG AudioPlayer.android stop: ${e.message}")
        }
        player = null
        tempFile?.delete()
        tempFile = null
    }

    actual fun release() {
        stop()
    }
}