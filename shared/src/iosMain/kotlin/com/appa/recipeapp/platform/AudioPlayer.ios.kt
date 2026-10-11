package com.appa.recipeapp.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer actual constructor() {

    private var player: AVAudioPlayer? = null

    actual fun play(bytes: ByteArray) {
        stop()
        val data = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong(),
            )
        }
        player = AVAudioPlayer(data = data, error = null).apply {
            prepareToPlay()
            play()
        }
    }

    actual fun stop() {
        player?.stop()
        player = null
    }

    actual fun release() {
        stop()
    }
}