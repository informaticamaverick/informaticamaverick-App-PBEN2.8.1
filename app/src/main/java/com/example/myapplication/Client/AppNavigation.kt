package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// 1. DEFINICIÓN CENTRALIZADA DE TODAS LAS RUTAS
sealed class Screen(val route: String, val title: String) {
    // Pantallas Principales
    object Home : Screen("home", "Inicio")
    object Presupuestos : Screen("presupuestos", "Presupuestos")
    object Chat : Screen("chat", "Chat")
    object Calendar : Screen("calendar", "Calendario")
    object Promo : Screen("promo", "Promociones")

    // Pantallas Secundarias
    object CrearLicitacion : Screen("crear_licitacion", "Crear Licitación")
    object PerfilPrestador : Screen("perfil_prestador/{providerId}", "Perfil del Prestador")
    object PerfilCliente : Screen("perfil_cliente", "Mi Perfil")
    object ResultBusqueda : Screen("result_busqueda/{category}", "Resultados de Búsqueda")
    object ChatConversation : Screen("chat_conversation/{providerId}", "Conversación de Chat")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navItems = listOf(
        Screen.Home,
        Screen.Presupuestos,
        Screen.Chat,
        Screen.Calendar,
        Screen.Promo
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedScreen = navItems.find { it.route == currentRoute } ?: Screen.Home
    val mainScreenRoutes = navItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (currentRoute in mainScreenRoutes) {
                AppBottomNavigationBar(
                    navController = navController,
                    allItems = navItems,
                    selectedScreen = selectedScreen
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) { HomeScreenCliente(navController = navController) }
                composable(Screen.Presupuestos.route) { PresupuestosScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Chat.route) { ChatScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Calendar.route) { CalendarScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.Promo.route) { PromoScreen(navController = navController, onBack = { navController.popBackStack() }) }

                composable(Screen.CrearLicitacion.route) { CrearLicScreen(onBack = { navController.popBackStack() }) }
                composable(Screen.PerfilCliente.route) { 
                    PerfilUsuarioScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            // TODO: Implementar navegación al Login aquí
                            // Ejemplo: navController.navigate("login") { popUpTo(0) }
                        }
                    ) 
                }
                composable(
                    route = Screen.ResultBusqueda.route,
                    arguments = listOf(navArgument("category") { type = NavType.StringType })
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    ResultBusquedaCategoriaScreen(
                        categoryName = category,
                        onBack = { navController.popBackStack() },
                        onNavigateToProviderProfile = { providerId ->
                            navController.navigate("perfil_prestador/$providerId")
                        },
                        onNavigateToChat = { providerId ->
                            navController.navigate("chat_conversation/$providerId")
                        }
                    )
                }
                composable(
                    route = Screen.PerfilPrestador.route,
                    arguments = listOf(navArgument("providerId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                    PerfilPrestadorCliente(providerId = providerId, onBack = { navController.popBackStack() })
                }
                composable(
                    route = Screen.ChatConversation.route,
                    arguments = listOf(navArgument("providerId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId")
                    ChatScreen(onBack = { navController.popBackStack() }, initialProviderId = providerId)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun AppNavigationPreview() {
    AppNavigation()
}
