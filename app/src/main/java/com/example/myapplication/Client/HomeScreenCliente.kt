package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenCliente(navController: NavHostController) {
    var isCategoriesFullScreen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var categorySortMode by remember { mutableStateOf("Random") }

    val userName = "Maximiliano"
    val userProfileUrl: String? = null
    val hasNotification = true

    val randomCategories = remember { CategorySampleDataFalso.categories.shuffled() }
    val filteredCategories = remember(searchText, categorySortMode, randomCategories) {
        val filtered = if (searchText.isBlank()) {
            randomCategories
        } else {
            randomCategories.filter { category ->
                category.name.split(" ").any { it.startsWith(searchText, ignoreCase = true) }
            }
        }

        when (categorySortMode) {
            "Alpha" -> filtered.sortedBy { it.name }
            "Super" -> filtered.sortedWith(compareBy({ it.superCategory }, { it.name }))
            else -> filtered
        }
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopSection(
                userName = userName,
                photoUrl = userProfileUrl,
                hasNotification = hasNotification,
                onProfileClick = { navController.navigate(Screen.PerfilCliente.route) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RectangleShape,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.5f))
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val firstTabWeight by animateFloatAsState(targetValue = if (pagerState.currentPage == 0) 3f else 1f, label = "firstTabWeight")
                        val secondTabWeight by animateFloatAsState(targetValue = if (pagerState.currentPage == 1) 3f else 1f, label = "secondTabWeight")

                        CustomTab(
                            modifier = Modifier.weight(firstTabWeight),
                            selected = pagerState.currentPage == 0,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                            text = "Categorías",
                            activeIcon = "🗂️",
                            inactiveIcon = Icons.Outlined.Folder
                        )

                        VerticalDivider(modifier = Modifier.height(48.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

                        CustomTab(
                            modifier = Modifier.weight(secondTabWeight),
                            selected = pagerState.currentPage == 1,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                            text = "Favoritos",
                            activeIcon = "⭐",
                            inactiveIcon = Icons.Outlined.StarOutline
                        )
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        when (page) {
                            0 -> CategoriesSection(
                                categories = filteredCategories,
                                onNavigateToCategoryResults = { categoryName ->
                                    navController.navigate("result_busqueda/$categoryName")
                                },
                                searchText = searchText,
                                onSearchTextChange = { searchText = it },
                                sortMode = categorySortMode,
                                onSortModeChange = { categorySortMode = it }
                            )
                            1 -> FavoritesBox(
                                onNavigateToProviderProfile = { providerId ->
                                    navController.navigate("perfil_prestador/$providerId")
                                },
                                onNavigateToChat = { providerId ->
                                    navController.navigate("chat_conversation/$providerId")
                                }
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 70.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { navController.navigate(Screen.Promo.route) },
                containerColor = Color(0xFFFFC107),
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.FlashOn, contentDescription = "Promociones Flash")
            }

            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.CrearLicitacion.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Gavel, contentDescription = null) },
                text = { Text("Licitación") }
            )
        }

        AnimatedVisibility(
            visible = isCategoriesFullScreen,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FullScreenCategories(
                onClose = { isCategoriesFullScreen = false },
                onNavigateToCategoryResults = { categoryName ->
                    navController.navigate("result_busqueda/$categoryName")
                }
            )
        }
    }
}

