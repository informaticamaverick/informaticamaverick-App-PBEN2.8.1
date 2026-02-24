package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de base de datos: Sucursal
 * guarda las sucursales del negocio del prestador
 */

@Entity(
    tableName = "sucursales",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["businessId"])]
)
data class SucursalEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    val nombre: String,
    val direccion: String,
    val codigoPostal: String,
    val telefono: String? = null,
    val email: String? = null,
    val horario: String? = null, //Horario de atención
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)