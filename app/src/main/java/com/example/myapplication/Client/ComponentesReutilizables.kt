package com.example.myapplication.Client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.getAppColors

// [NUEVO] Clase de datos para las acciones del menú moderno
data class MenuAction(
    val text: String,
    val icon: ImageVector,
    val isDestructive: Boolean = false,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit
)

/**
 * [COMPONENTE REUTILIZABLE]
 * Tarjeta unificada para mostrar la información de un prestador.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PrestadorCard(
    provider: PrestadorProfileFalso,
    onClick: () -> Unit,
    onChat: (() -> Unit)? = null,
    onDeleteRequest: (() -> Unit)? = null,
    actionContent: @Composable (() -> Unit)? = null
) {
    val appColors = getAppColors()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<() -> Unit>({}) }
    var confirmationTitle by remember { mutableStateOf("") }
    var confirmationMessage by remember { mutableStateOf("") }
    var confirmButtonText by remember { mutableStateOf("") }
    var isDestructiveAction by remember { mutableStateOf(false) }

    val fallbackCategoryColor = MaterialTheme.colorScheme.secondaryContainer
    val othersCategoryColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val activeAttributeColor = Color(0xFF388E3C)

    // Función auxiliar para obtener el color de la categoría
    fun getCategoryColor(categoryName: String): Color {
        return CategorySampleDataFalso.categories.find { it.name == categoryName }?.color
            ?: fallbackCategoryColor
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineColor, MaterialTheme.shapes.large)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        showBottomSheet = true
                    }
                ),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    if (provider.services.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val maxTagsToShow = 3
                            val displayTags = provider.services.take(maxTagsToShow)
                            val showOthers = provider.services.size > maxTagsToShow

                            displayTags.forEachIndexed { index, service ->
                                if (index == maxTagsToShow - 1 && showOthers) {
                                    ServiceTag(text = "Otros", color = othersCategoryColor)
                                } else {
                                    ServiceTag(text = service, color = getCategoryColor(service))
                                }
                            }
                        }
                        Divider(color = outlineColor, thickness = 1.dp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp,
                                top = if (provider.services.isNotEmpty()) 12.dp else 16.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                             modifier = Modifier.clickable(onClick = onClick) // Acción de perfil en la imagen
                        ) {
                            AsyncImage(
                                model = provider.profileImageUrl,
                                contentDescription = "Foto de perfil de ${provider.name}",
                                fallback = painterResource(id = R.drawable.iconapp),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            // [MODIFICACIÓN] El indicador de online se mueve arriba a la izquierda
                            if (provider.isOnline) {
                                Badge(
                                    modifier = Modifier
                                        // TopStart = Arriba Izquierda
                                        .align(Alignment.TopStart)
                                        .size(16.dp)
                                        .border(2.dp, appColors.surfaceColor, CircleShape),
                                    containerColor = Color(0xFF10B981)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onClick) // Acción de perfil en el texto
                        ) {
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
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Perfil Verificado",
                                        tint = appColors.accentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            provider.companyName?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = appColors.textSecondaryColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            
                            if (provider.companyName.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "24hs",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (provider.works24h) activeAttributeColor else appColors.textSecondaryColor.copy(alpha = 0.3f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Domicilio",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (provider.doesHomeVisits) activeAttributeColor else appColors.textSecondaryColor.copy(alpha = 0.3f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = "Local",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (provider.hasPhysicalLocation) activeAttributeColor else appColors.textSecondaryColor.copy(alpha = 0.3f)
                                    )
                                    
                                    Icon(
                                        imageVector = if (provider.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Favorito",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (provider.isFavorite) activeAttributeColor else appColors.textSecondaryColor.copy(alpha = 0.3f)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFFFC107)
                                    )
                                    Text(
                                        text = "${provider.rating}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textPrimaryColor
                                    )
                                }
                            }
                        }

                        // El actionContent ahora está envuelto en un Box con un clickable
                        if (actionContent != null && onChat != null) {
                            Box(modifier = Modifier.clickable(onClick = onChat)) {
                                actionContent()
                            }
                        } else if (actionContent != null){
                            actionContent()
                        }
                    }
                }
            }
        }
        
        // [NUEVO] Bottom Sheet para opciones (Menú inferior)
        if (showBottomSheet) {
            val actions = mutableListOf<MenuAction>()
            
            actions.add(MenuAction("Ver Perfil", Icons.Default.Person) {
                showBottomSheet = false
                onClick()
            })
            
            if (onChat != null) {
                actions.add(MenuAction("Enviar Mensaje", Icons.Default.Chat) {
                    showBottomSheet = false
                    onChat()
                })
            }

            // Lógica de Favoritos (Alternar)
            if (provider.isFavorite) {
                actions.add(MenuAction(
                    text = "Eliminar de Favoritos",
                    icon = Icons.Default.Delete, // O Icons.Default.FavoriteBorder
                    isDestructive = true,
                    onClick = {
                        showBottomSheet = false
                        // Preparar diálogo de confirmación para eliminar
                        confirmationTitle = "Eliminar de Favoritos"
                        confirmationMessage = "¿Estás seguro de que deseas eliminar a ${provider.name} de tus favoritos?"
                        confirmButtonText = "Eliminar"
                        isDestructiveAction = true
                        pendingAction = { 
                            SampleDataFalso.toggleFavorite(provider.id)
                            // Si se pasó un onDeleteRequest (ej: desde pantalla Favoritos), ejecutarlo también si es necesario, 
                            // pero toggleFavorite ya actualiza la lista global.
                            onDeleteRequest?.invoke()
                        }
                        showConfirmationDialog = true
                    }
                ))
            } else {
                actions.add(MenuAction(
                    text = "Agregar a Favoritos",
                    icon = Icons.Default.Favorite, // O Icons.Default.Add
                    isPrimary = true,
                    onClick = {
                        showBottomSheet = false
                        // Preparar diálogo de confirmación para agregar
                        confirmationTitle = "Agregar a Favoritos"
                        confirmationMessage = "¿Deseas agregar a ${provider.name} a tus favoritos?"
                        confirmButtonText = "Agregar"
                        isDestructiveAction = false
                        pendingAction = { 
                            SampleDataFalso.toggleFavorite(provider.id)
                        }
                        showConfirmationDialog = true
                    }
                ))
            }

            // Si se pasa onDeleteRequest explícitamente y NO es un favorito (ej: eliminar chat), se añade opción genérica de eliminar
            if (onDeleteRequest != null && !provider.isFavorite) {
                 actions.add(MenuAction("Eliminar", Icons.Default.Delete, isDestructive = true) {
                    showBottomSheet = false
                    confirmationTitle = "Eliminar Elemento"
                    confirmationMessage = "¿Estás seguro de realizar esta acción?"
                    confirmButtonText = "Eliminar"
                    isDestructiveAction = true
                    pendingAction = { onDeleteRequest() }
                    showConfirmationDialog = true
                })
            }

            GenericBottomSheet(
                onDismiss = { showBottomSheet = false },
                actions = actions
            )
        }

        // [NUEVO] Bottom Sheet de Confirmación
        if (showConfirmationDialog) {
            ConfirmationBottomSheet(
                title = confirmationTitle,
                message = confirmationMessage,
                confirmText = confirmButtonText,
                isDestructive = isDestructiveAction,
                onConfirm = {
                    pendingAction()
                    showConfirmationDialog = false
                },
                onCancel = { showConfirmationDialog = false }
            )
        }
    }
}

// [NUEVO] Menú Bottom Sheet Genérico
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericBottomSheet(
    onDismiss: () -> Unit,
    actions: List<MenuAction>
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Barra pequeña superior indicadora (handle)
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            actions.forEach { action ->
                BottomSheetItem(action)
            }
        }
    }
}

// [NUEVO] Item del menú Bottom Sheet
@Composable
fun BottomSheetItem(action: MenuAction) {
    Surface(
        onClick = action.onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Fondo sutil
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = when {
                    action.isDestructive -> Color.Red
                    action.isPrimary -> MaterialTheme.colorScheme.primary // O verde: Color(0xFF388E3C)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = action.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    action.isDestructive -> Color.Red
                    action.isPrimary -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

// [NUEVO] Diálogo de confirmación estilo Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationBottomSheet(
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive) Color.Red else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(confirmText)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ServiceTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color,
        contentColor = Color.Black
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActionContent(
    inDeleteMode: Boolean,
    onMessageClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = !inDeleteMode,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            IconButton(onClick = onMessageClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Ir al chat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = inDeleteMode,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            IconButton(onClick = onDeleteRequest) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun GenericFloatingMenu(
    icon: ImageVector,
    options: List<Pair<String, () -> Unit>>,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = icon, contentDescription = "Opciones")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (title, action) ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        expanded = false
                        action()
                    }
                )
            }
        }
    }
}