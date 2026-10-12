package com.appa.recipeapp.data.remote

/**
 * Конфигурация Supabase.
 *
 * URL — адрес нашего Nginx на сервере (HTTPS, порт 8444).
 * ANON_KEY — публичный ключ для запросов от имени анонимного пользователя.
 *
 * ВАЖНО: ANON_KEY — это JWT, подписанный JWT_SECRET. Он безопасен для
 * хранения в клиенте (как и Firebase API key), потому что RLS-политики
 * на сервере контролируют доступ к данным.
 */
object SupabaseConfig {
    val BASE_URL: String = com.appa.recipeapp.platform.getSupabaseBaseUrl()
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImlzcyI6InN1cGFiYXNlIiwiaWF0IjoxNzkxNTU2NTk3LCJleHAiOjIxMDY5MTY1OTd9.7uo_yuo7gtD0UqStd4TKbjee1lhHkz_lCSLmBRCTgyI"

    /** Endpoint-ы */
    const val AUTH_PATH = "/auth/v1"
    const val REST_PATH = "/rest/v1"
    const val STORAGE_PATH = "/storage/v1"

    /** Bucket для фото рецептов */
    const val RECIPE_PHOTOS_BUCKET = "recipe-photos"
}