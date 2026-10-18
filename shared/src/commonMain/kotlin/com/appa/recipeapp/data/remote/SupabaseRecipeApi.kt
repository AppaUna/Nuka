package com.appa.recipeapp.data.remote

import com.appa.recipeapp.data.remote.dto.RatingDto
import com.appa.recipeapp.data.remote.dto.RecipeDto
import com.appa.recipeapp.data.remote.dto.RecipeInsertDto
import com.appa.recipeapp.data.remote.dto.RecipeUpdateDto
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.appa.recipeapp.data.remote.dto.ProfileDto

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ApiResult<Nothing>()
}

@Serializable
private data class GetRecipesRandomRequest(
    val seed: Long,
    @SerialName("page_limit") val pageLimit: Int,
    @SerialName("page_offset") val pageOffset: Int,
)

@Serializable
private data class GetRecipesByTagRandomRequest(
    val tag: String,
    val seed: Long,
    @SerialName("page_limit") val pageLimit: Int,
    @SerialName("page_offset") val pageOffset: Int,
)

class SupabaseRecipeApi(private val client: SupabaseClient) {

    /**
     * Страница рецептов в СЛУЧАЙНОМ порядке (стабильно для одного seed).
     */
    suspend fun getRecipesPage(seed: Long, limit: Int = 20, offset: Int = 0): ApiResult<List<RecipeDto>> {
        return try {
            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/rpc/get_recipes_random",
            ) {
                headers {
                    client.anonHeaders().forEach { (key, value) -> header(key, value) }
                }
                contentType(ContentType.Application.Json)
                setBody(
                    GetRecipesRandomRequest(
                        seed = seed,
                        pageLimit = limit,
                        pageOffset = offset,
                    )
                )
            }
            if (response.status.value in 200..299) {
                ApiResult.Success(response.body())
            } else {
                val errorText = response.body<String>()
                println("DEBUG getRecipesPage error: ${response.status} — $errorText")
                ApiResult.Error("HTTP ${response.status}")
            }
        } catch (e: Throwable) {
            println("DEBUG getRecipesPage EXCEPTION: ${e::class.simpleName} — ${e.message}")
            ApiResult.Error(e.message ?: "Network error", e)
        }
    }

    /**
     * Страница рецептов по тегу в СЛУЧАЙНОМ порядке.
     */
    suspend fun getRecipesByTagPage(
        tag: String,
        seed: Long,
        limit: Int = 10,
        offset: Int = 0,
    ): ApiResult<List<RecipeDto>> {
        return try {
            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/rpc/get_recipes_by_tag_random",
            ) {
                headers {
                    client.anonHeaders().forEach { (key, value) -> header(key, value) }
                }
                contentType(ContentType.Application.Json)
                setBody(
                    GetRecipesByTagRandomRequest(
                        tag = tag,
                        seed = seed,
                        pageLimit = limit,
                        pageOffset = offset,
                    )
                )
            }
            if (response.status.value in 200..299) {
                ApiResult.Success(response.body())
            } else {
                val errorText = response.body<String>()
                println("DEBUG getRecipesByTagPage error: ${response.status} — $errorText")
                ApiResult.Error("HTTP ${response.status}")
            }
        } catch (e: Throwable) {
            println("DEBUG getRecipesByTagPage EXCEPTION: ${e::class.simpleName} — ${e.message}")
            ApiResult.Error(e.message ?: "Network error", e)
        }
    }

    /**
     * Weekly — популярное (не случайное, сортировка по popularity).
     */
    suspend fun getWeeklyRecipes(limit: Int = 6): ApiResult<List<RecipeDto>> {
        return try {
            val response: HttpResponse = client.http.get(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/recipes_weekly",
            ) {
                parameter("select", "*")
                parameter("order", "popularity_score.desc")
                parameter("limit", limit)
                headers {
                    client.anonHeaders().forEach { (key, value) -> header(key, value) }
                }
            }
            if (response.status.value in 200..299) {
                ApiResult.Success(response.body())
            } else {
                val errorText = response.body<String>()
                println("DEBUG getWeeklyRecipes error: ${response.status} — $errorText")
                ApiResult.Error("HTTP ${response.status}")
            }
        } catch (e: Throwable) {
            println("DEBUG getWeeklyRecipes EXCEPTION: ${e::class.simpleName} — ${e.message}")
            ApiResult.Error(e.message ?: "Network error", e)
        }
    }

    suspend fun saveRecipe(accessToken: String, dto: RecipeInsertDto): RecipeDto? {
        return try {
            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/recipes",
            ) {
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                    header("Prefer", "return=representation")
                }
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            if (response.status.value in 200..299) {
                val list: List<RecipeDto> = response.body()
                list.firstOrNull()
            } else {
                val errorText = response.body<String>()
                println("DEBUG saveRecipe error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG saveRecipe EXCEPTION: ${e::class.simpleName} — ${e.message}")
            null
        }
    }

    suspend fun updateRecipe(accessToken: String, recipeId: String, dto: RecipeUpdateDto): RecipeDto? {
        return try {
            val response: HttpResponse = client.http.patch(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/recipes",
            ) {
                parameter("id", "eq.$recipeId")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                    header("Prefer", "return=representation")
                }
                contentType(ContentType.Application.Json)
                setBody(dto)
            }
            if (response.status.value in 200..299) {
                val list: List<RecipeDto> = response.body()
                list.firstOrNull()
            } else {
                val errorText = response.body<String>()
                println("DEBUG updateRecipe error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG updateRecipe EXCEPTION: ${e::class.simpleName} — ${e.message}")
            null
        }
    }

    suspend fun deleteRecipe(accessToken: String, recipeId: String): Boolean {
        return try {
            val response: HttpResponse = client.http.delete(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/recipes",
            ) {
                parameter("id", "eq.$recipeId")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                }
            }
            response.status.value in 200..299
        } catch (e: Throwable) {
            println("DEBUG deleteRecipe EXCEPTION: ${e::class.simpleName} — ${e.message}")
            false
        }
    }

    // ─── Favorites ───

    suspend fun getFavoriteRecipeIds(accessToken: String, userId: String): Set<String> {
        return try {
            val response: HttpResponse = client.http.get(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/favorites",
            ) {
                parameter("user_id", "eq.$userId")
                parameter("select", "recipe_id")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                }
            }
            if (response.status.value in 200..299) {
                val rows: List<FavoriteRow> = response.body()
                rows.map { it.recipeId }.toSet()
            } else emptySet()
        } catch (e: Throwable) {
            emptySet()
        }
    }

    suspend fun addFavorite(accessToken: String, userId: String, recipeId: String): Boolean {
        return try {
            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/favorites",
            ) {
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                    header("Prefer", "resolution=merge-duplicates,return=representation")
                }
                contentType(ContentType.Application.Json)
                setBody(FavoriteInsert(userId = userId, recipeId = recipeId))
            }
            val bodyText = try { response.body<String>() } catch (e: Throwable) { "<no body>" }
            println("DEBUG addFavorite: status=${response.status}, body=$bodyText")
            response.status.value in 200..299
        } catch (e: Throwable) {
            println("DEBUG addFavorite EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFavorite(accessToken: String, userId: String, recipeId: String): Boolean {
        return try {
            val response: HttpResponse = client.http.delete(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/favorites",
            ) {
                parameter("user_id", "eq.$userId")
                parameter("recipe_id", "eq.$recipeId")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                }
            }
            val bodyText = try { response.body<String>() } catch (e: Throwable) { "<no body>" }
            println("DEBUG removeFavorite: status=${response.status}, body=$bodyText")
            response.status.value in 200..299
        } catch (e: Throwable) {
            println("DEBUG removeFavorite EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // ─── Ratings ───

    suspend fun getRatingsForRecipe(recipeId: String): List<RatingDto> {
        return try {
            val response: HttpResponse = client.http.get(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/ratings_with_users",
            ) {
                parameter("recipe_id", "eq.$recipeId")
                parameter("select", "*")
                parameter("order", "created_at.desc")
                headers {
                    client.anonHeaders().forEach { (key, value) -> header(key, value) }
                }
            }
            if (response.status.value in 200..299) response.body() else emptyList()
        } catch (e: Throwable) {
            println("DEBUG getRatingsForRecipe EXCEPTION: ${e::class.simpleName} — ${e.message}")
            emptyList()
        }
    }

    suspend fun getMyRatingsCount(accessToken: String, userId: String): Int {
        return try {
            val response: HttpResponse = client.http.get(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/ratings",
            ) {
                parameter("user_id", "eq.$userId")
                parameter("select", "id")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                }
            }
            if (response.status.value in 200..299) {
                val rows: List<RatingIdRow> = response.body()
                rows.size
            } else 0
        } catch (e: Throwable) {
            println("DEBUG getMyRatingsCount EXCEPTION: ${e::class.simpleName} — ${e.message}")
            0
        }
    }

    suspend fun upsertRating(
        accessToken: String,
        recipeId: String,
        userId: String,
        stars: Int,
        comment: String,
    ): RatingDto? {
        return try {
            val body = RatingInsert(
                recipeId = recipeId,
                userId = userId,
                stars = stars,
                comment = comment,
            )
            val response: HttpResponse = client.http.post(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/ratings",
            ) {
                parameter("on_conflict", "recipe_id,user_id")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                    header("Prefer", "resolution=merge-duplicates,return=representation")
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            if (response.status.value in 200..299) {
                val list: List<RatingDto> = response.body()
                list.firstOrNull()
            } else {
                val errorText = response.body<String>()
                println("DEBUG upsertRating error: ${response.status} — $errorText")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG upsertRating EXCEPTION: ${e::class.simpleName} — ${e.message}")
            null
        }
    }

    /**
     * Удалить оценку текущего пользователя у рецепта.
     * Возвращает true при успехе
     */
    suspend fun deleteRating(accessToken: String, recipeId: String, userId: String): Boolean {
        return try {
            val response: HttpResponse = client.http.delete(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/ratings",
            ) {
                parameter("recipe_id", "eq.$recipeId")
                parameter("user_id", "eq.$userId")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) -> header(key, value) }
                }
            }
            val bodyText = try { response.body<String>() } catch (e: Throwable) { "<no body>" }
            println("DEBUG deleteRating: status=${response.status}, body=$bodyText")
            // 200/204 — успех, 404 — оценки и не было (тоже ок)
            response.status.value in 200..299 || response.status.value == 404
        } catch (e: Throwable) {
            println("DEBUG deleteRating EXCEPTION: ${e::class.simpleName} — ${e.message}")
            false
        }
    }

    suspend fun fetchSpotifyThumbnail(spotifyUrl: String): String? {
        return try {
            val response: HttpResponse = client.http.get(
                "https://open.spotify.com/oembed",
            ) {
                parameter("url", spotifyUrl)
                headers {
                    header("Accept", "application/json")
                }
            }
            if (response.status.value in 200..299) {
                @kotlinx.serialization.Serializable
                data class OEmbedResponse(
                    @kotlinx.serialization.SerialName("thumbnail_url")
                    val thumbnailUrl: String? = null,
                )
                val data: OEmbedResponse = response.body()
                data.thumbnailUrl
            } else {
                println("DEBUG fetchSpotifyThumbnail error: ${response.status}")
                null
            }
        } catch (e: Throwable) {
            println("DEBUG fetchSpotifyThumbnail EXCEPTION: ${e::class.simpleName} — ${e.message}")
            null
        }
    }

    // ─── Profiles / Avatars ───

    suspend fun getProfiles(): List<ProfileDto> {
        return try {
            val response: HttpResponse = client.http.get(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/profiles",
            ) {
                parameter("select", "id,avatar_url,display_name,name_color")
                headers {
                    client.anonHeaders().forEach { (key, value) -> header(key, value) }
                }
            }
            if (response.status.value in 200..299) response.body() else emptyList()
        } catch (e: Throwable) {
            emptyList()
        }
    }

    suspend fun updateProfileAvatar(accessToken: String, userId: String, avatarUrl: String): Boolean {
        return try {
            val body = mapOf("avatar_url" to avatarUrl)
            val response: HttpResponse = client.http.patch(
                "${SupabaseConfig.BASE_URL}${SupabaseConfig.REST_PATH}/profiles",
            ) {
                parameter("id", "eq.$userId")
                headers {
                    client.authHeaders(accessToken).forEach { (key, value) ->
                        println("DEBUG PATCH header: $key = ${value.take(20)}...")
                        header(key, value)
                    }
                    header("Prefer", "return=representation")
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            val bodyText = try { response.body<String>() } catch (e: Throwable) { "<empty>" }
            println("DEBUG updateProfileAvatar: status=${response.status}, body=$bodyText")
            response.status.value in 200..299
        } catch (e: Throwable) {
            println("DEBUG updateProfileAvatar EXCEPTION: ${e::class.simpleName} — ${e.message}")
            false
        }
    }
}

@Serializable
private data class FavoriteRow(
    @SerialName("recipe_id")
    val recipeId: String,
)

@Serializable
private data class FavoriteInsert(
    @SerialName("user_id")
    val userId: String,
    @SerialName("recipe_id")
    val recipeId: String,
)

@Serializable
private data class RatingInsert(
    @SerialName("recipe_id")
    val recipeId: String,
    @SerialName("user_id")
    val userId: String,
    val stars: Int,
    val comment: String,
)

@Serializable
private data class RatingIdRow(
    val id: String,
)