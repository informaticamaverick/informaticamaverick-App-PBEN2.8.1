package com.example.myapplication.ui.screens.client.home

import android.os.Build
import com.example.myapplication.mock.sample.UserFalso
import com.example.myapplication.mock.sample.SampleDataFalso
import com.example.myapplication.ui.components.geminiGradientEffect
import com.example.myapplication.ui.components.GeminiTopSearchBar
import com.example.myapplication.ui.components.GeminiFABWithScrim
import com.example.myapplication.ui.components.GeminiSplitFAB
import com.example.myapplication.ui.components.SmallActionFab
import com.example.myapplication.ui.components.SmallFabTool
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.viewmodel.ProfileSharedViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import com.example.myapplication.mock.sample.*
import com.example.myapplication.data.model.OpenMeteoResponse
import com.example.myapplication.R
import com.example.myapplication.viewmodel.LocationViewModel
import com.example.myapplication.viewmodel.LocationViewModelFactory
import com.example.myapplication.viewmodel.WeatherViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.data.model.ProfileMode

// ==================================================================================
// --- SECCIÓN: MODELOS DE DATOS ---
// ==================================================================================

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
    
    // --- ESTADOS DE LA UI (AHORA DESDE EL VIEWMODEL) ---
    val profileMode by viewModel.profileMode.collectAsState() // <-- ESTADO CENTRALIZADO
    val userState by viewModel.userState.collectAsState() // <-- DATOS DEL USUARIO
    
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
        profileMode = profileMode,
        userState = userState,
        onModeToggle = viewModel::toggleProfileMode,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherData = weatherData,
        weatherDescription = weatherDescription,
        windSpeed = windSpeed,
        humidity = humidity,
        cityName = cityName,
        onRefreshLocation = { locationViewModel.fetchLocation() },
        latitude = latitude,
        longitude = longitude
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    profileMode: ProfileMode,
    userState: UserEntity?,
    onModeToggle: () -> Unit,
    temperature: String = "24°C",
    weatherEmoji: String = "☀️",
    weatherData: OpenMeteoResponse? = null,
    weatherDescription: String = "Despejado",
    windSpeed: String = "15 km/h",
    humidity: String = "60%",
    cityName: String = "Tucumán",
    onRefreshLocation: () -> Unit = {},
    latitude: Double? = null,
    longitude: Double? = null
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
        android.util.Log.d("WeatherDebug", "LaunchedEffect triggered - Lat: $latitude, Lon: $longitude")
        
        // Esperar a que haya coordenadas GPS disponibles
        if (latitude != null && longitude != null) {
            android.util.Log.d("WeatherDebug", "Fetching weather for coordinates: $latitude, $longitude")
            
            coroutineScope.launch {
                try {
                    // Usar coordenadas reales del GPS
                    val weatherData = weatherRepository.getCurrentWeather(latitude, longitude)
                    android.util.Log.d("WeatherDebug", "Weather data received: $weatherData")
                    
                    weatherData?.let { data ->
                        currentTemperature = data.temperature
                        currentWeatherEmoji = data.weatherEmoji
                        currentWeatherDescription = data.weatherDescription
                        currentWindSpeed = data.windSpeed
                        currentHumidity = data.humidity
                        // NO sobrescribir el nombre de ciudad, usar el del GPS
                        android.util.Log.d("WeatherDebug", "Weather updated: ${currentCityName}, ${data.temperature}")
                    } ?: android.util.Log.e("WeatherDebug", "Weather data is null")
                    
                    forecastDays = weatherRepository.getForecast(latitude, longitude)
                    android.util.Log.d("WeatherDebug", "Forecast days: ${forecastDays.size}")
                } catch (e: Exception) {
                    android.util.Log.e("WeatherDebug", "Error fetching weather: ${e.message}", e)
                }
            }
        } else {
            android.util.Log.w("WeatherDebug", "Latitude or longitude is null")
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
                    profileMode = profileMode,
                    user = userState, // <-- PASAR USUARIO REAL
                    onModeToggle = onModeToggle, // <-- ACCIÓN CENTRALIZADA
                    temperature = currentTemperature,
                    weatherEmoji = currentWeatherEmoji,
                    cityName = currentCityName,
                    gpsLocation = currentCityName, // Pasar ubicación GPS
                    onWeatherClick = { showWeatherDetails = !showWeatherDetails },
                    onRefreshLocation = onRefreshLocation
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


            
            // --- CAPA 2.5: PANEL EXPANDIDO DEL CLIMA (SOBREPUESTO) ---
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
            GeminiFABWithScrim(
                bottomPadding = bottomPadding,
                showScrim = true // Solo mostrar el degradado si no hay menús abiertos
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
                        SmallFabTool(label = if (allCategoryExpanded)
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

// ==================================================================================
// --- SECCIÓN: COMPONENTES DEL TOP BAR (REFACTORIZADO) ---
// ==================================================================================
@Composable
fun TopHeaderSection(
    navController: NavHostController,
    profileMode: ProfileMode,
    user: UserEntity?, // <-- AHORA RECIBE EL USUARIO REAL (Puede ser null al cargar)
    onModeToggle: () -> Unit,
    temperature: String = "24°C",
    weatherEmoji: String = "☀️",
    cityName: String = "Tucumán",
    gpsLocation: String = "Tucumán",
    onWeatherClick: () -> Unit = {},
    onRefreshLocation: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    // --- NUEVO --- Fondo adaptativo para toda la barra superior
    Surface(
        color = colors.surface,
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
            // --- 1. COLUMNA IZQUIERDA: WIDGET DE CLIMA ---
            Box(modifier = Modifier.weight(1f)) {
                WeatherWidget(
                    temperature = temperature,
                    weatherEmoji = weatherEmoji,
                    cityName = cityName,
                    onClick = onWeatherClick
                )
            }

            // --- 2. COLUMNA CENTRAL: UBICACIÓN Y SELECTOR ---
            Box(modifier = Modifier.weight(1.3f)) {
                LocationSelector(
                    user = user, 
                    mode = profileMode,
                    gpsLocation = gpsLocation,
                    onRefreshLocation = onRefreshLocation
                )
            }

            // --- 3. COLUMNA DERECHA: PERFIL Y CAMBIO DE MODO ---
            Box(modifier = Modifier.weight(1f)) {
                ProfileModeSection(
                    user = user,
                    mode = profileMode,
                    onModeToggle = onModeToggle,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun WeatherWidget(
    temperature: String, 
    weatherEmoji: String, 
    cityName: String = "Tucumán",
    onClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 0.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = weatherEmoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = temperature,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = cityName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

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
    mode: ProfileMode,
    gpsLocation: String = "Tucumán",
    onRefreshLocation: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    var useGpsLocation by remember { mutableStateOf(true) } // Usar GPS por defecto
    
    // DEBUG: Log para verificar datos del usuario
    LaunchedEffect(user) {
        android.util.Log.d("LocationSelector", "User: ${user?.name}")
        android.util.Log.d("LocationSelector", "GPS Location: $gpsLocation")
        android.util.Log.d("LocationSelector", "Personal Addresses: ${user?.personalAddresses?.size}")
        android.util.Log.d("LocationSelector", "Companies: ${user?.companies?.size}")
        user?.personalAddresses?.forEach { address ->
            android.util.Log.d("LocationSelector", "Address: ${address.fullString()}")
        }
    }
    
    // Calcular la dirección a mostrar
    val displayAddress = remember(user, mode, gpsLocation, useGpsLocation) {
        if (useGpsLocation) {
            gpsLocation
        } else {
            if (mode == ProfileMode.CLIENTE) {
                user?.personalAddresses?.firstOrNull()?.calle ?: "Sin Dirección"
            } else {
                user?.companies?.firstOrNull()?.branches?.firstOrNull()?.address?.calle 
                    ?: user?.companies?.firstOrNull()?.name 
                    ?: "Sin Sucursal"
            }
        }
    }
    
    var currentAddress by remember { mutableStateOf(displayAddress) }

    // Actualizar la dirección cuando cambie
    LaunchedEffect(displayAddress) {
        currentAddress = displayAddress
        android.util.Log.d("LocationSelector", "Updated address: $currentAddress")
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
        onClick = { expanded = true }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Solo Texto Centrado
            Text(
                text = if (user == null) "Cargando..." else currentAddress,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = colors.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            // --- Lógica para mostrar direcciones según el modo ---
            if (mode == ProfileMode.CLIENTE) {
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
                            text = { Text(address.fullString()) },
                            onClick = {
                                useGpsLocation = false
                                currentAddress = address.calle
                                expanded = false
                            }
                        )
                    }
                }
            } else {
                Text(
                    "Mis Empresas", 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                
                if (user?.companies.isNullOrEmpty()) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "No hay empresas registradas",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            ) 
                        },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    user?.companies?.forEach { company ->
                        company.branches.forEach { branch ->
                             DropdownMenuItem(
                                text = { Text("${company.name} - ${branch.name}") },
                                onClick = {
                                    useGpsLocation = false
                                    currentAddress = branch.name
                                    expanded = false
                                }
                            )
                        }
                    }
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
                    currentAddress = gpsLocation
                    onRefreshLocation()
                    expanded = false
                }
            )
        }
    }
}
/**
 * --- MODIFICADO ---
 * Sección de perfil con nombre, modo en dos líneas y nuevo estilo.
 */
@Composable
fun ProfileModeSection(
    user: UserEntity?,
    mode: ProfileMode,
    onModeToggle: () -> Unit,
    navController: NavHostController
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
        onClick = onModeToggle
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Columna para Modo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Modo", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray
                )
                Text(
                    text = if (mode == ProfileMode.CLIENTE) "Cliente" else "Empresa",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (mode == ProfileMode.CLIENTE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                )
            }
            
            // Columna para Perfil (Imagen y Nombre)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate(Screen.PerfilCliente.route) }
            ) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .size(32.dp)
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
}

