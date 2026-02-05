package com.example.myapplication.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _hasProfile = MutableStateFlow(false)
    val hasProfile: StateFlow<Boolean> = _hasProfile.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun login() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithEmailAndPassword(
                _uiState.value.email,
                _uiState.value.password
            )

            result.onSuccess { user ->
                _userName.value = user.displayName

                Log.d("LoginViewModel", "Usuario logueado: ${user.uid}, nombre: ${user.displayName}")

                val profileExists = authRepository.checkUserProfileExists(user.uid)
                _hasProfile.value = profileExists

                Log.d("LoginViewModel", "¿Perfil existe? $profileExists")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }

            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al iniciar sesión"
                    )
                }
            }
        }
    }


    fun signInWithGoogle() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    fun onSignInCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithGoogle(idToken)

            result.onSuccess { user ->
                _userName.value = user.displayName

                val profileExists = authRepository.checkUserProfileExists(user.uid)
                _hasProfile.value = profileExists

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al iniciar sesión con Google"
                    )
                }
            }
        }
    }

    // --- FUNCIÓN PARA RECUPERACIÓN DE CONTRASEÑA ---
    fun resetPassword(email: String) {
        // Validar email vacío
        if (email.isEmpty()) {
            _uiState.update { it.copy(error = "Por favor ingresa tu email") }
            return
        }

        // Validar formato de email
        if (!email.contains("@")) {
            _uiState.update { it.copy(error = "Email inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.sendPasswordResetEmail(email)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        passwordResetEmailSent = true
                    )
                }
            }.onFailure { error ->
                val errorMessage = when {
                    error.message?.contains("user-not-found") == true ->
                        "No existe una cuenta con este email"
                    error.message?.contains("invalid-email") == true ->
                        "Email inválido"
                    error.message?.contains("too-many-requests") == true ->
                        "Demasiados intentos. Intenta más tarde"
                    else ->
                        "Error al enviar el correo: ${error.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isEmpty() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Ingresa un email válido") }
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return false
        }

        return true
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _userName.value = user.displayName

                val profileExists = authRepository.checkUserProfileExists(user.uid)
                _hasProfile.value = profileExists

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }
            }
        }
    }
}