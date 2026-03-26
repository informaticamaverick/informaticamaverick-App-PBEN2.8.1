package com.example.myapplication.presentation.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.profile.ProfileViewModel // 🔥 USAMOS EL NUEVO CEREBRO
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.collections.isNotEmpty

// ==================================================================================
// --- PANTALLA PRINCIPAL ---
// ==================================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController, 
    bottomPadding: PaddingValues, 
    profileViewModel: ProfileViewModel = hiltViewModel(), // 🔥 CAMBIADO A ProfileViewModel (CEREBRO UNIFICADO)
    weatherViewModel: WeatherViewModel = viewModel(), 
    providerViewModel: ProviderViewModel = hiltViewModel(), 
    categoryViewModel: CategoryViewModel = hiltViewModel(), 
    simulationViewModel: SimulationViewModel = hiltViewModel(), 
    beViewModel: BeBrainViewModel = hiltViewModel() 
) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(context))
    
    val providers by providerViewModel.providers.collectAsStateWithLifecycle() 
    val favorites by providerViewModel.favorites.collectAsStateWithLifecycle() 
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle() 
    val userState by profileViewModel.userState.collectAsStateWithLifecycle() // 🔥 Escuchando al cerebro unificado
    val selectedLocation by beViewModel.selectedLocation.collectAsStateWithLifecycle() 

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

    LaunchedEffect(userState) {
        if (userState != null) {
            beViewModel.updateProfile(userState)
        }
    }

    LaunchedEffect(cityName) {
        if (selectedLocation == null && cityName.isNotEmpty()) {
            beViewModel.updateLocation(LocationOption.Gps(address = cityName, locality = "Ubicación Actual"))
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
        beViewModel = beViewModel
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    userState: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    onRefreshLocation: () -> Unit,
    allProviders: List<Provider>,
    favoriteProviders: List<Provider>,
    onToggleFavorite: (String, Boolean) -> Unit,
    onLogout: () -> Unit,
    allCategories: List<CategoryEntity>,
    beViewModel: BeBrainViewModel
) {
    val superCategories by beViewModel.superCategories.collectAsStateWithLifecycle()
    val activeSortFilters by beViewModel.activeSortFilters.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()
    val individualGridState = rememberLazyGridState()
    val isScrolling by remember {
        derivedStateOf { gridState.isScrollInProgress || individualGridState.isScrollInProgress }
    }

    LaunchedEffect(allCategories, activeSortFilters) {
        beViewModel.updateSuperCategories(allCategories)
    }
    
    var showWeatherDetails by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var isSuperCategoryView by remember { mutableStateOf(true) }

    val bannerItems = remember(allCategories, allProviders, refreshTrigger) {
        generateEnrichedBannerItems(allCategories, allProviders)
    }

    LaunchedEffect(activeSortFilters, isSuperCategoryView) {
        gridState.scrollToItem(0)
        individualGridState.scrollToItem(0)
    }

    Scaffold(containerColor = Color(0xFF0A0E14),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(145.dp))
                if (bannerItems.isNotEmpty()) {
                    PremiumLensCarousel(
                        items = bannerItems,
                        isPaused = isScrolling || showWeatherDetails||beViewModel.isSearchActive.collectAsState().value,
                        onSettingsClick = { },
                        onItemClick = { banner ->
                            if (banner.provider != null) {
                                navController.navigate("perfil_prestador/${banner.provider.id}")
                            } else if (banner.originalCategory != null) {
                                navController.navigate("result_busqueda/${banner.originalCategory.name}")
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EXPLORA LAS CATEGORIAS ",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            maxLines = 1
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.9f))
                    }
                    MenuOrdenamiento(
                        activeFilters = activeSortFilters,
                        onAction = { newFilter ->
                            if (newFilter == "view_bento") {
                                isSuperCategoryView = true
                            } else if (newFilter == "view_grid") {
                                isSuperCategoryView = false
                            }
                            beViewModel.updateSortFilters(if (newFilter.isEmpty()) emptySet() else setOf(newFilter))
                        },
                        onApply = { },
                        onClearFilters = {
                            beViewModel.updateSortFilters(emptySet())
                            isSuperCategoryView = true 
                        },
                        showNombre = true,
                        showViewModes = true
                    )
                }

                if (isSuperCategoryView) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = gridState, 
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 180.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(
                            items = superCategories,
                            key = { it.title },
                            contentType = { "super_category_card" }
                        ) { superCat ->
                            BentoSuperCategoryCard(
                                superCategory = superCat,
                                emoji = superCat.icon, 
                                height = 180.dp,       
                                onClick = { beViewModel.selectSuperCategory(superCat) }
                            )
                        }
                    }

                } else {
                    val sortedIndividual = remember(allCategories, activeSortFilters) {
                        when {
                            activeSortFilters.contains("sort_nombre_asc") -> allCategories.sortedBy { it.name }
                            activeSortFilters.contains("sort_nombre_desc") -> allCategories.sortedByDescending { it.name }
                            else -> allCategories
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = individualGridState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 180.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(  
                            items = sortedIndividual,
                            key = { it.name }, 
                            contentType = { "single_cat" }
                        ) { category ->
                            CompactCategoryCard(item = category, onClick = { navController.navigate("result_busqueda/${category.name}") })
                        }
                    }
                }
            }
            Box(modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .zIndex(10f)) {
                Box(modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.65f)))
                Box(modifier = Modifier.statusBarsPadding()) {
                    var currentLocationState by remember {
                        mutableStateOf<LocationOption>(LocationOption.Gps(address = cityName, locality = "Ubicación Actual"))
                    }
                    
                    LaunchedEffect(cityName) {
                        if (cityName.isNotEmpty()) {
                            val newLoc = LocationOption.Gps(address = cityName, locality = "Ubicación Actual")
                            currentLocationState = newLoc
                            beViewModel.updateLocation(newLoc) 
                        }
                    }
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
                        onLocationSelected = { nuevaSeleccion -> 
                            currentLocationState = nuevaSeleccion
                            beViewModel.updateLocation(nuevaSeleccion) 
                        },
                        onLogout = onLogout,
                        beViewModel = beViewModel 
                    )
                }
            }
            if (showWeatherDetails) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .zIndex(15f)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showWeatherDetails = false })
            }
            AnimatedVisibility(
                visible = showWeatherDetails, enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(), exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 60.dp)
                    .zIndex(20f)
            ) {
                WeatherExpandedCard(temperature = temperature, weatherEmoji = weatherEmoji, weatherDescription = weatherDescription, cityName = cityName, forecastDays = emptyList())
            }
            if (showFavorites) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .zIndex(11f)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        showFavorites = false
                    })
            }
            AnimatedVisibility(visible = showFavorites, enter = slideInHorizontally(initialOffsetX = { it }), exit = slideOutHorizontally(targetOffsetX = { it }), modifier = Modifier
                .align(Alignment.CenterEnd)
                .zIndex(12f)) {
                FavoritesPanel(navController = navController, favorites = favoriteProviders, onClose = { showFavorites = false }, onToggleFavorite = onToggleFavorite)
            }
            
            SuperCategoryDetailsPanel(
                beViewModel = beViewModel,
                onCategoryClick = { categoryName ->
                    navController.navigate("result_busqueda/$categoryName")
                }
            )
        }
    }
}

