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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.getAppColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Tune
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.*
//import androidx.compose.ui.platform.LocalContentColor
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.myapplication.data.model.CompanyProvider


import androidx.compose.material3.ExperimentalMaterial3Api








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
    // Si isAnimated es true, creamos la transición infinita. Si no, usamos 0f.
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
        0f // Posición estática
    }

    return Brush.linearGradient(
        colors = GeminiColors,
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Mirror
    )
}




// =================================================================================
// --- COMPONENTES REUTILIZABLES PARA EL EFECTO GEMINI Y FAB -- -
// =================================================================================

/**
 * Efecto de gradiente animado estilo Gemini.
 */
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

/**
 * Botón de acción pequeño con ícono y texto DENTRO del botón.
 * [MODIFICADO] Se agregó un contorno más claro y brillante para diferenciarlo mejor.
 */
@Composable
fun SmallActionFab(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 64.dp, height = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        shadowElevation = 8.dp,
        // [MODIFICADO] Borde más claro y visible
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor, // Color personalizado para el ícono
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(24.dp)
            )
            Text(
                text = label,
                color = colors.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Herramienta pequeña para el menú expandido del FAB.
 */
@Composable
fun SmallFabTool(label: String, icon: ImageVector, isSelected: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

/**
 * Estructura base para un FAB dividido estilo Gemini.
 */
@Composable
fun GeminiSplitFAB(
    isExpanded: Boolean,
    isSearchActive: Boolean,
    isSecondaryPanelVisible: Boolean = false,
    onToggleExpand: () -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onCloseSecondaryPanel: () -> Unit = {},
    secondaryActions: @Composable RowScope.() -> Unit = {},
    expandedTools: @Composable ColumnScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    val fabIconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabRotation"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(16.dp) // --- AUMENTADO --- Espacio entre los botones y el FAB principal
    ) {
        // --- ACCIONES RÁPIDAS (IZQUIERDA) ---
        AnimatedVisibility(
            visible = !isSecondaryPanelVisible && !isSearchActive,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // --- AUMENTADO --- Espacio entre los botones de acción
                secondaryActions()
            }
        }

        // --- BLOQUE PRINCIPAL (DERECHA) ---
        Column(horizontalAlignment = Alignment.End) {
            // Herramientas que aparecen arriba cuando se expande
            AnimatedVisibility(
                visible = isExpanded && !isSecondaryPanelVisible && !isSearchActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    expandedTools()
                }
            }

            // El Botón Dividido
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Parte Izquierda: Buscar
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Surface(
                        onClick = onActivateSearch,
                        modifier = Modifier
                            .height(56.dp)
                            .width(130.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                        color = colors.surface,
                        border = BorderStroke(2.5.dp, rainbowBrush),
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Buscar", color = colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Search, null, tint = colors.onSurface.copy(0.8f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Parte Derecha: Ajustes / Cerrar
                Surface(
                    onClick = {
                        when {
                            isSecondaryPanelVisible -> onCloseSecondaryPanel()
                            isSearchActive -> onCloseSearch()
                            else -> onToggleExpand()
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    shape = if (isSearchActive || isSecondaryPanelVisible) CircleShape else RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                    color = colors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSecondaryPanelVisible || isSearchActive) {
                            Icon(Icons.Default.Close, "Cerrar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                        } else {
                            Icon(
                                Icons.Default.Settings,
                                "Ajustes",
                                tint = colors.onSurface,
                                modifier = Modifier.size(26.dp).rotate(fabIconRotation)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun GeminiTopSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholderText: String = "Buscar...",
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    // Obtenemos el controlador del teclado para mostrarlo automáticamente
    val keyboardController = LocalSoftwareKeyboardController.current

    // Efecto para solicitar foco y mostrar el teclado cuando aparece
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
            Icon(
                Icons.Default.Search,
                null,
                tint = colors.onSurface.copy(0.8f),
                modifier = Modifier.padding(start = 24.dp).size(20.dp)
            )

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
                    .focusRequester(focusRequester),
                textStyle = TextStyle(color = colors.onSurface, fontSize = 17.sp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(placeholderText, color = colors.onSurfaceVariant, fontSize = 16.sp)
                        }
                        inner()
                    }
                }
            )
        }
    }
}

/**
 * Barra de búsqueda con botón de configuración reutilizable
 * FAB en la parte inferior derecha que se expande hacia arriba al buscar
 * Mismo estilo visual que el HomeScreen
 */
@Composable
fun SearchBarWithSettings(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchToggle: () -> Unit,
    onSettingsClick: () -> Unit = {},
    placeholderText: String = "Buscar...",
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val rainbowBrush = geminiGradientEffect()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = modifier.fillMaxSize()) {
        // Contenido principal
        content(PaddingValues(bottom = 80.dp))

        // Barra de búsqueda expandida (aparece arriba cuando está activa)
        if (isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .zIndex(10f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.surface,
                        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                        shadowElevation = 12.dp,
                        border = BorderStroke(2.5.dp, rainbowBrush)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                null,
                                tint = Color.White.copy(0.8f),
                                modifier = Modifier.padding(start = 24.dp).size(20.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (searchQuery.isEmpty()) {
                                            Text(placeholderText, color = Color.Gray, fontSize = 16.sp)
                                        }
                                        inner()
                                    }
                                }
                            )
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }

                // Botón de Cerrar (X)
                Surface(
                    modifier = Modifier.size(56.dp).clickable {
                        onSearchToggle()
                        onSearchQueryChange("")
                        keyboardController?.hide()
                    },
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, "Cerrar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        // FABs en la parte inferior derecha (solo cuando NO está buscando)
        if (!isSearchActive) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .zIndex(10f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Configuración/Engranaje
                Surface(
                    modifier = Modifier.size(56.dp),
                    onClick = onSettingsClick,
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, "Configuración", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                    }
                }

                // Botón de Búsqueda
                Surface(
                    modifier = Modifier.size(56.dp),
                    onClick = onSearchToggle,
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(2.5.dp, rainbowBrush),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Search, "Buscar", tint = colors.onSurface, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
    }
}

// =================================================================================
//                      GEMINI FAB CON SCRIM Y LOADER GELATINA ACTUALIZADOS
// =================================================================================
// --- COMPONENTE 1: SCRIM ANIMADO (MEJORADO) ---
@Composable
fun GeminiFABWithScrim(
    bottomPadding: PaddingValues,
    showScrim: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Animación de entrada y salida del degradado
        AnimatedVisibility(
            visible = showScrim,
            enter = fadeIn(animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // Altura suficiente para cubrir los botones
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 1f) // Negro profundo pero translúcido
                            )
                        )
                    )
            )
        }

        // Contenedor de botones (FAB)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottomPadding) // Respeta la altura del NavigationBar
                .navigationBarsPadding() // Mejora: Asegura que no choque con gestos del sistema
                .padding(bottom = 10.dp, end = 10.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            content()
        }

    }
}

