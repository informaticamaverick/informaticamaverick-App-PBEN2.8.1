package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES ---
 * [ACTUALIZADO] Se añadieron campos de imágenes para soportar perfiles de empresa completos
 * y galerías por sucursal.
 */
data class CompanyProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Nombre de fantasía
    val razonSocial: String = "",
    val cuit: String = "",
    val description: String = "", // Detalle o descripción de la empresa
    val rating: Float = 0f, // Rating de la empresa

    val categories: List<String> = emptyList(), // Categorías de la empresa
    val productImages: List<String> = emptyList(), // Álbum de trabajos de la empresa (fallback)
    
    // --- [NUEVOS DATOS] Imágenes de Perfil y Banner de Empresa ---
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,

    // --- Características de la Empresa ---
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val isVerified: Boolean = false, // Verificación a nivel empresa
    val workingHours: String = "", // Horario de atención de la empresa en general

    // --- Estructura de Casa Central y Sucursales ---
    val mainBranch: BranchProvider? = null,
    val branches: List<BranchProvider> = emptyList()
)

data class BranchProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Ej: "Sucursal Norte" o "Casa Central"
    val address: AddressProvider = AddressProvider(), // *Direccion
    val employees: List<EmployeeProvider> = emptyList(), // *Referente o equipo de trabajo
    
    // --- [NUEVOS DATOS] Galería de imágenes por sucursal ---
    val galleryImages: List<String> = emptyList(),

    // --- Características específicas de ESTA sucursal ---
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val isVerified: Boolean = false, // Por si una sucursal está verificada y otra no
    val rating: Float = 0f, // Calificación independiente por sucursal
    val workingHours: String = "" // Horario específico de esta sucursal
)

data class EmployeeProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val position: String = "", // Rol (Ej: Referente, Técnico)
    val detail: String = "",
    val photoUrl: String? = null
)
