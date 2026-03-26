package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.ProviderEntity
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProviderRepository(
    private val providerDao: ProviderDao,
    private val firestore: FirebaseFirestore
) {

    val allProviders: Flow<List<Provider>> = providerDao.getAllProviders().map {
        entities -> entities.map { it.toDomain() }
    }

    val favoriteProviders: Flow<List<Provider>> = providerDao.getFavoriteProviders().map {
        entities -> entities.map { it.toDomain() }
    }

    fun getProviderById(providerId: String): Flow<Provider?> {
        return providerDao.getProviderFlowById(providerId).map { it?.toDomain() }
    }

    suspend fun getProvidersByCategory(category: String): List<Provider> {
        return providerDao.getProvidersByCategory(category).map { it.toDomain() }
    }

    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean) {
        providerDao.updateFavoriteStatus(providerId, isFavorite)
    }

    /**
     * Descarga todos los prestadores desde Firestore y los guarda en Room.
     * Busca en colección "usuarios" donde el array "roles" contiene "prestador".
     */
    suspend fun syncFromFirestore() {
        try {
            val snapshot = firestore.collection("usuarios")
                .whereArrayContains("roles", "prestador")
                .get()
                .await()

            if (snapshot.isEmpty) return

            val entities = snapshot.documents.mapNotNull { doc ->
                try {
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val apellido = doc.getString("apellido") ?: ""
                    val email = doc.getString("email") ?: return@mapNotNull null

                    // Maneja la inconsistencia entre PrestadorRegisterViewModel y EditProfileViewModel.
                    // Register escribe: is24Hours, isHomeService, hasPhysicalStore, hasStoreAppointments
                    // EditProfile escribe: atencionUrgencias, vaDomicilio, turnosEnLocal
                    // Prioridad: EditProfileViewModel (atencionUrgencias, vaDomicilio, turnosEnLocal)
                    // Fallback: PrestadorRegisterViewModel (is24Hours, isHomeService, hasPhysicalStore)
                    val works24h = doc.getBoolean("atencionUrgencias")
                        ?: doc.getBoolean("is24Hours") ?: false
                    val doesHomeVisits = doc.getBoolean("vaDomicilio")
                        ?: doc.getBoolean("isHomeService") ?: false
                    val hasPhysicalLocation = doc.getBoolean("turnosEnLocal")
                        ?: doc.getBoolean("hasPhysicalStore") ?: false
                    val acceptsAppointments = doc.getBoolean("turnosEnLocal")
                        ?: doc.getBoolean("hasStoreAppointments") ?: false

                    @Suppress("UNCHECKED_CAST")
                    val servicios = (doc.get("servicios") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()

                    val address = AddressProvider(
                        calle = doc.getString("direccion") ?: "",
                        provincia = doc.getString("provincia") ?: "",
                        codigoPostal = doc.getString("codigoPostal") ?: "",
                        pais = doc.getString("pais") ?: "Argentina"
                    )

                    ProviderEntity(
                        id = doc.id, // Firebase Auth UID del prestador
                        email = email,
                        displayName = "$nombre $apellido".trim(),
                        name = nombre,
                        lastName = apellido,
                        phoneNumber = doc.getString("telefono") ?: "",
                        photoUrl = doc.getString("imageBase64") ?: doc.getString("imagenUrl"),
                        description = doc.getString("description") ?: "",
                        categories = servicios,
                        rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                        matricula = doc.getString("matricula"),
                        titulo = doc.getString("titulo") ?: doc.getString("profesion") ?: "",
                        cuilCuit = doc.getString("dniCuit"),
                        address = address,
                        works24h = works24h,
                        doesHomeVisits = doesHomeVisits,
                        hasPhysicalLocation = hasPhysicalLocation,
                        acceptsAppointments = acceptsAppointments,
                        doesShipping = doc.getBoolean("envios") ?: false,
                        workingHours = doc.getString("horarioLocal") ?: "",
                        hasCompanyProfile = doc.getBoolean("tieneEmpresa") ?: false,
                        isSubscribed = doc.getBoolean("suscripto") ?: false,
                        isVerified = doc.getBoolean("verificado") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("ProviderRepo", "Error mapeando prestador ${doc.id}: ${e.message}")
                    null
                }
            }

            if (entities.isNotEmpty()) {
                providerDao.insertAll(entities)
                Log.d("ProviderRepo", "Sync OK: ${entities.size} prestadores guardados en Room")
            }
        } catch (e: Exception) {
            // Fallo silencioso: la app sigue funcionando con los datos que ya tiene en Room
            Log.e("ProviderRepo", "Error sync Firestore: ${e.message}")
        }
    }
}