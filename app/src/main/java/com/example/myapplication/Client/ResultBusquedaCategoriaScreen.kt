package com.example.myapplication.Client

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext

// [CORRECCIÓN] Se elimina la importación innecesaria. Como 'ComponentesReutilizables.kt'
// está en el mismo paquete, sus funciones (como GenericFloatingMenu) son accesibles directamente.

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val category = CategorySampleDataFalso.categories.find { it.name == categoryName }
    // [CORRECCIÓN] Se aplica 'distinctBy' para eliminar duplicados basados en el ID del prestador.
    val professionals = category?.providerIds?.mapNotNull { SampleDataFalso.getPrestadorById(it) }?.distinctBy { it.id } ?: emptyList()

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    // [IMPLEMENTACIÓN] Se define la lógica para el botón flotante aquí, dentro de la pantalla principal.
    val context = LocalContext.current
    val shareOptions = listOf(
        "Compartir categoría" to { shareContent(context, "¡Mira los profesionales en la categoría '${category?.name}' en Maverick! https://tuapp.com") },
        "Invitar a un amigo" to { shareContent(context, "¡Descarga Maverick y encuentra los mejores servicios! https://tuapp.com") }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = category?.name ?: "Categoría no encontrada")
                        Spacer(modifier = Modifier.width(8.dp))
                        category?.icon?.let {
                            Text(text = it, fontSize = 24.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        // [IMPLEMENTACIÓN] Se añade el FloatingActionButton al Scaffold de la pantalla.
        // Utiliza el componente reutilizable 'GenericFloatingMenu' del otro archivo.
        floatingActionButton = {
            GenericFloatingMenu(
                icon = Icons.Default.Share,
                options = shareOptions
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Recomendados") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Todos") }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> RecommendedTab(
                        professionals = professionals.filter { it.isSubscribed },
                        onNavigateToProviderProfile = onNavigateToProviderProfile,
                        onNavigateToChat = onNavigateToChat
                    )
                    1 -> AllTab(
                        professionals = professionals.filter { !it.isSubscribed },
                        onNavigateToProviderProfile = onNavigateToProviderProfile,
                        onNavigateToChat = onNavigateToChat
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = "Lo sentimos mucho, pero en estos momentos, no hay Prestadores de Servicios registrados en esta Categoría 😥",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Estamos seguro de que muy pronto estará llena 😊",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendedTab(
    professionals: List<PrestadorProfileFalso>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    var verifiedOnly by remember { mutableStateOf(false) }
    var works24hOnly by remember { mutableStateOf(false) }
    var homeVisitsOnly by remember { mutableStateOf(false) }
    var physicalLocationOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Rating") }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredAndSortedProfessionals = remember(professionals, verifiedOnly, works24hOnly, homeVisitsOnly, physicalLocationOnly, sortOrder) {
        professionals
            .filter { if (verifiedOnly) it.isVerified else true }
            .filter { if (works24hOnly) it.works24h else true }
            .filter { if (homeVisitsOnly) it.doesHomeVisits else true }
            .filter { if (physicalLocationOnly) it.hasPhysicalLocation else true }
            .sortedByDescending {
                when (sortOrder) {
                    "Rating" -> it.rating
                    else -> it.rating // Default sort
                }
            }
    }

    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            // Filters and Sorting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = verifiedOnly,
                        onClick = { verifiedOnly = !verifiedOnly },
                        label = { Text("") },
                        leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = works24hOnly,
                        onClick = { works24hOnly = !works24hOnly },
                        label = { Text("24hs") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = homeVisitsOnly,
                        onClick = { homeVisitsOnly = !homeVisitsOnly },
                        label = { Text("Visita") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    FilterChip(
                        selected = physicalLocationOnly,
                        onClick = { physicalLocationOnly = !physicalLocationOnly },
                        label = { Text("Local") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    OutlinedButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⭐")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Mejor Puntuación") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                            onClick = { sortOrder = "Rating"; showSortMenu = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAndSortedProfessionals.isEmpty()) {
                EmptyStateMessage()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredAndSortedProfessionals) { professional ->
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
                    }
                }
            }
        }
    }
}

@Composable
fun AllTab(
    professionals: List<PrestadorProfileFalso>,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    if (professionals.isEmpty()) {
        EmptyStateMessage()
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(professionals) { professional ->
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
            }
        }
    }
}

// [CORRECCIÓN] Se elimina la función de prueba 'BotonFlotanteResultBusquedaCategoriaScreen'
// porque su lógica ya fue integrada en la pantalla principal 'ResultSerchCategoryScreen'.

// Función que dispara el menú de compartir del sistema
private fun shareContent(context: Context, text: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(intent, "Compartir vía")
    context.startActivity(chooser)
}

@Preview(showBackground = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    MyApplicationTheme {
        ResultBusquedaCategoriaScreen(
            categoryName = "Electricidad", 
            onBack = {}, 
            onNavigateToProviderProfile = {}, 
            onNavigateToChat = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun EmptyResultBusquedaCategoriaScreenPreview() {
    MyApplicationTheme {
        ResultBusquedaCategoriaScreen(
            categoryName = "Astrología", 
            onBack = {}, 
            onNavigateToProviderProfile = {}, 
            onNavigateToChat = {}
        )
    }
}
