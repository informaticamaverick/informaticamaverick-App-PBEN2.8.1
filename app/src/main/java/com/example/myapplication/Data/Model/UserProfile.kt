package com.example.myapplication.Data.Model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "", // Nombre completo
    val email: String = "",// Correo electrónico
    val phoneNumber: String = "",// Número de teléfono
    val address: String = "",// Dirección
    val addressHome: String = "",// Dirección de casa
    val addressWork: String = "",// Dirección de trabajo
    val cityHome: String = "",// Ciudad de casa
    val stateHome: String = "",// Provincia de casa
    val zipCodeHome: String = "",// Código postal de casa
    val cityWork: String = "",// Ciudad de trabajo
    val stateWork: String = "",// Provincia de trabajo
    val zipCodeWork: String = "",// Código postal de trabajo
    val city: String = "",// Ciudad
    val state: String = "",// Provincia
    val zipCode: String = "",// Código postal
    val photoUrl: String = "",// URL de la foto de perfil
    val coverPhotoUrl: String = "",// URL de la foto de portada
    val bio: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val latitudeHome: Double = 0.0,
    val longitudeHome: Double = 0.0,
    val latitudeWork: Double = 0.0,
    val longitudeWork: Double = 0.0,
    val isProfileComplete: Boolean = false
)
