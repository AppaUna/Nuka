package com.appa.recipeapp.platform

import com.appa.recipeapp.data.remote.SupabaseClient
import com.appa.recipeapp.data.remote.SupabaseConfig
import com.russhwolf.settings.Settings
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val settings: Settings by lazy { createSettings() }
private val client: SupabaseClient by lazy { SupabaseClient() }

fun saveFcmTokenLocally(token: String) {
    settings.putString("fcm_token", token)
    println("DEBUG FCM: Токен сохранен локально")
}

fun sendFcmTokenToSupabase(token: String) {
    val accessToken = settings.getStringOrNull("auth_access_token")
    val userId = settings.getStringOrNull("auth_user_id")

    if (accessToken == null || userId == null) {
        println("DEBUG FCM: Нет токена авторизации или userId")
        return
    }

    CoroutineScope(Dispatchers.Default).launch {
        try {
            // 1. Удаляем старую подписку для этого пользователя
            client.http.delete("${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/push_subscriptions") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $accessToken")
                parameter("user_id", "eq.$userId")
            }

            // 2. Вставляем новую
            val response: HttpResponse = client.http.post("${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/push_subscriptions") {
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody("""{"user_id":"$userId","endpoint":"$token","p256dh":"","auth":""}""")
            }
            if (response.status.isSuccess()) {
                println("DEBUG FCM: Токен успешно отправлен в Supabase")
            } else {
                println("DEBUG FCM: Ошибка Supabase: ${response.status}")
            }
        } catch (e: Exception) {
            println("DEBUG FCM: Ошибка отправки: ${e.message}")
        }
    }
}