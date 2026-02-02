package com.example.myapplication.Client


//APPNAVIGATION CONTROLA LAS RUTAS Y LO QUE SUCEDE CON LA NAVIGATIONBAR
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.HomeScreenComplete

// 1. DEFINICIÓN CENTRALIZADA DE TODAS LAS RUTAS
sealed class Screen(val route: String, val title: String) {
    // Pantallas Principales
    object Home : Screen("home", "Inicio") 
    object Presupuestos : Screen("presupuestos", "Presupuestos") 
    object Chat : Screen("chat?providerId={providerId}", "Chat") 
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
    // Ocultar barra de navegación en chat conversation
    val shouldShowBottomBar = currentRoute?.split("?")?.first() in mainScreenRoutes && 
                              !isInConversation

    // --- NUEVO: Lógica para determinar el índice de la pantalla y la dirección de la animación ---
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
        // Contenedor del NavHost----------------------------------------------------------------------------------------------------------
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
            //modifier = Modifier.padding(innerPadding)
        ) {

            // --- NUEVO: Animaciones de desplazamiento lateral (Left <-> Right) ---
            // Se compara el índice de destino con el inicial para saber si deslizar a izq o der.

            // 1. PANTALLA PRINCIPAL (HOME)
            composable(
                route = Screen.Home.route,
                // Las transiciones son PARÁMETROS, van dentro del paréntesis ( )
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex != -1 && targetIndex > initialIndex) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                },
                exitTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (targetIndex != -1 && targetIndex > initialIndex) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                }
            ) { // <--- Aquí cerramos el paréntesis y abrimos la llave del contenido

                // El contenido de la pantalla va aquí adentro
                HomeScreenComplete(
                    navController = navController,
                    bottomPadding = innerPadding // ¡Ahora sí pasamos el padding correctamente!
                )
            }

            // 2. PANTALLA PRESUPUESTOS
            composable(
                route = Screen.Presupuestos.route,
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex < targetIndex) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                     val initialIndex = getRouteIndex(initialState.destination.route)
                     val targetIndex = getRouteIndex(targetState.destination.route)
                     if (initialIndex < targetIndex) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                     else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) { 
                PresupuestosScreen(
                    onBack = { navController.popBackStack() },
                    onChatClick = { prestadorId ->
                        navController.navigate("chat_conversation/$prestadorId")
                    },
                    onProfileClick = { prestadorId ->
                        navController.navigate("perfil_prestador/$prestadorId")
                    }
                )
            }
            
            // 3. PANTALLA CHAT
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("providerId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }),
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex < targetIndex) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                     val initialIndex = getRouteIndex(initialState.destination.route)
                     val targetIndex = getRouteIndex(targetState.destination.route)
                     if (initialIndex < targetIndex) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                     else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId")
                ChatScreen(
                    onBack = { navController.popBackStack() },
                    initialProviderId = providerId,
                    navController = null, // No usar navegación, manejar todo internamente
                    onInConversationChange = { inConversation -> 
                        isInConversation = inConversation 
                    }
                )
            }
            
            // 4. PANTALLA CALENDARIO
            composable(
                route = Screen.Calendar.route,
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    if (initialIndex < targetIndex) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                     val initialIndex = getRouteIndex(initialState.destination.route)
                     val targetIndex = getRouteIndex(targetState.destination.route)
                     if (initialIndex < targetIndex) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                     else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) { 
                CalendarScreen(onBack = { navController.popBackStack() }) 
            }
            
            // 5. PANTALLA PROMOCIONES
            composable(
                route = Screen.Promo.route,
                enterTransition = {
                    val initialIndex = getRouteIndex(initialState.destination.route)
                    val targetIndex = getRouteIndex(targetState.destination.route)
                    // Como es la última, generalmente entra desde la derecha si vienes de una anterior
                    if (initialIndex < targetIndex) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                    else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                },
                exitTransition = {
                     val initialIndex = getRouteIndex(initialState.destination.route)
                     val targetIndex = getRouteIndex(targetState.destination.route)
                     if (initialIndex < targetIndex) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
                     else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
                }
            ) { 
                PromoScreen(navController = navController, onBack = { navController.popBackStack() }) 
            }
            
            // --- PANTALLAS SECUNDARIAS ---

            composable(Screen.CrearLicitacion.route) { CrearLicScreen(onBack = { navController.popBackStack() }) }
            
            composable(Screen.PerfilCliente.route) {
                PerfilUsuarioScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = { }
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
                    onNavigateToProviderProfile = { providerId -> navController.navigate("perfil_prestador/$providerId") },
                    onNavigateToChat = { providerId -> navController.navigate("chat_conversation/$providerId") }
                )
            }
            
            composable(
                route = Screen.PerfilPrestador.route,
                arguments = listOf(navArgument("providerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                PerfilPrestadorCliente(providerId = providerId, onBack = { navController.popBackStack() })
            }
            
            // Ruta chat_conversation eliminada - ahora todo se maneja internamente en ChatScreen
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    navController: NavHostController,
    allItems: List<Screen>,
    currentRoute: String?
) {
    // Determinamos el color de fondo: en modo oscuro aplicamos un tono que continúe el degradado (negro con alpha)
    val isDark = isSystemInDarkTheme()
    val navBarColor = if (isDark) Color.Black.copy(alpha = 1f) else MaterialTheme.colorScheme.surface

    NavigationBar(
        modifier = Modifier
            .height(80.dp) 
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) 
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)), 
        containerColor = navBarColor, 
        tonalElevation = if (isDark) 0.dp else 8.dp
    ) {
        allItems.forEach { screen ->
            // Determinamos si este item está seleccionado
            val isSelected = currentRoute?.startsWith(screen.route.split("?").first()) == true

            // --- ANIMACIÓN POP (Resorte) ---
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.25f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "iconScale"
            )

            // --- ANIMACIÓN DE ROTACIÓN (Borde de Carga) ---
            val infiniteTransition = rememberInfiniteTransition(label = "borderRotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing), // 3 segundos por vuelta (lento/tenue)
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            // Colores para el borde giratorio
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondaryContainer

            NavigationBarItem(
                // Desactivamos el indicador por defecto (óvalo) para dibujar el nuestro circular
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                ),
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp) // Tamaño del área del indicador circular
                            .drawBehind {
                                if (isSelected) {
                                    // 1. DIBUJAR FONDO CIRCULAR (Reemplazo del óvalo)
                                    drawCircle(
                                        color = secondaryColor,
                                        radius = size.minDimension / 2
                                    )

                                    // 2. DIBUJAR BORDE GIRATORIO (Efecto barra de carga tenue)
                                    rotate(rotation) {
                                        drawCircle(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(
                                                    primaryColor.copy(alpha = 0.0f), // Inicio transparente
                                                    primaryColor.copy(alpha = 0.1f), 
                                                    primaryColor.copy(alpha = 0.5f)  // Final tenue (50% opacidad)
                                                )
                                            ),
                                            radius = (size.minDimension / 2) - 1.dp.toPx(), // Un poco más adentro del borde
                                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }
                            // Aplicamos la escala POP al contenido (Icono/Emoji)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    ) {
                        if (isSelected) {
                            Text(text = getEmojiForScreen(screen), fontSize = 24.sp)
                        } else {
                            val icon = when (screen) {
                                Screen.Home -> Icons.Filled.Home
                                Screen.Presupuestos -> Icons.Filled.AttachMoney
                                Screen.Chat -> Icons.Filled.Chat
                                Screen.Calendar -> Icons.Filled.CalendarToday
                                Screen.Promo -> Icons.Filled.LocalFireDepartment
                                else -> Icons.Filled.Home 
                            }
                            Icon(icon, contentDescription = screen.title)
                        }
                    }
                },
                selected = isSelected,
                alwaysShowLabel = false, // Asegura que no se reserve espacio extra para el label
                onClick = {
                    val route = screen.route.split("?").first()
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// Función auxiliar para obtener el emoji de cada pantalla
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
@Preview
@Composable
fun AppNavigationPreview() {
    AppNavigation()
}
