package com.example.myapplication.data.model

/**
 * --- MODELO DE DOMINIO PARA PROVEEDORES ---
 *
 * Propósito:
 * Representa a un proveedor dentro de la lógica de negocio de la aplicación.
 * Esta es la clase que el ViewModel y la UI consumirán.
 */
data class Provider(
    val uid: String,
    val email: String,
    val displayName: String,
    val name: String,
    val lastName: String,
    val phoneNumber: String,
    val category: String, // Añadido para ChatScreen y Filtros
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),
    val matricula: String?,
    val titulo: String?,
    val photoUrl: String?,
    val bannerImageUrl: String?,
    val galleryImages: List<String> = emptyList(),
    val personalAddresses: List<AddressProvider> = emptyList(),
    val companies: List<CompanyProvider> = emptyList(),
    val hasCompanyProfile: Boolean,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isFavorite: Boolean,
    val rating: Float,
    val createdAt: Long
) {
    // --- PROPIEDADES DE COMPATIBILIDAD (Bridge para la UI) ---
    // Esto permite que la UI use .id y .profileImage sin romper el modelo de datos real
    val id: String get() = uid
    val profileImage: String? get() = photoUrl
}
