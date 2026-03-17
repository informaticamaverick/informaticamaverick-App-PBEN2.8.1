package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DireccionFirestoreSync @Inject
constructor(
    private val firestore: FirebaseFirestore,
    private val direccionRepository: DireccionRpository
) {
    private val coleccion = firestore.collection("direcciones")

    suspend fun subirDireccion(direccion: DireccionEntity): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to direccion.id,
                "referenciaId" to direccion.referenciaId,
                "referenciaTipo" to direccion.referenciaTipo,
                "pais" to direccion.pais,
                "provincia" to direccion.provincia,
                "localidad" to direccion.localidad,
                "codigoPostal" to direccion.codigoPostal,
                "calle" to direccion.calle,
                "numero" to direccion.numero,
                "latitud" to direccion.latitud,
                "longitud" to direccion.longitud,
                "createdAt" to direccion.createdAt,
                "updatedAt" to direccion.updatedAt
            )
            coleccion.document(direccion.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bajarDireccion(referenciaId: String, referenciaTipo: String):
            Result<DireccionEntity?> {
        return try {
            val snapshot = coleccion
                .whereEqualTo("referenciaId", referenciaId)
                .whereEqualTo("referenciaTipo", referenciaTipo)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull() ?: return Result.success(null)
            val direccion = DireccionEntity(id = doc.getString("id") ?: doc.id,
                referenciaId = doc.getString("referenciaId") ?: referenciaId,
                referenciaTipo = doc.getString("referenciaTipo") ?: referenciaTipo,
                pais = doc.getString("pais") ?: "Argentina",
                provincia = doc.getString("provincia"),
                localidad = doc.getString("localidad"),
                codigoPostal = doc.getString("codigoPostal"),
                calle = doc.getString("calle"),
                numero = doc.getString("numero"),
                latitud = doc.getDouble("latitud"),
                longitud = doc.getDouble("longitud"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )

            direccionRepository.saveDireccion(direccion)
            Result.success(direccion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizar(referenciaId: String, referenciaTipo: String):
            DireccionEntity? {
        val local = direccionRepository.getDireccionByReferencia(referenciaId, referenciaTipo)
        if (local != null) return local
        return bajarDireccion(referenciaId, referenciaTipo).getOrNull()
        }

    suspend fun guardarYSincronizar(
        referenciaId: String,
        referenciaTipo: String,
        pais: String = "Argentina",
        provincia: String? = null,
        localidad: String? = null,
        codigoPostal: String? = null,
        calle: String? = null,
        numero: String? = null,
        latitud: Double? = null,
        longitud: Double? = null
    ): Result<DireccionEntity> {
        return try {
            val result = direccionRepository.upsertDireccion(
                referenciaId = referenciaId,
                referenciaTipo = referenciaTipo,
                pais = pais,
                provincia = provincia,
                localidad = localidad,
                codigoPostal = codigoPostal,
                calle = calle,
                numero = numero,
                latitud = latitud,
                longitud = longitud
            )

            result.getOrNull()?.let { subirDireccion(it) }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}