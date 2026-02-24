package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES ---
 *
 * Estructura solicitada:
 * > Mis Negocios ( puede tener 1 o mas negocios)
 *   - Nombre de la Empresa
 *   - Razon Social
 *   - CUIT
 *   - Sucursal (Puede Tener mas de 1 por empresa)
 */
data class CompanyClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val razonSocial: String = "",
    val cuit: String = "",
    // Imágenes a nivel Empresa (opcional pero recomendado para el header)
    val bannerImageUrl: String? = null,
    val photoUrl: String? = null,
    val branches: List<BranchClient> = emptyList()
)

/**
 * Modelo de Sucursal
 * Estructura solicitada:
 * - Descripcion de la sucursal (Nombre)
 * - Direccion Completa (AddressClient)
 * - Persona Acargo de la sucursal (RepresentativeClient)
 */
data class BranchClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // "Descripcion de la sucursal"
    val address: AddressClient = AddressClient(),
    // Se mantiene como lista por flexibilidad, aunque la UI puede mostrar solo 1
    val representatives: List<RepresentativeClient> = emptyList()
)

/**
 * Modelo de Representante / Persona a Cargo
 * Estructura solicitada:
 * - Nombre
 * - Cargo
 * - Foto o imagen
 */
data class RepresentativeClient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val apellido: String = "",
    val cargo: String = "",
    val photoUrl: String? = null // Nueva propiedad para la foto del representante
)
