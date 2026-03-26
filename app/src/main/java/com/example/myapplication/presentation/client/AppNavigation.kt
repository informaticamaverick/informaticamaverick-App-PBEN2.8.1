package com.example.myapplication.presentation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ==================================================================================
// --- DEFINICIÓN CENTRALIZADA DE TODAS LAS RUTAS DE LA APLICACIÓN ---
// ==================================================================================
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
    //object ChatConversation : Screen("chat_conversation/{providerId}", "Conversación de Chat")
    object Fast : Screen("fast", "Maverick FAST")
    object Login : Screen("login", "Iniciar Sesión")
}
 //* AppNavigation: Orquestador principal de la navegación y el HUD Global.
 //* Gestiona el cerebro de Be (BeBrainViewModel) y la inyección de dependencias.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    // Inyectamos el nuevo ViewModel renombrado que actúa como cerebro del HUD
    hudViewModel: BeBrainViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel() // 🔥 NUEVO: ViewModel de categorías
) {
    // Suscripción reactiva a los estados del cerebro de Be
    val showBe by hudViewModel.showBe.collectAsStateWithLifecycle()
    val isSearchActive by hudViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQueries by hudViewModel.searchQuery.collectAsStateWithLifecycle() // 🔥 NUEVO: Para búsqueda en tiempo real
    val beMessages by hudViewModel.beMessages.collectAsStateWithLifecycle()
    val currentActions by hudViewModel.currentActions.collectAsStateWithLifecycle()
    val favorites by providerViewModel.favorites.collectAsStateWithLifecycle()
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle() // 🔥 NUEVO: Categorías globales
    val isBottomBarVisible by hudViewModel.isBottomBarVisible.collectAsStateWithLifecycle()// 🔥 NUEVO: Suscripción a la visibilidad de la barra de navegación
    // Estados de composición del HUD (Dormido y Herramientas BeBuild)
    // 🔥 CAMBIO: Ahora recolectamos estos estados como lo hacemos con los demás
    val isDormido by hudViewModel.isBeDormido.collectAsStateWithLifecycle()
    val showBeTools by hudViewModel.showBeTools.collectAsStateWithLifecycle()
    val requestKeyboard by hudViewModel.requestKeyboard.collectAsStateWithLifecycle() // 🔥 CONEXIÓN: Recolectamos el estado del teclado del ViewModel


    AppNavigationContent(
        beViewModel = hudViewModel,
        showBe = showBe,
        isSearchActive = isSearchActive,
        searchQuery = searchQueries, // 🔥 Pasamos el query
        beMessages = beMessages,
        isDormido = isDormido,
        showBeTools = showBeTools,
        requestKeyboard = requestKeyboard, // 👈 Pasamos el estado aquí
        currentActions = currentActions,
        favorites = favorites,
        allCategories = categories, // 🔥 Pasamos las categorías
        isBottomBarVisible = isBottomBarVisible,
        onRouteChanged = { hudViewModel.onRouteChanged(it) },
        onToggleFavorite = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) },
        onBeClick = { hudViewModel.onBeClick() },
        onBeLongClick = { hudViewModel.onBeLongClick() },
        onBeDoubleClick = { hudViewModel.onBeDoubleClick() },
        onSearchQueryChange = { hudViewModel.updateSearchQuery(it) } // 🔥 Callback de actualización
    )
}
 //* AppNavigationContent: Estructura interna que contiene el NavHost y overlays globales.
 @RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigationContent(
    beViewModel: BeBrainViewModel,
    showBe: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    beMessages: List<BeMessage>,
    isDormido: Boolean,
    showBeTools: Boolean,
    requestKeyboard: Boolean, // 👈 Agregamos este parámetro
    currentActions: List<BeSmallActionModel>,
    favorites: List<Provider>,
    allCategories: List<CategoryEntity>, // 🔥 NUEVO: Todas las categorías
    isBottomBarVisible: Boolean,
    onRouteChanged: (String?) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onBeClick: () -> Unit,
    onBeLongClick: () -> Unit,
    onBeDoubleClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val navController = rememberNavController()
    var isInConversation by remember { mutableStateOf(false) }
    var showFavoritesPanel by remember { mutableStateOf(false) }
    val navItems = listOf(Screen.Home, Screen.Presupuestos, Screen.Chat, Screen.Calendar, Screen.Promo)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // 🔥 NUEVO: Observamos los eventos de acción del ViewModel (como el botón Fast)
    LaunchedEffect(beViewModel.actionEvent) {
        beViewModel.actionEvent.collectLatest { actionId ->
            when (actionId) {
                "fast" -> navController.navigate(Screen.Fast.route) { launchSingleTop = true }
                "licit" -> navController.navigate(Screen.CrearLicitacion.route) { launchSingleTop = true }
                "fav" -> showFavoritesPanel = !showFavoritesPanel
            }
        }

        //Notificamos al cerebro cada cambio de pantalla para adaptar el contexto de Be
    }

    LaunchedEffect(currentRoute) { onRouteChanged(currentRoute) }

    val chatBaseRoute = Screen.Chat.route.split("?").first()
    val shouldShowBottomBar = currentRoute?.split("?")?.first() in navItems.map { it.route.split("?").first() } &&
                              isBottomBarVisible &&
                              currentRoute?.split("?")?.first() != chatBaseRoute

    fun getRouteIndex(route: String?): Int {
        if (route == null) return -1
        val baseRoute = route.substringBefore("?").substringBefore("/")
        return navItems.indexOfFirst { it.route.substringBefore("?").substringBefore("/") == baseRoute }
    }
    // --- LÓGICA DE TRANSICIONES ANIMADAS ---
    val mainEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
        val initialIndex = getRouteIndex(initialState.destination.route)
        val targetIndex = getRouteIndex(targetState.destination.route)
        if (initialIndex != -1 && targetIndex != -1) {
            if (targetIndex > initialIndex) slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        } else fadeIn(tween(300))
    }
    val mainExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
        val initialIndex = getRouteIndex(initialState.destination.route)
        val targetIndex = getRouteIndex(targetState.destination.route)
        if (initialIndex != -1 && targetIndex != -1) {
            if (targetIndex > initialIndex) slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        } else fadeOut(tween(300))

    }
    val secondaryEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(600, easing = FastOutSlowInEasing))
    }
    val secondaryExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(600, easing = FastOutSlowInEasing))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppHUDShell(
            showBe = showBe,
            isSearchActive = isSearchActive,
            searchQuery = searchQuery, // 🔥 Pasamos el query
            beMessages = beMessages,
            isDormido = isDormido,
            showBeTools = showBeTools,
            currentActions = currentActions,
            shouldShowBottomBar = shouldShowBottomBar,
            bottomBar = {
                AppBottomNavigationBar(navController = navController, allItems = navItems, currentRoute = currentRoute)
            },
            onBeClick = onBeClick,
            onBeLongClick = onBeLongClick,
            onBeDoubleClick = onBeDoubleClick,
            onSearchQueryChange = onSearchQueryChange // 🔥 Pasamos el callback
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.fillMaxSize()) {
                composable(route = Screen.Home.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) {
                    HomeScreenComplete(navController = navController, bottomPadding = innerPadding, beViewModel = beViewModel)
                }
                composable(route = Screen.Presupuestos.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) {
                    PresupuestosScreen(viewModel = hiltViewModel(), onBack = { navController.popBackStack() }, onChatClick = { pid -> navController.navigate("chat?providerId=$pid") }, bottomPadding = innerPadding)
                }
                composable(route = Screen.Chat.route, arguments = listOf(navArgument("providerId") { type = NavType.StringType; nullable = true; defaultValue = null }), enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId")
                    ChatScreen(onBack = { navController.popBackStack() }, initialProviderId = providerId)
                }
                composable(route = Screen.Calendar.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { CalendarScreen(onBack = { navController.popBackStack() }) }
                composable(route = Screen.Promo.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { PromoScreen(navController = navController, onBack = { navController.popBackStack() }) }
                composable(route = Screen.CrearLicitacion.route) { CrearLicScreen(onBack = { navController.popBackStack() }) }
                composable(route = Screen.PerfilCliente.route) { PerfilUsuarioScreen(onNavigateBack = { navController.popBackStack() }, onLogout = { }) }
                composable(route = Screen.ResultBusqueda.route, arguments = listOf(navArgument("category") { type = NavType.StringType })) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    ResultBusquedaCategoriaScreen(categoryName = category, onBack = { navController.popBackStack() }, onNavigateToProviderProfile = { pid -> navController.navigate("perfil_prestador/$pid") }, onNavigateToChat = { pid -> navController.navigate("chat?providerId=$pid") })
                }
                composable(route = Screen.PerfilPrestador.route, arguments = listOf(navArgument("providerId") { type = NavType.StringType }), enterTransition = secondaryEnterTransition, exitTransition = secondaryExitTransition) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                    PerfilPrestadorCliente(providerId = providerId, onBack = { navController.popBackStack() })
                }
                composable(route = Screen.Fast.route, enterTransition = secondaryEnterTransition, exitTransition = secondaryExitTransition) {
                    FastScreen(navController = navController)
                }
                composable(route = Screen.Login.route, enterTransition = secondaryEnterTransition, exitTransition = secondaryExitTransition) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Login Screen Placeholder") }
                }
            }
        }
        // --- PANTALLA DE RESULTADOS DE BE (GLOBAL OVERLAY) ---
        // Se abre automáticamente al activar la búsqueda en HOME
        BeResultadoScreen(
            viewModel = beViewModel,
            onClose = { beViewModel.setResultadoVisible(false) },
            onProviderClick = { pid -> navController.navigate("perfil_prestador/$pid") },
            allCategories = allCategories,
            favoriteProviders = favorites,
            onCategoryClick = { cat -> beViewModel.setResultadoVisible(false); navController.navigate("result_busqueda/$cat") },
            onSuperCategoryClick = { beViewModel.setResultadoVisible(false) }
        )
        // --- COMPONENTE BE ASSISTANT GLOBAL (OVERLAY SUPERIOR) ---
        // 🔥 MODIFICACIÓN: Se mueve aquí para que siempre esté por encima de BeResultadoScreen
        AnimatedVisibility(
            visible = showBe,
            enter = scaleIn(spring(dampingRatio = 0.6f)) + fadeIn(),
            exit = scaleOut(spring(dampingRatio = 0.8f)) + fadeOut(),
            modifier = Modifier.zIndex(500f) // Máxima prioridad visual
        ) {
            // Posicionamiento dinámico de Be
            val beVerticalBias by animateFloatAsState(
                targetValue = if (isSearchActive) -1f else 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                label = "be_v_bias"
            )
            val beHorizontalPadding by animateDpAsState(
                targetValue = if (isSearchActive) 0.dp else 0.dp,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                label = "be_h_pad"
            )
            val topInsets = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val beTopPadding by animateDpAsState(targetValue = if (isSearchActive) topInsets + 8.dp else 0.dp)
            val beBottomPadding by animateDpAsState(targetValue = if (isSearchActive) 0.dp else 40.dp)
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // 🔥 MODIFICACIÓN CLAVE: Obliga al contenedor padre a usar todo el ancho de la pantalla.
                        .align(BiasAlignment(horizontalBias = 1f, verticalBias = beVerticalBias))
                        .navigationBarsPadding()
                        .padding(
                            end = beHorizontalPadding,
                            top = beTopPadding,
                            bottom = beBottomPadding
                        )
                ) {
                    BeAssistantSearchFab(
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        contextMessages = beMessages,
                        onSearchQueryChange = onSearchQueryChange,
                        onSearchStateChange = { },
                        onBubbleActionClick = { },
                        isDormido = isDormido,
                        currentActions = currentActions,
                        showSmallActions = showBeTools,
                        requestKeyboard = requestKeyboard,
                        onToggleSearch = onBeClick,
                        onToggleActions = onBeLongClick,
                        onToggleSleep = onBeDoubleClick
                    )
                }
            }
        }

           /**
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.align(BiasAlignment(horizontalBias = 1f, verticalBias = beVerticalBias))
                        .navigationBarsPadding().padding(end = beHorizontalPadding, top = beTopPadding, bottom = beBottomPadding)
                ) {
                    BeAssistantSearchFab(
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        contextMessages = beMessages,
                        onSearchQueryChange = onSearchQueryChange,
                        onSearchStateChange = { },
                        onBubbleActionClick = { },
                        isDormido = isDormido,
                        currentActions = currentActions,
                        showSmallActions = showBeTools,
                        requestKeyboard = requestKeyboard, // 🔥 CONEXIÓN FINAL: El componente ahora recibe la orden
                        onToggleSearch = onBeClick,
                        onToggleActions = onBeLongClick,
                        onToggleSleep = onBeDoubleClick
                    )
                }
            }
        }

        **/
        // --- PANEL DE FAVORITOS (GLOBAL OVERLAY) ---
        if (showFavoritesPanel) {
            Box(modifier = Modifier.fillMaxSize().zIndex(600f).background(Color.Black.copy(alpha = 0.65f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showFavoritesPanel = false })
            AnimatedVisibility(visible = showFavoritesPanel, enter = slideInHorizontally(initialOffsetX = { it }), exit = slideOutHorizontally(targetOffsetX = { it }), modifier = Modifier.align(Alignment.CenterEnd).zIndex(610f)) {
                FavoritesPanel(navController = navController, favorites = favorites, onClose = { showFavoritesPanel = false }, onToggleFavorite = onToggleFavorite)
            }
        }
    }
}
// * AppHUDShell: Capa superior que encapsula al asistente Be y gestiona los Scrims de fondo.
@Composable
fun AppHUDShell(
    showBe: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    beMessages: List<BeMessage>,
    isDormido: Boolean,
    showBeTools: Boolean,
    currentActions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean,
    bottomBar: @Composable () -> Unit,
    onBeClick: () -> Unit,
    onBeLongClick: () -> Unit,
    onBeDoubleClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. CAPA BASE: EL CONTENIDO Y LA BARRA DE NAVEGACIÓN ---
        Scaffold(
            bottomBar = { if (shouldShowBottomBar) bottomBar() },
            containerColor = Color.Transparent
        ) { innerPadding ->
            content(innerPadding)
        }
        // 🔥 ELIMINADO: Bloque residual de Blur Negro que causaba el error visual.
    }
}
// * AppBottomNavigationBar: Barra de navegación inferior con efectos visuales y emojis.
@Composable
fun AppBottomNavigationBar(navController: NavHostController, allItems: List<Screen>, currentRoute: String?) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth().height(65.dp).zIndex(550f),
        containerColor = Color.Black,
        tonalElevation = 0.dp
    ) {
        allItems.forEach { screen ->
            val isSelected = currentRoute?.startsWith(screen.route.split("?").first()) == true
            val scope = rememberCoroutineScope()
            val rotation = remember { Animatable(0f) }

            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent, unselectedIconColor = Color.White.copy(alpha = 0.85f), unselectedTextColor = Color.White.copy(alpha = 0.45f), selectedIconColor = Color.White, selectedTextColor = Color.White),
                selected = isSelected,
                onClick = {
                    scope.launch {
                        // 🔥 EFECTO SHAKE AL TOCAR
                        rotation.animateTo(12f, tween(50, easing = LinearEasing))
                        rotation.animateTo(-12f, tween(50, easing = LinearEasing))
                        rotation.animateTo(0f, tween(50, easing = LinearEasing))
                        
                        val baseRoute = screen.route.split("?").first()
                        navController.navigate(baseRoute) {
                            navController.graph.findStartDestination().let { startDest ->
                                popUpTo(startDest.id) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Box(modifier = Modifier.graphicsLayer { rotationZ = rotation.value }) {
                        AnimatedContent(
                            targetState = isSelected,
                            transitionSpec = { (scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                            label = "nav_icon_animation"
                        ) { selected ->
                            if (selected) Text(getEmojiForScreen(screen), fontSize = 26.sp)
                            else {
                                val icon = when (screen) {
                                    is Screen.Home -> Icons.Filled.Home
                                    is Screen.Presupuestos -> Icons.Filled.AttachMoney
                                    is Screen.Chat -> Icons.AutoMirrored.Filled.Chat
                                    is Screen.Calendar -> Icons.Filled.CalendarToday
                                    is Screen.Promo -> Icons.Filled.LocalFireDepartment
                                    else -> Icons.Filled.Home
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                            }
                        }
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
@Preview(showBackground = true)
@Composable
fun AppBottomNavigationBarPreview() {
    val navItems = listOf(Screen.Home, Screen.Presupuestos, Screen.Chat, Screen.Calendar, Screen.Promo)
    MyApplicationTheme { AppBottomNavigationBar(navController = rememberNavController(), allItems = navItems, currentRoute = Screen.Home.route) }
}
