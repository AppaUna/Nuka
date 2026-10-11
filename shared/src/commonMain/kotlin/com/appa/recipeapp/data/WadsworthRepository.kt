package com.appa.recipeapp.data

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class WadsworthJoke(
    val id: Int,
    val textEn: String,
    val audioFile: String,   // имя файла в composeResources/files/jokes/
)

object WadsworthRepository {

    private val jokes = listOf(
        WadsworthJoke(
            id = 1,
            textEn = "Two cannibals are eating a clown. One turns to the other and says, \"Does this taste funny to you?\"",
            audioFile = "joke_1.mp4",
        ),
        WadsworthJoke(
            id = 2,
            textEn = "Two atoms are sitting in a bar. One says to the other, \"I think I've lost an electron.\" The other asks \"Are you sure?\" To which the first replies, \"I'm positive.\"",
            audioFile = "joke_2.mp4",
        ),
        WadsworthJoke(
            id = 3,
            textEn = "A neutron walks into a bar and asks the bartender, \"How much for a drink?\" To which the bartender replies, \"For you, no charge!\"",
            audioFile = "joke_3.mp4",
        ),
        WadsworthJoke(
            id = 4,
            textEn = "I once visited a crematorium where they gave discounts to burn victims.",
            audioFile = "joke_4.mp4",
        ),
        WadsworthJoke(
            id = 5,
            textEn = "I was going to attend the clairvoyant's meeting, but it was canceled due to unforeseen events.",
            audioFile = "joke_5.mp4",
        ),
        WadsworthJoke(
            id = 6,
            textEn = "It's common knowledge that irradiated cats have 18 half-lives.",
            audioFile = "joke_6.mp4",
        ),
        WadsworthJoke(
            id = 7,
            textEn = "You know, the best contraceptive for old people is nudity.",
            audioFile = "joke_7.mp4",
        ),
        WadsworthJoke(
            id = 8,
            textEn = "War does not determine who is right, only who is left.",
            audioFile = "joke_8.mp4",
        ),
        WadsworthJoke(
            id = 9,
            textEn = "I'm reading a book about anti-gravity. It's impossible to put down.",
            audioFile = "joke_9.mp4",
        ),
        WadsworthJoke(
            id = 10,
            textEn = "Photons have mass? I didn't even know they were Catholic.",
            audioFile = "joke_10.mp4",
        ),
    )

    fun getCurrentJoke(): WadsworthJoke {
        val index = (getCurrentSlotId() % jokes.size + jokes.size) % jokes.size
        return jokes[index]
    }

    fun getCurrentSlotId(): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val days = now.date.toEpochDays().toInt()
        val quarterOfDay = now.hour / 6
        return days * 4 + quarterOfDay
    }
}