// --- COMPONENTE 2: CARGADOR M3 (GELATINA) ---
@Composable
fun M3JellyLoader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jelly")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)), label = "rot"
    )
    val shapeValue by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "morph"
    )

    Box(
        modifier = modifier.size(size).graphicsLayer {
            rotationZ = rotation
            scaleX = 1f + (0.15f * shapeValue)
            scaleY = 1f - (0.15f * shapeValue)
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                val segments = 12
                val innerRadius = size.toPx() * 0.42f
                val outerRadius = size.toPx() * 0.48f
                val angleStep = (2 * Math.PI / segments).toFloat()
                for (i in 0 until segments) {
                    val angle = i * angleStep
                    val x = center.x + Math.cos(angle.toDouble()).toFloat() * outerRadius
                    val y = center.y + Math.sin(angle.toDouble()).toFloat() * outerRadius
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                    val midAngle = angle + angleStep / 2
                    val mx = center.x + Math.cos(midAngle.toDouble()).toFloat() * innerRadius
                    val my = center.y + Math.sin(midAngle.toDouble()).toFloat() * innerRadius
                    lineTo(mx, my)
                }
                close()
            }
            drawPath(path, color = color)
        }
    }
}



@Composable
fun ContainedJellyLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer { clip = true; shape = CircleShape }
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            //.graphicsLayer { clip = true; shape = CircleShape }
            //.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        M3JellyLoader(size = 44.dp)
    }
}

// =================================================================================
// --- OTROS COMPONENTES REUTILIZABLES ---
// =================================================================================

data class MenuAction(
    val text: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ServiceTag(text: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )
    }
}

