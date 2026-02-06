package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES ---
 * [ACTUALIZADO] Se añadieron campos de servicios y disponibilidad para coincidir con la UI.
 */
data class CompanyProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val razonSocial: String = "",
    val cuit: String = "",
    // Imágenes a nivel Empresa
    val bannerImageUrl: String? = null,
    val photoUrl: String? = null,
    
    // --- [NUEVOS CAMPOS] Para soporte de PerfilPrestadorScreen ---
    val services: List<String> = emptyList(), // Lista de categorías/servicios
    val works24h: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val acceptsAppointments: Boolean = false, // Turnos en local
    val description: String = "", // Descripción "Sobre Nosotros"
    val productImages: List<String> = emptyList(), // Álbum de trabajos
    
    val branches: List<BranchProvider> = emptyList()
)

/**
 * Modelo de Sucursal
 */
data class BranchProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", 
    val address: AddressProvider = AddressProvider(),
    val employees: List<EmployeeProvider> = emptyList()
)

/**
 * Modelo de Empleado / Persona a Cargo
 */
data class EmployeeProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val position: String = "", // Cargo o puesto
    val detail: String = "",   // Descripción breve
    val photoUrl: String? = null
)
