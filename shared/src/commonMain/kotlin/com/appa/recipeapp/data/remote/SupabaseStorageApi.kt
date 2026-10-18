package com.appa.recipeapp.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class SupabaseStorageApi(private val client: SupabaseClient) {

    suspend fun uploadPhoto(
        accessToken: String,
        fileName: String,
        bytes: ByteArray,
        contentType: String,
    ): String? = uploadPhotoToBucket(
        accessToken = accessToken,
        bucket = SupabaseConfig.RECIPE_PHOTOS_BUCKET,
        fileName = fileName,
        bytes = bytes,
        contentType = contentType,
    )

    suspend fun uploadPhotoToBucket(
        accessToken: String,
        bucket: String,
        fileName: String,
        bytes: ByteArray,
        contentType: String,
    ): String? {
        return try {
            // Загрузка — через внутренний BASE_URL (с токеном, через API)
            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.STORAGE_PATH}/object/$bucket/$fileName",
            ) {
                headers {
                    header("apikey", SupabaseConfig.ANON_KEY)
                    header("Authorization", "Bearer $accessToken")
                    header("x-upsert", "true")
                }
                contentType(ContentType.parse(contentType))
                setBody(bytes)
            }

            if (response.status.isSuccess()) {
                // Публичный URL — ВСЕГДА через DuckDNS (работает и на Android, и на PWA)
                "${SupabaseConfig.PUBLIC_STORAGE_BASE_URL}/object/public/$bucket/$fileName"
            } else {
                val errorText = try {
                    response.body<String>()
                } catch (e: Throwable) {
                    "не удалось прочитать тело"
                }
                println("DEBUG uploadPhotoToBucket error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG uploadPhotoToBucket EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            null
        }
    }
}