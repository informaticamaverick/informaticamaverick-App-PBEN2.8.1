package com.example.myapplication.presentation.client

import android.Manifest
import android.app.ProgressDialog.show
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.Normalizer
import kotlin.collections.isNotEmpty

// ==================================================================================
// --- HELPERS GLOBALES ---
// ==================================================================================
// * Obtiene la actividad actual desde el contexto de Compose.
// * Útil para operaciones que requieren una instancia de Activity.
private fun android.content.Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
// * Elimina acentos de un String para facilitar comparaciones de texto.
private fun String.removeAccentsLocal(): String {
    val normalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    return pattern.replace(normalizedString, "")
}

// ==================================================================================
// --- MODELOS DE DATOS UI ---
// ==================================================================================
// Agrupación de categorías bajo una temática (Ej: Tecnología -> Software, Reparación PC)
data class SuperCategory(
    val title: String,
    val icon: String,
    val items: List<CategoryEntity>
)
// Representa las opciones de ubicación que el usuario puede seleccionar
sealed class LocationOption {
    data class Gps(val address: String, val locality: String) : LocationOption()
    data class Personal(val address: String, val number: String, val locality: String) : LocationOption()
    data class Business(val companyName: String, val branchName: String, val address: String, val number: String, val locality: String) : LocationOption()
}

// ==================================================================================
// --- PANTALLA PRINCIPAL ---
// ==================================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController, // Controlador de navegación entre pantallas
    bottomPadding: PaddingValues, // Padding inferior para respetar la NavigationBar
    profileViewModel: ProfileSharedViewModel = hiltViewModel(), // Datos del usuario logueado (inyectado con Hilt)
    weatherViewModel: WeatherViewModel = viewModel(), // Datos climáticos (ViewModel estándar)
    providerViewModel: ProviderViewModel = hiltViewModel(), // Datos de prestadores (Hilt)
    categoryViewModel: CategoryViewModel = hiltViewModel(), // Datos de categorías (Hilt)
    simulationViewModel: SimulationViewModel = hiltViewModel(), // Datos de simulación/licitaciones (Hilt)
    beViewModel: BeBrainViewModel = hiltViewModel() // El Cerebro de Be
) {
    val context = LocalContext.current
    // Inicialización manual de LocationViewModel con su Factory
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(context))
    // Suscripción a flujos de datos (States) desde los ViewModels con observancia del ciclo de vida
    val providers by providerViewModel.providers.collectAsStateWithLifecycle() // Lista de prestadores
    val favorites by providerViewModel.favorites.collectAsStateWithLifecycle() // Prestadores favoritos
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle() // Todas las categorías
    val userState by profileViewModel.userState.collectAsState() // Estado del perfil de usuario
    // Datos del clima obtenidos del WeatherViewModel
    val temperature by weatherViewModel.temperature.collectAsState()
    val weatherEmoji by weatherViewModel.weatherEmoji.collectAsState()
    val weatherDescription by weatherViewModel.weatherDescription.collectAsState()
    // Datos de ubicación obtenidos del LocationViewModel
    val cityName by locationViewModel.locationName.collectAsState()
    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()
    // Manejador de permisos de ubicación para Android
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si se otorga cualquier permiso de ubicación, disparamos la búsqueda de coordenadas
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            locationViewModel.fetchLocation()
        }
    }
    // Efecto que se dispara al iniciar: verifica permisos y pide ubicación
    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) locationViewModel.fetchLocation()
        else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    // Cuando cambian las coordenadas, actualizamos los datos del clima
    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            weatherViewModel.fetchWeather(lat = latitude!!, lon = longitude!!)
        }
    }
    // Llamada al componente visual principal pasando todos los estados recolectados
    HomeScreenContent(
        navController = navController,
        bottomPadding = bottomPadding,
        userState = userState,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        onRefreshLocation = { locationViewModel.fetchLocation() }, // Acción para refrescar GPS
        allProviders = providers,
        favoriteProviders = favorites,
        allCategories = categories,
        onToggleFavorite = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) }, // Toggle favorito en DB
        onLogout = {
            profileViewModel.logout() // Limpia sesión en ViewModel
            navController.navigate(Screen.Login.route) { popUpTo(0) } // Redirige a Login
        },
        beViewModel = beViewModel
    )
}

