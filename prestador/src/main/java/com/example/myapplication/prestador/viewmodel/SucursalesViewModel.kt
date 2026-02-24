package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.data.repository.BusinessRepository
import com.example.myapplication.prestador.data.repository.SucursalRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SucursalesViewModel @Inject constructor(
    private val repository: SucursalRepository,
    private val businessRepository: BusinessRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val providerId: String
        get() = auth.currentUser?.uid ?: ""
    
    // Obtener el businessId real del provider
    private val _businessId = MutableStateFlow<String?>(null)
    val businessId: StateFlow<String?> = _businessId.asStateFlow()

    init {
        loadBusinessId()
    }

    private fun loadBusinessId() {
        viewModelScope.launch {
            try {
                val businesses = businessRepository.getBusinessesByProvider(providerId).first()
                _businessId.value = businesses.firstOrNull()?.id
            } catch (e: Exception) {
                _businessId.value = null
            }
        }
    }

    val sucursales: StateFlow<List<SucursalEntity>> = businessId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getActiveSucursales(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun addSucursal(nombre: String, direccion: String, codigoPostal: String, telefono: String?) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val currentBusinessId = _businessId.value
                if (currentBusinessId == null) {
                    _uiState.value = UiState.Error("Debe completar los datos de empresa primero")
                    return@launch
                }
                
                // Validación
                if (nombre.isBlank()) {
                    _uiState.value = UiState.Error("El nombre de la sucursal es obligatorio")
                    return@launch
                }
                if (direccion.isBlank()) {
                    _uiState.value = UiState.Error("La dirección es obligatoria")
                    return@launch
                }
                if (codigoPostal.isBlank()) {
                    _uiState.value = UiState.Error("El código postal es obligatorio")
                    return@launch
                }
                
                val sucursal = SucursalEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = currentBusinessId,
                    nombre = nombre,
                    direccion = direccion,
                    codigoPostal = codigoPostal,
                    telefono = telefono,
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                repository.saveSucursal(sucursal)
                _uiState.value = UiState.Success("Sucursal agregada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar sucursal")
            }
        }
    }

    fun updateSucursal(sucursal: SucursalEntity) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                // Validación
                if (sucursal.nombre.isBlank()) {
                    _uiState.value = UiState.Error("El nombre de la sucursal es obligatorio")
                    return@launch
                }
                if (sucursal.direccion.isBlank()) {
                    _uiState.value = UiState.Error("La dirección es obligatoria")
                    return@launch
                }
                if (sucursal.codigoPostal.isBlank()) {
                    _uiState.value = UiState.Error("El código postal es obligatorio")
                    return@launch
                }
                
                repository.updateSucursal(sucursal.copy(updatedAt = System.currentTimeMillis()))
                _uiState.value = UiState.Success("Sucursal actualizada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar sucursal")
            }
        }
    }

    fun deleteSucursal(sucursalId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                repository.deleteSucursal(sucursalId)
                _uiState.value = UiState.Success("Sucursal eliminada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar sucursal")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
    
    fun refreshBusinessId() {
        loadBusinessId()
    }
}
