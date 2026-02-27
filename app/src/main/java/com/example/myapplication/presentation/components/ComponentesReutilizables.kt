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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import kotlinx.coroutines.delay
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.util.lerp
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

// ==========================================================================================
// --- BOTONES COMPACTOS RESTAURADOS ---
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

@Composable
fun GeminiSplitFAB(
    isExpanded: Boolean,
    isSearchActive: Boolean,
    isSecondaryPanelVisible: Boolean = false,
    onToggleExpand: () -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onCloseSecondaryPanel: () -> Unit = {},
    // Estados del Panel Táctico
    activeFilters: Set<String> = emptySet(),
    dynamicCategories: List<ControlItem> = emptyList(), // Si está vacía, no renderiza categorías (ideal para Home)
    onAction: (String) -> Unit = {},
    onResetAll: () -> Unit = {},
    // Botones Horizontales
    secondaryActions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()

    // Si el panel está expandido, mostramos la X girada.
    val fabIconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabRotation"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {

        // --- POPUP: CENTRO DE CONTROL TÁCTICO V2 ---
        if (isExpanded) {
            Popup(
                alignment = Alignment.BottomCenter,
                offset = IntOffset(x = 0, y = -260),
                onDismissRequest = onToggleExpand, // Permite cerrar tocando afuera
                properties = PopupProperties(focusable = true)
            ) {
                TacticalControlPanel(
                    activeFilters = activeFilters,
                    dynamicCategories = dynamicCategories,
                    onAction = onAction,
                    onResetAll = {
                        onResetAll()
                        onToggleExpand()
                    }
                )
            }
        }

        // --- BOTONES FAB PRINCIPALES Y SECUNDARIOS ---
        Row(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- BOTONES LATERALES ---
            AnimatedVisibility(
                visible = !isSearchActive && !isExpanded && !isSecondaryPanelVisible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {

                    // 🔥 BOTÓN MÁGICO DE LIMPIEZA RÁPIDA (Aparece solo si hay filtros activos)
                    AnimatedVisibility(visible = activeFilters.isNotEmpty()) {
                        Surface(
                            onClick = {
                                onResetAll()
                                if(isExpanded) onToggleExpand()
                            },
                            modifier = Modifier.height(56.dp).wrapContentWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("LIMPIAR", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    secondaryActions()
                }
            }

            // --- FABS PRINCIPALES (Búsqueda Pill y Engranaje Circular) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // FAB BUSCAR (Con texto y más ancho - Pill Shape)
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Surface(
                        onClick = onActivateSearch,
                        modifier = Modifier.height(56.dp).wrapContentWidth().defaultMinSize(minWidth = 56.dp),
                        shape = CircleShape, // Pill
                        color = colors.surface,
                        border = BorderStroke(2.5.dp, rainbowBrush),
                        shadowElevation = 12.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = colors.onSurface, modifier = Modifier.size(26.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Buscar", fontSize = 15.sp, fontWeight = FontWeight.Black, color = colors.onSurface)
                        }
                    }
                }

                // FAB AJUSTES (Siempre Redondo con Badge Dinámico)
                Surface(
                    onClick = {
                        when {
                            isSecondaryPanelVisible -> onCloseSecondaryPanel()
                            isSearchActive -> onCloseSearch()
                            else -> onToggleExpand()
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape, // SIEMPRE CIRCULAR
                    color = colors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isExpanded || isSearchActive || isSecondaryPanelVisible) Icons.Default.Close else Icons.Default.Settings,
                            contentDescription = null,
                            tint = colors.onSurface,
                            modifier = Modifier.size(30.dp).rotate(fabIconRotation) // Icono animado
                        )

                        // 🔥 EL BADGE NOTIFICADOR DE FILTROS ACTIVOS
                        if (!isExpanded && !isSearchActive && activeFilters.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .size(20.dp)
                                    .background(Color(0xFFE91E63), CircleShape)
                                    .border(2.dp, colors.surface, CircleShape),
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
}

/**
 * Panel Táctico Horizontal Animado V2 con Scroll dinámico
 */
@Composable
fun TacticalControlPanel(
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    onAction: (String) -> Unit,
    onResetAll: () -> Unit
) {
    // ESTADO PARA LA ANIMACIÓN DE ENTRADA MODERNA
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateIn = true }

    AnimatedVisibility(
        visible = animateIn,
        enter = slideInVertically(
            initialOffsetY = { 200 }, // Sube desde más abajo
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
        ) + fadeIn(tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { 200 }) + fadeOut(tween(200))
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.96f).wrapContentHeight()) {

            // BOTÓN RESET MAESTRO (FLOTANTE FUERA DEL PANEL)
            Surface(
                onClick = onResetAll,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(48.dp)
                    .zIndex(10f),
                shape = CircleShape,
                color = Color(0xFF1F2937),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)),
                shadowElevation = 25.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            // PANEL PRINCIPAL OSCURO (V2)
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 550.dp), // Límite de alto para pantallas chicas
                color = Color(0xFF05070A).copy(alpha = 0.98f),
                shape = RoundedCornerShape(44.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 50.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 24.dp).verticalScroll(rememberScrollState())) {

                    // --- SECCIÓN 1: CATEGORÍAS (Scroll Horizontal Dinámico) ---
                    // Sólo se muestra si se pasan categorías al componente
                    if (dynamicCategories.isNotEmpty()) {
                        PanelSection(title = "Categorías en pantalla", icon = Icons.Default.Category) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp) // Sangría exterior
                            ) {
                                items(dynamicCategories) { item ->
                                    EmojiItemButton(
                                        item = item,
                                        isSelected = activeFilters.contains(item.id),
                                        modifier = Modifier.width(72.dp) // Ancho fijo para scroll
                                    ) { onAction(item.id) }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp), color = Color.White.copy(alpha = 0.1f))
                    }

                    // --- SECCIÓN 2: FILTROS TÉCNICOS ---
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        PanelSection(title = "Refinar búsqueda", icon = Icons.Default.FilterList) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val filters = listOf(
                                    ControlItem("Verif.", Icons.Default.Verified, "✅", Color(0xFF9B51E0)),
                                    ControlItem("Urgente", Icons.Default.FlashOn, "🚨", Color(0xFFFF3D00)),
                                    ControlItem("Nuevos", Icons.Default.NotificationsActive, "🔔", Color(0xFFFACC15)),
                                    ControlItem("Favs", Icons.Default.Favorite, "❤️", Color(0xFFE91E63))
                                )
                                filters.forEach { item ->
                                    EmojiItemButton(item, activeFilters.contains(item.id), Modifier.weight(1f)) { onAction(item.id) }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp), color = Color.White.copy(alpha = 0.1f))

                    // --- SECCIÓN 3: ORDEN TÁCTICO ---
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        PanelSection(title = "Orden táctico", icon = Icons.AutoMirrored.Filled.TrendingDown) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val sorts = listOf(
                                    ControlItem("Precio", Icons.AutoMirrored.Filled.TrendingDown, "📉", Color(0xFF2197F5)),
                                    ControlItem("Rank", Icons.Default.Star, "⭐", Color(0xFFF59E0B)),
                                    ControlItem("Entrega", Icons.Default.Timer, "⏱️", Color(0xFF94A3B8)),
                                    ControlItem("Fecha", Icons.Default.CalendarToday, "📅", Color(0xFF64748B))
                                )
                                sorts.forEach { item ->
                                    EmojiItemButton(item, activeFilters.contains(item.id), Modifier.weight(1f)) { onAction(item.id) }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp), color = Color.White.copy(alpha = 0.1f))

                    // --- SECCIÓN 4: HERRAMIENTAS Y SIMULACIÓN ---
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        PanelSection(title = "Gestión y Simulación", icon = Icons.Default.Build) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val tools = listOf(
                                    ControlItem("Vista", Icons.Default.GridView, "🖼️", Color(0xFF00FFC2), "vista"),
                                    ControlItem("Sim. Chat", Icons.Default.PrecisionManufacturing, "🤖", Color(0xFF9B51E0), "sim_chat"),
                                    ControlItem("Sim. Lic.", Icons.Default.GroupAdd, "📝", Color(0xFFF87171), "sim_lic"),
                                    ControlItem("Refresh", Icons.Default.Refresh, "🔄", Color(0xFF10B981), "refresh")
                                )
                                tools.forEach { item ->
                                    EmojiItemButton(item, activeFilters.contains(item.id), Modifier.weight(1f)) { onAction(item.id) }
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
 * Modificado para no forzar un Row interno, permitiendo LazyRow en categorías.
 */
@Composable
fun PanelSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = if(title.contains("Categorías")) 24.dp else 8.dp, bottom = 14.dp)
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(8.dp))
            Text(title.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.5.sp)
        }
        content()
    }
}

