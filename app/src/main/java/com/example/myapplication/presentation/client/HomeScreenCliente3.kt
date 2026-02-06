//package com.example.myapplication.ui.screens
package com.example.myapplication.presentation.client

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
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import com.example.myapplication.data.model.OpenMeteoResponse
import com.example.myapplication.R
import com.example.myapplication.presentation.client.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.launch
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

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
    val items: List<CategoryEntity>
)
sealed class LocationOption {
    // 1. Modo GPS
    data class Gps(val address: String, val locality: String) : LocationOption()

    // 2. Modo Dirección Personal
    data class Personal(val address: String, val number: String, val locality: String) : LocationOption()

    // 3. Modo Empresa/Sucursal
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
    weatherViewModel: WeatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // 🔥 LocationViewModel con su Factory real
    val locationViewModel: LocationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
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
        if (permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            locationViewModel.fetchLocation()
        }
    }
    
    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission) locationViewModel.fetchLocation()
        else locationPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
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
        }
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
    onLogout: () -> Unit
) {
    val isSystemInDarkMode = isSystemInDarkTheme()
    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearchActive by remember { mutableStateOf(false) }
    var showWeatherDetails by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
// Estado de la tarjeta de ubicación
    var currentLocationState by remember {
        mutableStateOf<LocationOption>(LocationOption.Gps(address = cityName, locality = ""))
    }

    // --- AGREGA O CORRIGE ESTO ---
    // Esto es lo que "escucha" cuando el GPS termina de buscar
    LaunchedEffect(cityName) {
        // Solo actualizamos si la ciudad tiene texto y estamos en modo GPS
        if (cityName.isNotEmpty() && currentLocationState is LocationOption.Gps) {
            currentLocationState = LocationOption.Gps(
                address = cityName, // Aquí llega la dirección real
                locality = "Ubicación Actual"
            )
        }
    }



    var manualSelectedAddress by remember { mutableStateOf<String?>(null) }

    var selectedSuperCategory by remember { mutableStateOf<SuperCategory?>(null) }
// Agrupación por Súper Categorías
    val superCategories = remember(allCategories, refreshTrigger) {
        allCategories.groupBy { it.superCategory }
            .map { SuperCategory(it.key, it.value.shuffled()) }
            .shuffled()
    }
// 🔥 GENERACIÓN DE CONTENIDO PARA EL NUEVO CARRUSEL
    val bannerItems = remember(allCategories, allProviders, refreshTrigger) {
        generateEnterpriseBannerItems(allCategories, allProviders)
        //generateRealBannerContent(allCategories, allProviders)
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

            // --- CONTENIDO PRINCIPAL: GRID ---
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(145.dp)) // [RESTAURADO] Espaciado para TopBar
// 🔥          IMPLEMENTACIÓN DEL NUEVO CARRUSEL PREMIUM
                if (bannerItems.isNotEmpty()) {
                    PremiumLensCarousel(
                        items = bannerItems,
                        onSettingsClick = {
                            // Aquí puedes abrir un BottomSheet de configuración a futuro
                        },
                        onItemClick = { banner ->
                            // Navegación inteligente según el origen
                            if (banner.originalCategory != null) {
                                navController.navigate("result_busqueda/${banner.originalCategory.name}")
                            } else if (banner.type == BannerType.GOOGLE_AD) {
                                // Lógica para clics en anuncios externos
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }


              /**  if (bannerItems.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        AutoPlayingBannerSection(
                            bannerItems = bannerItems,
                            onCategoryClick = { navController.navigate("result_busqueda/$it") },
                            navController = navController
                        )
                    }
                }
                **/


                // Grid Staggered para Súper Categorías
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = paddingValues.calculateBottomPadding() + 100.dp),
                    verticalItemSpacing = 16.dp, 
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(superCategories) { superCat ->
                        val height = when {
                            superCat.items.size <= 3 -> 150.dp
                            superCat.items.size in 4..7 -> 190.dp
                            else -> 240.dp
                        }
                        BentoSuperCategoryCard(
                            superCategory = superCat,
                            emoji = getCategoryEmoji(superCat.title),
                            height = height,
                            onClick = { selectedSuperCategory = superCat }
                        )
                    }
                }
            }

            // --- CAPA HEADER (Glassmorphism) ---
            Box(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.65f)))
                Box(modifier = Modifier.statusBarsPadding()) {
                    TopHeaderSection(
                        navController = navController,
                        user = userState,
                        temperature = temperature,
                        weatherEmoji = weatherEmoji,
                        cityName = cityName,
                        onWeatherClick = { showWeatherDetails = !showWeatherDetails },
                        onRefreshLocation = {
                            // 1. Poner estado visual en "Cargando/GPS"
                            currentLocationState = LocationOption.Gps(address = "Actualizando...", locality = "")
                            // 2. Llamar al ViewModel real
                            onRefreshLocation()
                        },
                        //onRefreshLocation = { manualSelectedAddress = null; onRefreshLocation() },
                        onLogout = onLogout,
                        currentLocationState = currentLocationState, // <--- La variable 'remember' que creamos antes
                        onLocationSelected = { nuevaSeleccion ->
                            currentLocationState = nuevaSeleccion // Actualizamos el estado
                        },

                        //onAddressSelected = { manualSelectedAddress = it },
                        //gpsLocation = cityName,
                        //manualSelection = manualSelectedAddress
                    )
                }
            }

            // --- Popup de Subcategorías ---
            if (selectedSuperCategory != null) {
                FolderExpandedView(
                    superCategoryName = selectedSuperCategory!!.title,
                    items = selectedSuperCategory!!.items,
                    onDismiss = { selectedSuperCategory = null },
                    onCategoryClick = { categoryName ->
                        selectedSuperCategory = null
                        navController.navigate("result_busqueda/$categoryName")
                    }
                )
            }

            // --- [RESTAURADO] PANEL EXPANDIDO DEL CLIMA CON SCRIM ---
            if (showWeatherDetails) {
                Box(
                    modifier = Modifier.fillMaxSize().zIndex(1.5f).background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showWeatherDetails = false }
                )
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

            // --- [RESTAURADO] SCRIM DE BÚSQUEDA ---
            if (isSearchActive) {
                Box(
                    modifier = Modifier.fillMaxSize().zIndex(8f).background(Color.Black.copy(alpha = 0.5f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { closeSearch() }
                )
            }

            // --- [RESTAURADO] BARRA DE BÚSQUEDA GEMINI ---
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
                        GeminiTopSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            placeholderText = "Buscar servicios...",
                            focusRequester = remember { FocusRequester() }
                        )
                    }
                    val rainbowBrush = geminiGradientEffect()
                    Surface(
                        modifier = Modifier.size(56.dp).clickable(onClick = closeSearch),
                        shape = CircleShape, color = Color(0xFF121212), border = BorderStroke(2.5.dp, rainbowBrush), shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }
