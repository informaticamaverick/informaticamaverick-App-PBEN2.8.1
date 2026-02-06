package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider

/**
 * ENTIDAD DE PROVEEDOR (ProviderEntity)
 * Representa la tabla 'provider_profile' en la base de datos Room.
 */
@Entity(tableName = "provider_profile")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val name: String,
    val lastName: String,
    val phoneNumber: String,
    val category: String = "General", // Campo añadido para la BD

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
            displayName = displayName,
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            category = category, // Pasamos la categoría al dominio
            additionalEmails = additionalEmails,
            additionalPhones = additionalPhones,
            matricula = matricula,
            titulo = titulo,
            photoUrl = photoUrl,
            bannerImageUrl = bannerImageUrl,
            galleryImages = galleryImages,
            personalAddresses = personalAddresses,
            companies = companies,
            hasCompanyProfile = hasCompanyProfile,
            isSubscribed = isSubscribed,
            isVerified = isVerified,
            isOnline = isOnline,
            isFavorite = isFavorite,
            rating = rating,
            createdAt = createdAt
        )
    }
}
