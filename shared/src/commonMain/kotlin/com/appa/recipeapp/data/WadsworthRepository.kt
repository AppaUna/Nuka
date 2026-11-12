package com.appa.recipeapp.data

import com.appa.recipeapp.utils.currentTimeOfDay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class WadsworthJoke(
    val id: Int,
    val textEn: String,
    val audioFile: String,
)

object WadsworthRepository {

    private val jokes = listOf(
        WadsworthJoke(1, "Two cannibals are eating a clown. One turns to the other and says, \"Does this taste funny to you?\"", "joke_1.mp4"),
        WadsworthJoke(2, "Two atoms are sitting in a bar. One says to the other, \"I think I've lost an electron.\" The other asks \"Are you sure?\" To which the first replies, \"I'm positive.\"", "joke_2.mp4"),
        WadsworthJoke(3, "A neutron walks into a bar and asks the bartender, \"How much for a drink?\" To which the bartender replies, \"For you, no charge!\"", "joke_3.mp4"),
        WadsworthJoke(4, "I once visited a crematorium where they gave discounts to burn victims.", "joke_4.mp4"),
        WadsworthJoke(5, "I was going to attend the clairvoyant's meeting, but it was canceled due to unforeseen events.", "joke_5.mp4"),
        WadsworthJoke(6, "It's common knowledge that irradiated cats have 18 half-lives.", "joke_6.mp4"),
        WadsworthJoke(7, "You know, the best contraceptive for old people is nudity.", "joke_7.mp4"),
        WadsworthJoke(8, "War does not determine who is right, only who is left.", "joke_8.mp4"),
        WadsworthJoke(9, "I'm reading a book about anti-gravity. It's impossible to put down.", "joke_9.mp4"),
        WadsworthJoke(10, "Photons have mass? I didn't even know they were Catholic.", "joke_10.mp4"),
    )

    fun getCurrentJoke(): WadsworthJoke {
        val index = (getCurrentSlotId() % jokes.size + jokes.size) % jokes.size
        return jokes[index]
    }

    fun getCurrentSlotId(): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val days = now.date.toEpochDays().toInt()
        val timeSlot = currentTimeOfDay().ordinal
        return days * 4 + timeSlot
    }
}