@Composable
fun RowItemDetail(icon: ImageVector, text: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isActive) Color.White else Color.Gray.copy(alpha = 0.5f),
            textDecoration = if (!isActive) TextDecoration.LineThrough else null
        )
    }
}


// ==========================================================================================
// --- SECCIÓN 3: PRESTADOR CARD (LÓGICA ORIGINAL + IU CYBERPUNK) ---
// ==========================================================================================

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
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    // Estilos y Animaciones
    val staticBrush = geminiGradientBrush(isAnimated = false)
    val animateBrush = geminiGradientBrush(isAnimated = true)

    // Colores de estado
    val activeColor = Color(0xFF22D3EE) // Cyan Encendido
    val inactiveColor = Color.White.copy(alpha = 0.15f) // Gris Apagado
    val cyberBackground = Color(0xFF0A0E14)

    // Datos de la Compañía
    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""
    val services = mainCompany?.services ?: emptyList()

    // Booleans de estado
    val works24h = mainCompany?.works24h ?: false
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: false
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: false
    val acceptsAppointments = mainCompany?.acceptsAppointments ?: false // Nuevo dato

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
            colors = CardDefaults.cardColors(containerColor = cyberBackground),
            shape = RoundedCornerShape(24.5.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.05f)))

                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- SECCIÓN AVATAR ---
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
                                    fallback = painterResource(id = com.example.myapplication.R.drawable.iconapp),
                                    modifier = Modifier.fillMaxSize().clickable { onClick() },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Punto Online (Hice el tamaño más grande: 16.dp)
                            if (provider.isOnline && showBadges) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp) // Aumentado de 12 a 16
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .background(Color(0xFF00E676), CircleShape) // Verde brillante
                                        .border(2.dp, cyberBackground, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // --- SECCIÓN INFO CENTRAL ---
                    Column(modifier = Modifier.weight(1f)) {
                        // Nombre Prestador
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

                        // Nombre de Compañía (Debajo del nombre)
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

                        // Fila de Ranking e Iconos de Servicios
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        ) {
                            // Rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // ICONOS BOOLEANOS (Centrados entre Rating y Corazón)
                            if (showBadges) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // 1. 24 Horas
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "24hs",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (works24h) activeColor else inactiveColor
                                    )
                                    // 2. Visitas a Domicilio
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Domicilio",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (doesHomeVisits) activeColor else inactiveColor
                                    )
                                    // 3. Local Físico
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Local",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (hasPhysicalLocation) activeColor else inactiveColor
                                    )
                                    // 4. Turnos / Citas
                                    Icon(
                                        imageVector = Icons.Default.Event, // O CalendarMonth
                                        contentDescription = "Turnos",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (acceptsAppointments) activeColor else inactiveColor
                                    )
                                }
                            }

                            // Botón Favorito
                            IconButton(onClick = { showFavoriteDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (provider.isFavorite) Color.Red else Color.Gray
                                )
                            }
                        }
                    }

                    // Botón de Acción Lateral (Chat)
                    if (actionContent != null) {
                        actionContent()
                    } else {
                        // Ajuste visual si es necesario separar un poco del botón fav
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onChat?.invoke() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(animateBrush, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Dropdown Menu (Context Menu)
        DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) {
            DropdownMenuItem(text = { Text("Ver Perfil Completo") }, leadingIcon = { Icon(Icons.Default.Person, null) }, onClick = { showContextMenu = false; onClick() })
            HorizontalDivider()
            DropdownMenuItem(text = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, leadingIcon = { Icon(imageVector = if (provider.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (provider.isFavorite) Color.Red else Color.Unspecified) }, onClick = { showContextMenu = false; showFavoriteDialog = true })
        }
    }

    // Modal Bottom Sheet (Detalle)
    if (showDetailSheet) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }, containerColor = cyberBackground, tonalElevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = provider.photoUrl, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(modifier = Modifier.width(16.dp)); Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = provider.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        if (provider.isVerified) { Spacer(modifier = Modifier.width(6.dp)); Icon(Icons.Filled.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(24.dp)) }
                    }
                    if (companyName.isNotEmpty()) {
                        Text(text = companyName, style = MaterialTheme.typography.titleSmall, color = Color(0xFF22D3EE))
                    }
                    Text(text = provider.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                }
                Spacer(modifier = Modifier.height(24.dp)); Text("Servicios Ofrecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    services.forEach { service -> ServiceTag(text = service, color = Color.White.copy(0.1f)) }
                }
                Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = Color.White.copy(0.1f)); Spacer(modifier = Modifier.height(24.dp))

                // Detalles con lógica visual en el BottomSheet también
                RowItemDetail(icon = Icons.Default.Schedule, text = "Disponible 24hs", isActive = works24h)
                RowItemDetail(icon = Icons.Default.Home, text = "Visitas a Domicilio", isActive = doesHomeVisits)
                RowItemDetail(icon = Icons.Default.Storefront, text = "Local Físico", isActive = hasPhysicalLocation)
                RowItemDetail(icon = Icons.Default.Event, text = "Turnos / Citas", isActive = acceptsAppointments)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showDetailSheet = false; onClick() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(animateBrush, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Perfil Completo", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showFavoriteDialog) {
        AlertDialog(onDismissRequest = { showFavoriteDialog = false }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }, title = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, text = { Text(if (provider.isFavorite) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") }, confirmButton = { TextButton(onClick = { onToggleFavorite?.invoke(provider.id, provider.isFavorite); showFavoriteDialog = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { showFavoriteDialog = false }) { Text("Cancelar") } })
    }
}


/**
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
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    // Animaciones en las tarjetas 
    val staticBrush = geminiGradientBrush(isAnimated = false)
    val animateBrush =geminiGradientBrush(isAnimated = true)

    val activeColor = Color(0xFF22D3EE)
    val cyberBackground = Color(0xFF0A0E14)

    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""
    val services = mainCompany?.services ?: emptyList()
    val works24h = mainCompany?.works24h ?: false
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: false
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: false

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(staticBrush, RoundedCornerShape(26.dp))
            .padding(1.5.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.5.dp))
                .combinedClickable(
                    onClick = { showDetailSheet = true },
                    onLongClick = { showContextMenu = true }
                ),
            colors = CardDefaults.cardColors(containerColor = cyberBackground),
            shape = RoundedCornerShape(24.5.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.matchParentSize().background(Color.White.copy(alpha = 0.05f)))

                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showAvatars) {
                        Box(contentAlignment = Alignment.TopStart) {
                            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = provider.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    fallback = painterResource(id = com.example.myapplication.R.drawable.iconapp),
                                    modifier = Modifier.fillMaxSize().clickable { onClick() },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (provider.isOnline && showBadges) {
                                Box(modifier = Modifier.size(12.dp).offset(x = (-4).dp, y = (-4).dp).background(activeColor, CircleShape).border(2.dp, cyberBackground, CircleShape))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = provider.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            if (provider.isVerified && showBadges) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(18.dp))
                            }
                        }

                        if (companyName.isNotEmpty() && showPreviews) {
                            Text(text = companyName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                Text(text = " ${provider.rating}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            if (showBadges) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (works24h) Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
                                    if (doesHomeVisits) Icon(Icons.Default.Home, null, modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
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
                        IconButton(
                            onClick = { onChat?.invoke() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(animateBrush, RoundedCornerShape(14.dp))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = provider.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        if (provider.isVerified) { Spacer(modifier = Modifier.width(6.dp)); Icon(Icons.Filled.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(24.dp)) }
                    }
                    Text(text = provider.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                }
                Spacer(modifier = Modifier.height(24.dp)); Text("Servicios Ofrecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    services.forEach { service -> ServiceTag(text = service, color = Color.White.copy(0.1f)) }
                }
                Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = Color.White.copy(0.1f)); Spacer(modifier = Modifier.height(24.dp))

                // Aquí se llaman los RowItemDetail con la lógica de color
                RowItemDetail(icon = Icons.Default.Schedule, text = "Disponible 24hs", isActive = works24h)
                RowItemDetail(icon = Icons.Default.Home, text = "Visitas a Domicilio", isActive = doesHomeVisits)
                RowItemDetail(icon = Icons.Default.Storefront, text = "Local Físico", isActive = hasPhysicalLocation)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showDetailSheet = false; onClick() },
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(animateBrush, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Perfil Completo", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showFavoriteDialog) {
        AlertDialog(onDismissRequest = { showFavoriteDialog = false }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }, title = { Text(if (provider.isFavorite) "Quitar de Favoritos" else "Añadir a Favoritos") }, text = { Text(if (provider.isFavorite) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") }, confirmButton = { TextButton(onClick = { onToggleFavorite?.invoke(provider.id, provider.isFavorite); showFavoriteDialog = false }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { showFavoriteDialog = false }) { Text("Cancelar") } })
    }
}

    // * Un menú flotante genérico que muestra un FAB y despliega un menú desplegable al hacer clic.
    // *
    // * @param icon El icono para mostrar en el FAB.
    // * @param options Una lista de pares donde el primero es el texto de la opción y el segundo es la acción a ejecutar.
    // */

@Composable
fun GenericFloatingMenu(
        icon: ImageVector,
        options: List<Pair<String, () -> Unit>>
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box {
            FloatingActionButton(onClick = { expanded = !expanded }) {
                Icon(icon, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (label, action) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            action()
                            expanded = false
                        }
                    )
                }
            }
        }
    }

