package com.appa.recipeapp.data

import com.appa.recipeapp.utils.currentTimeOfDay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DailyQuote(
    val text: String,
    val author: String,
)

object QuoteRepository {

    private val quotes: List<DailyQuote> = listOf(
        DailyQuote("Готовить может каждый", "Auguste Gusteau"),
        DailyQuote("Если ты всё время будешь оглядываться назад, то не сможешь увидеть, что лежит впереди...", "Auguste Gusteau"),
        DailyQuote("Если мы то, что мы едим, то мусор я есть не буду", "Auguste Gusteau"),
        DailyQuote("Ты не должен позволять другим определять границы твоих возможностей только из-за твоего происхождения. Твоя единственная граница — твоя душа", "Auguste Gusteau"),
        DailyQuote("Только бесстрашные могут быть великими", "Auguste Gusteau"),
        DailyQuote("Если ты сосредоточишься на том, что осталось в прошлом, ты никогда не сможешь узнать, что тебя ждёт в будущем!", "Auguste Gusteau"),
        DailyQuote("Но что я могу сделать? Я лишь плод твоего воображения", "Auguste Gusteau"),
    )

    fun getToday(): DailyQuote {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val days = now.date.toEpochDays().toInt()
        val timeSlot = currentTimeOfDay().ordinal
        val index = ((days * 4 + timeSlot) % quotes.size + quotes.size) % quotes.size
        return quotes[index]
    }
}