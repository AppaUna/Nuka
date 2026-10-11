package com.appa.recipeapp.data.model

import kotlinx.serialization.Serializable

/**
 * Текущая сессия пользователя.
 * Хранится локально в multiplatform-settings.
 */
@Serializable
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val login: String,
    val displayName: String,
    val expiresAt: Long = 0L,  // миллисекунды, 0 = "неизвестно, обновлять сразу"
) {
    fun isExpiringSoon(thresholdMillis: Long = 5 * 60 * 1000L): Boolean {
        return expiresAt > 0 && (expiresAt - currentTimeMillis()) < thresholdMillis
    }

    fun isExpired(): Boolean {
        return expiresAt > 0 && expiresAt <= currentTimeMillis()
    }
}
