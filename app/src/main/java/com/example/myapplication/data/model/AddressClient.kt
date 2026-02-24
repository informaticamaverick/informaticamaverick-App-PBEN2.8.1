package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN CLIENTE (AddressClient) ---
 *
 * Exclusivo para el módulo CLIENTE.
 * Representa direcciones personales o de las empresas del cliente.
 *
 * Estructura solicitada:
 * - Calle y numero
 * - localidad
 * - Provincia/Estado
 * - Pais
 * - Codigo Postal o ZipCode
 *
 * @param id Identificador único para la dirección (Útil para listas en Compose).
 * @param calle Nombre de la calle.
 * @param numero Número de la dirección (ej: "123").
 * @param localidad Ciudad o localidad.
 * @param provincia Provincia o estado.
 * @param pais País.
 * @param codigoPostal Código postal (ZIP code).
 */
data class AddressClient(
    val id: String = UUID.randomUUID().toString(),
    var calle: String = "",
    var numero: String = "",
    var localidad: String = "",
    var provincia: String = "",
    var pais: String = "",
    var codigoPostal: String = ""
) {
    /**
     * Devuelve una representación completa y legible de la dirección.
     * Ejemplo: "Av. Siempre Viva 742, Springfield, EE. UU."
     */
    fun fullString(): String {
        val calleYNumero = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