// --- [NUEVO] PANEL DE RESULTADOS DE BÚSQUEDA ---
            AnimatedVisibility(
                visible = isSearchActive && searchQuery.isNotEmpty(),
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
                modifier = Modifier.zIndex(9f).align(Alignment.TopCenter).padding(top = 130.dp)
            ) {
                SearchResultsPanel(
                    searchQuery = searchQuery,
                    allCategories = allCategories,
                    onCategoryClick = { categoryName ->
                        closeSearch()
                        navController.navigate("result_busqueda/$categoryName")
                    }
                )
            }




            // --- [RESTAURADO] PANEL DE FAVORITOS CON SCRIM ---
            if (showFavorites) {
                Box(
                    modifier = Modifier.fillMaxSize().zIndex(11f).background(Color.Black.copy(alpha = 0.65f))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showFavorites = false }
                )
            }

            AnimatedVisibility(
                visible = showFavorites,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)
            ) {
                FavoritesPanel(
                    navController = navController,
                    favorites = favoriteProviders,
                    onClose = { showFavorites = false },
                    onToggleFavorite = onToggleFavorite
                )
            }

            // --- FAB GEMINI DIVIDIDO ---
            val fabSurfaceColor = if (isSystemInDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.surface
            GeminiFABWithScrim(
                bottomPadding = bottomPadding,
                showScrim = !isFabMenuExpanded
            ) {
                MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = fabSurfaceColor)) {
                    GeminiSplitFAB(
                        isExpanded = isFabMenuExpanded,
                        isSearchActive = isSearchActive,
                        isSecondaryPanelVisible = showFavorites,
                        onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                        onActivateSearch = { isSearchActive = true },
                        onCloseSearch = closeSearch,
                        onCloseSecondaryPanel = { showFavorites = false },
                        secondaryActions = {
                            SmallActionFab(icon = Icons.Default.Gavel, label = "Licitar", iconColor = Color(0xFF4285F4), onClick = { navController.navigate(Screen.CrearLicitacion.route) })
                            SmallActionFab(icon = Icons.Default.Bolt, label = "Rápido", iconColor = Color(0xFFFBC02D), onClick = { })
                            SmallActionFab(icon = Icons.Default.Favorite, label = "Favs", iconColor = Color(0xFFE91E63), onClick = { showFavorites = true; isFabMenuExpanded = false })
                        },
                        expandedTools = {
                            SmallFabTool(label = "Actualizar", icon = Icons.Default.Refresh, onClick = { isFabMenuExpanded = false; refreshTrigger++ })
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
fun BentoSuperCategoryCard(superCategory: SuperCategory, emoji: String, height: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
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
fun CategoryCard(item: CategoryEntity, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(item.color)),
        modifier = Modifier.fillMaxSize().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = item.icon, fontSize = 110.sp, modifier = Modifier.align(Alignment.Center).blur(16.dp).alpha(0.4f))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))
            Text(item.icon, fontSize = 60.sp, modifier = Modifier.align(Alignment.Center).graphicsLayer { shadowElevation = 12f })
            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), fontSize = 14.sp, maxLines = 2)
            if (item.isNew) Surface(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(topStart = 24.dp, bottomEnd = 8.dp)) {
                Text("NUEVO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
        }
    }
}

