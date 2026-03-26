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
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
import com.example.myapplication.prestador.ui.promotion.PromotionDetailScreen
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
    val activity = LocalContext.current as? Activity
    
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
                        // Usuario nuevo de Google, necesita completar registro (sin email/contraseña)
                        navController.navigate(PrestadorRoutes.Register.createRoute(isGoogle = true)) {
                            popUpTo(PrestadorRoutes.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(PrestadorRoutes.Register.createRoute(isGoogle = false))
                }
            )
        }
        
        composable(
            route = PrestadorRoutes.Register.route,
            arguments = listOf(
                navArgument("isGoogle") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isGoogle = backStackEntry.arguments?.getBoolean("isGoogle") ?: false
            PrestadorRegisterScreen(
                isGoogleUser = isGoogle,
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
                    // 1. Cerrar sesión en Firebase Auth
                    FirebaseAuth.getInstance().signOut()
                    // 2. Revocar token de Google
                    try {
                        activity?.let { ctx ->
                            GoogleSignIn.getClient(
                                ctx,
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            ).signOut()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Logout", "Error signOut Google: ${e.message}")
                    }
                    // 3. Reiniciar Activity sin estado guardado (limpia backstack y ViewModels)
                    activity?.also { ctx ->
                        val restartIntent = Intent(ctx, ctx::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        ctx.startActivity(restartIntent)
                        ctx.finish()
                    }
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
                onBack = { navController.popBackStack() },
                onPromotionClick = { promotionId ->
                    navController.navigate(PrestadorRoutes.PromotionDetail.createRoute(promotionId))
                }
            )
        }

        composable(
            route = PrestadorRoutes.PromotionDetail.route,
            arguments = listOf(
                navArgument("promotionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val promotionId = backStackEntry.arguments?.getString("promotionId") ?: return@composable
            PromotionDetailScreen(
                promotionId = promotionId,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(PrestadorRoutes.EditPromotion.createRoute(id))
                }
            )
        }

        composable(
            route = PrestadorRoutes.EditPromotion.route,
            arguments = listOf(
                navArgument("promotionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val promotionId = backStackEntry.arguments?.getString("promotionId") ?: return@composable
            CreatePromotionScreen(
                promotionId = promotionId,
                onBack = { navController.popBackStack() },
                onPublish = { navController.popBackStack() }
            )
        }
    }
    }
}
