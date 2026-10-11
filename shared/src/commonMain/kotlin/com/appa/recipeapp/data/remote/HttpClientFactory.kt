package com.appa.recipeapp.data.remote

import io.ktor.client.engine.HttpClientEngine

/**
 * Создаёт платформенный движок для Ktor.
 * На Android — OkHttp с игнорированием SSL-ошибок.
 * На iOS — Darwin с игнорированием SSL-ошибок.
 */
expect fun createHttpEngine(): HttpClientEngine