package com.example.myapplication.Client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.getAppColors

// =================================================================================
// --- COMPONENTES REUTILIZABLES PARA EL FAB Y SUS HERRAMIENTAS ---
// =================================================================================

@Composable
fun ToolItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = color,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
        ) {
            Icon(icon, null, tint = Color.Black)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplitFloatingActionButton(
    modifier: Modifier = Modifier,
    isSearchExpanded: Boolean,
    isToolsExpanded: Boolean,
    onSearchClick: (Boolean) -> Unit,
    onToolsClick: (Boolean) -> Unit,
    searchContent: @Composable () -> Unit,
    horizontalTools: @Composable RowScope.() -> Unit,
    verticalTools: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        AnimatedVisibility(
            visible = isToolsExpanded && !isSearchExpanded,
            enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(transformOrigin = TransformOrigin(0.5f, 1f)),
            exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut(transformOrigin = TransformOrigin(0.5f, 1f)),
            modifier = Modifier.padding(bottom = 72.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) { verticalTools() }
        }
        
        AnimatedVisibility(
            visible = isToolsExpanded && !isSearchExpanded,
            enter = fadeIn() + slideInHorizontally { it / 2 } + scaleIn(transformOrigin = TransformOrigin(1f, 0.5f)),
            exit = fadeOut() + slideOutHorizontally { it / 2 } + scaleOut(transformOrigin = TransformOrigin(1f, 0.5f)),
            modifier = Modifier.padding(end = 72.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) { horizontalTools() }
        }

        Surface(
            modifier = Modifier
                .height(56.dp)
                .width(animateDpAsState(if (isSearchExpanded) 340.dp else 180.dp, label = "fabWidth").value),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onSearchClick(!isSearchExpanded) }.padding(start = 16.dp, end = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = isSearchExpanded, label = "SearchContent",
                        transitionSpec = { fadeIn(animationSpec = tween(200, 150)) togetherWith fadeOut(animationSpec = tween(150)) }
                    ) { searchActive ->
                        if (searchActive) {
                            searchContent()
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                                Spacer(Modifier.width(8.dp))
                                Text("Buscar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Box(
                    modifier = Modifier.size(56.dp).clickable { 
                        if (isSearchExpanded) onSearchClick(false) else onToolsClick(!isToolsExpanded)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = isSearchExpanded || isToolsExpanded, label = "ToolsIcon") { isExpanded ->
                         Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Tune,
                            contentDescription = if(isExpanded) "Cerrar" else "Herramientas"
                        )
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- NUEVO COMPONENTE REUTILIZABLE ---
// =================================================================================

@Composable
fun FavoriteCardItem(
    provider: PrestadorProfileFalso,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val cardColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isSelectionMode, onClick = onSelect),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onSelect() }, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            AsyncImage(
                model = provider.profileImageUrl,
                contentDescription = "Foto de perfil",
                fallback = painterResource(id = R.drawable.iconapp),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${provider.name} ${provider.lastName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = provider.services.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Text(text = "${provider.rating}", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (!isSelectionMode) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorito", tint = Color.Red)
            }
        }
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
    provider: PrestadorProfileFalso,
    onClick: () -> Unit, // Navegar al perfil
    onChat: (() -> Unit)? = null,
    onDeleteRequest: (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null
) {
    val appColors = getAppColors()
    var showDetailSheet by remember { mutableStateOf(false) } // Sheet para mostrar detalle expandido
    var showContextMenu by remember { mutableStateOf(false) }
    var showFavoriteDialog by remember { mutableStateOf(false) }
    
    // Estado local para favoritos (solo para visualización inmediata)
    var isFavoriteLocal by remember { mutableStateOf(provider.isFavorite) }

    val activeColor = Color(0xFF10B981) // Verde
    val inactiveColor = appColors.textSecondaryColor

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
                    if (provider.isOnline) {
                        Badge(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(16.dp)
                                .border(2.dp, appColors.surfaceColor, CircleShape),
                            containerColor = Color(0xFF10B981)
                        )
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
                        if (provider.isVerified) {
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
                    provider.companyName?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
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
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "${provider.rating}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        VerticalDivider(modifier = Modifier.height(12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Iconos Booleanos (Más compactos)
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "24hs",
                            modifier = Modifier.size(20.dp),
                            tint = if (provider.works24h) activeColor else inactiveColor
                        )
                        Icon(
                            imageVector = Icons.Default.Home, 
                            contentDescription = "Visita a Domicilio",
                            modifier = Modifier.size(20.dp),
                            tint = if (provider.doesHomeVisits) activeColor else inactiveColor
                        )
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Local Físico",
                            modifier = Modifier.size(20.dp),
                            tint = if (provider.hasPhysicalLocation) activeColor else inactiveColor
                        )

                        // [MODIFICADO] Icono de Favorito Visible
                        //Spacer(modifier = Modifier.weight(1f)) // Empuja el favorito a la derecha
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
                            text = provider.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textSecondaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Categorías / Servicios (Texto)
                Text("Servicios Ofrecidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    provider.services.forEach { service ->
                        ServiceTag(text = service, color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                // Detalles Booleanos
                Text("Disponibilidad y Modalidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                
                RowItemDetail(icon = Icons.Default.Schedule, text = "Disponible 24hs", isActive = provider.works24h)
                RowItemDetail(icon = Icons.Default.Home, text = "Visitas a Domicilio", isActive = provider.doesHomeVisits)
                RowItemDetail(icon = Icons.Default.Storefront, text = "Local Físico", isActive = provider.hasPhysicalLocation)

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
            icon = { Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary) },
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
            textDecoration = if (!isActive) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
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
