package com.example.myapplication.data.model

import com.example.myapplication.data.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class LoginResquest(
    val email: String,
    val password: String,
    val role: UserRole //Enum que se crea arriba//

)