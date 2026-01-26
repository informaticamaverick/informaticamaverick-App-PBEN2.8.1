package com.example.myapplication.prestador.ui.navigation

sealed class PrestadorRoutes(val route: String) {
    object Login : PrestadorRoutes("login")
    object Register : PrestadorRoutes("register")
    object Success : PrestadorRoutes("success")
    object Dashboard : PrestadorRoutes("dashboard")
    object EditProfile : PrestadorRoutes("edit_profile")
    object Profile : PrestadorRoutes("profile")
    object Services : PrestadorRoutes("servives")
    object ServiceConfig : PrestadorRoutes("service_config")
    object  CrearPresupuesto : PrestadorRoutes("crear_presupuesto")
}

