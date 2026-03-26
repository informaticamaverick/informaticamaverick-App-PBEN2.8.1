package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN CLIENTE (AddressClient) ---
 */
data class AddressClient(
    val id: String = UUID.randomUUID().toString(),
    val calle: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "",
    val codigoPostal: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val label: String = "" // Ej: "Casa", "Sucursal Centro"
) {
    fun fullString(): String {
        val calleYNumero = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
