package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.ui.theme.MyApplicationTheme

// ==========================================================================================
// --- MODELOS DE DATOS Y COMPONENTES BASE ---
// ==========================================================================================

/**
 * ControlItem: Modelo de datos para elementos de filtrado y ordenamiento.
 */
data class ControlItem(
    val label: String,
    val icon: ImageVector?,
    val emoji: String,
    val color: Color,
    val id: String = label.lowercase()
)

/**
 * CompactItemButton: Botón minimalista utilizado en los menús tácticos.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactItemButton(
    item: ControlItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    overlayEmoji: String? = null,
    overlayAlignment: Alignment = Alignment.BottomEnd
) {
    Column(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                .border(if (isSelected) 1.5.dp else 0.8.dp, if (isSelected) item.color else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = item.emoji, 
                    fontSize = 24.sp, 
                    style = TextStyle(shadow = Shadow(color = item.color, offset = androidx.compose.ui.geometry.Offset(0f, 0f), blurRadius = 25f))
                )
            } else {
                item.icon?.let { Icon(it, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp)) } 
                    ?: run { Text(item.emoji, fontSize = 20.sp, modifier = Modifier.alpha(0.6f)) }
            }

            overlayEmoji?.let { emoji ->
                Text(
                    text = emoji, fontSize = 11.sp, color = Color.White,
                    modifier = Modifier
                        .align(overlayAlignment)
                        .offset(x = 6.dp, y = if (overlayAlignment == Alignment.TopEnd) (-6).dp else 6.dp)
                        .graphicsLayer { shadowElevation = 10f }
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.label, 
            fontSize = 9.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = if (isSelected) Color.White else Color.LightGray, 
            textAlign = TextAlign.Center, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================================================================
// --- COMPONENTE PRINCIPAL: MENU FILTROS ---
// ==========================================================================================

/**
 * MenuFiltros: Componente táctico para la gestión de filtros y categorías.
 */