/**
 * Componente abstracto para los botones del panel (Soporta Weight y Width fijo).
 */
@Composable
fun EmojiItemButton(
    item: ControlItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    if (isSelected) item.color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(18.dp)
                )
                .border(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) item.color else Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // EFECTO GLOW PARA SELECCIONADO CON EMOJI
            if (isSelected) {
                Box(modifier = Modifier.size(30.dp).graphicsLayer { alpha = 0.4f }.blur(15.dp).background(item.color))
                Text(text = item.emoji, fontSize = 24.sp)
            } else {
                item.icon?.let { Icon(it, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp)) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
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
        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
        shadowElevation = 12.dp,
        border = BorderStroke(2.5.dp, rainbowBrush)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(0.8f), modifier = Modifier.padding(start = 24.dp).size(20.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier.weight(1f).padding(start = 12.dp).focusRequester(focusRequester),
                textStyle = TextStyle(color = colors.onSurface, fontSize = 17.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) { Text(placeholderText, color = colors.onSurfaceVariant, fontSize = 16.sp) }
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
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 1f)))))
        }
        Box(
            modifier = Modifier.fillMaxSize().padding(bottomPadding).navigationBarsPadding().padding(bottom = 10.dp, end = 10.dp),
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
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isActive) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = if (isActive) Color.White else Color.Gray.copy(alpha = 0.5f), textDecoration = if (!isActive) TextDecoration.LineThrough else null)
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
    val services = mainCompany?.services ?: emptyList()

    val works24h = mainCompany?.works24h ?: false
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: false
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: false
    val acceptsAppointments = mainCompany?.acceptsAppointments ?: false

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
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = provider.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    fallback = painterResource(id = R.drawable.iconapp),
                                    modifier = Modifier.fillMaxSize().clickable { onClick() },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                overflow = TextOverflow.Ellipsis
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
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = if (works24h) activeColor else inactiveColor)
                                    Icon(Icons.Default.Home, null, modifier = Modifier.size(16.dp), tint = if (doesHomeVisits) activeColor else inactiveColor)
                                    Icon(Icons.Default.Storefront, null, modifier = Modifier.size(16.dp), tint = if (hasPhysicalLocation) activeColor else inactiveColor)
                                    Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp), tint = if (acceptsAppointments) activeColor else inactiveColor)
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
                    services.forEach { service ->
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
        AlertDialog(onDismissRequest = { showFavoriteDialog = false }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }, title = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, text = { Text(if (provider.isFavorite) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") }, confirmButton = { TextButton(onClick = { onToggleFavorite?.invoke(provider.id, provider.isFavorite); showFavoriteDialog = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { showFavoriteDialog = false }) { Text("Cancelar") } })
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

enum class BannerType(val label: String) { GOOGLE_AD("SPONSORED"), PROMO("PROMOCIÓN"), NEW_CATEGORY("NUEVA CATEGORÍA"), NEW_PROVIDER("NUEVOS PRESTADORES"), PRODUCT_SALE("VENTA DE PRODUCTO") }

data class AccordionBanner(val id: String, val title: String, val subtitle: String, val icon: String, val color: Color, val type: BannerType, val originalCategory: CategoryEntity? = null, val isNew: Boolean = false, val imageUrl: String? = null)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumLensCarousel(items: List<AccordionBanner>, onSettingsClick: () -> Unit, onItemClick: (AccordionBanner) -> Unit, modifier: Modifier = Modifier, autoplayDelay: Long = 5000L) {
    if (items.isEmpty()) return
    val infiniteCount = Int.MAX_VALUE
    val initialPage = infiniteCount / 2 - (infiniteCount / 2 % items.size)
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }
    LaunchedEffect(key1 = items) { while (true) { delay(autoplayDelay); if (items.size > 1) { pagerState.animateScrollToPage(pagerState.currentPage + 1) } } }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "DESTACADOS & NOVEDADES", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(34.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)) { Icon(Icons.Default.Tune, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)) }
        }
        HorizontalPager(state = pagerState, pageSize = PageSize.Fixed(260.dp), pageSpacing = 16.dp, contentPadding = PaddingValues(horizontal = 32.dp), modifier = Modifier.fillMaxWidth().height(140.dp)) { index ->
            val actualIndex = index % items.size
            val item = items[actualIndex]
            val pageOffset = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
            Box(modifier = Modifier.graphicsLayer { val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)); scaleX = scale; scaleY = scale; alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f)) }) {
                if (item.type == BannerType.GOOGLE_AD) AdBannerItem(item = item) else PremiumBannerItem(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
fun AdBannerItem(item: AccordionBanner) {
    Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AdsClick, null, tint = Color.Gray); Text(item.title, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp)); Text(item.subtitle, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp)) } }
            Box(modifier = Modifier.align(Alignment.TopStart).background(Color(0xFFFFC107), RoundedCornerShape(bottomEnd = 8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("ANUNCIO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
        }
    }
}

@Composable
fun PremiumBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxSize().clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = item.color), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
            Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache { val gradient = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent), start = Offset(0f, 0f), end = Offset(size.width, size.height)); onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) } })
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent), startX = 0f, endX = 500f)))
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(0.6f).fillMaxHeight().padding(start = 16.dp, top = 16.dp, bottom = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Column { AutoResizingText(text = item.title.uppercase(), color = Color.White, maxFontSize = 16.sp); Text(text = item.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 1); Spacer(modifier = Modifier.height(8.dp)); Box(modifier = Modifier.width(32.dp).height(2.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))) }
                }
                Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.7f).background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent))))
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) { Text(text = item.icon, fontSize = 90.sp, modifier = Modifier.offset(x = 15.dp)) }
            }
            if (item.isNew) Surface(color = Color(0xFFFFD600), shape = RoundedCornerShape(bottomEnd = 14.dp), modifier = Modifier.align(Alignment.TopStart)) { Text(text = "NUEVO", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }
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
    var active by remember { mutableStateOf(setOf("verif.", "precio")) }

    val mockCategories = listOf(
        ControlItem("Informática", null, "💻", Color(0xFFB2EBF2), "cat_info"),
        ControlItem("Mecánica", null, "🔧", Color(0xFFFFDAC1), "cat_mec"),
        ControlItem("Limpieza", null, "🧹", Color(0xFFFAD2E1), "cat_limp")
    )

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0E14))) {
            GeminiSplitFAB(
                isExpanded = expanded,
                isSearchActive = false,
                activeFilters = active,
                dynamicCategories = mockCategories, // Prueba pasándole categorías o emptyList()
                onToggleExpand = { expanded = !expanded },
                onActivateSearch = {},
                onCloseSearch = {},
                onAction = { id ->
                    active = if(active.contains(id)) active - id else active + id
                },
                onResetAll = { active = emptySet(); expanded = false },
                secondaryActions = {
                    SmallActionFab(icon = Icons.Default.Favorite, label = "Favs", iconColor = Color(0xFFE91E63)) {}
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun SmallActionFabPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(16.dp).background(Color(0xFF05070A)),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SmallActionFab(
                icon = Icons.Default.Favorite,
                label = "Favs",
                iconColor = Color(0xFFE91E63),
                isActive = false,
                onClick = {}
            )
            SmallActionFab(
                icon = Icons.Default.Favorite,
                label = "Favs",
                iconColor = Color(0xFFE91E63),
                isActive = true,
                onClick = {}
            )
        }
    }
}
