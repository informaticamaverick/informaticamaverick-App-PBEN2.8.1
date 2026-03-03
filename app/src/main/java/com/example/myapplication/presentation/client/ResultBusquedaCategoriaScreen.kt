package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.components.GeminiLoadingScreen
import com.example.myapplication.presentation.components.GeminiSplitFAB
import com.example.myapplication.presentation.components.PrestadorCard
import com.example.myapplication.presentation.components.geminiGradientEffect
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ==================================================================================
// --- SECCIÓN: PANTALLA RESULTADOS DE BÚSQUEDA ---
// ==================================================================================

/**
 * Pantalla que muestra los prestadores filtrados por una categoría específica.
 * Consume datos de Room a través del ProviderViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    // 🔥 [FLUJO DE DATOS REAL] - Obtenemos todos los prestadores desde Room
    val allProviders by providerViewModel.providers.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.categories.collectAsStateWithLifecycle()

    ResultBusquedaCategoriaContent(
        allProviders = allProviders,
        allCategories = allCategories,
        categoryName = categoryName,
        onBack = onBack,
        onNavigateToProviderProfile = onNavigateToProviderProfile,
        onNavigateToChat = onNavigateToChat,
        isProvider24h = { provider -> providerViewModel.isProvider24h(provider) },
        doesProviderHomeVisits = { provider -> providerViewModel.doesProviderHomeVisits(provider) },
        hasProviderPhysicalLocation = { provider -> providerViewModel.hasProviderPhysicalLocation(provider) },
        toggleFavoriteStatus = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaContent(
    allProviders: List<Provider>,
    allCategories: List<CategoryEntity>,
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    isProvider24h: (Provider) -> Boolean,
    doesProviderHomeVisits: (Provider) -> Boolean,
    hasProviderPhysicalLocation: (Provider) -> Boolean,
    toggleFavoriteStatus: (String, Boolean) -> Unit
) {
    // --- ESTADOS DE UI PARA FILTROS ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    var subscribedOnly by remember { mutableStateOf(true) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") }

    var isFabMenuExpanded by remember { mutableStateOf(false) }

    // --- NUEVO: ESTADO DE CARGA INTELIGENTE (SENSADO DE DATOS) ---
    var minimumWaitDone by remember { mutableStateOf(false) }
    var timeoutDone by remember { mutableStateOf(false) }

    LaunchedEffect(categoryName) {
        minimumWaitDone = false
        timeoutDone = false

        delay(1200) // Tiempo mínimo garantizado para ver la animación orbital premium
        minimumWaitDone = true

        delay(1800) // Si la BD demora más, damos 1.8s extras (3s total) antes de forzar el timeout
        timeoutDone = true
    }

    // Lógica inteligente: Sabemos que hay datos si Room nos entregó una lista no vacía.
    val isDataLoaded = allProviders.isNotEmpty()

    // Mostramos el loading mientras: No pase el tiempo mínimo O (pasó el mínimo, pero NO hay datos Y aún no pasó el timeout).
    val showLoadingScreen = !minimumWaitDone || (!isDataLoaded && !timeoutDone)

    // Obtenemos la categoría seleccionada para el encabezado dinámico
    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }

    val closeSearch: () -> Unit = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
    }

    // 🔥 [LÓGICA DE FILTRADO REAL]
    val filteredList = remember(allProviders, categoryName, searchQuery, subscribedOnly, verifiedOnly, works24hOnly, homeVisitsOnly, physicalLocationOnly, sortOrder) {
        allProviders
            .filter { p ->
                // Filtro 1: Categoría (Coincidencia exacta en lista de categorías del proveedor o de sus empresas)
                p.categories.any { it.equals(categoryName, ignoreCase = true) } ||
                        p.companies.any { it.categories.any { s -> s.equals(categoryName, ignoreCase = true) } }
            }
            .filter { p ->
                // Filtro 2: Búsqueda por texto en nombre o categorías de empresa
                if (searchQuery.isNotEmpty()) {
                    p.name.contains(searchQuery, ignoreCase = true) ||
                            p.lastName.contains(searchQuery, ignoreCase = true) ||
                            p.companies.any { it.categories.any { s -> s.contains(searchQuery, ignoreCase = true) } }
                } else true
            }
            .filter { p -> if (subscribedOnly) p.isSubscribed else true }
            .filter { p -> if (verifiedOnly) p.isVerified else true }
            .filter { p -> if (works24hOnly) isProvider24h(p) else true }
            .filter { p -> if (homeVisitsOnly) doesProviderHomeVisits(p) else true }
            .filter { p -> if (physicalLocationOnly) hasProviderPhysicalLocation(p) else true }
            // Ordenamiento
            .let { list ->
                if (sortOrder == "Rating") list.sortedByDescending { it.rating }
                else list.sortedBy { it.name }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (!isSearchActive) {
                    ResultHeaderSection(
                        category = selectedCategory,
                        categoryName = categoryName,
                        onBack = onBack
                    )
                }

                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
                    modifier = Modifier.zIndex(20f)
                ) {
                    TopSearchBarResult(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onCancel = closeSearch
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Solo mostramos el texto de recuento si YA terminó de cargar
                if (!showLoadingScreen && filteredList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if(subscribedOnly) "Recomendados" else "Todos los resultados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${filteredList.size}", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }

                // Muestra la Pantalla de Carga Inteligente o la Lista
                if (showLoadingScreen) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        GeminiLoadingScreen(text = "BUSCANDO PRESTADORES...")
                    }
                } else {
                    ProviderListContent(
                        professionals = filteredList,
                        allCategories = allCategories,
                        onNavigateToProviderProfile = onNavigateToProviderProfile,
                        onNavigateToChat = onNavigateToChat,
                        onToggleFavorite = { id, isFav -> toggleFavoriteStatus(id, isFav) }
                    )
                }
            }

            // --- SECCIÓN: FAB DIVIDIDO GEMINI ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabMenuExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                    onActivateSearch = { isSearchActive = true },
                    onCloseSearch = closeSearch
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: BARRA DE BÚSQUEDA ---
// ==================================================================================

@Composable
fun TopSearchBarResult(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    val rainbowBrush = geminiGradientEffect()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp),
        color = Color(0xFF121212),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 12.dp,
        border = BorderStroke(2.5.dp, rainbowBrush)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                null,
                tint = Color.White.copy(0.8f),
                modifier = Modifier.padding(start = 20.dp).size(20.dp)
            )
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier.weight(1f).padding(start = 12.dp).focusRequester(focusRequester),
                textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) Text("Buscar en esta categoría...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        innerTextField()
                    }
                }
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE LISTA ---
// ==================================================================================

@Composable
fun ProviderListContent(
    professionals: List<Provider>,
    allCategories: List<CategoryEntity>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 1.dp, start = 1.dp, end = 0.dp, bottom = 80.dp),
        ) {
            items(professionals, key = { it.id }) { professional ->
                Column {
                    PrestadorCard(
                        provider = professional,
                        onClick = { onNavigateToProviderProfile(professional.id) },
                        onChat = { onNavigateToChat(professional.id) },
                        onToggleFavorite = onToggleFavorite,
                        allCategories = allCategories
                    )
                }
            }
        }
    }
}

@Composable
fun ResultHeaderSection(
    category: CategoryEntity?,
    categoryName: String,
    onBack: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text(text = "Sobre esta pantalla") },
            text = {
                Text(
                    "Aquí encontrarás una lista de profesionales verificados en la categoría seleccionada.\n\n" +
                            "• Usa la lupa para buscar por nombre.\n" +
                            "• Usa el menú de herramientas para filtrar por ubicación, horario y más.\n" +
                            "• Los prestadores 'Recomendados' aparecen primero."
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    val baseColor = category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.surface

    Surface(
        color = baseColor,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(80.dp) // Un poco más alto para replicar el estilo de la tarjeta
        ) {
            // 1. Gradiente de superposición (Replicado de CompactCategoryCard)
            Box(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    val gradient = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(gradient, blendMode = BlendMode.Overlay)
                    }
                }
            )

            // 2. Gradiente horizontal oscuro para legibilidad
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        startX = 0f,
                        endX = 600f
                    )
                )
            )

            // 3. Icono de categoría en el fondo (Derecha)
            category?.icon?.let { iconEmoji ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = iconEmoji,
                        fontSize = 100.sp,
                        modifier = Modifier
                            .offset(x = 20.dp)
                            .graphicsLayer(alpha = 1f)
                    )
                }
            }

            // 4. Contenido Principal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = categoryName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { showInfoDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Info, "Información")
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No se encontraron prestadores", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            Text("Intenta ajustar los filtros de búsqueda.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// --- [SECCIÓN: HELPERS DE FILTRADO] ---

fun isProvider24h(provider: Provider): Boolean =
    provider.companies.any { it.works24h }

fun doesProviderHomeVisits(provider: Provider): Boolean =
    provider.companies.any { it.doesHomeVisits }

fun hasProviderPhysicalLocation(provider: Provider): Boolean =
    provider.companies.any { it.hasPhysicalLocation }

@Preview(showBackground = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    val sampleProviders = listOf(
        Provider(
            uid = "1",
            email = "provider1@example.com",
            displayName = "Juan Perez",
            name = "Juan",
            lastName = "Perez",
            phoneNumber = "123456789",
            categories = listOf("Informatica"),
            matricula = "12345",
            titulo = "Técnico en PC",
            cuilCuit = "20-12345678-9",
            address = null,
            photoUrl = "",
            bannerImageUrl = "",
            hasCompanyProfile = true,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = false,
            rating = 4.5f,
            createdAt = System.currentTimeMillis(),
            companies = listOf(
                CompanyProvider(
                    name = "PC Solutions",
                    categories = listOf("Informatica", "Reparación de PC"),
                    works24h = true,
                    doesHomeVisits = true,
                    hasPhysicalLocation = true
                )
            )
        ),
        Provider(
            uid = "2",
            email = "provider2@example.com",
            displayName = "Maria Lopez",
            name = "Maria",
            lastName = "Lopez",
            phoneNumber = "987654321",
            categories = listOf("Informatica"),
            matricula = "54321",
            titulo = "Desarrolladora Web",
            cuilCuit = "27-98765432-1",
            address = null,
            photoUrl = "",
            bannerImageUrl = "",
            hasCompanyProfile = true,
            isSubscribed = false,
            isVerified = true,
            isOnline = false,
            isFavorite = true,
            rating = 4.8f,
            createdAt = System.currentTimeMillis(),
            companies = listOf(
                CompanyProvider(
                    name = "Web Experts",
                    categories = listOf("Informatica", "Desarrollo Web"),
                    works24h = false,
                    doesHomeVisits = false,
                    hasPhysicalLocation = true
                )
            )
        )
    )

    MyApplicationTheme {
        ResultBusquedaCategoriaContent(
            allProviders = sampleProviders,
            allCategories = listOf(
                CategoryEntity(
                    name = "Informatica",
                    icon = "💻",
                    color = 0xFF2197F5,
                    superCategory = "Tecnología",
                    providerIds = listOf("1", "2"),
                    imageUrl = null,
                    isNew = true,
                    isNewPrestador = false,
                    isAd = false
                )
            ),
            categoryName = "Informatica",
            onBack = {},
            onNavigateToProviderProfile = {},
            onNavigateToChat = {},
            isProvider24h = ::isProvider24h,
            doesProviderHomeVisits = ::doesProviderHomeVisits,
            hasProviderPhysicalLocation = ::hasProviderPhysicalLocation,
            toggleFavoriteStatus = { _, _ -> }
        )
    }
}

/**
package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.components.GeminiSplitFAB
import com.example.myapplication.presentation.components.PrestadorCard
import com.example.myapplication.presentation.components.geminiGradientEffect
import androidx.compose.foundation.interaction.MutableInteractionSource

// --- [SECCIÓN: MODELOS DE DATOS REALES] ---
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ==================================================================================
// --- SECCIÓN: PANTALLA RESULTADOS DE BÚSQUEDA ---
// ==================================================================================

/**
 * Pantalla que muestra los prestadores filtrados por una categoría específica.
 * [ACTUALIZADO] Consume datos de Room a través del ProviderViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    // 🔥 [FLUJO DE DATOS REAL] - Obtenemos todos los prestadores desde Room
    val allProviders by providerViewModel.providers.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.categories.collectAsStateWithLifecycle()

    ResultBusquedaCategoriaContent(
        allProviders = allProviders,
        allCategories = allCategories,
        categoryName = categoryName,
        onBack = onBack,
        onNavigateToProviderProfile = onNavigateToProviderProfile,
        onNavigateToChat = onNavigateToChat,
        isProvider24h = { provider -> providerViewModel.isProvider24h(provider) },
        doesProviderHomeVisits = { provider -> providerViewModel.doesProviderHomeVisits(provider) },
        hasProviderPhysicalLocation = { provider -> providerViewModel.hasProviderPhysicalLocation(provider) },
        toggleFavoriteStatus = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaContent(
    allProviders: List<Provider>,
    allCategories: List<CategoryEntity>,
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    isProvider24h: (Provider) -> Boolean,
    doesProviderHomeVisits: (Provider) -> Boolean,
    hasProviderPhysicalLocation: (Provider) -> Boolean,
    toggleFavoriteStatus: (String, Boolean) -> Unit
) {
    // --- ESTADOS DE UI PARA FILTROS ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    var subscribedOnly by remember { mutableStateOf(true) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") }

    var isFabMenuExpanded by remember { mutableStateOf(false) }

    // Obtenemos la categoría seleccionada para el encabezado dinámico
    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }

    // [CORREGIDO] Lambda con tipo explícito para evitar error Function0<Unit?>
    val closeSearch: () -> Unit = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
    }

    // 🔥 [LÓGICA DE FILTRADO REAL]
    val filteredList = remember(allProviders, categoryName, searchQuery, subscribedOnly, verifiedOnly, works24hOnly, homeVisitsOnly, physicalLocationOnly, sortOrder) {
        allProviders
            .filter { p ->
                // Filtro 1: Categoría (Coincidencia exacta en lista de categorías del proveedor o de sus empresas)
                p.categories.any { it.equals(categoryName, ignoreCase = true) } ||
                        p.companies.any { it.categories.any { s -> s.equals(categoryName, ignoreCase = true) } }
            }
            .filter { p ->
                // Filtro 2: Búsqueda por texto en nombre o categorías de empresa
                if (searchQuery.isNotEmpty()) {
                    p.name.contains(searchQuery, ignoreCase = true) ||
                            p.lastName.contains(searchQuery, ignoreCase = true) ||
                            p.companies.any { it.categories.any { s -> s.contains(searchQuery, ignoreCase = true) } }
                } else true
            }
            .filter { p -> if (subscribedOnly) p.isSubscribed else true }
            .filter { p -> if (verifiedOnly) p.isVerified else true }
            .filter { p -> if (works24hOnly) isProvider24h(p) else true }
            .filter { p -> if (homeVisitsOnly) doesProviderHomeVisits(p) else true }
            .filter { p -> if (physicalLocationOnly) hasProviderPhysicalLocation(p) else true }
            // Ordenamiento
            .let { list ->
                if (sortOrder == "Rating") list.sortedByDescending { it.rating }
                else list.sortedBy { it.name }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                if (!isSearchActive) {
                    ResultHeaderSection(
                        category = selectedCategory,
                        categoryName = categoryName,
                        onBack = onBack
                    )
                }

                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
                    modifier = Modifier.zIndex(20f)
                ) {
                    TopSearchBarResult(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onCancel = closeSearch
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (filteredList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if(subscribedOnly) "Recomendados" else "Todos los resultados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text("${filteredList.size}", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
                HorizontalDivider(  )
                // 🔥 Lista Real
                ProviderListContent(
                    professionals = filteredList,
                    allCategories = allCategories,
                    onNavigateToProviderProfile = onNavigateToProviderProfile,
                    onNavigateToChat = onNavigateToChat,
                    onToggleFavorite = { id, isFav -> toggleFavoriteStatus(id, isFav) }
                )
            }

            // --- SECCIÓN: FAB DIVIDIDO GEMINI ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabMenuExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                    onActivateSearch = { isSearchActive = true },
                    onCloseSearch = closeSearch
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: BARRA DE BÚSQUEDA ---
// ==================================================================================

@Composable
fun TopSearchBarResult(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    
    val rainbowBrush = geminiGradientEffect()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp),
        color = Color(0xFF121212),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 12.dp,
        border = BorderStroke(2.5.dp, rainbowBrush)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search, 
                null, 
                tint = Color.White.copy(0.8f), 
                modifier = Modifier.padding(start = 20.dp).size(20.dp)
            )
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier.weight(1f).padding(start = 12.dp).focusRequester(focusRequester),
                textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) Text("Buscar en esta categoría...", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        innerTextField()
                    }
                }
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE LISTA ---
// ==================================================================================

@Composable
fun ProviderListContent(
    professionals: List<Provider>,
    allCategories: List<CategoryEntity>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 1.dp, start = 1.dp, end = 0.dp, bottom = 80.dp),
        ) {
            items(professionals, key = { it.id }) { professional ->
                Column {
                    PrestadorCard(
                        provider = professional,
                        onClick = { onNavigateToProviderProfile(professional.id) },
                        onChat = { onNavigateToChat(professional.id) },
                        onToggleFavorite = onToggleFavorite,
                        allCategories = allCategories
                    )
                }
            }
        }
    }
}

@Composable
fun ResultHeaderSection(
    category: CategoryEntity?,
    categoryName: String,
    onBack: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text(text = "Sobre esta pantalla") },
            text = {
                Text(
                    "Aquí encontrarás una lista de profesionales verificados en la categoría seleccionada.\n\n" +
                    "• Usa la lupa para buscar por nombre.\n" +
                    "• Usa el menú de herramientas para filtrar por ubicación, horario y más.\n" +
                    "• Los prestadores 'Recomendados' aparecen primero."
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    val baseColor = category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.surface

    Surface(
        color = baseColor,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(80.dp) // Un poco más alto para replicar el estilo de la tarjeta
        ) {
            // 1. Gradiente de superposición (Replicado de CompactCategoryCard)
            Box(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    val gradient = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(gradient, blendMode = BlendMode.Overlay)
                    }
                }
            )

            // 2. Gradiente horizontal oscuro para legibilidad
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        startX = 0f,
                        endX = 600f
                    )
                )
            )

            // 3. Icono de categoría en el fondo (Derecha)
            category?.icon?.let { iconEmoji ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = iconEmoji,
                        fontSize = 100.sp,
                        modifier = Modifier
                            .offset(x = 20.dp)
                            .graphicsLayer(alpha = 1f)
                    )
                }
            }

            // 4. Contenido Principal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = categoryName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { showInfoDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Info, "Información")
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text("No hay resultados con estos filtros 🔍", textAlign = TextAlign.Center)
    }
}

// --- [SECCIÓN: HELPERS DE FILTRADO] ---

fun isProvider24h(provider: Provider): Boolean =
    provider.companies.any { it.works24h }

fun doesProviderHomeVisits(provider: Provider): Boolean =
    provider.companies.any { it.doesHomeVisits }

fun hasProviderPhysicalLocation(provider: Provider): Boolean =
    provider.companies.any { it.hasPhysicalLocation }

@Preview(showBackground = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    val sampleProviders = listOf(
        Provider(
            uid = "1",
            email = "provider1@example.com",
            displayName = "Juan Perez",
            name = "Juan",
            lastName = "Perez",
            phoneNumber = "123456789",
            categories = listOf("Informatica"),
            matricula = "12345",
            titulo = "Técnico en PC",
            cuilCuit = "20-12345678-9",
            address = null,
            photoUrl = "",
            bannerImageUrl = "",
            hasCompanyProfile = true,
            isSubscribed = true,
            isVerified = true,
            isOnline = true,
            isFavorite = false,
            rating = 4.5f,
            createdAt = System.currentTimeMillis(),
            companies = listOf(
                CompanyProvider(
                    name = "PC Solutions",
                    categories = listOf("Informatica", "Reparación de PC"),
                    works24h = true,
                    doesHomeVisits = true,
                    hasPhysicalLocation = true
                )
            )
        ),
        Provider(
            uid = "2",
            email = "provider2@example.com",
            displayName = "Maria Lopez",
            name = "Maria",
            lastName = "Lopez",
            phoneNumber = "987654321",
            categories = listOf("Informatica"),
            matricula = "54321",
            titulo = "Desarrolladora Web",
            cuilCuit = "27-98765432-1",
            address = null,
            photoUrl = "",
            bannerImageUrl = "",
            hasCompanyProfile = true,
            isSubscribed = false,
            isVerified = true,
            isOnline = false,
            isFavorite = true,
            rating = 4.8f,
            createdAt = System.currentTimeMillis(),
            companies = listOf(
                CompanyProvider(
                    name = "Web Experts",
                    categories = listOf("Informatica", "Desarrollo Web"),
                    works24h = false,
                    doesHomeVisits = false,
                    hasPhysicalLocation = true
                )
            )
        )
    )

    MyApplicationTheme {
        ResultBusquedaCategoriaContent(
            allProviders = sampleProviders,
            allCategories = listOf(
                CategoryEntity(
                    name = "Informatica",
                    icon = "💻",
                    color = 0xFF2197F5,
                    superCategory = "Tecnología",
                    providerIds = listOf("1", "2"),
                    imageUrl = null,
                    isNew = true,
                    isNewPrestador = false,
                    isAd = false
                )
            ),
            categoryName = "Informatica",
            onBack = {},
            onNavigateToProviderProfile = {},
            onNavigateToChat = {},
            isProvider24h = ::isProvider24h,
            doesProviderHomeVisits = ::doesProviderHomeVisits,
            hasProviderPhysicalLocation = ::hasProviderPhysicalLocation,
            toggleFavoriteStatus = { _, _ -> }
        )
    }
}
**/