// =========================================================
// 1. LA VISTA DE LA CARPETA (El Popup)
// =========================================================
@Composable
fun FolderExpandedView(
    superCategoryName: String,
    items: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)) // Fondo oscuro
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                // Efecto "Pop" elástico
                enter = scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f)) + fadeIn(),
                exit = scaleOut(targetScale = 0.8f) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f) 
                        .fillMaxHeight(0.85f) 
                        .padding(16.dp)
                        .clickable(enabled = false) {}, // Evita cerrar al tocar dentro
                    shape = RoundedCornerShape(28.dp),
                    colors = cardColors(containerColor = Color(0xFF15191C)), // Gris muy oscuro
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        // --- CABECERA ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                superCategoryName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- GRILLA (AQUÍ ESTÁ LA MAGIA) ---
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2), 
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),   // Espacio vertical chico
                            horizontalArrangement = Arrangement.spacedBy(8.dp), // Espacio horizontal chico
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(items) { item ->
                                CompactCategoryCard(
                                    item = item,
                                    onClick = { onCategoryClick(item.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 2. LA TARJETA COMPACTA Categorias (Estilo Premium Mini)
// =========================================================

/**
 * Tarjeta de Categoría Premium Empresarial.
 * Diseñada para cuadrículas de 2 columnas.
 */
@Composable
fun CompactCategoryCard(
    item: CategoryEntity,
    onClick: () -> Unit
) {
    val baseColor = Color(item.color)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = cardColors(containerColor = baseColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Capa de Textura y Brillo (Efecto empresarial)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        val gradient = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(gradient, blendMode = BlendMode.Overlay)
                        }
                    }
            )

            // 2. Gradiente lateral para mejorar legibilidad del texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent),
                            startX = 0f,
                            endX = 450f
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LADO IZQUIERDO: TEXTO (60%)
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .padding(
                            start = 8.dp,//Donde Inicia el Texto
                            end = 2.dp,//Donde Termina el texto
                            top = if (item.isNew) 18.dp else 0.dp // Baja el texto si hay etiqueta
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        AutoResizingText(
                            text = item.name.uppercase(),
                            color = Color.White,
                            maxFontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Línea de detalle premium
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(1.5.dp)
                                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                        )
                    }
                }

                // DIVISOR MODERNO
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(85.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // LADO DERECHO: ICONO (40%)
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = item.icon,
                        fontSize = 90.sp, // Icono masivo
                        modifier = Modifier
                            .offset(x = 10.dp) // Mitad del icono queda fuera (efecto ventana)
                            .graphicsLayer(alpha = 1f)
                    )
                }
            }

            // ETIQUETA NUEVO (Estilo refinado en esquina)
            if (item.isNew) {
                Surface(
                    color = Color(0xFFFFD600),
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "NUEVO",
                        color = Color.Black,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Helper: Texto que reduce su tamaño automáticamente si no cabe en 2 líneas.
 */
@Composable
private fun AutoResizingText(
    text: String,
    color: Color,
    maxFontSize: TextUnit,
    minFontSize: TextUnit = 8.sp
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = if (readyToDraw) color else Color.Transparent,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        lineHeight = fontSize * 1.1f,
        maxLines = 2,
        letterSpacing = 1.1.sp,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > minFontSize) {
                fontSize = (fontSize.value * 0.9f).sp
            } else {
                readyToDraw = true
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
// ==========================================================================================
// --- SECCIÓN 1: MODELOS DE DATOS PARA COMPONENTES ---
// ==========================================================================================

/**
 * Define el tipo de etiqueta visual que mostrará el banner del carrusel.
 * Ayuda a diferenciar visualmente entre publicidad, promos y novedades.
 */
enum class BannerType(val label: String) {
    GOOGLE_AD("SPONSORED"),
    PROMO("PROMOCIÓN"),
    NEW_CATEGORY("NUEVA CATEGORÍA"),
    NEW_PROVIDER("NUEVOS PRESTADORES")
}

/**
 * Estructura de datos que consume el Carrusel.
 * Vincula la lógica de negocio con la visualización del banner.
 */
data class AccordionBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val type: BannerType,
    val originalCategory: CategoryEntity? = null,
    val isNew : Boolean = false
)

// ==========================================================================================
// --- SECCIÓN 2: COMPONENTE CARRUSEL PREMIUM (MATERIAL 3 CENTER-ALIGNED HERO) ---
// Implementa el diseño oficial de M3 con un item central grande y adelantos laterales.
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumLensCarousel(
    items: List<AccordionBanner>, // Asumo que AccordionBanner tiene isNew, type, etc.
    onSettingsClick: () -> Unit,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 5000L
) {
    if (items.isEmpty()) return

    val carouselState = rememberCarouselState { items.size }
// Usamos un estado local para el autoplay
    var currentItemIndex by remember { mutableIntStateOf(0) }
    // Lógica de Autoplay
    // Usamos un índice local para controlar el scroll automático sin romper el estado del usuario
    LaunchedEffect(key1 = items) {
        while (true) {
            delay(autoplayDelay)
            // Calculamos el siguiente índice de forma segura
            currentItemIndex = (currentItemIndex + 1) % items.size
            //val nextIndex = (carouselState.currentItemOffset.toInt() + 1) % items.size
            try {
                carouselState.animateScrollToItem(currentItemIndex)
               // carouselState.animateScrollToItem(nextIndex)
            } catch (e: Exception) {
                // Manejo silencioso si el carrusel se desmonta
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // --- CABECERA (Igual que antes) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DESTACADOS & NOVEDADES",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Ajustes",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // --- CARRUSEL ---
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = 240.dp, // Ancho un poco mayor para lucir el diseño 60/40
            itemSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp) // Altura suficiente para el diseño Premium
        ) { index ->
            val item = items[index]

            // -----------------------------------------------------------
            // 🛑 ZONA DE ANUNCIOS (PREPARADO PARA ADMOB)
            // -----------------------------------------------------------
            // Asumo que tu enum AccordionType tiene un valor 'Ad' o 'GoogleAd'
            // Si no lo tiene, puedes filtrar por título o agregar el tipo.
            val isGoogleAd = item.type.name == "GoogleAd" || item.title == "Publicidad"

            if (isGoogleAd) {
                // RENDERIZADO DEL ANUNCIO
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // AQUÍ VA EL CODIGO DE ADMOB REAL CUANDO LO TENGAS
                        /*
                        AndroidView(
                            factory = { context ->
                                AdView(context).apply {
                                    setAdSize(AdSize.BANNER)
                                    adUnitId = "ca-app-pub-TU-ID"
                                    loadAd(AdRequest.Builder().build())
                                }
                            }
                        )
                        */

                        // Placeholder Visual por ahora
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AdsClick, null, tint = Color.Gray)
                            Text("Publicidad", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        // Etiqueta "Anuncio" obligatoria por Google
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(Color(0xFFFFC107), RoundedCornerShape(bottomEnd = 8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ANUNCIO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            } else {
                // -----------------------------------------------------------
                // ✨ TARJETA PREMIUM (ESTILO COMPACTCATEGORYCARD)
                // -----------------------------------------------------------
                val baseColor = item.color // Color base de la categoría

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onItemClick(item) },
                    shape = RoundedCornerShape(24.dp), // Radio coincidente con el carrusel
                    colors = CardDefaults.cardColors(containerColor = baseColor),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        // 1. Capa de Textura y Brillo (Efecto empresarial)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(alpha = 0.99f)
                                .drawWithCache {
                                    val gradient = Brush.linearGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                                    )
                                    onDrawWithContent {
                                        drawContent()
                                        drawRect(gradient, blendMode = BlendMode.Overlay)
                                    }
                                }
                        )

                        // 2. Gradiente lateral NEGRO -> TRANSPARENTE
                        // ESTO ES CLAVE PARA LA LEGIBILIDAD DEL TEXTO
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                                        startX = 0f,
                                        endX = 500f // Cubre la mitad izquierda
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- LADO IZQUIERDO: TEXTO (60%) ---
                            Box(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .fillMaxHeight()
                                    .padding(start = 16.dp, top = 16.dp, bottom = 12.dp, end = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(verticalArrangement = Arrangement.Center) {
                                    // Titulo Principal (Ajustable)
                                    AutoResizingText(
                                        text = item.title.uppercase(),
                                        color = Color.White,
                                        maxFontSize = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Subtítulo / Detalle (Ej: "5 Nuevos Locales")
                                    Text(
                                        text = item.subtitle,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Línea de detalle premium
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(2.dp)
                                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                                    )
                                }
                            }

                            // --- DIVISOR VERTICAL MODERNO ---
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(0.7f) // No llega hasta los bordes
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // --- LADO DERECHO: ICONO (40%) ---
                            Box(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = item.icon,
                                    fontSize = 90.sp, // Icono Masivo
                                    modifier = Modifier
                                        .offset(x = 15.dp) // Efecto ventana (se sale un poco)
                                        .graphicsLayer(alpha = 1f)
                                )
                            }
                        }

                        // 3. ETIQUETA "NUEVO" (Esquina Superior Izquierda)
                        // Verifica tu propiedad booleana, aquí asumo item.isNew
                        // Si no tienes isNew, puedes usar lógica: item.subtitle.contains("Nuevo")
                        if (item.isNew) {
                            Surface(
                                color = Color(0xFFFFD600), // Amarillo intenso
                                shape = RoundedCornerShape(bottomEnd = 14.dp),
                                modifier = Modifier.align(Alignment.TopStart),
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "NUEVO", // O "NUEVO PRESTADOR"
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper NECESARIO para que funcione el texto ajustable ---

/**
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumLensCarousel(
    items: List<AccordionBanner>,
    onSettingsClick: () -> Unit,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 5000L
) {
    if (items.isEmpty()) return

    // Estado oficial de Material 3 Carousel
    val carouselState = rememberCarouselState { items.size }

    // Corregimos el acceso a pagerState usando un índice local para el Autoplay
    var currentItemIndex by remember { mutableIntStateOf(0) }

    // Lógica de Autoplay corregida para evitar errores de acceso interno
    LaunchedEffect(key1 = items) {
        while (true) {
            delay(autoplayDelay)
            currentItemIndex = (currentItemIndex + 1) % items.size
            carouselState.animateScrollToItem(currentItemIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Cabecera estilizada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DESTACADOS & NOVEDADES",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // HorizontalMultiBrowseCarousel configurado para efecto Hero Centrado
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            // preferredItemWidth alto (ej. 320.dp) fuerza a un diseño focal centrado (Hero)
            preferredItemWidth = 220.dp,
            itemSpacing = 10.dp,
            contentPadding = PaddingValues(horizontal = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) { index ->
            val item = items[index]

            // Tarjeta con diseño premium 60/40
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        currentItemIndex = index // Sincronizamos el índice al tocar
                        onItemClick(item)
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = item.color),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Capa de Brillo y Textura
                    Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache {
                        val gradient = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent))
                        onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) }
                    })

                    // 2. Gradiente de Profundidad
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                    ))

                    // 3. Contenido 60/40 (Texto Izquierda / Icono Derecha)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LADO IZQUIERDO: TEXTO (60%)
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .padding(start = 10.dp, top = 10.dp, end = 5.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Column {
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = item.type.label,
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.2.dp)
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                AutoResizingText(text = item.title.uppercase(), color = Color.White, maxFontSize = 14.sp)
                                Text(
                                    text = item.subtitle,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(modifier = Modifier.width(24.dp).height(1.5.dp).background(Color.White.copy(alpha = 0.4f), CircleShape))
                            }
                        }

                        // DIVIDER
                        Box(modifier = Modifier.width(1.dp).height(80.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent))))

                        // LADO DERECHO: ICONO (40%)
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = item.icon,
                                fontSize = 100.sp,
                                modifier = Modifier
                                    .offset(x = 20.dp)
                                    .graphicsLayer(alpha = 1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
**/
// ==========================================================================================
// --- 2. CONTENEDOR MAESTRO: GeminiCyberWrapper ---
// Úsalo para envolver CUALQUIER cosa (Tarjetas, Dialogs, Menús)
// ==========================================================================================

@Composable
fun GeminiCyberWrapper(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderThickness: Dp = 1.5.dp,
    isAnimated: Boolean = false, // Por defecto estático como pediste para las tarjetas
    showGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val geminiBrush = geminiGradientBrush(isAnimated = isAnimated)
    val cyberBackground = Color(0xFF0A0E14)

    Box(modifier = modifier) {
        // 1. Resplandor (Glow) exterior (opcional)
        if (showGlow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(borderThickness)
                    .blur(15.dp)
                    .background(geminiBrush, RoundedCornerShape(cornerRadius))
                    .graphicsLayer { alpha = 0.3f }
            )
        }

        // 2. El Borde Neón (Contenedor externo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(geminiBrush, RoundedCornerShape(cornerRadius))
                .padding(borderThickness) // El grosor del borde
        ) {
            // 3. El cuerpo de la tarjeta (Contenedor interno)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius - borderThickness)), // Ajuste de radio para el borde
                color = cyberBackground
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Capa de cristal (Glassmorphism)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.05f))
                    )

                    // Tu contenido real va aquí
                    content()
                }
            }
        }
    }
}