// ==================================================================================
// --- CONTENIDO VISUAL ---
// ==================================================================================
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    beViewModel: BeBrainViewModel
) {
    val coroutineScope = rememberCoroutineScope() // Scope para animaciones y scroll
    // Estados locales de la UI
    var showWeatherDetails by remember { mutableStateOf(false) } // Controla el panel expandido de clima
    var showFavorites by remember { mutableStateOf(false) } // Controla el panel lateral de favoritos
    var refreshTrigger by remember { mutableStateOf(0) } // Trigger para refrescar estados calculados
    var isSuperCategoryView by remember { mutableStateOf(true) } // Alterna entre vista agrupada o individual

    // --- NUEVO ESTADO PARA ORDENAMIENTO ---
    var activeSortFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    //var showSortMenu by remember { mutableStateOf(false) }

    // Estados de scroll para las grillas de categorías
    val superCatGridState = rememberLazyStaggeredGridState()
    val individualCatGridState = rememberLazyGridState()
    var selectedSuperCategory by remember { mutableStateOf<SuperCategory?>(null) } // Supercategoría seleccionada para ver detalles
    
    // Agrupación reactiva de categorías por su campo "superCategory" y aplicación de ORDENAMIENTO
    val superCategories = remember(allCategories, refreshTrigger, activeSortFilters) {
        val grouped = allCategories.groupBy { it.superCategory }
            .map { entry ->
                val dbIcon = entry.value.firstOrNull()?.superCategoryIcon ?: "📂"
                SuperCategory(title = entry.key, icon = dbIcon, items = entry.value)
            }
        
        // Aplicar ordenamiento a las supercategorías (por nombre del grupo)
        when {
            activeSortFilters.contains("sort_nombre_asc") -> grouped.sortedBy { it.title }
            activeSortFilters.contains("sort_nombre_desc") -> grouped.sortedByDescending { it.title }
            else -> grouped.shuffled()
        }
    }

    // Generación de banners usando la nueva lógica que incluye prestadores y anuncios de Google
    val bannerItems = remember(allCategories, allProviders, refreshTrigger) {
        generateEnrichedBannerItems(allCategories, allProviders)
    }

    Scaffold(containerColor = Color(0xFF0A0E14)) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // --- CAPA 1: SCROLL DE CONTENIDO PRINCIPAL ---
            Column(modifier = Modifier.fillMaxSize()) {
                // Espaciador para no quedar debajo de la TopBar fija
                Spacer(modifier = Modifier.height(145.dp))
                // Llamada al nuevo Carrusel Premium desde ComponentesReutilizables.kt
                if (bannerItems.isNotEmpty()) {
                    PremiumLensCarousel(
                        items = bannerItems,
                        onSettingsClick = { /* Opcional */ },
                        onItemClick = { banner ->
                            // Navegación inteligente según el tipo de banner
                            if (banner.provider != null) {
                                navController.navigate("perfil_prestador/${banner.provider.id}")
                            } else if (banner.originalCategory != null) {
                                navController.navigate("result_busqueda/${banner.originalCategory.name}")
                            }
                        }
                    )
                }

                // Cabecera de la sección de exploración
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EXPLORA SERVICIOS", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        if (activeSortFilters.isNotEmpty()) {
                            val currentSort = activeSortFilters.first()
                            val sortLabel = when(currentSort) {
                                "sort_nombre_asc" -> "Nombre (A-Z)"
                                "sort_nombre_desc" -> "Nombre (Z-A)"
                                "view_bento" -> "Vista por Grupos"
                                "view_grid" -> "Vista en Grilla"
                                else -> "Personalizado"
                            }
                            Text("Filtro: $sortLabel", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // --- NUEVO MENU DE ORDENAMIENTO INTEGRADO EN CABECERA ---
                    MenuOrdenamiento(
                        activeFilters = activeSortFilters,
                        onAction = { newFilter ->
                            // Lógica para cambiar el modo de vista reactivamente
                            if (newFilter == "view_bento") {
                                isSuperCategoryView = true
                            } else if (newFilter == "view_grid") {
                                isSuperCategoryView = false
                            }
                            activeSortFilters = if (newFilter.isEmpty()) emptySet() else setOf(newFilter)
                        },
                        onApply = { /* El menú se cierra solo */ },
                        onClearFilters = {
                            activeSortFilters = emptySet()
                            isSuperCategoryView = true // Vuelve a la vista por defecto
                        },
                        showNombre = true,
                        showViewModes = true
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))

                // Grilla de Categorías
                if (isSuperCategoryView) {
                    LazyVerticalStaggeredGrid(
                        state = superCatGridState, columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = paddingValues.calculateBottomPadding() + 100.dp),
                        verticalItemSpacing = 16.dp, horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = superCategories, key = { it.title }) { superCat ->
                            BentoSuperCategoryCard(superCategory = superCat, emoji = superCat.icon, height = 180.dp, onClick = { selectedSuperCategory = superCat })
                        }
                    }
                } else {
                    // Vista Grilla Individual: También aplica el ordenamiento
                    val sortedIndividual = remember(allCategories, activeSortFilters) {
                        when {
                            activeSortFilters.contains("sort_nombre_asc") -> allCategories.sortedBy { it.name }
                            activeSortFilters.contains("sort_nombre_desc") -> allCategories.sortedByDescending { it.name }
                            else -> allCategories.shuffled()
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = paddingValues.calculateBottomPadding() + 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = sortedIndividual, key = { it.name }) { category ->
                            CompactCategoryCard(item = category, onClick = { navController.navigate("result_busqueda/${category.name}") })
                        }
                    }
                }
            }
            // --- CAPA 2: HEADER FIJO (TopBar) ---
            Box(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(IntrinsicSize.Min).zIndex(10f)) {
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.65f)))
                Box(modifier = Modifier.statusBarsPadding()) {
                    // Estado local para la ubicación seleccionada visualmente
                    var currentLocationState by remember {
                        mutableStateOf<LocationOption>(LocationOption.Gps(address = cityName, locality = "Ubicación Actual"))
                    }
                    LaunchedEffect(cityName) {
                        if (cityName.isNotEmpty()) {
                            currentLocationState = LocationOption.Gps(address = cityName, locality = "Ubicación Actual")
                        }
                    }
                    // Cabecera con Widgets de Clima, Selector de Ubicación y Perfil
                    TopHeaderSection(
                        navController = navController, user = userState, temperature = temperature, weatherEmoji = weatherEmoji,
                        weatherDescription = weatherDescription, cityName = cityName, currentLocationState = currentLocationState,
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
            // --- CAPA 3: PANELES EMERGENTES (Z-Index alto) ---
            // Panel de Clima Expandido
            if (showWeatherDetails) {
                Box(modifier = Modifier.fillMaxSize().zIndex(15f).background(Color.Black.copy(alpha = 0.5f)).clickable { showWeatherDetails = false })
            }
            AnimatedVisibility(
                visible = showWeatherDetails, enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(), exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 60.dp).zIndex(20f)
            ) {
                WeatherExpandedCard(temperature = temperature, weatherEmoji = weatherEmoji, weatherDescription = weatherDescription, cityName = cityName, forecastDays = emptyList())
            }
            // Panel Lateral de Favoritos
            if (showFavorites) {
                Box(modifier = Modifier.fillMaxSize().zIndex(11f).background(Color.Black.copy(alpha = 0.65f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showFavorites = false })
            }
            AnimatedVisibility(visible = showFavorites, enter = slideInHorizontally(initialOffsetX = { it }), exit = slideOutHorizontally(targetOffsetX = { it }), modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)) {
                FavoritesPanel(navController = navController, favorites = favoriteProviders, onClose = { showFavorites = false }, onToggleFavorite = onToggleFavorite)
            }
            // Detalle de Supercategoría Seleccionada (Bottom Sheet persistente)
            if (selectedSuperCategory != null) {
                Box(modifier = Modifier.fillMaxSize().zIndex(180f).background(Color.Black.copy(alpha = 0.75f)).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    selectedSuperCategory = null
                })
            }
            AnimatedVisibility(
                visible = selectedSuperCategory != null,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(dampingRatio = 0.85f)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).zIndex(185f).padding(bottom = bottomPadding.calculateBottomPadding())
            ) {
                selectedSuperCategory?.let { superCat ->
                    SuperCategoryDetailsPanel(
                        superCategory = superCat,
                        beViewModel = beViewModel,
                        onClose = { selectedSuperCategory = null },
                        onCategoryClick = { categoryName ->
                            selectedSuperCategory = null
                            navController.navigate("result_busqueda/$categoryName")
                        }
                    )
                }
            }
        }
    }
}
// ==================================================================================
// --- SUB-COMPONENTES DE UI ---
// ==================================================================================
//* Tarjeta estilo Bento para las supercategorías.
//* Muestra iconos de categorías internas difuminados en el fondo.
@Composable
fun BentoSuperCategoryCard(superCategory: SuperCategory, emoji: String, height: Dp, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(height).clickable(onClick = onClick).shadow(12.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))
            Box(modifier = Modifier.fillMaxSize().blur(radius = 20.dp).alpha(0.35f)) {
                LazyVerticalGrid(GridCells.Fixed(2), userScrollEnabled = false) {
                    items(items = superCategory.items, key = { it.name }) { item ->
                        Text(item.icon, fontSize = 55.sp, modifier = Modifier.padding(8.dp).alpha(0.5f))
                    }
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(superCategory.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("${superCategory.items.size} servicios", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            }
            Text(emoji, fontSize = 44.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).alpha(0.85f))
        }
    }
}
// * Panel que despliega todas las categorías pertenecientes a un grupo.
/**
 * Panel que despliega todas las categorías pertenecientes a un grupo.
 * Se comunica con BeBrainViewModel para posicionar al asistente en modo búsqueda.
 */
