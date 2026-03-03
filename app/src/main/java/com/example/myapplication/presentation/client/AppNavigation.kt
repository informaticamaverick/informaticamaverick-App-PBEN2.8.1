package com.example.myapplication.presentation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.MyApplicationTheme

// 1. DEFINICIÓN CENTRALIZADA DE TODAS LAS RUTAS
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Inicio")
    object Presupuestos : Screen("presupuestos", "Presupuestos")
    object Chat : Screen("chat?providerId={providerId}", "Chat")
    object Calendar : Screen("calendar", "Calendario")
    object Promo : Screen("promo", "Promociones")
    object CrearLicitacion : Screen("crear_licitacion", "Crear Licitación")
    object PerfilPrestador : Screen("perfil_prestador/{providerId}", "Perfil del Prestador")
    object PerfilCliente : Screen("perfil_cliente", "Mi Perfil")
    object ResultBusqueda : Screen("result_busqueda/{category}", "Resultados de Búsqueda")
    object ChatConversation : Screen("chat_conversation/{providerId}", "Conversación de Chat")
    object Fast : Screen("fast", "Maverick FAST")
    object Login : Screen("login", "Iniciar Sesión")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var isInConversation by remember { mutableStateOf(false) }

    val navItems = listOf(
        Screen.Home,
        Screen.Presupuestos,
        Screen.Chat,
        Screen.Calendar,
        Screen.Promo
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreenRoutes = navItems.map { it.route.split("?").first() }
    val shouldShowBottomBar = currentRoute?.split("?")?.first() in mainScreenRoutes && !isInConversation

    fun getRouteIndex(route: String?): Int {
        return navItems.indexOfFirst { it.route == route }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                AppBottomNavigationBar(
                    navController = navController,
                    allItems = navItems,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex > initialIndex) {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    } else {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    }
                },
                exitTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (targetIndex != -1 && targetIndex > initialIndex) {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    } else {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                    }
                }
            ) {
                HomeScreenComplete(
                    navController = navController,
                    bottomPadding = innerPadding
                )
            }

            composable(Screen.Presupuestos.route) {
                val budgetViewModel: BudgetViewModel = hiltViewModel()
                PresupuestosScreen(
                    viewModel = budgetViewModel,
                    onBack = { navController.popBackStack() },
                    onChatClick = { pid: String -> navController.navigate("chat?providerId=$pid") },
                    bottomPadding = innerPadding // PASAMOS EL PADDING PARA LA UBICACIÓN DEL FAB
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("providerId") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                })
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId")
                ChatScreen(
                    onBack = { navController.popBackStack() },
                    initialProviderId = providerId,
                    navController = null,
                    onInConversationChange = { isInConversation = it }
                )
            }

            composable(Screen.Calendar.route) { CalendarScreen(onBack = { navController.popBackStack() }) }

            composable(Screen.Promo.route) {
                PromoScreen(navController = navController, onBack = { navController.popBackStack() })
            }

            composable(Screen.CrearLicitacion.route) { CrearLicScreen(onBack = { navController.popBackStack() }) }

            composable(Screen.PerfilCliente.route) {
                PerfilUsuarioScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = { /* Lógica de logout */ }
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
                    onNavigateToProviderProfile = { pid -> navController.navigate("perfil_prestador/$pid") },
                    onNavigateToChat = { pid -> navController.navigate("chat?providerId=$pid") }
                )
            }

            composable(
                route = Screen.PerfilPrestador.route,
                arguments = listOf(navArgument("providerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                PerfilPrestadorCliente(providerId = providerId, onBack = { navController.popBackStack() })
            }

            composable(Screen.Fast.route) { FastScreen(navController = navController) }

            composable(Screen.Login.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pantalla de Login")
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController, allItems: List<Screen>, currentRoute: String?) {
    val isDark = isSystemInDarkTheme()
    val navBarColor = if (isDark) Color.Black.copy(alpha = 1f) else MaterialTheme.colorScheme.surface

    NavigationBar(
        modifier = Modifier
            .height(55.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = navBarColor,
        tonalElevation = if (isDark) 0.dp else 8.dp
    ) {
        allItems.forEach { screen ->
            val isSelected = currentRoute?.startsWith(screen.route.split("?").first()) == true
            val scale by animateFloatAsState(targetValue = if (isSelected) 1.25f else 1.0f, label = "scale")

            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                selected = isSelected,
                onClick = {
                    val route = screen.route.split("?").first()
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (isSelected) Text(getEmojiForScreen(screen), fontSize = 24.sp)
                    else {
                        val icon = when (screen) {
                            Screen.Home -> Icons.Filled.Home
                            Screen.Presupuestos -> Icons.Filled.AttachMoney
                            Screen.Chat -> Icons.Filled.Chat
                            Screen.Calendar -> Icons.Filled.CalendarToday
                            Screen.Promo -> Icons.Filled.LocalFireDepartment
                            else -> Icons.Filled.Home
                        }
                        Icon(icon, contentDescription = null)
                    }
                }
            )
        }
    }
}

fun getEmojiForScreen(screen: Screen): String {
    return when (screen) {
        Screen.Home -> "🏠"
        Screen.Presupuestos -> "💰"
        Screen.Chat -> "💬"
        Screen.Calendar -> "📅"
        Screen.Promo -> "🔥"
        else -> ""
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppNavigationPreview() {
    MyApplicationTheme {
        AppNavigation()
    }
}

@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    val navController = rememberNavController()
    val navItems = listOf(
        Screen.Home,
        Screen.Presupuestos,
        Screen.Chat,
        Screen.Calendar,
        Screen.Promo
    )
    MyApplicationTheme {
        AppBottomNavigationBar(
            navController = navController,
            allItems = navItems,
            currentRoute = Screen.Home.route
        )
    }
}
