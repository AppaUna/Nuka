package com.appa.recipeapp.data.model

import kotlinx.datetime.Clock

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()