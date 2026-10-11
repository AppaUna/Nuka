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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: RecipeRepository,
    private val authRepository: SupabaseAuthRepository,
    private val recipeId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        println("DEBUG DetailViewModel: создан #${hashCode()}")
        loadRecipe()
        loadRatings()
    }

    private fun loadRecipe() {
        repository.getRecipe(recipeId)
            .onEach { recipe ->
                if (recipe == null) {
                    _state.value = _state.value.copy(
                        recipe = null,
                        isLoading = false,
                        errorMessage = "Рецепт не найден",
                    )
                    return@onEach
                }

                val myId = authRepository.getUserId()
                val isMine = myId.isNotBlank() && recipe.authorId == myId

                _state.value = _state.value.copy(
                    recipe = recipe,
                    isLoading = false,
                    errorMessage = null,
                    isMyRecipe = isMine,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadRatings() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            val ratings = repo.loadRatingsForRecipe(recipeId)

            val myId = authRepository.getUserId()
            val myRating = ratings.find { it.userId == myId }

            val draftStars = if (_state.value.draftStars == 0 && myRating != null) {
                myRating.stars
            } else {
                _state.value.draftStars
            }
            val draftComment = if (_state.value.draftComment.isBlank() && myRating != null) {
                myRating.comment
            } else {
                _state.value.draftComment
            }

            _state.value = _state.value.copy(
                ratings = ratings,
                draftStars = draftStars,
                draftComment = draftComment,
            )
        }
    }

    fun onStarsChange(stars: Int) {
        _state.value = _state.value.copy(draftStars = stars)
    }

    fun onCommentChange(text: String) {
        _state.value = _state.value.copy(draftComment = text)
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
                comment = s.draftComment.trim(),
                createdAt = 0L,
            )

            repository.addOrUpdateRating(recipe.id, rating)

            _state.value = _state.value.copy(isSubmitting = false)
            refreshRatingsOnly()
        }
    }

    private fun refreshRatingsOnly() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            val ratings = repo.loadRatingsForRecipe(recipeId)
            _state.value = _state.value.copy(ratings = ratings)
        }
    }

    fun toggleFavorite() {
        val recipe = _state.value.recipe ?: return
        viewModelScope.launch {
            repository.toggleFavorite(recipe.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("DEBUG DetailViewModel: очищен #${hashCode()}")
    }
}