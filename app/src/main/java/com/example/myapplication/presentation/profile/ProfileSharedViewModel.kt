
package com.example.myapplication.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel compartido para gestionar los datos del perfil de usuario.
 *
 * Este ViewModel centraliza la lógica para obtener, actualizar y guardar la información del usuario,
 * asegurando que los cambios se reflejen de manera consistente en toda la app.
 *
 * @param loginRepository El repositorio que maneja tanto la lógica de login como el acceso a datos locales del usuario.
 */
@HiltViewModel
class ProfileSharedViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    // 1. Exponer el estado del usuario como un StateFlow
    // El usuario se carga desde el repositorio y se convierte en un StateFlow.
    // `stateIn` convierte el Flow frío del repositorio en un Flow caliente, compartiendo el último valor emitido
    // con todos los colectores. Se mantiene activo mientras el ViewModelScope esté activo.
    val userState: StateFlow<UserEntity?> = loginRepository.getCurrentUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // El upstream flow se inicia cuando hay subscriptores y se detiene 5s después de que el último se vaya
            initialValue = null // Valor inicial mientras se carga el primer dato
        )

    /**
     * Guarda el perfil del usuario actualizado.
     *
     * @param updatedUser La entidad UserEntity con los datos modificados.
     */
    fun saveUserProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            // La actualización se delega completamente al repositorio.
            // El StateFlow `userState` se actualizará automáticamente gracias a que observa la BD.
            loginRepository.saveUserProfile(updatedUser)
        }
    }

    /**
     * Cierra la sesión del usuario.
     */
    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
        }
    }
}
