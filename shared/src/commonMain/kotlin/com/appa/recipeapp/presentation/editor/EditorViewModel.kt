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
import com.russhwolf.settings.Settings
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.appa.recipeapp.data.model.currentTimeMillis
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class EditorViewModel(
    private val repository: RecipeRepository,
    private val recipeId: String?,
    private val settings: Settings,
    private val onSaveSuccess: (String) -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(EditorState(recipeId = recipeId))
    val state: StateFlow<EditorState> = _state.asStateFlow()
    private val _movingStepId = MutableStateFlow<Long?>(null)
    val movingStepId: StateFlow<Long?> = _movingStepId.asStateFlow()

    private var nextIngredientId = 1L
    private var nextStepId = 1L
    private var nextTrackId = 0L
    private var isExiting = false

    private var pendingStepId: Long? = null
    private val thumbnailJobs = mutableMapOf<Long, Job>()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadAllTags()
        if (recipeId != null) {
            loadExistingRecipe(recipeId)
        } else {
            restoreDraft()
        }

        _state.onEach { currentState ->
            if (recipeId == null && !isExiting && !currentState.isLoading && !currentState.isSaving && currentState.hasChanges) {
                saveDraftImmediate(currentState)
            }
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun saveDraftImmediate(state: EditorState) {
        try {
            // --- ЛОГИ ---
            val mainPhotoSize = state.photoBytes?.size ?: 0
            val stepsPhotoSize = state.steps.sumOf { it.photoBytes?.size ?: 0 }
            println("NUKA_DEBUG: Байты главного фото в стейте = $mainPhotoSize")
            println("NUKA_DEBUG: Байты фото шагов в стейте = $stepsPhotoSize")
            // -----------

            val stateToSave = state.copy(
                photoBase64 = state.photoBytes?.let { Base64.encode(it) },
                steps = state.steps.map { step ->
                    step.copy(photoBase64 = step.photoBytes?.let { Base64.encode(it) })
                }
            )
            val draftJson = json.encodeToString(EditorState.serializer(), stateToSave)
            println("NUKA_DEBUG: Размер JSON черновика (Base64) = ${draftJson.length} символов")
            settings.putString("draft_recipe", draftJson)
            settings.putLong("draft_time", currentTimeMillis())
        } catch (e: Throwable) {
            println("NUKA_DEBUG: Ошибка сохранения черновика: ${e.message}")
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun restoreDraft() {
        val draftStr = settings.getStringOrNull("draft_recipe")
        if (draftStr.isNullOrBlank()) {
            println("NUKA_DEBUG: Черновик пуст")
            return
        }

        println("NUKA_DEBUG: Прочитан черновик длиной ${draftStr.length} символов")

        val draftTime = settings.getLong("draft_time", 0L)
        if (currentTimeMillis() - draftTime > 259_200_000L) {
            clearDraft()
            return
        }
        try {
            val draftState = json.decodeFromString(EditorState.serializer(), draftStr)
            val restoredState = draftState.copy(
                isDraftRestored = true,
                allTags = _state.value.allTags,
                photoBytes = draftState.photoBase64?.let { Base64.decode(it) },
                steps = draftState.steps.map { step ->
                    step.copy(photoBytes = step.photoBase64?.let { Base64.decode(it) })
                }
            )
            println("NUKA_DEBUG: Восстановлено фото шагов: ${restoredState.steps.count { it.photoBytes != null }}")
            _state.value = restoredState
            nextIngredientId = (draftState.ingredients.maxOfOrNull { it.id } ?: 0L) + 1
            nextStepId = (draftState.steps.maxOfOrNull { it.id } ?: 0L) + 1
            nextTrackId = (draftState.musicTracks.maxOfOrNull { it.id } ?: 0L) + 1
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun clearDraft() {
        settings.remove("draft_recipe")
        settings.remove("draft_time")
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
                    }.ifEmpty { listOf(IngredientDraft(id = 0L)) },
                    steps = recipe.steps.mapIndexed { index, step ->
                        StepDraft(
                            id = index.toLong(),
                            text = step.text,
                            photoUri = step.photoUrl,
                        )
                    }.ifEmpty { listOf(StepDraft(id = 0L)) },
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
                    notes = recipe.notes,
                    isLoading = false,
                )
                nextIngredientId = (recipe.ingredients.size).toLong()
                nextStepId = (recipe.steps.size).toLong()
                nextTrackId = (recipe.musicTracks.size).toLong()
            }
            .launchIn(viewModelScope)
    }

    fun onTitleChange(text: String) { _state.value = _state.value.copy(title = text) }
    fun onDescriptionChange(text: String) { _state.value = _state.value.copy(description = text) }
    fun onCookingTimeChange(text: String) {
        val digits = text.filter { it.isDigit() }
        _state.value = _state.value.copy(cookingTime = digits)
    }
    fun onNotesChange(text: String) { _state.value = _state.value.copy(notes = text) }

    fun addIngredient() { _state.value = _state.value.copy(ingredients = _state.value.ingredients + IngredientDraft(id = nextIngredientId++)) }
    fun removeIngredient(id: Long) {
        val newList = _state.value.ingredients.filterNot { it.id == id }
        _state.value = _state.value.copy(ingredients = newList.ifEmpty { listOf(IngredientDraft(id = nextIngredientId++)) })
    }
    fun updateIngredientName(id: Long, name: String) {
        _state.value = _state.value.copy(ingredients = _state.value.ingredients.map { if (it.id == id) it.copy(name = name) else it })
    }
    fun updateIngredientQuantity(id: Long, quantity: String) {
        _state.value = _state.value.copy(ingredients = _state.value.ingredients.map { if (it.id == id) it.copy(quantity = quantity) else it })
    }

    fun addStep() { _state.value = _state.value.copy(steps = _state.value.steps + StepDraft(id = nextStepId++)) }
    fun removeStep(id: Long) {
        val newList = _state.value.steps.filterNot { it.id == id }
        _state.value = _state.value.copy(steps = newList.ifEmpty { listOf(StepDraft(id = nextStepId++)) })
    }
    fun updateStep(id: Long, text: String) {
        _state.value = _state.value.copy(steps = _state.value.steps.map { if (it.id == id) it.copy(text = text) else it })
    }

    fun moveStepUp(id: Long) {
        _movingStepId.value = id
        val steps = _state.value.steps.toMutableList()
        val index = steps.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = steps.removeAt(index)
            steps.add(index - 1, item)
            _state.value = _state.value.copy(steps = steps)
        }
        viewModelScope.launch { delay(300); _movingStepId.value = null }
    }

    fun moveStepDown(id: Long) {
        _movingStepId.value = id
        val steps = _state.value.steps.toMutableList()
        val index = steps.indexOfFirst { it.id == id }
        if (index != -1 && index < steps.size - 1) {
            val item = steps.removeAt(index)
            steps.add(index + 1, item)
            _state.value = _state.value.copy(steps = steps)
        }
        viewModelScope.launch { delay(300); _movingStepId.value = null }
    }

    fun onStepPhotoClicked(stepId: Long) { pendingStepId = stepId }
    fun getPendingStepId(): Long? = pendingStepId
    fun onCropStart(file: PlatformFile, stepId: Long? = null) {
        _state.value = _state.value.copy(isCropping = true, croppingFile = file, croppingForStepId = stepId)
    }
    fun onCropConfirm(bytes: ByteArray, cropRecipe: String? = null) {
        val stepId = _state.value.croppingForStepId
        if (stepId == null) {
            _state.value = _state.value.copy(
                photoBytes = bytes,
                photoCropRecipe = cropRecipe,
                isCropping = false,
                croppingFile = null,
                croppingForStepId = null
            )
        } else {
            _state.value = _state.value.copy(
                steps = _state.value.steps.map {
                    if (it.id == stepId) it.copy(photoBytes = bytes, photoCropRecipe = cropRecipe) else it
                },
                isCropping = false,
                croppingFile = null,
                croppingForStepId = null
            )
        }
    }
    fun onCropCancel() { _state.value = _state.value.copy(isCropping = false, croppingFile = null, croppingForStepId = null) }

    fun removeMainPhoto() { _state.value = _state.value.copy(photoBytes = null, photoUri = null, photoCropRecipe = null) }
    fun removeStepPhoto(stepId: Long) {
        _state.value = _state.value.copy(steps = _state.value.steps.map { if (it.id == stepId) it.copy(photoBytes = null, photoUri = null, photoCropRecipe = null) else it })
    }

    fun toggleTag(tag: String) {
        val current = _state.value.selectedTags
        val updated = if (tag in current) current - tag else current + tag
        _state.value = _state.value.copy(selectedTags = updated)
    }
    fun onNewTagChange(text: String) { _state.value = _state.value.copy(newTagDraft = text) }
    fun addNewTag() {
        val tag = _state.value.newTagDraft.trim().lowercase()
        if (tag.isBlank()) return
        _state.value = _state.value.copy(
            selectedTags = (_state.value.selectedTags + tag),
            allTags = (_state.value.allTags + tag).distinct().sorted(),
            newTagDraft = "",
        )
    }

    fun onMusicNoteChange(text: String) { _state.value = _state.value.copy(musicNote = text) }
    fun addMusicTrack() { _state.value = _state.value.copy(musicTracks = _state.value.musicTracks + MusicTrackDraft(id = nextTrackId++)) }
    fun removeMusicTrack(id: Long) {
        thumbnailJobs[id]?.cancel()
        thumbnailJobs.remove(id)
        _state.value = _state.value.copy(musicTracks = _state.value.musicTracks.filterNot { it.id == id })
    }
    fun updateMusicTrackTitle(id: Long, title: String) {
        _state.value = _state.value.copy(musicTracks = _state.value.musicTracks.map { if (it.id == id) it.copy(title = title) else it })
    }
    fun updateMusicTrackUrl(id: Long, url: String) {
        _state.value = _state.value.copy(musicTracks = _state.value.musicTracks.map { if (it.id == id) it.copy(url = url, thumbnailUrl = "") else it })
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
                _state.value = _state.value.copy(musicTracks = _state.value.musicTracks.map { if (it.id == trackId) it.copy(thumbnailUrl = thumbnail) else it })
            }
        }
    }

    fun onBackPressed() {
        if (_state.value.hasChanges) {
            _state.value = _state.value.copy(showExitDialog = true)
        }
    }

    fun cancelExit() {
        _state.value = _state.value.copy(showExitDialog = false)
    }

    fun saveDraftAndExit(onExit: () -> Unit) {
        isExiting = true
        if (recipeId == null) {
            saveDraftImmediate(_state.value)
        }
        _state.value = _state.value.copy(showExitDialog = false)
        onExit()
    }

    fun exitWithoutSaving(onExit: () -> Unit) {
        isExiting = true
        if (recipeId == null) {
            clearDraft()
        }
        _state.value = _state.value.copy(showExitDialog = false)
        onExit()
    }

    fun save() {
        if (!_state.value.isValid) return
        val s = _state.value
        viewModelScope.launch {
            try {
                val hasNewPhoto = s.photoBytes != null
                _state.value = s.copy(isSaving = true, saveStatus = if (hasNewPhoto) SaveStatus.UPLOADING_PHOTO else SaveStatus.SAVING_RECIPE)

                val repo = repository as? SupabaseRecipeRepository ?: run {
                    _state.value = _state.value.copy(isSaving = false, saveStatus = SaveStatus.IDLE)
                    return@launch
                }
                val session = repo.ensureValidSession() ?: run {
                    _state.value = _state.value.copy(isSaving = false, saveStatus = SaveStatus.IDLE)
                    return@launch
                }

                val photoUrls: List<String> = when {
                    s.photoBytes != null -> {
                        val url = repo.uploadPhotoBytes(session.accessToken, s.photoBytes)
                        if (url != null) listOf(url) else emptyList()
                    }
                    !s.photoUri.isNullOrBlank() && (s.photoUri.startsWith("http://") || s.photoUri.startsWith("https://")) -> listOf(s.photoUri)
                    else -> if (s.recipeId != null) listOf("") else emptyList()
                }

                _state.value = _state.value.copy(saveStatus = SaveStatus.SAVING_RECIPE)

                val steps = mutableListOf<RecipeStep>()
                for (draft in s.steps.filter { it.text.isNotBlank() }) {
                    val stepPhotoUrl: String? = when {
                        draft.photoBytes != null -> repo.uploadPhotoBytes(session.accessToken, draft.photoBytes)
                        !draft.photoUri.isNullOrBlank() && (draft.photoUri.startsWith("http://") || draft.photoUri.startsWith("https://")) -> draft.photoUri
                        else -> null
                    }
                    steps.add(RecipeStep(text = draft.text.trim(), photoUrl = stepPhotoUrl))
                }

                val ingredients: List<Ingredient> = s.ingredients
                    .filter { it.name.isNotBlank() }
                    .map { draft ->
                        val parts = splitQuantity(draft.quantity)
                        Ingredient(name = draft.name.trim(), amount = parts.first, unit = parts.second)
                    }

                val musicTracks = s.musicTracks
                    .filter { it.url.isNotBlank() }
                    .map { draft -> MusicTrack(title = draft.title.trim(), url = draft.url.trim(), thumbnailUrl = draft.thumbnailUrl) }

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
                    notes = s.notes.trim(),
                    createdAt = 0L,
                )

                val savedId: String = if (s.recipeId == null) repository.addRecipe(recipe)
                else { repository.updateRecipe(recipe); s.recipeId }

                _state.value = _state.value.copy(isSaving = false, saveStatus = SaveStatus.IDLE, savedRecipeId = savedId)

                if (savedId.isNotBlank()) {
                    isExiting = true
                    clearDraft()
                    onSaveSuccess(savedId)
                }
            } catch (e: Throwable) {
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
        return if (amount.firstOrNull()?.isDigit() == true) amount to unit else trimmed to ""
    }
}