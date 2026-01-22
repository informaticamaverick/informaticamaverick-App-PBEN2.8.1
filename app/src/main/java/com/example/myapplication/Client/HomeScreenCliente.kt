package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

// LA DATA CLASS "CategoryItem" SE HA MOVIDO A "CategorySampleDataFalso.kt"

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navItems = listOf(
        Screen.Calendar, Screen.Budget, Screen.Home, Screen.Chats, Screen.Promotions
    )

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                navController = navController,
                navItems = navItems
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavHost(navController = navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenLayout(onNavigateToProfile: () -> Unit, onNavigateToBidding: () -> Unit, navController: NavHostController) {
    var isFavoritesExpanded by remember { mutableStateOf(false) }
    var isCategoriesFullScreen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val userName = "Maximiliano"
    val userProfileUrl: String? = null
    val hasNotification = true

    // Guardamos una versión aleatoria de la lista de categorías
    val randomCategories = remember { CategorySampleDataFalso.categories.shuffled() }

    // Filtramos sobre la lista aleatoria cuando el usuario busca
    val filteredCategories = remember(searchText, randomCategories) {
        if (searchText.isBlank()) {
            randomCategories
        } else {
            randomCategories.filter {
                it.name.startsWith(searchText, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopSection(
                userName = userName,
                photoUrl = userProfileUrl,
                hasNotification = hasNotification,
                onProfileClick = onNavigateToProfile
            )
            // 3. PASAMOS EL ESTADO Y LA FUNCIÓN DE ACTUALIZACIÓN A LA BARRA DE BÚSQUEDA
            SearchAndActionsBox(
                onBiddingClick = onNavigateToBidding,
                searchText = searchText,
                onSearchTextChange = { searchText = it }
            )
            // 4. PASAMOS LA LISTA FILTRADA A LA SECCIÓN DE CATEGORÍAS
            CategoriesSection(
                onExpandClick = { isCategoriesFullScreen = true },
                modifier = Modifier.weight(1f),
                categories = filteredCategories,
                navController = navController
            )
            FavoritesBox(
                isExpanded = isFavoritesExpanded,
                onToggle = { isFavoritesExpanded = !isFavoritesExpanded },
                navController = navController
            )
        }

        if (isCategoriesFullScreen) {
            FullScreenCategories(onClose = { isCategoriesFullScreen = false }, navController = navController)
        }
    }
}

@Composable
fun FullScreenCategories(onClose: () -> Unit, navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Categorias",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
            // LA VISTA DE PANTALLA COMPLETA USA LA LISTA COMPLETA, SIN FILTRAR
            CategoriesGrid(
                modifier = Modifier.padding(horizontal = 16.dp),
                categories = CategorySampleDataFalso.categories,
                navController = navController
            )
        }
    }
}

@Composable
fun CategoriesSection(
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    categories: List<CategoryItem>,
    navController: NavHostController
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Categorias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onExpandClick) {
                Icon(Icons.Default.Fullscreen, contentDescription = "Expandir Categorías")
            }
        }
        // PASA LA LISTA (POSIBLEMENTE FILTRADA) AL GRID
        CategoriesGrid(categories = categories, navController = navController)
    }
}

@Composable
fun SearchAndActionsBox(
    modifier: Modifier = Modifier, 
    onBiddingClick: () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = { Text("Buscar servicios...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(0.6f),
            shape = RoundedCornerShape(50)
        )

        // Action Icons
        Row(
            modifier = Modifier.weight(0.4f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Conectamos el onClick
                IconButton(onClick = onBiddingClick) {
                    Icon(Icons.Default.Description, contentDescription = "Licitaciones")
                }
                Text("Licitaciones", fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Fast")
                }
                Text("Fast", fontSize = 12.sp)
            }
        }
    }
}


