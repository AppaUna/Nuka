package com.appa.recipeapp.presentation.auth

/**
 * Состояние экрана авторизации.
 * Immutable, обновляется через AuthViewModel.
 */
data class AuthState(
    val displayName: String = "",
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegisterMode: Boolean = false,
) {
    val canSubmit: Boolean
        get() = login.isNotBlank() &&
                password.length >= 6 &&
                (!isRegisterMode || displayName.isNotBlank()) &&
                !isLoading
}