@Composable
fun FavoritesPanel(navController: NavHostController, favorites: List<Provider>, onClose: () -> Unit, onToggleFavorite: (String, Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(320.dp), color = Color(0xFF121212), tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
            HorizontalDivider(color = Color.White.copy(0.1f))
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (favorites.isEmpty()) {
                    item { Text("No tienes favoritos guardados.", modifier = Modifier.padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center) }
                } else {
                    items(favorites, key = { it.id }) { provider ->
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.05f)).clickable { navController.navigate("perfil_prestador/${provider.id}") }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = provider.photoUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape), contentScale = ContentScale.Crop, error = painterResource(R.drawable.iconapp))
                            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
                                Text(provider.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(provider.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { onToggleFavorite(provider.id, provider.isFavorite) }) {
                                Icon(if (provider.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (provider.isFavorite) Color.Red else Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: COMPONENTES DEL TOP BAR (RESTAURADOS) ---
// ==================================================================================

@Composable
fun TopHeaderSection(
    navController: NavHostController,
    user: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    cityName: String,
    // CAMBIO 1: Ahora recibimos el estado completo, no solo strings sueltos
    currentLocationState: LocationOption,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    // CAMBIO 2: El callback de salida también debe devolver el objeto completo
    onLocationSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit
) {
    val cardGradientBrush = Brush.verticalGradient(listOf(Color.White.copy(0.15f), Color.White.copy(0.03f)))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. WIDGET CLIMA
        Box(Modifier.weight(0.8f)) {
            WeatherWidget(temperature, weatherEmoji, cityName, onWeatherClick, cardGradientBrush)
        }

        // 2. SELECTOR DE UBICACIÓN (Aquí estaba el error)
        Box(Modifier.weight(1.6f)) {
            LocationSelector(
                user = user,
                currentLocation = currentLocationState, // Pasamos el objeto LocationOption
                onRefresh = onRefreshLocation,
                onLocationSelected = onLocationSelected, // Pasamos el callback directo
                brush = cardGradientBrush
            )
        }

        // 3. PERFIL
        Box(Modifier.weight(0.8f)) {
            ProfileSection(
                user,
                navController,
                // ADAPTADOR: Como el perfil antiguo devuelve solo String, lo convertimos temporalmente
                // para que sea compatible con el nuevo sistema.
                onAddressSelected = onLocationSelected,
                onLogout,
                cardGradientBrush
            )
        }
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

    // --- 1. Lógica de Textos Reorganizada ---
    // Estructura: Título (Pequeño) -> Dirección (Grande) -> Localidad (Pequeño)
    val (linea1, linea2, linea3) = when (currentLocation) {
        is LocationOption.Gps -> Triple(
            "UBICACIÓN ACTUAL",          // Línea 1 (Arriba)
            currentLocation.address,     // Línea 2 (Medio - Grande)
            "GPS Activo"                 // Línea 3 (Abajo)
        )
        is LocationOption.Personal -> Triple(
            "MI CASA / PERSONAL",
            "${currentLocation.address} ${currentLocation.number}",
            currentLocation.locality
        )
        is LocationOption.Business -> Triple(
            currentLocation.companyName.uppercase(), // Línea 1: NOMBRE EMPRESA
            currentLocation.branchName,              // Línea 2: SUCURSAL
            "${currentLocation.address} ${currentLocation.number}" // Línea 3: Dirección
        )
    }

    // --- 2. Layout Principal ---
    // IMPORTANTE: Quitamos el padding top del Box para que NO agrande la fila de tarjetas
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Solo dejamos un padding derecho pequeño para que el icono no se corte si la pantalla termina justo ahí
            .padding(end = 6.dp)
    ) {

        // --- TARJETA DE FONDO ---
        Card(
            onClick = { expanded = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(brush)
                    // Ajustamos padding interno:
                    // top/bottom equilibrados para altura normal.
                    // end = 40.dp para que el texto no se meta debajo del icono.
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 46.dp)
            ) {
                // Reemplaza el Column anterior por este:
                Column {
                    // LÍNEA 1: Título o Nombre de Empresa (Color Cyan)
                    Text(
                        text = linea1,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF22D3EE), // Cyan
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // LÍNEA 2: Dirección Principal o SUCURSAL (Blanco, Negrita)
                    Text(
                        text = linea2,
                        // Usamos titleSmall para que no sea GIGANTE pero se lea bien
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // LÍNEA 3: Detalle o Dirección física de la sucursal (Gris, más chico)
                    Text(
                        text = linea3,
                        style = MaterialTheme.typography.bodySmall, // Letra chica para que entre la dirección
                        fontSize = 10.sp, // Forzamos un poco más chico si la dirección es larga
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
        }

        // --- BOTÓN GPS (Flotante tipo Clip) ---
        // Lo ponemos en un Surface para que tenga fondo y tape el borde de la tarjeta si es necesario
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd) // Alineado a la derecha arriba
                // MAGIA AQUÍ:
                // x = 8.dp -> Lo saca un poquito hacia la derecha (fuera de la tarjeta)
                // y = (-14).dp -> Lo sube la MITAD de su altura (aprox), quedando "mordiendo" el borde.
                // Como quitamos el padding del padre, esto se dibujará "fuera" de los límites,
                // pero NO empujará a las tarjetas vecinas.
                .offset(x = 8.dp, y = (-14).dp),
            shape = CircleShape,
            color = Color(0xFF1E1E1E), // Color oscuro para resaltar (o usa MaterialTheme.colorScheme.surface)
            border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.6f)),
            shadowElevation = 6.dp
        ) {
            IconButton(
                onClick = {
                    onRefresh() // Acción directa
                    // Opcional: Cerrar menú si estaba abierto, aunque aquí es el botón directo
                },
                modifier = Modifier.size(40.dp) // Tamaño un poco más grande
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Actualizar GPS",
                    tint = Color(0xFF22D3EE),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- MENÚ DESPLEGABLE ---
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF0D1117).copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = { Text("Usar GPS Actual", color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold) },
                onClick = {
                    onRefresh() // <--- ESTO EJECUTA LA ACTUALIZACIÓN
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = Color(0xFF22D3EE)) }
            )

            HorizontalDivider(color = Color.White.copy(0.1f))

            user?.personalAddresses?.forEach { addr ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("${addr.calle} ${addr.numero}", color = Color.White)
                            Text(addr.localidad, fontSize = 10.sp, color = Color.Gray)
                        }
                    },
                    onClick = {
                        onLocationSelected(LocationOption.Personal(addr.calle, addr.numero, addr.localidad))
                        expanded = false
                    }
                )
            }

            if (user?.companies?.isNotEmpty() == true) {
                HorizontalDivider(color = Color.White.copy(0.1f))
                user.companies.forEach { company ->
                    company.branches.forEach { branch ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${company.name} - ${branch.name}", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("${branch.address.calle} ${branch.address.numero}", fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onLocationSelected(LocationOption.Business(
                                    company.name, branch.name, branch.address.calle, branch.address.numero, branch.address.localidad
                                ))
                                expanded = false
                            }
                        )
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

    // Procesamos el nombre (MAXIMILIANO es largo, aseguramos que entre)
    val displayName = remember(user) {
        user?.name?.trim()?.split(" ")?.firstOrNull()?.uppercase() ?: "PERFIL"
    }