@Composable
fun SuperCategoryDetailsPanel(
    superCategory: SuperCategory,
    beViewModel: BeBrainViewModel,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    // 🔥 ESTADO LOCAL PARA FILTROS: Permite filtrar dentro del panel (Productos/Servicios)
    var activeFilters by remember { mutableStateOf(setOf<String>()) }
    // 🔥 Capturamos la consulta de búsqueda de Be en tiempo real
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()

    // 🔥 Ciclo de Vida: Gestionamos la visibilidad de Be y la barra de navegación automáticamente
    DisposableEffect(Unit) {
        // Al aparecer el panel: Ocultamos barra y activamos búsqueda de Be
        beViewModel.setUIBlocked(true)
        beViewModel.setSearchActive(true)
        beViewModel.setBottomBarVisible(false)
        
        onDispose {
            // Al desaparecer el panel (cierre o navegación): Restauramos todo a la normalidad
            beViewModel.setUIBlocked(false)
            beViewModel.setSearchActive(false)
            beViewModel.setBottomBarVisible(true)
            // 🔥 Opcional: Limpiar búsqueda al salir
            beViewModel.updateSearchQuery("")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.78f),
        color = Color(0xFF0A0E14),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation = 16.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Encabezado del Panel con Título y Filtros ---
                // 🔥 MODIFICACIÓN: Se agrupa el título con el menú de filtros en un Row para alineación centrada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = superCategory.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // 🔥 MENÚ DE FILTROS: Integrado a la derecha del título con filtros de Productos y Servicios habilitados
                    MenuFiltros(
                        activeFilters = activeFilters,
                        dynamicCategories = emptyList(), // Categorías dinámicas no requeridas aquí
                        onAction = { filterId ->
                            activeFilters = if (activeFilters.contains(filterId)) activeFilters - filterId else activeFilters + filterId
                        },
                        onApply = { /* El popup se cierra al hacer click fuera o en Check */ },
                        onClearFilters = { activeFilters = emptySet() },
                        showProductService = true // 🔥 Habilitamos la sección de Productos y Servicios
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                // --- Grilla de Categorías ---
                // 🔥 MODIFICACIÓN: Lógica de filtrado reactivo combinando filtros y búsqueda de Be
                val filteredItems = remember(superCategory.items, activeFilters, searchQuery) {
                    superCategory.items.filter { cat ->
                        var matchesFilters = true
                        // Filtro de Productos (Placeholder hasta que se activen los campos en Room)
                        // if (activeFilters.contains("filter_productos") && !cat.isNewProduct) matchesFilters = false
                        // Filtro de Servicios (Placeholder hasta que se activen los campos en Room)
                        // if (activeFilters.contains("filter_servicios") && !cat.isNewService) matchesFilters = false

                        // 🔥 Filtro por búsqueda de Be (ignorando acentos y mayúsculas)
                        val matchesSearch = if (searchQuery.isEmpty()) true 
                        else cat.name.removeAccentsLocal().contains(searchQuery.removeAccentsLocal(), ignoreCase = true)
                        
                        matchesFilters && matchesSearch
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = filteredItems, key = { it.name }) { category ->
                        CompactCategoryCard(
                            item = category,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }

                    // Item final para dar espacio y que Be (en posición de búsqueda)
                    // o la barra de herramientas no tapen el contenido.
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            }

            // --- Botón de Cierre Premium ---
            // 🔥 MODIFICACIÓN: Botón flotante en el corner superior derecho siguiendo el estilo Cyber
            Surface(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp)
                    .zIndex(10f)
                    .shadow(8.dp, CircleShape, spotColor = Color.Red),
                shape = CircleShape,
                color = Color(0xFF0A0E14),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// * Panel lateral para mostrar los proveedores favoritos del usuario.
@Composable
fun FavoritesPanel(navController: NavHostController, favorites: List<Provider>, onClose: () -> Unit, onToggleFavorite: (String, Boolean) -> Unit) {
    Surface(modifier = Modifier.fillMaxHeight().width(320.dp), color = Color(0xFF0A0E14), tonalElevation = 16.dp, shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }
            HorizontalDivider(color = Color.White.copy(0.1f))
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (favorites.isEmpty()) {
                    item { Text("No tienes favoritos guardados.", modifier = Modifier.fillMaxWidth().padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center) }
                } else {
                    items(items = favorites, key = { it.id }) { provider ->
                        PrestadorCard(provider = provider, onClick = { navController.navigate("perfil_prestador/${provider.id}") }, onToggleFavorite = { id, isFav -> onToggleFavorite(id, isFav) }, showPreviews = false, viewMode = "Compacta", onChat = { navController.navigate("chat/${provider.id}") })
                    }
                }
            }
        }
    }
}
// * Sección superior que organiza los widgets tácticos (Clima, Location, Perfil).
@Composable
fun TopHeaderSection(
    navController: NavHostController, user: UserEntity?, temperature: String, weatherEmoji: String, weatherDescription: String,
    cityName: String, currentLocationState: LocationOption, onWeatherClick: () -> Unit, onRefreshLocation: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit, onLogout: () -> Unit
) {
    val cardGradientBrush = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(0.8f)) { WeatherWidget(temperature, weatherEmoji, cityName, onWeatherClick, cardGradientBrush) }
        Box(Modifier.weight(1.6f)) {
            LocationSelector(user = user, currentLocation = currentLocationState, onRefresh = onRefreshLocation, onLocationSelected = onLocationSelected, brush = cardGradientBrush)
        }
        Box(Modifier.weight(0.8f)) { ProfileSection(user, navController, onAddressSelected = onLocationSelected, onLogout, cardGradientBrush) }
    }
}
// * Widget compacto de clima.
@Composable
fun WeatherWidget(temp: String, emoji: String, city: String, onClick: () -> Unit, brush: Brush) {
    Card(modifier = Modifier.clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.Transparent), border = BorderStroke(1.dp, Color.White.copy(0.1f))) {
        Box(Modifier.fillMaxSize().background(brush).padding(4.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(emoji, fontSize = 22.sp)
                Text(temp, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(city, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(0.7f))
            }
        }
    }
}
// * Selector de ubicación con menú desplegable que incluye direcciones personales y de empresa.
@Composable
fun LocationSelector(
    user: UserEntity?, currentLocation: LocationOption, onRefresh: () -> Unit, onLocationSelected: (LocationOption) -> Unit, brush: Brush
) {
    var expanded by remember { mutableStateOf(false) }
    val (linea1, linea2, linea3) = when (currentLocation) {
        is LocationOption.Gps -> Triple("UBICACIÓN ACTUAL", currentLocation.address, "GPS Activo")
        is LocationOption.Personal -> Triple("MI CASA / PERSONAL", "${currentLocation.address} ${currentLocation.number}", currentLocation.locality)
        is LocationOption.Business -> Triple(currentLocation.companyName.uppercase(), currentLocation.branchName, "${currentLocation.address} ${currentLocation.number}")
    }
    Box(modifier = Modifier.fillMaxWidth().padding(end = 6.dp)) {
        Card(modifier = Modifier.fillMaxWidth().clickable { expanded = true }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.Transparent), border = BorderStroke(1.dp, Color.White.copy(0.15f))) {
            Box(modifier = Modifier.background(brush).padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 46.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = linea1, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = linea2, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = linea3, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Surface(modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-14).dp), shape = CircleShape, color = Color(0xFF1E1E1E), border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.6f)), shadowElevation = 6.dp) {
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) { Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Actualizar GPS", tint = Color(0xFF22D3EE), modifier = Modifier.size(20.dp)) }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xFF0D1117).copy(alpha = 0.95f)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))) {
            DropdownMenuItem(text = { Text("Usar GPS Actual", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold) }, onClick = { onRefresh(); expanded = false }, leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = Color(0xFF22D3EE)) })
            HorizontalDivider(color = Color.White.copy(0.1f))
            user?.personalAddresses?.forEach { addr -> DropdownMenuItem(text = { Column { Text("${addr.calle} ${addr.numero}", color = Color.White); Text(addr.localidad, fontSize = 10.sp, color = Color.Gray) } }, onClick = { onLocationSelected(LocationOption.Personal(addr.calle, addr.numero, addr.localidad)); expanded = false }) }
            if (user?.companies?.isNotEmpty() == true) {
                HorizontalDivider(color = Color.White.copy(0.1f))
                user.companies.forEach { company ->
                    company.branches.forEach { branch ->
                        DropdownMenuItem(text = { Column { Text("${company.name} - ${branch.name}", color = Color.White, fontWeight = FontWeight.Bold); Text("${branch.address.calle} ${branch.address.numero}", fontSize = 11.sp, color = Color.Gray) } }, onClick = { onLocationSelected(LocationOption.Business(companyName = company.name, branchName = branch.name, address = branch.address.calle, number = branch.address.numero, locality = branch.address.localidad)); expanded = false })
                    }
                }
            }
        }
    }
}
// * Sección de perfil que muestra el avatar del usuario y abre el panel de gestión de cuenta. SI CAUSA ERROR EN PERFIL SELECTION , SE QUITO PRIVATE FUN
@Composable
fun ProfileSection(
    user: UserEntity?, navController: NavHostController, onAddressSelected: (LocationOption) -> Unit, onLogout: () -> Unit, brush: Brush
) {
    var showPopup by remember { mutableStateOf(false) }
    val displayName = remember(user) { user?.name?.trim()?.split(" ")?.firstOrNull()?.uppercase() ?: "PERFIL" }

    Box(modifier = Modifier.fillMaxSize().background(brush, RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).clickable { showPopup = true }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(4.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
                if (user?.photoUrl != null) { AsyncImage(model = user.photoUrl, contentDescription = "Avatar", modifier = Modifier.matchParentSize().clip(CircleShape).border(1.0.dp, Color(0xFF22D3EE), CircleShape), contentScale = ContentScale.Crop) }
                else { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = displayName, style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    if (showPopup && user != null) {
        Dialog(onDismissRequest = { showPopup = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showPopup = false }) {
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 70.dp, end = 12.dp).width(340.dp).clickable(enabled = false) {}) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = animateIn,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn() + slideInVertically { -40 },
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        UserProfilePopup(user = user, onClose = { showPopup = false }, onLogout = { showPopup = false; onLogout() }, onAddressSelected = { onAddressSelected(it); showPopup = false }, onProfileClick = { showPopup = false; navController.navigate(Screen.PerfilCliente.route) })
                    }
                }
            }
        }
    }
}
// * Popup de perfil con estilo Cyberpunk que muestra datos del usuario y árbol de direcciones.
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
                            company.branches.forEach { branch -> CyberTreeLeaf(icon = Icons.Default.Storefront, title = branch.name, subtitle = "${branch.address.calle} ${branch.address.numero}", accentColor = cyberPurple, onClick = { onAddressSelected(LocationOption.Business(companyName = company.name, branchName = branch.name, address = branch.address.calle, number = branch.address.numero, locality = branch.address.localidad)); onClose() }) }
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
// * Carpeta expandible en el árbol de perfiles.
@Composable
fun CyberTreeDirectory(title: String, icon: ImageVector, accentColor: Color, isExpanded: Boolean, isNested: Boolean = false, onToggle: () -> Unit, content: @Composable () -> Unit) {
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
// * Hoja (ítem final) en el árbol de perfiles.
@Composable
fun CyberTreeLeaf(icon: ImageVector, title: String, subtitle: String, accentColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 6.dp).drawWithCache { onDrawWithContent { drawLine(color = accentColor.copy(alpha = 0.2f), start = Offset(0f, size.height / 2), end = Offset(15.dp.toPx(), size.height / 2), strokeWidth = 1.dp.toPx()); drawContent() } }.padding(start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(16.dp))
        Column { Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(text = subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}
//---------------------Datos Falsos HAY QUE REMPLAZAR CON LOS BANNER DE PUBLICIDAD REAL-----------
// **************************** Lógica para filtrar y generar ítems de banner basados en los datos reales de la App.
 fun generateEnrichedBannerItems(categories: List<CategoryEntity>, providers: List<Provider>): List<AccordionBanner> {
    val bannerList = mutableListOf<AccordionBanner>()
    // 1. Agregar Categorías Nuevas (Máximo 5)
    categories.filter { it.isNew }.take(5).forEach { cat ->
        bannerList.add(AccordionBanner(
            id = "cat_${cat.name}", title = cat.name, subtitle = "🚀 EXPLORA LO NUEVO", icon = cat.icon, color = Color(0xFF2197F5), type = BannerType.NEW_CATEGORY, originalCategory = cat
        ))
    }
    // 2. Agregar Promociones de Prestadores Suscriptos (Máximo 5) 👻
    providers.filter { it.isSubscribed }.take(5).forEach { provider ->
        bannerList.add(AccordionBanner(
            id = "promo_${provider.uid}", title = "Oferta Especial", subtitle = "Servicio destacado de ${provider.displayName}", icon = "🔥", color = Color(0xFFE91E63), type = BannerType.PROMO, discount = (15..45).random(), provider = provider
        ))
    }
    // 3. Inyectar ítem base para Publicidad de Google (el carrusel Premium se encargará de repetirlo) 👻
    bannerList.add(AccordionBanner(
        id = "ad_google_phantom", title = "Anuncio Patrocinado", subtitle = "Descubre más en Google Ads", icon = "🌐", color = Color.DarkGray, type = BannerType.GOOGLE_AD
    ))
    return bannerList.shuffled() // Mezclamos el contenido para que cada entrada sea dinámica
}
//----------------------------FIN DE LA CARGA DE BANNER FALSOS-------------
// * Card expandible que muestra el pronóstico y detalles adicionales del clima.
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

// ==================================================================================
// --- PREVIEW ---
// ==================================================================================
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
            cityName = "Buenos Aires",
            onRefreshLocation = {},
            allProviders = emptyList(),
            favoriteProviders = emptyList(),
            allCategories = emptyList(),
            onToggleFavorite = { _, _ -> },
            onLogout = {},
            beViewModel = viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BentoSuperCategoryCardPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Limpieza", icon = "🧹", color = 0xFFFAD2E1L, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        ),
        CategoryEntity(
            name = "Plomería", icon = "🪠", color = 0xFFBCAAA4L, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        ),
        CategoryEntity(
            name = "Electricidad", icon = "⚡", color = 0xFFFFF59DL, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        ),
        CategoryEntity(
            name = "Carpintería", icon = "🪚", color = 0xFFD7CCC8L, superCategory = "Hogar",
            superCategoryIcon = "🏠", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false
        )
    )
    val sampleSuperCat = SuperCategory(
        title = "Hogar y Construcción",
        icon = "🏠",
        items = sampleCategories
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).width(300.dp)) {
            BentoSuperCategoryCard(
                superCategory = sampleSuperCat,
                emoji = sampleSuperCat.icon,
                height = 200.dp,
                onClick = {}
            )
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SuperCategoryDetailsPanelPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Plomería",
            icon = "🚰",
            color = 0xFF2196F3L,
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            imageUrl = null,
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Electricidad",
            icon = "⚡",
            color = 0xFFFFC107L,
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            imageUrl = null,
            isNew = true,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Carpintería",
            icon = "🪚",
            color = 0xFF795548L,
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            imageUrl = null,
            isNew = false,
            isNewPrestador = true,
            isAd = false
        )
    )

    val sampleSuperCategory = SuperCategory(
        title = "Servicios del Hogar",
        icon = "🏠",
        items = sampleCategories
    )

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SuperCategoryDetailsPanel(
                superCategory = sampleSuperCategory,
                beViewModel = viewModel(),
                onClose = {},
               onCategoryClick = {}
            )
        }
    }
}
