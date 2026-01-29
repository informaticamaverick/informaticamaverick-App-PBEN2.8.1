package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier

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

// 2. CONTENEDOR DE NAVEGACIÓN (NAVHOST)
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    pagerState: PagerState,
    navItems: List<Screen>,
    modifier: Modifier = Modifier,
    onInConversationChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Ruta para el HorizontalPager que contiene las pantallas principales
        composable(Screen.Home.route) {
            HorizontalPager(state = pagerState) { page ->
                when (navItems[page]) {
                    Screen.Home -> HomeScreenCliente(navController = navController)
                    Screen.Presupuestos -> PresupuestosScreen(onBack = { navController.popBackStack() })
                    Screen.Chat -> ChatScreen(
                        onBack = { navController.popBackStack() },
                        navController = navController,
                        onInConversationChange = onInConversationChange
                    )
                    Screen.Calendar -> CalendarScreen(onBack = { navController.popBackStack() })
                    Screen.Promo -> PromoScreen(onBack = { navController.popBackStack() }, navController = navController)
                    else -> {}
                }
            }
        }

        // Rutas de las pantallas secundarias
        composable(Screen.CrearLicitacion.route) { 
            CrearLicScreen(onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.PerfilCliente.route) { 
            PerfilUsuarioScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = { onLogout() }
            ) 
        }

        composable(Screen.PerfilPrestador.route) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId") ?: return@composable
            PerfilPrestadorCliente(
                providerId = providerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ResultBusqueda.route) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: return@composable
            ResultBusquedaCategoriaScreen(
                categoryName = category,
                onBack = { navController.popBackStack() },
                onNavigateToProviderProfile = { providerId ->
                    navController.navigate(Screen.PerfilPrestador.route.replace("{providerId}", providerId))
                },
                onNavigateToChat = { providerId ->
                    navController.navigate(Screen.ChatConversation.route.replace("{providerId}", providerId))
                }
            )
        }

        composable(Screen.ChatConversation.route) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId")
            ChatScreen(
                onBack = { navController.popBackStack() },
                initialProviderId = providerId,
                navController = navController,
                onInConversationChange = onInConversationChange
            )
        }
    }
}
