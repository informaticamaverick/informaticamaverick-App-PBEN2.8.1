package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.Profile.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Pantalla principal del cliente rediseñada con carruseles avanzados por SuperCategoría (MD3).
 * Implementa efectos premium de escala dinámica tipo acordeón y actualización por gesto.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenCliente(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Carga inicial del perfil
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    var searchText by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    // --- LÓGICA DE CATEGORÍAS BARAJADAS (SHUFFLE) ---
    var shuffledGroupedCategories by remember { 
        mutableStateOf<List<Pair<String, List<CategoryItem>>>>(emptyList()) 
    }

    val shuffleData = {
        val grouped = CategorySampleDataFalso.categories
            .groupBy { it.superCategory }
            .mapValues { it.value.shuffled() } 
            .toList()
            .shuffled() 
        shuffledGroupedCategories = grouped
    }

    LaunchedEffect(Unit) {
        shuffleData()
    }

    val density = LocalDensity.current
    
    // --- Configuración del Panel Lateral (Favoritos) ---
    val panelWidth = 340.dp
    val panelWidthPx = with(density) { panelWidth.toPx() }
    val peekWidthPx = with(density) { 80.dp.toPx() }

    // Animatable para controlar el deslizamiento del panel lateral
    val panelOffset = remember { Animatable(panelWidthPx) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sección superior
            HomeTopSection(
                userName = uiState.displayName,
                photoUrl = uiState.photoUrl,
                hasNotification = true,
                onProfileClick = { navController.navigate(Screen.PerfilCliente.route) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp), 
                thickness = 1.dp, 
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // --- CONTENEDOR PRINCIPAL CON GESTO DE ACTUALIZAR ---
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        delay(1000)
                        shuffleData()
                        isRefreshing = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    // Sección de categorías organizada en carruseles
                    CategoriesCarouselSection(
                        shuffledGroups = shuffledGroupedCategories,
                        searchText = searchText,
                        onNavigateToCategoryResults = { categoryName ->
                            navController.navigate("result_busqueda/$categoryName")
                        }
                    )
                }
            }
        }

        // --- SCRIM (Sombra panel lateral) ---
        if (panelOffset.value < panelWidthPx) {
            val alpha = ((panelWidthPx - panelOffset.value) / panelWidthPx) * 0.6f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = alpha))
                    .clickable { scope.launch { panelOffset.animateTo(panelWidthPx) } }
            )
        }

        // --- BOTONES FLOTANTES (FABs) ---
        // Se posicionan arriba de la barra de navegación y suben con el teclado

// --- BOTONES FLOTANTES (FABs) ---

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 1.dp), // Espaciado sobre el teclado/navBar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // --- Botones de acción rápida (Licitación, Flash, Favoritos) ---
                AnimatedVisibility(
                    visible = !isFabExpanded,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Favoritos
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    if (panelOffset.value >= panelWidthPx) panelOffset.animateTo(panelWidthPx - peekWidthPx)
                                    else panelOffset.animateTo(panelWidthPx)
                                }
                            },
                            shape = CircleShape,
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ) {
                            Icon(
                                imageVector = if (panelOffset.value >= panelWidthPx) Icons.Filled.Star else Icons.Default.Close,
                                contentDescription = "Favoritos"
                            )
                        }
                        // Botón Flash
                        SmallFloatingActionButton(
                            onClick = { navController.navigate(Screen.Promo.route) },
                            shape = CircleShape,
                            containerColor = Color(0xFFFFC107),
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = "Promociones Flash")
                        }
                        // Botón Licitación
                        SmallFloatingActionButton(
                            onClick = { navController.navigate(Screen.CrearLicitacion.route) },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(Icons.Filled.Gavel, contentDescription = "Licitación")
                        }
                    }
                }

                // --- FAB Principal para Búsqueda y Herramientas ---
                var fabSearchText by remember { mutableStateOf("") }

                SplitFloatingActionButton(
                    onStateChange = { expanded, _ -> isFabExpanded = expanded },
                    onSearchClick = { expanded ->
                        if (!expanded) {
                            fabSearchText = ""
                            searchText = ""
                        }
                    },
                    onToolsClick = { /* Acciones */ },
                    searchContent = {
                        TextField(
                            value = fabSearchText,
                            onValueChange = {
                                fabSearchText = it
                                searchText = it
                            },
                            placeholder = { Text("Buscar servicio...", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    },
                    toolsContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (!isRefreshing) {
                                        scope.launch {
                                            isRefreshing = true
                                            delay(1500)
                                            shuffleData()
                                            isRefreshing = false
                                        }
                                    }
                                }
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, "Refrescar", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            IconButton(onClick = { /* Acción Filtrar */ }) {
                                Icon(Icons.Default.FilterList, "Filtrar", tint = MaterialTheme.colorScheme.secondary)
                            }
                            IconButton(onClick = { /* Acción Ordenar */ }) {
                                Icon(Icons.AutoMirrored.Filled.Sort, "Ordenar", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                )
            }
        }

        // --- PANEL DE FAVORITOS ---
        Surface(
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .offset { IntOffset(panelOffset.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val newValue = (panelOffset.value + delta).coerceIn(0f, panelWidthPx)
                            panelOffset.snapTo(newValue)
                        }
                    },
                    onDragStopped = {
                        val target = if (panelOffset.value < (panelWidthPx - peekWidthPx) / 2) 0f
                        else if (panelOffset.value < panelWidthPx - (peekWidthPx / 2)) panelWidthPx - peekWidthPx
                        else panelWidthPx
                        panelOffset.animateTo(target)
                    }
                ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
        ) {
            FavoritesBox(
                onNavigateToProviderProfile = { providerId ->
                    navController.navigate("perfil_prestador/$providerId")
                    scope.launch { panelOffset.animateTo(panelWidthPx) }
                },
                onNavigateToChat = { providerId ->
                    navController.navigate("chat_conversation/$providerId")
                    scope.launch { panelOffset.animateTo(panelWidthPx) }
                }
            )
        }
    }
}

