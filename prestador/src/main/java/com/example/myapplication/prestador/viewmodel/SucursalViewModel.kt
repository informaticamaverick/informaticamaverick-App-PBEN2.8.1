package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.SucursalEntity
import com.example.myapplication.prestador.data.repository.SucursalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SucursalViewModel @Inject constructor(
    private val repository: SucursalRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sucursal = MutableStateFlow<SucursalEntity?>(null)
    val sucursal: StateFlow<SucursalEntity?> = _sucursal.asStateFlow()

    private val _sucursales = MutableStateFlow<List<SucursalEntity>>(emptyList())
    val sucursales: StateFlow<List<SucursalEntity>> = _sucursales.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadSucursal(sucursalId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getSucursalById(sucursalId).collect { sucursal ->
                    _sucursal.value = sucursal
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar sucursal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSucursalesByBusiness(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getSucursalesByBusiness(businessId).collect { sucursales ->
                    _sucursales.value = sucursales
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar sucursales: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadActiveSucursales(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getActiveSucursales(businessId).collect { sucursales ->
                    _sucursales.value = sucursales
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar sucursales activas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSucursal(sucursal: SucursalEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveSucursal(sucursal)
                _successMessage.value = "Sucursal guardada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar sucursal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSucursal(sucursal: SucursalEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateSucursal(sucursal)
                _successMessage.value = "Sucursal actualizada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar sucursal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSucursal(sucursalId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteSucursal(sucursalId)
                _successMessage.value = "Sucursal eliminada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar sucursal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSucursalStatus(sucursalId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateSucursalStatus(sucursalId, isActive)
                _successMessage.value = "Estado actualizado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar estado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchSucursales(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.searchSucursalesByName(name).collect { sucursales ->
                    _sucursales.value = sucursales
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al buscar sucursales: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
