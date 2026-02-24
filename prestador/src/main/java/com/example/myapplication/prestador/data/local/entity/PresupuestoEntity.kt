package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "presupuestos",
    // TEMPORALMENTE DESACTIVADO: Foreign Keys causan crash si no existe el Provider
    // TODO: Re-activar cuando implementemos login y tengamos providerId real
    /*
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["prestadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    */
    indices = [
        Index(value = ["clienteId"]),
        Index(value = ["prestadorId"]),
        Index(value = ["clienteId", "estado"])
    ]
)
data class PresupuestoEntity(
    @PrimaryKey val id: String,
    val numeroPresupuesto: String,
    val clienteId: String,
    val prestadorId: String,
    val fecha: String, // Formato ISO: "2024-01-15"
    val validezDias: Int = 7,
    val subtotal: Double,
    val impuestos: Double,
    val total: Double,
    val estado: String, // "Pendiente", "Aprobado", "Rechazado", "Enviado"
    val notas: String = "",
    val itemsJson: String = "", // Items separados por |
    val serviciosJson: String = "", // Servicios separados por |
    val appointmentId: String? = null, // ID de la cita asociada
    val firestoreId: String? = null,   // ID del doc en Firestore (para sync)
    val syncedAt: Long? = null,        // Timestamp del último sync con Firestore
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)