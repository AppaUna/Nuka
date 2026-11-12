package com.appa.recipeapp.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class TimeOfDay {
    MORNING, // 6-12
    DAY,     // 12-18
    EVENING, // 18-24
    NIGHT    // 0-6
}

fun currentTimeOfDay(): TimeOfDay {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        in 6..11 -> TimeOfDay.MORNING
        in 12..17 -> TimeOfDay.DAY
        in 18..23 -> TimeOfDay.EVENING
        else -> TimeOfDay.NIGHT
    }
}

fun greetingForNow(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = now.hour
    return when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
}

fun dayOfYear(): Int {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return now.dayOfYear
}