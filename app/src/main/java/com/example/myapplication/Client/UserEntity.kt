package com.example.myapplication.Client

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ENTIDAD DE USUARIO (UserEntity)
 * 
 * Esta clase define la tabla 'user_profile' en Room. Es nuestra "Fuente Única de Verdad".
 * Los datos aquí guardados persisten incluso si se cierra la aplicación.
 * 
 * 🔥 Sincronización: Este modelo debe mapearse con los documentos de Firebase Firestore.
 */
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val name: String,
    val lastName: String,
    
    // Datos profesionales
    val matricula: String?,
    val titulo: String?,
    
    // Listas de contacto (Manejadas por Converters.kt)
    val emails: List<String>,
    val phones: List<String>,
    
    // Multimedia
    val profileImageUrl: String?,
    val bannerImageUrl: String?,
    
    // Direcciones y Ubicación
    val personalAddresses: List<Address>,
    
    // Estados de cuenta y Perfil
    val hasCompanyProfile: Boolean,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isFavorite: Boolean,
    val rating: Float,
    
    // Datos Empresariales (Manejados por Converters.kt)
    val companies: List<Company>
)