/**
 * Organiza las categorías en una lista vertical de carruseles.
 */
@Composable
fun CategoriesCarouselSection(
    shuffledGroups: List<Pair<String, List<CategoryItem>>>,
    searchText: String,
    onNavigateToCategoryResults: (String) -> Unit
) {
    // Lógica de búsqueda mejorada
    val searchResults = remember(searchText, shuffledGroups) {
        if (searchText.isBlank()) {
            emptyList()
        } else {
            // 1. Obtiene todas las categorías individuales, aplanando la lista
            val allCategories = shuffledGroups.flatMap { it.second }
            // 2. Filtra por coincidencia de palabras
            allCategories.filter {
                it.name.split(" ").any { word ->
                    word.startsWith(searchText, ignoreCase = true)
                }
            }
        }
    }

    // Muestra resultados de búsqueda o los carruseles normales
    if (searchText.isNotBlank()) {
        // --- VISTA DE RESULTADOS DE BÚSQUEDA (GRID 3 COLUMNAS) ---
        if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron resultados para \"$searchText\"", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults, key = { it.name }) { category ->
                    CategoryCarouselItem(
                        category = category,
                        onNavigateToCategoryResults = onNavigateToCategoryResults,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    } else {
        // --- VISTA DE CARRUSELES POR SUPERCATEGORÍA ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Busca el Servicio que quieras",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(shuffledGroups) { _, (superCategory, list) ->
                SuperCategoryCarousel(
                    title = superCategory,
                    items = list,
                    onNavigateToCategoryResults = onNavigateToCategoryResults
                )
                Spacer(modifier = Modifier.height(1.dp))
            }
        }
    }
}

/**
 * Carrusel con efecto de escala dinámica tipo acordeón.
 * Ajustado para mostrar 3 tarjetas por fila.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuperCategoryCarousel(
    title: String,
    items: List<CategoryItem>,
    onNavigateToCategoryResults: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        // ENCABEZADO MD3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 8.dp,
            pageSize = androidx.compose.foundation.pager.PageSize.Fixed(110.dp),
            beyondViewportPageCount = 3
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))

            CategoryCarouselItem(
                category = items[page],
                onNavigateToCategoryResults = onNavigateToCategoryResults,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(110.dp) // Tamaño cuadrado
            )
        }
    }
}

/**
 * Tarjeta individual de categoría rediseñada para ser más pequeña y cuadrada.
 */
