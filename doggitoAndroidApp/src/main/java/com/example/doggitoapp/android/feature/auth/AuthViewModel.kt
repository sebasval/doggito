package com.example.doggitoapp.android.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val userId: String? = null
)

class AuthViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Esperar al primer estado definitivo (ignorar LoadingFromStorage)
            val status = supabaseClient.auth.sessionStatus
                .first { it !is SessionStatus.LoadingFromStorage }

            when (status) {
                is SessionStatus.Authenticated -> {
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        isCheckingSession = false,
                        userId = status.session.user?.id
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isCheckingSession = false)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userId = userId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userId = userId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al registrarse"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
                _uiState.value = AuthUiState()
            } catch (_: Exception) {
                _uiState.value = AuthUiState()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getCurrentUserId(): String {
        return _uiState.value.userId
            ?: supabaseClient.auth.currentUserOrNull()?.id
            ?: "local_user"
    }
}
