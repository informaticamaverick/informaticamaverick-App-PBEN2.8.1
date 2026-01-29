package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
// [MODIFICACIÓN] Se corrige la importación de 'navArgument'.
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// [PASO 2 y 3] Se crea el MainScreen y la NavigationBar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val navItems = listOf(
        Screen.Home,
        Screen.Presupuestos,
        Screen.Chat,
        Screen.Calendar,
        Screen.Promo
    )
    val pagerState = rememberPagerState(pageCount = { navItems.size })
    val coroutineScope = rememberCoroutineScope()
    var isInConversation by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Ocultar barra cuando está en conversación de chat o en pantallas secundarias
    val showBottomBar = when {
        currentRoute?.startsWith("chat_conversation") == true -> false
        currentRoute == "chat" && isInConversation -> false
        navItems.any { it.route == currentRoute } -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    navController = navController,
                    pagerState = pagerState,
                    coroutineScope = coroutineScope,
                    navItems = navItems
                )
            }
        }
    ) { innerPadding ->
        // [CORRECCIÓN ESTRUCTURAL] Un único NavHost gestiona todas las pantallas.
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route, // La ruta de inicio es una de las principales
            modifier = Modifier.padding(innerPadding)
        ) {
            // Contenedor para el HorizontalPager
            // Esto permite que el swipe funcione, pero la navegación real la controla el NavController
            composable(Screen.Home.route) { 
                MainPagerScreen(
                    navController = navController,
                    pagerState = pagerState,
                    navItems = navItems,
                    onInConversationChange = { isInConversation = it },
                    isInConversation = isInConversation
                ) 
            }
            composable(Screen.Presupuestos.route) {
                 MainPagerScreen(
                    navController = navController,
                    pagerState = pagerState,
                    navItems = navItems,
                    onInConversationChange = { isInConversation = it },
                    isInConversation = isInConversation
                ) 
            }
            composable(Screen.Chat.route) {
                 MainPagerScreen(
                    navController = navController,
                    pagerState = pagerState,
                    navItems = navItems,
                    onInConversationChange = { isInConversation = it },
                    isInConversation = isInConversation
                ) 
            }
            composable(Screen.Calendar.route) { 
                 MainPagerScreen(
                    navController = navController,
                    pagerState = pagerState,
                    navItems = navItems,
                    onInConversationChange = { isInConversation = it },
                    isInConversation = isInConversation
                ) 
            }
            composable(Screen.Promo.route) { 
                 MainPagerScreen(
                    navController = navController,
                    pagerState = pagerState,
                    navItems = navItems,
                    onInConversationChange = { isInConversation = it },
                    isInConversation = isInConversation
                ) 
            }

            // [MODIFICACIÓN] La ruta CrearLicitacion ahora abre CreaLicScreen.
            // Se elimina la referencia a BiddingScreen.
            composable(Screen.CrearLicitacion.route) { CrearLicScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.PerfilCliente.route) { 
                PerfilUsuarioScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = { onLogout() }
                ) 
            }
            
            // [MODIFICACIÓN] La ruta result_busqueda ahora abre ResultBusquedaCategoriaScreen.
            // Se elimina la referencia a SearchResultsScreen.
            composable(
                route = "result_busqueda/{category}",
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
                route = "perfil_prestador/{providerId}",
                arguments = listOf(navArgument("providerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                PerfilPrestadorCliente(providerId = providerId, onBack = { navController.popBackStack() })
            }

            composable(
                route = "chat_conversation/{providerId}",
                arguments = listOf(navArgument("providerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId")
                ChatScreen(
                    onBack = { navController.popBackStack() }, 
                    initialProviderId = providerId,
                    onInConversationChange = { isInConversation = it }
                )
            }
        }
    }

    // Sincronización del Pager y NavController
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val newRoute = navItems[pagerState.currentPage].route
            if (currentRoute != newRoute) {
                navController.navigate(newRoute) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    LaunchedEffect(currentRoute) {
        val index = navItems.indexOfFirst { it.route == currentRoute }
        if (index != -1 && pagerState.currentPage != index) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    navController: NavHostController, 
    pagerState: PagerState, 
    navItems: List<Screen>,
    onInConversationChange: (Boolean) -> Unit = {},
    isInConversation: Boolean = false
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = !isInConversation // Deshabilitar swipe cuando está en conversación
    ) { page ->
        when (navItems[page]) {
            Screen.Home -> HomeScreenCliente(navController = navController)
            Screen.Presupuestos -> PresupuestosScreen(onBack = { navController.popBackStack() })
            Screen.Chat -> ChatScreen(
                onBack = { navController.popBackStack() },
                onInConversationChange = onInConversationChange
            )
            Screen.Calendar -> CalendarScreen(onBack = { navController.popBackStack() })
            Screen.Promo -> PromoScreen(navController = navController, onBack = { navController.popBackStack() })
            else -> {}
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppBottomNavigationBar(
    navController: NavHostController,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    navItems: List<Screen>
) {
    // [MODIFICACIÓN] Se envuelve el NavigationBar en un Column para añadir un borde superior.
    Column {
        // [MODIFICACIÓN] Se añade un Divider para crear un contorno oscuro en la parte superior.
        Divider(color = Color.DarkGray, thickness = 0.5.dp)
        
        // [MODIFICACIÓN] Se ajusta la altura del NavigationBar para hacerlo más compacto.
        NavigationBar(modifier = Modifier.height(60.dp)) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            navItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                NavigationBarItem(
                    icon = {
                        Box {
                            // [MODIFICACIÓN] Se reemplaza Crossfade por AnimatedContent para una animación más personalizable.
                            AnimatedContent(
                                targetState = selected,
                                transitionSpec = {
                                    // [MODIFICACIÓN] Define la animación: el icono nuevo entra desde arriba y el viejo sale hacia arriba.
                                    slideInVertically { height -> height } + fadeIn() togetherWith
                                            slideOutVertically { height -> -height } + fadeOut() using
                                            SizeTransform(clip = false)
                                }, label = "iconAnimation"
                            ) { isSelected ->
                                if (isSelected) {
                                    Text(getEmojiForScreen(screen), fontSize = 24.sp)
                                } else {
                                    Icon(imageVector = getUnselectedIconForScreen(screen), contentDescription = screen.title)
                                }
                            }
                            // [MODIFICACIÓN] La condición ahora incluye Chat, Calendar y Promo.
                            val showNotification = (screen.route == Screen.Chat.route ||
                                    screen.route == Screen.Calendar.route ||
                                    screen.route == Screen.Promo.route) && !selected

                            if (showNotification) {
                                Box(
                                    modifier = Modifier
                                        // [MODIFICACIÓN] El punto se mantiene en la esquina superior derecha (TopEnd).
                                        .align(Alignment.TopEnd)
                                        .padding(end = 4.dp, top = 4.dp)
                                        .size(8.dp)
                                        .background(Color.Red, CircleShape)
                                        // [MODIFICACIÓN] Se oscurece el borde del punto de notificación.
                                        .border(0.5.dp, Color.Black, CircleShape)
                                )
                            }
                        }
                    },
                    selected = selected,
                    onClick = {
                        if (currentDestination?.route != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

// [MODIFICACIÓN] Nueva función para obtener el emoji correspondiente a cada pantalla.
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

// [MODIFICACIÓN] Nueva función para obtener el icono no seleccionado.
fun getUnselectedIconForScreen(screen: Screen): ImageVector {
    return when (screen) {
        Screen.Home -> Icons.Outlined.Home
        Screen.Presupuestos -> Icons.Outlined.AttachMoney
        Screen.Chat -> Icons.Outlined.Chat
        Screen.Calendar -> Icons.Outlined.CalendarToday
        Screen.Promo -> Icons.Outlined.LocalFireDepartment
        else -> Icons.Default.BrokenImage
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}
