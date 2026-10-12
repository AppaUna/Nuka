package com.appa.recipeapp.platform

/** Инициализация JS-буфера нужного размера. */
@JsFun("(size) => window.nukaBytes.init(size)")
private external fun jsInitBuffer(size: Int)

/** Установка одного байта в буфер. */
@JsFun("(index, value) => window.nukaBytes.set(index, value)")
private external fun jsSetByte(index: Int, value: Int)

/** Создание Blob URL из буфера. */
@JsFun("() => window.nukaBytes.toAudioUrl()")
private external fun jsGetAudioUrl(): String

/** Воспроизведение URL. */
@JsFun("""
    (url) => {
        if (!window.__nukaAudio) {
            window.__nukaAudio = new Audio();
        }
        window.__nukaAudio.src = url;
        window.__nukaAudio.play().catch(e => console.error('Audio play error:', e));
    }
""")
private external fun jsPlayUrl(url: String)

/** Остановка. */
@JsFun("""
    () => {
        if (window.__nukaAudio) {
            window.__nukaAudio.pause();
            window.__nukaAudio.currentTime = 0;
        }
    }
""")
private external fun jsStopAudio()

actual class AudioPlayer actual constructor() {

    actual fun play(bytes: ByteArray) {
        try {
            // 1. Инициализируем JS-буфер
            jsInitBuffer(bytes.size)
            // 2. Копируем байты поэтапно
            for (i in bytes.indices) {
                jsSetByte(i, bytes[i].toInt() and 0xFF)
            }
            // 3. Получаем Blob URL
            val url = jsGetAudioUrl()
            // 4. Играем
            jsPlayUrl(url)
        } catch (e: Throwable) {
            println("DEBUG AudioPlayer.wasmJs play: ${e.message}")
        }
    }

    actual fun stop() {
        try {
            jsStopAudio()
        } catch (e: Throwable) {
            println("DEBUG AudioPlayer.wasmJs stop: ${e.message}")
        }
    }

    actual fun release() {
        stop()
    }
}