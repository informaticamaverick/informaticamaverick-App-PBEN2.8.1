package com.example.myapplication.Client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// ==================================================================================
// --- SECCIÓN: PANTALLA RESULTADOS DE BÚSQUEDA ---
// ==================================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    // Carga de la categoría y profesionales asociados
    val category = CategorySampleDataFalso.categories.find { it.name == categoryName }
    val professionals = remember(categoryName) {
        category?.providerIds
            ?.mapNotNull { SampleDataFalso.getPrestadorById(it) }
            ?.distinctBy { it.id }
            ?.shuffled() 
            ?: emptyList()
    }

    // --- ESTADOS LOCALES ---
    var isSearchActive by remember { mutableStateOf(false) } // Control de búsqueda
    var searchQuery by remember { mutableStateOf("") } // Texto de búsqueda
    val keyboardController = LocalSoftwareKeyboardController.current

    // Filtros de búsqueda (Estado)
    var subscribedOnly by remember { mutableStateOf(true) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") } 

    var isFabMenuExpanded by remember { mutableStateOf(false) } // Control del FAB expandido
    
    // Función para cerrar búsqueda
    val closeSearch = {
        isSearchActive = false
        searchQuery = ""
        keyboardController?.hide()
        Unit
    }

    // Lógica de filtrado unificada
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
        topBar = {
            Box { 
                // Header normal (Solo si no hay búsqueda)
                if (!isSearchActive) {
                    ResultHeaderSection(
                        categoryName = categoryName,
                        categoryIcon = category?.icon,
                        onBack = onBack
                    )
                }

                // Barra de búsqueda Gemini
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
                // Indicador de tipo de resultados (Recomendados / Todos)
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

                // Lista de prestadores
                ProviderListContent(
                    professionals = filteredList,
                    onNavigateToProviderProfile = onNavigateToProviderProfile,
                    onNavigateToChat = onNavigateToChat
                )
            }

            // --- SECCIÓN: FAB DIVIDIDO GEMINI ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, end = 16.dp), // DISTANCIA DEL BORDE
                contentAlignment = Alignment.BottomEnd 
            ) {
                GeminiSplitFAB(
                    isExpanded = isFabMenuExpanded,
                    isSearchActive = isSearchActive,
                    onToggleExpand = { isFabMenuExpanded = !isFabMenuExpanded },
                    onActivateSearch = { isSearchActive = true },
                    onCloseSearch = closeSearch,
                    expandedTools = {
                        // Herramientas de filtrado rápido
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
                            onClick = { sortOrder = if (sortOrder == "Rating") "Name" else "Rating" }
                        )
                    }
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: BARRA DE BÚSQUEDA CON ESTILO GEMINI ---
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
            .padding(8.dp), // ESPACIADO EXTERNO
        color = Color(0xFF121212), // FONDO OSCURO GEMINI
        shape = RoundedCornerShape(28.dp), // FORMA REDONDEADA
        shadowElevation = 12.dp,
        border = BorderStroke(2.5.dp, rainbowBrush) // BORDE ARCOIRIS GEMINI
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp), // ALTURA DE LA BARRA
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
                textStyle = TextStyle(color = Color.White, fontSize = 17.sp), // TEXTO BLANCO
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
// --- SECCIÓN: COMPONENTES DE SOPORTE (LISTA Y HEADER) ---
// ==================================================================================

@Composable
fun ProviderListContent(
    professionals: List<UserFalso>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 100.dp),
        ) {
            items(professionals) { professional ->
                Column {
                    PrestadorCard(
                        provider = professional,
                        onClick = { onNavigateToProviderProfile(professional.id) },
                        onChat = { onNavigateToChat(professional.id) }
                    )
                    // DIVISOR ENTRE TARJETAS
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
            Spacer(modifier = Modifier.width(8.dp))
            categoryIcon?.let { Text(it, fontSize = 24.sp) }
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

@Preview(showBackground = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    MyApplicationTheme { ResultBusquedaCategoriaScreen("Electricidad", {}, {}, {}) }
}
