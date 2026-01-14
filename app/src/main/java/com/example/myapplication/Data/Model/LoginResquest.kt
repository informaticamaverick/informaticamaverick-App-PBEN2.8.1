package com.example.myapplication.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResquest(
    val email: String,
    val password: String,
    val role: UserRole //Enum que se crea arriba//

)

