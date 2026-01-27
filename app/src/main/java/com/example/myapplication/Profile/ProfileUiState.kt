package com.example.myapplication.Profile

import retrofit2.http.Url

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val address: String = "",
    val addressHome: String = "",
    val addressWork: String = "",
    val cityHome: String ="",
    val stateHome: String= "",
    val zipCodeHome: String = "",
    val cityWork: String ="",
    val stateWork: String ="",
    val zipCodeWork: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val photoUrl: String = "",
    val coverPhotoUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isComplete: Boolean = false






)
