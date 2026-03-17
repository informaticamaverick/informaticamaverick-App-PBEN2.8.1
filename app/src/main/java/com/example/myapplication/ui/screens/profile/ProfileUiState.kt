package com.example.myapplication.ui.screens.profile

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
    val isComplete: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val isPublicProfile: Boolean = false,
//********************************************************************************************************************
//             DATOS DE PRUEBA PERFIL USUARIO DATOS EMPRESA APP CLIENTE
//********************************************************************************************************************
    val isEmpresa: Boolean = false,
    val nameComercialEmpresa: String = "Maverick Informatica",
    val nameRazonSocialEmpresa: String = "Maverick Developers",
    val numberCuitEmpresa: String = "343270675",
    val emailEmpresa: String = "informaticamaverick@gmail.com",
    val phoneNumberEmpresa: String = "3815444511111",

    val addressEmpresa: String = "B. Matienzo 1339",
    val cityEmpresa: String = "San Miguel de tucuman",
    val stateEmpresa: String = "Tucuman",
    val zipCodeEmpresa: String = "4000",
    val addressEmpresaSucursal1: String = " Siempre viva 123",
    val cityEmpresaSucursal1: String = "San Miguel de Tucuman",
    val stateEmpresaSucursal1: String = "asdf",
    val zipCodeEmpresaSucursal1: String = "4000",
    val addressEmpresaSucursal2: String = "fasdfdsfd 1213",
    val cityEmpresaSucursal2: String = "asdfadsfasdf",
    val stateEmpresaSucursal2: String = "sadfsdafdsafdsfasdff",
    val zipCodeEmpresaSucursal2: String = "4500",

)
