package com.appa.recipeapp.platform

/** Инициализация JS-буфера (оставляем, но для Web Audio не используем). */
@JsFun("(size) => window.nukaBytes.init(size)")
private external fun jsInitBuffer(size: Int)

/** Установка одного байта в буфер. */
@JsFun("(index, value) => window.nukaBytes.set(index, value)")
private external fun jsSetByte(index: Int, value: Int)

/** Создание Uint8Array из Kotlin ByteArray (через JS). */
@JsFun("(bytes) => new Uint8Array(bytes)")
private external fun jsCreateUint8ArrayFromKotlin(bytes: ByteArray): dynamic

/** Воспроизведение через Web Audio API. */
@JsFun("""
    (bytesArray) => {
        // Создаём AudioContext один раз
        if (!window.__nukaAudioCtx) {
            const AudioContextClass = window.AudioContext || window.webkitAudioContext;
            window.__nukaAudioCtx = new AudioContextClass();
        }
        const ctx = window.__nukaAudioCtx;

        // Разблокируем (вызывается из обработчика клика)
        if (ctx.state === 'suspended') {
            ctx.resume().catch(e => console.error('AudioContext resume error:', e));
        }

        // Декодируем и играем
        const arrayBuffer = bytesArray.buffer.slice(bytesArray.byteOffset, bytesArray.byteOffset + bytesArray.byteLength);
        ctx.decodeAudioData(arrayBuffer, (audioBuffer) => {
            const source = ctx.createBufferSource();
            source.buffer = audioBuffer;
            source.connect(ctx.destination);
            source.start(0);
            window.__nukaAudioSource = source;
            console.log('DEBUG WebAudio: воспроизведение начато');
        }, (err) => {
            console.error('DEBUG WebAudio: decodeAudioData error', err);
        });
    }
""")
private external fun jsPlayBytes(bytesArray: dynamic)

/** Остановка. */
@JsFun("""
    () => {
        if (window.__nukaAudioSource) {
            try { window.__nukaAudioSource.stop(); } catch(e) {}
            window.__nukaAudioSource = null;
        }
    }
""")
private external fun jsStopAudio()

actual class AudioPlayer actual constructor() {

    actual fun play(bytes: ByteArray) {
        try {
            // Конвертируем ByteArray в Uint8Array через JS-функцию
            val uint8 = jsCreateUint8ArrayFromKotlin(bytes)
            jsPlayBytes(uint8)
        } catch (e: Throwable) {
            println("DEBUG AudioPlayer.js play: ${e.message}")
        }
    }

    actual fun stop() {
        try {
            jsStopAudio()
        } catch (e: Throwable) {
            println("DEBUG AudioPlayer.js stop: ${e.message}")
        }
    }

    actual fun release() {
        stop()
    }
}