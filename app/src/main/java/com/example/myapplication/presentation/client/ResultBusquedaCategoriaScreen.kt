package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.myapplication.presentation.components.PrestadorCardVerticalV2
import com.example.myapplication.ui.theme.MyApplicationTheme


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
    val isLoading by providerViewModel.isLoading.collectAsStateWithLifecycle()

    ResultBusquedaCategoriaContent(
        allProviders = allProviders,
        allCategories = allCategories,
        categoryName = categoryName,
        isLoading = isLoading,
        onBack = onBack,
        onNavigateToProviderProfile = onNavigateToProviderProfile,
        onNavigateToChat = onNavigateToChat,
        isProvider24h = { provider -> providerViewModel.isProvider24h(provider) },
        doesProviderHomeVisits = { provider -> providerViewModel.doesProviderHomeVisits(provider) },
        hasProviderPhysicalLocation = { provider -> providerViewModel.hasProviderPhysicalLocation(provider) },
        toggleFavoriteStatus = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBusquedaCategoriaContent(
    allProviders: List<Provider>,
    allCategories: List<CategoryEntity>,
    categoryName: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    isProvider24h: (Provider) -> Boolean,
    doesProviderHomeVisits: (Provider) -> Boolean,
    hasProviderPhysicalLocation: (Provider) -> Boolean,
    toggleFavoriteStatus: (String, Boolean) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var subscribedOnly by remember { mutableStateOf(true) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") }

    // 🔥 ESTADO ESTRELLA: Guarda el proveedor seleccionado para expandirlo en el Overlay
    var expandedProvider by remember { mutableStateOf<Provider?>(null) }

    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }

    //val filteredList = // ... (MANTÉN TU LÓGICA DE FILTRADO AQUÍ TAL CUAL LA TIENES) ...

    // 🔥 [LÓGICA DE FILTRADO REAL]
    val filteredList = remember(allProviders, categoryName, searchQuery, subscribedOnly, verifiedOnly, works24hOnly, homeVisitsOnly, physicalLocationOnly, sortOrder) {
        allProviders
            .filter { p ->
                // Filtro 1: Categoría
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
                ResultHeaderSection(
                    category = selectedCategory,
                    categoryName = categoryName,
                    onBack = onBack
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ==========================================
                // CAPA 1: FONDO Y GRILLA NORMAL
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    if (filteredList.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (subscribedOnly) "Recomendados" else "Todos los resultados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text("${filteredList.size}", modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ProviderListContent(
                            professionals = filteredList,
                            allCategories = allCategories,
                            onNavigateToProviderProfile = onNavigateToProviderProfile,
                            onNavigateToChat = onNavigateToChat,
                            onToggleFavorite = { id, isFav -> toggleFavoriteStatus(id, isFav) },
                            // 🔥 Pasamos la función que actualiza qué proveedor se debe expandir
                            onExpandToggle = { provider -> expandedProvider = provider }
                        )
                    }
                }

                // ==========================================
                // CAPA 2: OVERLAY ANIMADO (TARJETA FLOTANTE)
                // ==========================================
                AnimatedVisibility(
                    visible = expandedProvider != null,
                    enter = fadeIn(animationSpec = tween(400)) + scaleIn(
                        initialScale = 0.5f, // Empieza pequeña como la de la grilla
                        transformOrigin = TransformOrigin(0.5f, 0.5f),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy, // Un poco de rebote premium
                            stiffness = Spring.StiffnessLow
                        )
                    /**
                    visible = expandedProvider != null,
                    // Animación premium: Aparece, se desenfoca el fondo y salta desde un 80% de su tamaño con efecto resorte
                    enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    **/

                    ),
                    exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f)
                ) {
                    // Fondo oscuro (Scrim)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)) // Fondo oscuro casi opaco
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // Cierra la tarjeta si el usuario toca la zona oscura
                                expandedProvider = null
                            },
                        contentAlignment = Alignment.Center // 🔥 Centra la tarjeta en la pantalla
                    ) {
                        expandedProvider?.let { provider ->
                            // Contenedor "trampa" para evitar que al tocar la tarjeta se cierre el fondo
                            Box(
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {}
                            ) {
                                // Renderizamos la tarjeta forzada a su estado Expandido
                                PrestadorCardVerticalV2(
                                    provider = provider,
                                    isExpanded = true, // 🔥 Siempre abierta en esta capa
                                    onExpandToggle = { expandedProvider = null }, // La cerramos si la vuelven a tocar
                                    onClick = {
                                        expandedProvider = null // Cerramos overlay
                                        onNavigateToProviderProfile(provider.id) // Navegamos al perfil
                                    },
                                    onChat = { onNavigateToChat(provider.id) },
                                    onToggleFavorite = { id, isFav -> toggleFavoriteStatus(id, isFav) },
                                    allCategories = allCategories
                                )
                            }
                        }
                    }
                }
            }
        }
}

