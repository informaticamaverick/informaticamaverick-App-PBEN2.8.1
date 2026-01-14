package com.example.myapplication.Login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Data.Repository.AuthRepository
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

            // CORRECCIÓN 1: Cambiado 'resul' a 'result'
            val result = authRepository.signInWithEmailAndPassword(
                _uiState.value.email,
                _uiState.value.password
            )

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
                        error = error.message ?: "Error al iniciar sesión"
                    )
                }
            }
        }
    }


    fun signInWithGoogle() {
        _uiState.update { it.copy(isLoading = true, error = null) }
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

    // --- FUNCIÓN CORREGIDA ---
    fun resetPassword(email: String) {
        // Primero validamos si el email está vacío antes de lanzar la corrutina
        if (email.isEmpty()) {
            _uiState.update { it.copy(error = "Por favor ingresa tu email") }
            return
        }

        viewModelScope.launch {
            // Corregido: isLoading (sin n) y _uiState
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.sendPasswordResetEmail(email)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false, // Corregido typo
                        error = null,      // Agregada la coma que faltaba
                        passwordResetEmailSent = true
                    )
                }
            }.onFailure { error -> // Corregida sintaxis de lambda
                _uiState.update {
                    it.copy(
                        isLoading = false, // Corregido typo
                        error = error.message ?: "Error al enviar el correo de recuperación"
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
}