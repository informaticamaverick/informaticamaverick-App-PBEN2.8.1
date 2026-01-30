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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.util.lerp
import androidx.navigation.NavHostController // Importación necesaria para la navegación
import com.example.myapplication.Client.*
import kotlin.math.absoluteValue


// ==========================================
// SECCIÓN DE MODELOS DE DATOS (DATA MODELS)
// ==========================================

data class SuperCategory(
    val title: String,           // Título de la sección (ej: Hogar, Tecnología)
    val items: List<CategoryItem> // Lista de categorías dentro de esta sección
)

// ==========================================
// PANTALLA PRINCIPAL (MAIN SCREEN)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(navController: NavHostController) { // Recibe el NavController desde AppNavigation
    // --- ESTADOS LOCALES DE LA PANTALLA ---
    var isSearchActive by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var favoritesSortBy by remember { mutableStateOf("Name") }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFavorites by remember { mutableStateOf(setOf<String>()) }

    // Generación de datos de prueba
    val (novedades, regularCategories) = remember { generateFakeCategories() }
    
    // Animación del scrim (fondo oscuro)
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isFabMenuExpanded || isSearchActive || showFavorites) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrim"
    )

    // Función para manejar el clic en una categoría y navegar
    val onCategoryClick: (String) -> Unit = { categoryName ->
        // Navega a la ruta de resultados, pasando el nombre de la categoría como argumento
        navController.navigate("result_busqueda/$categoryName")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
        // La BottomBar se gestiona en AppNavigation.kt
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Contenido principal de la pantalla
            Column(modifier = Modifier.fillMaxSize()) {
                TopHeaderSection()
                
                // Sección de Novedades
                if (novedades != null) {
                    SuperCategorySection(superCategory = novedades, onCategoryClick = onCategoryClick)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Lista de categorías regulares
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
                ) {
                    items(regularCategories) { superCat ->
                        SuperCategorySection(superCategory = superCat, onCategoryClick = onCategoryClick)
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }

            // Scrim interactivo para cerrar menús
            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .zIndex(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Al hacer clic en el scrim, se desactiva todo.
                            isFabMenuExpanded = false
                            isSearchActive = false
                            showFavorites = false
                            isSelectionMode = false
                            selectedFavorites = emptySet()
                            searchQuery = ""
                        }
                )
            }

            // Panel de Favoritos (Animado desde la derecha)
            AnimatedVisibility(
                visible = showFavorites,
                enter = slideInHorizontally(initialOffsetX = { it }), // Anima desde la derecha.
                exit = slideOutHorizontally(targetOffsetX = { it }),   // Sale hacia la derecha.
                modifier = Modifier
                    .align(Alignment.CenterEnd) // Se alinea a la derecha.
                    .zIndex(1.5f)
            ) {
                FavoritesPanel(
                    onClose = {
                        showFavorites = false
                        isFabMenuExpanded = false
                        isSelectionMode = false
                        searchQuery = ""
                        selectedFavorites = emptySet()
                    },
                    sortBy = favoritesSortBy,
                    selectionMode = isSelectionMode,
                    selectedIds = selectedFavorites,
                    searchQuery = searchQuery,
                    onToggleSelection = { id ->
                        selectedFavorites = if (selectedFavorites.contains(id)) selectedFavorites - id else selectedFavorites + id
                    }
                )
            }
            
            // Panel de resultados de búsqueda (Animado desde abajo)
            // Se muestra solo si hay búsqueda activa, texto escrito y no están los favoritos abiertos
            SearchResultsPanel(
                isVisible = isSearchActive && searchQuery.isNotEmpty() && !showFavorites,
                searchQuery = searchQuery,
                onCategoryClick = onCategoryClick,
                modifier = Modifier
                    .zIndex(1.5f)
                     // MODIFICADO: Posicionamiento preciso. 64dp (Header) + 64dp (Padding superior SearchBar) + 56dp (Altura SearchBar) + 8dp (Padding inferior SearchBar) + 2dp (Separación)
                    .padding(top = 64.dp + 64.dp + 56.dp + 8.dp + 2.dp)
            )

            // FAB Menu Overlay (Alineado abajo a la derecha)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .zIndex(2f),
                contentAlignment = Alignment.BottomEnd
            ) {
                // El FAB y sus menús solo se muestran si la búsqueda principal no está activa.
                if (!isSearchActive) {
                    FabMenuOverlay(
                        isFabMenuExpanded = isFabMenuExpanded,
                        showFavorites = showFavorites,
                        onToggleFabMenu = { isFabMenuExpanded = !isFabMenuExpanded },
                        onOpenFavorites = {
                            showFavorites = true
                            isFabMenuExpanded = false
                        },
                        onActivateSearch = { // Callback para abrir la barra de búsqueda
                            isSearchActive = true
                            isFabMenuExpanded = false
                        },
                        // Callbacks para el menú de favoritos
                        currentSortBy = favoritesSortBy,
                        isSelectionMode = isSelectionMode,
                        onSortByName = { favoritesSortBy = "Name" },
                        onSortByRank = { favoritesSortBy = "Rank" },
                        onToggleSelectMode = { isSelectionMode = !isSelectionMode },
                        onDeleteSelection = {
                            // Lógica para eliminar favoritos (simulada)
                            selectedFavorites = emptySet()
                            isSelectionMode = false
                        },
                        // Callback para que el botón 'X' del FAB pueda cerrar el panel de favoritos.
                        onCloseFavorites = {
                            showFavorites = false
                            isSelectionMode = false
                            searchQuery = ""
                        }
                    )
                }
            }

            // Barra de Búsqueda Superior (Animada desde arriba)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }), // Aparece desde arriba.
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),   // Desaparece hacia arriba.
                modifier = Modifier
                    .zIndex(2f) // zIndex alto para estar sobre todo.
                    .align(Alignment.TopCenter) // Se alinea en la parte superior.
            ) {
                TopSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCancel = { // Acción para el botón 'X'.
                        isSearchActive = false
                        searchQuery = ""
                    },
                    showFavorites = showFavorites
                )
            }
        }
    }
}


