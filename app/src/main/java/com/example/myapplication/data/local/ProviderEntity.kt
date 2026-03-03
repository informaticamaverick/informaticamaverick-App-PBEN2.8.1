package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider

@Entity(tableName = "provider_profile")
data class ProviderEntity(
    @PrimaryKey val id: String,

    val email: String,
    val alternateEmail: String? = null,
    val displayName: String, // Restaurado

    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val titulo: String? = null,
    val cuilCuit: String? = null,
    val address: AddressProvider? = null,
    val phoneNumber: String,
    val additionalPhones: List<String> = emptyList(),

    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val isFavorite: Boolean = false,
    val isOnline: Boolean = false,

    val rating: Float = 0f,
    val workingHours: String = "", // 🔥 NUEVO: Horario de atención del prestador
    val categories: List<String> = emptyList(),
    val description: String = "",

    val companies: List<CompanyProvider> = emptyList(),
    val hasCompanyProfile: Boolean = false,

    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val favoriteProviderIds: List<String> = emptyList(),
    val createdAt: Long
) {
    /**
     * Convierte la entidad de la base de datos (ProviderEntity) en un objeto de dominio (Provider).
     */
    fun toDomain(): Provider {
        return Provider(
            uid = id,
            email = email,
            alternateEmail = alternateEmail,
            displayName = displayName,
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            additionalPhones = additionalPhones,
            matricula = matricula,
            titulo = titulo,
            cuilCuit = cuilCuit,
            address = address,

            works24h = works24h,
            hasPhysicalLocation = hasPhysicalLocation,
            doesHomeVisits = doesHomeVisits,
            doesShipping = doesShipping,
            acceptsAppointments = acceptsAppointments,
            isSubscribed = isSubscribed,
            isVerified = isVerified,
            isOnline = isOnline,
            isFavorite = isFavorite,

            rating = rating,
            workingHours = workingHours, // 🔥 NUEVO: Mapeo del horario al modelo de dominio
            categories = categories,
            description = description,

            companies = companies,
            hasCompanyProfile = hasCompanyProfile,
            photoUrl = photoUrl,
            bannerImageUrl = bannerImageUrl,
            galleryImages = galleryImages,
            favoriteProviderIds = favoriteProviderIds,
            createdAt = createdAt
        )
    }
}