// ============================================
// CAMBIOS APLICADOS EN ESTA SECCIÓN
// ============================================
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeTopSection(
    modifier: Modifier = Modifier,
    userName: String,
    photoUrl: String?,
    hasNotification: Boolean,
    onProfileClick: () -> Unit
) {
    var locationExpanded by remember { mutableStateOf(false) }
    var profileExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left Section (Weather and Location)
        Column(horizontalAlignment = Alignment.Start) {
            Card(modifier = Modifier.padding(bottom = 8.dp)) {
                Text("Tarjeta Clima", modifier = Modifier.padding(8.dp))
            }
            Box {
                Row(
                    modifier = Modifier.clickable { locationExpanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Ubicación")
                    Text("Ubicación Actual", modifier = Modifier.padding(start = 4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Home") },
                        onClick = { locationExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Trabajo") },
                        onClick = { locationExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Ubicación Actual") },
                        onClick = { locationExpanded = false },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                    )
                }
            }
        }

        // --- SECCIÓN DE PERFIL MODIFICADA ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // 1. Ahora toda la columna es clickable para ir al perfil
            modifier = Modifier.combinedClickable(
                onClick = onProfileClick, 
                onLongClick = { profileExpanded = true } 
            ).padding(8.dp)
        ) {
            Box {
                // 2. Usamos BadgedBox para el indicador de notificación
                BadgedBox(
                    badge = {
                        if (hasNotification) {
                            Badge(modifier = Modifier.offset(x = (-8).dp, y = 6.dp))
                        }
                    }
                ) {
                    // 2. AsyncImage para mostrar la foto real del usuario
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Perfil",
                        // SOLUCIÓN: Usamos un VectorPainter como fallback, que es más seguro
                        fallback = rememberVectorPainter(image = Icons.Outlined.AccountCircle),
                        modifier = Modifier
                            .size(40.dp)
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
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión", color = Color.Red) },
                        onClick = { profileExpanded = false },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
            // 3. Muestra el nombre real del usuario
            Text(
                text = userName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CategoriesGrid(modifier: Modifier = Modifier, categories: List<CategoryItem>, navController: NavHostController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { 
        items(categories) { category ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.SearchResults.createRoute(category.name)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = category.color)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(category.icon, contentDescription = null, modifier = Modifier.fillMaxSize(0.5f))
                    }
                }
                Text(category.name, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

// ============================================
// FAVORITESBOX AHORA CONECTADO A LA BASE DE DATOS FALSA
// ============================================

@Composable
fun FavoritesBox(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val cornerRadius by animateDpAsState(targetValue = if (isExpanded) 0.dp else 12.dp, label = "")
    
    // SE ACTUALIZA LA REFERENCIA A LA NUEVA FUENTE DE DATOS
    var providers by remember { mutableStateOf(SampleDataFalso.prestadores.filter { it.isFavorite }) }
    
    var showOnlyOnline by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(true) }
    var providerToDelete by remember { mutableStateOf<PrestadorProfileFalso?>(null) }

    // 2. La lógica de filtrado y ordenamiento ahora trabaja sobre la lista de favoritos
    val filteredAndSortedProviders = remember(providers, showOnlyOnline, sortAscending) {
        providers
            .filter { if (showOnlyOnline) it.isOnline else true }
            .let { list ->
                if (sortAscending) list.sortedBy { provider -> "${provider.name} ${provider.lastName}" } 
                else list.sortedByDescending { provider -> "${provider.name} ${provider.lastName}" }
            }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
        tonalElevation = 8.dp
    ) {
        Column {
            // ... (Header sin cambios)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, contentDescription = "Expandir")
            }

            // Expandable content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // --- FILTROS Y ORDENAMIENTO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Botón para ordenar
                        TextButton(onClick = { sortAscending = !sortAscending }) {
                            Text(if (sortAscending) "A-Z" else "Z-A")
                            Icon(Icons.Default.SortByAlpha, contentDescription = "Ordenar por nombre")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Switch para online/offline
                        Text("Online", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = showOnlyOnline,
                            onCheckedChange = { showOnlyOnline = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    // --- LISTA DE FAVORITOS ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(filteredAndSortedProviders, key = { it.id }) { provider ->
                            // 3. Pasamos el nuevo modelo de datos a la tarjeta
                            FavoriteCard(
                                provider = provider,
                                onMessageClick = { /* TODO */ },
                                // AQUÍ CONECTAMOS LA NAVEGACIÓN
                                onProfileClick = { navController.navigate(Screen.ProviderProfile.createRoute(provider.id)) },
                                onDeleteRequest = { providerToDelete = provider }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN PARA ELIMINAR ---
    if (providerToDelete != null) {
        AlertDialog(
            onDismissRequest = { providerToDelete = null },
            title = { Text("Eliminar Favorito") },
            text = { Text("¿Estás seguro de que quieres eliminar a ${providerToDelete!!.name} de tu lista de favoritos?") },
            confirmButton = {
                Button(
                    onClick = {
                        providers = providers.filter { it.id != providerToDelete!!.id }
                        providerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { providerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// SOLUCIÓN 2: AÑADIMOS LA ANOTACIÓN PARA LA API EXPERIMENTAL
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteCard(
    provider: PrestadorProfileFalso,
    onMessageClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    var inDeleteMode by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onProfileClick,
                onLongClick = { inDeleteMode = !inDeleteMode }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = provider.profileImageUrl,
                    contentDescription = "Foto de ${provider.name}",
                    fallback = rememberVectorPainter(image = Icons.Default.Person),
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (provider.isOnline) {
                    Badge(modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(15.dp)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape), 
                        containerColor = Color(0xFF10B981)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            // --- INFORMACIÓN DEL PROVEEDOR ---
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${provider.name} ${provider.lastName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    // --- ÍCONO DE VERIFICADO ---
                    if (provider.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Perfil Verificado",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(provider.services.firstOrNull() ?: "Servicio General", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            // --- ICONO DE ACCIÓN (MENSAJE O PAPELERA) ---
            AnimatedVisibility(visible = inDeleteMode) {
                IconButton(onClick = onDeleteRequest) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(32.dp))
                }
            }
            AnimatedVisibility(visible = !inDeleteMode) {
                 IconButton(onClick = onMessageClick) {
                    Icon(Icons.Default.Email, contentDescription = "Enviar Mensaje", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    navController: NavHostController,
    navItems: List<Screen>
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            // SE AÑADE EL CASO PARA ProviderProfile PARA QUE EL WHEN SEA EXHAUSTIVO
            val icon = when(screen) {
                Screen.Home -> Icons.Default.Home
                Screen.Calendar -> Icons.Default.DateRange
                Screen.Budget -> Icons.Default.AttachMoney
                Screen.Chats -> Icons.Default.Email
                Screen.Promotions -> Icons.Default.Star
                Screen.ClientProfile -> Icons.Default.Person
                Screen.Bidding -> Icons.Default.Description
                // Como ProviderProfile no está en la barra de navegación, podemos usar un ícono genérico.
                is Screen.ProviderProfile -> Icons.Default.Person
                is Screen.SearchResults -> Icons.Default.Search
            }
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = screen.route) },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun ScreenContent(screenName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Pantalla $screenName", fontSize = 24.sp)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
