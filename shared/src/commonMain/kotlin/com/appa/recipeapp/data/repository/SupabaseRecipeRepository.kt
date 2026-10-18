package com.appa.recipeapp.data.repository

import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.data.model.currentTimeMillis
import com.appa.recipeapp.data.remote.ApiResult
import com.appa.recipeapp.data.remote.SupabaseRecipeApi
import com.appa.recipeapp.data.remote.SupabaseStorageApi
import com.appa.recipeapp.data.remote.dto.toDomain
import com.appa.recipeapp.data.remote.dto.toInsertDto
import com.appa.recipeapp.data.remote.dto.toUpdateDto
import com.appa.recipeapp.platform.readFileBytes
import com.appa.recipeapp.utils.fuzzyMatches
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.appa.recipeapp.data.model.UserSession
import kotlin.concurrent.Volatile

class SupabaseRecipeRepository(
    private val api: SupabaseRecipeApi,
    private val storageApi: SupabaseStorageApi,
    private val authRepository: SupabaseAuthRepository,
) : RecipeRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ─── Кэши ───
    private val recipesCache = MutableStateFlow<List<Recipe>>(emptyList())
    private val weeklyRecipesCache = MutableStateFlow<List<Recipe>>(emptyList())
    private val favoriteIdsCache = MutableStateFlow<Set<String>>(emptySet())

    // Общий кэш всех аватарок по authorId
    private val avatarCache = MutableStateFlow<Map<String, String?>>(emptyMap())

    // Публичный стейт для текущего пользователя (чтобы ProfileViewModel мог подписаться)
    private val _myAvatarState = MutableStateFlow<String?>(null)
    val myAvatarState: StateFlow<String?> = _myAvatarState.asStateFlow()

    // ─── Кэш для количества оценок ───
    private val ratingsCountCache = MutableStateFlow<Int?>(null)
    val myRatingsCountState: StateFlow<Int?> = ratingsCountCache.asStateFlow()

    // ─── Флаги состояния mainFeed ───
    private var mainFeedOffset = 0
    private var isEndReached = false
    private var isLoadingMore = false
    private var isRefreshingMain = false

    private val _isMainFeedLoadingMore = MutableStateFlow(false)
    val isMainFeedLoadingMore: StateFlow<Boolean> = _isMainFeedLoadingMore.asStateFlow()

    private val _isMainFeedEndReached = MutableStateFlow(false)
    val isMainFeedEndReached: StateFlow<Boolean> = _isMainFeedEndReached.asStateFlow()

    // ─── Флаги для weekly ───
    private var isWeeklyRefreshing = false
    private val _isWeeklyLoaded = MutableStateFlow(false)
    val isWeeklyLoaded: StateFlow<Boolean> = _isWeeklyLoaded.asStateFlow()

    // ─── Флаги для секций ───
    private val breakfastCache = MutableStateFlow<List<Recipe>>(emptyList())
    private val lunchCache = MutableStateFlow<List<Recipe>>(emptyList())
    private val dinnerCache = MutableStateFlow<List<Recipe>>(emptyList())
    private var isSectionsRefreshing = false
    private val _isSectionsLoaded = MutableStateFlow(false)
    val isSectionsLoaded: StateFlow<Boolean> = _isSectionsLoaded.asStateFlow()

    // ─── Общие флаги ───
    private val _isRecipesLoaded = MutableStateFlow(false)
    val isRecipesLoaded: StateFlow<Boolean> = _isRecipesLoaded.asStateFlow()

    private val _hasNetworkError = MutableStateFlow(false)
    val hasNetworkError: StateFlow<Boolean> = _hasNetworkError.asStateFlow()

    private val MAIN_FEED_PAGE_SIZE = 20
    private val SECTION_PAGE_SIZE = 10
    private val WEEKLY_LIMIT = 6

    private val randomSeed: Long = currentTimeMillis()

    init {
        scope.launch {
            var isFirstEmission = true
            authRepository.session.collect { session ->
                if (isFirstEmission) {
                    isFirstEmission = false
                    lastRefreshTimeMs = currentTimeMillis()
                    if (session != null) {
                        refreshFavoritesSuspend()
                        refreshRatingsCountSuspend()
                        refreshProfilesSuspend()
                    }
                    refreshMainFeedSuspend(reset = true)
                    refreshWeeklySuspend()
                    refreshSectionsSuspend()
                } else {
                    if (session != null) {
                        refreshFavoritesSuspend()
                        refreshRatingsCountSuspend()
                        refreshProfilesSuspend()
                    } else {
                        clearAllFavoriteFlags()
                        ratingsCountCache.value = null
                        avatarCache.value = emptyMap()
                        _myAvatarState.value = null
                    }
                }
            }
        }

        // Подписываемся на изменения avatarCache, чтобы обновлять аватарку текущего юзера
        scope.launch {
            avatarCache.collect { map ->
                val userId = authRepository.getUserId()
                _myAvatarState.value = map[userId]
            }
        }
    }

    private suspend fun refreshRatingsCountSuspend() {
        val session = authRepository.ensureValidSession() ?: return
        try {
            val count = api.getMyRatingsCount(session.accessToken, session.userId)
            ratingsCountCache.value = count
        } catch (e: Throwable) {}
    }

    private suspend fun refreshProfilesSuspend() {
        try {
            val profiles = api.getProfiles()
            println("DEBUG refreshProfiles: получил ${profiles.size} профилей")
            val newMap = profiles.associate { it.id to it.avatarUrl }
            println("DEBUG refreshProfiles: map = $newMap")
            if (newMap == avatarCache.value) return
            avatarCache.value = newMap
            recipesCache.value = recipesCache.value.reapplyAvatars(newMap)
            // ...
        } catch (e: Throwable) {
            println("DEBUG refreshProfiles EXCEPTION: ${e::class.simpleName} — ${e.message}")
        }
    }

    private fun List<Recipe>.reapplyAvatars(map: Map<String, String?>): List<Recipe> =
        map { r ->
            val avatar = map[r.authorId]
            if (r.authorAvatarUrl == avatar) r else r.copy(authorAvatarUrl = avatar)
        }

    private fun mapDto(dto: com.appa.recipeapp.data.remote.dto.RecipeDto): Recipe {
        val avatar = avatarCache.value[dto.authorId]
        return dto.toDomain(favoriteIdsCache.value, avatarOverride = avatar)
    }

    private fun mergeRecipes(old: List<Recipe>, new: List<Recipe>): List<Recipe> {
        val oldById = old.associateBy { it.id }
        return new.map { newItem ->
            val oldItem = oldById[newItem.id]
            if (oldItem != null && oldItem == newItem) oldItem else newItem
        }
    }

    private fun clearAllFavoriteFlags() {
        favoriteIdsCache.value = emptySet()
        recipesCache.value = recipesCache.value.clearFavoriteFlags()
        weeklyRecipesCache.value = weeklyRecipesCache.value.clearFavoriteFlags()
        breakfastCache.value = breakfastCache.value.clearFavoriteFlags()
        lunchCache.value = lunchCache.value.clearFavoriteFlags()
        dinnerCache.value = dinnerCache.value.clearFavoriteFlags()
    }

    private fun List<Recipe>.clearFavoriteFlags(): List<Recipe> =
        map { if (it.isFavorite) it.copy(isFavorite = false) else it }

    private fun applyFavoriteFlags(ids: Set<String>) {
        recipesCache.value = recipesCache.value.applyFavoriteFlags(ids)
        weeklyRecipesCache.value = weeklyRecipesCache.value.applyFavoriteFlags(ids)
        breakfastCache.value = breakfastCache.value.applyFavoriteFlags(ids)
        lunchCache.value = lunchCache.value.applyFavoriteFlags(ids)
        dinnerCache.value = dinnerCache.value.applyFavoriteFlags(ids)
    }

    private fun List<Recipe>.applyFavoriteFlags(ids: Set<String>): List<Recipe> =
        map { r ->
            val shouldBeFav = r.id in ids
            if (r.isFavorite == shouldBeFav) r else r.copy(isFavorite = shouldBeFav)
        }

    private suspend fun refreshMainFeedSuspend(reset: Boolean): Boolean {
        if (reset) {
            if (isRefreshingMain) return true
            isRefreshingMain = true
            mainFeedOffset = 0
            isEndReached = false
        } else {
            if (isLoadingMore || isEndReached) return true
            isLoadingMore = true
            _isMainFeedLoadingMore.value = true
        }

        try {
            val offset = if (reset) 0 else mainFeedOffset
            return when (val result = api.getRecipesPage(seed = randomSeed, limit = MAIN_FEED_PAGE_SIZE, offset = offset)) {
                is ApiResult.Success -> {
                    val newList = result.data.map { mapDto(it) }
                    if (reset) {
                        recipesCache.value = newList
                    } else {
                        val existingIds = recipesCache.value.map { it.id }.toSet()
                        val toAdd = newList.filter { it.id !in existingIds }
                        recipesCache.value = recipesCache.value + toAdd
                        mainFeedOffset += newList.size
                    }
                    if (newList.size < MAIN_FEED_PAGE_SIZE) {
                        isEndReached = true
                        _isMainFeedEndReached.value = true
                    }
                    _isRecipesLoaded.value = true
                    _hasNetworkError.value = false
                    true
                }
                is ApiResult.Error -> {
                    _hasNetworkError.value = true
                    _isRecipesLoaded.value = true
                    false
                }
            }
        } finally {
            if (reset) isRefreshingMain = false else {
                isLoadingMore = false
                _isMainFeedLoadingMore.value = false
            }
        }
    }

    suspend fun loadMoreMainFeed() {
        refreshMainFeedSuspend(reset = false)
    }

    private suspend fun refreshWeeklySuspend(): Boolean {
        if (isWeeklyRefreshing) return true
        isWeeklyRefreshing = true
        try {
            return when (val result = api.getWeeklyRecipes(limit = WEEKLY_LIMIT)) {
                is ApiResult.Success -> {
                    val newList = result.data.map { mapDto(it) }
                    weeklyRecipesCache.value = mergeRecipes(weeklyRecipesCache.value, newList)
                    _isWeeklyLoaded.value = true
                    true
                }
                is ApiResult.Error -> {
                    _isWeeklyLoaded.value = true
                    false
                }
            }
        } finally {
            isWeeklyRefreshing = false
        }
    }

    private suspend fun refreshSectionsSuspend() {
        if (isSectionsRefreshing) return
        isSectionsRefreshing = true
        try {
            coroutineScope {
                val breakfastJob = async {
                    when (val r = api.getRecipesByTagPage(tag = "завтрак", seed = randomSeed, limit = SECTION_PAGE_SIZE)) {
                        is ApiResult.Success -> breakfastCache.value = r.data.map { mapDto(it) }
                        is ApiResult.Error -> Unit
                    }
                }
                val lunchJob = async {
                    when (val r = api.getRecipesByTagPage(tag = "обед", seed = randomSeed, limit = SECTION_PAGE_SIZE)) {
                        is ApiResult.Success -> lunchCache.value = r.data.map { mapDto(it) }
                        is ApiResult.Error -> Unit
                    }
                }
                val dinnerJob = async {
                    when (val r = api.getRecipesByTagPage(tag = "ужин", seed = randomSeed, limit = SECTION_PAGE_SIZE)) {
                        is ApiResult.Success -> dinnerCache.value = r.data.map { mapDto(it) }
                        is ApiResult.Error -> Unit
                    }
                }
                breakfastJob.await()
                lunchJob.await()
                dinnerJob.await()
            }
            _isSectionsLoaded.value = true
        } finally {
            isSectionsRefreshing = false
        }
    }

    private suspend fun refreshFavoritesSuspend() {
        val session = authRepository.ensureValidSession() ?: return
        try {
            val ids = api.getFavoriteRecipeIds(session.accessToken, session.userId)
            favoriteIdsCache.value = ids
            applyFavoriteFlags(ids)
        } catch (e: Throwable) {}
    }

    private suspend fun uploadPhotoIfNeeded(
        accessToken: String,
        localUri: String?,
    ): String? {
        if (localUri.isNullOrBlank()) return null
        if (localUri.startsWith("http://") || localUri.startsWith("https://")) return localUri

        val bytes = readFileBytes(localUri) ?: return null

        val ext = when {
            localUri.contains(".png", ignoreCase = true) -> "png"
            localUri.contains(".webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        val randomName = "${currentTimeMillis()}_${(0..9999).random()}.$ext"

        val contentType = when (ext) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }

        return storageApi.uploadPhoto(accessToken, randomName, bytes, contentType)
    }

    suspend fun uploadPhotoBytes(
        accessToken: String,
        bytes: ByteArray,
        ext: String = "jpg",
    ): String? {
        val randomName = "${currentTimeMillis()}_${(0..9999).random()}.$ext"
        val contentType = when (ext) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        return storageApi.uploadPhoto(accessToken, randomName, bytes, contentType)
    }

    suspend fun uploadAvatar(bytes: ByteArray, ext: String = "jpg"): String? {
        val session = authRepository.ensureValidSession() ?: return null
        val randomName = "avatar_${session.userId}_${currentTimeMillis()}.$ext"
        val contentType = when (ext) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val publicUrl = storageApi.uploadPhotoToBucket(
            accessToken = session.accessToken,
            bucket = "avatars",
            fileName = randomName,
            bytes = bytes,
            contentType = contentType,
        ) ?: return null

        val ok = api.updateProfileAvatar(session.accessToken, session.userId, publicUrl)
        if (ok) {
            avatarCache.value = avatarCache.value + (session.userId to publicUrl)
            recipesCache.value = recipesCache.value.reapplyAvatars(avatarCache.value)
            weeklyRecipesCache.value = weeklyRecipesCache.value.reapplyAvatars(avatarCache.value)
            breakfastCache.value = breakfastCache.value.reapplyAvatars(avatarCache.value)
            lunchCache.value = lunchCache.value.reapplyAvatars(avatarCache.value)
            dinnerCache.value = dinnerCache.value.reapplyAvatars(avatarCache.value)
        }
        return if (ok) publicUrl else null
    }

    suspend fun uploadAvatar(localUri: String?): String? {
        if (localUri.isNullOrBlank()) return null
        val bytes = readFileBytes(localUri) ?: return null
        val ext = when {
            localUri.contains(".png", ignoreCase = true) -> "png"
            localUri.contains(".webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        return uploadAvatar(bytes, ext)
    }

    suspend fun ensureValidSession(): UserSession? = authRepository.ensureValidSession()

    override fun getMainFeed(tags: Set<String>, query: String): Flow<List<Recipe>> =
        recipesCache.map { list ->
            var filtered = list
            if (tags.isNotEmpty()) {
                filtered = filtered.filter { recipe -> recipe.tags.any { it in tags } }
            }
            if (query.isNotBlank()) {
                filtered = filtered.filter { recipe ->
                    fuzzyMatches(recipe.title, query) ||
                            fuzzyMatches(recipe.authorName, query)
                }
            }
            filtered
        }

    override fun getByTag(tag: String): Flow<List<Recipe>> {
        return when (tag.lowercase()) {
            "завтрак" -> breakfastCache
            "обед" -> lunchCache
            "ужин" -> dinnerCache
            else -> breakfastCache
        }
    }

    override fun getTopRated(limit: Int): Flow<List<Recipe>> =
        weeklyRecipesCache.map { list -> list.take(limit) }

    override fun getRecipe(id: String): Flow<Recipe?> =
        recipesCache.map { list -> list.find { it.id == id } }

    override fun getUserRecipes(userId: String): Flow<List<Recipe>> =
        recipesCache.map { list -> list.filter { it.authorId == userId } }

    override fun getAllTags(): Flow<List<String>> =
        recipesCache.map { list -> list.flatMap { it.tags }.distinct().sorted() }

    override fun getFavorites(): Flow<List<Recipe>> =
        recipesCache.map { list -> list.filter { it.isFavorite } }

    override suspend fun toggleFavorite(recipeId: String) {
        val session = authRepository.ensureValidSession() ?: return
        val isCurrentlyFavorite = recipeId in favoriteIdsCache.value

        val newIds = if (isCurrentlyFavorite) {
            favoriteIdsCache.value - recipeId
        } else {
            favoriteIdsCache.value + recipeId
        }
        favoriteIdsCache.value = newIds

        recipesCache.value = recipesCache.value.toggleFavoriteFor(recipeId, !isCurrentlyFavorite)
        weeklyRecipesCache.value = weeklyRecipesCache.value.toggleFavoriteFor(recipeId, !isCurrentlyFavorite)
        breakfastCache.value = breakfastCache.value.toggleFavoriteFor(recipeId, !isCurrentlyFavorite)
        lunchCache.value = lunchCache.value.toggleFavoriteFor(recipeId, !isCurrentlyFavorite)
        dinnerCache.value = dinnerCache.value.toggleFavoriteFor(recipeId, !isCurrentlyFavorite)

        if (isCurrentlyFavorite) {
            api.removeFavorite(session.accessToken, session.userId, recipeId)
        } else {
            api.addFavorite(session.accessToken, session.userId, recipeId)
        }

        refreshFavoritesSuspend()
    }

    private fun List<Recipe>.toggleFavoriteFor(id: String, favorite: Boolean): List<Recipe> {
        var mutated = false
        val result = map { r ->
            if (r.id == id && r.isFavorite != favorite) {
                mutated = true
                r.copy(isFavorite = favorite)
            } else r
        }
        return if (mutated) result else this
    }

    override suspend fun addRecipe(recipe: Recipe): String {
        val session = authRepository.ensureValidSession() ?: return ""

        val photoUrl = uploadPhotoIfNeeded(session.accessToken, recipe.photoUrls.firstOrNull())
        val recipeWithRemotePhoto = recipe.copy(photoUrls = listOfNotNull(photoUrl))

        val dto = recipeWithRemotePhoto.toInsertDto(authorId = session.userId)
        val created = api.saveRecipe(session.accessToken, dto) ?: return ""

        refreshMainFeedSuspend(reset = true)
        refreshWeeklySuspend()
        refreshSectionsSuspend()
        return created.id
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val session = authRepository.ensureValidSession() ?: return
        if (recipe.id.isBlank()) return

        val photoUrl = uploadPhotoIfNeeded(session.accessToken, recipe.photoUrls.firstOrNull())
        val recipeWithRemotePhoto = recipe.copy(photoUrls = listOfNotNull(photoUrl))

        val dto = recipeWithRemotePhoto.toUpdateDto()
        api.updateRecipe(session.accessToken, recipe.id, dto) ?: return

        refreshMainFeedSuspend(reset = true)
        refreshWeeklySuspend()
        refreshSectionsSuspend()
    }

    override suspend fun deleteRecipe(id: String) {
        val session = authRepository.ensureValidSession() ?: return
        val ok = api.deleteRecipe(session.accessToken, id)
        if (ok) {
            recipesCache.value = recipesCache.value.filterNot { it.id == id }
            weeklyRecipesCache.value = weeklyRecipesCache.value.filterNot { it.id == id }
            breakfastCache.value = breakfastCache.value.filterNot { it.id == id }
            lunchCache.value = lunchCache.value.filterNot { it.id == id }
            dinnerCache.value = dinnerCache.value.filterNot { it.id == id }
        }
    }

    override suspend fun addOrUpdateRating(recipeId: String, rating: Rating) {
        val session = authRepository.ensureValidSession() ?: return

        val result = api.upsertRating(
            accessToken = session.accessToken,
            recipeId = recipeId,
            userId = session.userId,
            stars = rating.stars,
            comment = rating.comment,
        )
        if (result == null) return

        refreshWeeklySuspend()
        refreshRatingsCountSuspend()
    }

    override suspend fun deleteRating(recipeId: String, userId: String) {
        val session = authRepository.ensureValidSession() ?: return

        val ok = api.deleteRating(session.accessToken, recipeId, userId)
        if (ok) {
            refreshWeeklySuspend()
            refreshRatingsCountSuspend()
        }
    }

    suspend fun getMyRatingsCount(): Int {
        val session = authRepository.ensureValidSession() ?: return 0
        return api.getMyRatingsCount(session.accessToken, session.userId)
    }

    suspend fun loadRatingsForRecipe(recipeId: String): List<Rating> {
        val dtos = api.getRatingsForRecipe(recipeId)
        return dtos.map { dto ->
            Rating(
                userId = dto.userId,
                userName = dto.userName ?: dto.userId.take(8),
                stars = dto.stars,
                comment = dto.comment,
                createdAt = 0L,
            )
        }
    }

    suspend fun fetchThumbnailFor(url: String): String? {
        return api.fetchSpotifyThumbnail(url)
    }

    @Volatile
    private var lastRefreshTimeMs: Long = 0L

    suspend fun refreshIfStale(thresholdMs: Long = 30_000L) {
        val now = currentTimeMillis()
        if (now - lastRefreshTimeMs < thresholdMs) return
        lastRefreshTimeMs = now

        coroutineScope {
            val r1 = async { refreshMainFeedSuspend(reset = true) }
            val r2 = async { refreshWeeklySuspend() }
            launch { refreshSectionsSuspend() }
            launch { refreshFavoritesSuspend() }
            launch { refreshRatingsCountSuspend() }
            launch { refreshProfilesSuspend() }
            r1.await()
            r2.await()
        }
    }

    suspend fun refreshAll() {
        lastRefreshTimeMs = currentTimeMillis()
        coroutineScope {
            launch { refreshMainFeedSuspend(reset = true) }
            launch { refreshWeeklySuspend() }
            launch { refreshSectionsSuspend() }
            launch { refreshFavoritesSuspend() }
            launch { refreshRatingsCountSuspend() }
            launch { refreshProfilesSuspend() }
        }
    }
}