package com.appa.recipeapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appa.recipeapp.data.repository.SupabaseAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: SupabaseAuthRepository,
    private val onAuthSuccess: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onDisplayNameChange(value: String) {
        _state.update { it.copy(displayName = value, errorMessage = null) }
    }

    fun onLoginChange(value: String) {
        _state.update { it.copy(login = value.trim(), errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    fun toggleMode() {
        _state.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
            )
        }
    }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (current.isRegisterMode) {
                authRepository.signUp(
                    login = current.login,
                    password = current.password,
                    displayName = current.displayName,
                )
            } else {
                authRepository.signIn(current.login, current.password)
            }

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    onAuthSuccess()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Ошибка авторизации",
                        )
                    }
                },
            )
        }
    }
}