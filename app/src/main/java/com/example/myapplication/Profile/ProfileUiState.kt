package com.example.myapplication.Profile

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val photoUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)
