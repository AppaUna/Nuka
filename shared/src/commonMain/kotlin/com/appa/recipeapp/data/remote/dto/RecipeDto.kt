package com.appa.recipeapp.data.remote.dto

import androidx.compose.runtime.Immutable
import com.appa.recipeapp.data.model.Ingredient
import com.appa.recipeapp.data.model.MusicTrack
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.data.model.RecipeStep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

@Immutable
@Serializable
data class RecipeDto(
    val id: String = "",
    @SerialName("author_id")
    val authorId: String = "",
    @SerialName("author_name")
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    @SerialName("cooking_time_minutes")
    val cookingTimeMinutes: Int = 0,
    val ingredients: JsonElement? = null,
    val steps: JsonElement? = null,
    val tags: List<String> = emptyList(),
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("music_note")
    val musicNote: String = "",
    @SerialName("music_tracks")
    val musicTracks: JsonElement? = null,
    val notes: String = "",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("avg_rating")
    val avgRating: Double = 0.0,
    @SerialName("ratings_count")
    val ratingsCount: Int = 0,
    @SerialName("popularity_score")
    val popularityScore: Double = 0.0,
)

@Immutable
@Serializable
data class RecipeInsertDto(
    @SerialName("author_id")
    val authorId: String,
    val title: String,
    val description: String,
    @SerialName("cooking_time_minutes")
    val cookingTimeMinutes: Int,
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
    val tags: List<String>,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("music_note")
    val musicNote: String,
    @SerialName("music_tracks")
    val musicTracks: List<MusicTrack>,
    val notes: String = "",
)

@Immutable
@Serializable
data class RecipeUpdateDto(
    val title: String,
    val description: String,
    @SerialName("cooking_time_minutes")
    val cookingTimeMinutes: Int,
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
    val tags: List<String>,
    @SerialName("photo_url")
    val photoUrl: String = "",
    @SerialName("music_note")
    val musicNote: String,
    @SerialName("music_tracks")
    val musicTracks: List<MusicTrack>,
    val notes: String = "",
)

@Immutable
@Serializable
data class RatingDto(
    val id: String = "",
    @SerialName("recipe_id")
    val recipeId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val stars: Int = 0,
    val comment: String = "",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_name")
    val userName: String? = null,
)

private val dtoJson = Json { ignoreUnknownKeys = true }

/** Размер превью для карточек — 512px достаточно на любом экране. */
private const val PREVIEW_SIZE = 512

/**
 * Преобразует оригинальный URL фото в URL Supabase Storage Transform.
 *
 * Работает только для Supabase Storage public URL. Если URL ведёт на другой
 * домен или уже является render-URL — возвращается как есть.
 *
 * Формат Supabase Storage Transform:
 *   /storage/v1/object/public/<bucket>/<file>
 *   → /storage/v1/render/image/public/<bucket>/<file>?width=...&height=...&resize=cover&quality=...
 *
 * Под капотом запрос уходит в imgproxy, который уменьшает фото на лету.
 */
private fun transformPhotoUrl(originalUrl: String): String {
    val marker = "/storage/v1/object/public/"
    if (!originalUrl.contains(marker)) return originalUrl

    val renderUrl = originalUrl.replace(marker, "/storage/v1/render/image/public/")
    return "$renderUrl?width=$PREVIEW_SIZE&height=$PREVIEW_SIZE&resize=cover&quality=80"
}

fun RecipeDto.toDomain(favoriteIds: Set<String> = emptySet()): Recipe = Recipe(
    id = id,
    authorId = authorId,
    authorName = authorName.ifBlank { "Аноним" },
    title = title,
    description = description,
    ingredients = parseIngredients(ingredients),
    steps = parseSteps(steps),
    tags = tags,
    photoUrls = listOfNotNull(
        photoUrl?.takeIf { it.isNotBlank() }?.let(::transformPhotoUrl)
    ),
    ratings = emptyList(),
    cookingTimeMinutes = cookingTimeMinutes,
    createdAt = 0L,
    isFavorite = id in favoriteIds,
    musicNote = musicNote,
    musicTracks = parseMusicTracks(musicTracks),
    notes = notes,
    averageRating = avgRating,
    ratingsCount = ratingsCount,
    popularityScore = popularityScore,
)

fun Recipe.toInsertDto(authorId: String): RecipeInsertDto = RecipeInsertDto(
    authorId = authorId,
    title = title,
    description = description,
    cookingTimeMinutes = cookingTimeMinutes,
    ingredients = ingredients,
    steps = steps,
    tags = tags,
    photoUrl = photoUrls.firstOrNull(),
    musicNote = musicNote,
    musicTracks = musicTracks,
    notes = notes,
)

fun Recipe.toUpdateDto(): RecipeUpdateDto = RecipeUpdateDto(
    title = title,
    description = description,
    cookingTimeMinutes = cookingTimeMinutes,
    ingredients = ingredients,
    steps = steps,
    tags = tags,
    photoUrl = photoUrls.firstOrNull() ?: "",
    musicNote = musicNote,
    musicTracks = musicTracks,
    notes = notes,
)

private fun parseIngredients(element: JsonElement?): List<Ingredient> {
    if (element == null || element !is JsonArray) return emptyList()
    return try {
        element.map { dtoJson.decodeFromJsonElement(Ingredient.serializer(), it) }
    } catch (e: Throwable) {
        println("DEBUG parseIngredients failed: ${e.message}")
        emptyList()
    }
}

private fun parseSteps(element: JsonElement?): List<RecipeStep> {
    if (element == null || element !is JsonArray) return emptyList()
    return try {
        element.map { dtoJson.decodeFromJsonElement(RecipeStep.serializer(), it) }
    } catch (e: Throwable) {
        println("DEBUG parseSteps failed: ${e.message}")
        emptyList()
    }
}

private fun parseMusicTracks(element: JsonElement?): List<MusicTrack> {
    if (element == null || element !is JsonArray) return emptyList()
    return try {
        element.map { dtoJson.decodeFromJsonElement(MusicTrack.serializer(), it) }
    } catch (e: Throwable) {
        println("DEBUG parseMusicTracks failed: ${e.message}")
        emptyList()
    }
}