@Composable
fun CategoryCarouselItem(
    category: CategoryItem,
    onNavigateToCategoryResults: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "press")

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onNavigateToCategoryResults(category.name) }
            ),
        colors = CardDefaults.cardColors(containerColor = category.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Icono central grande
            Text(
                text = category.icon,
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-10).dp)///Movimiento de texto
            )

            // Gradiente oscuro inferior para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                            startY = 10f
                        )
                    )
            )

            // Texto posicionado abajo a la izquierda (Interior)
            Text(
                text = category.name,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Muestra un ítem en la lista de resultados de búsqueda (No se usa más, se usa CategoryCarouselItem)
 */
@Composable
fun SearchResultItem(category: CategoryItem, onClick: (String) -> Unit) {
     // Reemplazado por CategoryCarouselItem en el grid
}


// --- RESTO DE COMPONENTES AUXILIARES ---

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesBox(
    modifier: Modifier = Modifier,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val allFavorites = SampleDataFalso.prestadores.filter { it.isFavorite }
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var onlineOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var doesHomeVisitsOnly by remember { mutableStateOf(false) }

    val filteredFavorites = remember(allFavorites, searchText, onlineOnly, works24hOnly, doesHomeVisitsOnly) {
        allFavorites.filter { provider ->
            val fullName = "${provider.name} ${provider.lastName}"
            val matchesText = if (searchText.isBlank()) true else
                fullName.split(" ").any { it.startsWith(searchText, ignoreCase = true) }
            val matchesOnline = if (onlineOnly) provider.isOnline else true
            val matches24h = if (works24hOnly) provider.works24h else true
            val matchesHome = if (doesHomeVisitsOnly) provider.doesHomeVisits else true
            matchesText && matchesOnline && matches24h && matchesHome
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar en favoritos...") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = CircleShape,
                        leadingIcon = {
                            IconButton(onClick = { isSearchExpanded = false; searchText = "" }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        singleLine = true
                    )
                } else {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        SmallFilterChip(selected = onlineOnly, onClick = { onlineOnly = !onlineOnly }, label = { Text("On") }, leadingIcon = { Icon(Icons.Default.Circle, null, tint = if(onlineOnly) Color.Green else Color.Gray, modifier = Modifier.size(8.dp)) })
                        Spacer(modifier = Modifier.width(4.dp))
                        SmallFilterChip(selected = works24hOnly, onClick = { works24hOnly = !works24hOnly }, label = { Text("24h") }, leadingIcon = { Icon(Icons.Default.Schedule, null, modifier = Modifier.size(12.dp)) })
                        Spacer(modifier = Modifier.width(4.dp))
                        SmallFilterChip(selected = doesHomeVisitsOnly, onClick = { doesHomeVisitsOnly = !doesHomeVisitsOnly }, label = { Text("Casa") }, leadingIcon = { Icon(Icons.Default.Home, null, modifier = Modifier.size(12.dp)) })
                    }
                    IconButton(onClick = { isSearchExpanded = true }) { Icon(Icons.Default.Search, null) }
                }
            }
        }
        if (filteredFavorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aún no tienes favoritos.", color = Color.Gray) }
        } else {
            LazyColumn(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items = filteredFavorites, key = { it.id }) { provider ->
                    PrestadorCard(
                        provider = provider, 
                        onClick = { onNavigateToProviderProfile(provider.id) }, 
                        onChat = { onNavigateToChat(provider.id) }, 
                        actionContent = { 
                            ActionContent(
                                inDeleteMode = false, 
                                onMessageClick = { onNavigateToChat(provider.id) }, 
                                onDeleteRequest = {}
                            ) 
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallFilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit, leadingIcon: @Composable (() -> Unit)?) {
    FilterChip(selected = selected, onClick = onClick, label = { ProvideTextStyle(value = MaterialTheme.typography.labelSmall) { label() } }, leadingIcon = leadingIcon, modifier = Modifier.height(32.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC8E6C9), selectedLabelColor = Color(0xFF1B5E20), containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), labelColor = MaterialTheme.colorScheme.onSurfaceVariant), border = null, shape = CircleShape)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeTopSection(modifier: Modifier = Modifier, userName: String, photoUrl: String?, hasNotification: Boolean, onProfileClick: () -> Unit) {
    val user = UserSampleDataFalso.findUserByUsername("maxinanterne")
    var selectedLocation by remember { mutableStateOf("Ubicación Actual") }
    val locationDetail = when(selectedLocation) { "Home" -> user?.direccionCasa ?: ""; "Trabajo" -> user?.direccionTrabajo ?: "N/A"; else -> user?.ciudad ?: "" }
    var locationExpanded by remember { mutableStateOf(false) }
    var profileExpanded by remember { mutableStateOf(false) }

    Row(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.Start) {
            Card(shape = RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("☀️", fontSize = 18.sp)
                    Text(text = "24°C en ${user?.ciudad}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Column {
                    Row(modifier = Modifier.clickable { locationExpanded = true }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                        Text(selectedLocation, modifier = Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    Text(text = locationDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 22.dp) )
                }
                DropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                    DropdownMenuItem(text = { Text("Home") }, onClick = { selectedLocation = "Home"; locationExpanded = false }, leadingIcon = { Icon(Icons.Default.Home, null) })
                    user?.direccionTrabajo?.let { DropdownMenuItem(text = { Text("Trabajo") }, onClick = { selectedLocation = "Trabajo"; locationExpanded = false }, leadingIcon = { Icon(Icons.Default.Build, null) }) }
                    DropdownMenuItem(text = { Text("Ubicación Actual") }, onClick = { selectedLocation = "Ubicación Actual"; locationExpanded = false }, leadingIcon = { Icon(Icons.Default.Place, null) })
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .combinedClickable(onClick = onProfileClick, onLongClick = { profileExpanded = true })
            .padding(8.dp)) {
            Box {
                BadgedBox(badge = { if (hasNotification) Badge(modifier = Modifier.offset(x = (-8).dp, y = 6.dp)) }) {
                    AsyncImage(
                        model = photoUrl, 
                        contentDescription = "Perfil", 
                        fallback = rememberVectorPainter(image = Icons.Outlined.AccountCircle), 
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                DropdownMenu(expanded = profileExpanded, onDismissRequest = { profileExpanded = false }) {
                    DropdownMenuItem(text = { Text("Configuración") }, onClick = { profileExpanded = false }, leadingIcon = { Icon(Icons.Outlined.Settings, null) })
                    DropdownMenuItem(text = { Text("Notificaciones") }, onClick = { profileExpanded = false }, leadingIcon = { Icon(Icons.Outlined.Notifications, null) })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Cerrar Sesión", color = Color.Red) }, onClick = { profileExpanded = false }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red) })
                }
            }
            Text(text = userName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenClientePreview() {
    val navController = rememberNavController()
    HomeScreenCliente(navController = navController)
}
