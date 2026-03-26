package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES ---
 * 
 * Estructura:
 * - Una empresa puede tener múltiples sucursales.
 * - Cada sucursal tiene su propia dirección, equipo de trabajo y galería.
 */
data class CompanyClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Nombre Comercial
    val razonSocial: String = "",
    val cuit: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bannerImageUrl: String? = null,
    val photoUrl: String? = null,
    val branches: List<BranchClient> = emptyList()
)

/**
 * Modelo de Sucursal
 * - Cada sucursal tiene UNA ÚNICA dirección principal.
 * - Cada sucursal tiene un equipo de trabajo (representantes).
 * - Cada sucursal tiene su propia galería de fotos.
 */
data class BranchClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // "Sucursal Centro", "Casa Central", etc.
    val isMainBranch: Boolean = false, // Para identificar la "Casa Central"
    val address: AddressClient = AddressClient(), // UNA SOLA DIRECCIÓN
    val representatives: List<RepresentativeClient> = emptyList(), // Equipo de trabajo
    val galleryImages: List<String> = emptyList() // Galería de la sucursal
)

/**
 * Modelo de Representante / Equipo de Trabajo
 */
data class RepresentativeClient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val apellido: String = "",
    val cargo: String = "",
    val photoUrl: String? = null
)