// ==========================================================================================
//                                  PREVIEWS
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PremiumInterfacePreview() {
    val mockBanners = listOf(
        AccordionBanner("1", "Hamburguesas", "Nuevos locales", "🍔", Color(0xFFD32F2F), BannerType.NEW_CATEGORY),
        AccordionBanner("2", "Google Ads", "Anuncio patrocinado", "📢", Color(0xFF1976D2), BannerType.GOOGLE_AD),
        AccordionBanner("3", "Plomería", "Nuevos técnicos", "🪠", Color(0xFF388E3C), BannerType.NEW_PROVIDER)
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0E14)).padding(vertical = 16.dp)) {
        PremiumLensCarousel(
            items = mockBanners,
            onSettingsClick = {},
            onItemClick = {}
        )

        Spacer(Modifier.height(32.dp))

        Text("VISTA DE TARJETA INDIVIDUAL", color = Color.White.copy(0.3f), fontSize = 10.sp, modifier = Modifier.padding(start = 24.dp))

        Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
            // USAMOS ARGUMENTOS NOMBRADOS PARA EVITAR ERRORES DE ORDEN O TIPO
            // Revisa que los nombres (name, icon, color, etc.) coincidan con tu CategoryEntity
            CompactCategoryCard(
                item = CategoryEntity(
                    name = "Plomería",
                    icon = "🪠",
                    color = 0xFF2196F3,
                    superCategory = "Hogar",
                    isNew = true,
                    isNewPrestador = false,
                    isAd = false,
                    imageUrl = null
                    // Si tu clase pide una lista, agrégala aquí como: tags = emptyList()
                ),
                onClick = {}
            )

        }

    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorCardPreview() {
    val sampleProvider = Provider(
        uid = "1",
        email = "juan.perez@example.com",
        displayName = "Juan Pérez",
        name = "Juan",
        lastName = "Perez",
        phoneNumber = "123456789",
        category = "Plomería",
        matricula = "MP-1234",
        titulo = "Plomero Matriculado",
        photoUrl = "https://i.pravatar.cc/150?u=1",
        bannerImageUrl = null,
        hasCompanyProfile = true,
        isSubscribed = true,
        isVerified = true,
        isOnline = true,
        isFavorite = false,
        rating = 4.8f,
        createdAt = System.currentTimeMillis(),
        companies = listOf(
            CompanyProvider(
                name = "Pérez Plomería",
                services = listOf("Plomería", "Gasista", "Destapes"),
                works24h = true,
                doesHomeVisits = true,
                hasPhysicalLocation = false
            )
        )
    )

    MyApplicationTheme {
        Surface(color = Color(0xFF0A0E14)) {
            Box(modifier = Modifier.padding(16.dp)) {
                PrestadorCard(
                    provider = sampleProvider,
                    onClick = {},
                    onChat = {},
                    onToggleFavorite = { _, _ -> }
                )
            }
        }
    }
}
