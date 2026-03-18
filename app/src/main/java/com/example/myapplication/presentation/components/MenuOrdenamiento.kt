package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * MenuOrdenamiento: Componente táctico para la gestión de ordenamientos.
 * Recibe una lista dinámica de 'sortOptions' para adaptarse a cada pantalla.
 */
@Composable
fun MenuOrdenamiento(
    activeFilters: Set<String>,
    sortOptions: List<ControlItem> = emptyList(), // 🔥 NUEVO: Opciones dinámicas desde el ViewModel
    onAction: (String) -> Unit,
    onApply: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    // 🔥 MANTENIDOS PARA COMPATIBILIDAD CON CARRUSEL DE HOMESCREEN
    showNombre: Boolean = false,
    showRank: Boolean = false,
    showViewModes: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 🔥 CORRECCIÓN: Solo muestra la X si hay filtros de tipo 'sort_' o 'view_'
    val hasSortFilters = activeFilters.any { it.startsWith("sort_") || it.startsWith("view_") }

    // 1. Rotación de la tuerca
    val gearRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "GearRotation"
    )

    // 2. Escala con Rebote (Spring)
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(200)
        },
        label = "ScaleOrdenamiento"
    )

    val cardBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1F26), Color(0xFF0A0E14))
    )

    // 🔥 CORRECCIÓN: Quitamos fillMaxWidth() para que no tape el texto de HomeScreen
    Box(
       modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 🔥 BOTÓN X (Limpiar): Brota y se esconde detrás del engranaje
        // Lo ponemos ANTES en el código para que el Engranaje se dibuje ENCIMA (Z-index natural)
        AnimatedVisibility(
            visible = hasSortFilters,
            enter = fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { it })
        ) {
            // El paddingEnd asegura que la X se detenga a la izquierda del engranaje
            Surface(
                onClick = { onClearFilters() },
                modifier = Modifier.padding(end = 40.dp).size(32.dp),
                shape = CircleShape,
                color = Color(0xFF1A1F26),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.7f), Color.Transparent))),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFFEF4444).copy(0.15f), Color.Transparent)))) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                }
            }
        }

        // --- BOTÓN ENGRANAJE (Ancla absoluta) ---
        // Al estar al final del Box y alineado al End, nunca se moverá
        Surface(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.size(32.dp).graphicsLayer { rotationZ = gearRotation },
            shape = CircleShape,
            color = Color(0xFF1A1F26),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.7f), Color.Transparent))),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "⚙️", fontSize = 16.sp)
            }
        }

        // --- EL POPUP QUE "BROTA" ---
        if (isExpanded || scale > 0.01f) {
            Popup(alignment = Alignment.TopEnd, offset = IntOffset(-50, 115), properties = PopupProperties(focusable = true, dismissOnClickOutside = true), onDismissRequest = { isExpanded = false }) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(280.dp).graphicsLayer { scaleX = scale; scaleY = scale; alpha = scale.coerceIn(0f, 1f); transformOrigin = TransformOrigin(1f, 0f) }) {
                    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)).border(width = 1.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp))) {
                        Box(modifier = Modifier.background(cardBackground)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "ORDENAR POR", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.5.sp)
                                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.9f))
                                    Surface(onClick = { isExpanded = false; onApply() }, modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFF1A1F26), border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.7f), Color.Transparent))), shadowElevation = 6.dp) {
                                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp)) }
                                    }
                                }
                                // ... (Sigue el contenido de CompactItemButton igual que antes)

                                Spacer(modifier = Modifier.height(10.dp))

                                // --- BLOQUE DINÁMICO: OPCIONES DESDE EL VIEWMODEL ---
                                if (sortOptions.isNotEmpty()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        sortOptions.forEach { item ->
                                            CompactItemButton(
                                                item = item,
                                                isSelected = activeFilters.contains(item.id),
                                                onClick = { onAction(item.id) }
                                            )
                                        }
                                    }
                                }

                                // --- BLOQUE MANTENIDO: PARA HOMESCREEN (CARRUSEL) ---
                                if (showNombre || showRank || showViewModes) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        if (showNombre) {
                                            val isAsc = activeFilters.contains("sort_nombre_asc")
                                            val isDesc = activeFilters.contains("sort_nombre_desc")
                                            CompactItemButton(
                                                item = ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_nombre"),
                                                isSelected = isAsc || isDesc,
                                                onClick = { onAction(if (isAsc) "sort_nombre_desc" else if (isDesc) "" else "sort_nombre_asc") }
                                            )
                                        }
                                        if (showRank) {
                                            val isRAsc = activeFilters.contains("sort_rank_asc")
                                            val isRDesc = activeFilters.contains("sort_rank_desc")
                                            CompactItemButton(
                                                item = ControlItem("Rank", Icons.Default.Star, "⭐", Color(0xFF9B51E0), "sort_rank"),
                                                isSelected = isRAsc || isRDesc,
                                                onClick = { onAction(if (isRAsc) "sort_rank_desc" else if (isRDesc) "" else "sort_rank_asc") }
                                            )
                                        }
                                        if (showViewModes) {
                                            CompactItemButton(
                                                item = ControlItem("Grupos", Icons.Default.GridView, "🍱", Color(0xFF2197F5), "view_bento"),
                                                isSelected = activeFilters.contains("view_bento"),
                                                onClick = { onAction("view_bento") }
                                            )
                                            CompactItemButton(
                                                item = ControlItem("Grilla", Icons.Default.Dashboard, "📱", Color(0xFF9B51E0), "view_grid"),
                                                isSelected = activeFilters.contains("view_grid"),
                                                onClick = { onAction("view_grid") }
                                            )
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

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuOrdenamientoPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MenuOrdenamiento(
                activeFilters = setOf("sort_alpha"),
                sortOptions = listOf(ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_alpha")),
                onAction = {},
                onApply = {},
                onClearFilters = {}
            )
        }
    }
}
