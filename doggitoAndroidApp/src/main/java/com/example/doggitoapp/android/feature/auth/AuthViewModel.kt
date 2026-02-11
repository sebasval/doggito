package com.example.doggitoapp.android.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doggitoapp.android.core.service.FcmTokenManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserUpdateBuilder
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
    val userId: String? = null,
    val resetEmailSent: Boolean = false,
    val otpVerified: Boolean = false,
    val passwordUpdated: Boolean = false
)

class AuthViewModel(
    private val supabaseClient: SupabaseClient,
    private val fcmTokenManager: FcmTokenManager
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
                    // Registrar token FCM al recuperar sesion
                    try {
                        fcmTokenManager.registerToken()
                    } catch (e: Exception) {
                        Log.w("AuthViewModel", "No se pudo registrar FCM token", e)
                    }
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
                // Registrar token FCM despues de login exitoso
                try {
                    fcmTokenManager.registerToken()
                } catch (e: Exception) {
                    Log.w("AuthViewModel", "No se pudo registrar FCM token post-login", e)
                }
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
                // Eliminar token FCM antes de cerrar sesion
                try {
                    fcmTokenManager.unregisterToken()
                } catch (_: Exception) { }
                supabaseClient.auth.signOut()
                _uiState.value = AuthUiState()
            } catch (_: Exception) {
                _uiState.value = AuthUiState()
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                supabaseClient.auth.resetPasswordForEmail(email)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resetEmailSent = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al enviar el correo de recuperación"
                )
            }
        }
    }

    fun verifyRecoveryOtp(email: String, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                supabaseClient.auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = email,
                    token = token
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    otpVerified = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Codigo incorrecto o expirado"
                )
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                supabaseClient.auth.updateUser {
                    password = newPassword
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    passwordUpdated = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar la contrasena"
                )
            }
        }
    }

    fun clearResetState() {
        _uiState.value = _uiState.value.copy(
            resetEmailSent = false,
            otpVerified = false,
            passwordUpdated = false,
            error = null
        )
    }

    fun clearResetEmailSent() {
        _uiState.value = _uiState.value.copy(resetEmailSent = false)
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
