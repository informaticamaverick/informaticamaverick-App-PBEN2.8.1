package com.example.myapplication.Data.Model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val name: String,
)