// ... (El resto del archivo permanece igual)
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
    val favorites = SampleDataFalso.prestadores.filter { it.isFavorite }

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

// ==================================================================================
// --- SECCIÓN: AUXILIARES Y DATOS FALSOS ---
// ==================================================================================

fun generateFakeBannerAndCategories(): Pair<List<BannerContent>, List<SuperCategory>> {
    val allCategories = CategorySampleDataFalso.categories
    val bannerList = mutableListOf<BannerContent>()

    // 1. Agregar Google Ads (Falso)
    bannerList.add(
        BannerContent.GoogleAd(
            title = "Anuncio Patrocinado",
            contentDescription = "Descubre las mejores ofertas en herramientas.",
            imageUrl = ""
        )
    )

    // 2. Agregar Promociones de Prestadores Suscritos (isSubscribed = true)
    // Se toma de PrestadorSampleDataFalso
    val subscribedProviders = SampleDataFalso.prestadores.filter { it.isSubscribed }
    subscribedProviders.forEach { provider ->
        // ADAPTACIÓN: obtener primer servicio si existe
        val firstService = provider.companies.firstOrNull()?.services?.firstOrNull() ?: "Servicios"
        bannerList.add(
            BannerContent.ProviderPromo(
                provider = provider,
                promoTitle = "¡20% OFF en $firstService!"
            )
        )
    }

    // 3. Agregar Categorías "Nuevas" al Banner
    val newCategories = allCategories.filter { it.isNew || it.isNewPrestador }
    newCategories.forEach { cat ->
        bannerList.add(BannerContent.Category(cat))
    }

    // Mezclar el banner para variedad
    bannerList.shuffle()

    // 4. Generar lista regular (Super Categorías) excluyendo las que ya están en banner si se quisiera,
    // pero generalmente se muestran todas abajo ordenadas.
    // Aquí agrupamos todas por su superCategory.
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

@Preview(showBackground = true)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenCompletePreview() { 
    val fakeUser = UserEntity(
        id = "preview_user",
        email = "preview@example.com",
        displayName = "Preview User",
        name = "Preview",
        lastName = "User",
        phoneNumber = "+54 123456789",
        additionalEmails = emptyList(),
        additionalPhones = emptyList(),
        matricula = null,
        titulo = null,
        photoUrl = null,
        bannerImageUrl = null,
        galleryImages = emptyList(),
        personalAddresses = listOf(
            com.example.myapplication.data.model.AddressClient(
                calle = "Calle Falsa",
                numero = "123",
                localidad = "Tucuman",
                provincia = "Tucuman",
                pais = "Argentina",
                codigoPostal = "4000"
            )
        ),
        companies = emptyList(),
        hasCompanyProfile = false,
        isSubscribed = false,
        isVerified = false,
        isOnline = true,
        isFavorite = false,
        rating = 4.5f,
        favoriteProviderIds = emptyList(),
        createdAt = System.currentTimeMillis()
    )

    MyApplicationTheme { 
        HomeScreenContent(
            navController = rememberNavController(), 
            bottomPadding = PaddingValues(0.dp),
            profileMode = ProfileMode.CLIENTE,
            userState = fakeUser,
            onModeToggle = {}
        )
    } 
}

// ==================================================================================
// --- SECCIÓN: TARJETA EXPANDIDA DEL CLIMA ---
// ==================================================================================
@Composable
fun WeatherExpandedCard(
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    windSpeed: String,
    humidity: String,
    cityName: String = "Tucumán",
    forecastDays: List<ForecastDay> = emptyList()
) {
    val colors = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.85f) // Más transparencia
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ciudad y descripción del clima
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = weatherDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Temperatura con emoji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherEmoji,
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = temperature,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = colors.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información adicional: Viento y Humedad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherInfoCard(
                    icon = Icons.Default.Air,
                    label = "Viento",
                    value = windSpeed,
                    modifier = Modifier.weight(1f)
                )
                
                WeatherInfoCard(
                    icon = Icons.Default.WaterDrop,
                    label = "Humedad",
                    value = humidity,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pronóstico extendido de 5 días
            Text(
                text = "Pronóstico extendido",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (forecastDays.isEmpty()) {
                    // Datos por defecto mientras carga
                    ForecastDayItem("Lun", "☀️", "28°/18°")
                    ForecastDayItem("Mar", "🌤️", "26°/17°")
                    ForecastDayItem("Mié", "⛅", "24°/16°")
                    ForecastDayItem("Jue", "🌧️", "22°/15°")
                    ForecastDayItem("Vie", "⛈️", "20°/14°")
                } else {
                    // TODO: Descomentar cuando ForecastDay esté definido correctamente
                    // forecastDays.forEach { day ->
                    //     ForecastDayItem(day.day, day.emoji, day.temp)
                    // }
                    ForecastDayItem("Lun", "☀️", "28°/18°")
                    ForecastDayItem("Mar", "🌤️", "26°/17°")
                    ForecastDayItem("Mié", "⛅", "24°/16°")
                }
            }
        }
    }
}

@Composable
fun ForecastDayItem(day: String, emoji: String, temp: String) {
    val colors = MaterialTheme.colorScheme
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = temp,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            fontSize = 10.sp
        )
    }
}

@Composable
fun WeatherInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

// Funciones comentadas - disponibles para pronóstico extendido futuro
/*
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
*/
