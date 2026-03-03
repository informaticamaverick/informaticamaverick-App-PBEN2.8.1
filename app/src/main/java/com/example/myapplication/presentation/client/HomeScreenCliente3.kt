package com.example.myapplication.presentation.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==================================================================================
// --- SECCIÓN: MODELOS DE DATOS UI ---
// ==================================================================================

sealed interface BannerContent {
    data class Category(val item: CategoryEntity) : BannerContent
    data class GoogleAd(val title: String, val contentDescription: String, val imageUrl: String) : BannerContent
    data class ProviderPromo(val provider: Provider, val promoTitle: String) : BannerContent
}

data class SuperCategory(
    val title: String,
    val icon: String, // 🔥 AHORA RECIBE EL ICONO DIRECTO DE ROOM
    val items: List<CategoryEntity>
)

sealed class LocationOption {
    data class Gps(val address: String, val locality: String) : LocationOption()
    data class Personal(val address: String, val number: String, val locality: String) : LocationOption()
    data class Business(val companyName: String, val branchName: String, val address: String, val number: String, val locality: String) : LocationOption()
}

// ==================================================================================
// --- SECCIÓN: PANTALLA PRINCIPAL ---
// ==================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    profileViewModel: ProfileSharedViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    simulationViewModel: SimulationViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(context)
    )

    val providers by providerViewModel.providers.collectAsStateWithLifecycle()
    val favorites by providerViewModel.favorites.collectAsStateWithLifecycle()
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val userState by profileViewModel.userState.collectAsState()

    val temperature by weatherViewModel.temperature.collectAsState()
    val weatherEmoji by weatherViewModel.weatherEmoji.collectAsState()
    val weatherDescription by weatherViewModel.weatherDescription.collectAsState()
    val cityName by locationViewModel.locationName.collectAsState()
    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            locationViewModel.fetchLocation()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) locationViewModel.fetchLocation()
        else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            weatherViewModel.fetchWeather(lat = latitude!!, lon = longitude!!)
        }
    }

    HomeScreenContent(
        navController = navController,
        bottomPadding = bottomPadding,
        userState = userState,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        onRefreshLocation = { locationViewModel.fetchLocation() },
        allProviders = providers,
        favoriteProviders = favorites,
        allCategories = categories,
        onToggleFavorite = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) },
        onLogout = {
            profileViewModel.logout()
            navController.navigate(Screen.Login.route) { popUpTo(0) }
        },
        simulationViewModel = simulationViewModel
    )
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    userState: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    onRefreshLocation: () -> Unit,
    allProviders: List<Provider>,
    favoriteProviders: List<Provider>,
    allCategories: List<CategoryEntity>,
    onToggleFavorite: (String, Boolean) -> Unit,
    onLogout: () -> Unit,
    simulationViewModel: SimulationViewModel? = null
) {
    val isSystemInDarkMode = isSystemInDarkTheme()
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    var isSearchActive by remember { mutableStateOf(false) }
    var showWeatherDetails by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    // --- ESTADOS DE VISTA ---
    var isSuperCategoryView by remember { mutableStateOf(true) }
    var showViewMenu by remember { mutableStateOf(false) }

    // Estados de Scroll separados para poder reiniciarlos
    val superCatGridState = rememberLazyStaggeredGridState()
    val individualCatGridState = rememberLazyGridState()

    // Resetea la vista y el scroll cada vez que la pantalla entra en composición
    LaunchedEffect(Unit, refreshTrigger) {
        isSuperCategoryView = true
        superCatGridState.scrollToItem(0)
        individualCatGridState.scrollToItem(0)
    }

    // Estado del panel táctico del FAB
    var activeFilters by remember { mutableStateOf(setOf<String>()) }

    var currentLocationState by remember {
        mutableStateOf<LocationOption>(LocationOption.Gps(address = cityName, locality = ""))
    }

    LaunchedEffect(cityName) {
        if (cityName.isNotEmpty() && currentLocationState is LocationOption.Gps) {
            currentLocationState = LocationOption.Gps(
                address = cityName,
                locality = "Ubicación Actual"
            )
        }
    }

    // Estado para el panel de Supercategorías
    var selectedSuperCategory by remember { mutableStateOf<SuperCategory?>(null) }
    val showSuperCategoryPanel = selectedSuperCategory != null

    // 🔥 AGRUPACIÓN DE SUPERCATEGORÍAS 100% REAL DE ROOM
    val superCategories = remember(allCategories, refreshTrigger) {
        allCategories.groupBy { it.superCategory }
            .map { entry ->
                // Sacamos el icono desde el primer elemento de la lista (viene de Room)
                val dbIcon = entry.value.firstOrNull()?.superCategoryIcon ?: "📂"
                SuperCategory(
                    title = entry.key,
                    icon = dbIcon,
                    items = entry.value.shuffled()
                )
            }
            .shuffled()
    }

    val bannerItems = remember(allCategories, allProviders, refreshTrigger) {
        generateEnrichedBannerItems(allCategories, allProviders)
    }

    val closeSearch: () -> Unit = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
    }

    Scaffold(
        containerColor = Color(0xFF0A0E14)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // --- 1. CONTENIDO PRINCIPAL: GRID ---
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(145.dp))

                if (bannerItems.isNotEmpty()) {
                    PremiumLensCarousel(
                        items = bannerItems,
                        onSettingsClick = { /* Abrir settings */ },
                        onItemClick = { banner ->
                            if (banner.originalCategory != null) {
                                navController.navigate("result_busqueda/${banner.originalCategory.name}")
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // --- ENCABEZADO DE SERVICIOS ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXPLORA SERVICIOS",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        if (!isSuperCategoryView) {
                            Text(
                                text = "Vista: Categorías Aleatorias",
                                color = Color(0xFF2197F5),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botón de 3 puntos o Botón de Limpiar (X)
                    if (!isSuperCategoryView) {
                        IconButton(
                            onClick = {
                                isSuperCategoryView = true
                                coroutineScope.launch { superCatGridState.scrollToItem(0) }
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Restaurar Vista", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = { showViewMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones de Vista", tint = Color.White.copy(alpha = 0.7f))
                            }

                            DropdownMenu(
                                expanded = showViewMenu,
                                onDismissRequest = { showViewMenu = false },
                                modifier = Modifier.background(Color(0xFF161C24)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Vista por Grupos (Default)",
                                            color = if(isSuperCategoryView) Color(0xFF2197F5) else Color.White,
                                            fontWeight = if(isSuperCategoryView) FontWeight.Black else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        isSuperCategoryView = true
                                        showViewMenu = false
                                        coroutineScope.launch { superCatGridState.scrollToItem(0) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Vista Aleatoria (Todas)",
                                            color = if(!isSuperCategoryView) Color(0xFF2197F5) else Color.White,
                                            fontWeight = if(!isSuperCategoryView) FontWeight.Black else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        isSuperCategoryView = false
                                        showViewMenu = false
                                        coroutineScope.launch { individualCatGridState.scrollToItem(0) }
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))

                // --- LISTA CONDICIONAL SEGÚN VISTA ---
                if (isSuperCategoryView) {
                    LazyVerticalStaggeredGrid(
                        state = superCatGridState,
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = paddingValues.calculateBottomPadding() + 100.dp),
                        verticalItemSpacing = 16.dp,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(superCategories) { superCat ->
                            val height = 180.dp // Tamaño fijo
                            BentoSuperCategoryCard(
                                superCategory = superCat,
                                emoji = superCat.icon, // 🔥 LECTURA DIRECTA DE ROOM
                                height = height,
                                onClick = { selectedSuperCategory = superCat }
                            )
                        }
                    }
                } else {
                    // Vista Aleatoria de Categorías Compactas
                    val allShuffled = remember(allCategories, refreshTrigger) { allCategories.shuffled() }

                    LazyVerticalGrid(
                        state = individualCatGridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = paddingValues.calculateBottomPadding() + 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allShuffled) { category ->
                            CompactCategoryCard(
                                item = category,
                                onClick = { navController.navigate("result_busqueda/${category.name}") }
                            )
                        }
                    }
                }
            }

            // --- 2. CAPA HEADER (Glassmorphism) ---
            Box(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.65f)))
                Box(modifier = Modifier.statusBarsPadding()) {
                    TopHeaderSection(
                        navController = navController,
                        user = userState,
                        temperature = temperature,
                        weatherEmoji = weatherEmoji,
                        weatherDescription = weatherDescription,
                        cityName = cityName,
                        currentLocationState = currentLocationState,
                        onWeatherClick = { showWeatherDetails = !showWeatherDetails },
                        onRefreshLocation = {
                            currentLocationState = LocationOption.Gps(address = "Actualizando...", locality = "")
                            onRefreshLocation()
                        },
                        onLocationSelected = { nuevaSeleccion -> currentLocationState = nuevaSeleccion },
                        onLogout = onLogout
                    )
                }
            }

            // --- 3. PANEL EXPANDIDO DEL CLIMA ---
            if (showWeatherDetails) {
                Box(modifier = Modifier.fillMaxSize().zIndex(1.5f).background(Color.Black.copy(alpha = 0.5f)).clickable { showWeatherDetails = false })
            }

            AnimatedVisibility(
                visible = showWeatherDetails,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 60.dp).zIndex(2f)
            ) {
                WeatherExpandedCard(
                    temperature = temperature,
                    weatherEmoji = weatherEmoji,
                    weatherDescription = weatherDescription,
                    cityName = cityName,
                    forecastDays = emptyList()
                )
            }

            // --- 4. BÚSQUEDA ---
            if (isSearchActive) {
                Box(modifier = Modifier.fillMaxSize().zIndex(8f).background(Color.Black.copy(alpha = 0.5f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { closeSearch() })
            }

            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }),
                modifier = Modifier.zIndex(10f).align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GeminiTopSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it }, placeholderText = "Buscar servicios...")
                    }
                    val rainbowBrush = geminiGradientEffect()
                    Surface(
                        modifier = Modifier.size(56.dp).clickable(onClick = closeSearch),
                        shape = CircleShape, color = Color(0xFF121212), border = BorderStroke(2.5.dp, rainbowBrush), shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(26.dp)) }
                    }
                }
            }

            AnimatedVisibility(
                visible = isSearchActive && searchQuery.isNotEmpty(),
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
                modifier = Modifier.zIndex(9f).align(Alignment.TopCenter).padding(top = 130.dp)
            ) {
                SearchResultsPanel(searchQuery = searchQuery, allCategories = allCategories, onCategoryClick = { categoryName -> closeSearch(); navController.navigate("result_busqueda/$categoryName") })
            }

            // --- 5. PANEL DE FAVORITOS ---
            if (showFavorites) {
                Box(modifier = Modifier.fillMaxSize().zIndex(11f).background(Color.Black.copy(alpha = 0.65f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showFavorites = false })
            }

            AnimatedVisibility(
                visible = showFavorites,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)
            ) {
                FavoritesPanel(navController = navController, favorites = favoriteProviders, onClose = { showFavorites = false }, onToggleFavorite = onToggleFavorite)
            }

            // --- 6. PANEL DE SUPERCATEGORÍAS (ANIMACIÓN DESDE ABAJO) ---
            if (showSuperCategoryPanel) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(13f)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { selectedSuperCategory = null }
                )
            }

            AnimatedVisibility(
                visible = showSuperCategoryPanel,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(14f)
            ) {
                selectedSuperCategory?.let { superCat ->
                    SuperCategoryDetailsPanel(
                        superCategory = superCat,
                        onClose = { selectedSuperCategory = null },
                        onCategoryClick = { categoryName ->
                            selectedSuperCategory = null
                            navController.navigate("result_busqueda/$categoryName")
                        }
                    )
                }
            }

            // --- 7. FAB GEMINI DIVIDIDO ---
            val fabSurfaceColor = if (isSystemInDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.surface
            GeminiFABWithScrim(
                bottomPadding = bottomPadding,
                showScrim = !isFabMenuExpanded
            ) {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = fabSurfaceColor)) {
                    GeminiSplitFAB(
                        isExpanded = isFabMenuExpanded,
                        isSearchActive = isSearchActive,
                        onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                        onActivateSearch = { isSearchActive = true },
                        onCloseSearch = closeSearch,
                        activeFilters = activeFilters,
                        onAction = { actionId ->
                            when (actionId) {
                                // Aquí atrapamos los IDs de las herramientas tácticas
                                "sim_chat" -> {
                                    isFabMenuExpanded = false
                                    simulationViewModel?.simulateProviderWelcomeAndBudget(userState?.id)
                                }
                                "sim_lic" -> {
                                    isFabMenuExpanded = false
                                    simulationViewModel?.simulateTenderResponses()
                                }
                                "refresh" -> {
                                    isFabMenuExpanded = false
                                    refreshTrigger++
                                }
                                else -> {
                                    // Filtros y Ordenamiento (Toggle)
                                    activeFilters = if (activeFilters.contains(actionId)) activeFilters - actionId else activeFilters + actionId
                                }
                            }
                        },
                        onResetAll = {
                            activeFilters = emptySet()
                            isFabMenuExpanded = false
                        },
                        // BOTONES LATERALES ORIGINALES
                        secondaryActions = {
                            SmallActionFab(icon = Icons.Default.Gavel, label = "Licitar", iconColor = Color(0xFF4285F4), onClick = { navController.navigate(Screen.CrearLicitacion.route) })
                            SmallActionFab(icon = Icons.Default.Bolt, label = "Rápido", iconColor = Color(0xFFFBC02D), onClick = { navController.navigate(Screen.Fast.route) })
                            SmallActionFab(icon = Icons.Default.Favorite, label = "Favs", iconColor = Color(0xFFE91E63), onClick = { showFavorites = true; isFabMenuExpanded = false })
                        }
                    )
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE UI ---
// ==================================================================================

@Composable
fun BentoSuperCategoryCard(superCategory: SuperCategory, emoji: String, height: Dp, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(height).clickable(onClick = onClick).shadow(12.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().blur(radius = 20.dp).alpha(0.35f)) {
                LazyVerticalGrid(GridCells.Fixed(2), userScrollEnabled = false) {
                    items(superCategory.items.take(4)) { item ->
                        Text(item.icon, fontSize = 55.sp, modifier = Modifier.padding(8.dp).alpha(0.5f))
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(superCategory.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("${superCategory.items.size} servicios", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            }
            Text(emoji, fontSize = 44.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).alpha(0.85f))
        }
    }
}

@Composable
fun SuperCategoryDetailsPanel(
    superCategory: SuperCategory,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val rows = (superCategory.items.size + 1) / 2
    val estimatedHeight = minOf(0.85f, (rows * 0.15f + 0.25f).toFloat())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(estimatedHeight),
        color = Color(0xFF0A0E14),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation = 16.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera del Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = superCategory.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Selecciona una categoría para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }

                // Grid Reutilizando CompactCategoryCard
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(superCategory.items) { category ->
                        CompactCategoryCard(
                            item = category,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }
                }
            }

            // Botón X Flotante (Esquina superior derecha flotando)
            Surface(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .zIndex(10f), // Asegura que esté por encima del scroll
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun FavoritesPanel(
    navController: NavHostController,
    favorites: List<Provider>,
    onClose: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(320.dp),
        color = Color(0xFF0A0E14),
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
            HorizontalDivider(color = Color.White.copy(0.1f))
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (favorites.isEmpty()) {
                    item { Text("No tienes favoritos guardados.", modifier = Modifier.fillMaxWidth().padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center) }
                } else {
                    items(favorites, key = { it.id }) { provider ->
                        PrestadorCard(provider = provider, onClick = { navController.navigate("perfil_prestador/${provider.id}") }, onToggleFavorite = { id, isFav -> onToggleFavorite(id, isFav) }, showPreviews = false, viewMode = "Compacta", onChat = { navController.navigate("chat/${provider.id}") })
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderSection(
    navController: NavHostController,
    user: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    currentLocationState: LocationOption,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit
) {
    val cardGradientBrush = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(0.8f)) { WeatherWidget(temperature, weatherEmoji, cityName, onWeatherClick, cardGradientBrush) }
        Box(Modifier.weight(1.6f)) { LocationSelector(user = user, currentLocation = currentLocationState, onRefresh = onRefreshLocation, onLocationSelected = onLocationSelected, brush = cardGradientBrush) }
        Box(Modifier.weight(0.8f)) { ProfileSection(user, navController, onAddressSelected = onLocationSelected, onLogout, cardGradientBrush) }
    }
}

@Composable
fun WeatherWidget(temp: String, emoji: String, city: String, onClick: () -> Unit, brush: Brush) {
    Card(onClick = onClick, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.Transparent), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
        Box(Modifier.fillMaxSize().background(brush).padding(4.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(emoji, fontSize = 22.sp)
                Text(temp, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(city, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(0.7f))
            }
        }
    }
}

@Composable
fun LocationSelector(
    user: UserEntity?,
    currentLocation: LocationOption,
    onRefresh: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    brush: Brush
) {
    var expanded by remember { mutableStateOf(false) }

    val (linea1, linea2, linea3) = when (currentLocation) {
        is LocationOption.Gps -> Triple("UBICACIÓN ACTUAL", currentLocation.address, "GPS Activo")
        is LocationOption.Personal -> Triple("MI CASA / PERSONAL", "${currentLocation.address} ${currentLocation.number}", currentLocation.locality)
        is LocationOption.Business -> Triple(currentLocation.companyName.uppercase(), currentLocation.branchName, "${currentLocation.address} ${currentLocation.number}")
    }

    Box(modifier = Modifier.fillMaxWidth().padding(end = 6.dp)) {
        Card(onClick = { expanded = true }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.Transparent), border = BorderStroke(1.dp, Color.White.copy(0.15f)), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.background(brush).padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 46.dp)) {
                Column {
                    Text(text = linea1, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = linea2, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = linea3, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-14).dp),
            shape = CircleShape,
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.6f)),
            shadowElevation = 6.dp
        ) {
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Actualizar GPS", tint = Color(0xFF22D3EE), modifier = Modifier.size(20.dp))
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xFF0D1117).copy(alpha = 0.95f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))) {
            DropdownMenuItem(text = { Text("Usar GPS Actual", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold) }, onClick = { onRefresh(); expanded = false }, leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = Color(0xFF22D3EE)) })
            HorizontalDivider(color = Color.White.copy(0.1f))
            user?.personalAddresses?.forEach { addr ->
                DropdownMenuItem(text = { Column { Text("${addr.calle} ${addr.numero}", color = Color.White); Text(addr.localidad, fontSize = 10.sp, color = Color.Gray) } }, onClick = { onLocationSelected(LocationOption.Personal(addr.calle, addr.numero, addr.localidad)); expanded = false })
            }
            if (user?.companies?.isNotEmpty() == true) {
                HorizontalDivider(color = Color.White.copy(0.1f))
                user.companies.forEach { company ->
                    company.branches.forEach { branch ->
                        DropdownMenuItem(text = { Column { Text("${company.name} - ${branch.name}", color = Color.White, fontWeight = FontWeight.Bold); Text("${branch.address.calle} ${branch.address.numero}", fontSize = 11.sp, color = Color.Gray) } }, onClick = { onLocationSelected(LocationOption.Business(company.name, branch.name, branch.address.calle, branch.address.numero, branch.address.localidad)); expanded = false })
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    user: UserEntity?,
    navController: NavHostController,
    onAddressSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit,
    brush: Brush
) {
    var showPopup by remember { mutableStateOf(false) }
    val displayName = remember(user) { user?.name?.trim()?.split(" ")?.firstOrNull()?.uppercase() ?: "PERFIL" }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scaleAnimation")

    Box(
        modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale).background(brush, RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).clickable(interactionSource = interactionSource, indication = ripple(), onClick = { showPopup = true }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(4.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
                if (user?.photoUrl != null) {
                    AsyncImage(model = user.photoUrl, contentDescription = "Avatar", modifier = Modifier.matchParentSize().clip(CircleShape).border(1.0.dp, Color(0xFF22D3EE), CircleShape), contentScale = ContentScale.Crop, error = rememberVectorPainter(Icons.Default.Person))
                } else {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = displayName, style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    if (showPopup && user != null) {
        Popup(alignment = Alignment.TopEnd, onDismissRequest = { showPopup = false }, properties = PopupProperties(focusable = true)) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showPopup = false }) {
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }
                Box(Modifier.align(Alignment.TopEnd).padding(top = 70.dp, end = 12.dp).width(340.dp).clickable(enabled = false) {}) {
                    androidx.compose.animation.AnimatedVisibility(visible = animateIn, enter = expandVertically(expandFrom = Alignment.Top) + fadeIn() + slideInVertically { -40 }, exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()) {
                        UserProfilePopup(user = user, onClose = { showPopup = false }, onLogout = { showPopup = false; onLogout() }, onAddressSelected = { onAddressSelected(it); showPopup = false }, onProfileClick = { showPopup = false; navController.navigate(Screen.PerfilCliente.route) })
                    }
                }
            }
        }
    }
}

fun generateEnrichedBannerItems(categories: List<CategoryEntity>, providers: List<Provider>): List<AccordionBanner> {
    val bannerList = mutableListOf<AccordionBanner>()
    categories.filter { it.isNew }.forEach { cat -> bannerList.add(AccordionBanner(id = "cat_new_${cat.name}", title = cat.name, subtitle = "¡Nueva categoría disponible!", icon = cat.icon, color = Color(cat.color), type = BannerType.NEW_CATEGORY, originalCategory = cat, isNew = true)) }
    categories.filter { it.isNewPrestador }.forEach { cat -> bannerList.add(AccordionBanner(id = "cat_prov_${cat.name}", title = cat.name, subtitle = "Nuevos profesionales registrados", icon = cat.icon, color = Color(cat.color), type = BannerType.NEW_PROVIDER, originalCategory = cat)) }
    val googleAds = listOf(Triple("Google Workspace", "Potencia tu productividad", "https://workspace.google.com/static/img/google-workspace-logo.png"), Triple("Google Cloud", "La nube más avanzada", "https://cloud.google.com/static/images/social-icon-google-cloud-1200-630.png"), Triple("Google Pixel 9", "El nuevo estándar en IA", "https://lh3.googleusercontent.com/pw/AP1GczPH_Z_X_..."), Triple("YouTube Premium", "Música y videos sin anuncios", "https://www.gstatic.com/youtube/img/branding/youtubepremium/2x/youtubepremium_logo_dark_64.png"))
    googleAds.shuffled().forEachIndexed { index, ad -> bannerList.add(AccordionBanner(id = "ad_google_$index", title = ad.first, subtitle = ad.second, icon = "📢", color = Color(0xFF1967D2), type = BannerType.GOOGLE_AD, imageUrl = ad.third)) }
    val sampleProducts = listOf(Triple("Kit de Herramientas", "Marca Bosch - Profesional", "🔧"), Triple("Pintura Sintética", "Oferta por 20L - Varios colores", "🎨"), Triple("Cámara de Seguridad", "Instalación incluida", "📹"), Triple("Grifería de Lujo", "Diseño moderno y duradero", "🚰"))
    providers.filter { it.isSubscribed }.shuffled().take(3).forEachIndexed { index, provider -> val product = sampleProducts.getOrNull(index % sampleProducts.size) ?: Triple("Producto Especial", "Consultar disponibilidad", "🎁"); bannerList.add(AccordionBanner(id = "product_${provider.id}_$index", title = product.first, subtitle = "Vendido por ${provider.displayName}", icon = product.third, color = Color(0xFF43A047), type = BannerType.PRODUCT_SALE)) }
    providers.filter { it.isSubscribed }.shuffled().take(2).forEach { provider -> bannerList.add(AccordionBanner(id = "promo_${provider.id}", title = provider.displayName, subtitle = "¡Aprovecha esta oferta especial!", icon = "🔥", color = Color(0xFFE91E63), type = BannerType.PROMO)) }
    return bannerList.shuffled()
}

@Composable
fun SearchResultsPanel(searchQuery: String, allCategories: List<CategoryEntity>, onCategoryClick: (String) -> Unit) {
    val prefixMatches = remember(searchQuery, allCategories) { allCategories.filter { it.name.startsWith(searchQuery, ignoreCase = true) } }
    val approximateMatches = remember(searchQuery, allCategories) { allCategories.filter { it.name.contains(searchQuery, ignoreCase = true) && !it.name.startsWith(searchQuery, ignoreCase = true) } }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (prefixMatches.isEmpty() && approximateMatches.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { Text("No se encontraron resultados", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = Color.Gray) }
            } else {
                if (prefixMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text("Coincidencia Exacta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(prefixMatches) { category -> CompactCategoryCard(item = category, onClick = { onCategoryClick(category.name) }) }
                }
                if (approximateMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text("Resultados relacionados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                    items(approximateMatches) { category -> CompactCategoryCard(item = category, onClick = { onCategoryClick(category.name) }) }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherExpandedCard(temperature: String, weatherEmoji: String, weatherDescription: String, cityName: String, forecastDays: List<ForecastDay>) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(cityName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(weatherDescription, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(weatherEmoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(temperature, fontSize = 64.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun UserProfilePopup(user: UserEntity, onClose: () -> Unit, onLogout: () -> Unit, onAddressSelected: (LocationOption) -> Unit, onProfileClick: () -> Unit) {
    val cyberCyan = Color(0xFF22D3EE); val cyberMagenta = Color(0xFFE91E63); val cyberPurple = Color(0xFF9B51E0); val deepGlass = Color(0xFF0D1117).copy(alpha = 0.92f)
    var personalExpanded by remember { mutableStateOf(true) }
    var businessExpanded by remember { mutableStateOf(true) }
    GeminiCyberWrapper(modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 1.dp), cornerRadius = 24.dp, isAnimated = true, showGlow = true) {
        Column(modifier = Modifier.background(deepGlass).fillMaxWidth().heightIn(max = 650.dp).verticalScroll(rememberScrollState()).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(text = "DATOS DE USUARIO V4", style = MaterialTheme.typography.labelSmall, color = cyberCyan, fontWeight = FontWeight.Black, letterSpacing = 2.sp); Text(text = "STATUS: ACTIVE_SESSION", fontSize = 8.sp, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.03f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).clickable { onProfileClick() }.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) { Box(modifier = Modifier.size(64.dp).border(1.dp, cyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))); AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)).border(1.5.dp, cyberCyan, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop) }
                    Spacer(Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) { Text(text = "${user.name} ${user.lastName}".uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp); Text(text = "UID: ${user.email}", style = MaterialTheme.typography.labelSmall, color = cyberCyan.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    Icon(Icons.Default.QrCodeScanner, null, tint = cyberCyan, modifier = Modifier.size(20.dp).graphicsLayer { alpha = 0.5f })
                }
            }
            Spacer(Modifier.height(32.dp))
            CyberTreeDirectory(title = "DIR_PERSONALES", icon = Icons.Default.FolderOpen, accentColor = cyberCyan, isExpanded = personalExpanded, onToggle = { personalExpanded = !personalExpanded }) {
                user.personalAddresses.forEach { addr -> CyberTreeLeaf(icon = Icons.Default.LocationOn, title = "${addr.calle} ${addr.numero}", subtitle = "${addr.localidad}, ${addr.provincia}", accentColor = cyberCyan, onClick = { onAddressSelected(LocationOption.Personal(address = addr.calle, number = addr.numero, locality = addr.localidad)); onClose() }) }
            }
            Spacer(Modifier.height(16.dp))
            if (user.companies.isNotEmpty()) {
                CyberTreeDirectory(title = "DIR_EMPRESA/COMERCIO", icon = Icons.Default.Dns, accentColor = cyberPurple, isExpanded = businessExpanded, onToggle = { businessExpanded = !businessExpanded }) {
                    user.companies.forEach { company ->
                        var companyItemExpanded by remember { mutableStateOf(false) }
                        CyberTreeDirectory(title = company.name.uppercase(), icon = Icons.Default.Business, accentColor = cyberPurple.copy(alpha = 0.8f), isExpanded = companyItemExpanded, isNested = true, onToggle = { companyItemExpanded = !companyItemExpanded }) {
                            company.branches.forEach { branch -> CyberTreeLeaf(icon = Icons.Default.Storefront, title = branch.name, subtitle = "${branch.address.calle} ${branch.address.numero}", accentColor = cyberPurple, isNested = true, onClick = { onAddressSelected(LocationOption.Business(companyName = company.name, branchName = branch.name, address = branch.address.calle, number = branch.address.numero, locality = branch.address.localidad)); onClose() }) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Box(modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(12.dp)).background(cyberMagenta.copy(alpha = 0.05f)).border(1.dp, Brush.horizontalGradient(listOf(cyberMagenta, Color.Transparent)), RoundedCornerShape(12.dp)).clickable { onLogout() }.padding(horizontal = 20.dp), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PowerSettingsNew, null, tint = cyberMagenta, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(16.dp)); Text(text = "Cerrar_Sesion", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp) }
                Box(modifier = Modifier.align(Alignment.CenterEnd).size(8.dp).background(cyberMagenta, CircleShape).blur(4.dp))
            }
        }
    }
}

@Composable
private fun CyberTreeDirectory(title: String, icon: ImageVector, accentColor: Color, isExpanded: Boolean, isNested: Boolean = false, onToggle: () -> Unit, content: @Composable () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f)
    Column(modifier = Modifier.padding(start = if (isNested) 16.dp else 0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = accentColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp).rotate(rotation))
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        }
        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Box(modifier = Modifier.padding(start = 7.dp).drawWithCache { onDrawWithContent { drawLine(color = accentColor.copy(alpha = 0.2f), start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 1.dp.toPx()); drawContent() } }) { Column { content() } }
        }
    }
}

@Composable
private fun CyberTreeLeaf(icon: ImageVector, title: String, subtitle: String, accentColor: Color, isNested: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 6.dp).drawWithCache { onDrawWithContent { drawLine(color = accentColor.copy(alpha = 0.2f), start = Offset(0f, size.height / 2), end = Offset(15.dp.toPx(), size.height / 2), strokeWidth = 1.dp.toPx()); drawContent() } }.padding(start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(16.dp))
        Column { Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(text = subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenCompletePreview() {
    MyApplicationTheme {
        HomeScreenContent(
            navController = rememberNavController(),
            bottomPadding = PaddingValues(0.dp),
            userState = null,
            temperature = "25°C",
            weatherEmoji = "☀️",
            weatherDescription = "Despejado",
            cityName = "Paraná",
            onRefreshLocation = {},
            allProviders = emptyList(),
            favoriteProviders = emptyList(),
            allCategories = emptyList(),
            onToggleFavorite = { _, _ -> },
            onLogout = {},
            simulationViewModel = null
        )
    }
}


