package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AddressClient // <--- CRUCIAL IMPORTARLO
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.User // <--- CRUCIAL (Ver archivo abajo)

/**
 * ENTIDAD DE USUARIO (UserEntity)
 * Persistencia local del perfil de Cliente.
 */
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String,

    val email: String,
    val displayName: String,
    val name: String,
    val lastName: String,
    val phoneNumber: String,

    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    val matricula: String?,
    val titulo: String?,

    val photoUrl: String?,
    val bannerImageUrl: String?,
    val galleryImages: List<String> = emptyList(),

    // 🔥 Usando modelos del archivo CompanyModels.kt
    val personalAddresses: List<AddressClient> = emptyList(),
    val companies: List<CompanyClient> = emptyList(),

    val hasCompanyProfile: Boolean,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isFavorite: Boolean,
    val rating: Float,
    val favoriteProviderIds: List<String> = emptyList(),

    val createdAt: Long
) {
    fun toDomain(): User {
        return User(
            uid = id,
            email = email,
            displayName = displayName,
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
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
            favoriteProviderIds = favoriteProviderIds,
            createdAt = createdAt
        )
    }
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = uid,
        email = email,
        displayName = displayName,
        name = name,
        lastName = lastName,
        phoneNumber = phoneNumber,
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
        favoriteProviderIds = favoriteProviderIds,
        createdAt = createdAt
    )
}