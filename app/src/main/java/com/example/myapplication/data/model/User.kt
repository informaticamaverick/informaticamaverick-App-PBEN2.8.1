package com.example.myapplication.data.model

import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.CompanyClient

/**
 * --- MODELO DE DOMINIO: User (PERFIL DEL DUEÑO) ---
 *
 * Este objeto se utiliza en la capa de UI y lógica de negocio.
 * Representa la proyección de los datos del dueño de la app (cliente).
 * Se han eliminado campos de perfil profesional y campos planos redundantes.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    
    // --- DATOS PERSONALES ---
    var name: String = "",
    var lastName: String = "",
    var phoneNumber: String = "",
    var bio: String = "",
    var photoUrl: String? = null,
    var bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(), // Galería personal del usuario

    // --- CONTACTOS ADICIONALES ---
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    // --- DIRECCIONES PERSONALES ---
    // Soporta múltiples direcciones (Casa, Oficina, etc.)
    val personalAddresses: List<AddressClient> = emptyList(),

    // --- GESTIÓN DE NEGOCIOS (EMPRESAS) ---
    var hasCompanyProfile: Boolean = false, // Habilita la sección de empresas
    val companies: List<CompanyClient> = emptyList(), // Lista de empresas con sus sucursales
    
    // --- ESTADOS Y BANDERAS ---
    var isProfileComplete: Boolean = false,
    var isSubscribed: Boolean = false,
    var isVerified: Boolean = false,
    var isOnline: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val isPublicProfile: Boolean = false,
    
    // --- SOCIAL Y REPUTACIÓN ---
    val rating: Float = 0f, // Ranking otorgado por prestadores
    val favoriteProviderIds: List<String> = emptyList(), // Prestadores favoritos

    // --- GEOLOCALIZACIÓN Y FECHAS ---
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Nombre completo calculado
     */
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName
        
    /**
     * Dirección principal
     */
    val mainAddress: AddressClient?
        get() = personalAddresses.firstOrNull()
}
