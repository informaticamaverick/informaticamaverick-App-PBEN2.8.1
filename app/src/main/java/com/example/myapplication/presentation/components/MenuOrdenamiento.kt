package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun MenuOrdenamiento(
    activeFilters: Set<String>,
    onAction: (String) -> Unit, // Se dispara en cada clic para actualización real-time
    onApply: () -> Unit,       // Solo para cerrar el menú formalmente
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    showNombre: Boolean = false,
    showRank: Boolean = false,
    showViewModes: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasFilters = activeFilters.isNotEmpty()

    // --- ANIMACIÓN DE REBOTE PARA EL CONTENIDO ---
    // Usamos un float que va de 0 a 1 para controlar la escala con rebote
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isExpanded

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) {
            // Spring con bajo damping causa el efecto "boing" o rebote
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(durationMillis = 150)
        },
        label = "ReboteMenu"
    )

    val cardBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1F26), Color(0xFF0A0E14))
    )

    Box(modifier = modifier) {
        // --- 1. BOTONES BASE (X y Engranaje) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            // Botón X (Limpiar)
            if (hasFilters) {
                IconButton(
                    onClick = { onClearFilters() },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }

            // Engranaje (Solo emoji)
            Text(
                text = "⚙️",
                fontSize = 28.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isExpanded = !isExpanded }
            )
        }

        // --- 2. POPUP CON REBOTE ---
        if (isExpanded || scale > 0.01f) { // Mantenemos el popup vivo mientras la animación termina
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, -10), // Ajuste fino sobre el icono
                properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
                onDismissRequest = { isExpanded = false }
            ) {
                // Aplicamos la escala de rebote al contenedor
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .width(280.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            // El punto de origen es abajo a la derecha (donde está el icono)
                            transformOrigin = TransformOrigin(1f, 1f)
                            alpha = scale.coerceIn(0f, 1f)
                        }
                        .padding(bottom = 45.dp) // Espacio para que la cola apunte al icono
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(modifier = Modifier.background(cardBackground)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // HEADER
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ORDENAR POR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)

                                    // Botón aplicar (Cierra el menú)
                                    IconButton(
                                        onClick = {
                                            isExpanded = false
                                            onApply()
                                        },
                                        modifier = Modifier.size(24.dp).background(Color(0xFF10B981), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // BOTONES DE ACCIÓN (MULTISELECCIÓN REAL-TIME)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (showNombre) {
                                        val isAsc = activeFilters.contains("sort_nombre_asc")
                                        val isDesc = activeFilters.contains("sort_nombre_desc")

                                        // Cada clic llama a onAction, actualizando la lista externa inmediatamente
                                        CompactItemButton(
                                            item = ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_nombre"),
                                            isSelected = isAsc || isDesc,
                                            onClick = {
                                                val next = when {
                                                    isAsc -> "sort_nombre_desc"
                                                    isDesc -> ""
                                                    else -> "sort_nombre_asc"
                                                }
                                                onAction(next)
                                            },
                                            overlayEmoji = if (isAsc) "🔼" else if (isDesc) "🔽" else null
                                        )
                                    }

                                    if (showRank) {
                                        val isRAsc = activeFilters.contains("sort_rank_asc")
                                        val isRDesc = activeFilters.contains("sort_rank_desc")
                                        CompactItemButton(
                                            item = ControlItem("Rank", Icons.Default.Star, "⭐", Color(0xFF9B51E0), "sort_rank"),
                                            isSelected = isRAsc || isRDesc,
                                            onClick = {
                                                onAction(if (isRAsc) "sort_rank_desc" else if (isRDesc) "" else "sort_rank_asc")
                                            },
                                            overlayEmoji = if (isRAsc) "🔼" else if (isRDesc) "🔽" else null
                                        )
                                    }

                                    if (showViewModes) {
                                        val isBento = activeFilters.contains("view_bento")
                                        val isGrid = activeFilters.contains("view_grid")

                                        // Update instantáneo de modo de vista
                                        CompactItemButton(
                                            item = ControlItem("Grupos", Icons.Default.GridView, "🍱", Color(0xFF2197F5), "view_bento"),
                                            isSelected = isBento,
                                            onClick = { onAction("view_bento") }
                                        )
                                        CompactItemButton(
                                            item = ControlItem("Grilla", Icons.Default.Dashboard, "📱", Color(0xFF9B51E0), "view_grid"),
                                            isSelected = isGrid,
                                            onClick = { onAction("view_grid") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // COLA DE LA BURBUJA (Apunta al engranaje)
                    Box(
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .size(18.dp, 10.dp)
                            .background(Color(0xFF0A0E14), DownwardComicTailShape())
                    )
                }
            }
        }
    }
}

/**
 * Triángulo que apunta hacia abajo para la burbuja.
 */
fun DownwardComicTailShape() = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuOrdenamientoPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MenuOrdenamiento(
                activeFilters = setOf("sort_nombre_asc"),
                onAction = {},
                onApply = {},
                onClearFilters = {},
                showNombre = true,
                showViewModes = true
            )
        }
    }
}
