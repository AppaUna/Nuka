package com.appa.recipeapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SupabaseClient {

    val http: HttpClient = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
            })
        }
        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
    }

    fun anonHeaders(): Map<String, String> = mapOf(
        "apikey" to SupabaseConfig.ANON_KEY,
        "Authorization" to "Bearer ${SupabaseConfig.ANON_KEY}",
    )

    fun authHeaders(accessToken: String): Map<String, String> = mapOf(
        "apikey" to SupabaseConfig.ANON_KEY,
        "Authorization" to "Bearer $accessToken",
    )

    fun close() {
        http.close()
    }
}