@Composable
fun FavoritesPanel(
    onClose: () -> Unit,
    sortBy: String,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    searchQuery: String,
    onToggleSelection: (String) -> Unit
) {
    val favorites by remember(sortBy, searchQuery) {
        derivedStateOf {
            var list = SampleDataFalso.prestadores.filter { it.isFavorite }
            if (searchQuery.isNotEmpty()) {
                list = list.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.lastName.contains(searchQuery, ignoreCase = true)
                }
            }
            when (sortBy) {
                "Name" -> list.sortedBy { it.name }
                "Rank" -> list.sortedByDescending { it.rating }
                else -> list
            }
        }
    }

    Surface(
        modifier = Modifier
          //.height(600.dp)
            .fillMaxHeight() // Ocupa toda la altura.------------------------------------------------------------------------------
            .width(300.dp), // Ancho fijo.
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (searchQuery.isNotEmpty()) {
                        Text("Resultados para \"$searchQuery\"", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar Panel de Favoritos")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (searchQuery.isNotEmpty()) "Sin coincidencias" else "No tienes favoritos aún.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(favorites, key = { it.id }) { provider ->
                        FavoriteCardItem( // Ahora usa el componente refactorizado.
                            provider = provider,
                            isSelectionMode = selectionMode,
                            isSelected = selectedIds.contains(provider.id),
                            onSelect = { onToggleSelection(provider.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderSection() {
    var locationExpanded by remember { mutableStateOf(false) }
    var profileExpanded by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf("Tucumán, AR") }

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth().zIndex(0.5f)) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Widget del clima
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.height(40.dp)) {
                Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WbSunny, "Clima", tint = Color(0xFFF9A825))
                    Spacer(Modifier.width(4.dp))
                    Text("28°C", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            // Selector de ubicación
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { locationExpanded = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(currentLocation, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                    DropdownMenuItem(text = { Text("Ubicación Actual") }, onClick = { currentLocation = "Tucumán, AR"; locationExpanded = false })
                }
            }
            // Menú de perfil
            Box {
                IconButton(onClick = { profileExpanded = true }) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Person, "Perfil") }
                    }
                }
                DropdownMenu(expanded = profileExpanded, onDismissRequest = { profileExpanded = false }, modifier = Modifier.width(200.dp)) {
                    DropdownMenuItem(leadingIcon = { Icon(Icons.Default.Settings, null) }, text = { Text("Configuración") }, onClick = { profileExpanded = false })
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuperCategorySection(superCategory: SuperCategory, onCategoryClick: (String) -> Unit) {
    val totalItems = superCategory.items.size
    var isExpanded by remember { mutableStateOf(true) }
    val startPage = (Int.MAX_VALUE / 2) // Punto de partida para simular un pager infinito.
    val initialPage = startPage - (startPage % totalItems)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })
    val currentRealIndex = (pagerState.currentPage % totalItems) + 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.colorScheme.surface)))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded } // Permite expandir/contraer la sección.
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(superCategory.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 0.5.dp, color = Color.Black.copy(alpha = 0.5f))
            Text("$currentRealIndex/$totalItems", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(110.dp), // Ancho fijo para cada item.
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 8.dp, // Espacio entre items.
                modifier = Modifier.height(110.dp)
            ) { page ->
                val realIndex = page % totalItems
                val item = superCategory.items[realIndex]
                CategoryCard(item = item, onClick = { onCategoryClick(item.name) })
            }
        }
    }
}

