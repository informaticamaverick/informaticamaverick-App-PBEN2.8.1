package com.example.myapplication.data.model

/**
 * --- MODELO DE USUARIO (User) ---
 * 
 * Este es el modelo principal del CLIENTE.
 * Se sincroniza con la colección 'users' en Firestore.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    
    // Datos Personales
    var name: String = "",
    var lastName: String = "",
    var phoneNumber: String = "",
    
    // Listas de contacto
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    // Perfil Profesional
    var matricula: String? = null,
    var titulo: String? = null,
    
    // Multimedia
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),

    // --- UBICACIONES (Usando modelo AddressClient) ---
    val personalAddresses: List<AddressClient> = emptyList(),

    // --- EMPRESAS (Usando modelo CompanyClient) ---
    // 🔥 FIREBASE: Al guardar esto, se debe verificar si se crea un documento espejo en 'prestadores'
    val companies: List<CompanyClient> = emptyList(),

    // Estados
    val hasCompanyProfile: Boolean = false,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    
    // Social / Prestador
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val favoriteProviderIds: List<String> = emptyList(),

    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName
        
    val mainAddress: AddressClient?
        get() = personalAddresses.firstOrNull()
}