@Composable
fun CustomTab(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    activeIcon: String,
    inactiveIcon: ImageVector
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.2f else 1.0f, label = "scale")
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            if (selected) {
                Text(activeIcon, fontSize = 22.sp)
            } else {
                Icon(inactiveIcon, contentDescription = text, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) Color.Unspecified else Color.Gray, fontSize = if(selected) 16.sp else 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesSection(
    modifier: Modifier = Modifier,
    categories: List<CategoryItem>,
    onNavigateToCategoryResults: (String) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    sortMode: String,
    onSortModeChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Buscar categorías...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            IconButton(
                onClick = { onSortModeChange(if (sortMode == "Alpha") "Random" else "Alpha") },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (sortMode == "Alpha") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.SortByAlpha, contentDescription = "Ordenar Alfabéticamente")
            }

            IconButton(
                onClick = { onSortModeChange(if (sortMode == "Super") "Random" else "Super") },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (sortMode == "Super") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ordenar por Supercategoría")
            }
        }
        
        if (sortMode == "Super") {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val grouped = categories.groupBy { it.superCategory }
                grouped.forEach { (superCategory, list) ->
                    item(span = { GridItemSpan(3) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = superCategory,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    items(list) { category ->
                        CategoryCardItem(category = category, onNavigateToCategoryResults = onNavigateToCategoryResults)
                    }
                }
            }
        } else {
            CategoriesGrid(
                modifier = Modifier.weight(1f),
                categories = categories,
                onNavigateToCategoryResults = onNavigateToCategoryResults
            )
        }
    }
}