// ==========================================
// 4. BOTÓN FLOTANTE DIVIDIDO (SPLIT FAB)
// ==========================================
@Composable
fun FabMenuOverlay(
    isFabMenuExpanded: Boolean,
    showFavorites: Boolean, // NUEVO: Estado para saber si el panel de favoritos está abierto.
    onToggleFabMenu: () -> Unit,
    onOpenFavorites: () -> Unit,
    onActivateSearch: () -> Unit,
    onCloseFavorites: () -> Unit, // NUEVO: Callback para cerrar el panel de favoritos.
    // Callbacks y estados para el menú horizontal de favoritos
    currentSortBy: String,
    isSelectionMode: Boolean,
    onSortByName: () -> Unit,
    onSortByRank: () -> Unit,
    onToggleSelectMode: () -> Unit,
    onDeleteSelection: () -> Unit
) {
    val fabIconRotation by animateFloatAsState(
        targetValue = if (isFabMenuExpanded) 45f else 0f, // Anima la rotación del ícono principal. Gira 45 grados cuando el menú está expandido.
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabIconRotation"
    )

    Box(contentAlignment = Alignment.BottomEnd) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // MENÚ HORIZONTAL DE FAVORITOS (Aparece cuando showFavorites es true)
            AnimatedVisibility(
                visible = showFavorites,
                enter = fadeIn(animationSpec = tween(150, 150)) + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut(animationSpec = tween(150)) + slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    SmallFabTool(label = "Nombre", icon = Icons.AutoMirrored.Filled.Sort, onClick = onSortByName, isSelected = currentSortBy == "Name")
                    SmallFabTool(label = "Rank", icon = Icons.Default.Star, onClick = onSortByRank, isSelected = currentSortBy == "Rank")
                    SmallFabTool(label = "Elegir", icon = Icons.Default.CheckCircle, onClick = onToggleSelectMode, isSelected = isSelectionMode)
                }
            }

            // RE-AGREGADO: MENÚ HORIZONTAL DE ACCIONES GENERALES
            // Visible solo cuando el FAB se expande y NO se muestran los favoritos.
            AnimatedVisibility(
                visible = isFabMenuExpanded && !showFavorites,
                enter = fadeIn(animationSpec = tween(150, 150)) + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut(animationSpec = tween(150)) + slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    SmallFabTool(label = "Editar", icon = Icons.Default.Edit, onClick = {})
                    SmallFabTool(label = "Borrar", icon = Icons.Default.Delete, onClick = {})
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // MENÚ VERTICAL DE ACCESOS DIRECTOS
                // Condición corregida: Visible solo si el FAB está expandido Y los favoritos NO están visibles.
                AnimatedVisibility(
                    visible = isFabMenuExpanded && !showFavorites,
                    enter = fadeIn(animationSpec = tween(150, 150)) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        SmallFabTool(label = "Favoritos", icon = Icons.Default.Favorite, onClick = onOpenFavorites)
                        SmallFabTool(label = "Filtros", icon = Icons.Default.FilterList, onClick = {})
                    }
                }

                // FAB PRINCIPAL (IZQUIERDA + DERECHA)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // PARTE IZQUIERDA DEL FAB
                    Surface(
                        modifier = Modifier
                            .height(56.dp)
                            .width(140.dp)
                            .clickable(onClick = onActivateSearch),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Buscar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    if (showFavorites) "Favoritos" else "Servicios",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // PARTE DERECHA DEL FAB
                    Surface(
                        modifier = Modifier.size(56.dp).clickable {
                            if (showFavorites) {
                                onCloseFavorites()
                            } else {
                                onToggleFabMenu()
                            }
                        },
                        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (showFavorites) {
                                Icon(Icons.Default.Close, "Cerrar Favoritos")
                            } else {
                                Icon(Icons.Default.Settings, "Opciones", modifier = Modifier.rotate(fabIconRotation))
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SmallFabTool(label: String, icon: ImageVector, onClick: () -> Unit, isSelected: Boolean = false) {
    val animatedBg by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, label = "bg")
    val animatedContent by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "content")

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = animatedBg, shadowElevation = 4.dp, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = animatedContent) }
        }
        Spacer(Modifier.height(4.dp))
        Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun animateAlignmentAsState(targetAlignment: Alignment): State<Alignment> {
    val springSpec = spring<Float>(stiffness = Spring.StiffnessLow)
    val horizontalBias by animateFloatAsState(
        targetValue = if (targetAlignment == Alignment.TopCenter) 0f else 1f, // Anima el bias horizontal. 0f es centro, 1f es final (derecha).
        animationSpec = springSpec,
        label = "hBias"
    )
    val verticalBias by animateFloatAsState(
        targetValue = if (targetAlignment == Alignment.TopCenter) -0.95f else 0.95f, // Anima el bias vertical. -1f es arriba, 1f es abajo. Se usa 0.95 para un pequeño margen.
        animationSpec = springSpec,
        label = "vBias"
    )
    return remember(horizontalBias, verticalBias) {
        derivedStateOf { BiasAlignment(horizontalBias, verticalBias) }
    }
}

