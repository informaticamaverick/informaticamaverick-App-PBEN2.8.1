package com.example.myapplication.data.model

/**
 * --- MODELO DE DOMINIO PARA PROVEEDORES ---
 * Actualizado para soportar múltiples empresas, sucursales y nuevos booleanos.
 * 🔥 [NUEVO]: Se agregó el campo de horario de atención.
 */
data class Provider(
    val uid: String,

    // > Datos de correo
    val email: String,
    val alternateEmail: String? = null,

    // > Datos Prestador
    val displayName: String,
    val name: String,
    val lastName: String,
    val phoneNumber: String,
    val additionalPhones: List<String> = emptyList(),
    val matricula: String?,
    val titulo: String?,
    val cuilCuit: String?,
    // 💡 [COMENTARIO]: Se consolida a una sola dirección principal en lugar de una lista
    val address: AddressProvider?,

    // > Datos Boolean de Prestador
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isFavorite: Boolean,

    // > Otros datos Prestador
    val rating: Float,
    // 🔥 [NUEVO]: Variable inyectada para almacenar el horario de atención general del prestador.
    // Esto hace match exacto con lo que envía ProviderEntity.toDomain()
    val workingHours: String = "",
    val categories: List<String> = emptyList(), // Cambiado de String a List<String>
    val description: String = "",

    // > Datos de empresas
    val companies: List<CompanyProvider> = emptyList(),
    val hasCompanyProfile: Boolean,

    // > Multimedia y Metadatos
    val photoUrl: String?,
    val bannerImageUrl: String?,
    val galleryImages: List<String> = emptyList(),
    val favoriteProviderIds: List<String> = emptyList(),
    val createdAt: Long
) {
    // --- PROPIEDADES DE COMPATIBILIDAD (Bridge para la UI) ---
    // Esto permite que la UI use .id y .profileImage sin romper el modelo de datos real
    val id: String get() = uid
    val profileImage: String? get() = photoUrl
}

/**
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
**/