@Composable
fun CategoryCardItem(category: CategoryItem, onNavigateToCategoryResults: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) {
                    onNavigateToCategoryResults(category.name)
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = category.color),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = category.icon, fontSize = 40.sp)
            }
        }
        Text(category.name, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun CategoriesGrid(modifier: Modifier = Modifier, categories: List<CategoryItem>, onNavigateToCategoryResults: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            CategoryCardItem(category = category, onNavigateToCategoryResults = onNavigateToCategoryResults)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val availableCategories = remember(allFavorites) {
        allFavorites.flatMap { it.services }.distinct().sorted()
    }

    val filteredFavorites = remember(
        allFavorites, searchText, onlineOnly, works24hOnly, doesHomeVisitsOnly, selectedCategoryFilter
    ) {
        allFavorites.filter { provider ->
            val fullName = "${provider.name} ${provider.lastName}"
            val matchesText = if (searchText.isBlank()) true else
                fullName.split(" ").any { it.startsWith(searchText, ignoreCase = true) }

            val matchesOnline = if (onlineOnly) provider.isOnline else true
            val matches24h = if (works24hOnly) provider.works24h else true
            val matchesHome = if (doesHomeVisitsOnly) provider.doesHomeVisits else true
            val matchesCategory = selectedCategoryFilter == null || provider.services.contains(selectedCategoryFilter)

            matchesText && matchesOnline && matches24h && matchesHome && matchesCategory
        }
    }

    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Fila de búsqueda o filtros
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar en favoritos...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(end = 8.dp),
                        shape = CircleShape,
                        leadingIcon = {
                            IconButton(onClick = {
                                isSearchExpanded = false
                                searchText = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    // Contenedor principal de filtros
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Filtros fijos
                        SmallFilterChip(selected = onlineOnly, onClick = { onlineOnly = !onlineOnly }, label = { Text("Online") }, leadingIcon = { Icon(Icons.Default.Circle, contentDescription = null, tint = if(onlineOnly) Color.Green else Color.Gray, modifier = Modifier.size(8.dp)) })
                        Spacer(modifier = Modifier.width(4.dp))
                        SmallFilterChip(selected = works24hOnly, onClick = { works24hOnly = !works24hOnly }, label = { Text("24hs") }, leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = if(works24hOnly) Color.Green else Color.Gray, modifier = Modifier.size(12.dp)) })
                        Spacer(modifier = Modifier.width(4.dp))
                        SmallFilterChip(selected = doesHomeVisitsOnly, onClick = { doesHomeVisitsOnly = !doesHomeVisitsOnly }, label = { Text("Visitas") }, leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = if(doesHomeVisitsOnly) Color.Green else Color.Gray, modifier = Modifier.size(12.dp)) })
                    }

                    // Botón de categorías (Widget definido con esquinas redondeadas)
                    Box(contentAlignment = Alignment.TopCenter) {
                        val categoryColorMap = remember {
                            CategorySampleDataFalso.categories.associate { it.name to it.color }
                        }
                        val activeColor = if (selectedCategoryFilter != null) {
                            categoryColorMap[selectedCategoryFilter] ?: MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        Surface(
                            modifier = Modifier.padding(end = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCategoryFilter != null) activeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selectedCategoryFilter != null) activeColor.copy(alpha = 0.5f) else Color.Transparent
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (selectedCategoryFilter != null) {
                                            selectedCategoryFilter = null
                                        } else {
                                            showCategoryMenu = true
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedCategoryFilter != null) Icons.Default.Close else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Categorías",
                                    tint = activeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (selectedCategoryFilter != null) {
                                    Text(
                                        text = selectedCategoryFilter!!,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = activeColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.widthIn(max = 50.dp)
                                    )
                                }
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            // Ajuste de posición: se intenta colocar justo debajo del trigger
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp - 32.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Filtrar por Categoría",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                val categoriesWithColors = remember {
                                    CategorySampleDataFalso.categories.associate { it.name to it.color }
                                }
                                
                                // Cuadrícula de 3 columnas
                                availableCategories.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { categoryName ->
                                            val isSelected = selectedCategoryFilter == categoryName
                                            val categoryColor = categoriesWithColors[categoryName] ?: MaterialTheme.colorScheme.primary
                                            
                                            FilterChip(
                                                modifier = Modifier.weight(1f),
                                                selected = isSelected,
                                                onClick = {
                                                    selectedCategoryFilter = if (isSelected) null else categoryName
                                                    showCategoryMenu = false
                                                },
                                                label = {
                                                    Text(
                                                        text = categoryName,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = categoryColor,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                ),
                                                border = null,
                                                shape = CircleShape
                                            )
                                        }
                                        // Completar espacio vacío si la fila tiene menos de 3
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                                
                                if (selectedCategoryFilter != null) {
                                    TextButton(
                                        onClick = {
                                            selectedCategoryFilter = null
                                            showCategoryMenu = false
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Limpiar filtro", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    IconButton(onClick = { isSearchExpanded = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            }
        }

        if (filteredFavorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if(searchText.isNotEmpty() || onlineOnly || works24hOnly || doesHomeVisitsOnly || selectedCategoryFilter != null){
                     Text("No se encontraron coincidencias.", color = Color.Gray)
                } else {
                     Text("No tienes favoritos aún.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredFavorites, key = { it.id }) { provider ->
                    PrestadorCard(
                        provider = provider,
                        onClick = {
                            onNavigateToProviderProfile(provider.id)
                        },
                        onChat = { onNavigateToChat(provider.id) },
                        onDeleteRequest = null,
                        actionContent = {
                            ActionContent(
                                inDeleteMode = false,
                                onMessageClick = { onNavigateToChat(provider.id) },
                                onDeleteRequest = {} // No-op
                            )
                        }
                    )
                }
            }
        }
    }
}


// [NUEVO] Componente auxiliar para Chips Pequeños
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    leadingIcon: @Composable (() -> Unit)?
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            // Estilo de texto más pequeño
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                label()
            }
        },
        leadingIcon = leadingIcon,
        modifier = Modifier.height(32.dp), // Altura fija reducida
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFC8E6C9), // Verde claro activo
            selectedLabelColor = Color(0xFF1B5E20), // Texto verde oscuro activo
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null, // Sin borde por defecto
        shape = CircleShape
    )
}

