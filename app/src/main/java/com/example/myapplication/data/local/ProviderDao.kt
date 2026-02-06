package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * --- DATA ACCESS OBJECT (DAO) PARA PROVEEDORES ---
 * [ACTUALIZADO] Soporte para flujos individuales por ID.
 */
@Dao
interface ProviderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Query("SELECT * FROM provider_profile")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM provider_profile WHERE isFavorite = 1")
    fun getFavoriteProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    suspend fun getProviderById(providerId: String): ProviderEntity?

    /**
     * Obtiene un flujo de datos de un proveedor específico para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    fun getProviderFlowById(providerId: String): Flow<ProviderEntity?>

    @Query("UPDATE provider_profile SET isFavorite = :isFavorite WHERE id = :providerId")
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean)
}
