package com.example.myapplication.prestador.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.register.PrestadorRegisterScreen
import com.example.myapplication.prestador.ui.success.PrestadorSuccessScreen
import com.example.myapplication.prestador.ui.dashboard.PrestadorDashboardScreen
import com.example.myapplication.prestador.ui.config.ServiceConfigScreen
import com.example.myapplication.prestador.ui.CrearPresupuestoPrestadorScreen

@Composable
fun PrestadorNavGraph(
    navController: NavHostController,
    startDestination: String = PrestadorRoutes.Dashboard.route // TEMPORAL para pruebas
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        
        // ANIMACIÓN GLOBAL: SUAVE Y PROFESIONAL
        enterTransition = { 
            // Entra desvaneciéndose y creciendo un poco
            fadeIn(animationSpec = tween(300)) + 
            scaleIn(initialScale = 0.92f, animationSpec = tween(300)) 
        },
        exitTransition = { 
            // Sale desvaneciéndose y encogiéndose un poco
            fadeOut(animationSpec = tween(300)) + 
            scaleOut(targetScale = 0.92f, animationSpec = tween(300)) 
        },
        
        // Para que al volver atrás no se sienta raro
        popEnterTransition = { 
            fadeIn(animationSpec = tween(300)) + 
            scaleIn(initialScale = 0.92f, animationSpec = tween(300)) 
        },
        popExitTransition = { 
            fadeOut(animationSpec = tween(300)) + 
            scaleOut(targetScale = 0.92f, animationSpec = tween(300)) 
        }
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
            PrestadorDashboardScreen(
                onNavigateToEditProfile = {
                    navController.navigate(PrestadorRoutes.EditProfile.route)
                },
                onNavigateToServiceConfig = {
                    navController.navigate(PrestadorRoutes.ServiceConfig.route)
                },
                onLogout = {
                    navController.navigate(PrestadorRoutes.Login.route) {
                        popUpTo(0) { inclusive = true}
                    } // Limpia toda la pila de navegacion
                },
                onNavigateToPresupuesto = {
                    navController.navigate(PrestadorRoutes.CrearPresupuesto.route)
                }

            )
        }
        
        composable(PrestadorRoutes.ServiceConfig.route) {
            ServiceConfigScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(PrestadorRoutes.EditProfile.route) {
            // TODO: Implementar PrestadorEditProfileScreen
            Text("Editar Perfil - En construcción")
        }

        composable("chat") {
            PrestadorChatScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPresupuesto = {
                    navController.navigate(PrestadorRoutes.CrearPresupuesto.route)
                }
            )
        }
        
        composable(PrestadorRoutes.CrearPresupuesto.route) {
            CrearPresupuestoPrestadorScreen()
        }
    }
}
