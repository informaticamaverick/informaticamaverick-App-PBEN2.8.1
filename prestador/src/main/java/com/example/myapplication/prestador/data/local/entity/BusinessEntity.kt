package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de base de datos: Business (negocio)
 * Guarda la información del negocio del prestador
 */

@Entity(tableName = "business")
data class BusinessEntity(
    @PrimaryKey
    val id: String,
    val providerId: String,
    val nombreNegocio: String,
    val razonSocial: String,
    val cuitNegocio: String,
    val direccion: String,
    val codigoPostal: String,
    val telefono: String? = null,
    val email: String? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)