// --- ACTUALIZACIÓN DE LA LISTA ---
@Composable
fun ProviderListContent(
    professionals: List<Provider>,
    allCategories: List<CategoryEntity>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onExpandToggle: (Provider) -> Unit // 🔥 Nuevo parámetro
) {
    if (professionals.isEmpty()) {
        // EmptyStateMessage() // Asegúrate de tener tu función aquí
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 4 Tarjetas
            contentPadding = PaddingValues(top = 6.dp, start = 2.dp, end = 2.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(professionals, key = { p -> p.id }) { professional ->
                PrestadorCardVerticalV2(
                    provider = professional,
                    isExpanded = false, // 🔥 En la grilla SIEMPRE son compactas
                    onExpandToggle = { onExpandToggle(professional) }, // Avisa al padre
                    onClick = { onNavigateToProviderProfile(professional.id) },
                    onChat = { onNavigateToChat(professional.id) },
                    onToggleFavorite = onToggleFavorite,
                    allCategories = allCategories
                )
            }
        }
    }
}



/**
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
    val isLoading by providerViewModel.isLoading.collectAsStateWithLifecycle()

    ResultBusquedaCategoriaContent(
        allProviders = allProviders,
        allCategories = allCategories,
        categoryName = categoryName,
        isLoading = isLoading,
        onBack = onBack,
        onNavigateToProviderProfile = onNavigateToProviderProfile,
        onNavigateToChat = onNavigateToChat,
        isProvider24h = { provider -> providerViewModel.isProvider24h(provider) },
        doesProviderHomeVisits = { provider -> providerViewModel.doesProviderHomeVisits(provider) },
        hasProviderPhysicalLocation = { provider -> providerViewModel.hasProviderPhysicalLocation(provider) },
        toggleFavoriteStatus = { id, isFav -> providerViewModel.toggleFavoriteStatus(id, isFav) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBusquedaCategoriaContent(
    allProviders: List<Provider>,
    allCategories: List<CategoryEntity>,
    categoryName: String,
    isLoading: Boolean,
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

    var subscribedOnly by remember { mutableStateOf(true) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") }

    // --- LÓGICA DEL ASISTENTE Y EXPANSIÓN (ELIMINADA) ---
    // var expandedId by remember { mutableStateOf<String?>(null) }
    // --- REQUERIMIENTO: Reintroducir el estado de expansión ---
    var expandedId by remember { mutableStateOf<String?>(null) }
    // Obtenemos la categoría seleccionada para el encabezado dinámico
    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }

    // 🔥 [LÓGICA DE FILTRADO REAL]
    val filteredList = remember(allProviders, categoryName, searchQuery, subscribedOnly, verifiedOnly, works24hOnly, homeVisitsOnly, physicalLocationOnly, sortOrder) {
        allProviders
            .filter { p ->
                // Filtro 1: Categoría
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
            // La barra de búsqueda del asistente se ha eliminado de aquí.
            ResultHeaderSection(
                category = selectedCategory,
                categoryName = categoryName,
                onBack = onBack
            )
        }
    ) { paddingValues ->
// REQUERIMIENTO: Contenedor Box para superponer el scrim y el contenido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (subscribedOnly) "Recomendados" else "Todos los resultados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                "${filteredList.size}",
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider()
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // 🔥 Lista Real (4 columnas) - Ahora con lógica de expansión
                    ProviderListContent(
                        professionals = filteredList,
                        allCategories = allCategories,
                        onNavigateToProviderProfile = onNavigateToProviderProfile,
                        onNavigateToChat = onNavigateToChat,
                        onToggleFavorite = { id, isFav -> toggleFavoriteStatus(id, isFav) },
                        expandedId = expandedId,
                        onExpandToggle = { id -> expandedId = id }
                    )
                }
            }

            // REQUERIMIENTO: Scrim (fondo desenfocado) que aparece cuando una tarjeta se expande
            AnimatedVisibility(
                visible = expandedId != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .blur(8.dp) // Efecto de desenfoque suave
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Sin efecto ripple al tocar
                        ) {
                            expandedId = null // Al tocar el fondo, se cierra la tarjeta
                        }
                )
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
    onToggleFavorite: (String, Boolean) -> Unit,
    expandedId: String?,
    onExpandToggle: (String?) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        // Grid simple con 4 columnas, sin modificadores de posicionamiento para la expansión
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 🔥 4 tarjetas por fila
            contentPadding = PaddingValues(top = 6.dp, start = 2.dp, end = 1.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
                //.graphicsLayer(clip = false) // Permite que la tarjeta expandida se vea completa
        ) {
            items(professionals, key = { p -> p.id }) { professional ->
                val isExpanded = expandedId == professional.id

                // REQUERIMIENTO: Contenedor para aplicar zIndex y que la tarjeta expandida se muestre por encima
                Box(
                    //modifier = Modifier.zIndex(if (isExpanded) 500f else 400f)
                ) {
                    PrestadorCardVerticalV2(
                        provider = professional,
                        onClick = { onNavigateToProviderProfile(professional.id) },
                        onChat = { onNavigateToChat(professional.id) },
                        onToggleFavorite = onToggleFavorite,
                        allCategories = allCategories,
                        //isExpanded = isExpanded,
                        //onExpandToggle = {
                           // onExpandToggle(if (isExpanded) null else professional.id)
                        //}
                    )
                }


            }
        }
    }
}
**/

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
                        colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent),
                        startX = 600f,
                        endX = 1200f
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
                            .offset(x = 10.dp)
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
            isLoading = false,
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
