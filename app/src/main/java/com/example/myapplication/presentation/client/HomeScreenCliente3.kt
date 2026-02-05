package com.example.myapplication.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import com.example.myapplication.data.model.OpenMeteoResponse
import com.example.myapplication.R
import com.example.myapplication.presentation.client.LocationViewModel
import com.example.myapplication.presentation.client.LocationViewModelFactory
import com.example.myapplication.presentation.client.WeatherViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.fake.CategoryItem
import com.example.myapplication.data.model.fake.CategorySampleDataFalso
import com.example.myapplication.data.model.fake.SampleDataFalso
import com.example.myapplication.presentation.client.ProfileSharedViewModel
import com.example.myapplication.presentation.client.Screen
import com.example.myapplication.data.model.fake.UserFalso
import com.example.myapplication.presentation.components.GeminiFABWithScrim
import com.example.myapplication.presentation.components.GeminiSplitFAB
import com.example.myapplication.presentation.components.GeminiTopSearchBar
import com.example.myapplication.presentation.components.SmallActionFab
import com.example.myapplication.presentation.components.SmallFabTool
import com.example.myapplication.presentation.components.geminiGradientEffect
import com.example.myapplication.data.model.fake.UserSampleDataFalso

// ==================================================================================
// --- SECCIÓN: MODELOS DE DATOS ---
// ==================================================================================

/**
 * Define los modos en los que puede estar la interfaz del cliente.
 */
enum class ProfileMode {
    CLIENTE, EMPRESA
}

/**
 * Interfaz sellada para representar los diferentes tipos de items en el banner.
 */
sealed interface BannerContent {
    data class Category(val item: CategoryItem) : BannerContent
    data class GoogleAd(val title: String, val contentDescription: String, val imageUrl: String) : BannerContent
    data class ProviderPromo(val provider: UserFalso, val promoTitle: String) : BannerContent // UserFalso
}

/**
 * Representa una "Súper Categoría" (ej: Hogar, Tecnología) que agrupa varias subcategorías.
 */
data class SuperCategory(
    val title: String,
    val items: List<CategoryItem>
)

