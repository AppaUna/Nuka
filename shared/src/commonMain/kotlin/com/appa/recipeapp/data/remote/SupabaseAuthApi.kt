package com.appa.recipeapp.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null,
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class UpdateUserRequest(
    val data: Map<String, String>? = null,
)

@Serializable
data class AuthUser(
    val id: String = "",
    val email: String = "",
    @SerialName("user_metadata")
    val userMetadata: UserMetadata? = null,
)

@Serializable
data class UserMetadata(
    @SerialName("display_name")
    val displayName: String? = null,
)

@Serializable
data class AuthSession(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("expires_in") val expiresIn: Long = 3600L,
    val user: AuthUser? = null,
)

class SupabaseAuthApi(private val client: SupabaseClient) {

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String? = null,
    ): AuthSession? {
        val body = SignUpRequest(
            email = email,
            password = password,
            data = displayName?.let { mapOf("display_name" to it) },
        )
        return request("${SupabaseConfig.BASE_URL}${SupabaseConfig.AUTH_PATH}/signup", body)
    }

    suspend fun signIn(email: String, password: String): AuthSession? {
        val body = SignInRequest(email = email, password = password)
        return request(
            "${SupabaseConfig.BASE_URL}${SupabaseConfig.AUTH_PATH}/token?grant_type=password",
            body,
        )
    }

    suspend fun refreshSession(refreshToken: String): AuthSession? {
        val body = RefreshTokenRequest(refreshToken = refreshToken)
        return request(
            "${SupabaseConfig.BASE_URL}${SupabaseConfig.AUTH_PATH}/token?grant_type=refresh_token",
            body,
        )
    }

    suspend fun getCurrentUser(accessToken: String): AuthUser? {
        return try {
            val response: HttpResponse = client.http.get(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.AUTH_PATH}/user",
            ) {
                headers {
                    header("apikey", SupabaseConfig.ANON_KEY)
                    header("Authorization", "Bearer $accessToken")
                }
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorText = response.body<String>()
                println("DEBUG getCurrentUser error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG getCurrentUser EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Обновляет user_metadata (в том числе display_name) текущего пользователя.
     * GoTrue сольёт новые поля с существующими (не перезапишет).
     */
    suspend fun updateUserMetadata(
        accessToken: String,
        displayName: String,
    ): AuthUser? {
        return try {
            val body = UpdateUserRequest(
                data = mapOf("display_name" to displayName),
            )
            val response: HttpResponse = client.http.put(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.AUTH_PATH}/user",
            ) {
                headers {
                    header("apikey", SupabaseConfig.ANON_KEY)
                    header("Authorization", "Bearer $accessToken")
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorText = response.body<String>()
                println("DEBUG updateUserMetadata error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG updateUserMetadata EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private suspend inline fun <reified T : Any> request(url: String, body: T): AuthSession? {
        return try {
            val response: HttpResponse = client.http.post(url) {
                headers {
                    client.anonHeaders().forEach { (key, value) ->
                        header(key, value)
                    }
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            if (response.status.value in 200..299) {
                response.body()
            } else {
                val errorText = response.body<String>()
                println("DEBUG authRequest error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG authRequest EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            null
        }
    }
}