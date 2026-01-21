package com.example.myapplication.prestador.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.prestador.ui.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.register.PrestadorRegisterScreen
import com.example.myapplication.prestador.ui.success.PrestadorSuccessScreen
import com.example.myapplication.prestador.ui.dashboard.PrestadorDashboardScreen

@Composable
fun PrestadorNavGraph(
    navController: NavHostController,
    startDestination: String = PrestadorRoutes.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(PrestadorRoutes.Login.route) {
            PrestadorLoginScreen(
                onLoginSuccess = { hasProfile ->
                    if (hasProfile) {
                        // Usuario existente con perfil completo
                        navController.navigate(PrestadorRoutes.Dashboard.route) {
                            popUpTo(PrestadorRoutes.Login.route) { inclusive = true }
                        }
                    } else {
                        // Usuario nuevo, necesita completar registro
                        navController.navigate(PrestadorRoutes.Register.route) {
                            popUpTo(PrestadorRoutes.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(PrestadorRoutes.Register.route) {
            PrestadorRegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(PrestadorRoutes.Success.route) {
                        popUpTo(PrestadorRoutes.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(PrestadorRoutes.Login.route) {
                        popUpTo(PrestadorRoutes.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(PrestadorRoutes.Success.route) {
            PrestadorSuccessScreen(
                onNavigateToDashboard = {
                    navController.navigate(PrestadorRoutes.Dashboard.route) {
                        popUpTo(PrestadorRoutes.Success.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(PrestadorRoutes.Dashboard.route) {
            PrestadorDashboardScreen()
        }
    }
}