fun generateFakeCategories(): Pair<SuperCategory?, List<SuperCategory>> {
    val allCategories = CategorySampleDataFalso.categories
    val novedadesItems = allCategories.filter { it.isNew || it.isNewPrestador }.toMutableList()
    if (novedadesItems.isNotEmpty()) {
        novedadesItems.add(CategoryItem("Publicidad", "", Color.LightGray, "Novedades", isAd = true))
        novedadesItems.shuffle()
    }
    val regularSuperCategories = allCategories
        .filterNot { it.isNew || it.isNewPrestador }
        .shuffled()
        .groupBy { it.superCategory }
        .map { (key, value) -> SuperCategory(title = key, items = value.shuffled()) }
        .shuffled()

    val novedadesCategory = if (novedadesItems.isNotEmpty()) SuperCategory(title = "Novedades", items = novedadesItems) else null
    return Pair(novedadesCategory, regularSuperCategories)
}


@Composable
fun CategoryCard(item: CategoryItem, onClick: () -> Unit) {//Agrega la funcion para tocar y llevar al resultado
    var showNewCategoryMenu by remember { mutableStateOf(false) }
    var showNewPrestadorMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = item.color),
        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.isAd) {
                // Contenido para anuncios
            } else {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f)))))
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
                Text(item.icon, fontSize = 50.sp, modifier = Modifier.align(BiasAlignment(0f, -0.3f)))

                if (item.isNew) {
                    Box(modifier = Modifier.align(Alignment.TopStart)) {
                        Surface(
                            onClick = { showNewCategoryMenu = true },
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(bottomEnd = 8.dp)
                        ) {
                            Text("NUEVO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiary, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        DropdownMenu(
                            expanded = showNewCategoryMenu,
                            onDismissRequest = { showNewCategoryMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Esta es una categoría nueva", style = MaterialTheme.typography.bodySmall) },
                                onClick = { showNewCategoryMenu = false }
                            )
                        }
                    }
                }

                if (item.isNewPrestador) {
                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(onClick = { showNewPrestadorMenu = true }) {
                            Icon(Icons.Default.PriorityHigh, "Nuevo Prestador", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp).background(Color.White, CircleShape).padding(2.dp))
                        }
                        DropdownMenu(
                            expanded = showNewPrestadorMenu,
                            onDismissRequest = { showNewPrestadorMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Hay nuevos prestadores aquí", style = MaterialTheme.typography.bodySmall) },
                                onClick = { showNewPrestadorMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TopSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCancel: () -> Unit,
    showFavorites: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 8.dp), // Padding para ubicarla debajo del TopHeader.
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (showFavorites) "Buscar en Favoritos..." else "Buscar Servicios...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            // Botón de cancelar (X).
            IconButton(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, "Cancelar Búsqueda")
                    }
                }
            }
        }
    }
}

// MODIFICADO: SearchResultsPanel ahora usa LazyVerticalGrid, tiene un encabezado y lógica de búsqueda mejorada.
@Composable
fun SearchResultsPanel(
    isVisible: Boolean,
    searchQuery: String,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.trim()
            CategorySampleDataFalso.categories.filter { category ->
                val nameMatches = category.name.split(" ").any { it.startsWith(query, ignoreCase = true) }
                val superCategoryMatches = category.superCategory.split(" ").any { it.startsWith(query, ignoreCase = true) }
                
                !category.isAd && category.superCategory != "Novedades" && (nameMatches || superCategoryMatches)
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Column {
                Text(
                    text = "Resultados de Búsqueda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchResults.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron resultados.",
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    } else {
                        items(searchResults) { category ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.height(110.dp).width(110.dp)) {
                                    CategoryCard(item = category, onClick = { onCategoryClick(category.name) })
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = category.superCategory,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun HomeScreenCompletePreview() {
    // Para la preview, pasamos un NavController de prueba
    val navController = androidx.navigation.compose.rememberNavController()
    MaterialTheme { HomeScreenComplete(navController = navController) }
}
