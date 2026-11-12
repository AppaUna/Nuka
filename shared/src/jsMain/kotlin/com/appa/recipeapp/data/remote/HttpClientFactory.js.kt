package com.appa.recipeapp.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createHttpEngine(): HttpClientEngine = Js.create()