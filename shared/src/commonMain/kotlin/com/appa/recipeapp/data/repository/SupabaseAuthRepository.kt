package com.appa.recipeapp.data.repository

import com.appa.recipeapp.data.model.UserSession
import com.appa.recipeapp.data.model.currentTimeMillis
import com.appa.recipeapp.data.remote.SupabaseAuthApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SupabaseAuthRepository(
    private val authApi: SupabaseAuthApi,
    private val settings: Settings,
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_LOGIN = "auth_login"
        private const val KEY_DISPLAY_NAME = "auth_display_name"
        private const val KEY_EXPIRES_AT = "auth_expires_at"

        private const val FAKE_DOMAIN = "@nukapp.local"
    }

    private val _session = MutableStateFlow<UserSession?>(loadSession())
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    /** Мьютекс, чтобы одновременно не запускать несколько refresh-запросов. */
    private val refreshMutex = Mutex()

    fun isLoggedIn(): Boolean = _session.value != null

    fun getLogin(): String = _session.value?.login.orEmpty()

    fun getDisplayName(): String = _session.value?.displayName.orEmpty()

    fun getUserId(): String = _session.value?.userId.orEmpty()

    /**
     * Возвращает актуальную сессию.
     * Если токен истекает < 5 минут — рефрешит его (с мьютексом).
     * Если рефреш не удался — возвращает null (сессия невалидна).
     */
    suspend fun ensureValidSession(): UserSession? {
        val current = _session.value ?: return null

        // Если токен ещё долго живёт — возвращаем как есть
        if (!current.isExpiringSoon()) return current

        // Рефрешим под мьютексом — чтобы два параллельных запроса
        // не сделали два refresh одновременно
        return refreshMutex.withLock {
            // Перепроверяем после захвата мьютекса: возможно, кто-то уже обновил
            val latest = _session.value ?: return@withLock null
            if (!latest.isExpiringSoon()) return@withLock latest

            println("DEBUG ensureValidSession: рефреш токена...")
            val refreshed = authApi.refreshSession(latest.refreshToken)

            if (refreshed == null) {
                println("DEBUG ensureValidSession: рефреш не удался — сессия невалидна")
                // Refresh не сработал — разлогиниваем
                signOut()
                return@withLock null
            }

            val newExpiresAt = currentTimeMillis() + refreshed.expiresIn * 1000L
            val updated = latest.copy(
                accessToken = refreshed.accessToken,
                refreshToken = refreshed.refreshToken.ifBlank { latest.refreshToken },
                expiresAt = newExpiresAt,
            )
            saveSession(updated)
            println("DEBUG ensureValidSession: токен обновлён, expiresAt=${updated.expiresAt}")
            updated
        }
    }

    suspend fun signUp(
        login: String,
        password: String,
        displayName: String,
    ): Result<UserSession> {
        val email = loginToEmail(login)
        val authSession = authApi.signUp(email, password, displayName)
            ?: return Result.failure(Exception("Не удалось создать аккаунт"))

        val expiresAt = currentTimeMillis() + authSession.expiresIn * 1000L
        val userSession = UserSession(
            accessToken = authSession.accessToken,
            refreshToken = authSession.refreshToken,
            userId = authSession.user?.id.orEmpty(),
            login = login,
            displayName = displayName,
            expiresAt = expiresAt,
        )
        saveSession(userSession)
        return Result.success(userSession)
    }

    suspend fun signIn(login: String, password: String): Result<UserSession> {
        val email = loginToEmail(login)
        val authSession = authApi.signIn(email, password)
            ?: return Result.failure(Exception("Неверный логин или пароль"))

        val authUser = authApi.getCurrentUser(authSession.accessToken)
        val displayName = authUser?.userMetadata?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: login

        val expiresAt = currentTimeMillis() + authSession.expiresIn * 1000L
        val userSession = UserSession(
            accessToken = authSession.accessToken,
            refreshToken = authSession.refreshToken,
            userId = authSession.user?.id.orEmpty(),
            login = login,
            displayName = displayName,
            expiresAt = expiresAt,
        )
        saveSession(userSession)
        return Result.success(userSession)
    }

    /**
     * Обновляет display_name на сервере (user_metadata) и в локальной сессии.
     * Возвращает true при успехе.
     */
    suspend fun updateDisplayName(newName: String): Boolean {
        val current = _session.value ?: return false
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return false

        val updatedUser = authApi.updateUserMetadata(current.accessToken, trimmed)
            ?: return false

        val updated = current.copy(displayName = trimmed)
        saveSession(updated)
        println("DEBUG updateDisplayName: обновлено на '$trimmed'")
        return true
    }

    fun signOut() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_LOGIN)
        settings.remove(KEY_DISPLAY_NAME)
        settings.remove(KEY_EXPIRES_AT)
        _session.value = null
    }

    private fun saveSession(session: UserSession) {
        settings.putString(KEY_ACCESS_TOKEN, session.accessToken)
        settings.putString(KEY_REFRESH_TOKEN, session.refreshToken)
        settings.putString(KEY_USER_ID, session.userId)
        settings.putString(KEY_LOGIN, session.login)
        settings.putString(KEY_DISPLAY_NAME, session.displayName)
        settings.putLong(KEY_EXPIRES_AT, session.expiresAt)
        _session.value = session
    }

    private fun loadSession(): UserSession? {
        val accessToken = settings.getStringOrNull(KEY_ACCESS_TOKEN) ?: return null
        val refreshToken = settings.getStringOrNull(KEY_REFRESH_TOKEN) ?: return null
        val userId = settings.getStringOrNull(KEY_USER_ID).orEmpty()
        val login = settings.getStringOrNull(KEY_LOGIN).orEmpty()
        val displayName = settings.getStringOrNull(KEY_DISPLAY_NAME).orEmpty()
        val expiresAt = settings.getLong(KEY_EXPIRES_AT, 0L)
        return UserSession(accessToken, refreshToken, userId, login, displayName, expiresAt)
    }

    private fun loginToEmail(login: String): String {
        return if (login.contains("@")) login else "$login$FAKE_DOMAIN"
    }
}