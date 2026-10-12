package com.appa.recipeapp.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appa.recipeapp.data.model.Ingredient
import com.appa.recipeapp.data.model.MusicTrack
import com.appa.recipeapp.data.model.Recipe
import com.appa.recipeapp.data.model.RecipeStep
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseRecipeRepository
import com.appa.recipeapp.utils.MusicSource
import com.appa.recipeapp.utils.MusicSourceDetector
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: RecipeRepository,
    private val recipeId: String?,
    private val onSaveSuccess: (String) -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState(recipeId = recipeId))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var nextIngredientId = 1L
    private var nextStepId = 1L
    private var nextTrackId = 0L

    private var pendingStepId: Long? = null
    private val thumbnailJobs = mutableMapOf<Long, Job>()

    init {
        println("DEBUG EditorViewModel: init, recipeId=$recipeId")
        loadAllTags()
        if (recipeId != null) {
            loadExistingRecipe(recipeId)
        }
    }

    private fun loadAllTags() {
        repository.getAllTags()
            .onEach { tags ->
                _state.value = _state.value.copy(allTags = tags.sorted())
            }
            .launchIn(viewModelScope)
    }

    private fun loadExistingRecipe(id: String) {
        _state.value = _state.value.copy(isLoading = true)
        repository.getRecipe(id)
            .onEach { recipe ->
                if (recipe == null) {
                    _state.value = _state.value.copy(isLoading = false)
                    return@onEach
                }
                _state.value = EditorState(
                    recipeId = recipe.id,
                    photoUri = recipe.photoUrls.firstOrNull(),
                    title = recipe.title,
                    description = recipe.description,
                    cookingTime = recipe.cookingTimeMinutes.toString(),
                    ingredients = recipe.ingredients.mapIndexed { index, ing ->
                        IngredientDraft(
                            id = index.toLong(),
                            name = ing.name,
                            quantity = "${ing.amount} ${ing.unit}".trim(),
                        )
                    }.ifEmpty {
                        listOf(IngredientDraft(id = 0L))
                    },
                    steps = recipe.steps.mapIndexed { index, step ->
                        StepDraft(
                            id = index.toLong(),
                            text = step.text,
                            photoUri = step.photoUrl,
                        )
                    }.ifEmpty {
                        listOf(StepDraft(id = 0L))
                    },
                    allTags = _state.value.allTags,
                    selectedTags = recipe.tags.toSet(),
                    musicNote = recipe.musicNote,
                    musicTracks = recipe.musicTracks.mapIndexed { index, track ->
                        MusicTrackDraft(
                            id = index.toLong(),
                            title = track.title,
                            url = track.url,
                            thumbnailUrl = track.thumbnailUrl,
                        )
                    },
                    isLoading = false,
                )
                nextIngredientId = (recipe.ingredients.size).toLong()
                nextStepId = (recipe.steps.size).toLong()
                nextTrackId = (recipe.musicTracks.size).toLong()
            }
            .launchIn(viewModelScope)
    }

    // ─── Основные поля ───

    fun onTitleChange(text: String) {
        _state.value = _state.value.copy(title = text)
    }

    fun onDescriptionChange(text: String) {
        _state.value = _state.value.copy(description = text)
    }

    fun onCookingTimeChange(text: String) {
        val digits = text.filter { it.isDigit() }
        _state.value = _state.value.copy(cookingTime = digits)
    }

    // ─── Ингредиенты ───

    fun addIngredient() {
        val newList = _state.value.ingredients + IngredientDraft(id = nextIngredientId++)
        _state.value = _state.value.copy(ingredients = newList)
    }

    fun removeIngredient(id: Long) {
        val newList = _state.value.ingredients.filterNot { it.id == id }
        _state.value = _state.value.copy(
            ingredients = newList.ifEmpty { listOf(IngredientDraft(id = nextIngredientId++)) },
        )
    }

    fun updateIngredientName(id: Long, name: String) {
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients.map {
                if (it.id == id) it.copy(name = name) else it
            },
        )
    }

    fun updateIngredientQuantity(id: Long, quantity: String) {
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients.map {
                if (it.id == id) it.copy(quantity = quantity) else it
            },
        )
    }

    // ─── Шаги ───

    fun addStep() {
        val newList = _state.value.steps + StepDraft(id = nextStepId++)
        _state.value = _state.value.copy(steps = newList)
    }

    fun removeStep(id: Long) {
        val newList = _state.value.steps.filterNot { it.id == id }
        _state.value = _state.value.copy(
            steps = newList.ifEmpty { listOf(StepDraft(id = nextStepId++)) },
        )
    }

    fun updateStep(id: Long, text: String) {
        _state.value = _state.value.copy(
            steps = _state.value.steps.map {
                if (it.id == id) it.copy(text = text) else it
            },
        )
    }

    fun onStepPhotoClicked(stepId: Long) {
        pendingStepId = stepId
    }

    fun getPendingStepId(): Long? = pendingStepId

    // ─── Кроп фото ───

    fun onCropStart(file: PlatformFile, stepId: Long? = null) {
        _state.value = _state.value.copy(
            isCropping = true,
            croppingFile = file,
            croppingForStepId = stepId,
        )
    }

    fun onCropConfirm(bytes: ByteArray) {
        val stepId = _state.value.croppingForStepId
        if (stepId == null) {
            _state.value = _state.value.copy(
                photoBytes = bytes,
                isCropping = false,
                croppingFile = null,
                croppingForStepId = null,
            )
        } else {
            _state.value = _state.value.copy(
                steps = _state.value.steps.map {
                    if (it.id == stepId) it.copy(photoBytes = bytes) else it
                },
                isCropping = false,
                croppingFile = null,
                croppingForStepId = null,
            )
        }
    }

    fun onCropCancel() {
        _state.value = _state.value.copy(
            isCropping = false,
            croppingFile = null,
            croppingForStepId = null,
        )
    }

    // ─── Теги ───

    fun toggleTag(tag: String) {
        val current = _state.value.selectedTags
        val updated = if (tag in current) current - tag else current + tag
        _state.value = _state.value.copy(selectedTags = updated)
    }

    fun onNewTagChange(text: String) {
        _state.value = _state.value.copy(newTagDraft = text)
    }

    fun addNewTag() {
        val tag = _state.value.newTagDraft.trim().lowercase()
        if (tag.isBlank()) return
        val updatedTags = (_state.value.selectedTags + tag)
        val updatedAllTags = (_state.value.allTags + tag).distinct().sorted()
        _state.value = _state.value.copy(
            selectedTags = updatedTags,
            allTags = updatedAllTags,
            newTagDraft = "",
        )
    }

    // ─── Музыка ───

    fun onMusicNoteChange(text: String) {
        _state.value = _state.value.copy(musicNote = text)
    }

    fun addMusicTrack() {
        val newList = _state.value.musicTracks + MusicTrackDraft(id = nextTrackId++)
        _state.value = _state.value.copy(musicTracks = newList)
    }

    fun removeMusicTrack(id: Long) {
        thumbnailJobs[id]?.cancel()
        thumbnailJobs.remove(id)
        _state.value = _state.value.copy(
            musicTracks = _state.value.musicTracks.filterNot { it.id == id },
        )
    }

    fun updateMusicTrackTitle(id: Long, title: String) {
        _state.value = _state.value.copy(
            musicTracks = _state.value.musicTracks.map {
                if (it.id == id) it.copy(title = title) else it
            },
        )
    }

    fun updateMusicTrackUrl(id: Long, url: String) {
        _state.value = _state.value.copy(
            musicTracks = _state.value.musicTracks.map {
                if (it.id == id) it.copy(url = url, thumbnailUrl = "") else it
            },
        )
        tryFetchThumbnail(id, url)
    }

    private fun tryFetchThumbnail(trackId: Long, url: String) {
        thumbnailJobs[trackId]?.cancel()
        val source = MusicSourceDetector.detect(url)
        if (source != MusicSource.SPOTIFY) return

        thumbnailJobs[trackId] = viewModelScope.launch {
            delay(500)
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            val thumbnail = repo.fetchThumbnailFor(url)
            if (thumbnail != null) {
                _state.value = _state.value.copy(
                    musicTracks = _state.value.musicTracks.map {
                        if (it.id == trackId) it.copy(thumbnailUrl = thumbnail) else it
                    },
                )
            }
        }
    }

    // ─── Диалог отмены ───

    fun onBackPressed() {
        _state.value = _state.value.copy(showExitDialog = true)
    }

    fun dismissExitDialog() {
        _state.value = _state.value.copy(showExitDialog = false)
    }

    // ─── Сохранение ───

    fun save() {
        println("DEBUG save() ВЫЗВАН, isValid=${_state.value.isValid}")
        if (!_state.value.isValid) return

        val s = _state.value
        viewModelScope.launch {
            try {
                val hasNewPhoto = s.photoBytes != null
                _state.value = s.copy(
                    isSaving = true,
                    saveStatus = if (hasNewPhoto) SaveStatus.UPLOADING_PHOTO else SaveStatus.SAVING_RECIPE,
                )

                // Получаем репозиторий и сессию
                val repo = repository as? SupabaseRecipeRepository ?: run {
                    println("DEBUG save(): repository не SupabaseRecipeRepository")
                    _state.value = _state.value.copy(isSaving = false, saveStatus = SaveStatus.IDLE)
                    return@launch
                }

                val session = repo.ensureValidSession() ?: run {
                    println("DEBUG save(): сессия невалидна")
                    _state.value = _state.value.copy(isSaving = false, saveStatus = SaveStatus.IDLE)
                    return@launch
                }

                // ─── Главное фото ───
                val photoUrls: List<String> = when {
                    s.photoBytes != null -> {
                        val url = repo.uploadPhotoBytes(session.accessToken, s.photoBytes)
                        if (url != null) listOf(url) else emptyList()
                    }
                    !s.photoUri.isNullOrBlank() && (s.photoUri.startsWith("http://") || s.photoUri.startsWith("https://")) -> {
                        listOf(s.photoUri)
                    }
                    else -> emptyList()
                }

                _state.value = _state.value.copy(saveStatus = SaveStatus.SAVING_RECIPE)

                // ─── Шаги с фото ───
                val steps = mutableListOf<RecipeStep>()
                for (draft in s.steps.filter { it.text.isNotBlank() }) {
                    val stepPhotoUrl: String? = when {
                        draft.photoBytes != null -> {
                            repo.uploadPhotoBytes(session.accessToken, draft.photoBytes)
                        }
                        !draft.photoUri.isNullOrBlank() && (draft.photoUri.startsWith("http://") || draft.photoUri.startsWith("https://")) -> {
                            draft.photoUri
                        }
                        else -> null
                    }
                    steps.add(
                        RecipeStep(
                            text = draft.text.trim(),
                            photoUrl = stepPhotoUrl,
                        )
                    )
                }

                val ingredients: List<Ingredient> = s.ingredients
                    .filter { it.name.isNotBlank() }
                    .map { draft ->
                        val parts = splitQuantity(draft.quantity)
                        Ingredient(name = draft.name.trim(), amount = parts.first, unit = parts.second)
                    }

                val musicTracks = s.musicTracks
                    .filter { it.url.isNotBlank() }
                    .map { draft ->
                        MusicTrack(
                            title = draft.title.trim(),
                            url = draft.url.trim(),
                            thumbnailUrl = draft.thumbnailUrl,
                        )
                    }

                val recipe = Recipe(
                    id = s.recipeId ?: "",
                    authorId = "",
                    authorName = "",
                    title = s.title.trim(),
                    description = s.description.trim(),
                    ingredients = ingredients,
                    steps = steps,
                    tags = s.selectedTags.toList(),
                    photoUrls = photoUrls,
                    cookingTimeMinutes = s.cookingTime.toIntOrNull() ?: 0,
                    musicNote = s.musicNote.trim(),
                    musicTracks = musicTracks,
                    createdAt = 0L,
                )

                val savedId: String = if (s.recipeId == null) {
                    repository.addRecipe(recipe)
                } else {
                    repository.updateRecipe(recipe)
                    s.recipeId
                }

                _state.value = _state.value.copy(
                    isSaving = false,
                    saveStatus = SaveStatus.IDLE,
                    savedRecipeId = savedId,
                )

                if (savedId.isNotBlank()) {
                    onSaveSuccess(savedId)
                }
            } catch (e: Throwable) {
                println("DEBUG save() EXCEPTION: ${e::class.simpleName} — ${e.message}")
                e.printStackTrace()
                _state.value = _state.value.copy(isSaving = false, saveStatus = SaveStatus.IDLE)
            }
        }
    }

    private fun splitQuantity(input: String): Pair<String, String> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "" to ""

        val spaceIndex = trimmed.indexOf(' ')
        if (spaceIndex == -1) return trimmed to ""

        val amount = trimmed.substring(0, spaceIndex).trim()
        val unit = trimmed.substring(spaceIndex + 1).trim()

        return if (amount.firstOrNull()?.isDigit() == true) {
            amount to unit
        } else {
            trimmed to ""
        }
    }
}