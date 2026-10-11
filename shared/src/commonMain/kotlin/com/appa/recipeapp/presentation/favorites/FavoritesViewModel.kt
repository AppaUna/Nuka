package com.appa.recipeapp.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: RecipeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        println("DEBUG FavoritesViewModel: создан #${hashCode()}")
        repository.getFavorites()
            .onEach { list ->
                _state.value = FavoritesState(
                    recipes = list,
                    isLoading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(recipeId)
        }
    }

    fun refreshIfStale() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository
            repo?.refreshIfStale()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val repo = repository as? SupabaseRecipeRepository
                repo?.refreshAll()
                kotlinx.coroutines.delay(500)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("DEBUG FavoritesViewModel: очищен #${hashCode()}")
    }
}