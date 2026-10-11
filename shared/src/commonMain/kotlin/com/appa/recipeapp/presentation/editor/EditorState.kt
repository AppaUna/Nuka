package com.appa.recipeapp.presentation.editor

import com.appa.recipeapp.data.model.Ingredient

data class IngredientDraft(
    val id: Long,
    val name: String = "",
    val quantity: String = "",
)

data class StepDraft(
    val id: Long,
    val text: String = "",
    val photoUri: String? = null,
)

data class MusicTrackDraft(
    val id: Long,
    val title: String = "",
    val url: String = "",
    val thumbnailUrl: String = "",
)

/** Обязательные теги — должен быть выбран хотя бы один. */
val REQUIRED_TAGS = listOf("завтрак", "обед", "ужин")

/** Популярные теги для быстрого выбора (не обязательные). */
val POPULAR_TAGS = listOf(
    "суп", "салат", "десерт", "выпечка", "напитки", "закуска",
    "горячее", "быстро", "праздничное", "русская кухня",
    "итальянская кухня", "азиатская кухня",
)

/** Этапы сохранения — для индикации в UI. */
enum class SaveStatus {
    IDLE,           // ничего не сохраняется
    UPLOADING_PHOTO, // грузим фото
    SAVING_RECIPE,  // сохраняем рецепт
}

data class EditorState(
    val recipeId: String? = null,
    val photoUri: String? = null,

    val isCropping: Boolean = false,
    val croppingUri: String? = null,
    val croppingForStepId: Long? = null,

    val title: String = "",
    val description: String = "",
    val cookingTime: String = "",

    val ingredients: List<IngredientDraft> = listOf(
        IngredientDraft(id = 0L),
    ),

    val steps: List<StepDraft> = listOf(
        StepDraft(id = 0L),
    ),

    val allTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val newTagDraft: String = "",

    val musicNote: String = "",
    val musicTracks: List<MusicTrackDraft> = emptyList(),

    val showExitDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveStatus: SaveStatus = SaveStatus.IDLE,
    val savedRecipeId: String? = null,
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
                photoUri != null

    /** Текст статуса — для отображения на кнопке. */
    val saveStatusText: String
        get() = when (saveStatus) {
            SaveStatus.IDLE -> "Сохранить"
            SaveStatus.UPLOADING_PHOTO -> "Загрузка фото..."
            SaveStatus.SAVING_RECIPE -> "Сохранение..."
        }
}