package com.example.myapplication.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.Client.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import com.example.myapplication.R

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
fun HomeScreenComplete(navController: NavHostController, bottomPadding: PaddingValues) {
    // --- ESTADOS DE LA UI ---
    var profileMode by remember { mutableStateOf(ProfileMode.CLIENTE) } // Estado para alternar entre Cliente/Empresa
    var isSearchActive by remember { mutableStateOf(false) } // ¿Barra de búsqueda visible?
    var isFabMenuExpanded by remember { mutableStateOf(false) } // ¿Menú del FAB abierto?
    var showFavorites by remember { mutableStateOf(false) } // ¿Panel de favoritos visible?
    var searchQuery by remember { mutableStateOf("") } // Texto que escribe el usuario
    
    // Estados para favoritos (orden y selección)
    var favoritesSortBy by remember { mutableStateOf("Name") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFavorites by remember { mutableStateOf(setOf<String>()) }

    // Estados para manejo de recarga y barajado
    var isLoading by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Generación de datos falsos (Categorías y Banner)
    // Se recalcula cuando cambia refreshTrigger
    val generatedData = remember(refreshTrigger) { 
        generateFakeBannerAndCategories() 
    }
    val bannerItems = generatedData.first
    val regularCategories = generatedData.second

    // Efecto para simular carga inicial y al refrescar
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        delay(1500) // Simular retardo de carga para ver el efecto
        isLoading = false
    }

    // Efecto para barajar al entrar a la pantalla (simulado con el trigger inicial 0)
    LaunchedEffect(Unit) {
        // Al entrar, si queremos forzar un refresh:
        refreshTrigger++
    }

    // Animación del Scrim (Fondo oscuro transparente al abrir menús)
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isFabMenuExpanded || isSearchActive || showFavorites) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrim"
    )
    val showScrim by remember { mutableStateOf(true) }
    // Callback para navegar a resultados de categoría
    val onCategoryClick: (String) -> Unit = { categoryName ->
        navController.navigate("result_busqueda/$categoryName")
    }

    // Función auxiliar para cerrar búsqueda y teclado
    val closeSearch = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
        Unit
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // [MODIFICABLE] Color de fondo de toda la pantalla
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            // --- CAPA 1: CONTENIDO DE FONDO (SCROLLABLE) ---
            if (!isLoading) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // SECCIÓN DEL ENCABEZADO (Refactorizada con 3 columnas)
                    TopHeaderSection(
                        navController = navController,
                        profileMode = profileMode,
                        onModeToggle = {
                            profileMode = if (profileMode == ProfileMode.CLIENTE) ProfileMode.EMPRESA else ProfileMode.CLIENTE
                        }
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
                                onCategoryClick = onCategoryClick
                            )
                            Spacer(modifier = Modifier.height(1.dp)) // [MODIFICABLE] Separación entre filas de categorías
                        }
                    }
                }
            } else {
                 // --- LOADING SCREEN ---
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     ContainedJellyLoader()
                 }
            }

            // --- CAPA 2: SCRIM (FONDO OSCURO INTERACTIVO) ---
            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha)) // [MODIFICABLE] Color y opacidad del fondo
                        .zIndex(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Al tocar fuera, cerramos todo
                            isFabMenuExpanded = false
                            if (isSearchActive) closeSearch()
                            showFavorites = false
                        }
                )
            }

            // --- CAPA 3: BARRA DE BÚSQUEDA (ANIMADA DESDE ABAJO) ---
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { fullHeight -> fullHeight }), // Entra subiendo
                exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }), // Sale bajando
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
                        color = Color(0xFF121212), // [MODIFICABLE] Fondo oscuro
                        border = BorderStroke(2.5.dp, rainbowBrush), // [MODIFICABLE] Grosor borde
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }

            // --- CAPA 4: PANEL DE RESULTADOS DE BÚSQUEDA ---
            AnimatedVisibility(
                visible = isSearchActive && searchQuery.isNotEmpty() && !showFavorites,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
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
                    sortBy = favoritesSortBy,
                    selectionMode = isSelectionMode,
                    selectedIds = selectedFavorites,
                    searchQuery = searchQuery,
                    onToggleSelection = { id ->
                        selectedFavorites = if (selectedFavorites.contains(id)) selectedFavorites - id else selectedFavorites + id
                    }
                )
            }

            // --- CAPA 6: FAB DIVIDIDO CON SCRIM REUTILIZABLE ---
            GeminiFABWithScrim(
                //showScrim = showScrim
                bottomPadding = bottomPadding,
                showScrim = true // Solo mostrar el degradado si no hay menús abiertos
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre elementos si añades más
                ) {
                    // Si quieres que el cargador de gelatina aparezca arriba del FAB cuando buscas:
                    if (isSearchActive) {
                        M3JellyLoader(size = 40.dp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                GeminiSplitFAB(
                    isExpanded = isFabMenuExpanded,
                    isSearchActive = isSearchActive,
                    isSecondaryPanelVisible = showFavorites,
                    onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                    onActivateSearch = { isSearchActive = true },
                    onCloseSearch = closeSearch,
                    onCloseSecondaryPanel = { showFavorites = false },
                    // Botones de acción rápida (Izquierda del FAB)
                    secondaryActions = {
                        SmallActionFab(icon = Icons.Default.Gavel, onClick = { navController.navigate(Screen.CrearLicitacion.route) })
                        SmallActionFab(icon = Icons.Default.Bolt, onClick = { println("Acción rápida") })
                        SmallActionFab(icon = Icons.Default.Favorite, onClick = {
                            showFavorites = true
                            isFabMenuExpanded = false
                        })
                    },


                    // Herramientas que salen hacia arriba
                    expandedTools = {
                        SmallFabTool(label = "Filtros", icon = Icons.Default.FilterList, onClick = {})
                        // [NUEVO] Botón de Actualizar / Refresh
                        SmallFabTool(
                            label = "Actualizar", 
                            icon = Icons.Default.Refresh, 
                            onClick = { 
                                isFabMenuExpanded = false
                                refreshTrigger++ // Dispara la recarga y barajado
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
    onModeToggle: () -> Unit
) {
    // Usamos el currentUser simulado
    val user = remember { UserSampleDataFalso.currentUser }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. COLUMNA IZQUIERDA: WIDGET DE CLIMA ---
        Box(modifier = Modifier.weight(1f)) {
            WeatherWidget(temperature = "24°C")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- 2. COLUMNA CENTRAL: UBICACIÓN Y SELECTOR ---
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
            LocationSelector(user = user, mode = profileMode)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- 3. COLUMNA DERECHA: PERFIL Y CAMBIO DE MODO ---
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            ProfileModeSection(
                user = user,
                mode = profileMode,
                onModeToggle = onModeToggle,
                navController = navController
            )
        }
    }
}

/**
 * Widget que muestra información del clima de forma compacta.
 */
@Composable
fun WeatherWidget(temperature: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.WbSunny,
            contentDescription = "Clima",
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = temperature,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                lineHeight = 14.sp
            )
            Text(
                text = "Tucumán",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 8.sp
            )
        }
    }
}

/**
 * Selector de ubicación que permite filtrar según el modo (Cliente/Empresa).
 */
@Composable
fun LocationSelector(user: UserFalso?, mode: ProfileMode) {
    var expanded by remember { mutableStateOf(false) }
    // Estado para la dirección seleccionada actualmente
    var currentAddress by remember { 
        mutableStateOf(user?.personalAddresses?.firstOrNull()?.calle ?: "Ubicación") 
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = currentAddress,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Icono para actualización en tiempo real
        IconButton(
            onClick = { /* Lógica para actualizar ubicación GPS */ },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Actualizar GPS",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        // MENÚ DESPLEGABLE DE UBICACIONES
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            if (mode == ProfileMode.CLIENTE) {
                // SECCIÓN CLIENTE
                Text(
                    "Mis Direcciones Guardadas",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                user?.personalAddresses?.forEach { address ->
                    DropdownMenuItem(
                        text = { Text(address.fullString(), fontSize = 12.sp) },
                        onClick = {
                            currentAddress = address.calle
                            expanded = false
                        }
                    )
                }
            } else {
                // SECCIÓN EMPRESA
                Text(
                    "Ubicaciones de Mis Empresas",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                user?.companies?.forEachIndexed { index, company ->
                    if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    // Nombre de la empresa (No clickeable directamente si tiene sucursales)
                    Text(
                        text = company.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    
                    // Listar sucursales de la empresa
                    company.branches.forEach { branch ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Store, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(branch.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(branch.address.fullString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            },
                            onClick = {
                                currentAddress = "${company.name} - ${branch.name}"
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección derecha que muestra el perfil y permite cambiar de modo (Cliente/Empresa).
 */
@Composable
fun ProfileModeSection(
    user: UserFalso?,
    mode: ProfileMode,
    onModeToggle: () -> Unit,
    navController: NavHostController
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onModeToggle() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Etiqueta de Modo
            Surface(
                color = if (mode == ProfileMode.CLIENTE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (mode == ProfileMode.CLIENTE) "CLIENTE" else "EMPRESA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    fontSize = 7.sp
                )
            }

        }
        
        // Imagen de Perfil
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .size(32.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                .clickable { navController.navigate(Screen.PerfilCliente.route) },
            shadowElevation = 2.dp
        ) {
            AsyncImage(
                model = user?.profileImageUrl,
                contentDescription = "Perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.iconapp),
                fallback = painterResource(id = R.drawable.iconapp)
            )
        }
    }
}

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
    
    // Estados para los diálogos de Ad y Promo
    var showAdDialog by remember { mutableStateOf<BannerContent.GoogleAd?>(null) } // GoogleAd
    var showPromoDialog by remember { mutableStateOf<BannerContent.ProviderPromo?>(null) } // ProviderPromo

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
fun SuperCategorySection(superCategory: SuperCategory, onCategoryClick: (String) -> Unit) {
    val totalItems = superCategory.items.size
    var isExpanded by remember { mutableStateOf(true) } // Estado para contraer/expandir la fila
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2, pageCount = { Int.MAX_VALUE })
    
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
    MyApplicationTheme { 
        HomeScreenComplete(rememberNavController(), PaddingValues(0.dp)) 
    } 
}