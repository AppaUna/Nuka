package com.appa.recipeapp.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURLSessionConfiguration

actual fun createHttpEngine(): HttpClientEngine = Darwin.create {
    // iOS-движок Darwin по умолчанию хорошо работает с самоподписанными
    // сертификатами, если приложение доверяет им через установку профиля.
    // Для dev-сборки можно игнорировать через NSURLSession delegate,
    // но это требует дополнительной настройки — отложим.
}