package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.repository.BusinessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessViewModel @Inject constructor(
    private val repository: BusinessRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _business = MutableStateFlow<BusinessEntity?>(null)
    val business: StateFlow<BusinessEntity?> = _business.asStateFlow()

    private val _businesses = MutableStateFlow<List<BusinessEntity>>(emptyList())
    val businesses: StateFlow<List<BusinessEntity>> = _businesses.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadBusiness(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getBusinessById(businessId).collect { business ->
                    _business.value = business
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar negocio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBusinessesByProvider(providerId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getBusinessesByProvider(providerId).collect { businesses ->
                    _businesses.value = businesses
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar negocios: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBusiness(business: BusinessEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveBusiness(business)
                _successMessage.value = "Negocio guardado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar negocio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBusiness(business: BusinessEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateBusiness(business)
                _successMessage.value = "Negocio actualizado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar negocio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBusiness(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteBusiness(businessId)
                _successMessage.value = "Negocio eliminado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar negocio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBusinesses(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.searchBusinessesByName(name).collect { businesses ->
                    _businesses.value = businesses
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al buscar negocios: ${e.message}"
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
