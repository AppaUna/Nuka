package com.appa.recipeapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import com.appa.recipeapp.data.repository.SupabaseRecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RecipeRepository,
    private val authRepository: SupabaseAuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeState(
            greetingName = authRepository.getDisplayName(),
        )
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var mainFeedJob: Job? = null

    init {
        observeDisplayName()
        loadAllTags()
        observeStaticSections()
        reloadMainFeed()
        observeLoadingFlags()

        viewModelScope.launch {
            refreshIfStaleInternal()
        }
    }

    private fun observeDisplayName() {
        authRepository.session
            .onEach { session ->
                val name = session?.displayName.orEmpty()
                if (name.isNotEmpty() && name != _state.value.greetingName) {
                    _state.value = _state.value.copy(greetingName = name)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLoadingFlags() {
        val repo = repository as? SupabaseRecipeRepository ?: return

        repo.isRecipesLoaded
            .onEach { loaded ->
                _state.value = _state.value.copy(isRecipesLoaded = loaded)
            }
            .launchIn(viewModelScope)

        repo.isWeeklyLoaded
            .onEach { loaded ->
                _state.value = _state.value.copy(isWeeklyLoaded = loaded)
            }
            .launchIn(viewModelScope)

        repo.hasNetworkError
            .onEach { hasError ->
                _state.value = _state.value.copy(hasNetworkError = hasError)
            }
            .launchIn(viewModelScope)

        repo.isMainFeedLoadingMore
            .onEach { loading ->
                _state.value = _state.value.copy(isMainFeedLoadingMore = loading)
            }
            .launchIn(viewModelScope)

        repo.isMainFeedEndReached
            .onEach { endReached ->
                _state.value = _state.value.copy(isMainFeedEndReached = endReached)
            }
            .launchIn(viewModelScope)
    }

    private fun loadAllTags() {
        repository.getAllTags()
            .onEach { tags ->
                val filtered = tags
                    .filterNot { it in setOf("завтрак", "обед", "ужин") }
                _state.value = _state.value.copy(allTags = filtered)
            }
            .launchIn(viewModelScope)
    }

    private fun observeStaticSections() {
        combine(
            repository.getByTag("завтрак"),
            repository.getByTag("обед"),
            repository.getByTag("ужин"),
            repository.getTopRated(6),
        ) { breakfast, lunch, dinner, top ->

            _state.value = _state.value.copy(
                breakfast = breakfast,
                lunch = lunch,
                dinner = dinner,
                topRated = top,
                isStaticSectionsLoaded = true,
            )
        }.launchIn(viewModelScope)
    }

    fun onSearchChange(query: String) {
        _state.value = _state.value.copy(query = query)
        reloadMainFeed()
    }

    fun toggleTag(tag: String) {
        val current = _state.value.selectedTags
        val updated = if (tag in current) current - tag else current + tag
        _state.value = _state.value.copy(selectedTags = updated)
        reloadMainFeed()
    }

    fun clearTags() {
        _state.value = _state.value.copy(selectedTags = emptySet())
        reloadMainFeed()
    }

    fun toggleBreakfastExpanded() {
        _state.value = _state.value.copy(
            isBreakfastExpanded = !_state.value.isBreakfastExpanded,
        )
    }

    fun toggleLunchExpanded() {
        _state.value = _state.value.copy(
            isLunchExpanded = !_state.value.isLunchExpanded,
        )
    }

    fun toggleDinnerExpanded() {
        _state.value = _state.value.copy(
            isDinnerExpanded = !_state.value.isDinnerExpanded,
        )
    }

    private fun reloadMainFeed() {
        mainFeedJob?.cancel()

        val tags = _state.value.selectedTags
        val query = _state.value.query

        mainFeedJob = repository.getMainFeed(tags, query)
            .onEach { recipes ->
                _state.value = _state.value.copy(mainFeed = recipes)
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
            refreshIfStaleInternal()
        }
    }

    private suspend fun refreshIfStaleInternal() {
        val repo = repository as? SupabaseRecipeRepository
        repo?.refreshIfStale()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val repo = repository as? SupabaseRecipeRepository
                repo?.refreshAll()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMoreMainFeed() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            if (_state.value.selectedTags.isNotEmpty()) return@launch
            if (_state.value.query.isNotBlank()) return@launch
            repo.loadMoreMainFeed()
        }
    }
}