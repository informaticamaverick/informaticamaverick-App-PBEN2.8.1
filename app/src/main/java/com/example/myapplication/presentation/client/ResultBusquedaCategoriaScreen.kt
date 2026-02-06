package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.components.GeminiSplitFAB
import com.example.myapplication.presentation.components.PrestadorCard
import com.example.myapplication.presentation.components.SmallFabTool
import com.example.myapplication.presentation.components.geminiGradientEffect
import androidx.compose.foundation.interaction.MutableInteractionSource

// --- [COMENTADO] IMPORTACIONES DE DATOS FALSOS ---
// import com.example.myapplication.data.model.fake.CategorySampleDataFalso
// import com.example.myapplication.data.model.fake.SampleDataFalso
// import com.example.myapplication.data.model.fake.UserFalso

// --- [SECCIÓN: MODELOS DE DATOS REALES] ---
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
    providerViewModel: ProviderViewModel = hiltViewModel()
) {
    // 🔥 [FLUJO DE DATOS REAL] - Obtenemos todos los prestadores desde Room
    val allProviders by providerViewModel.providers.collectAsStateWithLifecycle()

    ResultBusquedaCategoriaContent(
        allProviders = allProviders,
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
                // Filtro 1: Categoría (Coincidencia exacta o en lista de servicios)
                p.category.equals(categoryName, ignoreCase = true) ||
                        p.companies.any { it.services.any { s -> s.equals(categoryName, ignoreCase = true) } }
            }
            .filter { p ->
                // Filtro 2: Búsqueda por texto en nombre o servicios
                if (searchQuery.isNotEmpty()) {
                    p.name.contains(searchQuery, ignoreCase = true) ||
                            p.lastName.contains(searchQuery, ignoreCase = true) ||
                            p.companies.any { it.services.any { s -> s.contains(searchQuery, ignoreCase = true) } }
                } else true
            }
            // [CORREGIDO] Se usa 'p' para evitar conflictos con clases internas.
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
                        categoryName = categoryName,
                        onBack = onBack
                    )
                }

                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
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

                // 🔥 Lista Real
                ProviderListContent(
                    professionals = filteredList,
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
                    onCloseSearch = closeSearch,
                    expandedTools = {
                        SmallFabTool(
                            label = if (subscribedOnly) "Top" else "Todos",
                            icon = if (subscribedOnly) Icons.Default.WorkspacePremium else Icons.Default.Group,
                            isSelected = subscribedOnly,
                            onClick = { subscribedOnly = !subscribedOnly }
                        )
                        SmallFabTool(
                            label = if (sortOrder == "Rating") "Rating" else "Nombre",
                            icon = if (sortOrder == "Rating") Icons.Default.Star else Icons.AutoMirrored.Filled.Sort,
                            isSelected = true,
                            onClick = {
                                sortOrder = if (sortOrder == "Rating") "Name" else "Rating"
                            }
                        )
                    }
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
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 100.dp),
        ) {
            items(professionals, key = { it.id }) { professional ->
                Column {
                    PrestadorCard(
                        provider = professional,
                        onClick = { onNavigateToProviderProfile(professional.id) },
                        onChat = { onNavigateToChat(professional.id) },
                        onToggleFavorite = onToggleFavorite
                    )

                    /**
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 1.dp, bottom = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                    **/
                }
            }
        }
    }
}

@Composable
fun ResultHeaderSection(
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

   Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().zIndex(10f)
    ) {
        Row(
            modifier = Modifier.statusBarsPadding().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(categoryName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showInfoDialog = true }) {
                Icon(Icons.Default.Info, "Información")
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
            category = "Informatica",
            matricula = "12345",
            titulo = "Técnico en PC",
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
                    services = listOf("Informatica", "Reparación de PC"),
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
            category = "Informatica",
            matricula = "54321",
            titulo = "Desarrolladora Web",
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
                    services = listOf("Informatica", "Desarrollo Web"),
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