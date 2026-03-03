package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

// ==========================================================================================
// --- CONFIGURACIÓN VISUAL: GEMINI CYBERPUNK ---
// ==========================================================================================

val GeminiColors = listOf(
    Color(0xFF2197F5), // Azul
    Color(0xFF9B51E0), // Púrpura
    Color(0xFFE91E63)  // Rosa
)

@Composable
fun geminiGradientBrush(isAnimated: Boolean = true): Brush {
    val offset = if (isAnimated) {
        val infiniteTransition = rememberInfiniteTransition(label = "geminiAnim")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1500f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        ).value
    } else {
        0f
    }

    return Brush.linearGradient(
        colors = GeminiColors,
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Mirror
    )
}

@Composable
fun geminiGradientEffect(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "geminiAnim")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF4285F4)
        ),
        start = Offset(offsetAnim - 500f, offsetAnim - 500f),
        end = Offset(offsetAnim, offsetAnim)
    )
}

// ==========================================================================================
// --- MODELOS DE DATOS ---
// ==========================================================================================

data class ControlItem(
    val label: String,
    val icon: ImageVector?,
    val emoji: String,
    val color: Color,
    val id: String = label.lowercase()
)

// Enum para el tipo de pantalla/contexto
enum class ScreenContext {
    DEFAULT,
    CREATE_LICITACION
}

// ==========================================================================================
// --- ANIMACIONES DE CARGA (LOADING) MAVERICK PRO ---
// ==========================================================================================

/**
 * Indicador de carga futurista orbital.
 * Recreación exacta del HTML con doble anillo, núcleo pulsante y efecto de flote.
 */
@Composable
fun GeminiLoadingIndicator(modifier: Modifier = Modifier, size: Dp = 100.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_anim")

    // Rotación Anillo Exterior (Derecha)
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "rotOuter"
    )

    // Rotación Anillo Interior (Izquierda)
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "rotInner"
    )

    // Latido del núcleo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "pulse"
    )

    // Flote vertical del icono central
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "float"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        // Núcleo brillante pulsante
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .scale(pulse)
                .background(geminiGradientEffect(), CircleShape)
                .blur(20.dp)
                .alpha(0.5f)
        )

        // Anillo Exterior (Gira a la derecha)
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotationOuter)) {
            drawArc(
                brush = Brush.linearGradient(listOf(Color(0xFF2197F5), Color(0xFF9B51E0))),
                startAngle = 0f, sweepAngle = 270f, useCenter = false,
                style = Stroke(width = (size.value * 0.05f).dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Anillo Interior (Gira a la izquierda)
        Canvas(modifier = Modifier.size(size * 0.7f).rotate(rotationInner)) {
            drawArc(
                brush = Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF2197F5))),
                startAngle = 0f, sweepAngle = 180f, useCenter = false,
                style = Stroke(width = (size.value * 0.04f).dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Icono Central Eléctrico flotando
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(size * 0.35f)
                .offset(y = floatOffset.dp)
                .graphicsLayer {
                    shadowElevation = 15f
                    ambientShadowColor = Color.White
                    spotShadowColor = Color.White
                }
        )
    }
}

/**
 * Pantalla de Carga de Bloqueo Completo. Bloquea clics y muestra animación espectacular.
 */
@Composable
fun GeminiLoadingScreen(text: String = "Procesando Datos", modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_anim")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1500f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "textOffset"
    )

    // Fondo oscuro radial con bloqueo de clics
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2197F5).copy(alpha = 0.08f), Color(0xFF05070A)),
                    center = Offset.Infinite,
                    radius = 1500f
                )
            )
            .background(Color(0xFF05070A).copy(alpha = 0.90f))
            .pointerInput(Unit) {}, // Absorbe todos los toques para que el usuario no toque nada por detrás
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Animación Orbital
            GeminiLoadingIndicator(size = 100.dp)

            Spacer(modifier = Modifier.height(32.dp))

            // Texto con animación de brillo de gradiente
            val textBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF2197F5)),
                start = Offset(gradientOffset - 500f, 0f),
                end = Offset(gradientOffset, 0f)
            )

            Text(
                text = text.uppercase(),
                style = TextStyle(brush = textBrush),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        }
    }
}

// ==========================================================================================
// --- BOTONES COMPACTOS (ACCIONES LATERALES DEL FAB) ---
// ==========================================================================================

