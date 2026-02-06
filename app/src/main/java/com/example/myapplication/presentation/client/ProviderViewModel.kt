package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL PARA PROVEEDORES ---
 * [ACTUALIZADO] Soporte para Hilt y flujos de datos reales de Room.
 * Provee la lógica de negocio para la lista de prestadores y perfiles individuales.
 */
@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val repository: ProviderRepository
) : ViewModel() {

    // --- [ESTADOS DE UI] ---

    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers: StateFlow<List<Provider>> = _providers.asStateFlow()

    private val _favorites = MutableStateFlow<List<Provider>>(emptyList())
    val favorites: StateFlow<List<Provider>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 🔥 Carga inicial de datos desde Room
        loadAllProviders()
        loadFavoriteProviders()
    }

    // --- [SECCIÓN: CARGA DE DATOS] ---

    private fun loadAllProviders() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.allProviders
                .catch { e ->
                    _error.value = "Error al cargar los proveedores: ${e.message}"
                    _isLoading.value = false
                }
                .collect { providerList ->
                    _providers.value = providerList
                    _isLoading.value = false
                }
        }
    }

    private fun loadFavoriteProviders() {
        viewModelScope.launch {
            repository.favoriteProviders
                .catch { e -> _error.value = "Error al cargar los favoritos: ${e.message}" }
                .collect { favoriteList ->
                    _favorites.value = favoriteList
                }
        }
    }

    // --- [SECCIÓN: OPERACIONES DE NEGOCIO] ---

    /**
     * 🔥 Obtiene un flujo de datos de un proveedor específico desde Room.
     * Este flujo es reactivo: si Room se actualiza (vía Firebase a futuro), la UI también.
     */
    fun getProviderById(providerId: String) = repository.getProviderById(providerId)

    /**
     * Alterna el estado de favorito de un prestador.
     */
    fun toggleFavoriteStatus(providerId: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStatus(providerId, !isCurrentlyFavorite)
            } catch (e: Exception) {
                _error.value = "Error al actualizar el estado de favorito: ${e.message}"
            }
        }
    }

    // --- [SECCIÓN: HELPERS DE FILTRADO] ---

    fun isProvider24h(provider: Provider): Boolean = 
        provider.companies.any { it.works24h }

    fun doesProviderHomeVisits(provider: Provider): Boolean = 
        provider.companies.any { it.doesHomeVisits }

    fun hasProviderPhysicalLocation(provider: Provider): Boolean = 
        provider.companies.any { it.hasPhysicalLocation }
}
