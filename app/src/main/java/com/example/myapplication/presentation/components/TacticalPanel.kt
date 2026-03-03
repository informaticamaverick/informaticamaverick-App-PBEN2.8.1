/**

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================================================================
// --- MODELOS DE DATOS DEL PANEL ---
// ==========================================================================================

data class ControlItem(
    val label: String,
    val icon: ImageVector?,
    val emoji: String,
    val color: Color,
    val id: String = label.lowercase()
)

enum class ScreenContext {
    DEFAULT,
    CREATE_LICITACION
}

// ==========================================================================================
// --- CONTENEDOR CON SCRIM OSCURO ---
// ==========================================================================================

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 1f))))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottomPadding)
                .navigationBarsPadding()
                .padding(bottom = 8.dp, end = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            content()
        }
    }
}

// ==========================================================================================
// --- BOTONES COMPACTOS (ACCIONES LATERALES) ---
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
// --- GEMINI SPLIT FAB MAESTRO ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSplitFAB(
    isExpanded: Boolean,
    isSearchActive: Boolean,
    isSecondaryPanelVisible: Boolean = false,

    isMultiSelectionActive: Boolean = false,
    onToggleExpand: () -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onCloseSecondaryPanel: () -> Unit = {},

    screenContext: ScreenContext = ScreenContext.DEFAULT,

    onCompareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},

    activeFilters: Set<String> = emptySet(),
    dynamicCategories: List<ControlItem> = emptyList(),
    onAction: (String) -> Unit = {},
    onResetAll: () -> Unit = {},

    secondaryActions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect() // 🔥 Efecto Gemini mantenido

    val fabIconRotation by animateFloatAsState(
        targetValue = if (isExpanded || isMultiSelectionActive) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabRotation"
    )

    var showCustomDatePopup by remember { mutableStateOf(false) }
    var selectingDateType by remember { mutableStateOf<String?>(null) }
    var tempStartDate by remember { mutableStateOf<Long?>(null) }
    var tempEndDate by remember { mutableStateOf<Long?>(null) }
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    val showCategories = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showFilters = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showSort = remember(screenContext) { screenContext == ScreenContext.DEFAULT }
    val showTools = remember(screenContext) { true }

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
                        onClick = onToggleExpand
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
                onOpenCalendar = { showCustomDatePopup = true }
            )
        }

        // --- BOTONES FAB PRINCIPALES Y SECUNDARIOS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            // LADO IZQUIERDO: Acciones secundarias (SmallActionFabs)
            AnimatedVisibility(
                visible = !isSearchActive && !isExpanded && !isSecondaryPanelVisible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                    AnimatedVisibility(visible = activeFilters.isNotEmpty() && !isMultiSelectionActive && showFilters) {
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

            Spacer(modifier = Modifier.weight(1f))

            // LADO DERECHO: FAB CIRCULAR PRINCIPAL (ENGRANAJE)
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
                    border = BorderStroke(2.dp, rainbowBrush), // 🔥 Gemini Glow Preservado
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

                // BADGE SUPERPUESTO
                if (!isExpanded && !isSearchActive && !isMultiSelectionActive && activeFilters.isNotEmpty() && showFilters) {
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
    var toolsExpanded by rememberSaveable { mutableStateOf(false) }

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

                    // --- SECCIÓN 3: ORDEN TÁCTICO ---
                    if (showSort) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PanelSection(
                                title = "Orden táctico",
                                icon = Icons.AutoMirrored.Filled.Sort,
                                isExpanded = orderExpanded,
                                onToggleExpand = { orderExpanded = !orderExpanded }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {

                                    val isNombreAsc = activeFilters.contains("sort_nombre_asc")
                                    val isNombreDesc = activeFilters.contains("sort_nombre_desc")
                                    val nombreSortArrow = when {
                                        isNombreAsc -> "🔼"
                                        isNombreDesc -> "🔽"
                                        else -> null
                                    }
                                    val nombreItem = ControlItem("Nombre", Icons.Default.SortByAlpha, "A", Color(0xFF2197F5), "sort_nombre")
                                    CompactItemButton(
                                        item = nombreItem, isSelected = isNombreAsc || isNombreDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isNombreAsc -> "sort_nombre_desc"
                                                isNombreDesc -> ""
                                                else -> "sort_nombre_asc"
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = nombreSortArrow, overlayAlignment = if (isNombreAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    val isFechaAsc = activeFilters.contains("sort_fecha_asc")
                                    val isFechaDesc = activeFilters.contains("sort_fecha_desc")
                                    val hasDateRange = activeFilters.any { it.startsWith("date_range_") }
                                    val fechaSortArrow = when {
                                        isFechaAsc -> "🔼"
                                        isFechaDesc -> "🔽"
                                        else -> null
                                    }
                                    val fechaItem = ControlItem("Fecha", Icons.Default.CalendarToday, "D", Color(0xFFFACC15), "sort_fecha")
                                    CompactItemButton(
                                        item = fechaItem, isSelected = isFechaAsc || isFechaDesc || hasDateRange,
                                        onClick = {
                                            val newFilterId = when {
                                                isFechaAsc -> "sort_fecha_desc"
                                                isFechaDesc -> ""
                                                else -> "sort_fecha_asc"
                                            }
                                            onAction(newFilterId)
                                        },
                                        onLongClick = onOpenCalendar,
                                        overlayEmoji = fechaSortArrow, overlayAlignment = if (isFechaAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    val isPrecioAsc = activeFilters.contains("sort_precio_asc")
                                    val isPrecioDesc = activeFilters.contains("sort_precio_desc")
                                    val precioSortArrow = when {
                                        isPrecioAsc -> "🔼"
                                        isPrecioDesc -> "🔽"
                                        else -> null
                                    }
                                    val precioItem = ControlItem("Precio", Icons.Default.AttachMoney, "$", Color(0xFF10B981), "sort_precio")
                                    CompactItemButton(
                                        item = precioItem, isSelected = isPrecioAsc || isPrecioDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isPrecioAsc -> "sort_precio_desc"
                                                isPrecioDesc -> ""
                                                else -> "sort_precio_asc"
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = precioSortArrow, overlayAlignment = if (isPrecioAsc) Alignment.TopEnd else Alignment.BottomEnd
                                    )

                                    val isRankAsc = activeFilters.contains("sort_rank_asc")
                                    val isRankDesc = activeFilters.contains("sort_rank_desc")
                                    val rankSortArrow = when {
                                        isRankAsc -> "🔼"
                                        isRankDesc -> "🔽"
                                        else -> null
                                    }
                                    val rankItem = ControlItem("Rank", Icons.Default.Star, "⭐", Color(0xFF9B51E0), "sort_rank")
                                    CompactItemButton(
                                        item = rankItem, isSelected = isRankAsc || isRankDesc,
                                        onClick = {
                                            val newFilterId = when {
                                                isRankAsc -> "sort_rank_desc"
                                                isRankDesc -> ""
                                                else -> "sort_rank_asc"
                                            }
                                            onAction(newFilterId)
                                        },
                                        overlayEmoji = rankSortArrow, overlayAlignment = if (isRankAsc) Alignment.TopEnd else Alignment.BottomEnd
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

            val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, animationSpec = tween(200), label = "arrowRotate")
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
                Text(text = item.emoji, fontSize = 24.sp, style = TextStyle(shadow = Shadow(color = item.color, offset = Offset(0f, 0f), blurRadius = 25f)))
            } else {
                item.icon?.let { Icon(it, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp)) } ?: run { Text(item.emoji, fontSize = 20.sp, modifier = Modifier.alpha(0.6f)) }
            }

            overlayEmoji?.let { emoji ->
                Text(
                    text = emoji, fontSize = 11.sp, color = Color.White,
                    modifier = Modifier.align(overlayAlignment).offset(x = if (overlayAlignment == Alignment.TopEnd) 6.dp else 6.dp, y = if (overlayAlignment == Alignment.TopEnd) (-6).dp else 6.dp)
                        .graphicsLayer { shadowElevation = 10f; ambientShadowColor = Color.Black.copy(alpha = 0.6f); spotShadowColor = Color.Black.copy(alpha = 0.6f) }
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(text = item.label, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) Color.White else Color.LightGray, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TacticalControlPanelPreview() {
    val sampleCategories = listOf(
        ControlItem("Limpieza", Icons.Default.CleaningServices, "🧹", Color(0xFFFAD2E1), "cat_limpieza"),
        ControlItem("Fontanería", Icons.Default.Build, "🪠", Color(0xFFD4A5A5), "cat_fontaneria"),
        ControlItem("Electricidad", Icons.Default.Bolt, "⚡", Color(0xFFFFF59D), "cat_electricidad")
    )
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            TacticalControlPanel(
                activeFilters = setOf("cat_limpieza", "filter_local", "sort_nombre_asc"),
                dynamicCategories = sampleCategories,
                onAction = {},
                onApply = {}
            )
        }
    }
}
**/