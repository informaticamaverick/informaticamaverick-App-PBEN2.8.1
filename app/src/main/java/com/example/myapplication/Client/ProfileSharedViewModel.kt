package com.example.myapplication.Client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.screens.ProfileMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VIEWMODEL COMPARTIDO DE PERFIL (ProfileSharedViewModel)
 * 
 * Este componente es el corazón de la lógica de usuario en la UI.
 * Centraliza el estado del modo (Cliente/Empresa) y los datos del perfil
 * para que todas las pantallas estén sincronizadas.
 */
@HiltViewModel
class ProfileSharedViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // =================================================================================
    // --- SECCIÓN: ESTADO DE DATOS DEL USUARIO ---
    // =================================================================================

    // Estado interno del usuario (UserEntity proveniente de Room)
    private val _userState = MutableStateFlow<UserEntity?>(null)
    // Estado público observado por la UI
    val userState: StateFlow<UserEntity?> = _userState.asStateFlow()

    // =================================================================================
    // --- SECCIÓN: ESTADO DEL MODO DE PERFIL ---
    // =================================================================================

    // Controla si la app se muestra como Cliente o Empresa
    private val _profileMode = MutableStateFlow(ProfileMode.CLIENTE)
    val profileMode: StateFlow<ProfileMode> = _profileMode.asStateFlow()

    // =================================================================================
    // --- SECCIÓN: INICIALIZACIÓN ---
    // =================================================================================

    init {
        // 1. Nos suscribimos al flujo de datos del Repositorio (Fuente Única de Verdad: Room)
        viewModelScope.launch {
            // Se suscribe al Flow del repositorio. Cada vez que los datos del usuario
            // cambien en la base de datos local (Room), este bloque se ejecutará
            // y actualizará el _userState.
            userRepository.userProfile.collect { userEntity ->
                _userState.value = userEntity
            }
        }
        
        // 2. Inicializamos/Sincronizamos datos al arrancar
        // 🔥 Futuro: Aquí se verificaría la sesión en Firebase Auth para cargar el perfil real
        viewModelScope.launch {
            userRepository.refreshUserFromRemote()
        }
    }

    // =================================================================================
    // --- SECCIÓN: ACCIONES DE LA UI (EVENTOS) ---
    // =================================================================================

    /**
     * Guarda los cambios del perfil de forma persistente en Room.
     * Recibe el objeto UserEntity completo para asegurar que todos los campos (listas, etc.) se guarden.
     */
    fun saveUserProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            // Guardamos en Room a través del repositorio
            userRepository.updateUser(updatedUser)
            
            // 🔥 Sincronización futura:
            // userRepository.syncWithFirebase(updatedUser)
        }
    }

    /**
     * Alterna globalmente entre el modo Cliente y Empresa.
     */
    fun toggleProfileMode() {
        _profileMode.update { currentMode ->
            if (currentMode == ProfileMode.CLIENTE) ProfileMode.EMPRESA else ProfileMode.CLIENTE
        }
    }
    
    /**
     * Fuerza una recarga de datos.
     * 🔥 Útil para refrescar datos desde Firebase manualmente.
     */
    fun refreshData() {
        viewModelScope.launch {
            userRepository.refreshUserFromRemote()
        }
    }
}
