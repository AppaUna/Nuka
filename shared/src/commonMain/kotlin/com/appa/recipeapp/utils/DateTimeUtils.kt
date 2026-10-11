package com.appa.recipeapp.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Возвращает приветствие по времени суток.
 * Работает одинаково на Android и iOS.
 */
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

/**
 * Номер дня года — используется для «цитата дня» и «анекдот дня».
 * Один и тот же в течение суток, меняется в полночь.
 */
fun dayOfYear(): Int {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return now.dayOfYear
}