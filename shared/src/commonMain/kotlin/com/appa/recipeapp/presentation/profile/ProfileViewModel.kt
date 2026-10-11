package com.appa.recipeapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appa.recipeapp.data.repository.RecipeRepository
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import com.appa.recipeapp.data.repository.SupabaseRecipeRepository
import com.appa.recipeapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: RecipeRepository,
    private val authRepository: SupabaseAuthRepository,
    private val userPreferences: UserPreferencesRepository,
    private val onLogout: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val myRatingsCountFlow = MutableStateFlow(0)

    init {
        println("DEBUG ProfileViewModel: создан #${hashCode()}")
        _state.value = _state.value.copy(
            login = authRepository.getLogin(),
            displayName = authRepository.getDisplayName(),
            selectedNameColor = userPreferences.getNameColor(),
        )

        loadMyRatingsCount()
        observeData()

        userPreferences.nameColor
            .onEach { color ->
                _state.value = _state.value.copy(selectedNameColor = color)
            }
            .launchIn(viewModelScope)

        // Следим за обновлением сессии (например, когда изменили display_name)
        authRepository.session
            .onEach { session ->
                if (session != null) {
                    _state.value = _state.value.copy(displayName = session.displayName)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadMyRatingsCount() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository
            if (repo != null) {
                myRatingsCountFlow.value = repo.getMyRatingsCount()
            }
        }
    }

    private fun observeData() {
        val userId = authRepository.getUserId()

        combine(
            repository.getUserRecipes(userId),
            repository.getFavorites(),
            repository.getMainFeed(),
            myRatingsCountFlow,
        ) { myRecipes, favorites, allRecipes, myRatingsCount ->
            _state.value.copy(
                myRecipes = myRecipes,
                recipesCount = myRecipes.size,
                ratingsCount = myRatingsCount,
                favoritesCount = favorites.size,
                isLoading = false,
            )
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    fun toggleMyRecipes() {
        _state.value = _state.value.copy(
            isMyRecipesExpanded = !_state.value.isMyRecipesExpanded,
        )
    }

    fun togglePersonalization() {
        _state.value = _state.value.copy(
            isPersonalizationExpanded = !_state.value.isPersonalizationExpanded,
        )
    }

    fun selectNameColor(color: String) {
        _state.value = _state.value.copy(selectedNameColor = color)
    }

    fun startEditingName() {
        _state.value = _state.value.copy(
            isEditingName = true,
            nameDraft = _state.value.displayName,
        )
    }

    fun onNameDraftChange(text: String) {
        _state.value = _state.value.copy(nameDraft = text)
    }

    fun saveName() {
        val trimmed = _state.value.nameDraft.trim()
        if (trimmed.isEmpty()) {
            _state.value = _state.value.copy(isEditingName = false)
            return
        }

        // Оптимистично обновляем UI
        _state.value = _state.value.copy(
            displayName = trimmed,
            isEditingName = false,
        )

        // Отправляем на сервер
        viewModelScope.launch {
            val success = authRepository.updateDisplayName(trimmed)
            if (!success) {
                println("DEBUG ProfileViewModel: не удалось обновить display_name на сервере")
                // Можно откатить, но пока оставим — при след. загрузке придёт старое
            }
        }
    }

    fun cancelEditingName() {
        _state.value = _state.value.copy(isEditingName = false)
    }

    fun applyPersonalization() {
        userPreferences.setNameColor(_state.value.selectedNameColor)
        _state.value = _state.value.copy(isPersonalizationExpanded = false)
    }

    fun onDeleteRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }

    /**
     * Автообновление при заходе на экран (не чаще раза в 30 сек).
     */
    fun refreshIfStale() {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository
            repo?.refreshIfStale()
            loadMyRatingsCount()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val repo = repository as? SupabaseRecipeRepository
                repo?.refreshAll()
                loadMyRatingsCount()
                kotlinx.coroutines.delay(500)
            } finally {
                _isRefreshing.value = false
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        println("DEBUG ProfileViewModel: очищен #${hashCode()}")
    }

    fun logout() {
        authRepository.signOut()
        onLogout()
    }
}