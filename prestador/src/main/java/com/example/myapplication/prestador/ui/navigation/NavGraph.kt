package com.example.myapplication.prestador.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.prestador.ui.chat.PrestadorChatScreen
import com.example.myapplication.prestador.ui.login.PrestadorLoginScreen
import com.example.myapplication.prestador.ui.register.PrestadorRegisterScreen
import com.example.myapplication.prestador.ui.success.PrestadorSuccessScreen
import com.example.myapplication.prestador.ui.dashboard.PrestadorDashboardScreen
import com.example.myapplication.prestador.ui.config.ServiceConfigScreen
import com.example.myapplication.prestador.ui.presupuesto.CrearPresupuestoPrestadorScreen
import com.example.myapplication.prestador.ui.presupuesto.PresupuestosScreen
import com.example.myapplication.prestador.ui.promotion.CreatePromotionScreen
import com.example.myapplication.prestador.ui.promotion.PromotionListScreen
import com.example.myapplication.prestador.ui.profile.EditProfileScreenUnified
import com.example.myapplication.prestador.ui.theme.ThemeDemoScreen
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.ChatSimulationViewModel

@Composable
fun PrestadorNavGraph(
    navController: NavHostController,
    chatSimulationViewModel: ChatSimulationViewModel,  // Recibido desde MainActivity
    startDestination: String = PrestadorRoutes.Login.route
) {
    val colors = getPrestadorColors()
    
    println("🌐 NavGraph: Usando ChatSimulationViewModel (${chatSimulationViewModel.hashCode()})")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
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
            println("🌐 Dashboard composable: Usando ChatSimulationViewModel (${chatSimulationViewModel.hashCode()})")
            
            PrestadorDashboardScreen(
                chatSimulationViewModel = chatSimulationViewModel,  // Pasar el ViewModel compartido
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
                    navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("dashboard"))
                },
                onNavigateToPresupuestoCita = { appointmentId ->
                    navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("calendar", appointmentId))
                },
                onNavigateToPresupuestos = {
                    navController.navigate(PrestadorRoutes.Presupuestos.route)
                },
                onNavigateToPromotion = {
                    navController.navigate(PrestadorRoutes.CreatePromotion.route)
                },
                onNavigateToPromotionList = {
                    navController.navigate(PrestadorRoutes.PromotionsList.route)
                }

            )
        }
        
        composable(PrestadorRoutes.ServiceConfig.route) {
            ServiceConfigScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(PrestadorRoutes.EditProfile.route) {
            EditProfileScreenUnified(
                onBack = { navController.navigateUp() }
            )
        }

        composable("chat") {
            println("🌐 Chat composable: Usando ChatSimulationViewModel (${chatSimulationViewModel.hashCode()})")
            
            PrestadorChatScreen(
                chatSimulationViewModel = chatSimulationViewModel,  // Pasar el ViewModel compartido
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPresupuesto = {
                    navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("chat"))
                }
            )
        }
        
        composable(
            route = PrestadorRoutes.CrearPresupuesto.route,
            arguments = listOf(
                navArgument("origin") {
                    type = NavType.StringType
                    defaultValue = "dashboard"
                },
                navArgument("appointmentId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val origin = backStackEntry.arguments?.getString("origin") ?: "dashboard"
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            CrearPresupuestoPrestadorScreen(
                appointmentId = appointmentId,
                onBack = { 
                    when (origin) {
                        "chat" -> navController.navigate("chat") {
                            popUpTo(PrestadorRoutes.CrearPresupuesto.route) { inclusive = true }
                        }
                        "presupuestos" -> navController.navigate(PrestadorRoutes.Presupuestos.route) {
                            popUpTo(PrestadorRoutes.CrearPresupuesto.route) { inclusive = true }
                        }
                        else -> navController.popBackStack()
                    }
                }
            )
        }
        
        composable(PrestadorRoutes.Presupuestos.route) {
            PresupuestosScreen(
                onBack = { navController.popBackStack() },
                onCrearNuevo = { 
                    navController.navigate(PrestadorRoutes.CrearPresupuesto.createRoute("presupuestos"))
                },
                onVerDetalle = { presupuesto ->
                    // TODO: Navegar a detalle de presupuesto
                }
            )
        }

        composable(PrestadorRoutes.CreatePromotion.route) {
            CreatePromotionScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPublish = { promotion ->
                    //Implementar lógica de publicacion
                    navController.popBackStack()
                }
            )
        }
        
        composable(PrestadorRoutes.PromotionsList.route) {
            PromotionListScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
    }
}
