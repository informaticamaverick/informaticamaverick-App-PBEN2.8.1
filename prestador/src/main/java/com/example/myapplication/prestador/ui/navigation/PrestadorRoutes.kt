package com.example.myapplication.prestador.ui.navigation

sealed class PrestadorRoutes(val route: String) {
    object Login : PrestadorRoutes("login")
    object Register : PrestadorRoutes("register")
    object Success : PrestadorRoutes("success")
    object Dashboard : PrestadorRoutes("dashboard")
    object Profile : PrestadorRoutes("profile")
    object Services : PrestadorRoutes("servives")
}

