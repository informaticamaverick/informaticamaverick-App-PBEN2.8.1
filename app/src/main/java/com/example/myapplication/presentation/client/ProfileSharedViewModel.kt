package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.screens.ProfileMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL COMPARTIDO DE PERFIL (ProfileSharedViewModel) ---
 *
 * Este ViewModel es la única fuente de verdad para los datos del usuario logueado.
 * Se encarga de cargar, observar y actualizar la información del perfil del usuario
 * desde el repositorio, asegurando que toda la UI que dependa de estos datos
 * (como HomeScreen, PerfilUsuarioScreen, etc.) esté siempre sincronizada.
 *
 * Ya no maneja modos de UI (Cliente/Empresa), su única responsabilidad es el UserEntity.
 */
@HiltViewModel
class ProfileSharedViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // =================================================================================
    // --- SECCIÓN: ESTADO DE DATOS DEL USUARIO ---
    // =================================================================================

    // Estado interno mutable que contiene el perfil del usuario (UserEntity de Room).
    private val _userState = MutableStateFlow<UserEntity?>(null)
    // Estado público inmutable, expuesto para ser observado por la UI de forma segura.
    val userState: StateFlow<UserEntity?> = _userState.asStateFlow()

    // =================================================================================
    // --- SECCIÓN: INICIALIZACIÓN ---
    // =================================================================================

    init {
        // Al iniciar el ViewModel, comenzamos a observar los cambios en el perfil del usuario.
        observeUserProfile()

        // Adicionalmente, forzamos una sincronización con el backend para asegurar
        // que tenemos los datos más recientes al arrancar la app.
        refreshData()
    }

    // =================================================================================
    // --- SECCIÓN: LÓGICA DE DATOS ---
    // =================================================================================

    /**
     * Se suscribe al flujo de datos del UserRepository.
     * Cada vez que el UserEntity cambie en la base de datos local (Room),
     * el `_userState` se actualizará automáticamente, refrescando así toda la UI.
     */
    private fun observeUserProfile() {
        viewModelScope.launch {
            userRepository.userProfile.collect { userEntity ->
                _userState.value = userEntity
            }
        }
    }

    /**
     * Guarda el objeto UserEntity completo en la base de datos local.
     * Esta función debe ser llamada cuando el usuario confirma cambios en su perfil.
     *
     * @param updatedUser El objeto UserEntity con la información actualizada.
     */
    fun saveUserProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
            // En un futuro, esta función también podría encargarse de sincronizar
            // los cambios con un backend como Firebase.
            // Ejemplo: userRepository.syncProfileWithFirebase(updatedUser)
        }
    }

    /**
     * Solicita al repositorio que sincronice los datos del perfil del usuario
     * desde la fuente remota (ej. Firebase Firestore).
     * Útil para implementar la funcionalidad de "refrescar" con un gesto de swipe-down.
     */
    fun refreshData() {
        viewModelScope.launch {
            userRepository.refreshUserFromRemote()
        }
    }

    /**
     * Cierra la sesión del usuario, limpiando la base de datos local y
     * desconectando de Firebase Auth.
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.clearLocalUser()
        }
    }
}