// ==================================================================================
// --- SECCIÓN: PANTALLA PRINCIPAL (HOME SCREEN) ---
// ==================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    viewModel: ProfileSharedViewModel = hiltViewModel(), // <-- OBTENER VIEWMODEL
    weatherViewModel: WeatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel() // <-- WEATHER VIEWMODEL
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationViewModel: LocationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = LocationViewModelFactory(context)
    )
    
    // Solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permisos otorgados, obtener ubicación
            locationViewModel.fetchLocation()
        }
    }
    
    // --- ESTADOS DE LA UI ---
    val userState by viewModel.userState.collectAsState()
    
    // --- ESTADOS DEL CLIMA ---
    val temperature by weatherViewModel.temperature.collectAsState()
    val weatherEmoji by weatherViewModel.weatherEmoji.collectAsState()
    val weatherData by weatherViewModel.weatherData.collectAsState()
    val weatherDescription by weatherViewModel.weatherDescription.collectAsState()
    val windSpeed by weatherViewModel.windSpeed.collectAsState()
    val humidity by weatherViewModel.humidity.collectAsState()

    // --- ESTADOS DE UBICACIÓN ---
    val cityName by locationViewModel.locationName.collectAsState()
    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()
    
    // --- OBTENER UBICACIÓN Y CLIMA AL INICIAR ---
    LaunchedEffect(Unit) {
        // Verificar si ya tiene permisos
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            locationViewModel.fetchLocation()
        } else {
            // Solicitar permisos
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // --- ACTUALIZAR CLIMA CUANDO CAMBIA LA UBICACIÓN ---
    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            weatherViewModel.fetchWeather(lat = latitude!!, lon = longitude!!)
        } else {
            // Ubicación por defecto: Tucumán
            weatherViewModel.fetchWeather(lat = -26.8083, lon = -65.2176)
        }
    }

    HomeScreenContent(
        navController = navController,
        bottomPadding = bottomPadding,
        userState = userState,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherData = weatherData,
        weatherDescription = weatherDescription,
        windSpeed = windSpeed,
        humidity = humidity,
        cityName = cityName,
        onRefreshLocation = { locationViewModel.fetchLocation() },
        latitude = latitude,
        longitude = longitude,
        onLogout = {
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) // Limpiar pila de navegación
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    userState: UserEntity?,
    temperature: String = "24°C",
    weatherEmoji: String = "☀️",
    weatherData: OpenMeteoResponse? = null,
    weatherDescription: String = "Despejado",
    windSpeed: String = "15 km/h",
    humidity: String = "60%",
    cityName: String = "Tucumán",
    onRefreshLocation: () -> Unit = {},
    latitude: Double? = null,
    longitude: Double? = null,
    onLogout: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val isSystemInDarkMode = isSystemInDarkTheme()
    var isSearchActive by remember { mutableStateOf(false) }
    var showWeatherDetails by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var allCategoryExpanded by remember { mutableStateOf(true)}

    // Estado para la dirección seleccionada manualmente desde el popup
    var manualSelectedAddress by remember { mutableStateOf<String?>(null) }

    // Weather API - Variables locales modificables
    var currentTemperature by remember { mutableStateOf(temperature) }
    var currentWeatherEmoji by remember { mutableStateOf(weatherEmoji) }
    var currentWeatherDescription by remember { mutableStateOf(weatherDescription) }
    var currentWindSpeed by remember { mutableStateOf(windSpeed) }
    var currentHumidity by remember { mutableStateOf(humidity) }
    var currentCityName by remember { mutableStateOf(cityName) }

    val weatherRepository = remember { WeatherRepository() }
    val coroutineScope = rememberCoroutineScope()
    var forecastDays by remember { mutableStateOf<List<ForecastDay>>(emptyList()) }

    // Actualizar nombre de ciudad cuando cambie
    LaunchedEffect(cityName) {
        if (cityName.isNotEmpty()) {
            currentCityName = cityName
        }
    }

    // Cargar datos del clima usando ubicación GPS real
    LaunchedEffect(latitude, longitude) {
        // Esperar a que haya coordenadas GPS disponibles
        if (latitude != null && longitude != null) {
            coroutineScope.launch {
                try {
                    // Usar coordenadas reales del GPS
                    val weatherData = weatherRepository.getCurrentWeather(latitude, longitude)

                    weatherData?.let { data ->
                        currentTemperature = data.temperature
                        currentWeatherEmoji = data.weatherEmoji
                        currentWeatherDescription = data.weatherDescription
                        currentWindSpeed = data.windSpeed
                        currentHumidity = data.humidity
                    } 

                    forecastDays = weatherRepository.getForecast(latitude, longitude)
                } catch (e: Exception) {
                    android.util.Log.e("WeatherDebug", "Error fetching weather: ${e.message}", e)
                }
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val generatedData = remember(refreshTrigger) {
        generateFakeBannerAndCategories()
    }
    val bannerItems = generatedData.first
    val regularCategories = generatedData.second

    LaunchedEffect(Unit) {
        refreshTrigger++
    }


    val onCategoryClick: (String) -> Unit = { categoryName ->
        navController.navigate("result_busqueda/$categoryName")
    }

    val closeSearch = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
        Unit
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // --- CAPA 1: CONTENIDO DE FONDO (SCROLLABLE) ---
            Column(modifier = Modifier.fillMaxSize()) {
                TopHeaderSection(
                    navController = navController,
                    user = userState, // <-- PASAR USUARIO REAL
                    temperature = currentTemperature,
                    weatherEmoji = currentWeatherEmoji,
                    cityName = currentCityName,
                    gpsLocation = currentCityName, // Pasar ubicación GPS
                    manualSelection = manualSelectedAddress, // Pasar selección manual
                    onWeatherClick = { showWeatherDetails = !showWeatherDetails },
                    onRefreshLocation = {
                         manualSelectedAddress = null // Resetear selección manual al refrescar
                         onRefreshLocation()
                    },
                    onAddressSelected = { selectedAddr -> manualSelectedAddress = selectedAddr }, // Callback
                    onLogout = onLogout // Callback
                )

                // Banner de Novedades (Carrusel Superior Mixto)
                if (bannerItems.isNotEmpty()) {
                    AutoPlayingBannerSection(
                        bannerItems = bannerItems,
                        onCategoryClick = onCategoryClick,
                        navController = navController
                    )
                }

                // Lista Vertical de Categorías
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        // [MODIFICABLE] Espacio inferior para que el contenido no quede tapado por el FAB/BottomBar
                        bottom = paddingValues.calculateBottomPadding() + 80.dp
                    )
                ) {
                    items(regularCategories) { superCat ->
                        SuperCategorySection(
                            superCategory = superCat,
                            onCategoryClick = onCategoryClick,
                            globalExpandState = allCategoryExpanded
                        )
                        Spacer(modifier = Modifier.height(1.dp)) // [MODIFICABLE] Separación entre filas de categorías
                    }
                }
            }



            // --- CAPA 2.5: PANEL EXPANDIDO DEL CLIMA (SOBREPUESTO) + SCRIM ---
             if (showWeatherDetails) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1.5f) // Justo debajo de la tarjeta del clima
                        .background(Color.Black.copy(alpha = 0.5f)) // Scrim oscuro
                        .clickable { showWeatherDetails = false }
                )
            }

            AnimatedVisibility(
                visible = showWeatherDetails,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(250)
                ) + fadeOut(
                    animationSpec = tween(200)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 60.dp) // Ajusta según altura del TopHeaderSection
                    .zIndex(2f)
            ) {
                WeatherExpandedCard(
                    temperature = currentTemperature,
                    weatherEmoji = currentWeatherEmoji,
                    weatherDescription = currentWeatherDescription,
                    windSpeed = currentWindSpeed,
                    humidity = currentHumidity,
                    cityName = currentCityName,
                    forecastDays = forecastDays
                )
            }

            // --- CAPA 2.8: SCRIM DE BÚSQUEDA (TAPA EL CONTENIDO AL BUSCAR) ---
            if (isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(8f) // Debajo de Resultados (9f) y Barra (10f)
                        .background(Color.Black.copy(alpha = 0.5f)) // Opaco oscuro
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { closeSearch() }
                )
            }
            
            // --- CAPA 3: BARRA DE BÚSQUEDA (ANIMADA DESDE ABAJO) ---
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier.zIndex(10f).align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        // Usamos el componente reutilizable GeminiTopSearchBar
                        GeminiTopSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            placeholderText = "Buscar servicios...",
                            focusRequester = remember { FocusRequester() } // El foco se pide dentro del componente
                        )
                    }

                    // Botón de Cerrar (X) estilo Gemini
                    val rainbowBrush = geminiGradientEffect()
                    Surface(
                        modifier = Modifier.size(56.dp).clickable(onClick = closeSearch),
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(2.5.dp, rainbowBrush),
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, "Cerrar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }

            // --- CAPA 4: PANEL DE RESULTADOS DE BÚSQUEDA ---
            AnimatedVisibility(
                visible = isSearchActive && searchQuery.isNotEmpty() && !showFavorites,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)),
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(250)),
                modifier = Modifier
                    .zIndex(9f)
                    .align(Alignment.TopCenter)
                    // [MODIFICABLE] Ajustar este padding si cambia la altura del TopBar o SearchBar
                    .padding(top = 130.dp)
            ) {
                SearchResultsPanel(
                    isVisible = true,
                    searchQuery = searchQuery,
                    onCategoryClick = onCategoryClick
                )
            }

            // --- CAPA 5: PANEL LATERAL DE FAVORITOS ---
            AnimatedVisibility(
                visible = showFavorites,
                enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }), // Entra desde la derecha
                exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }),
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)
            ) {
                FavoritesPanel(
                    navController = navController,
                    onClose = { showFavorites = false },
                    sortBy = "Name",
                    selectionMode = false,
                    selectedIds = emptySet(),
                    searchQuery = "",
                    onToggleSelection = { }
                )
            }

            // --- CAPA 6: FAB DIVIDIDO CON SCRIM REUTILIZABLE ---
            
            // COLOR MATTE BLACK para FAB en modo oscuro
            val fabSurfaceColor = if (isSystemInDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.surface
            
            GeminiFABWithScrim(
                bottomPadding = bottomPadding,
                showScrim = true // Solo mostrar el degradado si no hay menús abiertos
            ) {
                // Sobrescribimos el color surface para los componentes dentro del FAB (si usan MaterialTheme)
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(surface = fabSurfaceColor)
                ) {
                    GeminiSplitFAB(
                        isExpanded = isFabMenuExpanded,
                        isSearchActive = isSearchActive,
                        isSecondaryPanelVisible = showFavorites,
                        onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                        onActivateSearch = { isSearchActive = true },
                        onCloseSearch = closeSearch,
                        onCloseSecondaryPanel = { showFavorites = false },
                        // --- MODIFICADO --- Botones de acción con el nuevo estilo
                        secondaryActions = {
                            SmallActionFab(
                                icon = Icons.Default.Gavel,
                                label = "Licitar",
                                iconColor = Color(0xFF4285F4),
                                onClick = { navController.navigate(Screen.CrearLicitacion.route) }
                            )
                            SmallActionFab(
                                icon = Icons.Default.Bolt,
                                label = "Rápido",
                                iconColor = Color(0xFFFBC02D),
                                onClick = { println("Acción rápida") }
                            )
                            SmallActionFab(
                                icon = Icons.Default.Favorite,
                                label = "Favs",
                                iconColor = Color(0xFFE91E63),
                                onClick = {
                                    showFavorites = true
                                    isFabMenuExpanded = false
                                }
                            )
                        },


                        // Herramientas que salen hacia arriba
                        expandedTools = {
                            SmallFabTool(
                                label = if (allCategoryExpanded)
                                    "Colapsar" else "Expandir",
                                icon = if (allCategoryExpanded)
                                    Icons.Default.UnfoldLess else Icons.Default.UnfoldLess,
                                onClick = {
                                    allCategoryExpanded = !allCategoryExpanded
                                }
                            )

                            SmallFabTool(
                                label = "Actualizar",
                                icon = Icons.Default.Refresh,
                                onClick = {
                                    isFabMenuExpanded = false
                                    refreshTrigger++
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: COMPONENTES DEL TOP BAR (REFACTORIZADO) ---
// ==================================================================================
@Composable
fun TopHeaderSection(
    navController: NavHostController,
    user: UserEntity?,
    temperature: String = "24°C",
    weatherEmoji: String = "☀️",
    cityName: String = "Tucumán",
    gpsLocation: String = "Tucumán",
    manualSelection: String? = null, 
    onWeatherClick: () -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    onAddressSelected: (String) -> Unit = {}, // Callback pasado desde HomeScreenContent
    onLogout: () -> Unit = {} // Callback pasado desde HomeScreenContent
) {
    val isDarkMode = isSystemInDarkTheme()
    // Definimos el color Matte Black para modo oscuro
    val topBarBackgroundColor = if (isDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.surface
    
    // Gradiente blanco semi-transparente para las tarjetas
    val cardGradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Surface(
        color = topBarBackgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. WIDGET DE CLIMA ---
            // Reducido ligeramente para dar espacio
            Box(modifier = Modifier.weight(0.8f)) { 
                WeatherWidget(
                    temperature = temperature,
                    weatherEmoji = weatherEmoji,
                    cityName = cityName,
                    onClick = onWeatherClick,
                    backgroundBrush = cardGradientBrush // Pasamos el gradiente
                )
            }

            // --- 2. SELECTOR DE UBICACIÓN (CORREGIDO) ---
            // Aumentado peso (más ancho horizontalmente)
            Box(modifier = Modifier.weight(1.6f)) {
                LocationSelector(
                    user = user,
                    gpsLocation = gpsLocation,
                    manualSelection = manualSelection, // Pasar la selección manual
                    onRefreshLocation = onRefreshLocation,
                    backgroundBrush = cardGradientBrush // Pasamos el gradiente
                )
            }

            // --- 3. PERFIL (CON POPUP INTEGRADO) ---
            Box(modifier = Modifier.weight(0.8f)) {
                ProfileSection(
                    user = user, 
                    navController = navController,
                    onAddressSelected = onAddressSelected,
                    onLogout = onLogout,
                    backgroundBrush = cardGradientBrush // Pasamos el gradiente
                )
            }
        }
    }
}

// ... (Resto de ForecastDay permanece igual) ...
@Composable
fun ForecastDay(day: String, emoji: String, temp: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = temp,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
    }
}

@Composable
fun LocationSelector(
    user: UserEntity?,
    gpsLocation: String = "Tucumán",
    manualSelection: String? = null, // Parámetro para forzar actualización
    onRefreshLocation: () -> Unit = {},
    backgroundBrush: Brush? = null // Nuevo parámetro opcional
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    var useGpsLocation by remember { mutableStateOf(true) }

    // Lógica para determinar Título y Dirección
    val (title, displayAddress) = remember(user, gpsLocation, useGpsLocation, manualSelection) {
        when {
            // 1. Si hay una selección manual (o forzada)
            manualSelection != null -> {
                // Buscar si coincide con alguna dirección guardada para obtener su "nombre"
                val personal = user?.personalAddresses?.find { "${it.calle} ${it.numero}" == manualSelection }
                val branch = user?.companies?.flatMap { it.branches }?.find { "${it.address.calle} ${it.address.numero}" == manualSelection }
                
                if (personal != null) {
                    // Es una dirección personal (ej: Casa)
                    // Como AddressClient no tiene "nombre" (ej: Casa), usamos "Mi Dirección" o primera palabra
                    "Mi Dirección" to manualSelection
                } else if (branch != null) {
                    // Es una sucursal
                    branch.name to manualSelection
                } else {
                    // Dirección genérica seleccionada
                    "Ubicación Seleccionada" to manualSelection
                }
            }
            // 2. Si el usuario eligió GPS
            useGpsLocation -> "Ubicación Actual" to gpsLocation
            // 3. Por defecto: Primera dirección o GPS
            else -> {
                val firstAddr = user?.personalAddresses?.firstOrNull()
                if (firstAddr != null) {
                    "Mi Dirección" to "${firstAddr.calle} ${firstAddr.numero}"
                } else {
                    "Ubicación Actual" to gpsLocation
                }
            }
        }
    }
    
    // Si llega una selección manual, desactivamos modo GPS
    LaunchedEffect(manualSelection) {
        if (manualSelection != null) {
            useGpsLocation = false
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // Usamos transparente para que se vea el brush si existe
            containerColor = if (backgroundBrush != null) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        // Si hay brush, el borde podría ser redundante o necesitar ajuste, lo mantenemos sutil
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
        onClick = { expanded = true }
    ) {
        // Aplicamos el fondo con Brush si existe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Textos (Izquierda)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayAddress,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.onSurface
                    )
                }
                
                // Botón GPS (Derecha) - Siempre visible o solo cuando es GPS?
                // "COLOCA AL LADO EL BOTON PARA ACTUALIZAR LA UBICACION CON GPS"
                IconButton(
                    onClick = { 
                        useGpsLocation = true
                        onRefreshLocation() 
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Usar GPS",
                        tint = if (useGpsLocation) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
                Text(
                    "Mis Direcciones",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                if (user?.personalAddresses.isNullOrEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "No hay direcciones guardadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    user?.personalAddresses?.forEach { address ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text("Casa/Personal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) // Ejemplo estático, idealmente AddressClient tendría 'nombre'
                                    Text(address.fullString(), style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                useGpsLocation = false
                                // Nota: Esto actualiza localmente, pero el manualSelection tiene prioridad si está seteado
                                expanded = false
                            }
                        )
                    }
                }
            
            HorizontalDivider()

            // Opción de Ubicación Actual con Refresh
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ubicación GPS Actual")
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    useGpsLocation = true
                    onRefreshLocation()
                    expanded = false
                }
            )
        }
    }
}

/**
 * --- VERSIÓN CORREGIDA Y SIMPLIFICADA ---
 * Se renombró a 'ProfileSection' para coincidir con el Header.
 * Se agregó soporte para long click y Popup anclado.
 */
@Composable
fun ProfileSection(
    user: UserEntity?,
    navController: NavHostController,
    onAddressSelected: (String) -> Unit,
    onLogout: () -> Unit,
    backgroundBrush: Brush? = null // Nuevo parámetro
) {
    val colors = MaterialTheme.colorScheme
    var showPopup by remember { mutableStateOf(false) }

    Box { // Contenedor para anclar el popup
        Card(
            modifier = Modifier
                .fillMaxSize()
                // Reemplazamos onClick simple por pointerInput para detectar taps y long press
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showPopup = true }, // Tap abre Popup
                        onLongPress = { navController.navigate(Screen.PerfilCliente.route) } // Long Press va a Perfil
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (backgroundBrush != null) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
                    .padding(horizontal = 8.dp), // Padding interno reajustado
                contentAlignment = Alignment.Center
            ) {
                // Solo mostramos la info del usuario (Imagen + Nombre)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(40.dp) // [MODIFICADO] Aumentado tamaño de 32 a 40
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.iconapp),
                        fallback = painterResource(id = R.drawable.iconapp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.name ?: "Usuario",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- POPUP A PANTALLA COMPLETA CON SCRIM ---
        if (showPopup && user != null) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { showPopup = false },
                properties = PopupProperties(focusable = true)
            ) {
                 // SCRIM (Fondo oscuro transparente)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showPopup = false }
                ) {
                    // CONTENIDO DEL POPUP
                    // Animación de entrada
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(initialOffsetY = { -it/2 }) + fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                             .align(Alignment.TopEnd)
                             .padding(top = 80.dp, end = 16.dp) // Ajuste de posición
                    ) {
                        Box(
                            modifier = Modifier
                                .width(300.dp) // Ancho fijo o relativo
                                .heightIn(max = 450.dp) // Altura máxima con scroll
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = false) {} // Evitar que clicks dentro cierren el popup
                        ) {
                            UserProfilePopup(
                                user = user,
                                onClose = { showPopup = false },
                                onLogout = {
                                    showPopup = false
                                    onLogout()
                                },
                                onAddressSelected = {
                                    showPopup = false
                                    onAddressSelected(it)
                                },
                                onProfileClick = {
                                    showPopup = false
                                    navController.navigate(Screen.PerfilCliente.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- NUEVO COMPONENTE: POPUP DETALLADO DE USUARIO ---
// ==================================================================================
@Composable
fun UserProfilePopup(
    user: UserEntity,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onAddressSelected: (String) -> Unit, // Callback para selección de dirección
    onProfileClick: () -> Unit // Callback para navegar al perfil
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // [MODIFICADO] Wrap content
            .padding(16.dp)
    ) {
        // Encabezado con botón de cierre
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mi Cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "Cerrar", modifier = Modifier.size(16.dp))
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Contenido desplazable
        Column(
            modifier = Modifier
                .weight(1f, fill = false) // fill=false permite que se encoja si hay poco contenido
                .verticalScroll(rememberScrollState()), // [MODIFICADO] Scroll vertical simple
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECCIÓN 1: DATOS PERSONALES (CLICKABLE PARA IR AL PERFIL)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() } // Navega al perfil al tocar la tarjeta del usuario
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            error = painterResource(id = R.drawable.iconapp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("${user.name} ${user.lastName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(user.email, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // SECCIÓN 2: DIRECCIONES PERSONALES
            Text(
                "Mis Direcciones",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (user.personalAddresses.isEmpty()) {
                Text("No hay direcciones.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                user.personalAddresses.forEach { addr ->
                    Card(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                            .clickable { onAddressSelected("${addr.calle} ${addr.numero}") }, // CLICKABLE
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha=0.3f))
                    ) {
                        // [MODIFICADO] Layout de dirección (Nombre arriba, dirección abajo)
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Casa / Personal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${addr.calle} ${addr.numero}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("${addr.localidad}, ${addr.provincia}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            // SECCIÓN 3: MIS NEGOCIOS Y SUCURSALES
            Text(
                "Mis Negocios",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (user.companies.isEmpty()) {
                Text("No hay negocios.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                user.companies.forEach { company ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha=0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha=0.5f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Cabecera del Negocio
                            Text(company.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Lista de Sucursales
                            if (company.branches.isEmpty()) {
                                Text("Sin sucursales", style = MaterialTheme.typography.bodySmall)
                            } else {
                                company.branches.forEach { branch ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { onAddressSelected("${branch.address.calle} ${branch.address.numero}") }, // CLICKABLE
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparente para parecer parte de la lista
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Store, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                                Spacer(Modifier.width(4.dp))
                                                Text(branch.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                            }
                                            Text(
                                                "${branch.address.calle} ${branch.address.numero}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // BOTÓN DE CERRAR SESIÓN (AL PIE)
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}


// ... (Resto del archivo: SearchResultsPanel, FavoritesPanel, Banners, Categories, Previews permanecen igual) ...
@Composable
fun SearchResultsPanel(
    isVisible: Boolean,
    searchQuery: String,
    onCategoryClick: (String) -> Unit
) {
    if (!isVisible) return

    val allCategories = CategorySampleDataFalso.categories

    // 1. Filtrar las que coinciden EXACTAMENTE al inicio (letra por letra desde el principio)
    // Ejemplo: Query "Carp" -> "Carpintería"
    val prefixMatches = remember(searchQuery) {
        allCategories.filter {
            !it.isNew && it.name.startsWith(searchQuery, ignoreCase = true)
        }
    }

    // 2. Filtrar las que CONTIENEN el texto pero NO al inicio
    // Ejemplo: Query "nic" -> "Mecánica"
    val approximateMatches = remember(searchQuery) {
        allCategories.filter {
            !it.isNew &&
            it.name.contains(searchQuery, ignoreCase = true) &&
            !it.name.startsWith(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (prefixMatches.isEmpty() && approximateMatches.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "No se encontraron resultados",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                // Sección: Coincidencia Exacta (Prefijo)
                if (prefixMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Text(
                                "Coincidencia Exacta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                    items(prefixMatches) { category ->
                        Box(modifier = Modifier.height(115.dp)) {
                            CategoryCard(item = category, onClick = { onCategoryClick(category.name) })
                        }
                    }

                    // Separador entre secciones (Solo si hay de ambos tipos)
                    if (approximateMatches.isNotEmpty()) {
                         item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Sección: Resultados Aproximados (Contiene)
                if (approximateMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Text(
                                "Resultados relacionados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                    items(approximateMatches) { category ->
                        Box(modifier = Modifier.height(115.dp)) {
                            CategoryCard(item = category, onClick = { onCategoryClick(category.name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesPanel(
    navController: NavHostController,
    onClose: () -> Unit,
    sortBy: String,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    searchQuery: String,
    onToggleSelection: (String) -> Unit
) {
    // Usamos SampleDataFalso directamente si está disponible.
    val favorites = com.example.myapplication.data.model.fake.SampleDataFalso.prestadores.filter { it.isFavorite }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),

        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Mis Favoritos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            HorizontalDivider()

            // Lista
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (favorites.isEmpty()) {
                    item {
                        Text(
                            "Aún no tienes favoritos.",
                            modifier = Modifier.padding(32.dp),
                            color = Color.Gray
                        )
                    }
                } else {
                    items(favorites) { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("perfil_prestador/${provider.id}") },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             Surface(
                                 shape = CircleShape,
                                 modifier = Modifier.size(50.dp),
                                 color = Color.Gray
                             ) {
                                 // Imagen de perfil
                                 AsyncImage(
                                     model = provider.profileImageUrl,
                                     contentDescription = null,
                                     modifier = Modifier.fillMaxSize(),
                                     contentScale = ContentScale.Crop,
                                     error = painterResource(id = R.drawable.iconapp),
                                     fallback = painterResource(id = R.drawable.iconapp)
                                 )
                             }
                             Spacer(modifier = Modifier.width(12.dp))
                             Column {
                                 Text(
                                     "${provider.name} ${provider.lastName}",
                                     fontWeight = FontWeight.Bold
                                 )
                                 // ADAPTACIÓN: servicios y nombre de empresa
                                 val services = provider.companies.firstOrNull()?.services?.firstOrNull() ?: "Prestador"
                                 val compName = provider.companies.firstOrNull()?.name ?: "Sin Empresa"

                                 Text(
                                     if(services != "Prestador") services else compName,
                                     style = MaterialTheme.typography.bodySmall,
                                     color = Color.Gray
                                 )
                             }
                        }
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: BANNER DE NOVEDADES (AUTO-PLAYING) ---
// ==================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoPlayingBannerSection(
    bannerItems: List<BannerContent>,
    onCategoryClick: (String) -> Unit,
    navController: NavHostController
) {
    val totalItems = bannerItems.size
    // Iniciamos en un número alto para simular scroll infinito (loop)
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2, pageCount = { Int.MAX_VALUE })

    var showAdDialog by remember { mutableStateOf<BannerContent.GoogleAd?>(null) }
    var showPromoDialog by remember { mutableStateOf<BannerContent.ProviderPromo?>(null) }

    // Efecto de Auto-play: Cambia de página cada 4.5 segundos
    LaunchedEffect(pagerState.currentPage) {
        delay(4500)
        pagerState.animateScrollToPage(
            page = pagerState.currentPage + 1,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = Modifier.padding(vertical = 1.dp)) {
        // Título del Banner
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Novedades y Promociones",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            // Línea divisora
            HorizontalDivider(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }

        // Carrusel Horizontal
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(220.dp), // [MODIFICABLE] Ancho de la tarjeta del banner (Doble ancho aprox)
            contentPadding = PaddingValues(horizontal = 45.dp), // [MODIFICABLE] Para ver las tarjetas de los lados
            pageSpacing = 4.dp, // Espacio entre tarjetas
            modifier = Modifier.height(115.dp) // [MODIFICABLE] Altura igual a las categorías normales
        ) { page ->
            // Cálculo para el efecto de Zoom en la tarjeta central
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

            // Escala: 1.0 (100%) si está en el centro, baja hasta 0.9 (90%) si está a los lados
            val scale = lerp(
                start = 0.9f,
                stop = 1.0f,
                fraction = 1f - pageOffset.coerceIn(0f, 1f)
            )

            // Contenedor que aplica la escala y el orden de apilado (zIndex)
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .zIndex(if (pageOffset < 0.5f) 1f else 0f) // La central va por encima
            ) {
                // Seleccionamos la tarjeta según el tipo de contenido
                when (val item = bannerItems[page % totalItems]) {
                    is BannerContent.Category -> {
                        CategoryCard(item = item.item, onClick = { onCategoryClick(item.item.name) })
                    }
                    is BannerContent.GoogleAd -> {
                        GoogleAdCard(
                            item = item,
                            onClick = { showAdDialog = item }
                        )
                    }
                    is BannerContent.ProviderPromo -> {
                        ProviderPromoCard(
                            item = item,
                            onClick = { showPromoDialog = item },
                            onProfileClick = { navController.navigate("perfil_prestador/${item.provider.id}") }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---

    // Diálogo para Google Ads
    if (showAdDialog != null) {
        AlertDialog(
            onDismissRequest = { showAdDialog = null },
            confirmButton = { TextButton(onClick = { showAdDialog = null }) { Text("Cerrar") } },
            title = { Text(showAdDialog!!.title) },
            text = {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.Gray.copy(0.3f)), contentAlignment = Alignment.Center) {
                         Text("Aquí iría el video o imagen del anuncio", textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(showAdDialog!!.contentDescription)
                }
            }
        )
    }

    // Diálogo para Promoción de Prestador
    if (showPromoDialog != null) {
        val promo = showPromoDialog!!
        AlertDialog(
            onDismissRequest = { showPromoDialog = null },
            icon = {
                AsyncImage(
                    model = promo.provider.profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            },
            title = { Text(promo.promoTitle) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡Oferta especial de ${promo.provider.name}!", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Aquí se mostrarían los detalles de la promoción, similar a PromoScreen.", textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPromoDialog = null
                    // Aquí podrías navegar a PromoScreen si tuvieras la ruta configurada
                }) {
                    Text("Ver Promoción Completa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoDialog = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

// ==================================================================================
// --- SECCIÓN: TARJETAS DEL CARRUSEL (BANNER) ---
// ==================================================================================

@Composable
fun GoogleAdCard(item: BannerContent.GoogleAd, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)), // Color estilo Google Ads (azul muy claro)
        modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFF4285F4))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Etiqueta "Ad" pequeña
            Surface(
                color = Color(0xFFFBC02D), // Amarillo Ad
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    "Anuncio",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Contenido Central
            Column(
                modifier = Modifier.align(Alignment.Center).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.PlayCircleOutline, null, modifier = Modifier.size(40.dp), tint = Color(0xFF4285F4))
                Spacer(Modifier.height(4.dp))
                Text(
                    item.title,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1967D2)
                )
            }
        }
    }
}

@Composable
fun ProviderPromoCard(item: BannerContent.ProviderPromo, onClick: () -> Unit, onProfileClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
             // Fondo Imagen (Simulada o real si el prestador tuviera banner de promo)
             Box(modifier = Modifier.fillMaxSize().background(
                 Brush.verticalGradient(
                     listOf(MaterialTheme.colorScheme.primary.copy(alpha=0.2f), MaterialTheme.colorScheme.primary.copy(alpha=0.8f))
                 )
             ))

            // Icono Perfil Prestador (Top Right) - Navega al perfil
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clickable { onProfileClick() }, // Acción separada para ir al perfil
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White),
                shadowElevation = 4.dp
            ) {
                AsyncImage(
                    model = item.provider.profileImageUrl,
                    contentDescription = "Perfil Prestador",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.iconapp)
                )
            }

            // Contenido Texto (Abajo)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "PROMOCIÓN",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    item.promoTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 14.sp
                )

                // ADAPTACIÓN: Nombre empresa
                val companyName = item.provider.companies.firstOrNull()?.name ?: item.provider.name
                Text(
                    companyName,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: FILAS DE CATEGORÍAS (SUPER CATEGORÍAS) ---
// ==================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuperCategorySection(superCategory: SuperCategory, onCategoryClick: (String) -> Unit, globalExpandState: Boolean = true) {
    val totalItems = superCategory.items.size
    var isExpanded by remember { mutableStateOf(globalExpandState) }
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2, pageCount = { Int.MAX_VALUE })

    //Sincornizar con el estado global
    LaunchedEffect(globalExpandState) {
        isExpanded = globalExpandState
    }

    // Cálculo para mostrar "1/10"
    val currentRealIndex = (pagerState.currentPage % totalItems) + 1

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        // Encabezado de la Fila (Título + Divisor + Contador)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded } // Al hacer clic en el título, se contrae
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                superCategory.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp),
                style = MaterialTheme.typography.titleSmall
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
            // CONTADOR 1/10
            Text(
                "$currentRealIndex/$totalItems",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // Contenido Expandible
        AnimatedVisibility(visible = isExpanded) {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(115.dp), // [MODIFICABLE] Ancho de la tarjeta normal
                contentPadding = PaddingValues(horizontal = 6.dp),
                pageSpacing = 2.dp,
                modifier = Modifier.height(115.dp) // [MODIFICABLE] Alto de la tarjeta normal
            ) { page ->
                CategoryCard(
                    item = superCategory.items[page % totalItems],
                    onClick = { onCategoryClick(superCategory.items[page % totalItems].name) }
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: TARJETA DE CATEGORÍA (DISEÑO) ---
// ==================================================================================

@Composable
fun CategoryCard(item: CategoryItem, onClick: () -> Unit) {
    // Estados para los menús contextuales de las etiquetas
    var showNewMenu by remember { mutableStateOf(false) }
    var showPrestadorMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp), // [MODIFICABLE] Redondez de las esquinas
        colors = CardDefaults.cardColors(containerColor = item.color), // Color viene del modelo
        modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con gradiente negro transparente para leer mejor el texto blanco
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.9f)))))

            // ICONO CENTRAL (Emoji o Imagen)
            Text(
                item.icon,
                fontSize = 65.sp, // [MODIFICABLE] Tamaño del icono
                modifier = Modifier.align(Alignment.Center)
            )

            // NOMBRE DE LA CATEGORÍA
            Text(
                item.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp),
                fontSize = 14.sp, // [MODIFICABLE] Tamaño del texto
                maxLines = 2
            )

            // --- ETIQUETAS SUPERPUESTAS (NUEVO / ALERTA) ---
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Etiqueta "NUEVO"
                if (item.isNew) {
                    Box {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary, // [MODIFICABLE] Color etiqueta nuevo
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { showNewMenu = true } // Abre menú al tocar
                        ) {
                            Text(
                                "NUEVO",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        // Menú desplegable para "NUEVO"
                        DropdownMenu(expanded = showNewMenu, onDismissRequest = { showNewMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("¡Esta categoría es nueva!") },
                                onClick = { showNewMenu = false }
                            )
                        }
                    }
                }

                // Icono de Alerta (!)
                if (item.isNewPrestador) {
                    Box {
                        Surface(
                            color = MaterialTheme.colorScheme.error, // [MODIFICABLE] Color alerta
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp).clickable { showPrestadorMenu = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PriorityHigh, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        // Menú desplegable para "ALERTA"
                        DropdownMenu(expanded = showPrestadorMenu, onDismissRequest = { showPrestadorMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Hay nuevos prestadores disponibles") },
                                onClick = { showPrestadorMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Generar lista regular (Super Categorías) excluyendo las que ya están en banner si se quisiera,
// pero generalmente se muestran todas abajo ordenadas.
// Aquí agrupamos todas por su superCategory.\

fun generateFakeBannerAndCategories(): Pair<List<BannerContent>, List<SuperCategory>> {
    //Obtiene todas las categoria del repositorio falso
    val allCategories = CategorySampleDataFalso.categories
    //Crea una lista simulada para el banner (mezcla categorias, anuncios y promo)
    val bannerList = mutableListOf<BannerContent>()

    //Agregamos algunos ejempos al banner si hay categorias disponibles
    if (allCategories.isNotEmpty()) {
        bannerList.add(BannerContent.Category(allCategories.random()))
        bannerList.add(BannerContent.GoogleAd("Anuncio Google", "Publicidad simulada de Google Ads",""))
        bannerList.add(BannerContent.ProviderPromo(SampleDataFalso.prestadores.random(), "Promoción de prestador"))
    }

val regularCats = allCategories.shuffled() // [NUEVO] Barajar categorías generales también
    .groupBy { it.superCategory }
    .map { SuperCategory(it.key, it.value) }

return Pair(bannerList, regularCats)
}

// ==================================================================================
// --- PREVIEWS (VISTA PREVIA EN ANDROID STUDIO) ---
// ==================================================================================

@Preview(showBackground = true)
@Composable
fun FavoritesPanelPreview() {
    MyApplicationTheme {
        FavoritesPanel(rememberNavController(), {}, "Name", false, emptySet(), "", {})
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherForecastItem(
    date: String,
    maxTemp: Int,
    minTemp: Int,
    weatherCode: Int
) {
    val colors = MaterialTheme.colorScheme
    val emoji = getWeatherEmojiFromCode(weatherCode)

    // Parsear fecha para mostrar día de la semana
    val dayName = try {
        val parts = date.split("-")
        if (parts.size == 3) {
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            "$day/$month"
        } else {
            date.takeLast(5)
        }
    } catch (e: Exception) {
        date.takeLast(5)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "$maxTemp°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = " / $minTemp°",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherForecastItemPreview() {
    MyApplicationTheme {
        WeatherForecastItem(
            date = "2026-02-04",
            maxTemp = 32,
            minTemp = 24,
            weatherCode = 1,
        )
    }
}

fun getWeatherEmojiFromCode(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}


// =================================================================================================
// --- COMPONENTE: WIDGET DE CLIMA (TARJETA PEQUEÑA) --- ---
// Este componente NO existía en el proyecto, por eso aparecía en rojo en 'TopHeaderSection'.
// SOLUCIÓN: Definimos aquí el diseño visual de la tarjeta pequeña del clima.
// =================================================================================================

@Composable
fun WeatherWidget(
    temperature: String,
    weatherEmoji: String,
    cityName: String,
    onClick: () -> Unit,
    backgroundBrush: Brush? = null // Nuevo parámetro
) {
    Card(
        modifier = Modifier.fillMaxSize(), // Tamaño flexible dentro del peso
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (backgroundBrush != null) Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = weatherEmoji, fontSize = 22.sp)

                Text(
                    text = temperature,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = cityName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// ==================================================================================
// --- COMPONENTE: TARJETA DE CLIMA EXPANDIDA ---
// ==================================================================================


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherExpandedCard(
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    windSpeed: String,
    humidity: String,
    cityName: String,
    forecastDays: List<ForecastDay>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Encabezado: Ciudad y Estado
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weatherDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Clima Actual (Emoji Gigante y Temp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = weatherEmoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = temperature,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Detalles (Viento y Humedad)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(icon = Icons.Default.Air, label = "Viento", value = windSpeed)
                WeatherDetailItem(icon = Icons.Default.WaterDrop, label = "Humedad", value = humidity)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Pronóstico (Lista)
            Text(
                text = "Pronóstico Extendido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (forecastDays.isEmpty()) {
                Text("Cargando pronóstico...", style = MaterialTheme.typography.bodySmall)
            } else {
                forecastDays.forEach { day ->
                    WeatherForecastItem(
                        date = day.date,
                        maxTemp = day.maxTemp.toInt(),
                        minTemp = day.minTemp.toInt(),
                        weatherCode = day.weatherCode
                    )
                }
            }
        }
    }
}

// Sub-componente para detalles pequeños (Viento/Humedad)
@Composable
fun WeatherDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenCompletePreview() {
    val navController = rememberNavController()

    // Usuario Falso para Preview
    val fakeUser = UserEntity(
        id = "user_preview",
        email = "preview@example.com",
        displayName = "Preview User",
        name = "Juan",
        lastName = "Perez",
        phoneNumber = "123456789",
        matricula = null,
        titulo = null,
        photoUrl = null,
        bannerImageUrl = null,
        hasCompanyProfile = false,
        isSubscribed = true,
        isVerified = true,
        isOnline = true,
        isFavorite = false,
        rating = 4.5f,
        createdAt = System.currentTimeMillis()
    )

    MyApplicationTheme {
        HomeScreenContent(
            navController = navController,
            bottomPadding = PaddingValues(0.dp),
            userState = fakeUser
        )
    }
}
