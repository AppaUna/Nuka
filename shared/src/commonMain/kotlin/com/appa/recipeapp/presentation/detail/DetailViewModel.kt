package com.appa.recipeapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appa.recipeapp.data.model.Rating
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import com.appa.recipeapp.data.repository.SupabaseRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: RecipeRepository,
    private val authRepository: SupabaseAuthRepository,
    private val recipeId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            (repository as? SupabaseRecipeRepository)?.refreshAll()
        }
        loadRecipe()
        loadRatingsAndComments()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            try {
                val recipe = repository.getRecipe(recipeId).filterNotNull().first()
                val myId = authRepository.getUserId()
                _state.value = _state.value.copy(
                    recipe = recipe,
                    isLoading = false,
                    errorMessage = null,
                    isMyRecipe = myId.isNotBlank() && recipe.authorId == myId,
                    currentUserID = myId,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(recipe = null, isLoading = false, errorMessage = "Рецепт не найден")
            }
        }
    }

    private fun loadRatingsAndComments() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            val ratings = repo.loadRatingsForRecipe(recipeId)
            val comments = repo.loadCommentsForRecipe(recipeId)

            val myId = authRepository.getUserId()
            val myRating = ratings.find { it.userId == myId }

            val draftStars = if (_state.value.draftStars == 0 && myRating != null) myRating.stars else _state.value.draftStars

            _state.value = _state.value.copy(
                ratings = ratings,
                comments = comments,
                draftStars = draftStars,
            )
        }
    }

    fun onStarsChange(stars: Int) {
        _state.value = _state.value.copy(draftStars = stars)
    }

    fun submitRating() {
        val s = _state.value
        val recipe = s.recipe ?: return
        if (s.draftStars == 0) return

        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true)

            val rating = Rating(
                userId = authRepository.getUserId(),
                userName = authRepository.getDisplayName(),
                stars = s.draftStars,
                comment = "",
                createdAt = 0L,
            )

            repository.addOrUpdateRating(recipe.id, rating)
            _state.value = _state.value.copy(isSubmitting = false, draftStars = 0)
            loadRatingsAndComments()
        }
    }

    fun onCommentChange(text: String) {
        _state.value = _state.value.copy(draftComment = text)
    }

    fun submitComment() {
        val s = _state.value
        val recipe = s.recipe ?: return
        if (s.draftComment.isBlank()) return

        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            println("DEBUG VM: Отправка комментария: ${s.draftComment}")
            _state.value = s.copy(isSubmitting = true)
            val success = repo.addComment(recipe.id, s.draftComment.trim())
            println("DEBUG VM: Результат: $success")
            if (success) {
                _state.value = _state.value.copy(draftComment = "", isSubmitting = false)
                loadRatingsAndComments()
            } else {
                _state.value = _state.value.copy(isSubmitting = false)
            }
        }
    }

    fun toggleFavorite() {
        val recipe = _state.value.recipe ?: return
        _state.value = _state.value.copy(recipe = recipe.copy(isFavorite = !recipe.isFavorite))
        viewModelScope.launch {
            repository.toggleFavorite(recipe.id)
        }
    }

    fun deleteMyRating() {
        val s = _state.value
        val recipe = s.recipe ?: return
        val myId = authRepository.getUserId()
        if (myId.isBlank()) return

        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true)
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            repo.deleteRating(recipe.id, myId)
            _state.value = _state.value.copy(draftStars = 0, isSubmitting = false)
            loadRatingsAndComments()
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            if (repo.deleteComment(commentId)) {
                loadRatingsAndComments()
            }
        }
    }
}