@Composable
fun FavoritesPanel(navController: NavHostController, favorites: List<Provider>, onClose: () -> Unit, onToggleFavorite: (String, Boolean) -> Unit) {
    Surface(modifier = Modifier
        .fillMaxHeight()
        .width(320.dp), color = Color(0xFF0A0E14), tonalElevation = 16.dp, shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .statusBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }
            HorizontalDivider(color = Color.White.copy(0.1f))
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (favorites.isEmpty()) {
                    item { Text("No tienes favoritos guardados.", modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center) }
                } else {
                    items(items = favorites, key = { it.id }) { provider ->
                        PrestadorCard(provider = provider, onClick = { navController.navigate("perfil_prestador/${provider.id}") }, onToggleFavorite = { id, isFav -> onToggleFavorite(id, isFav) }, showPreviews = false, viewMode = "Compacta", onChat = { navController.navigate("chat/${provider.id}") })
                    }
                }
            }
        }
    }
}

 fun generateEnrichedBannerItems(categories: List<CategoryEntity>, providers: List<Provider>): List<AccordionBanner> {
    val bannerList = mutableListOf<AccordionBanner>()
    categories.filter { it.isNew }.take(5).forEach { cat ->
        bannerList.add(AccordionBanner(
            id = "cat_${cat.name}", title = cat.name, subtitle = "🚀 EXPLORA LO NUEVO", icon = cat.icon, color = Color(0xFF2197F5), type = BannerType.NEW_CATEGORY, originalCategory = cat
        ))
    }
    providers.filter { it.isSubscribed }.take(5).forEach { provider ->
        bannerList.add(AccordionBanner(
            id = "promo_${provider.uid}", title = "Oferta Especial", subtitle = "Servicio destacado de ${provider.displayName}", icon = "🔥", color = Color(0xFFE91E63), type = BannerType.PROMO, discount = (15..45).random(), provider = provider
        ))
    }
    bannerList.add(AccordionBanner(
        id = "ad_google_phantom", title = "Anuncio Patrocinado", subtitle = "Descubre más en Google Ads", icon = "🌐", color = Color.DarkGray, type = BannerType.GOOGLE_AD
    ))
    return bannerList.shuffled() 
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenCompletePreview() {
    MyApplicationTheme {
        HomeScreenContent(
            navController = rememberNavController(),
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
