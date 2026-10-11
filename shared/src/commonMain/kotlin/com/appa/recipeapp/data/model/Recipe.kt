package com.appa.recipeapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String = "",
    val amount: String = "",
    val unit: String = "",
)

@Serializable
data class RecipeStep(
    val text: String = "",
    val photoUrl: String? = null,
)

@Serializable
data class Rating(
    val userId: String = "",
    val userName: String = "",
    val stars: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L,
)

@Serializable
data class MusicTrack(
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
)

@Serializable
data class Recipe(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<RecipeStep> = emptyList(),
    val tags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(),
    val ratings: List<Rating> = emptyList(),
    val cookingTimeMinutes: Int = 0,
    val createdAt: Long = 0L,
    val isFavorite: Boolean = false,
    val musicNote: String = "",
    val musicTracks: List<MusicTrack> = emptyList(),
    /** Средний рейтинг из view `recipes_with_authors`. 0.0 если оценок нет. */
    val averageRating: Double = 0.0,
    /** Количество оценок из view `recipes_with_authors`. 0 если нет. */
    val ratingsCount: Int = 0,
    /** Оценка популярности из view `recipes_weekly`. 0.0 если нет недельной активности. */
    val popularityScore: Double = 0.0,
)

fun Recipe.averageRating(): Double {
    if (ratingsCount > 0) return averageRating
    if (ratings.isEmpty()) return 0.0
    return ratings.map { it.stars }.average()
}

fun Recipe.ratingCount(): Int {
    return if (ratingsCount > 0) ratingsCount else ratings.size
}