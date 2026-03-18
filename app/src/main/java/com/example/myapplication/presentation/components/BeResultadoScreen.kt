package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.presentation.client.SuperCategory
import com.example.myapplication.presentation.client.prepareForSearch
import com.example.myapplication.presentation.client.wordStartsWith
import com.example.myapplication.ui.theme.MyApplicationTheme


/*** Componente de texto que reduce automáticamente su tamaño si es demasiado largo */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    textAlign: TextAlign = TextAlign.Start
) {
    var multiplier by remember { mutableFloatStateOf(1f) }

    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        style = style.copy(fontSize = style.fontSize * multiplier),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && multiplier > 0.5f) {
                multiplier *= 0.9f
            }
        }
    )
}

/*** Pantalla de resultados inteligente de Be.
 * Integra búsqueda de Categorías, Supercategorías y Favoritos del HomeScreen. */
@Composable
fun BeResultadoScreen(
    viewModel: BeBrainViewModel,
    onClose: () -> Unit,
    onProviderClick: (String) -> Unit,
    allCategories: List<CategoryEntity> = emptyList(),
    favoriteProviders: List<Provider> = emptyList(),
    onCategoryClick: (String) -> Unit = {},
    onSuperCategoryClick: (SuperCategory) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isVisible by viewModel.isResultadoVisible.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    // --- LÓGICA DE FILTRADO REACTIVO (Consumiendo del Cerebro de Be) ---

    // Obtenemos categorías y supercategorías directamente del resultado global procesado por el ViewModel
    val categories = (searchResults as? BeBrainViewModel.SearchResult.GlobalMatch)?.categories ?: emptyList()
    val superCategories = (searchResults as? BeBrainViewModel.SearchResult.GlobalMatch)?.superCategories ?: emptyList()

    // Filtrar Favoritos por nombre del prestador usando la lógica centralizada de normalización
    val filteredFavorites = remember(favoriteProviders, searchQuery) {
        if (searchQuery.isEmpty()) emptyList()
        else {
            val normalizedQuery = searchQuery.prepareForSearch()
            favoriteProviders.filter {
                it.displayName.prepareForSearch().wordStartsWith(normalizedQuery)
            }
        }
    }

    // 🔥 CICLO DE VIDA: Gestión integral de apertura y cierre del asistente
    DisposableEffect(isVisible) {
        if (isVisible) {
            // Al abrirse la pantalla de resultados:
            viewModel.setSearchActive(true) // 1. Activamos el modo búsqueda (Be vuela hacia arriba)
            viewModel.setBottomBarVisible(false) // 2. Ocultamos la barra de navegación inferior para dar espacio
            viewModel.openKeyboard() // 3. Forzamos la apertura del teclado para que el usuario escriba de inmediato
        }
        onDispose {
            // --- SECCIÓN: LIMPIEZA AL CERRAR ---
            if (isVisible) {
                viewModel.cerrarBeAssistantCompleto()// Si la pantalla deja de ser visible (por navegación o cierre), ejecutamos el reset maestro para restaurar el HUD a la normalidad
            }
        }
    }

    BeResultadoContent(
        searchQuery = searchQuery,
        isVisible = isVisible,
        categories = categories,
        superCategories = superCategories,
        favorites = filteredFavorites,
        allCategories = allCategories,
        onClose = {
            // Cierre explícito desde la UI (Botón X o click fuera del panel)
            viewModel.cerrarBeAssistantCompleto()
            onClose() // Notifica al componente padre
        },
        // 🔥 MODIFICACIÓN: Al hacer click en cualquier resultado, cerramos Be completamente antes de ejecutar la acción
        onCategoryClick = { categoryName ->
            viewModel.cerrarBeAssistantCompleto() // Reset maestro del asistente
            onCategoryClick(categoryName) // Navegación a resultados
        },
        onSuperCategoryClick = { superCat ->
            // 🔥 MODIFICACIÓN: Al tocar una Supercategoría, notificamos al ViewModel global
            // Esto permite que HomeScreenCliente3 reaccione y abra el SuperCategoryDetailsPanel
            // viewModel.selectSuperCategory(superCat)
            viewModel.cerrarBeAssistantCompleto() // Cerramos Be para dar paso al nuevo panel
            onSuperCategoryClick(superCat)
        },
        onProviderClick = { providerId ->
            viewModel.cerrarBeAssistantCompleto() // Reset maestro del asistente
            onProviderClick(providerId) // Navega al perfil del prestador
        }
    )
}