// --- Componentes Auxiliares Restaurados ---

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullScreenCategories(onClose: () -> Unit, onNavigateToCategoryResults: (String) -> Unit) {
    var sortMode by remember { mutableStateOf("Alphabetical") }

    val groupedCategories = remember(sortMode) {
        when (sortMode) {
            "SuperCategory" -> CategorySampleDataFalso.categories.groupBy { it.superCategory }
            else -> mapOf("" to CategorySampleDataFalso.categories.sortedBy { it.name })
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🗂️", fontSize = 24.sp)
                    Text("Categorías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategorySortChip(
                    text = "Alfabético",
                    icon = Icons.Default.SortByAlpha,
                    isSelected = sortMode == "Alphabetical",
                    onClick = { sortMode = "Alphabetical" }
                )
                CategorySortChip(
                    text = "Super-categoría",
                    icon = Icons.AutoMirrored.Filled.List,
                    isSelected = sortMode == "SuperCategory",
                    onClick = { sortMode = "SuperCategory" }
                )
                CategorySortChip(
                    text = "Aleatorio",
                    icon = null,
                    emoji = "🎲",
                    isSelected = sortMode == "Random",
                    onClick = { sortMode = "Random" }
                )
            }


            if (sortMode == "SuperCategory") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedCategories.forEach { (superCategory, categories) ->
                        item(span = { GridItemSpan(3) }) {
                            Column {
                                Text(
                                    superCategory,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(vertical = 8.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                        items(categories) { category ->
                            CategoryCardItem(category = category, onNavigateToCategoryResults = onNavigateToCategoryResults)
                        }
                    }
                }
            } else {
                 CategoriesGrid(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    categories = if(sortMode == "Random") groupedCategories.values.flatten().shuffled() else groupedCategories.values.flatten(),
                    onNavigateToCategoryResults = onNavigateToCategoryResults
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySortChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector?,
    emoji: String? = null
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            } else if (emoji != null) {
                Text(emoji, fontSize = 16.sp)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color.Green.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeTopSection(
    modifier: Modifier = Modifier,
    userName: String,
    photoUrl: String?,
    hasNotification: Boolean,
    onProfileClick: () -> Unit
) {
    val user = UserSampleDataFalso.findUserByUsername("maxinanterne")
    var selectedLocation by remember { mutableStateOf("Ubicación Actual") }
    val locationDetail = when(selectedLocation) {
        "Home" -> user?.direccionCasa ?: ""
        "Trabajo" -> user?.direccionTrabajo ?: "N/A"
        else -> user?.ciudad ?: ""
    }
    var locationExpanded by remember { mutableStateOf(false) }
    var profileExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Card(shape = RoundedCornerShape(8.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("☀️", fontSize = 18.sp)
                    Text(text = "24°C en ${user?.ciudad}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Column {
                    Row(
                        modifier = Modifier.clickable { locationExpanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", modifier = Modifier.size(18.dp))
                        Text(selectedLocation, modifier = Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    Text(
                        text = locationDetail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }
                DropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Home") },
                        onClick = {
                            selectedLocation = "Home"
                            locationExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )
                    user?.direccionTrabajo?.let {
                        DropdownMenuItem(
                            text = { Text("Trabajo") },
                            onClick = {
                                selectedLocation = "Trabajo"
                                locationExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Ubicación Actual") },
                        onClick = {
                            selectedLocation = "Ubicación Actual"
                            locationExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.combinedClickable(
                onClick = onProfileClick,
                onLongClick = { profileExpanded = true }
            ).padding(8.dp)
        ) {
            Box {
                BadgedBox(
                    badge = {
                        if (hasNotification) {
                            Badge(modifier = Modifier.offset(x = (-8).dp, y = 6.dp))
                        }
                    }
                ) {
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
                DropdownMenu(
                    expanded = profileExpanded,
                    onDismissRequest = { profileExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Configuración") },
                        onClick = { profileExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Notificaciones") },
                        onClick = { profileExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión", color = Color.Red) },
                        onClick = { profileExpanded = false },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
            Text(
                text = userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
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

@Preview(showBackground = true)
@Composable
fun FavoritesBoxPreview() {
    com.example.myapplication.ui.theme.MyApplicationTheme {
        FavoritesBox(
            onNavigateToProviderProfile = {},
            onNavigateToChat = {}
        )
    }
}