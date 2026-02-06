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
//import androidx.compose.ui.platform.LocalContentColor
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.example.myapplication.data.model.fake.CategoryItem
import com.example.myapplication.data.model.fake.SampleDataFalso
import com.example.myapplication.data.model.fake.UserFalso
import com.example.myapplication.ui.screens.SuperCategory

// =================================================================================
// --- COMPONENTES REUTILIZABLES PARA EL EFECTO GEMINI Y FAB ---
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
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
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
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun PrestadorCard(
    provider: UserFalso,
    onClick: () -> Unit, // Navegar al perfil
    onChat: (() -> Unit)? = null,
    onDeleteRequest: (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null,
    viewMode: String = "Detallada", // Vista detallada
    showDates: Boolean = true, // Mostart fechas
    showAvatars: Boolean = true, // Mostrat avatar
    showPreviews: Boolean = true, // Mostrar previews
    showBadges: Boolean = true, // Mostrar badges
) {
    val appColors = getAppColors()
    var showDetailSheet by remember { mutableStateOf(false) } // Sheet para mostrar detalle expandido
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }

    // Estado local para favoritos (solo para visualización inmediata)
    var isFavoriteLocal by remember { mutableStateOf(provider.isFavorite) }

    val activeColor = Color(0xFF10B981) // Verde
    val inactiveColor = appColors.textSecondaryColor

    // Extraer datos de la empresa principal
    val mainCompany = provider.companies.firstOrNull()
    val companyName = mainCompany?.name ?: ""
    val services = mainCompany?.services ?: emptyList()
    val works24h = mainCompany?.works24h ?: false
    val doesHomeVisits = mainCompany?.doesHomeVisits ?: false
    val hasPhysicalLocation = mainCompany?.hasPhysicalLocation ?: false
    val email = provider.emails.firstOrNull() ?: ""

    // Menú Contextual (Long Press)
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { showDetailSheet = true }, // Click simple abre detalle
                    onLongClick = { showContextMenu = true } // Long click abre menú
                ),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sección de Perfil (Foto) - ÚNICO lugar que lleva al perfil completo
                if (showAvatars) {
                    Box(
                        modifier = Modifier
                            .clickable(onClick = onClick)
                            .padding(end = 16.dp)
                    ) {
                        AsyncImage(
                            model = provider.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            fallback = painterResource(id = R.drawable.iconapp),
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (provider.isOnline && showBadges) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .size(16.dp)
                                    .border(2.dp, appColors.surfaceColor, CircleShape),
                                containerColor = Color(0xFF10B981)
                            )
                        }
                    }
                }

                // Cuerpo de la tarjeta (Datos) - Click aquí abre el Sheet (heredado del card)
                Column(modifier = Modifier.weight(1f)) {
                    // Nombre y Verificación (Ya no llevan al perfil al hacer click)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${provider.name} ${provider.lastName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimaryColor
                        )
                        if (provider.isVerified && showBadges) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.Verified,
                                "Perfil Verificado",
                                tint = appColors.accentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Nombre de empresa
                    if (companyName.isNotEmpty() && showPreviews) {
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textSecondaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // [MODIFICADO] Fila inferior con Iconos Booleanos y Rating y favorito
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${provider.rating}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        if (showBadges) {
                            // Iconos Booleanos (Más compactos)
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "24hs",
                                modifier = Modifier.size(20.dp),
                                tint = if (works24h) activeColor else inactiveColor
                            )
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Visita a Domicilio",
                                modifier = Modifier.size(20.dp),
                                tint = if (doesHomeVisits) activeColor else inactiveColor
                            )
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Local Físico",
                                modifier = Modifier.size(20.dp),
                                tint = if (hasPhysicalLocation) activeColor else inactiveColor
                            )
                        }

                        // [MODIFICADO] Icono de Favorito Visible
                        IconButton(
                            onClick = { showFavoriteDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavoriteLocal) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavoriteLocal) "Quitar favorito" else "Añadir favorito",
                                tint = if (isFavoriteLocal) Color.Red else appColors.textSecondaryColor
                            )
                        }
                    }
                }

                // Botón de Mensaje (Abre Chat)
                IconButton(
                    onClick = { onChat?.invoke() },
                    modifier = Modifier
                        .size(48.dp)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar Mensaje",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Menú Dropdown (Contextual)
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            offset = DpOffset(x = 16.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Ver Perfil Completo") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                onClick = {
                    showContextMenu = false
                    onClick() // Navegar al perfil
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(if (isFavoriteLocal) "Quitar de Favoritos" else "Añadir a Favoritos") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFavoriteLocal) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavoriteLocal) Color.Red else LocalContentColor.current
                    )
                },
                onClick = {
                    showContextMenu = false
                    showFavoriteDialog = true
                }
            )
        }
    }

    // Detalle Expandido (BottomSheet Moderno)
    if (showDetailSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDetailSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp) // Espacio extra abajo
                ) {
                    // Encabezado Detalle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = provider.profileImageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            // [MODIFICADO] Nombre con icono de verificación en BottomSheet
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${provider.name} ${provider.lastName}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (provider.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.Verified,
                                        "Perfil Verificado",
                                        tint = appColors.accentBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = appColors.textSecondaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Categorías / Servicios (Texto)
                    Text(
                        "Servicios Ofrecidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        services.forEach { service ->
                            ServiceTag(
                                text = service,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(24.dp))

                    // Detalles Booleanos
                    Text(
                        "Disponibilidad y Modalidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RowItemDetail(
                        icon = Icons.Default.Schedule,
                        text = "Disponible 24hs",
                        isActive = works24h
                    )
                    RowItemDetail(
                        icon = Icons.Default.Home,
                        text = "Visitas a Domicilio",
                        isActive = doesHomeVisits
                    )
                    RowItemDetail(
                        icon = Icons.Default.Storefront,
                        text = "Local Físico",
                        isActive = hasPhysicalLocation
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón Acción Principal en Detalle
                    Button(
                        onClick = {
                            showDetailSheet = false
                            onClick() // Ir a perfil completo
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver Perfil Completo")
                    }
                }
            }
        }

        // Alerta de Favoritos
        if (showFavoriteDialog) {
            AlertDialog(
                onDismissRequest = { showFavoriteDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Favorite,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text(if (isFavoriteLocal) "Quitar de Favoritos" else "Añadir a Favoritos") },
                text = { Text(if (isFavoriteLocal) "¿Estás seguro de que quieres eliminar a este prestador de tus favoritos?" else "¿Quieres añadir a este prestador a tu lista de favoritos?") },
                confirmButton = {
                    TextButton(onClick = {
                        // TODO: Lógica real para actualizar favorito en ViewModel/BD
                        isFavoriteLocal = !isFavoriteLocal
                        SampleDataFalso.toggleFavorite(provider.id) // Actualizar dato falso
                        showFavoriteDialog = false
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFavoriteDialog = false }) {
                        Text("Cancelar")
                    }
                }
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
                color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.5f),
                textDecoration = if (!isActive) TextDecoration.LineThrough else null
            )
        }
    }

    /**
     * Un menú flotante genérico que muestra un FAB y despliega un menú desplegable al hacer clic.
     *
     * @param icon El icono para mostrar en el FAB.
     * @param options Una lista de pares donde el primero es el texto de la opción y el segundo es la acción a ejecutar.
     */
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
    superCategory: SuperCategory,
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
                        .fillMaxWidth(0.95f) // Ancho casi total
                        .fillMaxHeight(0.85f) // Alto casi total
                        .padding(16.dp)
                        .clickable(enabled = false) {}, // Evita cerrar al tocar dentro
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15191C)), // Gris muy oscuro
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
                                superCategory.title,
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
                            columns = GridCells.Fixed(2), // 2 Columnas
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),   // Espacio vertical chico
                            horizontalArrangement = Arrangement.spacedBy(8.dp), // Espacio horizontal chico
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(superCategory.items) { item ->
                                // LLAMAMOS A LA NUEVA TARJETA COMPACTA
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
// 2. LA TARJETA COMPACTA (Estilo Premium Mini)
// =========================================================
@Composable
fun CompactCategoryCard(item: CategoryItem, onClick: () -> Unit) {
    // Estados para etiquetas
    var showNewMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp), // Bordes menos redondos para aprovechar espacio
        colors = CardDefaults.cardColors(containerColor = item.color),
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp) // <--- ALTURA REDUCIDA (Antes 140dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fila Principal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // NOMBRE (Texto ajustado)
                Box(modifier = Modifier.weight(0.65f)) {
                    Text(
                        text = item.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp, // Fuente chica
                        lineHeight = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.shadow(4.dp)
                    )
                }

                // DIVISOR
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.White, Color.Transparent)))
                )

                // ICONO
                Box(modifier = Modifier.weight(0.35f), contentAlignment = Alignment.Center) {
                    Text(
                        text = item.icon,
                        fontSize = 32.sp // Icono chico
                    )
                }
            }

            // ETIQUETA NUEVO (Si corresponde)
            if (item.isNew) {
                Surface(
                    color = Color(0xFFFFD600),
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text("NUEVO", fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

// --- CARPETA INTELIGENTE INVENTO----------------------------------------------------------- ---