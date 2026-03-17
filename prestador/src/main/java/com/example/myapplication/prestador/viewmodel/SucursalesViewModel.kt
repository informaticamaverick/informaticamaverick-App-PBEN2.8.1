package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.data.repository.BusinessRepository
import com.example.myapplication.prestador.data.repository.DireccionRpository
import com.example.myapplication.prestador.data.repository.ReferenteRepository
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
    private val auth: FirebaseAuth,
    private val direccionRepository: DireccionRpository,
    private val referenteRepository: ReferenteRepository
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
        .onEach { lista -> loadAuxDataForSucursales(lista) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dirección y encargado por sucursalId
    private val _direccionesPorSucursal = MutableStateFlow<Map<String, DireccionEntity?>>(emptyMap())
    val direccionesPorSucursal: StateFlow<Map<String, DireccionEntity?>> = _direccionesPorSucursal.asStateFlow()

    private val _encargadosPorSucursal = MutableStateFlow<Map<String, ReferenteEntity?>>(emptyMap())
    val encargadosPorSucursal: StateFlow<Map<String, ReferenteEntity?>> = _encargadosPorSucursal.asStateFlow()

    private val _equipoPorSucursal = MutableStateFlow<Map<String, List<ReferenteEntity>>>(emptyMap())
    val equipoPorSucursal: StateFlow<Map<String, List<ReferenteEntity>>> = _equipoPorSucursal.asStateFlow()

    private fun loadAuxDataForSucursales(sucursales: List<SucursalEntity>) {
        viewModelScope.launch {
            val direcciones = mutableMapOf<String, DireccionEntity?>()
            val encargados = mutableMapOf<String, ReferenteEntity?>()
            val equipo = mutableMapOf<String, List<ReferenteEntity>>()
            sucursales.forEach { suc ->
                direcciones[suc.id] = try {
                    direccionRepository.getDireccionByReferencia(suc.id, "SUCURSAL")
                } catch (e: Exception) { null }
                val todosActivos = try {
                    referenteRepository.getReferentesBySucursal(suc.id).filter { it.activo }
                } catch (e: Exception) { emptyList() }
                val encargado = todosActivos.firstOrNull { it.id == suc.referenteId }
                    ?: todosActivos.firstOrNull()
                encargados[suc.id] = encargado
                equipo[suc.id] = todosActivos.filter { it.id != encargado?.id }
            }
            _direccionesPorSucursal.value = direcciones
            _encargadosPorSucursal.value = encargados
            _equipoPorSucursal.value = equipo
        }
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun addSucursal(
        nombre: String,
        provincia: String?,
        localidad: String?,
        calle: String?,
        numero: String?,
        cp: String?,
        horario: String?
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val currentBusinessId = _businessId.value
                if (currentBusinessId == null) {
                    _uiState.value = UiState.Error("Debe completar los datos de empresa primero")
                    return@launch
                }
                
                if (nombre.isBlank()) {
                    _uiState.value = UiState.Error("El nombre de la sucursal es obligatorio")
                    return@launch
                }
                
                val sucursal = SucursalEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = currentBusinessId,
                    nombre = nombre.trim(),
                    horario = horario?.takeIf { it.isNotBlank() },
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                repository.saveSucursal(sucursal)

                // Guardar dirección si se proporcionaron datos
                if (!provincia.isNullOrBlank() || !calle.isNullOrBlank()) {
                    direccionRepository.upsertDireccion(
                        referenciaId = sucursal.id,
                        referenciaTipo = "SUCURSAL",
                        pais = "Argentina",
                        provincia = provincia ?: "",
                        localidad = localidad ?: "",
                        codigoPostal = cp ?: "",
                        calle = calle ?: "",
                        numero = numero ?: ""
                    )
                    val nuevaDireccion = direccionRepository.getDireccionByReferencia(sucursal.id, "SUCURSAL")
                    _direccionesPorSucursal.value = _direccionesPorSucursal.value.toMutableMap().also {
                        it[sucursal.id] = nuevaDireccion
                    }
                }

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

    fun agregarMiembroEquipo(sucursalId: String, nombre: String, apellido: String?, cargo: String?, imageUrl: String? = null) {
        viewModelScope.launch {
            try {
                referenteRepository.addReferente(
                    providerId = providerId,
                    nombre = nombre,
                    apellido = apellido,
                    cargo = cargo,
                    imageUrl = imageUrl,
                    sucursalId = sucursalId
                )
                loadAuxDataForSucursales(sucursales.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar miembro")
            }
        }
    }

    fun desactivarMiembroEquipo(referenteId: String) {
        viewModelScope.launch {
            try {
                referenteRepository.desactivarReferente(referenteId)
                loadAuxDataForSucursales(sucursales.value)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al quitar miembro")
            }
        }
    }

    fun guardarDireccionSucursal(
        sucursalId: String,
        pais: String = "Argentina",
        provincia: String,
        localidad: String,
        codigoPostal: String,
        calle: String,
        numero: String
    ) {
        viewModelScope.launch {
            try {
                direccionRepository.upsertDireccion(
                    referenciaId = sucursalId,
                    referenciaTipo = "SUCURSAL",
                    pais = pais,
                    provincia = provincia,
                    localidad = localidad,
                    codigoPostal = codigoPostal,
                    calle = calle,
                    numero = numero
                )
                // Recargar dirección para esa sucursal
                val nueva = direccionRepository.getDireccionByReferencia(sucursalId, "SUCURSAL")
                _direccionesPorSucursal.value = _direccionesPorSucursal.value.toMutableMap().also {
                    it[sucursalId] = nueva
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al guardar dirección")
            }
        }
    }

    fun guardarEncargadoSucursal(
        sucursalId: String,
        nombre: String,
        apellido: String?,
        cargo: String?,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            try {
                val actual = _encargadosPorSucursal.value[sucursalId]
                val updated: ReferenteEntity
                if (actual != null) {
                    updated = actual.copy(
                        nombre = nombre.trim(),
                        apellido = apellido?.trim(),
                        cargo = cargo?.trim(),
                        imageUrl = imageUrl ?: actual.imageUrl,
                        updatedAt = System.currentTimeMillis()
                    )
                    referenteRepository.updateReferente(updated)
                } else {
                    val result = referenteRepository.addReferente(
                        providerId = providerId,
                        nombre = nombre,
                        apellido = apellido,
                        cargo = cargo,
                        imageUrl = imageUrl,
                        sucursalId = sucursalId
                    )
                    if (result.isFailure) {
                        _uiState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Error")
                        return@launch
                    }
                    updated = result.getOrNull()!!
                }
                _encargadosPorSucursal.value = _encargadosPorSucursal.value.toMutableMap().also {
                    it[sucursalId] = updated
                }
                // Vincular el referente como encargado en la entidad sucursal
                val sucursalActual = sucursales.value.find { it.id == sucursalId }
                if (sucursalActual != null && sucursalActual.referenteId != updated.id) {
                    try {
                        repository.updateSucursal(sucursalActual.copy(referenteId = updated.id, updatedAt = System.currentTimeMillis()))
                    } catch (e: Exception) { /* best effort */ }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al guardar encargado")
            }
        }
    }
}