// 1. Detecta si el widget está siendo presionado
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

// 2. Anima la escala: Si se presiona, baja a 0.95 (95% del tamaño), si no, vuelve a 1.0
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scaleAnimation"
    )
    // --- CAMBIO 1: Usamos Box en lugar de Card para quitar el fondo gris ---
    Box(
        modifier = Modifier
            .fillMaxSize() // Llena el espacio del cuadro (weight 0.8f)
            .graphicsLayer(scaleX = scale, scaleY = scale) // <--- APLICA LA ESCALA AQUÍ
            .background(brush, RoundedCornerShape(16.dp)) // El fondo transparente va AQUÍ
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)) // Borde sutil
            .clip(RoundedCornerShape(16.dp)) // Recorta lo que sobresalga
            .clickable(
                interactionSource = interactionSource, // Conectamos la detección de presión
                indication = ripple(), // Agregamos el efecto visual de onda
                onClick = { showPopup = true }
            ),

        contentAlignment = Alignment.Center // --- CAMBIO 2: Alineación central forzada ---
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // Centra verticalmente Icono y Texto
            modifier = Modifier.padding(4.dp)
        ) {
            // --- CAMBIO 3: Control estricto del tamaño de la imagen ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(34.dp) // Un poco más chico para dejar espacio al texto
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF22D3EE), CircleShape), // Borde Cyan
                        contentScale = ContentScale.Crop,
                        // Si falla la carga, NO mostramos el icono roto, mostramos el vector
                        error = rememberVectorPainter(Icons.Default.Person)
                    )
                } else {
                    // Si no hay foto, mostramos icono vectorial limpio
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp)) // Espacio pequeño

            // Texto del Nombre
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 9.sp, // Letra pequeña para nombres largos como MAXIMILIANO
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // --- POPUP (Sin cambios) ---
    if (showPopup && user != null) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = { showPopup = false },
            properties = PopupProperties(focusable = true)
        ) {
            // Fondo oscuro semitransparente que cubre toda la pantalla
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showPopup = false }
            ) {
                // Variable para detonar la animación al momento de crearse el Popup
                var animateIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { animateIn = true }

                // Caja contenedora alineada arriba a la derecha
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 70.dp, end = 12.dp) // Ajusté un poco el padding
                        // --- CAMBIO 1: ANCHO MÁS GRANDE ---
                        .width(340.dp)
                        .clickable(enabled = false) {} // Evita que el click pase al fondo
                ) {
                    // --- CAMBIO 2: ANIMACIÓN DE ENTRADA ---
                    androidx.compose.animation.AnimatedVisibility(
                        visible = animateIn,
                        // Efecto: Se expande desde arriba hacia abajo + Aparece suavemente
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn() + slideInVertically { -40 },
                        // (Opcional) Salida: Se contrae hacia arriba
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                    ) {
                        UserProfilePopup(
                            user = user,
                            onClose = { showPopup = false },
                            onLogout = {
                                showPopup = false
                                onLogout()
                            },
                            onAddressSelected = {
                                onAddressSelected(it)
                                showPopup = false
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


 /**
    if (showPopup && user != null) {
        // ... (Tu código del Popup existente va aquí igual que antes) ...
        // Solo para no llenar la respuesta, asumo que mantienes el Popup igual
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = { showPopup = false },
            properties = PopupProperties(focusable = true)
        ) {
            // ... Tu lógica de popup ...
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showPopup = false }
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 16.dp)
                        .width(280.dp)
                        .clickable(enabled = false) {}
                ) {
                    UserProfilePopup(
                        user = user,
                        onClose = { showPopup = false },
                        onLogout = { showPopup = false; onLogout() },
                        onAddressSelected = {
                            onAddressSelected(it)
                            showPopup = false
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

**/
//**
//* 🔥 NUEVA FUNCIÓN DE GENERACIÓN DE CONTENIDO PARA EL CARRUSEL
//* Filtra categorías nuevas, nuevos prestadores y anuncios.
//*/
fun generateEnterpriseBannerItems(
    categories: List<CategoryEntity>,
    providers: List<Provider>
): List<AccordionBanner> {
    val bannerList = mutableListOf<AccordionBanner>()

    // 1. Filtrar Categorías Nuevas (isNew == true)
    categories.filter { it.isNew }.forEach { cat ->
        bannerList.add(
            AccordionBanner(
                id = "cat_new_${cat.name}",
                title = cat.name,
                subtitle = "¡Nueva categoría disponible!",
                icon = cat.icon,
                color = Color(cat.color),
                type = BannerType.NEW_CATEGORY,
                originalCategory = cat
            )
        )
    }

    // 2. Filtrar Novedades de Prestadores (isNewPrestador == true)
    categories.filter { it.isNewPrestador }.forEach { cat ->
        bannerList.add(
            AccordionBanner(
                id = "cat_prov_${cat.name}",
                title = cat.name,
                subtitle = "Nuevos profesionales registrados",
                icon = cat.icon,
                color = Color(cat.color),
                type = BannerType.NEW_PROVIDER,
                originalCategory = cat
            )
        )
    }

    // 3. Agregar Publicidad de Google (isAd == true o estática)
    // Aquí puedes mapear anuncios reales si los tienes en la BD o uno de prueba
    bannerList.add(
        AccordionBanner(
            id = "ad_google_1",
            title = "Publicidad",
            subtitle = "Contenido patrocinado por Google",
            icon = "📢",
            color = Color(0xFF1967D2),
            type = BannerType.GOOGLE_AD
        )
    )

    // 4. Agregar Promociones de Prestadores Subscritos
    providers.filter { it.isSubscribed }.shuffled().take(2).forEach { provider ->
        bannerList.add(
            AccordionBanner(
                id = "promo_${provider.id}",
                title = provider.displayName,
                subtitle = "¡Aprovecha esta oferta especial!",
                icon = "🔥",
                color = Color(0xFFE91E63),
                type = BannerType.PROMO
            )
        )
    }

    return bannerList.shuffled()
}



@Composable
fun SearchResultsPanel(
    searchQuery: String,
    allCategories: List<CategoryEntity>,
    onCategoryClick: (String) -> Unit
) {
    val prefixMatches = remember(searchQuery, allCategories) {
        allCategories.filter { it.name.startsWith(searchQuery, ignoreCase = true) }
    }

    val approximateMatches = remember(searchQuery, allCategories) {
        allCategories.filter {
            it.name.contains(searchQuery, ignoreCase = true) && !it.name.startsWith(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
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
                if (prefixMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "Coincidencia Exacta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(prefixMatches) { category ->
                        CompactCategoryCard(
                            item = category,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }
                }

                if (approximateMatches.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "Resultados relacionados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(approximateMatches) { category ->
                        CompactCategoryCard(
                            item = category,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun GoogleAdCard(item: BannerContent.GoogleAd, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Color(0xFFE8F0FE)), modifier = Modifier.fillMaxSize().clickable(onClick = onClick), border = BorderStroke(1.dp, Color(0xFF4285F4))) {
        Box(Modifier.fillMaxSize()) {
            Surface(color = Color(0xFFFBC02D), shape = RoundedCornerShape(bottomEnd = 8.dp), modifier = Modifier.align(Alignment.TopStart)) {
                Text("Anuncio", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Column(Modifier.align(Alignment.Center).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PlayCircleOutline, null, modifier = Modifier.size(40.dp), tint = Color(0xFF4285F4))
                Text(item.title, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1967D2))
            }
        }
    }
}

@Composable
fun ProviderPromoCard(item: BannerContent.ProviderPromo, onClick: () -> Unit, onProfileClick: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxSize().clickable(onClick = onClick)) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(0.3f), MaterialTheme.colorScheme.primary.copy(0.9f)))))
            Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(32.dp).clickable(onClick = onProfileClick), shape = CircleShape, border = BorderStroke(1.dp, Color.White)) {
                AsyncImage(model = item.provider.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, error = painterResource(R.drawable.iconapp))
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Surface(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(4.dp)) { Text("PROMO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) }
                Spacer(Modifier.height(2.dp))
                Text(item.promoTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2)
                Text(item.provider.displayName, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
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

fun generateRealBannerContent(categories: List<CategoryEntity>, providers: List<Provider>): List<BannerContent> {
    val bannerList = mutableListOf<BannerContent>()
    if (categories.isNotEmpty()) categories.shuffled().take(2).forEach { bannerList.add(BannerContent.Category(it)) }
    bannerList.add(BannerContent.GoogleAd("Publicidad", "Anuncio patrocinado.", ""))
    providers.filter { it.isSubscribed }.shuffled().take(1).forEach { bannerList.add(BannerContent.ProviderPromo(it, "¡Oferta Especial!")) }
    return bannerList.shuffled()
}

fun getCategoryEmoji(title: String): String {
    return when {
        title.contains("Hogar", ignoreCase = true) -> "🏠"
        title.contains("Tecnología", ignoreCase = true) -> "💻"
        title.contains("Vehículos", ignoreCase = true) -> "🚗"
        title.contains("Eventos", ignoreCase = true) -> "🎉"
        title.contains("Salud", ignoreCase = true) -> "⚕️"
        title.contains("Enseñanza", ignoreCase = true) -> "📚"
        title.contains("Construcción", ignoreCase = true) -> "🏗️"
        title.contains("Mascotas", ignoreCase = true) -> "🐾"
        title.contains("Belleza", ignoreCase = true) -> "💅"
        title.contains("Transporte", ignoreCase = true) -> "🚚"
        title.contains("Gastronomía", ignoreCase = true) -> "🍔"
        title.contains("Profesionales", ignoreCase = true) -> "👨‍⚖️"
        else -> "📂"
    }
}

//------------------------------------------------------------------------------------------------------------------------------
 //* [ACTUALIZACIÓN] UserProfilePopup - Estética de Árbol de Código & HUD.
 //* Implementa una jerarquía expandible tipo directorio con líneas de conexión técnica.
// ==================================================================================
// --- SECCIÓN: COMPONENTES DEL TOP BAR                    ICONO PERFIL ---
// ==================================================================================
@Composable
fun UserProfilePopup(
    user: UserEntity,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    // CAMBIO A: Ahora recibe LocationOption, no String
    onAddressSelected: (LocationOption) -> Unit,
    onProfileClick: () -> Unit
) {
    val geminiBrush = geminiGradientBrush(isAnimated = true)
    val cyberCyan = Color(0xFF22D3EE)
    val cyberMagenta = Color(0xFFE91E63)
    val cyberPurple = Color(0xFF9B51E0)
    val deepGlass = Color(0xFF0D1117).copy(alpha = 0.92f)

    // Estados de expansión para las secciones principales
    var personalExpanded by remember { mutableStateOf(true) }
    var businessExpanded by remember { mutableStateOf(true) }

    GeminiCyberWrapper(
        modifier = Modifier
            .fillMaxWidth() // Más ancho para mejor lectura
            .padding(vertical = 15.dp, horizontal = 1.dp),
        cornerRadius = 24.dp,
        isAnimated = true,
        showGlow = true
    ) {
        Column(
            modifier = Modifier
                .background(deepGlass)
                .fillMaxWidth()
                .heightIn(max = 650.dp) // Límite de altura para activar scroll
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // --- HEADER: SYSTEM_PROTOCOL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DATOS DE USUARIO V4",
                        style = MaterialTheme.typography.labelSmall,
                        color = cyberCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "STATUS: ACTIVE_SESSION",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                }


            }

            Spacer(Modifier.height(24.dp))

            // --- SECCIÓN 1: PERFIL DIGITAL (HUD CARD) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .clickable { onProfileClick() }
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar con "Targeting" frame
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(64.dp).border(1.dp, cyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)))
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, cyberCyan, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${user.name} ${user.lastName}".uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "UID: ${user.email}",
                            style = MaterialTheme.typography.labelSmall,
                            color = cyberCyan.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(Icons.Default.QrCodeScanner, null, tint = cyberCyan, modifier = Modifier.size(20.dp).graphicsLayer { alpha = 0.5f })
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- SECCIÓN 2: ÁRBOL DE DIRECCIONES (PERSONAL_FS) ---
            CyberTreeDirectory(
                title = "DIR_PERSONALES",
                icon = Icons.Default.FolderOpen,
                accentColor = cyberCyan,
                isExpanded = personalExpanded,
                onToggle = { personalExpanded = !personalExpanded }
            ) {
                user.personalAddresses.forEach { addr ->
                    CyberTreeLeaf(
                        icon = Icons.Default.LocationOn,
                        title = "${addr.calle} ${addr.numero}",
                        subtitle = "${addr.localidad}, ${addr.provincia}",
                        accentColor = cyberCyan,
                        onClick = { // ENVIAMOS EL OBJETO PERSONAL COMPLETO
                            onAddressSelected(LocationOption.Personal(
                                address = addr.calle,
                                number = addr.numero,
                                locality = addr.localidad
                            ))
                            onClose() }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- SECCIÓN 3: ÁRBOL DE NEGOCIOS (BUSINESS_FS) ---
            if (user.companies.isNotEmpty()) {
                CyberTreeDirectory(
                    title = "DIR_EMPRESA/COMERCIO",
                    icon = Icons.Default.Dns,
                    accentColor = cyberPurple,
                    isExpanded = businessExpanded,
                    onToggle = { businessExpanded = !businessExpanded }
                ) {
                    user.companies.forEach { company ->
                        // Sub-directorio para cada empresa
                        var companyItemExpanded by remember { mutableStateOf(false) }

                        CyberTreeDirectory(
                            title = company.name.uppercase(),
                            icon = Icons.Default.Business,
                            accentColor = cyberPurple.copy(alpha = 0.8f),
                            isExpanded = companyItemExpanded,
                            isNested = true,
                            onToggle = { companyItemExpanded = !companyItemExpanded }
                        ) {
                            company.branches.forEach { branch ->
                                CyberTreeLeaf(
                                    icon = Icons.Default.Storefront,
                                    title = branch.name,
                                    subtitle = "${branch.address.calle} ${branch.address.numero}",
                                    accentColor = cyberPurple,
                                    isNested = true,
                                    onClick = {// ENVIAMOS EL OBJETO NEGOCIO COMPLETO
                                        onAddressSelected(LocationOption.Business(
                                            companyName = company.name,
                                            branchName = branch.name,
                                            address = branch.address.calle,
                                            number = branch.address.numero,
                                            locality = branch.address.localidad
                                        ))
                                        onClose()  }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- BOTÓN DE SALIDA: PURGE_SESSION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cyberMagenta.copy(alpha = 0.05f))
                    .border(1.dp, Brush.horizontalGradient(listOf(cyberMagenta, Color.Transparent)), RoundedCornerShape(12.dp))
                    .clickable { onLogout() }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, null, tint = cyberMagenta, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Cerrar_Sesion",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(8.dp)
                        .background(cyberMagenta, CircleShape)
                        .blur(4.dp)
                )
            }
        }
    }
}


 //* Representa un Directorio (Nodo Padre) en el árbol Cyberpunk.
 //*/
@Composable
private fun CyberTreeDirectory(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    isExpanded: Boolean,
    isNested: Boolean = false,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f)

    Column(modifier = Modifier.padding(start = if (isNested) 16.dp else 0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de flecha indicativa
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
            )
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Líneas de conexión y contenido expandible
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 7.dp) // Alineado con el centro de la flecha/icono
                    .drawWithCache {
                        onDrawWithContent {
                            // Línea vertical que conecta los hijos
                            drawLine(
                                color = accentColor.copy(alpha = 0.2f),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawContent()
                        }
                    }
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

/**
 * Representa una Hoja (Nodo Hijo) con línea de conexión horizontal.
 */
@Composable
private fun CyberTreeLeaf(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    isNested: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp)
            .drawWithCache {
                onDrawWithContent {
                    // Línea horizontal "L-shape"
                    drawLine(
                        color = accentColor.copy(alpha = 0.2f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(15.dp.toPx(), size.height / 2),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawContent()
                }
            }
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
