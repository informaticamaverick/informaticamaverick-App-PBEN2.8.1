package com.example.myapplication.Client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// ==========================================
// PANTALLA RESULTADOS DE BÚSQUEDA
// ==========================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val category = CategorySampleDataFalso.categories.find { it.name == categoryName }
    
    val professionals = remember(categoryName) {
        category?.providerIds
            ?.mapNotNull { SampleDataFalso.getPrestadorById(it) }
            ?.distinctBy { it.id }
            ?.shuffled() 
            ?: emptyList()
    }

    // --- ESTADOS DE BÚSQUEDA Y FILTROS ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filtros
    // [MODIFICADO] subscribedOnly en true por defecto para mostrar "Recomendados" primero
    var subscribedOnly by remember { mutableStateOf(true) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") } 

    // --- ESTADOS PARA EL FAB ---
    var isFabMenuExpanded by remember { mutableStateOf(true) }
    
    // --- LÓGICA DE FILTRADO UNIFICADA ---
    val filteredList = remember(professionals, searchQuery, subscribedOnly, verifiedOnly, works24hOnly, homeVisitsOnly, physicalLocationOnly, sortOrder) {
        professionals
            .filter { 
                if (searchQuery.isNotEmpty()) {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.lastName.contains(searchQuery, ignoreCase = true) ||
                    it.services.any { s -> s.contains(searchQuery, ignoreCase = true) }
                } else true 
            }
            .filter { if (subscribedOnly) it.isSubscribed else true }
            .filter { if (verifiedOnly) it.isVerified else true }
            .filter { if (works24hOnly) it.works24h else true }
            .filter { if (homeVisitsOnly) it.doesHomeVisits else true }
            .filter { if (physicalLocationOnly) it.hasPhysicalLocation else true }
            .sortedByDescending { it.rating }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Contenido Principal
            Column(modifier = Modifier.fillMaxSize()) {
                
                Spacer(modifier = Modifier.height(72.dp))

                // [NUEVO] Encabezado de Resultados (Lista única)
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

                // Lista de Resultados
                ProviderListContent(
                    professionals = filteredList,
                    onNavigateToProviderProfile = onNavigateToProviderProfile,
                    onNavigateToChat = onNavigateToChat
                )
            }

            // --- HEADER PERSONALIZADO ---
            if (!isSearchActive) {
                ResultHeaderSection(
                    categoryName = categoryName, 
                    categoryIcon = category?.icon,
                    onBack = onBack
                )
            }

            // --- BARRA DE BÚSQUEDA ---
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .zIndex(20f)
                    .align(Alignment.TopCenter)
            ) {
                TopSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCancel = { 
                        isSearchActive = false
                        searchQuery = "" 
                        isFabMenuExpanded = true 
                    }
                )
            }

            // --- FAB MENU OVERLAY (DIVIDIDO A LA DERECHA) ---
            if (!isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                        //.padding(16.dp) // Padding movido al contenido interno para permitir full-width gradient si se desea
                        //.zIndex(2f),
                    contentAlignment = Alignment.BottomEnd 
                ) {
                    
                    // [OPCIONAL: FONDO DEGRADADO FAB]
                    // Descomentar para activar el fondo oscuro detrás de las herramientas
                    /*
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                )
                            )
                    )
                    */

                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .zIndex(2f)
                    ) {
                        ResultFabMenuOverlay(
                            isFabMenuExpanded = isFabMenuExpanded,
                            onToggleFabMenu = { isFabMenuExpanded = !isFabMenuExpanded },
                            onActivateSearch = { 
                                isSearchActive = true 
                                isFabMenuExpanded = false 
                            },
                            verifiedOnly = verifiedOnly,
                            onToggleVerified = { verifiedOnly = !verifiedOnly },
                            works24hOnly = works24hOnly,
                            onToggleWorks24h = { works24hOnly = !works24hOnly },
                            homeVisitsOnly = homeVisitsOnly,
                            onToggleHomeVisits = { homeVisitsOnly = !homeVisitsOnly },
                            physicalLocationOnly = physicalLocationOnly,
                            onTogglePhysicalLocation = { physicalLocationOnly = !physicalLocationOnly },
                            currentSort = sortOrder,
                            onToggleSort = { sortOrder = if (sortOrder == "Rating") "Name" else "Rating" },
                            // Nuevo filtro de suscripción
                            subscribedOnly = subscribedOnly,
                            onToggleSubscribed = { subscribedOnly = !subscribedOnly }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTE FAB MENU PERSONALIZADO (SPLIT)
// ==========================================

@Composable
fun ResultFabMenuOverlay(
    isFabMenuExpanded: Boolean,
    onToggleFabMenu: () -> Unit,
    onActivateSearch: () -> Unit,
    // Parámetros de filtros
    verifiedOnly: Boolean, onToggleVerified: () -> Unit,
    works24hOnly: Boolean, onToggleWorks24h: () -> Unit,
    homeVisitsOnly: Boolean, onToggleHomeVisits: () -> Unit,
    physicalLocationOnly: Boolean, onTogglePhysicalLocation: () -> Unit,
    currentSort: String, onToggleSort: () -> Unit,
    subscribedOnly: Boolean, onToggleSubscribed: () -> Unit
) {
    val fabIconRotation by animateFloatAsState(
        targetValue = if (isFabMenuExpanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabIconRotation"
    )

    Box(contentAlignment = Alignment.BottomEnd) {
        Row(
            verticalAlignment = Alignment.Bottom, 
            horizontalArrangement = Arrangement.End
        ) {
            
            // --- CAJA DE HERRAMIENTAS HORIZONTAL (FILTROS) ---
            AnimatedVisibility(
                visible = isFabMenuExpanded, 
                enter = fadeIn(animationSpec = tween(150, 150)) + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut(animationSpec = tween(150)) + slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(end = 12.dp, bottom = 8.dp) 
                ) {
                    SmallFabTool(label = "Verificado", icon = Icons.Default.Verified, onClick = onToggleVerified, isSelected = verifiedOnly)
                    SmallFabTool(label = "24hs", icon = Icons.Default.Schedule, onClick = onToggleWorks24h, isSelected = works24hOnly)
                    SmallFabTool(label = "Visita", icon = Icons.Default.Home, onClick = onToggleHomeVisits, isSelected = homeVisitsOnly)
                    SmallFabTool(label = "Local", icon = Icons.Default.Storefront, onClick = onTogglePhysicalLocation, isSelected = physicalLocationOnly)
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- CAJA DE HERRAMIENTAS VERTICAL ---
                AnimatedVisibility(
                    visible = isFabMenuExpanded,
                    enter = fadeIn(animationSpec = tween(150, 150)) + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        // [NUEVO] Botón para alternar "Solo Recomendados"
                        SmallFabTool(
                            label = if (subscribedOnly) "Top" else "Todos",
                            icon = if (subscribedOnly) Icons.Default.WorkspacePremium else Icons.Default.Group, // Icono distintivo
                            onClick = onToggleSubscribed,
                            isSelected = subscribedOnly
                        )
                        
                        SmallFabTool(
                            label = if (currentSort == "Rating") "Rating" else "Nombre", 
                            icon = if (currentSort == "Rating") Icons.Default.Star else Icons.AutoMirrored.Filled.Sort, 
                            onClick = onToggleSort,
                            isSelected = true 
                        )
                    }
                }

                // FAB PRINCIPAL (DIVIDIDO)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Parte Izquierda (Buscar - Lupa)
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable(onClick = onActivateSearch),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 4.dp, bottomEnd = 4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar") 
                        }
                    }

                    // Parte Derecha (Menú - Engranaje)
                    Surface(
                        modifier = Modifier.size(56.dp).clickable { onToggleFabMenu() },
                        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Settings, "Opciones", modifier = Modifier.rotate(fabIconRotation))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES DE LISTA, HEADER Y SEARCHBAR
// ==========================================

@Composable
fun ProviderListContent(
    professionals: List<PrestadorProfileFalso>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 100.dp),
            // [MODIFICADO] Eliminado espacio vertical entre items para usar Divider
        ) {
            items(professionals) { professional ->
                Column {
                    PrestadorCard(
                        provider = professional,
                        onClick = { onNavigateToProviderProfile(professional.id) },
                        onChat = { onNavigateToChat(professional.id) },
                        actionContent = {
                            FilledTonalIconButton(
                                onClick = { onNavigateToChat(professional.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar mensaje",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    )
                    // [MODIFICADO] Separador ajustado a 1.dp
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 1.dp, bottom = 16.dp), 
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) 
                    )
                }
            }
        }
    }
}

@Composable
fun ResultHeaderSection(
    categoryName: String,
    categoryIcon: String?,
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
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            categoryIcon?.let {
                Text(text = it, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { showInfoDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TopSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.surface,
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
                modifier = Modifier.weight(1f).padding(start = 20.dp).focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) Text("Buscar por nombre o servicio...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        innerTextField()
                    }
                }
            )
            IconButton(onClick = onCancel, modifier = Modifier.padding(end = 8.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, "Cancelar", tint = MaterialTheme.colorScheme.onSecondaryContainer) }
                }
            }
        }
    }
}

@Composable
fun SmallFabTool(label: String, icon: ImageVector, onClick: () -> Unit, isSelected: Boolean = false) {
    val animatedBg by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, label = "bg")
    val animatedContent by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "content")
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
fun EmptyStateMessage() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No hay resultados con estos filtros 🔍", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    MyApplicationTheme { ResultBusquedaCategoriaScreen("Electricidad", {}, {}, {}) }
}
