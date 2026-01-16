package com.example.myapplication.Data.Model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val addressHome: String = "", // Dirección de casa
    val addressWork: String = "", // Dirección de trabajo
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val photoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val latitudeHome: Double = 0.0,
    val longitudeHome: Double = 0.0,
    val latitudeWork: Double = 0.0,
    val longitudeWork: Double = 0.0,
    val isProfileComplete: Boolean = false
)
