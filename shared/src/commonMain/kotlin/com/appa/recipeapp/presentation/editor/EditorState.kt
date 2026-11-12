package com.appa.recipeapp.presentation.editor

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable

@Serializable
data class IngredientDraft(
    val id: Long,
    val name: String = "",
    val quantity: String = "",
)

@Serializable
data class StepDraft(
    val id: Long,
    val text: String = "",
    val photoUri: String? = null,
    val photoCropRecipe: String? = null,
    @kotlinx.serialization.Transient
    val photoBytes: ByteArray? = null,
    val photoBase64: String? = null, // <--- УБРАЛИ @Transient
)

@Serializable
data class MusicTrackDraft(
    val id: Long,
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
)

val REQUIRED_TAGS = listOf("завтрак", "обед", "ужин")

val POPULAR_TAGS = listOf(
    "суп", "салат", "десерт", "выпечка", "напитки", "закуска",
    "горячее", "быстро", "праздничное", "русская кухня",
    "итальянская кухня", "азиатская кухня",
)

enum class SaveStatus {
    IDLE, UPLOADING_PHOTO, SAVING_RECIPE,
}

@Serializable
data class EditorState(
    val recipeId: String? = null,
    val photoUri: String? = null,
    val photoCropRecipe: String? = null,

    @kotlinx.serialization.Transient
    val photoBytes: ByteArray? = null,
    val photoBase64: String? = null,

    @kotlinx.serialization.Transient
    val isCropping: Boolean = false,
    @kotlinx.serialization.Transient
    val croppingFile: PlatformFile? = null,
    @kotlinx.serialization.Transient
    val croppingForStepId: Long? = null,

    val title: String = "",
    val description: String = "",
    val cookingTime: String = "",

    val ingredients: List<IngredientDraft> = listOf(IngredientDraft(id = 0L)),
    val steps: List<StepDraft> = listOf(StepDraft(id = 0L)),

    val allTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val newTagDraft: String = "",

    val musicNote: String = "",
    val musicTracks: List<MusicTrackDraft> = emptyList(),

    val notes: String = "",

    @kotlinx.serialization.Transient
    val showExitDialog: Boolean = false,
    @kotlinx.serialization.Transient
    val isLoading: Boolean = false,
    @kotlinx.serialization.Transient
    val isSaving: Boolean = false,
    @kotlinx.serialization.Transient
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    @kotlinx.serialization.Transient
    val savedRecipeId: String? = null,
    @kotlinx.serialization.Transient
    val isDraftRestored: Boolean = false,
) {
    val screenTitle: String
        get() = if (recipeId == null) "Новый рецепт" else "Редактирование"

    val hasRequiredTag: Boolean
        get() = REQUIRED_TAGS.any { it in selectedTags }

    val isValid: Boolean
        get() = title.isNotBlank() &&
                cookingTime.toIntOrNull()?.let { it > 0 } == true &&
                ingredients.any { it.name.isNotBlank() } &&
                steps.any { it.text.isNotBlank() } &&
                hasRequiredTag

    val hasChanges: Boolean
        get() = title.isNotBlank() ||
                description.isNotBlank() ||
                cookingTime.isNotBlank() ||
                ingredients.any { it.name.isNotBlank() || it.quantity.isNotBlank() } ||
                steps.any { it.text.isNotBlank() } ||
                selectedTags.isNotEmpty() ||
                musicNote.isNotBlank() ||
                musicTracks.any { it.url.isNotBlank() } ||
                notes.isNotBlank() ||
                photoBytes != null ||
                photoUri != null ||
                photoBase64 != null

    val saveStatusText: String
        get() = when (saveStatus) {
            SaveStatus.IDLE -> "Сохранить"
            SaveStatus.UPLOADING_PHOTO -> "Загрузка фото..."
            SaveStatus.SAVING_RECIPE -> "Сохранение..."
        }
}