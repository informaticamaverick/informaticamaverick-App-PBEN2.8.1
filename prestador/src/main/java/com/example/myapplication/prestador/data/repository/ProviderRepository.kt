package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * REPOSITORY para Providers
 * 
 * El Repository es el intermediario entre el ViewModel y el DAO.
 * Ventajas:
 * - Encapsula la lógica de acceso a datos
 * - Puede combinar datos de múltiples fuentes (BD local + API)
 * - Hace el código más testeable
 * - Separa responsabilidades (MVVM)
 * 
 * @Inject constructor = Hilt inyecta automáticamente el DAO
 * @Singleton = Una sola instancia para toda la app
 */
@Singleton
class ProviderRepository @Inject constructor(
    private val providerDao: ProviderDao
) {
    /**
     * OBTENER prestador por ID (observando cambios)
     * Flow = Emite automáticamente cuando cambia en la BD
     */
    fun getProviderById(id: String): Flow<ProviderEntity?> {
        return providerDao.getProviderById(id)
    }
    
    /**
     * OBTENER prestador por ID (una sola vez)
     */
    suspend fun getProviderByIdOnce(id: String): ProviderEntity? {
        return providerDao.getProviderByIdOnce(id)
    }
    
    /**
     * GUARDAR o ACTUALIZAR prestador
     * Si el ID existe, actualiza. Si no, inserta nuevo.
     */
    suspend fun saveProvider(provider: ProviderEntity) {
        providerDao.insertProvider(provider)
    }
    
    /**
     * ACTUALIZAR prestador existente
     */
    suspend fun updateProvider(provider: ProviderEntity) {
        providerDao.updateProvider(provider)
    }
    
    /**
     * ELIMINAR prestador por ID
     */
    suspend fun deleteProvider(providerId: String) {
        providerDao.deleteProviderById(providerId)
    }
    
    /**
     * ELIMINAR prestador entity
     */
    suspend fun deleteProviderEntity(provider: ProviderEntity) {
        providerDao.deleteProvider(provider)
    }
    
    /**
     * BUSCAR prestadores por nombre
     */
    fun searchProviders(query: String): Flow<List<ProviderEntity>> {
        return providerDao.searchProviders("%$query%")
    }
    
    /**
     * OBTENER todos los prestadores
     */
    fun getAllProviders(): Flow<List<ProviderEntity>> {
        return providerDao.getAllProviders()
    }
    
    /**
     * VERIFICAR si existe un prestador
     */
    suspend fun providerExists(id: String): Boolean {
        return providerDao.providerExists(id)
    }
    
    /**
     * ACTUALIZAR solo la imagen del prestador
     */
    suspend fun updateProviderImage(id: String, imageUrl: String) {
        providerDao.updateProviderImage(id, imageUrl)
    }
    
    /**
     * ACTUALIZAR el rating del prestador
     */
    suspend fun updateProviderRating(id: String, rating: Float) {
        providerDao.updateProviderRating(id, rating)
    }
    
    /**
     * CREAR un nuevo prestador con datos básicos
     * Método helper para facilitar la creación
     */
    suspend fun createProvider(
        id: String,
        name: String,
        email: String,
        phone: String,
        imageUrl: String? = null,
        description: String? = null
    ): ProviderEntity {
        val provider = ProviderEntity(
            id = id,
            name = name,
            email = email,
            phone = phone,
            imageUrl = imageUrl,
            description = description
        )
        saveProvider(provider)
        return provider
    }
}
