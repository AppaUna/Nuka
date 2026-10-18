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

    init {
        _state.value = _state.value.copy(
            login = authRepository.getLogin(),
            displayName = authRepository.getDisplayName(),
            selectedNameColor = userPreferences.getNameColor(),
        )

        observeData()

        userPreferences.nameColor
            .onEach { color ->
                _state.value = _state.value.copy(selectedNameColor = color)
            }
            .launchIn(viewModelScope)

        authRepository.session
            .onEach { session ->
                if (session != null) {
                    _state.value = _state.value.copy(displayName = session.displayName)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeData() {
        val userId = authRepository.getUserId()
        val repo = repository as? SupabaseRecipeRepository

        val ratingsFlow = repo?.myRatingsCountState ?: MutableStateFlow(null)
        val avatarFlow = repo?.myAvatarState ?: MutableStateFlow<String?>(null)

        combine(
            repository.getUserRecipes(userId),
            repository.getFavorites(),
            repository.getMainFeed(),
            ratingsFlow,
            avatarFlow,
        ) { myRecipes, favorites, allRecipes, myRatingsCount, myAvatarUrl ->
            _state.value.copy(
                myRecipes = myRecipes,
                recipesCount = myRecipes.size,
                ratingsCount = myRatingsCount ?: 0,
                favoritesCount = favorites.size,
                isLoading = false,
                avatarUrl = myAvatarUrl, // <-- Теперь аватарка всегда берется из кэша репозитория
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

        _state.value = _state.value.copy(
            displayName = trimmed,
            isEditingName = false,
        )

        viewModelScope.launch {
            authRepository.updateDisplayName(trimmed)
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
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun uploadAvatar(bytes: ByteArray, ext: String = "jpg") {
        println("DEBUG ProfileViewModel: uploadAvatar, ${bytes.size} байт")
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: run {
                println("DEBUG ProfileViewModel: repo is not SupabaseRecipeRepository")
                return@launch
            }
            val newUrl = repo.uploadAvatar(bytes, ext)
            println("DEBUG ProfileViewModel: uploadAvatar result = $newUrl")
            // _state.value = _state.value.copy(avatarUrl = newUrl) больше не нужно
            // combine сам подхватит новый URL из avatarFlow
        }
    }

    fun uploadAvatar(localUri: String) {
        viewModelScope.launch {
            val repo = repository as? SupabaseRecipeRepository ?: return@launch
            repo.uploadAvatar(localUri)
        }
    }

    fun logout() {
        authRepository.signOut()
        onLogout()
    }
}