@Composable
fun SmallActionFab(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 64.dp, height = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) colors.primaryContainer else colors.surface,
        shadowElevation = 8.dp,
        border = if (isActive) BorderStroke(1.5.dp, rainbowBrush) else BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) colors.primary else iconColor,
                modifier = Modifier.align(Alignment.TopCenter).size(24.dp)
            )
            Text(
                text = label,
                color = if (isActive) colors.primary else colors.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ==========================================================================================
// --- GEMINI SPLIT FAB CON PANEL TÁCTICO ANIMADO V2 (UX PRO) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSplitFAB(
    isExpanded: Boolean,
    isSearchActive: Boolean,
    isSecondaryPanelVisible: Boolean = false,

    // --- ESTADOS DE CONTEXTO ---
    isMultiSelectionActive: Boolean = false,
    onToggleExpand: () -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onCloseSecondaryPanel: () -> Unit = {},

    // --- Control de visibilidad de secciones según el contexto de la pantalla ---
    screenContext: ScreenContext = ScreenContext.DEFAULT,

    // --- CALLBACKS DE ACCIONES ---
    onCompareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},

    // --- DATOS DEL PANEL ---
    activeFilters: Set<String> = emptySet(),
    dynamicCategories: List<ControlItem> = emptyList(),
    onAction: (String) -> Unit = {},
    onResetAll: () -> Unit = {},

    // --- BOTONES LATERALES POR DEFECTO ---
    secondaryActions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    val density = LocalDensity.current

    val fabIconRotation by animateFloatAsState(
        targetValue = if (isExpanded || isMultiSelectionActive) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabRotation"
    )

    // --- ESTADOS PARA EL RANGO DE FECHAS PERSONALIZADO ---
    var showCustomDatePopup by remember { mutableStateOf(false) }
    var selectingDateType by remember { mutableStateOf<String?>(null) } // "start" o "end"
    var tempStartDate by remember { mutableStateOf<Long?>(null) }
    var tempEndDate by remember { mutableStateOf<Long?>(null) }
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    // Control de visibilidad de secciones basado en screenContext
    val showCategories = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showFilters = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showSort = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showTools = remember(screenContext) { true } // Herramientas siempre visibles, o se puede customizar

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {

        // --- POPUP CUSTOM PARA RANGO DE FECHAS ---
        if (showCustomDatePopup) {
            Dialog(
                onDismissRequest = { showCustomDatePopup = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF161C24),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shadowElevation = 24.dp
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color(0xFF2197F5))
                            Spacer(Modifier.width(12.dp))
                            Text("Filtrar por Rango", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }

                        Spacer(Modifier.height(24.dp))

                        // Botón Fecha Inicio
                        Text("FECHA DE INICIO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = { selectingDateType = "start" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (tempStartDate != null) Color(0xFF2197F5) else Color.White.copy(0.1f))
                        ) {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = tempStartDate?.let { dateFormatter.format(Date(it)) } ?: "Toca para seleccionar",
                                    color = if (tempStartDate != null) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botón Fecha Fin
                        Text("FECHA DE FIN", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = { selectingDateType = "end" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (tempEndDate != null) Color(0xFF9B51E0) else Color.White.copy(0.1f))
                        ) {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = tempEndDate?.let { dateFormatter.format(Date(it)) } ?: "Toca para seleccionar",
                                    color = if (tempEndDate != null) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = { showCustomDatePopup = false },
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (tempStartDate != null && tempEndDate != null) {
                                        onAction("date_range_${tempStartDate}_${tempEndDate}")
                                        showCustomDatePopup = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                enabled = tempStartDate != null && tempEndDate != null
                            ) {
                                Text("APLICAR", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // --- SELECTOR DE FECHA NATIVO (Se abre desde el PopUp) ---
        if (selectingDateType != null) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = if (selectingDateType == "start") tempStartDate else tempEndDate
            )
            DatePickerDialog(
                onDismissRequest = { selectingDateType = null },
                confirmButton = {
                    TextButton(onClick = {
                        if (selectingDateType == "start") tempStartDate = datePickerState.selectedDateMillis
                        else tempEndDate = datePickerState.selectedDateMillis
                        selectingDateType = null
                    }) {
                        Text("Confirmar", color = Color(0xFF2197F5), fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectingDateType = null }) { Text("Atrás", color = Color.Gray) }
                },
                colors = DatePickerDefaults.colors(containerColor = Color(0xFF161C24))
            ) {
                DatePicker(
                    state = datePickerState,
                    title = { Text(if (selectingDateType == "start") "Fecha de Inicio" else "Fecha de Fin", modifier = Modifier.padding(16.dp)) },
                    colors = DatePickerDefaults.colors(
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        selectedDayContainerColor = Color(0xFF2197F5),
                        todayContentColor = Color(0xFF2197F5),
                        todayDateBorderColor = Color(0xFF2197F5)
                    )
                )
            }
        }

        // --- SCRIM OSCURO REQUERIDO (Fondo translúcido al abrir el panel) ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleExpand // Cierra al tocar fuera
                    )
            )
        }

        // --- CENTRO DE CONTROL TÁCTICO V2 ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(250)) +
                    scaleIn(
                        initialScale = 0.8f,
                        transformOrigin = TransformOrigin(1f, 1f),
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) +
                    slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(200)) +
                    scaleOut(
                        targetScale = 0.8f,
                        transformOrigin = TransformOrigin(1f, 1f),
                        animationSpec = tween(200)
                    ) +
                    slideOutVertically(targetOffsetY = { 100 }, animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            TacticalControlPanel(
                activeFilters = activeFilters,
                dynamicCategories = dynamicCategories,
                onAction = onAction,
                onApply = {
                    onAction("apply_filters")
                    onToggleExpand()
                },
                isMultiSelectionActive = isMultiSelectionActive,
                onCompareClick = onCompareClick,
                onDeleteClick = onDeleteClick,
                onShareClick = onShareClick,
                showCategories = showCategories,
                showFilters = showFilters,
                showSort = showSort,
                showTools = showTools,
                onOpenCalendar = { showCustomDatePopup = true } // <-- AQUÍ SE CONECTA EL POPUP
            )
        }

        // --- BOTONES FAB PRINCIPALES Y SECUNDARIOS ---
        Row(
            modifier = Modifier.padding(12.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = !isSearchActive && !isExpanded && !isSecondaryPanelVisible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                    AnimatedVisibility(visible = activeFilters.isNotEmpty() && !isMultiSelectionActive && showFilters) { // Only show clear filters if filters are active and visible
                        Surface(
                            onClick = {
                                onResetAll()
                                if (isExpanded) onToggleExpand()
                            },
                            modifier = Modifier.size(height = 56.dp, width = 56.dp),
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar Filtros", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    AnimatedVisibility(visible = isMultiSelectionActive) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                            SmallActionFab(icon = Icons.AutoMirrored.Filled.CompareArrows, label = "Comp.", iconColor = Color(0xFF2197F5), onClick = onCompareClick)
                            SmallActionFab(icon = Icons.Default.Delete, label = "Elim.", iconColor = Color(0xFFE91E63), onClick = onDeleteClick)
                        }
                    }

                    if (!isMultiSelectionActive) {
                        secondaryActions()
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = !isSearchActive && !isMultiSelectionActive && showFilters, // Only show search if filters are enabled
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    // BOTÓN BÚSQUEDA ASIMÉTRICO MODERNO
                    Surface(
                        onClick = onActivateSearch,
                        modifier = Modifier.height(56.dp).wrapContentWidth().defaultMinSize(minWidth = 56.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        color = colors.surface,
                        border = BorderStroke(2.dp, rainbowBrush),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 20.dp, end = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(0.8f), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Buscar", fontSize = 14.sp, fontWeight = FontWeight.Black, color = colors.onSurface)
                        }
                    }
                }

                // CONTENEDOR BOX PARA EL FAB CIRCULAR + BADGE SUPERPUESTO REAL
                Box(contentAlignment = Alignment.TopEnd) {
                    Surface(
                        onClick = {
                            when {
                                isSecondaryPanelVisible -> onCloseSecondaryPanel()
                                isSearchActive -> onCloseSearch()
                                isMultiSelectionActive -> onAction("toggle_multiselect")
                                else -> onToggleExpand()
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(2.dp, rainbowBrush),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isExpanded || isSearchActive || isSecondaryPanelVisible || isMultiSelectionActive) Icons.Default.Close else Icons.Default.Settings,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(26.dp).rotate(fabIconRotation)
                            )
                        }
                    }

                    // BADGE SUPERPUESTO (Fuera del Surface principal)
                    if (!isExpanded && !isSearchActive && !isMultiSelectionActive && activeFilters.isNotEmpty() && showFilters) { // Only show badge if filters are active and visible
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(22.dp)
                                .background(Color(0xFFE91E63), CircleShape)
                                .border(2.dp, Color(0xFF05070A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeFilters.size.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Panel Táctico Horizontal Animado V2 con Soporte Multiselección y Diseño Ultra-Compacto
 */
@Composable
fun TacticalControlPanel(
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    onAction: (String) -> Unit,
    onApply: () -> Unit,
    isMultiSelectionActive: Boolean = false,
    showCategories: Boolean = true,
    showFilters: Boolean = true,
    showSort: Boolean = true,
    showTools: Boolean = true,
    onCompareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onOpenCalendar: () -> Unit = {}
) {
    var categoriesExpanded by rememberSaveable { mutableStateOf(true) }
    var filtersExpanded by rememberSaveable { mutableStateOf(true) }
    var orderExpanded by rememberSaveable { mutableStateOf(true) }
    var toolsExpanded by rememberSaveable { mutableStateOf(false) } // 🔥 Por defecto colapsado

    val darkGradientBg = Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF05070A)))

    Box(modifier = Modifier.fillMaxWidth(0.96f).wrapContentHeight()) {

        Surface(
            onClick = onApply,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(40.dp)
                .zIndex(10f),
            shape = CircleShape,
            color = Color(0xFF10B981),
            border = BorderStroke(1.5.dp, Color(0xFF059669)),
            shadowElevation = 15.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = "Aplicar", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shadowElevation = 25.dp
        ) {
            Box(modifier = Modifier.background(darkGradientBg)) {
                Column(modifier = Modifier.padding(vertical = 12.dp).verticalScroll(rememberScrollState())) {

                    // --- SECCIÓN 1: CATEGORÍAS ---
                    if (showCategories && dynamicCategories.isNotEmpty()) {
                        PanelSection(
                            title = "Categorías",
                            icon = Icons.Default.Category,
                            isExpanded = categoriesExpanded,
                            onToggleExpand = { categoriesExpanded = !categoriesExpanded }
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(dynamicCategories) { item ->
                                    CategoryTag(
                                        item = item,
                                        isSelected = activeFilters.contains(item.id),
                                        onClick = { onAction(item.id) }
                                    )
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 2: FILTROS TÉCNICOS ---
                    if (showFilters) {
                        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                            PanelSection(
                                title = "Refinar búsqueda",
                                icon = Icons.Default.FilterList,
                                isExpanded = filtersExpanded,
                                onToggleExpand = { filtersExpanded = !filtersExpanded }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Fila 1 (6 espacios máximos)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)) {
                                        val row1 = listOf(
                                            ControlItem("Local", Icons.Default.Storefront, "🏪", Color(0xFF2197F5), "filter_local"),
                                            ControlItem("Envios", Icons.Default.LocalShipping, "🚚", Color(0xFF9B51E0), "filter_envios"),
                                            ControlItem("24hs", Icons.Default.AccessTimeFilled, "⏳", Color(0xFFFF9800), "filter_24hs"),
                                            ControlItem("Turnos", Icons.Default.EventAvailable, "📅", Color(0xFF00FFC2), "filter_turnos"),
                                            ControlItem("Fast", Icons.Default.Bolt, "⚡", Color(0xFFFFEB3B), "filter_fast"),
                                            ControlItem("Verif.", Icons.Default.Verified, "✅", Color(0xFF9B51E0), "filter_verif")
                                        )
                                        row1.forEach { item ->
                                            CompactItemButton(item = item, isSelected = activeFilters.contains(item.id), onClick = { onAction(item.id) })
                                        }
                                    }
                                    // Fila 2
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                        val row2 = listOf(
                                            ControlItem("Favs", Icons.Default.Favorite, "❤️", Color(0xFFE91E63), "filter_favs"),
                                            ControlItem("Cerca", Icons.Default.LocationOn, "📍", Color(0xFF4CAF50), "filter_cerca")
                                        )
                                        row2.forEach { item ->
                                            CompactItemButton(item = item, isSelected = activeFilters.contains(item.id), onClick = { onAction(item.id) })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 3: ORDEN TÁCTICO (Inteligente Toggle) ---
                    if (showSort) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PanelSection(
                                title = "Orden táctico",
                                icon = Icons.AutoMirrored.Filled.Sort,
                                isExpanded = orderExpanded,
                                onToggleExpand = { orderExpanded = !orderExpanded }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {

                                    // NOMBRE (A-Z / Z-A) con Icono + Flecha
                                    val isNombreAsc = activeFilters.contains("sort_nombre_asc")
                                    val isNombreDesc = activeFilters.contains("sort_nombre_desc")
                                    val nombreSortArrow = when {
                                        isNombreAsc -> "🔼"
                                        isNombreDesc -> "🔽"
                                        else -> null
                                    }
                                    val nombreItem = ControlItem(
                                        label = "Nombre",
                                        icon = Icons.Default.SortByAlpha,
                                        emoji = "A", // Main emoji when selected
                                        color = Color(0xFF2197F5),
                                        id = "sort_nombre"
                                    )
                                    CompactItemButton(
                                        item = nombreItem,
                                        isSelected = isNombreAsc || isNombreDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isNombreAsc -> "sort_nombre_desc" // Was Asc, now Desc
                                                isNombreDesc -> "" // Was Desc, now Off
                                                else -> "sort_nombre_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = nombreSortArrow,
                                        overlayAlignment = if (isNombreAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    // FECHA (Soporta Click Largo para abrir Rango de Fechas)
                                    val isFechaAsc = activeFilters.contains("sort_fecha_asc")
                                    val isFechaDesc = activeFilters.contains("sort_fecha_desc")
                                    val hasDateRange = activeFilters.any { it.startsWith("date_range_") }
                                    val fechaSortArrow = when {
                                        isFechaAsc -> "🔼"
                                        isFechaDesc -> "🔽"
                                        else -> null
                                    }
                                    val fechaItem = ControlItem(
                                        label = "Fecha",
                                        icon = Icons.Default.CalendarToday,
                                        emoji = "D", // Main emoji when selected
                                        color = Color(0xFFFACC15),
                                        id = "sort_fecha"
                                    )
                                    CompactItemButton(
                                        item = fechaItem,
                                        isSelected = isFechaAsc || isFechaDesc || hasDateRange,
                                        onClick = {
                                            val newFilterId = when {
                                                isFechaAsc -> "sort_fecha_desc" // Was Asc, now Desc
                                                isFechaDesc -> "" // Was Desc, now Off
                                                else -> "sort_fecha_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        onLongClick = onOpenCalendar,
                                        overlayEmoji = fechaSortArrow,
                                        overlayAlignment = if (isFechaAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    // PRECIO
                                    val isPrecioAsc = activeFilters.contains("sort_precio_asc")
                                    val isPrecioDesc = activeFilters.contains("sort_precio_desc")
                                    val precioSortArrow = when {
                                        isPrecioAsc -> "🔼"
                                        isPrecioDesc -> "🔽"
                                        else -> null
                                    }
                                    val precioItem = ControlItem(
                                        label = "Precio",
                                        icon = Icons.Default.AttachMoney,
                                        emoji = "$", // Main emoji when selected
                                        color = Color(0xFF10B981),
                                        id = "sort_precio"
                                    )
                                    CompactItemButton(
                                        item = precioItem,
                                        isSelected = isPrecioAsc || isPrecioDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isPrecioAsc -> "sort_precio_desc" // Was Asc, now Desc
                                                isPrecioDesc -> "" // Was Desc, now Off
                                                else -> "sort_precio_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = precioSortArrow,
                                        overlayAlignment = if (isPrecioAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    // RANKING
                                    val isRankAsc = activeFilters.contains("sort_rank_asc")
                                    val isRankDesc = activeFilters.contains("sort_rank_desc")
                                    val rankSortArrow = when {
                                        isRankAsc -> "🔼"
                                        isRankDesc -> "🔽"
                                        else -> null
                                    }
                                    val rankItem = ControlItem(
                                        label = "Rank",
                                        icon = Icons.Default.Star,
                                        emoji = "⭐", // Main emoji when selected
                                        color = Color(0xFF9B51E0),
                                        id = "sort_rank"
                                    )
                                    CompactItemButton(
                                        item = rankItem,
                                        isSelected = isRankAsc || isRankDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isRankAsc -> "sort_rank_desc" // Was Asc, now Desc
                                                isRankDesc -> "" // Was Desc, now Off
                                                else -> "sort_rank_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = rankSortArrow,
                                        overlayAlignment = if (isRankAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 4: GESTIÓN Y HERRAMIENTAS ---
                    if (showTools) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PanelSection(
                                title = "Herramientas",
                                icon = Icons.Default.Build,
                                isExpanded = toolsExpanded,
                                onToggleExpand = { toolsExpanded = !toolsExpanded }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                    val multiItem = ControlItem("Multi", Icons.Default.Layers, "📑", Color(0xFFE91E63), "toggle_multiselect")
                                    val simChatItem = ControlItem("Sim Chat", Icons.Default.Forum, "💬", Color(0xFF22D3EE), "sim_chat")
                                    CompactItemButton(item = simChatItem, isSelected = false, onClick = { onAction(simChatItem.id) })

                                    val simLicItem = ControlItem("Sim Lic", Icons.Default.Gavel, "⚖️", Color(0xFFFACC15), "sim_lic")
                                    CompactItemButton(item = simLicItem, isSelected = false, onClick = { onAction(simLicItem.id) })

                                    if (isMultiSelectionActive) {
                                        CompactItemButton(item = multiItem, isSelected = true, onClick = { onAction("toggle_multiselect") })
                                        val compareItem = ControlItem("Comparar", Icons.AutoMirrored.Filled.CompareArrows, "⚖️", Color(0xFF2197F5), "compare")
                                        CompactItemButton(item = compareItem, isSelected = false, onClick = { onCompareClick() })
                                        val deleteItem = ControlItem("Eliminar", Icons.Default.Delete, "🗑️", Color(0xFFEF4444), "delete")
                                        CompactItemButton(item = deleteItem, isSelected = false, onClick = { onDeleteClick() })
                                        val shareItem = ControlItem("Share", Icons.Default.Share, "📤", Color(0xFFFACC15), "share")
                                        CompactItemButton(item = shareItem, isSelected = false, onClick = { onShareClick() })

                                        val refreshItem = ControlItem("Refresh", Icons.Default.Refresh, "🔄", Color(0xFF10B981), "refresh")
                                        CompactItemButton(item = refreshItem, isSelected = false, onClick = { onAction(refreshItem.id) })

                                    } else {
                                        CompactItemButton(item = multiItem, isSelected = activeFilters.contains(multiItem.id), onClick = { onAction("toggle_multiselect") })

                                        val copyItem = ControlItem("Copiar", Icons.Default.ContentCopy, "📋", Color(0xFFFACC15), "tool_copy")
                                        CompactItemButton(item = copyItem, isSelected = activeFilters.contains(copyItem.id), onClick = { onAction(copyItem.id) })

                                        val editItem = ControlItem("Editar", Icons.Default.Edit, "✏️", Color(0xFF2197F5), "tool_edit")
                                        CompactItemButton(item = editItem, isSelected = activeFilters.contains(editItem.id), onClick = { onAction(editItem.id) })

                                        val shareItem = ControlItem("Share", Icons.Default.Share, "📤", Color(0xFFFACC15), "tool_share")
                                        CompactItemButton(item = shareItem, isSelected = activeFilters.contains(shareItem.id), onClick = { onShareClick() })

                                        val deleteItem = ControlItem("Eliminar", Icons.Default.Delete, "🗑️", Color(0xFFEF4444), "tool_delete")
                                        CompactItemButton(item = deleteItem, isSelected = activeFilters.contains(deleteItem.id), onClick = { onDeleteClick() })
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
 * Divisor Blanco de alto contraste
 */
@Composable
fun DividerWhite(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.3f), // Más visible
        thickness = 1.dp
    )
}

/**
 * Encabezado colapsable para las secciones del panel táctico.
 */
@Composable
fun PanelSection(title: String, icon: ImageVector, isExpanded: Boolean, onToggleExpand: () -> Unit, content: @Composable () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)

            Spacer(Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, animationSpec = tween(200))
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Ocultar" else "Mostrar",
                tint = Color.White,
                modifier = Modifier.size(22.dp).rotate(arrowRotation)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) + fadeIn(tween(250)),
            exit = shrinkVertically(animationSpec = tween(200, easing = FastOutLinearInEasing)) + fadeOut(tween(200))
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal =1.dp, vertical = 1.dp)) {
                content()
            }
        }
    }
}

/**
 * Etiqueta Tipo "Pill" para las categorías.
 */
@Composable
fun CategoryTag(item: ControlItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if(isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
    val borderColor = if(isSelected) item.color else Color.White.copy(alpha = 0.15f)
    val textColor = if(isSelected) Color.White else Color.LightGray

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(item.emoji, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(item.label.uppercase(), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

/**
 * Componente más compacto para los botones de las grillas del panel.
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(10.dp)
                )
                .border(
                    if (isSelected) 1.5.dp else 0.8.dp,
                    if (isSelected) item.color else Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = item.emoji,
                    fontSize = 24.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = item.color,
                            offset = Offset(0f, 0f),
                            blurRadius = 25f
                        )
                    )
                )
            } else {
                item.icon?.let {
                    Icon(it, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
                } ?: run {
                    Text(item.emoji, fontSize = 20.sp, modifier = Modifier.alpha(0.6f))
                }
            }

            overlayEmoji?.let { emoji ->
                Text(
                    text = emoji,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(overlayAlignment)
                        .offset(
                            x = if (overlayAlignment == Alignment.TopEnd) 6.dp else 6.dp,
                            y = if (overlayAlignment == Alignment.TopEnd) (-6).dp else 6.dp
                        )
                        .graphicsLayer {
                            shadowElevation = 10f
                            ambientShadowColor = Color.Black.copy(alpha = 0.6f)
                            spotShadowColor = Color.Black.copy(alpha = 0.6f)
                        }
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

// =================================================================================
// --- COMPONENTES DE BÚSQUEDA Y NAVEGACIÓN ---
// =================================================================================

@Composable
fun GeminiTopSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholderText: String = "Buscar...",
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 8.dp, bottomEnd = 8.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, rainbowBrush)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(0.8f), modifier = Modifier.padding(start = 16.dp).size(18.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focusRequester),
                textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) { Text(placeholderText, color = colors.onSurfaceVariant, fontSize = 14.sp) }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
fun GeminiFABWithScrim(
    bottomPadding: PaddingValues,
    showScrim: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showScrim,
            enter = fadeIn(animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 1f)))))
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(bottomPadding).navigationBarsPadding().padding(bottom = 8.dp, end = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            content()
        }
    }
}

// =================================================================================
// --- COMPONENTES UI ADICIONALES Y TARJETAS ---
// =================================================================================

@Composable
fun ServiceTag(text: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (color.luminance() > 0.4f) Color.Black else Color.White, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

@Composable
fun RowItemDetail(icon: ImageVector, text: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isActive) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = if (isActive) Color.White else Color.Gray.copy(alpha = 0.5f), textDecoration = if (!isActive) TextDecoration.LineThrough else null)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorCard(
    provider: Provider,
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null,
    onDeleteRequest: (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null,
    onToggleFavorite: ((String, Boolean) -> Unit)? = null,
    viewMode: String = "Detallada",
    showAvatars: Boolean = true,
    showPreviews: Boolean = true,
    showBadges: Boolean = true,
    allCategories: List<CategoryEntity> = emptyList()
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    val staticBrush = geminiGradientBrush(isAnimated = false)
    val animateBrush = geminiGradientBrush(isAnimated = true)

    val activeColor = Color(0xFF22D3EE)
    val inactiveColor = Color.White.copy(alpha = 0.15f)
    val cyberBackground = Color(0xFF0A0E14)
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1F26),
            Color(0xFF0A0E14)
        ))

    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""
    val servicesToShow = mainCompany?.categories ?: provider.categories

    val works24h = mainCompany?.works24h ?: provider.works24h
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: provider.doesHomeVisits
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: provider.hasPhysicalLocation
    val acceptsAppointments = mainCompany?.acceptsAppointments ?: provider.acceptsAppointments

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(staticBrush, RoundedCornerShape(26.dp))
            .padding(1.5.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.5.dp))
                .combinedClickable(
                    onClick = { showDetailSheet = true },
                    onLongClick = { showContextMenu = true }
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.5.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .background(cardGradient)
                    .matchParentSize().background(Color.White.copy(alpha = 0.05f)))

                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showAvatars) {
                        Box(contentAlignment = Alignment.TopStart) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .clickable { onClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = provider.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    fallback = painterResource(id = R.drawable.iconapp),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (provider.isOnline && showBadges) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                        .border(2.dp, cyberBackground, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }) {
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (provider.isVerified && showBadges) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(18.dp))
                            }
                        }

                        if (companyName.isNotEmpty()) {
                            Text(
                                text = companyName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF22D3EE).copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable { onClick() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (showBadges) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (works24h) Icon(Icons.Default.AccessTimeFilled, "24Hs", modifier = Modifier.size(18.dp), tint = Color(0xFFFF9800))
                                    if (hasPhysicalLocation) Icon(Icons.Default.Storefront, "Local", modifier = Modifier.size(18.dp), tint = Color(0xFF2197F5))
                                    if (doesHomeVisits) Icon(Icons.Default.LocalShipping, "Visitas", modifier = Modifier.size(18.dp), tint = Color(0xFF9B51E0))
                                    if (acceptsAppointments) Icon(Icons.Default.EventAvailable, "Turnos", modifier = Modifier.size(18.dp), tint = Color(0xFF00FFC2))
                                }
                            }

                            IconButton(onClick = { showFavoriteDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (provider.isFavorite) Color.Red else Color.Gray)
                            }
                        }
                    }

                    if (actionContent != null) {
                        actionContent()
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onChat?.invoke() },
                            modifier = Modifier.size(44.dp).background(animateBrush, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) {
            DropdownMenuItem(text = { Text("Ver Perfil Completo") }, leadingIcon = { Icon(Icons.Default.Person, null) }, onClick = { showContextMenu = false; onClick() })
            HorizontalDivider()
            DropdownMenuItem(text = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, leadingIcon = { Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (provider.isFavorite) Color.Red else Color.Unspecified) }, onClick = { showContextMenu = false; showFavoriteDialog = true })
        }
    }

    if (showDetailSheet) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = cyberBackground, tonalElevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = provider.photoUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.width(16.dp)); Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(text = provider.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White); if (provider.isVerified) { Spacer(modifier = Modifier.width(6.dp)); Icon(Icons.Filled.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(24.dp)) } }
                    if (companyName.isNotEmpty()) { Text(text = companyName, style = MaterialTheme.typography.titleSmall, color = Color(0xFF22D3EE)) }
                    Text(text = provider.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                }
                Spacer(modifier = Modifier.height(24.dp)); Text("Servicios Ofrecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    servicesToShow.forEach { service ->
                        val catColorLong = allCategories.find { it.name.equals(service, ignoreCase = true) }?.color
                        val tagColor = if (catColorLong != null) Color(catColorLong) else Color.White.copy(alpha = 0.15f)
                        ServiceTag(text = service, color = tagColor)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = Color.White.copy(0.5f)); Spacer(modifier = Modifier.height(24.dp))
                RowItemDetail(icon = Icons.Default.Schedule, text = "Disponible 24hs", isActive = works24h)
                RowItemDetail(icon = Icons.Default.Home, text = "Visitas a Domicilio", isActive = doesHomeVisits)
                RowItemDetail(icon = Icons.Default.Storefront, text = "Local Físico", isActive = hasPhysicalLocation)
                RowItemDetail(icon = Icons.Default.Event, text = "Turnos / Citas", isActive = acceptsAppointments)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showDetailSheet = false; onClick() }, modifier = Modifier.fillMaxWidth().height(36.dp).background(animateBrush, RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(28.dp)) {
                    Text("Ver Perfil Completo", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showFavoriteDialog) {
        AlertDialog(onDismissRequest = { showFavoriteDialog = false }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }, title = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, text = { Text(if (provider.isFavorite) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") }, confirmButton = { TextButton(onClick = { onToggleFavorite?.invoke(provider.id, !provider.isFavorite); showFavoriteDialog = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { showFavoriteDialog = false }) { Text("Cancelar") } })
    }
}

// ==========================================================================================
// --- CARRUSELES Y CARDS ---
// ==========================================================================================

@Composable
fun CompactCategoryCard(item: CategoryEntity, onClick: () -> Unit) {
    val baseColor = Color(item.color)
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = baseColor), modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)), elevation = CardDefaults.cardElevation(0.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache {
                val gradient = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent), start = Offset(0f, 0f), end = Offset(size.width, size.height))
                onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) }
            })
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent), startX = 0f, endX = 450f)))
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight().padding(start = 8.dp, end = 2.dp, top = if (item.isNew) 18.dp else 0.dp), contentAlignment = Alignment.CenterStart) {
                    Column {
                        AutoResizingText(text = item.name.uppercase(), color = Color.White, maxFontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.width(24.dp).height(1.5.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    }
                }
                Box(modifier = Modifier.width(1.5.dp).height(85.dp).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent))))
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) { Text(text = item.icon, fontSize = 90.sp, modifier = Modifier.offset(x = 10.dp).graphicsLayer(alpha = 1f)) }
            }
            if (item.isNew) { Surface(color = Color(0xFFFFD600), shape = RoundedCornerShape(bottomEnd = 12.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "NUEVO", color = Color.Black, fontSize = 7.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), letterSpacing = 0.5.sp) } }
        }
    }
}

@Composable
private fun AutoResizingText(text: String, color: Color, maxFontSize: TextUnit, minFontSize: TextUnit = 8.sp) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(text = text, color = if (readyToDraw) color else Color.Transparent, fontWeight = FontWeight.Black, fontSize = fontSize, lineHeight = fontSize * 1.1f, maxLines = 2, letterSpacing = 1.1.sp, overflow = TextOverflow.Clip, onTextLayout = { result -> if (result.hasVisualOverflow && fontSize > minFontSize) { fontSize = (fontSize.value * 0.9f).sp } else { readyToDraw = true } }, modifier = Modifier.fillMaxWidth())
}

enum class BannerType(val label: String) {
    GOOGLE_AD("SPONSORED"),
    PROMO("PROMOCIÓN"),
    NEW_CATEGORY("NUEVA CATEGORÍA"),
    NEW_PROVIDER("NUEVOS PRESTADORES"),
    PRODUCT_SALE("VENTA DE PRODUCTO"),
    SERVICE_SALE("SERVICIO DESTACADO")
}

data class AccordionBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val type: BannerType,
    val originalCategory: CategoryEntity? = null,
    val isNew: Boolean = false,
    val imageUrl: String? = null,
    val discount: Int? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumLensCarousel(
    items: List<AccordionBanner>,
    onSettingsClick: () -> Unit,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 5000L
) {
    if (items.isEmpty()) return

    var expandedMenu by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var tempFilters by remember { mutableStateOf<Set<String>>(emptySet()) }

    val filteredItems = remember(items, activeFilters) {
        if (activeFilters.isEmpty()) items
        else items.filter {
            if (it.type == BannerType.GOOGLE_AD) true
            else {
                val isNovedad = it.type == BannerType.NEW_CATEGORY || it.type == BannerType.NEW_PROVIDER
                val isPromo = it.type == BannerType.PROMO || it.discount != null
                val isProd = it.type == BannerType.PRODUCT_SALE
                val isServ = it.type == BannerType.SERVICE_SALE

                (activeFilters.contains("NOVEDADES") && isNovedad) ||
                        (activeFilters.contains("PROMOCIONES") && isPromo) ||
                        (activeFilters.contains("PRODUCTOS") && isProd) ||
                        (activeFilters.contains("SERVICIOS") && isServ)
            }
        }
    }

    val displayItems = filteredItems.ifEmpty { items }

    val infiniteCount = Int.MAX_VALUE
    val initialPage = infiniteCount / 2 - (infiniteCount / 2 % displayItems.size.coerceAtLeast(1))
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }

    LaunchedEffect(key1 = displayItems) {
        while (true) {
            delay(autoplayDelay)
            if (displayItems.size > 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DESTACADOS & NOVEDADES",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Box {
                if (activeFilters.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            activeFilters = emptySet()
                            tempFilters = emptySet()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar Filtros", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    }
                } else {
                    IconButton(
                        onClick = {
                            tempFilters = activeFilters
                            expandedMenu = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Filtros", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(Color(0xFF161C24)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FILTROS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        IconButton(
                            onClick = {
                                activeFilters = tempFilters
                                expandedMenu = false
                            },
                            modifier = Modifier.size(24.dp).background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Aplicar", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    val options = listOf(
                        "NOVEDADES" to "🚀",
                        "PROMOCIONES" to "🔥",
                        "PRODUCTOS" to "🛍️",
                        "SERVICIOS" to "🛠️"
                    )

                    options.forEach { (option, emoji) ->
                        val isSelected = tempFilters.contains(option)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color(0xFF2197F5) else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF2197F5), modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                tempFilters = if (isSelected) tempFilters - option else tempFilters + option
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(300.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(start = 10.dp, end = 64.dp),
            modifier = Modifier.fillMaxWidth().height(120.dp)
        ) { index ->
            val actualIndex = index % displayItems.size
            val item = displayItems[actualIndex]

            val pageOffset = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
            Box(modifier = Modifier.graphicsLayer {
                val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                scaleX = scale
                scaleY = scale
                alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            }) {
                if (item.type == BannerType.GOOGLE_AD) {
                    AdBannerItem(item = item)
                } else {
                    PremiumBannerItem(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun AdBannerItem(item: AccordionBanner) {
    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AdsClick, null, tint = Color.Gray); Text(item.title, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp)); Text(item.subtitle, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp)) } }
            Box(modifier = Modifier.align(Alignment.TopStart).background(Color(0xFFFFC107), RoundedCornerShape(bottomEnd = 12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("ANUNCIO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black) }
        }
    }
}

@Composable
fun PremiumBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxSize().clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = item.color), elevation = CardDefaults.cardElevation(8.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
            Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache { val gradient = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent), start = Offset(0f, 0f), end = Offset(size.width, size.height)); onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) } })
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.4f), Color.Transparent), startX = 0f, endX = 600f)))
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(0.85f).fillMaxHeight().padding(start = 10.dp, top = 20.dp, bottom = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Column { AutoResizingText(text = item.title.uppercase(), color = Color.White, maxFontSize = 20.sp); Text(text = item.subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp); Spacer(modifier = Modifier.height(10.dp)); Box(modifier = Modifier.width(40.dp).height(3.dp).background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))) }
                }
                Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.7f).background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent))))

                Box(modifier = Modifier.weight(0.35f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = item.icon,
                        fontSize = 100.sp,
                        modifier = Modifier.offset(x = 20.dp),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.6f),
                                offset = Offset(-10f, 15f),
                                blurRadius = 20f
                            )
                        )
                    )
                }
            }
            if (item.isNew) {
                Surface(color = Color(0xFFFFD600), shape = RoundedCornerShape(bottomEnd = 16.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "NUEVO", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) }
            } else if (item.discount != null) {
                Surface(color = Color(0xFFE91E63), shape = RoundedCornerShape(bottomEnd = 16.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "${item.discount}% OFF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) }
            }
        }
    }
}

@Composable
fun GeminiCyberWrapper(modifier: Modifier = Modifier, cornerRadius: Dp = 24.dp, borderThickness: Dp = 1.5.dp, isAnimated: Boolean = false, showGlow: Boolean = true, content: @Composable () -> Unit) {
    val geminiBrush = geminiGradientBrush(isAnimated = isAnimated)
    val cyberBackground = Color(0xFF0A0E14)
    Box(modifier = modifier) {
        if (showGlow) Box(modifier = Modifier.matchParentSize().padding(borderThickness).blur(15.dp).background(geminiBrush, RoundedCornerShape(cornerRadius)).graphicsLayer { alpha = 0.3f })
        Box(modifier = Modifier.fillMaxWidth().background(geminiBrush, RoundedCornerShape(cornerRadius)).padding(borderThickness)) {
            Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadius - borderThickness)), color = cyberBackground) {
                Box(modifier = Modifier.fillMaxWidth()) { Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.05f))); content() }
            }
        }
    }
}

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun LoadingPreview() {
    MyApplicationTheme {
        GeminiLoadingScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TacticalFABPreview() {
    var expanded by remember { mutableStateOf(true) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isMultiSelectionActive by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf(setOf("filter_verif", "sort_precio_asc")) }

    val mockCategories = listOf(
        ControlItem("Informática", null, "💻", Color(0xFFB2EBF2), "cat_info"),
        ControlItem("Mecánica", null, "🔧", Color(0xFFFFDAC1), "cat_mec"),
        ControlItem("Limpieza", null, "🧹", Color(0xFFFAD2E1), "cat_limp")
    )

    MyApplicationTheme {
        Column {
            // Preview para ScreenContext.DEFAULT
            Text("Default Screen Context", color = Color.White)
            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color(0xFF0A0E14))) {
                GeminiSplitFAB(
                    isExpanded = expanded,
                    isSearchActive = isSearchActive,
                    isMultiSelectionActive = isMultiSelectionActive,
                    onToggleExpand = { expanded = !expanded },
                    onActivateSearch = { isSearchActive = true; expanded = false },
                    onCloseSearch = { isSearchActive = false },
                    onCompareClick = { },
                    onDeleteClick = { },
                    onShareClick = { },
                    activeFilters = activeFilters,
                    dynamicCategories = mockCategories,
                    onAction = { actionString ->
                        val currentActiveFilters = activeFilters.toMutableSet()
                        when (actionString) {
                            "toggle_multiselect" -> isMultiSelectionActive = !isMultiSelectionActive
                            "apply_filters" -> { /* Lógica de aplicar */ }
                            // Sort actions
                            "sort_nombre_asc" -> {
                                currentActiveFilters.remove("sort_nombre_desc")
                                currentActiveFilters.add("sort_nombre_asc")
                            }
                            "sort_nombre_desc" -> {
                                currentActiveFilters.remove("sort_nombre_asc")
                                currentActiveFilters.add("sort_nombre_desc")
                            }
                            "sort_fecha_asc" -> {
                                currentActiveFilters.remove("sort_fecha_desc")
                                currentActiveFilters.add("sort_fecha_asc")
                            }
                            "sort_fecha_desc" -> {
                                currentActiveFilters.remove("sort_fecha_asc")
                                currentActiveFilters.add("sort_fecha_desc")
                            }
                            "sort_precio_asc" -> {
                                currentActiveFilters.remove("sort_precio_desc")
                                currentActiveFilters.add("sort_precio_asc")
                            }
                            "sort_precio_desc" -> {
                                currentActiveFilters.remove("sort_precio_asc")
                                currentActiveFilters.add("sort_precio_desc")
                            }
                            "sort_rank_asc" -> {
                                currentActiveFilters.remove("sort_rank_desc")
                                currentActiveFilters.add("sort_rank_asc")
                            }
                            "sort_rank_desc" -> {
                                currentActiveFilters.remove("sort_rank_asc")
                                currentActiveFilters.add("sort_rank_desc")
                            }
                            // Clear action for sort
                            "" -> { // When no sort is active, remove both asc and desc
                                currentActiveFilters.remove("sort_nombre_asc")
                                currentActiveFilters.remove("sort_nombre_desc")
                                currentActiveFilters.remove("sort_fecha_asc")
                                currentActiveFilters.remove("sort_fecha_desc")
                                currentActiveFilters.remove("sort_precio_asc")
                                currentActiveFilters.remove("sort_precio_desc")
                                currentActiveFilters.remove("sort_rank_asc")
                                currentActiveFilters.remove("sort_rank_desc")
                            }
                            "tool_copy" -> { /* Handle copy */ }
                            "tool_edit" -> { /* Handle edit */ }
                            "tool_share" -> { /* Handle share */ }
                            "tool_delete" -> { /* Handle delete */ }
                            else -> {
                                if (currentActiveFilters.contains(actionString)) currentActiveFilters.remove(actionString)
                                else currentActiveFilters.add(actionString)
                            }
                        }
                        activeFilters = currentActiveFilters.toSet()
                    },
                    onResetAll = { activeFilters = emptySet(); expanded = false },
                    secondaryActions = {
                        SmallActionFab(icon = Icons.Default.Favorite, label = "Favs", iconColor = Color(0xFFE91E63), onClick = {})
                    },
                    screenContext = ScreenContext.DEFAULT
                )
            }
        }
    }
}


/**
package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

// ==========================================================================================
// --- CONFIGURACIÓN VISUAL: GEMINI CYBERPUNK ---
// ==========================================================================================

val GeminiColors = listOf(
    Color(0xFF2197F5), // Azul
    Color(0xFF9B51E0), // Púrpura
    Color(0xFFE91E63)  // Rosa
)

@Composable
fun geminiGradientBrush(isAnimated: Boolean = true): Brush {
    val offset = if (isAnimated) {
        val infiniteTransition = rememberInfiniteTransition(label = "geminiAnim")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1500f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        ).value
    } else {
        0f
    }

    return Brush.linearGradient(
        colors = GeminiColors,
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Mirror
    )
}

@Composable
fun geminiGradientEffect(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "geminiAnim")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF4285F4)
        ),
        start = Offset(offsetAnim - 500f, offsetAnim - 500f),
        end = Offset(offsetAnim, offsetAnim)
    )
}

// ==========================================================================================
// --- MODELOS DE DATOS ---
// ==========================================================================================

data class ControlItem(
    val label: String,
    val icon: ImageVector?,
    val emoji: String,
    val color: Color,
    val id: String = label.lowercase()
)

// Enum para el tipo de pantalla/contexto
enum class ScreenContext {
    DEFAULT,
    CREATE_LICITACION
}

// ==========================================================================================
// --- BOTONES COMPACTOS (ACCIONES LATERALES DEL FAB) ---
// ==========================================================================================

@Composable
fun SmallActionFab(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 64.dp, height = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) colors.primaryContainer else colors.surface,
        shadowElevation = 8.dp,
        border = if (isActive) BorderStroke(1.5.dp, rainbowBrush) else BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) colors.primary else iconColor,
                modifier = Modifier.align(Alignment.TopCenter).size(24.dp)
            )
            Text(
                text = label,
                color = if (isActive) colors.primary else colors.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ==========================================================================================
// --- GEMINI SPLIT FAB CON PANEL TÁCTICO ANIMADO V2 (UX PRO) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSplitFAB(
    isExpanded: Boolean,
    isSearchActive: Boolean,
    isSecondaryPanelVisible: Boolean = false,

    // --- ESTADOS DE CONTEXTO ---
    isMultiSelectionActive: Boolean = false,
    onToggleExpand: () -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onCloseSecondaryPanel: () -> Unit = {},

    // --- Control de visibilidad de secciones según el contexto de la pantalla ---
    screenContext: ScreenContext = ScreenContext.DEFAULT,

    // --- CALLBACKS DE ACCIONES ---
    onCompareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},

    // --- DATOS DEL PANEL ---
    activeFilters: Set<String> = emptySet(),
    dynamicCategories: List<ControlItem> = emptyList(),
    onAction: (String) -> Unit = {},
    onResetAll: () -> Unit = {},

    // --- BOTONES LATERALES POR DEFECTO ---
    secondaryActions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    val density = LocalDensity.current

    val fabIconRotation by animateFloatAsState(
        targetValue = if (isExpanded || isMultiSelectionActive) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabRotation"
    )

    // --- ESTADOS PARA EL RANGO DE FECHAS PERSONALIZADO ---
    var showCustomDatePopup by remember { mutableStateOf(false) }
    var selectingDateType by remember { mutableStateOf<String?>(null) } // "start" o "end"
    var tempStartDate by remember { mutableStateOf<Long?>(null) }
    var tempEndDate by remember { mutableStateOf<Long?>(null) }
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    // Control de visibilidad de secciones basado en screenContext
    val showCategories = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showFilters = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showSort = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showTools = remember(screenContext) { true } // Herramientas siempre visibles, o se puede customizar

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {

        // --- POPUP CUSTOM PARA RANGO DE FECHAS ---
        if (showCustomDatePopup) {
            Dialog(
                onDismissRequest = { showCustomDatePopup = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF161C24),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shadowElevation = 24.dp
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color(0xFF2197F5))
                            Spacer(Modifier.width(12.dp))
                            Text("Filtrar por Rango", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }

                        Spacer(Modifier.height(24.dp))

                        // Botón Fecha Inicio
                        Text("FECHA DE INICIO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = { selectingDateType = "start" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (tempStartDate != null) Color(0xFF2197F5) else Color.White.copy(0.1f))
                        ) {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = tempStartDate?.let { dateFormatter.format(Date(it)) } ?: "Toca para seleccionar",
                                    color = if (tempStartDate != null) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botón Fecha Fin
                        Text("FECHA DE FIN", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = { selectingDateType = "end" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (tempEndDate != null) Color(0xFF9B51E0) else Color.White.copy(0.1f))
                        ) {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = tempEndDate?.let { dateFormatter.format(Date(it)) } ?: "Toca para seleccionar",
                                    color = if (tempEndDate != null) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(
                                onClick = { showCustomDatePopup = false },
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (tempStartDate != null && tempEndDate != null) {
                                        onAction("date_range_${tempStartDate}_${tempEndDate}")
                                        showCustomDatePopup = false
                                    }
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                enabled = tempStartDate != null && tempEndDate != null
                            ) {
                                Text("APLICAR", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // --- SELECTOR DE FECHA NATIVO (Se abre desde el PopUp) ---
        if (selectingDateType != null) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = if (selectingDateType == "start") tempStartDate else tempEndDate
            )
            DatePickerDialog(
                onDismissRequest = { selectingDateType = null },
                confirmButton = {
                    TextButton(onClick = {
                        if (selectingDateType == "start") tempStartDate = datePickerState.selectedDateMillis
                        else tempEndDate = datePickerState.selectedDateMillis
                        selectingDateType = null
                    }) {
                        Text("Confirmar", color = Color(0xFF2197F5), fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectingDateType = null }) { Text("Atrás", color = Color.Gray) }
                },
                colors = DatePickerDefaults.colors(containerColor = Color(0xFF161C24))
            ) {
                DatePicker(
                    state = datePickerState,
                    title = { Text(if (selectingDateType == "start") "Fecha de Inicio" else "Fecha de Fin", modifier = Modifier.padding(16.dp)) },
                    colors = DatePickerDefaults.colors(
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        selectedDayContainerColor = Color(0xFF2197F5),
                        todayContentColor = Color(0xFF2197F5),
                        todayDateBorderColor = Color(0xFF2197F5)
                    )
                )
            }
        }

        // --- SCRIM OSCURO REQUERIDO (Fondo translúcido al abrir el panel) ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleExpand // Cierra al tocar fuera
                    )
            )
        }

        // --- CENTRO DE CONTROL TÁCTICO V2 ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(250)) +
                    scaleIn(
                        initialScale = 0.8f,
                        transformOrigin = TransformOrigin(1f, 1f),
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ) +
                    slideInVertically(
                        initialOffsetY = { 100 },
                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut(animationSpec = tween(200)) +
                    scaleOut(
                        targetScale = 0.8f,
                        transformOrigin = TransformOrigin(1f, 1f),
                        animationSpec = tween(200)
                    ) +
                    slideOutVertically(targetOffsetY = { 100 }, animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            TacticalControlPanel(
                activeFilters = activeFilters,
                dynamicCategories = dynamicCategories,
                onAction = onAction,
                onApply = {
                    onAction("apply_filters")
                    onToggleExpand()
                },
                isMultiSelectionActive = isMultiSelectionActive,
                onCompareClick = onCompareClick,
                onDeleteClick = onDeleteClick,
                onShareClick = onShareClick,
                showCategories = showCategories,
                showFilters = showFilters,
                showSort = showSort,
                showTools = showTools,
                onOpenCalendar = { showCustomDatePopup = true } // <-- AQUÍ SE CONECTA EL POPUP
            )
        }

        // --- BOTONES FAB PRINCIPALES Y SECUNDARIOS ---
        Row(
            modifier = Modifier.padding(12.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = !isSearchActive && !isExpanded && !isSecondaryPanelVisible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                    AnimatedVisibility(visible = activeFilters.isNotEmpty() && !isMultiSelectionActive && showFilters) { // Only show clear filters if filters are active and visible
                        Surface(
                            onClick = {
                                onResetAll()
                                if (isExpanded) onToggleExpand()
                            },
                            modifier = Modifier.size(height = 56.dp, width = 56.dp),
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar Filtros", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    AnimatedVisibility(visible = isMultiSelectionActive) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                            SmallActionFab(icon = Icons.AutoMirrored.Filled.CompareArrows, label = "Comp.", iconColor = Color(0xFF2197F5), onClick = onCompareClick)
                            SmallActionFab(icon = Icons.Default.Delete, label = "Elim.", iconColor = Color(0xFFE91E63), onClick = onDeleteClick)
                        }
                    }

                    if (!isMultiSelectionActive) {
                        secondaryActions()
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = !isSearchActive && !isMultiSelectionActive && showFilters, // Only show search if filters are enabled
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    // BOTÓN BÚSQUEDA ASIMÉTRICO MODERNO
                    Surface(
                        onClick = onActivateSearch,
                        modifier = Modifier.height(56.dp).wrapContentWidth().defaultMinSize(minWidth = 56.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                        color = colors.surface,
                        border = BorderStroke(2.dp, rainbowBrush),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 20.dp, end = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(0.8f), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Buscar", fontSize = 14.sp, fontWeight = FontWeight.Black, color = colors.onSurface)
                        }
                    }
                }

                // CONTENEDOR BOX PARA EL FAB CIRCULAR + BADGE SUPERPUESTO REAL
                Box(contentAlignment = Alignment.TopEnd) {
                    Surface(
                        onClick = {
                            when {
                                isSecondaryPanelVisible -> onCloseSecondaryPanel()
                                isSearchActive -> onCloseSearch()
                                isMultiSelectionActive -> onAction("toggle_multiselect")
                                else -> onToggleExpand()
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(2.dp, rainbowBrush),
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isExpanded || isSearchActive || isSecondaryPanelVisible || isMultiSelectionActive) Icons.Default.Close else Icons.Default.Settings,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(26.dp).rotate(fabIconRotation)
                            )
                        }
                    }

                    // BADGE SUPERPUESTO (Fuera del Surface principal)
                    if (!isExpanded && !isSearchActive && !isMultiSelectionActive && activeFilters.isNotEmpty() && showFilters) { // Only show badge if filters are active and visible
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(22.dp)
                                .background(Color(0xFFE91E63), CircleShape)
                                .border(2.dp, Color(0xFF05070A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeFilters.size.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Panel Táctico Horizontal Animado V2 con Soporte Multiselección y Diseño Ultra-Compacto
 */
@Composable
fun TacticalControlPanel(
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    onAction: (String) -> Unit,
    onApply: () -> Unit,
    isMultiSelectionActive: Boolean = false,
    showCategories: Boolean = true,
    showFilters: Boolean = true,
    showSort: Boolean = true,
    showTools: Boolean = true,
    onCompareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onOpenCalendar: () -> Unit = {}
) {
    var categoriesExpanded by rememberSaveable { mutableStateOf(true) }
    var filtersExpanded by rememberSaveable { mutableStateOf(true) }
    var orderExpanded by rememberSaveable { mutableStateOf(true) }
    var toolsExpanded by rememberSaveable { mutableStateOf(false) } // 🔥 Por defecto colapsado

    val darkGradientBg = Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF05070A)))

    Box(modifier = Modifier.fillMaxWidth(0.96f).wrapContentHeight()) {

        Surface(
            onClick = onApply,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(40.dp)
                .zIndex(10f),
            shape = CircleShape,
            color = Color(0xFF10B981),
            border = BorderStroke(1.5.dp, Color(0xFF059669)),
            shadowElevation = 15.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = "Aplicar", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shadowElevation = 25.dp
        ) {
            Box(modifier = Modifier.background(darkGradientBg)) {
                Column(modifier = Modifier.padding(vertical = 12.dp).verticalScroll(rememberScrollState())) {

                    // --- SECCIÓN 1: CATEGORÍAS ---
                    if (showCategories && dynamicCategories.isNotEmpty()) {
                        PanelSection(
                            title = "Categorías",
                            icon = Icons.Default.Category,
                            isExpanded = categoriesExpanded,
                            onToggleExpand = { categoriesExpanded = !categoriesExpanded }
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(dynamicCategories) { item ->
                                    CategoryTag(
                                        item = item,
                                        isSelected = activeFilters.contains(item.id),
                                        onClick = { onAction(item.id) }
                                    )
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 2: FILTROS TÉCNICOS ---
                    if (showFilters) {
                        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                            PanelSection(
                                title = "Refinar búsqueda",
                                icon = Icons.Default.FilterList,
                                isExpanded = filtersExpanded,
                                onToggleExpand = { filtersExpanded = !filtersExpanded }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Fila 1 (6 espacios máximos)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)) {
                                        val row1 = listOf(
                                            ControlItem("Local", Icons.Default.Storefront, "🏪", Color(0xFF2197F5), "filter_local"),
                                            // MODIFICACIÓN: "Casa" a "Envios"
                                            ControlItem("Envios", Icons.Default.LocalShipping, "🚚", Color(0xFF9B51E0), "filter_envios"),
                                            ControlItem("24hs", Icons.Default.AccessTimeFilled, "⏳", Color(0xFFFF9800), "filter_24hs"),
                                            ControlItem("Turnos", Icons.Default.EventAvailable, "📅", Color(0xFF00FFC2), "filter_turnos"),
                                            ControlItem("Fast", Icons.Default.Bolt, "⚡", Color(0xFFFFEB3B), "filter_fast"),
                                            ControlItem("Verif.", Icons.Default.Verified, "✅", Color(0xFF9B51E0), "filter_verif")
                                        )
                                        row1.forEach { item ->
                                            CompactItemButton(item = item, isSelected = activeFilters.contains(item.id), onClick = { onAction(item.id) })
                                        }
                                    }
                                    // Fila 2
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                        val row2 = listOf(
                                            // MODIFICACIÓN: Emoji para Favs
                                            ControlItem("Favs", Icons.Default.Favorite, "❤️", Color(0xFFE91E63), "filter_favs"),
                                            ControlItem("Cerca", Icons.Default.LocationOn, "📍", Color(0xFF4CAF50), "filter_cerca")
                                        )
                                        row2.forEach { item ->
                                            CompactItemButton(item = item, isSelected = activeFilters.contains(item.id), onClick = { onAction(item.id) })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 3: ORDEN TÁCTICO (Inteligente Toggle) ---
                    if (showSort) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PanelSection(
                                title = "Orden táctico",
                                icon = Icons.AutoMirrored.Filled.Sort,
                                isExpanded = orderExpanded,
                                onToggleExpand = { orderExpanded = !orderExpanded }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {

                                    // NOMBRE (A-Z / Z-A) con Icono + Flecha
                                    val isNombreAsc = activeFilters.contains("sort_nombre_asc")
                                    val isNombreDesc = activeFilters.contains("sort_nombre_desc")
                                    val nombreSortArrow = when {
                                        isNombreAsc -> "🔼"
                                        isNombreDesc -> "🔽"
                                        else -> null
                                    }
                                    val nombreItem = ControlItem(
                                        label = "Nombre",
                                        icon = Icons.Default.SortByAlpha,
                                        emoji = "A", // Main emoji when selected
                                        color = Color(0xFF2197F5),
                                        id = "sort_nombre"
                                    )
                                    CompactItemButton(
                                        item = nombreItem,
                                        isSelected = isNombreAsc || isNombreDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isNombreAsc -> "sort_nombre_desc" // Was Asc, now Desc
                                                isNombreDesc -> "" // Was Desc, now Off
                                                else -> "sort_nombre_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = nombreSortArrow,
                                        overlayAlignment = if (isNombreAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    // FECHA (Soporta Click Largo para abrir Rango de Fechas)
                                    val isFechaAsc = activeFilters.contains("sort_fecha_asc")
                                    val isFechaDesc = activeFilters.contains("sort_fecha_desc")
                                    val hasDateRange = activeFilters.any { it.startsWith("date_range_") }
                                    val fechaSortArrow = when {
                                        isFechaAsc -> "🔼"
                                        isFechaDesc -> "🔽"
                                        else -> null
                                    }
                                    val fechaItem = ControlItem(
                                        label = "Fecha",
                                        icon = Icons.Default.CalendarToday,
                                        emoji = "D", // Main emoji when selected
                                        color = Color(0xFFFACC15),
                                        id = "sort_fecha"
                                    )
                                    CompactItemButton(
                                        item = fechaItem,
                                        isSelected = isFechaAsc || isFechaDesc || hasDateRange,
                                        onClick = {
                                            val newFilterId = when {
                                                isFechaAsc -> "sort_fecha_desc" // Was Asc, now Desc
                                                isFechaDesc -> "" // Was Desc, now Off
                                                else -> "sort_fecha_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        onLongClick = onOpenCalendar,
                                        overlayEmoji = fechaSortArrow,
                                        overlayAlignment = if (isFechaAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    // PRECIO
                                    val isPrecioAsc = activeFilters.contains("sort_precio_asc")
                                    val isPrecioDesc = activeFilters.contains("sort_precio_desc")
                                    val precioSortArrow = when {
                                        isPrecioAsc -> "🔼"
                                        isPrecioDesc -> "🔽"
                                        else -> null
                                    }
                                    val precioItem = ControlItem(
                                        label = "Precio",
                                        icon = Icons.Default.AttachMoney,
                                        emoji = "$", // Main emoji when selected
                                        color = Color(0xFF10B981),
                                        id = "sort_precio"
                                    )
                                    CompactItemButton(
                                        item = precioItem,
                                        isSelected = isPrecioAsc || isPrecioDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isPrecioAsc -> "sort_precio_desc" // Was Asc, now Desc
                                                isPrecioDesc -> "" // Was Desc, now Off
                                                else -> "sort_precio_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = precioSortArrow,
                                        overlayAlignment = if (isPrecioAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    // RANKING
                                    val isRankAsc = activeFilters.contains("sort_rank_asc")
                                    val isRankDesc = activeFilters.contains("sort_rank_desc")
                                    val rankSortArrow = when {
                                        isRankAsc -> "🔼"
                                        isRankDesc -> "🔽"
                                        else -> null
                                    }
                                    val rankItem = ControlItem(
                                        label = "Rank",
                                        icon = Icons.Default.Star,
                                        emoji = "⭐", // Main emoji when selected
                                        color = Color(0xFF9B51E0),
                                        id = "sort_rank"
                                    )
                                    CompactItemButton(
                                        item = rankItem,
                                        isSelected = isRankAsc || isRankDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isRankAsc -> "sort_rank_desc" // Was Asc, now Desc
                                                isRankDesc -> "" // Was Desc, now Off
                                                else -> "sort_rank_asc" // Was Off, now Asc
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = rankSortArrow,
                                        overlayAlignment = if (isRankAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )
                                }
                            }
                        }
                    }

                    // --- SECCIÓN 4: GESTIÓN Y HERRAMIENTAS ---
                    if (showTools) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PanelSection(
                                title = "Herramientas",
                                icon = Icons.Default.Build,
                                isExpanded = toolsExpanded,
                                onToggleExpand = { toolsExpanded = !toolsExpanded }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                    val multiItem = ControlItem("Multi", Icons.Default.Layers, "📑", Color(0xFFE91E63), "toggle_multiselect")
                                    val simChatItem = ControlItem("Sim Chat", Icons.Default.Forum, "💬", Color(0xFF22D3EE), "sim_chat")
                                    CompactItemButton(item = simChatItem, isSelected = false, onClick = { onAction(simChatItem.id) })

                                    val simLicItem = ControlItem("Sim Lic", Icons.Default.Gavel, "⚖️", Color(0xFFFACC15), "sim_lic")
                                    CompactItemButton(item = simLicItem, isSelected = false, onClick = { onAction(simLicItem.id) })

                                    if (isMultiSelectionActive) {
                                        CompactItemButton(item = multiItem, isSelected = true, onClick = { onAction("toggle_multiselect") })
                                        val compareItem = ControlItem("Comparar", Icons.AutoMirrored.Filled.CompareArrows, "⚖️", Color(0xFF2197F5), "compare")
                                        CompactItemButton(item = compareItem, isSelected = false, onClick = { onCompareClick() })
                                        val deleteItem = ControlItem("Eliminar", Icons.Default.Delete, "🗑️", Color(0xFFEF4444), "delete")
                                        CompactItemButton(item = deleteItem, isSelected = false, onClick = { onDeleteClick() })
                                        val shareItem = ControlItem("Share", Icons.Default.Share, "📤", Color(0xFFFACC15), "share")
                                        CompactItemButton(item = shareItem, isSelected = false, onClick = { onShareClick() })

                                        val refreshItem = ControlItem("Refresh", Icons.Default.Refresh, "🔄", Color(0xFF10B981), "refresh")
                                        CompactItemButton(item = refreshItem, isSelected = false, onClick = { onAction(refreshItem.id) })

                                    } else {
                                        CompactItemButton(item = multiItem, isSelected = activeFilters.contains(multiItem.id), onClick = { onAction("toggle_multiselect") })

                                        // MODIFICACIÓN: Nuevas herramientas para el modo normal
                                        val copyItem = ControlItem("Copiar", Icons.Default.ContentCopy, "📋", Color(0xFFFACC15), "tool_copy")
                                        CompactItemButton(item = copyItem, isSelected = activeFilters.contains(copyItem.id), onClick = { onAction(copyItem.id) })

                                        val editItem = ControlItem("Editar", Icons.Default.Edit, "✏️", Color(0xFF2197F5), "tool_edit")
                                        CompactItemButton(item = editItem, isSelected = activeFilters.contains(editItem.id), onClick = { onAction(editItem.id) })

                                        val shareItem = ControlItem("Share", Icons.Default.Share, "📤", Color(0xFFFACC15), "tool_share") // Changed ID
                                        CompactItemButton(item = shareItem, isSelected = activeFilters.contains(shareItem.id), onClick = { onShareClick() })

                                        val deleteItem = ControlItem("Eliminar", Icons.Default.Delete, "🗑️", Color(0xFFEF4444), "tool_delete") // Changed ID
                                        CompactItemButton(item = deleteItem, isSelected = activeFilters.contains(deleteItem.id), onClick = { onDeleteClick() })
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
 * Divisor Blanco de alto contraste
 */
@Composable
fun DividerWhite(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.3f), // Más visible
        thickness = 1.dp
    )
}

/**
 * Encabezado colapsable para las secciones del panel táctico. Textos e iconos en BLANCO.
 * Se cambió Spring por Tween para lograr animaciones más fluidas sin lag.
 */
@Composable
fun PanelSection(title: String, icon: ImageVector, isExpanded: Boolean, onToggleExpand: () -> Unit, content: @Composable () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)

            Spacer(Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, animationSpec = tween(200))
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Ocultar" else "Mostrar",
                tint = Color.White,
                modifier = Modifier.size(22.dp).rotate(arrowRotation)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) + fadeIn(tween(250)),
            exit = shrinkVertically(animationSpec = tween(200, easing = FastOutLinearInEasing)) + fadeOut(tween(200))
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal =1.dp, vertical = 1.dp)) {
                content()
            }
        }
    }
}

/**
 * Etiqueta Tipo "Pill" para las categorías. Sustituye al botón cuadrado.
 */
@Composable
fun CategoryTag(item: ControlItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if(isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
    val borderColor = if(isSelected) item.color else Color.White.copy(alpha = 0.15f)
    val textColor = if(isSelected) Color.White else Color.LightGray

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(item.emoji, fontSize = 16.sp) // Emoji real de Room más grande
            Spacer(Modifier.width(8.dp))
            Text(item.label.uppercase(), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

/**
 * Componente más compacto para los botones de las grillas del panel.
 * TAMAÑO FIJO: Asegura que entren 6 botones exactos por fila si se alinean a la derecha.
 * Añadidos overlayEmoji y overlayAlignment para flechas de ordenamiento.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactItemButton(
    item: ControlItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    overlayEmoji: String? = null, // Nuevo parámetro para el emoji superpuesto (ej: flecha de orden)
    overlayAlignment: Alignment = Alignment.BottomEnd // Alineación del emoji superpuesto
) {
    Column(
        modifier = modifier
            .width(52.dp) // 🔥 Ancho Fijo para empaquetado de 6
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp) // 🔥 Caja un poco más grande
                .background(
                    if (isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(10.dp)
                )
                .border(
                    if (isSelected) 1.5.dp else 0.8.dp,
                    if (isSelected) item.color else Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                // Muestra el emoji principal con efecto de glow
                Text(
                    text = item.emoji,
                    fontSize = 24.sp, // Tamaño apropiado para el emoji principal
                    style = TextStyle(
                        shadow = Shadow(
                            color = item.color,
                            offset = Offset(0f, 0f),
                            blurRadius = 25f // Crea un halo de luz alrededor del emoji
                        )
                    )
                )
            } else {
                // Estado Inactivo (Claro y visible)
                item.icon?.let {
                    Icon(it, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
                } ?: run {
                    Text(item.emoji, fontSize = 20.sp, modifier = Modifier.alpha(0.6f))
                }
            }

            // MODIFICACIÓN: Emoji superpuesto para indicadores (ej: flechas de orden)
            // Se dibuja siempre al final para asegurar que esté en la parte superior del Box
            overlayEmoji?.let { emoji ->
                Text(
                    text = emoji,
                    fontSize = 11.sp, // Más pequeño para el overlay
                    color = Color.White, // Siempre blanco para mayor visibilidad
                    modifier = Modifier
                        .align(overlayAlignment)
                        .offset(
                            x = if (overlayAlignment == Alignment.TopEnd) 6.dp else 6.dp, // Ajuste de offset
                            y = if (overlayAlignment == Alignment.TopEnd) (-6).dp else 6.dp // Ajuste de offset
                        )
                        .graphicsLayer { // Pequeña sombra para que destaque
                            shadowElevation = 10f
                            ambientShadowColor = Color.Black.copy(alpha = 0.6f)
                            spotShadowColor = Color.Black.copy(alpha = 0.6f)
                        }
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.label,
            fontSize = 9.sp, // 🔥 Texto más grande y legible
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else Color.LightGray,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =================================================================================
// --- COMPONENTES DE BÚSQUEDA Y NAVEGACIÓN ---
// =================================================================================

@Composable
fun GeminiTopSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholderText: String = "Buscar...",
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 8.dp, bottomEnd = 8.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, rainbowBrush)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(0.8f), modifier = Modifier.padding(start = 16.dp).size(18.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focusRequester),
                textStyle = TextStyle(color = colors.onSurface, fontSize = 15.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) { Text(placeholderText, color = colors.onSurfaceVariant, fontSize = 14.sp) }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
fun GeminiFABWithScrim(
    bottomPadding: PaddingValues,
    showScrim: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showScrim,
            enter = fadeIn(animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 1f)))))
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(bottomPadding).navigationBarsPadding().padding(bottom = 8.dp, end = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            content()
        }
    }
}

// =================================================================================
// --- COMPONENTES UI ADICIONALES Y TARJETAS ---
// =================================================================================

@Composable
fun ServiceTag(text: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = if (color.luminance() > 0.4f) Color.Black else Color.White, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

@Composable
fun RowItemDetail(icon: ImageVector, text: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isActive) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = if (isActive) Color.White else Color.Gray.copy(alpha = 0.5f), textDecoration = if (!isActive) TextDecoration.LineThrough else null)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PrestadorCard(
    provider: Provider,
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null,
    onDeleteRequest: (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null,
    onToggleFavorite: ((String, Boolean) -> Unit)? = null,
    viewMode: String = "Detallada",
    showAvatars: Boolean = true,
    showPreviews: Boolean = true,
    showBadges: Boolean = true,
    allCategories: List<CategoryEntity> = emptyList()
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    val staticBrush = geminiGradientBrush(isAnimated = false)
    val animateBrush = geminiGradientBrush(isAnimated = true)

    val activeColor = Color(0xFF22D3EE) // Cyan para activos
    val inactiveColor = Color.White.copy(alpha = 0.15f)
    val cyberBackground = Color(0xFF0A0E14)
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1F26),
            Color(0xFF0A0E14)
        ))

    // 🔥 LOGICA ACTUALIZADA DE BOOLEANOS
    // Se prioriza la información de la empresa si existe, si no, se usan los datos personales del prestador.
    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""
    val servicesToShow = mainCompany?.categories ?: provider.categories

    val works24h = mainCompany?.works24h ?: provider.works24h
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: provider.doesHomeVisits
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: provider.hasPhysicalLocation
    val acceptsAppointments = mainCompany?.acceptsAppointments ?: provider.acceptsAppointments

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(staticBrush, RoundedCornerShape(26.dp))
            .padding(1.5.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.5.dp))
                .combinedClickable(
                    onClick = { showDetailSheet = true },
                    onLongClick = { showContextMenu = true }
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.5.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .background(cardGradient)
                    .matchParentSize().background(Color.White.copy(alpha = 0.05f)))

                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showAvatars) {
                        Box(contentAlignment = Alignment.TopStart) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp) // Un poco más grande para mejor tap target
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .clickable { onClick() }, // 🔥 CLICK AQUI LLEVA AL PERFIL
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = provider.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    fallback = painterResource(id = R.drawable.iconapp),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (provider.isOnline && showBadges) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .background(Color(0xFF00E676), CircleShape)
                                        .border(2.dp, cyberBackground, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }) { // 🔥 CLICK EN NOMBRE LLEVA AL PERFIL
                            Text(
                                text = provider.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (provider.isVerified && showBadges) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(18.dp))
                            }
                        }

                        if (companyName.isNotEmpty()) {
                            Text(
                                text = companyName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF22D3EE).copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable { onClick() } // 🔥 CLICK EN EMPRESA LLEVA AL PERFIL
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (showBadges) {
                                // 🔥 BOOLEANOS ORDENADOS Y LIMPIOS
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (works24h) Icon(Icons.Default.AccessTimeFilled, "24Hs", modifier = Modifier.size(18.dp), tint = Color(0xFFFF9800))
                                    if (hasPhysicalLocation) Icon(Icons.Default.Storefront, "Local", modifier = Modifier.size(18.dp), tint = Color(0xFF2197F5))
                                    if (doesHomeVisits) Icon(Icons.Default.LocalShipping, "Visitas", modifier = Modifier.size(18.dp), tint = Color(0xFF9B51E0))
                                    if (acceptsAppointments) Icon(Icons.Default.EventAvailable, "Turnos", modifier = Modifier.size(18.dp), tint = Color(0xFF00FFC2))
                                }
                            }

                            IconButton(onClick = { showFavoriteDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (provider.isFavorite) Color.Red else Color.Gray)
                            }
                        }
                    }

                    if (actionContent != null) {
                        actionContent()
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onChat?.invoke() },
                            modifier = Modifier.size(44.dp).background(animateBrush, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) {
            DropdownMenuItem(text = { Text("Ver Perfil Completo") }, leadingIcon = { Icon(Icons.Default.Person, null) }, onClick = { showContextMenu = false; onClick() })
            HorizontalDivider()
            DropdownMenuItem(text = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, leadingIcon = { Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (provider.isFavorite) Color.Red else Color.Unspecified) }, onClick = { showContextMenu = false; showFavoriteDialog = true })
        }
    }

    if (showDetailSheet) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = cyberBackground, tonalElevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = provider.photoUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.width(16.dp)); Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(text = provider.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White); if (provider.isVerified) { Spacer(modifier = Modifier.width(6.dp)); Icon(Icons.Filled.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(24.dp)) } }
                    if (companyName.isNotEmpty()) { Text(text = companyName, style = MaterialTheme.typography.titleSmall, color = Color(0xFF22D3EE)) }
                    Text(text = provider.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                }
                Spacer(modifier = Modifier.height(24.dp)); Text("Servicios Ofrecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    servicesToShow.forEach { service ->
                        val catColorLong = allCategories.find { it.name.equals(service, ignoreCase = true) }?.color
                        val tagColor = if (catColorLong != null) Color(catColorLong) else Color.White.copy(alpha = 0.15f)
                        ServiceTag(text = service, color = tagColor)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = Color.White.copy(0.5f)); Spacer(modifier = Modifier.height(24.dp))
                RowItemDetail(icon = Icons.Default.Schedule, text = "Disponible 24hs", isActive = works24h)
                RowItemDetail(icon = Icons.Default.Home, text = "Visitas a Domicilio", isActive = doesHomeVisits)
                RowItemDetail(icon = Icons.Default.Storefront, text = "Local Físico", isActive = hasPhysicalLocation)
                RowItemDetail(icon = Icons.Default.Event, text = "Turnos / Citas", isActive = acceptsAppointments)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { showDetailSheet = false; onClick() }, modifier = Modifier.fillMaxWidth().height(36.dp).background(animateBrush, RoundedCornerShape(12.dp)), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), shape = RoundedCornerShape(28.dp)) {
                    Text("Ver Perfil Completo", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showFavoriteDialog) {
        AlertDialog(onDismissRequest = { showFavoriteDialog = false }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }, title = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, text = { Text(if (provider.isFavorite) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") }, confirmButton = { TextButton(onClick = { onToggleFavorite?.invoke(provider.id, !provider.isFavorite); showFavoriteDialog = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { showFavoriteDialog = false }) { Text("Cancelar") } })
    }
}

// ==========================================================================================
// --- CARRUSELES Y CARDS ---
// ==========================================================================================

@Composable
fun CompactCategoryCard(item: CategoryEntity, onClick: () -> Unit) {
    val baseColor = Color(item.color)
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = baseColor), modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)), elevation = CardDefaults.cardElevation(0.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache {
                val gradient = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent), start = Offset(0f, 0f), end = Offset(size.width, size.height))
                onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) }
            })
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent), startX = 0f, endX = 450f)))
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight().padding(start = 8.dp, end = 2.dp, top = if (item.isNew) 18.dp else 0.dp), contentAlignment = Alignment.CenterStart) {
                    Column {
                        AutoResizingText(text = item.name.uppercase(), color = Color.White, maxFontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.width(24.dp).height(1.5.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    }
                }
                Box(modifier = Modifier.width(1.5.dp).height(85.dp).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent))))
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) { Text(text = item.icon, fontSize = 90.sp, modifier = Modifier.offset(x = 10.dp).graphicsLayer(alpha = 1f)) }
            }
            if (item.isNew) { Surface(color = Color(0xFFFFD600), shape = RoundedCornerShape(bottomEnd = 12.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "NUEVO", color = Color.Black, fontSize = 7.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), letterSpacing = 0.5.sp) } }
        }
    }
}

@Composable
private fun AutoResizingText(text: String, color: Color, maxFontSize: TextUnit, minFontSize: TextUnit = 8.sp) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(text = text, color = if (readyToDraw) color else Color.Transparent, fontWeight = FontWeight.Black, fontSize = fontSize, lineHeight = fontSize * 1.1f, maxLines = 2, letterSpacing = 1.1.sp, overflow = TextOverflow.Clip, onTextLayout = { result -> if (result.hasVisualOverflow && fontSize > minFontSize) { fontSize = (fontSize.value * 0.9f).sp } else { readyToDraw = true } }, modifier = Modifier.fillMaxWidth())
}

enum class BannerType(val label: String) {
    GOOGLE_AD("SPONSORED"),
    PROMO("PROMOCIÓN"),
    NEW_CATEGORY("NUEVA CATEGORÍA"),
    NEW_PROVIDER("NUEVOS PRESTADORES"),
    PRODUCT_SALE("VENTA DE PRODUCTO"),
    SERVICE_SALE("SERVICIO DESTACADO")
}

data class AccordionBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val type: BannerType,
    val originalCategory: CategoryEntity? = null,
    val isNew: Boolean = false,
    val imageUrl: String? = null,
    val discount: Int? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumLensCarousel(
    items: List<AccordionBanner>,
    onSettingsClick: () -> Unit,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 5000L
) {
    if (items.isEmpty()) return

    var expandedMenu by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var tempFilters by remember { mutableStateOf<Set<String>>(emptySet()) }

    val filteredItems = remember(items, activeFilters) {
        if (activeFilters.isEmpty()) items
        else items.filter {
            if (it.type == BannerType.GOOGLE_AD) true
            else {
                val isNovedad = it.type == BannerType.NEW_CATEGORY || it.type == BannerType.NEW_PROVIDER
                val isPromo = it.type == BannerType.PROMO || it.discount != null
                val isProd = it.type == BannerType.PRODUCT_SALE
                val isServ = it.type == BannerType.SERVICE_SALE

                (activeFilters.contains("NOVEDADES") && isNovedad) ||
                        (activeFilters.contains("PROMOCIONES") && isPromo) ||
                        (activeFilters.contains("PRODUCTOS") && isProd) ||
                        (activeFilters.contains("SERVICIOS") && isServ)
            }
        }
    }

    val displayItems = filteredItems.ifEmpty { items }

    val infiniteCount = Int.MAX_VALUE
    val initialPage = infiniteCount / 2 - (infiniteCount / 2 % displayItems.size.coerceAtLeast(1))
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }

    LaunchedEffect(key1 = displayItems) {
        while (true) {
            delay(autoplayDelay)
            if (displayItems.size > 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DESTACADOS & NOVEDADES",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Box {
                if (activeFilters.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            activeFilters = emptySet()
                            tempFilters = emptySet()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar Filtros", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    }
                } else {
                    IconButton(
                        onClick = {
                            tempFilters = activeFilters
                            expandedMenu = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Filtros", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(Color(0xFF161C24)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FILTROS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        IconButton(
                            onClick = {
                                activeFilters = tempFilters
                                expandedMenu = false
                            },
                            modifier = Modifier.size(24.dp).background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Aplicar", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    val options = listOf(
                        "NOVEDADES" to "🚀",
                        "PROMOCIONES" to "🔥",
                        "PRODUCTOS" to "🛍️",
                        "SERVICIOS" to "🛠️"
                    )

                    options.forEach { (option, emoji) ->
                        val isSelected = tempFilters.contains(option)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color(0xFF2197F5) else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF2197F5), modifier = Modifier.size(16.dp))
                                }
                            },
                            onClick = {
                                tempFilters = if (isSelected) tempFilters - option else tempFilters + option
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(300.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(start = 10.dp, end = 64.dp),
            modifier = Modifier.fillMaxWidth().height(120.dp)
        ) { index ->
            val actualIndex = index % displayItems.size
            val item = displayItems[actualIndex]

            val pageOffset = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
            Box(modifier = Modifier.graphicsLayer {
                val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                scaleX = scale
                scaleY = scale
                alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            }) {
                if (item.type == BannerType.GOOGLE_AD) {
                    AdBannerItem(item = item)
                } else {
                    PremiumBannerItem(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun AdBannerItem(item: AccordionBanner) {
    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AdsClick, null, tint = Color.Gray); Text(item.title, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp)); Text(item.subtitle, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp)) } }
            Box(modifier = Modifier.align(Alignment.TopStart).background(Color(0xFFFFC107), RoundedCornerShape(bottomEnd = 12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("ANUNCIO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black) }
        }
    }
}

@Composable
fun PremiumBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxSize().clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = item.color), elevation = CardDefaults.cardElevation(8.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
            Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache { val gradient = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent), start = Offset(0f, 0f), end = Offset(size.width, size.height)); onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) } })
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.4f), Color.Transparent), startX = 0f, endX = 600f)))
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(0.85f).fillMaxHeight().padding(start = 10.dp, top = 20.dp, bottom = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Column { AutoResizingText(text = item.title.uppercase(), color = Color.White, maxFontSize = 20.sp); Text(text = item.subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp); Spacer(modifier = Modifier.height(10.dp)); Box(modifier = Modifier.width(40.dp).height(3.dp).background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))) }
                }
                Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.7f).background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent))))

                Box(modifier = Modifier.weight(0.35f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = item.icon,
                        fontSize = 100.sp,
                        modifier = Modifier.offset(x = 20.dp),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.6f),
                                offset = Offset(-10f, 15f),
                                blurRadius = 20f
                            )
                        )
                    )
                }
            }
            if (item.isNew) {
                Surface(color = Color(0xFFFFD600), shape = RoundedCornerShape(bottomEnd = 16.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "NUEVO", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) }
            } else if (item.discount != null) {
                Surface(color = Color(0xFFE91E63), shape = RoundedCornerShape(bottomEnd = 16.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "${item.discount}% OFF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) }
            }
        }
    }
}

@Composable
fun GeminiCyberWrapper(modifier: Modifier = Modifier, cornerRadius: Dp = 24.dp, borderThickness: Dp = 1.5.dp, isAnimated: Boolean = false, showGlow: Boolean = true, content: @Composable () -> Unit) {
    val geminiBrush = geminiGradientBrush(isAnimated = isAnimated)
    val cyberBackground = Color(0xFF0A0E14)
    Box(modifier = modifier) {
        if (showGlow) Box(modifier = Modifier.matchParentSize().padding(borderThickness).blur(15.dp).background(geminiBrush, RoundedCornerShape(cornerRadius)).graphicsLayer { alpha = 0.3f })
        Box(modifier = Modifier.fillMaxWidth().background(geminiBrush, RoundedCornerShape(cornerRadius)).padding(borderThickness)) {
            Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadius - borderThickness)), color = cyberBackground) {
                Box(modifier = Modifier.fillMaxWidth()) { Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.05f))); content() }
            }
        }
    }
}

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TacticalFABPreview() {
    var expanded by remember { mutableStateOf(true) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isMultiSelectionActive by remember { mutableStateOf(false) }
    var activeFilters by remember { mutableStateOf(setOf("filter_verif", "sort_precio_asc")) }

    val mockCategories = listOf(
        ControlItem("Informática", null, "💻", Color(0xFFB2EBF2), "cat_info"),
        ControlItem("Mecánica", null, "🔧", Color(0xFFFFDAC1), "cat_mec"),
        ControlItem("Limpieza", null, "🧹", Color(0xFFFAD2E1), "cat_limp")
    )

    MyApplicationTheme {
        Column {
            // Preview para ScreenContext.DEFAULT
            Text("Default Screen Context", color = Color.White)
            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color(0xFF0A0E14))) {
                GeminiSplitFAB(
                    isExpanded = expanded,
                    isSearchActive = isSearchActive,
                    isMultiSelectionActive = isMultiSelectionActive,
                    onToggleExpand = { expanded = !expanded },
                    onActivateSearch = { isSearchActive = true; expanded = false },
                    onCloseSearch = { isSearchActive = false },
                    onCompareClick = { },
                    onDeleteClick = { },
                    onShareClick = { },
                    activeFilters = activeFilters,
                    dynamicCategories = mockCategories,
                    onAction = { actionString ->
                        val currentActiveFilters = activeFilters.toMutableSet()
                        when (actionString) {
                            "toggle_multiselect" -> isMultiSelectionActive = !isMultiSelectionActive
                            "apply_filters" -> { /* Lógica de aplicar */ }
                            // Sort actions
                            "sort_nombre_asc" -> {
                                currentActiveFilters.remove("sort_nombre_desc")
                                currentActiveFilters.add("sort_nombre_asc")
                            }
                            "sort_nombre_desc" -> {
                                currentActiveFilters.remove("sort_nombre_asc")
                                currentActiveFilters.add("sort_nombre_desc")
                            }
                            "sort_fecha_asc" -> {
                                currentActiveFilters.remove("sort_fecha_desc")
                                currentActiveFilters.add("sort_fecha_asc")
                            }
                            "sort_fecha_desc" -> {
                                currentActiveFilters.remove("sort_fecha_asc")
                                currentActiveFilters.add("sort_fecha_desc")
                            }
                            "sort_precio_asc" -> {
                                currentActiveFilters.remove("sort_precio_desc")
                                currentActiveFilters.add("sort_precio_asc")
                            }
                            "sort_precio_desc" -> {
                                currentActiveFilters.remove("sort_precio_asc")
                                currentActiveFilters.add("sort_precio_desc")
                            }
                            "sort_rank_asc" -> {
                                currentActiveFilters.remove("sort_rank_desc")
                                currentActiveFilters.add("sort_rank_asc")
                            }
                            "sort_rank_desc" -> {
                                currentActiveFilters.remove("sort_rank_asc")
                                currentActiveFilters.add("sort_rank_desc")
                            }
                            // Clear action for sort
                            "" -> { // When no sort is active, remove both asc and desc
                                currentActiveFilters.remove("sort_nombre_asc")
                                currentActiveFilters.remove("sort_nombre_desc")
                                currentActiveFilters.remove("sort_fecha_asc")
                                currentActiveFilters.remove("sort_fecha_desc")
                                currentActiveFilters.remove("sort_precio_asc")
                                currentActiveFilters.remove("sort_precio_desc")
                                currentActiveFilters.remove("sort_rank_asc")
                                currentActiveFilters.remove("sort_rank_desc")
                            }
                            "tool_copy" -> { /* Handle copy */ }
                            "tool_edit" -> { /* Handle edit */ }
                            "tool_share" -> { /* Handle share */ }
                            "tool_delete" -> { /* Handle delete */ }
                            else -> {
                                if (currentActiveFilters.contains(actionString)) currentActiveFilters.remove(actionString)
                                else currentActiveFilters.add(actionString)
                            }
                        }
                        activeFilters = currentActiveFilters.toSet()
                    },
                    onResetAll = { activeFilters = emptySet(); expanded = false },
                    secondaryActions = {
                        SmallActionFab(icon = Icons.Default.Favorite, label = "Favs", iconColor = Color(0xFFE91E63), onClick = {})
                    },
                    screenContext = ScreenContext.DEFAULT
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preview para ScreenContext.CREATE_LICITACION
            Text("Create Licitacion Screen Context", color = Color.White)
            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color(0xFF0A0E14))) {
                GeminiSplitFAB(
                    isExpanded = expanded,
                    isSearchActive = isSearchActive,
                    isMultiSelectionActive = isMultiSelectionActive,
                    onToggleExpand = { expanded = !expanded },
                    onActivateSearch = { isSearchActive = true; expanded = false },
                    onCloseSearch = { isSearchActive = false },
                    onCompareClick = { },
                    onDeleteClick = { },
                    onShareClick = { },
                    activeFilters = activeFilters, // Filters might still exist but won't be shown
                    dynamicCategories = emptyList(), // Categories won't be shown
                    onAction = { id ->
                        when (id) {
                            "toggle_multiselect" -> isMultiSelectionActive = !isMultiSelectionActive
                            "tool_copy" -> { /* Handle copy */ }
                            "tool_edit" -> { /* Handle edit */ }
                            "tool_share" -> { /* Handle share */ }
                            "tool_delete" -> { /* Handle delete */ }
                            else -> { /* No filters/sort in this context */ }
                        }
                    },
                    onResetAll = { activeFilters = emptySet(); expanded = false },
                    secondaryActions = { /* No secondary actions needed for this context, or custom ones */ },
                    screenContext = ScreenContext.CREATE_LICITACION
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PremiumLensCarouselPreview() {
    MyApplicationTheme {
        val mockCategory = CategoryEntity(
            name = "Fontanería",
            icon = "🪠",
            color = 0xFF42A5F5L, // Use Long for color
            superCategory = "Hogar",
            superCategoryIcon = "📂", // Added missing parameter to match CategoryEntity constructor
            providerIds = emptyList(),
            imageUrl = null,
            isNew = true,
            isNewPrestador = false, // Explicitly set
            isAd = false // Explicitly set
        )

        val mockItems = listOf(
            AccordionBanner(
                id = "promo_1",
                title = "Oferta Especial",
                subtitle = "¡Descuentos en todos los servicios!",
                icon = "🔥",
                color = Color(0xFFE91E63),
                type = BannerType.PROMO,
                isNew = true,
                discount = 25
            ),
            AccordionBanner(
                id = "new_cat_1",
                title = mockCategory.name,
                subtitle = "¡Nueva categoría disponible!",
                icon = mockCategory.icon,
                color = Color(mockCategory.color),
                type = BannerType.NEW_CATEGORY,
                originalCategory = mockCategory,
                isNew = true
            ),
            AccordionBanner(
                id = "ad_google_1",
                title = "Google Ads",
                subtitle = "Anúnciate aquí",
                icon = "📢",
                color = Color(0xFF1967D2),
                type = BannerType.GOOGLE_AD,
                imageUrl = "https://example.com/ad_image.jpg" // Placeholder URL
            ),
            AccordionBanner(
                id = "product_sale_1",
                title = "Kit Herramientas",
                subtitle = "Marca X - 20% OFF",
                icon = "🛠️",
                color = Color(0xFF43A047),
                type = BannerType.PRODUCT_SALE,
                isNew = true,
                discount = 20
            ),
            AccordionBanner(
                id = "service_sale_1",
                title = "Servicio de Limpieza",
                subtitle = "Profesionales a tu disposición",
                icon = "🧼",
                color = Color(0xFF00B0FF),
                type = BannerType.SERVICE_SALE,
                isNew = false,
                discount = 15
            )
        )

        PremiumLensCarousel(
            items = mockItems,
            onSettingsClick = { /* Handle settings click */ },
            onItemClick = { banner -> /* Handle item click */ }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14, widthDp = 200)
@Composable
fun CompactCategoryCardPreview() {
    MyApplicationTheme {
        val mockCategory = CategoryEntity(
            name = "Fontanería",
            icon = "🪠",
            color = 0xFF42A5F5, // Azul
            superCategory = "Hogar",
            superCategoryIcon = "📂", // Added missing parameter to match CategoryEntity constructor
            providerIds = emptyList(),
            imageUrl = null,
            isNew = true,
            isNewPrestador = false,
            isAd = false
        )
        // Agregamos un Box con padding para que la tarjeta no ocupe todo el ancho
        Box(modifier = Modifier.padding(16.dp)) {
            CompactCategoryCard(item = mockCategory, onClick = {})
        }
    }
}
**/