package com.appa.recipeapp.data.remote

object SupabaseConfig {
    /** Внутренний URL для API-запросов (различается на Android и Web) */
    val BASE_URL: String = com.appa.recipeapp.platform.getSupabaseBaseUrl()

    /**
     * Публичный URL для файлов (одинаковый для всех клиентов).
     * Используется для построения ссылок на картинки, которые должны
     * открываться на любом устройстве.
     */
    const val PUBLIC_STORAGE_BASE_URL = "https://nukaapp.duckdns.org:8446/api/storage/v1"

    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImlzcyI6InN1cGFiYXNlIiwiaWF0IjoxNzkxNTU2NTk3LCJleHAiOjIxMDY5MTY1OTd9.7uo_yuo7gtD0UqStd4TKbjee1lhHkz_lCSLmBRCTgyI"

    const val AUTH_PATH = "/auth/v1"
    const val REST_PATH = "/rest/v1"
    const val STORAGE_PATH = "/storage/v1"

    const val RECIPE_PHOTOS_BUCKET = "recipe-photos"
}