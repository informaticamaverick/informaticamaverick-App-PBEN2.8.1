package com.example.myapplication.presentation.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.components.CompactCategoryCard
import com.example.myapplication.presentation.components.MenuFiltros
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun SuperCategoryDetailsPanel(
    beViewModel: BeBrainViewModel,
    onCategoryClick: (String) -> Unit
) {
    // 🔥 OBSERVAMOS EL ESTADO GLOBAL
    val selectedSuperCategory by beViewModel.selectedSuperCategory.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by beViewModel.searchResults.collectAsStateWithLifecycle()
    val isVisible = selectedSuperCategory != null

    // 🔥 GESTIÓN DE EFECTOS SEGÚN VISIBILIDAD
    LaunchedEffect(isVisible) {
        if (isVisible) {
            beViewModel.setUIBlocked(true)
            beViewModel.setSearchActive(true)
            beViewModel.setBottomBarVisible(false)
        } else {
            beViewModel.setUIBlocked(false)
            beViewModel.setSearchActive(false)
            beViewModel.setBottomBarVisible(true)
            beViewModel.updateSearchQuery("")
        }
    }

    SuperCategoryDetailsPanelContent(
        selectedSuperCategory = selectedSuperCategory,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onClose = { beViewModel.selectSuperCategory(null) },
        onCategoryClick = onCategoryClick
    )
}

/*** Versión de SuperCategoryDetailsPanel sin dependencia directa de ViewModel para Previews. */
@Composable
fun SuperCategoryDetailsPanelContent(
    selectedSuperCategory: SuperCategory?,
    searchQuery: String,
    searchResults: BeBrainViewModel.SearchResult,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val isVisible = selectedSuperCategory != null
    
    // 🔥 CLAVE: Mantenemos la categoría en memoria mientras se anima la salida
    var currentSuperCat by remember { mutableStateOf<SuperCategory?>(null) }
    LaunchedEffect(selectedSuperCategory) {
        if (selectedSuperCategory != null) currentSuperCat = selectedSuperCategory
    }

    // 🔥 ESTRUCTURA IDÉNTICA A BeResultadoScreen.kt
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        ) + fadeOut(),
        modifier = Modifier.fillMaxSize().zIndex(200f)
    ) {
        // Fondo oscuro que cubre TODA la pantalla y cierra al tocar fuera
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            currentSuperCat?.let { superCat ->
                SuperCategoryDetailsContent(
                    superCategory = superCat,
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    onClose = onClose,
                    onCategoryClick = onCategoryClick
                )
            }
        }
    }
}

/*** Contenido interno del panel. */
@Composable
private fun SuperCategoryDetailsContent(
    superCategory: SuperCategory,
    searchQuery: String,
    searchResults: BeBrainViewModel.SearchResult,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    var activeFilters by remember { mutableStateOf(setOf<String>()) }

    // BOX RAÍZ: Nos permite superponer elementos fuera de los límites de la tarjeta (Surface)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.78f)
    ) {
        // --- TARJETA PRINCIPAL ---
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp) // ESPACIO VITAL: Empuja la tarjeta hacia abajo para que el botón sobresalga
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }, // Absorbe los clicks silenciosamente sin cerrar el panel
            color = Color(0xFF0A0E14),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            tonalElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(20.dp)) // Ajuste de espacio por el botón

                // --- Encabezado del Panel ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 40.dp)) {
                        Text(
                            text = superCategory.title.uppercase(),
                            fontSize = if (superCategory.title.length > 20) 18.sp else 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "BUSCANDO EN GRUPO: ${searchQuery.uppercase()}",
                                color = Color(0xFF22D3EE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    MenuFiltros(
                        activeFilters = activeFilters,
                        dynamicCategories = emptyList(),
                        onAction = { filterId ->
                            activeFilters = if (activeFilters.contains(filterId)) activeFilters - filterId else activeFilters + filterId
                        },
                        onApply = { },
                        onClearFilters = { activeFilters = emptySet() },
                        showProductService = true
                    )
                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color.White.copy(alpha = 0.1f))

                // --- Lógica de Filtrado de Grilla ---
                val displayItems = remember(superCategory.items, searchResults, searchQuery) {
                    if (searchQuery.isEmpty()) {
                        superCategory.items
                    } else {
                        // Obtenemos el resultado de tipo CategoryMatch que Be procesó
                        (searchResults as? BeBrainViewModel.SearchResult.CategoryMatch)?.categories ?: emptyList()
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = displayItems, key = { it.name }) { category ->
                        CompactCategoryCard(
                            item = category,
                            onClick = { onCategoryClick(category.name) }
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(130.dp)) // Espacio para Be
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

// ==================================================================================
// --- PREVIEW ---
// ==================================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SuperCategoryDetailsPanelPreview() {
    val sampleCategories = listOf(
        CategoryEntity(name = "Plomería", icon = "🚰", color = 0xFF2196F3L, superCategory = "Hogar", superCategoryIcon = "🏠", imageUrl = null, isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Electricidad", icon = "⚡", color = 0xFFFFC107L, superCategory = "Hogar", superCategoryIcon = "🏠", imageUrl = null, isNew = false, isNewPrestador = false, isAd = false)
    )
    val sampleSuperCategory = SuperCategory(title = "Servicios del Hogar", icon = "🏠", items = sampleCategories)
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SuperCategoryDetailsPanelContent(
                selectedSuperCategory = sampleSuperCategory,
                searchQuery = "",
                searchResults = BeBrainViewModel.SearchResult.Empty,
                onClose = {},
                onCategoryClick = {}
            )
        }
    }
}
