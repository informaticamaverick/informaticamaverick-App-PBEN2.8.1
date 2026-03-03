package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * --- DATA ACCESS OBJECT (DAO) PARA PROVEEDORES ---
 * [ACTUALIZADO] Soporte para flujos individuales por ID y búsqueda dentro de la lista de categorías.
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
     * 🔥 [ACTUALIZADO] Búsqueda por categoría.
     * Como "categories" ahora es una List<String> guardada como JSON,
     * usamos el operador LIKE para buscar la coincidencia dentro de la celda de texto.
     */
    @Query("SELECT * FROM provider_profile WHERE categories LIKE '%' || :category || '%'")
    suspend fun getProvidersByCategory(category: String): List<ProviderEntity>

    /**
     * Obtiene un flujo de datos de un proveedor específico para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    fun getProviderFlowById(providerId: String): Flow<ProviderEntity?>

    @Query("UPDATE provider_profile SET isFavorite = :isFavorite WHERE id = :providerId")
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean)
}



/**
package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * --- DATA ACCESS OBJECT (DAO) PARA PROVEEDORES ---
 * [ACTUALIZADO] Soporte para flujos individuales por ID y búsqueda por categoría.
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
     * 🔥 [NUEVO] Obtiene todos los prestadores de una categoría específica.
     * Utilizado para simular respuestas a licitaciones.
     */
    @Query("SELECT * FROM provider_profile WHERE category = :category")
    suspend fun getProvidersByCategory(category: String): List<ProviderEntity>

    /**
     * Obtiene un flujo de datos de un proveedor específico para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    fun getProviderFlowById(providerId: String): Flow<ProviderEntity?>

    @Query("UPDATE provider_profile SET isFavorite = :isFavorite WHERE id = :providerId")
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean)
}
**/