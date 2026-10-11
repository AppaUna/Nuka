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

class SupabaseRecipeRepository(
    private val api: SupabaseRecipeApi,
    private val storageApi: SupabaseStorageApi,
    private val authRepository: SupabaseAuthRepository,
) : RecipeRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ─── Кэши ───
    /** Все рецепты, которые сейчас загружены (страницами). */
    private val recipesCache = MutableStateFlow<List<Recipe>>(emptyList())
    /** Weekly (популярное) — 6 рецептов. */
    private val weeklyRecipesCache = MutableStateFlow<List<Recipe>>(emptyList())
    /** Избранные id. */
    private val favoriteIdsCache = MutableStateFlow<Set<String>>(emptySet())

    // ─── Флаги состояния mainFeed (пагинация) ───
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

    // ─── Флаги для секций (Завтрак / Обед / Ужин) ───
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

    /** Случайный seed для порядка рецептов. Генерируется один раз при создании репозитория. */
    private val randomSeed: Long = currentTimeMillis()

    init {
        scope.launch {
            var isFirstEmission = true
            authRepository.session.collect { session ->
                if (isFirstEmission) {
                    isFirstEmission = false
                    println("DEBUG init: первая эмиссия сессии = ${if (session != null) "есть" else "нет"}")
                    if (session != null) {
                        refreshFavoritesSuspend()
                    }
                    // Первая загрузка (анонимно или с токеном — неважно, all public)
                    refreshMainFeedSuspend(reset = true)
                    refreshWeeklySuspend()
                    refreshSectionsSuspend()
                } else {
                    if (session != null) {
                        println("DEBUG session: login — грузим favorites")
                        refreshFavoritesSuspend()
                        // Не перезагружаем mainFeed/sections/weekly — только favorites
                    } else {
                        println("DEBUG session: logout — чистим favorites")
                        favoriteIdsCache.value = emptySet()
                        recipesCache.value = recipesCache.value.map { it.copy(isFavorite = false) }
                        weeklyRecipesCache.value = weeklyRecipesCache.value.map { it.copy(isFavorite = false) }
                        breakfastCache.value = breakfastCache.value.map { it.copy(isFavorite = false) }
                        lunchCache.value = lunchCache.value.map { it.copy(isFavorite = false) }
                        dinnerCache.value = dinnerCache.value.map { it.copy(isFavorite = false) }
                    }
                }
            }
        }
    }

    private fun mapDto(dto: com.appa.recipeapp.data.remote.dto.RecipeDto): Recipe {
        return dto.toDomain(favoriteIdsCache.value)
    }

    private fun mergeRecipes(old: List<Recipe>, new: List<Recipe>): List<Recipe> {
        val oldById = old.associateBy { it.id }
        return new.map { newItem ->
            val oldItem = oldById[newItem.id]
            if (oldItem != null && oldItem == newItem) oldItem else newItem
        }
    }

    // ─── MainFeed (с пагинацией) ───

    /**
     * Загрузить страницу mainFeed.
     * @param reset — если true, сбрасывает offset и заменяет всё. Если false — догружает.
     */
    private suspend fun refreshMainFeedSuspend(reset: Boolean): Boolean {
        if (reset) {
            if (isRefreshingMain) {
                println("DEBUG refreshMainFeed: refresh уже идёт, пропускаем")
                return true
            }
            isRefreshingMain = true
            mainFeedOffset = 0
            isEndReached = false
        } else {
            if (isLoadingMore || isEndReached) {
                println("DEBUG loadMoreMainFeed: пропуск (isLoadingMore=$isLoadingMore, isEndReached=$isEndReached)")
                return true
            }
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
                        println("DEBUG refreshMainFeed: loaded ${newList.size} recipes (reset)")
                    } else {
                        // Догрузка — добавляем в конец (без дубликатов)
                        val existingIds = recipesCache.value.map { it.id }.toSet()
                        val toAdd = newList.filter { it.id !in existingIds }
                        recipesCache.value = recipesCache.value + toAdd
                        mainFeedOffset += newList.size
                        println("DEBUG loadMoreMainFeed: loaded +${toAdd.size} recipes (offset=${mainFeedOffset})")
                    }
                    if (newList.size < MAIN_FEED_PAGE_SIZE) {
                        isEndReached = true
                        _isMainFeedEndReached.value = true
                        println("DEBUG mainFeed: end reached (got ${newList.size})")
                    }
                    _isRecipesLoaded.value = true
                    _hasNetworkError.value = false
                    true
                }
                is ApiResult.Error -> {
                    println("DEBUG refreshMainFeed ERROR: ${result.message}")
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

    /**
     * Публичный метод — вызывается из ViewModel при скролле mainFeed до конца.
     */
    suspend fun loadMoreMainFeed() {
        refreshMainFeedSuspend(reset = false)
    }

    // ─── Weekly ───

    private suspend fun refreshWeeklySuspend(): Boolean {
        if (isWeeklyRefreshing) {
            println("DEBUG refreshWeekly: refresh уже идёт, пропускаем")
            return true
        }
        isWeeklyRefreshing = true
        try {
            return when (val result = api.getWeeklyRecipes(limit = WEEKLY_LIMIT)) {
                is ApiResult.Success -> {
                    val newList = result.data.map { mapDto(it) }
                    weeklyRecipesCache.value = mergeRecipes(weeklyRecipesCache.value, newList)
                    println("DEBUG refreshWeekly: loaded ${weeklyRecipesCache.value.size} weekly recipes")
                    _isWeeklyLoaded.value = true
                    true
                }
                is ApiResult.Error -> {
                    println("DEBUG refreshWeekly ERROR: ${result.message}")
                    _isWeeklyLoaded.value = true
                    false
                }
            }
        } finally {
            isWeeklyRefreshing = false
        }
    }

    // ─── Секции (Завтрак / Обед / Ужин) ───

    private suspend fun refreshSectionsSuspend() {
        if (isSectionsRefreshing) {
            println("DEBUG refreshSections: уже идёт, пропускаем")
            return
        }
        isSectionsRefreshing = true
        try {
            coroutineScope {
                val breakfastJob = async {
                    when (val r = api.getRecipesByTagPage(tag = "завтрак", seed = randomSeed, limit = SECTION_PAGE_SIZE)) {
                        is ApiResult.Success -> breakfastCache.value = r.data.map { mapDto(it) }
                        is ApiResult.Error -> println("DEBUG breakfast ERROR: ${r.message}")
                    }
                }
                val lunchJob = async {
                    when (val r = api.getRecipesByTagPage(tag = "обед", seed = randomSeed, limit = SECTION_PAGE_SIZE)) {
                        is ApiResult.Success -> lunchCache.value = r.data.map { mapDto(it) }
                        is ApiResult.Error -> println("DEBUG lunch ERROR: ${r.message}")
                    }
                }
                val dinnerJob = async {
                    when (val r = api.getRecipesByTagPage(tag = "ужин", seed = randomSeed, limit = SECTION_PAGE_SIZE)) {
                        is ApiResult.Success -> dinnerCache.value = r.data.map { mapDto(it) }
                        is ApiResult.Error -> println("DEBUG dinner ERROR: ${r.message}")
                    }
                }
                breakfastJob.await()
                lunchJob.await()
                dinnerJob.await()
            }
            _isSectionsLoaded.value = true
            println("DEBUG refreshSections: loaded breakfast=${breakfastCache.value.size}, lunch=${lunchCache.value.size}, dinner=${dinnerCache.value.size}")
        } finally {
            isSectionsRefreshing = false
        }
    }

    // ─── Favorites ───

    private suspend fun refreshFavoritesSuspend() {
        println("DEBUG refreshFavorites: стартуем")
        val session = authRepository.ensureValidSession()
        if (session == null) {
            println("DEBUG refreshFavorites: сессия невалидна — favorites не загружены")
            return
        }
        try {
            val ids = api.getFavoriteRecipeIds(session.accessToken, session.userId)
            favoriteIdsCache.value = ids
            println("DEBUG refreshFavorites: loaded ${ids.size} favorite ids — $ids")

            recipesCache.value = recipesCache.value.map { r ->
                val shouldBeFav = r.id in ids
                if (r.isFavorite == shouldBeFav) r else r.copy(isFavorite = shouldBeFav)
            }
            weeklyRecipesCache.value = weeklyRecipesCache.value.map { r ->
                val shouldBeFav = r.id in ids
                if (r.isFavorite == shouldBeFav) r else r.copy(isFavorite = shouldBeFav)
            }
            breakfastCache.value = breakfastCache.value.map { r ->
                val shouldBeFav = r.id in ids
                if (r.isFavorite == shouldBeFav) r else r.copy(isFavorite = shouldBeFav)
            }
            lunchCache.value = lunchCache.value.map { r ->
                val shouldBeFav = r.id in ids
                if (r.isFavorite == shouldBeFav) r else r.copy(isFavorite = shouldBeFav)
            }
            dinnerCache.value = dinnerCache.value.map { r ->
                val shouldBeFav = r.id in ids
                if (r.isFavorite == shouldBeFav) r else r.copy(isFavorite = shouldBeFav)
            }
        } catch (e: Throwable) {
            println("DEBUG refreshFavorites EXCEPTION: ${e::class.simpleName} — ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun uploadPhotoIfNeeded(
        accessToken: String,
        localUri: String?,
    ): String? {
        if (localUri.isNullOrBlank()) return null
        if (localUri.startsWith("http://") || localUri.startsWith("https://")) return localUri

        println("DEBUG uploadPhotoIfNeeded: читаем файл '$localUri'")
        val bytes = readFileBytes(localUri)
        if (bytes == null) {
            println("DEBUG uploadPhotoIfNeeded: НЕ УДАЛОСЬ прочитать файл")
            return null
        }

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

        val publicUrl = storageApi.uploadPhoto(accessToken, randomName, bytes, contentType)
        if (publicUrl == null) {
            println("DEBUG uploadPhotoIfNeeded: uploadPhoto вернул null")
        } else {
            println("DEBUG uploadPhotoIfNeeded: загружено → $publicUrl")
        }
        return publicUrl
    }

    // ─── Чтение ───

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
        // Возвращаем соответствующую секцию из отдельного кэша
        return when (tag.lowercase()) {
            "завтрак" -> breakfastCache
            "обед" -> lunchCache
            "ужин" -> dinnerCache
            else -> breakfastCache   // fallback
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

    // ─── Избранное ───

    override suspend fun toggleFavorite(recipeId: String) {
        val session = authRepository.ensureValidSession() ?: run {
            println("DEBUG toggleFavorite: сессия невалидна")
            return
        }
        val isCurrentlyFavorite = recipeId in favoriteIdsCache.value

        println("DEBUG toggleFavorite: recipeId=$recipeId, isCurrentlyFavorite=$isCurrentlyFavorite")

        // Оптимистичное обновление
        val newIds = if (isCurrentlyFavorite) {
            favoriteIdsCache.value - recipeId
        } else {
            favoriteIdsCache.value + recipeId
        }
        favoriteIdsCache.value = newIds

        recipesCache.value = recipesCache.value.map { r ->
            if (r.id == recipeId) r.copy(isFavorite = !isCurrentlyFavorite) else r
        }
        weeklyRecipesCache.value = weeklyRecipesCache.value.map { r ->
            if (r.id == recipeId) r.copy(isFavorite = !isCurrentlyFavorite) else r
        }
        breakfastCache.value = breakfastCache.value.map { r ->
            if (r.id == recipeId) r.copy(isFavorite = !isCurrentlyFavorite) else r
        }
        lunchCache.value = lunchCache.value.map { r ->
            if (r.id == recipeId) r.copy(isFavorite = !isCurrentlyFavorite) else r
        }
        dinnerCache.value = dinnerCache.value.map { r ->
            if (r.id == recipeId) r.copy(isFavorite = !isCurrentlyFavorite) else r
        }

        val success = if (isCurrentlyFavorite) {
            api.removeFavorite(session.accessToken, session.userId, recipeId)
        } else {
            api.addFavorite(session.accessToken, session.userId, recipeId)
        }

        println("DEBUG toggleFavorite: recipe=$recipeId, wasFav=$isCurrentlyFavorite, success=$success")

        // Перезагружаем favorites с сервера (актуализация)
        refreshFavoritesSuspend()
    }

    // ─── CRUD рецепта ───

    override suspend fun addRecipe(recipe: Recipe): String {
        val session = authRepository.ensureValidSession()
        if (session == null) return ""

        val photoUrl = uploadPhotoIfNeeded(session.accessToken, recipe.photoUrls.firstOrNull())
        val recipeWithRemotePhoto = recipe.copy(photoUrls = listOfNotNull(photoUrl))

        val dto = recipeWithRemotePhoto.toInsertDto(authorId = session.userId)
        val created = api.saveRecipe(session.accessToken, dto)
        if (created == null) return ""

        // После создания — полностью перезагружаем mainFeed (чтобы новый рецепт был сверху)
        refreshMainFeedSuspend(reset = true)
        refreshWeeklySuspend()
        refreshSectionsSuspend()
        println("DEBUG addRecipe: создан рецепт id=${created.id}")
        return created.id
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val session = authRepository.ensureValidSession() ?: return
        if (recipe.id.isBlank()) return

        val photoUrl = uploadPhotoIfNeeded(session.accessToken, recipe.photoUrls.firstOrNull())
        val recipeWithRemotePhoto = recipe.copy(photoUrls = listOfNotNull(photoUrl))

        val dto = recipeWithRemotePhoto.toUpdateDto()
        val updated = api.updateRecipe(session.accessToken, recipe.id, dto) ?: return

        refreshMainFeedSuspend(reset = true)
        refreshWeeklySuspend()
        refreshSectionsSuspend()
        println("DEBUG updateRecipe: обновлён рецепт id=${updated.id}")
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

    // ─── Оценки ───

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
        println("DEBUG addOrUpdateRating: оценка сохранена recipe=$recipeId")
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

    // ─── Обновление ───

    private var lastRefreshTimeMs: Long = 0L

    /**
     * Автообновление при заходе. Сбрасывает mainFeed (reset), грузит weekly, sections, favorites.
     */
    suspend fun refreshIfStale(thresholdMs: Long = 30_000L) {
        val now = currentTimeMillis()
        if (now - lastRefreshTimeMs < thresholdMs) {
            println("DEBUG refreshIfStale: пропуск (с прошлого раза ${now - lastRefreshTimeMs} мс)")
            return
        }
        println("DEBUG refreshIfStale: обновляем (прошло ${now - lastRefreshTimeMs} мс)")

        val success = coroutineScope {
            val r1 = async { refreshMainFeedSuspend(reset = true) }
            val r2 = async { refreshWeeklySuspend() }
            launch { refreshSectionsSuspend() }
            launch { refreshFavoritesSuspend() }
            r1.await() && r2.await()
        }

        if (success) {
            lastRefreshTimeMs = now
        } else {
            println("DEBUG refreshIfStale: была ошибка — lastRefreshTimeMs не обновляем")
        }
    }

    /**
     * Полная перезагрузка — сбрасывает mainFeed, weekly, sections, favorites.
     */
    suspend fun refreshAll() {
        coroutineScope {
            launch { refreshMainFeedSuspend(reset = true) }
            launch { refreshWeeklySuspend() }
            launch { refreshSectionsSuspend() }
            launch { refreshFavoritesSuspend() }
        }
    }
}