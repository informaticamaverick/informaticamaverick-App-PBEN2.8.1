/**  package com.example.myapplication.Client



import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenCliente(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var searchText by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var shuffledGroupedCategories by remember { mutableStateOf<List<Pair<String, List<CategoryItem>>>>(emptyList()) }
    
    var isSearchActive by remember { mutableStateOf(false) }
    var isToolsActive by remember { mutableStateOf(false) }

    val shuffleData = { shuffledGroupedCategories = CategorySampleDataFalso.categories.groupBy { it.superCategory }.mapValues { it.value.shuffled() }.toList().shuffled() }
    LaunchedEffect(Unit) { shuffleData() }

    val density = LocalDensity.current
    val panelWidth = 340.dp
    val panelWidthPx = with(density) { panelWidth.toPx() }
    val panelOffset = remember { Animatable(panelWidthPx) }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- CONTENIDO PRINCIPAL ---
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopSection(userName = uiState.displayName, photoUrl = uiState.photoUrl, hasNotification = true, onProfileClick = { navController.navigate(Screen.PerfilCliente.route) })
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { scope.launch { isRefreshing = true; delay(1000); shuffleData(); isRefreshing = false } },
                modifier = Modifier.weight(1f)
            ) {
                CategoriesCarouselSection(
                    shuffledGroups = shuffledGroupedCategories, searchText = searchText,
                    onNavigateToCategoryResults = { categoryName -> navController.navigate("result_busqueda/$categoryName") }
                )
            }
        }

        // --- PANEL LATERAL FAVORITOS ---
        Surface(
            modifier = Modifier.width(panelWidth).fillMaxHeight().align(Alignment.CenterEnd).offset { IntOffset(panelOffset.value.roundToInt(), 0) }.draggable(orientation = Orientation.Horizontal, state = rememberDraggableState { delta -> scope.launch { panelOffset.snapTo((panelOffset.value + delta).coerceIn(0f, panelWidthPx)) } }, onDragStopped = { scope.launch { if (panelOffset.value < panelWidthPx / 2) panelOffset.animateTo(0f) else panelOffset.animateTo(panelWidthPx) } }),
            color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, shadowElevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
        ) {
            FavoritesBox(
                onNavigateToProviderProfile = { providerId -> navController.navigate("perfil_prestador/$providerId"); scope.launch { panelOffset.animateTo(panelWidthPx) } },
                onNavigateToChat = { providerId -> navController.navigate("chat_conversation/$providerId"); scope.launch { panelOffset.animateTo(panelWidthPx) } }
            )
        }

        // --- SCRIM (FONDO OSCURO) ---
        if (isToolsActive) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { isToolsActive = false })
        }

        // --- CAPA DEL FAB ANIMADO ---
        val alignment by animateAlignmentAsState(if (isSearchActive) Alignment.TopCenter else Alignment.BottomEnd)

        Box(modifier = Modifier.fillMaxSize().padding(16.dp).navigationBarsPadding().imePadding(), contentAlignment = alignment) {
            SplitFloatingActionButton(
                isSearchExpanded = isSearchActive,
                isToolsExpanded = isToolsActive,
                onSearchClick = { active -> isSearchActive = active; if (active) isToolsActive = false },
                onToolsClick = { active -> isToolsActive = active; if (active) isSearchActive = false },
                searchContent = {
                    BasicTextField(
                        value = searchText, onValueChange = { searchText = it }, modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) Text("¿Qué servicio buscas?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            innerTextField()
                        }
                    )
                },
                horizontalTools = {
                    ToolItem(icon = Icons.Default.Star, label = "Favoritos", color = Color(0xFFFFDE03)) { scope.launch { panelOffset.animateTo(if (panelOffset.value >= panelWidthPx) panelWidthPx else 0f); isToolsActive = false } }
                    ToolItem(icon = Icons.Default.FlashOn, label = "Flash", color = Color(0xFFFF9800)) { navController.navigate(Screen.Promo.route); isToolsActive = false }
                },
                verticalTools = {
                    ToolItem(icon = Icons.Default.Gavel, label = "Licitación", color = MaterialTheme.colorScheme.tertiaryContainer) { navController.navigate(Screen.CrearLicitacion.route); isToolsActive = false }
                }
            )
        }
    }
}

@Composable
private fun animateAlignmentAsState(targetAlignment: Alignment): State<Alignment> {
    val biased = targetAlignment as BiasAlignment
    val horizontalBias by animateFloatAsState(biased.horizontalBias, label = "hBias")
    val verticalBias by animateFloatAsState(biased.verticalBias, label = "vBias")
    return remember { derivedStateOf { BiasAlignment(horizontalBias, verticalBias) } }
}

// El resto de componentes (HomeTopSection, CategoriesCarouselSection, etc.) continúan aquí
@Composable
fun SquareFabWithLabel(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scaleAnim", animationSpec = spring())

    Surface(
        onClick = onClick,
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.size(width = 64.dp, height = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoriesCarouselSection(
    shuffledGroups: List<Pair<String, List<CategoryItem>>>,
    searchText: String,
    onNavigateToCategoryResults: (String) -> Unit
) {
    val searchResults = remember(searchText, shuffledGroups) {
        if (searchText.isBlank()) emptyList()
        else {
            val allCategories = shuffledGroups.flatMap { it.second }
            allCategories.filter { it.name.split(" ").any { word -> word.startsWith(searchText, ignoreCase = true) } }
        }
    }

    if (searchText.isNotBlank()) {
        if (searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No se encontraron resultados", color = Color.Gray) }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(searchResults, key = { it.name }) { category -> CategoryCarouselItem(category = category, onNavigateToCategoryResults = onNavigateToCategoryResults, modifier = Modifier.aspectRatio(1f)) }
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)) {
            item { Text(text = "Busca el Servicio que quieras", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            itemsIndexed(shuffledGroups) { _, (superCategory, list) ->
                SuperCategoryCarousel(title = superCategory, items = list, onNavigateToCategoryResults = onNavigateToCategoryResults)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuperCategoryCarousel(title: String, items: List<CategoryItem>, onNavigateToCategoryResults: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1.4f))
        }
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 2.dp, pageSize = androidx.compose.foundation.pager.PageSize.Fixed(110.dp), beyondViewportPageCount = 3) { page ->
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            CategoryCarouselItem(category = items[page], onNavigateToCategoryResults = onNavigateToCategoryResults, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }.size(110.dp))
        }
    }
}

@Composable
fun CategoryCarouselItem(category: CategoryItem, onNavigateToCategoryResults: (String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "press")

    Card(shape = RoundedCornerShape(20.dp), modifier = modifier.fillMaxWidth().scale(pressScale).clickable(interactionSource = interactionSource, indication = null, onClick = { onNavigateToCategoryResults(category.name) }), colors = CardDefaults.cardColors(containerColor = category.color), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = category.icon, fontSize = 60.sp, modifier = Modifier.align(Alignment.Center).offset(y = (-8).dp))
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)), startY = 50f)))
            Text(text = category.name, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp, start = 4.dp, end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesBox(modifier: Modifier = Modifier, onNavigateToProviderProfile: (String) -> Unit, onNavigateToChat: (String) -> Unit) {
    val allFavorites = SampleDataFalso.prestadores.filter { it.isFavorite }
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var onlineOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var doesHomeVisitsOnly by remember { mutableStateOf(false) }

    val filteredFavorites = remember(allFavorites, searchText, onlineOnly, works24hOnly, doesHomeVisitsOnly) {
        allFavorites.filter { provider ->
            val fullName = "${provider.name} ${provider.lastName}"
            val matchesText = if (searchText.isBlank()) true else fullName.split(" ").any { it.startsWith(searchText, ignoreCase = true) }
            val matchesOnline = if (onlineOnly) provider.isOnline else true
            val matches24h = if (works24hOnly) provider.works24h else true
            val matchesHome = if (doesHomeVisitsOnly) provider.doesHomeVisits else true
            matchesText && matchesOnline && matches24h && matchesHome
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (isSearchExpanded) {
                    OutlinedTextField(value = searchText, onValueChange = { searchText = it }, placeholder = { Text("Buscar...") }, modifier = Modifier.weight(1f).height(48.dp), shape = CircleShape, leadingIcon = { IconButton(onClick = { isSearchExpanded = false; searchText = "" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }, singleLine = true)
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
        if (filteredFavorites.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aún no tienes favoritos.", color = Color.Gray) } }
        else { LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(filteredFavorites) { provider -> PrestadorCard(provider = provider, onClick = { onNavigateToProviderProfile(provider.id) }, onChat = { onNavigateToChat(provider.id) }, actionContent = { ActionContent(inDeleteMode = false, onMessageClick = { onNavigateToChat(provider.id) }, onDeleteRequest = {}) }) } } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallFilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit, leadingIcon: @Composable (() -> Unit)?) {
    FilterChip(selected = selected, onClick = onClick, label = { ProvideTextStyle(value = MaterialTheme.typography.labelSmall) { label() } }, leadingIcon = leadingIcon, modifier = Modifier.height(32.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC8E6C9), selectedLabelColor = Color(0xFF1B5E20), containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), labelColor = MaterialTheme.colorScheme.onSurfaceVariant), border = null, shape = CircleShape)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeTopSection(modifier: Modifier = Modifier, userName: String, photoUrl: String?, hasNotification: Boolean, onProfileClick: () -> Unit) {
    val user = UserSampleDataFalso.findUserByUsername("maxinanterne")
    var selectedLocation by remember { mutableStateOf("Ubicación Actual") }
    val locationDetail = when(selectedLocation) { "Home" -> user?.direccionCasa ?: ""; "Trabajo" -> user?.direccionTrabajo ?: "N/A"; else -> user?.ciudad ?: "" }
    var locationExpanded by remember { mutableStateOf(false) }
    var profileExpanded by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
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
                    Text(text = locationDetail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 22.dp))
                }
                DropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                    DropdownMenuItem(text = { Text("Home") }, onClick = { selectedLocation = "Home"; locationExpanded = false }, leadingIcon = { Icon(Icons.Default.Home, null) })
                    user?.direccionTrabajo?.let { DropdownMenuItem(text = { Text("Trabajo") }, onClick = { selectedLocation = "Trabajo"; locationExpanded = false }, leadingIcon = { Icon(Icons.Default.Build, null) }) }
                    DropdownMenuItem(text = { Text("Ubicación Actual") }, onClick = { selectedLocation = "Ubicación Actual"; locationExpanded = false }, leadingIcon = { Icon(Icons.Default.Place, null) })
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.combinedClickable(onClick = onProfileClick, onLongClick = { profileExpanded = true }).padding(8.dp)) {
            Box {
                BadgedBox(badge = { if (hasNotification) Badge(modifier = Modifier.offset(x = (-8).dp, y = 6.dp)) }) {
                    AsyncImage(model = photoUrl, contentDescription = "Perfil", fallback = rememberVectorPainter(image = Icons.Outlined.AccountCircle), modifier = Modifier.size(45.dp).clip(CircleShape), contentScale = ContentScale.Crop)
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
**/