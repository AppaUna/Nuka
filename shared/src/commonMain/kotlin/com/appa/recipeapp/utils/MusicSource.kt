package com.appa.recipeapp.utils

enum class MusicSource(val displayName: String) {
    SPOTIFY("Открыть в Spotify"),
    YANDEX("Открыть в Яндекс.Музыке"),
    UNKNOWN("Открыть ссылку"),
}

object MusicSourceDetector {
    fun detect(url: String): MusicSource {
        val lower = url.lowercase()
        return when {
            "spotify.com" in lower -> MusicSource.SPOTIFY
            "music.yandex.ru" in lower ||
                    "music.yandex.com" in lower ||
                    "yandex.ru/music" in lower -> MusicSource.YANDEX
            else -> MusicSource.UNKNOWN
        }
    }
}