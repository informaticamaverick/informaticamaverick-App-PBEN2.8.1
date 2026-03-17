package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.repository.DireccionFirestoreSync
import com.example.myapplication.prestador.data.repository.DireccionRpository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DireccionUiState {
    object Idle : DireccionUiState()
    object Loading : DireccionUiState()
    data class Success(val direccion: DireccionEntity?) : DireccionUiState()
    data class Error(val message: String) : DireccionUiState()
}

sealed class DireccionActionState {
    object Idle : DireccionActionState()
    object Loading : DireccionActionState()
    data class Success(val message: String) : DireccionActionState()
    data class Error(val message: String) : DireccionActionState()
}

@HiltViewModel
class DireccionViewModel @Inject constructor(
    private val direccionRepository: DireccionRpository,
    private val sync: DireccionFirestoreSync
) : ViewModel() {

    private val _uiState = MutableStateFlow<DireccionUiState>(DireccionUiState.Idle)
    val uiState: StateFlow<DireccionUiState> = _uiState.asStateFlow()

    private val _consultorioState = MutableStateFlow<DireccionUiState>(DireccionUiState.Idle)
    val consultorioState: StateFlow<DireccionUiState> = _consultorioState.asStateFlow()

    private val _actionState = MutableStateFlow<DireccionActionState>(DireccionActionState.Idle)
    val actionState: StateFlow<DireccionActionState> = _actionState.asStateFlow()

    /**
     * Carga la dirección asociada a una entidad (prestador, empresa o sucursal).
     * @param referenciaId ID del prestador, empresa o sucursal
     * @param referenciaTipo "PRESTADOR", "EMPRESA" o "SUCURSAL"
     */
    fun loadDireccion(referenciaId: String, referenciaTipo: String) {
        viewModelScope.launch {
            _uiState.value = DireccionUiState.Loading
            try {
                val direccion = sync.sincronizar(referenciaId, referenciaTipo)
                _uiState.value = DireccionUiState.Success(direccion)
            } catch (e: Exception) {
                _uiState.value = DireccionUiState.Error(e.message ?: "Error al cargar dirección")
            }
        }
    }


    fun loadConsultorioDireccion(referenciaId: String) {
        viewModelScope.launch {
            _consultorioState.value = DireccionUiState.Loading
            try {
                val direccion = sync.sincronizar(referenciaId, "CONSULTORIO")
                _consultorioState.value = DireccionUiState.Success(direccion)
            } catch (e: Exception) {
                _consultorioState.value = DireccionUiState.Error(e.message ?: "Error")
            }
        }
    }

    /**
     * Guarda o actualiza la dirección de una entidad (upsert).
     */
    fun guardarDireccion(
        referenciaId: String,
        referenciaTipo: String,
        pais: String = "Argentina",
        provincia: String? = null,
        localidad: String? = null,
        codigoPostal: String? = null,
        calle: String? = null,
        numero: String? = null,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        viewModelScope.launch {
            _actionState.value = DireccionActionState.Loading
            try {
                val result = sync.guardarYSincronizar(
                    referenciaId = referenciaId,
                    referenciaTipo = referenciaTipo,
                    pais = pais,
                    provincia = provincia,
                    localidad = localidad,
                    codigoPostal = codigoPostal,
                    calle = calle,
                    numero = numero,
                    latitud = latitud,
                    longitud = longitud
                )
                if (result.isSuccess) {
                    val direccion = result.getOrNull()
                    _uiState.value = DireccionUiState.Success(direccion)
                    if (referenciaTipo == "CONSULTORIO") {
                        _consultorioState.value = DireccionUiState.Success(direccion)
                    }
                    _actionState.value = DireccionActionState.Success("Dirección guardada")
                } else {
                    _actionState.value = DireccionActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al guardar dirección"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = DireccionActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Elimina la dirección de una entidad.
     */
    fun eliminarDireccion(direccion: DireccionEntity) {
        viewModelScope.launch {
            _actionState.value = DireccionActionState.Loading
            try {
                val result = direccionRepository.deleteDireccion(direccion)
                if (result.isSuccess) {
                    _uiState.value = DireccionUiState.Success(null)
                    _actionState.value = DireccionActionState.Success("Dirección eliminada")
                } else {
                    _actionState.value = DireccionActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al eliminar dirección"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = DireccionActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = DireccionActionState.Idle
    }
}
