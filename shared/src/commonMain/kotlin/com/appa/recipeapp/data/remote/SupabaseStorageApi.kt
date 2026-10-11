package com.appa.recipeapp.data.remote

import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.client.call.body

class SupabaseStorageApi(private val client: SupabaseClient) {

    /**
     * Загружает файл в бакет `recipe-photos`.
     *
     * @param accessToken — Bearer-токен авторизованного пользователя
     * @param fileName — например "abc-123.jpg" (уникальное имя)
     * @param bytes — содержимое файла
     * @param contentType — "image/jpeg" / "image/png" / "image/webp"
     * @return публичный URL файла, или null при ошибке
     */
    suspend fun uploadPhoto(
        accessToken: String,
        fileName: String,
        bytes: ByteArray,
        contentType: String,
    ): String? {
        return try {
            val bucket = SupabaseConfig.RECIPE_PHOTOS_BUCKET

            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.STORAGE_PATH}/object/$bucket/$fileName",
            ) {
                headers {
                    header("apikey", SupabaseConfig.ANON_KEY)
                    header("Authorization", "Bearer $accessToken")
                    // upsert: true — перезаписать, если такой файл есть
                    header("x-upsert", "true")
                }
                contentType(ContentType.parse(contentType))
                setBody(bytes)
            }

            if (response.status.isSuccess()) {
                // Публичный URL
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.STORAGE_PATH}/object/public/$bucket/$fileName"
            } else {
                val errorText = try {
                    response.body<String>()
                } catch (e: Throwable) {
                    "не удалось прочитать тело"
                }
                println("DEBUG uploadPhoto error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG uploadPhoto EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            null
        }
    }
}