package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val role: UserRole //Enum que se crea arriba//

)


/**
 * LoginRequest: Modelo de solicitud de login con email, contraseña y rol de usuario.
 */

