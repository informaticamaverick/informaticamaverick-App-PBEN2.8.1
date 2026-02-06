package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.model.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * --- REPOSITORIO PARA PROVEEDORES ---
 * [ACTUALIZADO] Soporte para búsqueda por ID y flujo de datos real.
 */
class ProviderRepository(private val providerDao: ProviderDao) {

    /**
     * Flujo de todos los proveedores, mapeados al modelo de dominio `Provider`.
     */
    val allProviders: Flow<List<Provider>> = providerDao.getAllProviders().map {
        entities -> entities.map { it.toDomain() }
    }

    /**
     * Flujo de los proveedores favoritos, mapeados al modelo de dominio `Provider`.
     */
    val favoriteProviders: Flow<List<Provider>> = providerDao.getFavoriteProviders().map {
        entities -> entities.map { it.toDomain() }
    }

    /**
     * Obtiene un proveedor específico por su ID y lo emite como un flujo de datos de dominio.
     */
    fun getProviderById(providerId: String): Flow<Provider?> {
        return providerDao.getProviderFlowById(providerId).map { it?.toDomain() }
    }

    /**
     * Actualiza el estado de favorito de un proveedor.
     */
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean) {
        providerDao.updateFavoriteStatus(providerId, isFavorite)
    }
}