@Composable
fun MenuFiltros(
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem> = emptyList(), // 🔥 NUEVO: Filtros tácticos dinámicos desde el ViewModel
    onAction: (String) -> Unit,
    onApply: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    showProductService: Boolean = false 
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 🔥 CORRECCIÓN: Solo muestra la X si hay filtros de tipo 'filter_' o 'cat_' para no duplicar con Ordenamiento
    val hasSpecificFilters = activeFilters.any { it.startsWith("filter_") || it.startsWith("cat_") }

    // 1. Animación de rotación para el icono (Tornado en este caso)
    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "RotationFiltros"
    )

    // 2. Animación de escala con Rebote para el panel (Réplica exacta)
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(200)
        },
        label = "ScaleFiltros"
    )

    // Gradiente premium para el fondo del panel
    val cardBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1F26), Color(0xFF0A0E14))
    )

    // CONTENEDOR PRINCIPAL (Anclado a la derecha como MenuOrdenamiento)
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {

        // --- SECCIÓN BOTONES (X que brota y Tornado anclado) ---
        Box(
            modifier = Modifier
                .padding(4.dp)
                .height(40.dp)
                .widthIn(min = 40.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // 🔥 BOTÓN X: Brota desde detrás del Tornado hacia la izquierda
            AnimatedVisibility(
                visible = hasSpecificFilters,
                enter = fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { it })
            ) {
                Surface(
                    onClick = { onClearFilters() },
                    modifier = Modifier.padding(end = 40.dp).size(32.dp),
                    shape = CircleShape,
                    color = Color(0xFF1A1F26),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.7f), Color.Transparent))),
                    shadowElevation = 6.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().background(
                            Brush.radialGradient(listOf(Color(0xFFEF4444).copy(0.15f), Color.Transparent))
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                    }
                }
            }

            // --- ICONO DISPARADOR (Tornado) ---
            Surface(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(32.dp).graphicsLayer { rotationZ = iconRotation },
                shape = CircleShape,
                color = Color(0xFF1A1F26),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.7f), Color.Transparent))),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "🌪️", fontSize = 18.sp)
                }
            }
        }

        // --- 2. PANEL POPUP (FILTROS) ---
        if (isExpanded || scale > 0.01f) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(-50, 115),
                properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
                onDismissRequest = { isExpanded = false }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .width(300.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = scale.coerceIn(0f, 1f)
                            transformOrigin = TransformOrigin(1f, 0f)
                        }
                ) {
                   Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(modifier = Modifier.background(cardBackground)) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // HEADER CON DIVIDER DINÁMICO (Estilo MenuOrdenamiento)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CENTRO DE FILTROS",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.5.sp
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                        thickness = 0.5.dp,
                                        color = Color.White.copy(alpha = 0.2f)
                                    )

                                    Surface(
                                        onClick = { isExpanded = false; onApply() },
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF1A1F26),
                                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.7f), Color.Transparent))),
                                        shadowElevation = 6.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // --- 1. SECCIÓN: TIPO DE OFERTA ---
                                if (showProductService) {
                                    Text("TIPO DE OFERTA", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        CompactItemButton(
                                            item = ControlItem("Productos", Icons.Default.ShoppingBag, "🛍️", Color(0xFF22D3EE), "filter_products"),
                                            isSelected = activeFilters.contains("filter_products"),
                                            onClick = { onAction("filter_products") }
                                        )
                                        CompactItemButton(
                                            item = ControlItem("Servicios", Icons.Default.Build, "🔧", Color(0xFFF59E0B), "filter_services"),
                                            isSelected = activeFilters.contains("filter_services"),
                                            onClick = { onAction("filter_services") }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // --- 2. SECCIÓN: CATEGORÍAS DINÁMICAS ---
                                if (dynamicCategories.isNotEmpty()) {
                                    Text("CATEGORÍAS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(dynamicCategories) { item ->
                                            val isSelected = activeFilters.contains(item.id)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { onAction(item.id) },
                                                label = { Text(item.label, fontSize = 10.sp) },
                                                leadingIcon = { Text(item.emoji) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // --- 3. SECCIÓN: REFINAR BÚSQUEDA (GRID DINÁMICO) ---
                                if (refinementFilters.isNotEmpty()) {
                                    Text("REFINAR BÚSQUEDA", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        // 🔥 DIBUJA AUTOMÁTICAMENTE LOS FILTROS RECIBIDOS EN FILAS DE 4
                                        refinementFilters.chunked(4).forEach { rowItems ->
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                rowItems.forEach { item ->
                                                    CompactItemButton(item = item, isSelected = activeFilters.contains(item.id), onClick = { onAction(item.id) })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                   }
                }
            }
        }
    }
}

/**
 * Vista previa exhaustiva de MenuFiltros con datos de ejemplo.
 */
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuFiltrosPreview() {
    MyApplicationTheme {
        // Estado local para simular la interacción en la Preview
        var activeFilters by remember { mutableStateOf(setOf("filter_products", "cat_plomeria")) }
        
        // Categorías dinámicas de ejemplo
        val sampleCategories = listOf(
            ControlItem("Plomería", null, "🔧", Color(0xFF2197F5), "cat_plomeria"),
            ControlItem("Electricidad", null, "⚡", Color(0xFFFFEB3B), "cat_electricidad"),
            ControlItem("Pintura", null, "🖌️", Color(0xFF4CAF50), "cat_pintura")
        )
        
        // Filtros de refinamiento (estilo BeBrain)
        val sampleRefinements = listOf(
            ControlItem("Suscrito", Icons.Default.Verified, "✅", Color(0xFF9B51E0), "filter_sub"),
            ControlItem("Favorito", Icons.Default.Favorite, "❤️", Color(0xFFE91E63), "filter_fav"),
            ControlItem("Online", Icons.Default.Circle, "🌐", Color(0xFF10B981), "filter_online"),
            ControlItem("24hs", Icons.Default.AccessTimeFilled, "⏳", Color(0xFFFF9800), "filter_24h")
        )

        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            MenuFiltros(
                activeFilters = activeFilters,
                dynamicCategories = sampleCategories,
                refinementFilters = sampleRefinements,
                showProductService = true,
                onAction = { id ->
                    val current = activeFilters.toMutableSet()
                    if (!current.add(id)) current.remove(id)
                    activeFilters = current
                },
                onApply = { /* Simulación de aplicar */ },
                onClearFilters = { activeFilters = emptySet() }
            )
        }
    }
}