/*** Contenido de la pantalla de resultados de Be con secciones colapsables.*/
@Composable
fun BeResultadoContent(
    searchQuery: String,
    isVisible: Boolean,
    categories: List<CategoryEntity>,
    superCategories: List<SuperCategory>,
    favorites: List<Provider>,
    allCategories: List<CategoryEntity>,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onSuperCategoryClick: (SuperCategory) -> Unit,
    onProviderClick: (String) -> Unit
) {
    val cyberBackground = Color(0xFF0A0E14)
    val textMain = Color(0xFFE2E8F0)
    val textMuted = Color(0xFF94A3B8)

    // Estados locales para colapsar/expandir secciones
    var categoriesExpanded by remember { mutableStateOf(true) }
    var superCategoriesExpanded by remember { mutableStateOf(true) }
    var favoritesExpanded by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // BOX RAÍZ: Contenedor que permite al botón sobresalir de la tarjeta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f) // Altura perfecta
            ) {
                // --- TARJETA PRINCIPAL ---
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp) // ESPACIO VITAL para que el botón sobresalga
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }, // Evita que clicks en el panel cierren el overlay de forma segura
                    color = cyberBackground,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    tonalElevation = 16.dp,
                    // 🔥 AQUÍ SE APLICA EL BORDE DE GEMINI: Toma la forma curva automáticamente
                    border = BorderStroke(2.dp, geminiGradientBrush(isAnimated = false))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp)) // Ajuste de espacio por el botón

                            // Header: Título
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, end = 40.dp) // end = 40.dp para que el texto no pise el botón
                            ) {
                                AutoSizeText(
                                    text = if (searchQuery.isEmpty()) "Análisis de Be" else "Resultados para: ${searchQuery.uppercase()}",
                                    color = textMain,
                                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                )
                                Text(
                                    text = "Inteligencia Maverick en acción ✨",
                                    style = TextStyle(
                                        brush = geminiGradientBrush(isAnimated = false),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.7f), thickness = 1.dp)

                            // Lista de Resultados Dinámica
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                            ) {
                                // --- SECCIÓN: CATEGORÍAS (SERVICIOS) ---
                                if (categories.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Servicios",
                                            count = categories.size,
                                            isExpanded = categoriesExpanded,
                                            onToggle = { categoriesExpanded = !categoriesExpanded }
                                        )
                                    }

                                    if (categoriesExpanded) {
                                        item {
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                            ) {
                                                items(categories) { category ->
                                                    Box(modifier = Modifier.width(150.dp)) {
                                                        CompactCategoryCard(
                                                            item = category,
                                                            onClick = { onCategoryClick(category.name) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // --- SECCIÓN: SUPERCATEGORÍAS (GRUPOS) ---
                                if (superCategories.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Grupos",
                                            count = superCategories.size,
                                            isExpanded = superCategoriesExpanded,
                                            onToggle = { superCategoriesExpanded = !superCategoriesExpanded }
                                        )
                                    }
                                    if (superCategoriesExpanded) {
                                        item {
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                            ) {
                                                items(superCategories) { superCat ->
                                                    Box(modifier = Modifier.width(280.dp)) {
                                                        BentoSuperCategoryCard(
                                                            superCategory = superCat,
                                                            emoji = superCat.icon,
                                                            height = 180.dp,
                                                            onClick = { onSuperCategoryClick(superCat) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // --- SECCIÓN: FAVORITOS (PRESTADORES) ---
                                if (favorites.isNotEmpty()) {
                                    item {
                                        CollapsibleSectionHeader(
                                            title = "Mis Favoritos",
                                            count = favorites.size,
                                            isExpanded = favoritesExpanded,
                                            onToggle = { favoritesExpanded = !favoritesExpanded }
                                        )
                                    }
                                    if (favoritesExpanded) {
                                        item {
                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                            ) {
                                                items(favorites) { provider ->
                                                    PrestadorCardVerticalV2(
                                                        provider = provider,
                                                        onClick = { onProviderClick(provider.id) },
                                                        allCategories = allCategories
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // --- EMPTY STATE ---
                                if (categories.isEmpty() && superCategories.isEmpty() && favorites.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = textMuted.copy(alpha = 0.3f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Busca categorías, grupos o profesionales favoritos para ver resultados.",
                                                color = textMuted,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- BOTÓN DE CIERRE SUPERPUESTO ---
                Surface(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Se alinea en el límite superior absoluto del Box
                        .padding(top = 0.dp, end = 8.dp) // Alineado horizontalmente con el contenido
                        .size(38.dp)
                        .shadow(12.dp, CircleShape, spotColor = Color.Red),
                    shape = CircleShape,
                    color = Color(0xFF0A0E14),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/*** Encabezado colapsable para secciones de resultados con contador. */
@Composable
fun CollapsibleSectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f, label = "rotation")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title.uppercase(),
                color = Color(0xFF22D3EE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BeResultadoContentPreview() {
    MyApplicationTheme(darkTheme = true) {
        BeResultadoContent(
            searchQuery = "Soporte",
            isVisible = true,
            categories = emptyList(),
            superCategories = emptyList(),
            favorites = emptyList(),
            allCategories = emptyList(),
            onClose = {},
            onCategoryClick = {},
            onSuperCategoryClick = {},
            onProviderClick = {}
        )
    }
}
