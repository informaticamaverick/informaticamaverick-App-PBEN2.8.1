package com.example.myapplication.prestador.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de lista de chats con búsqueda y filtros avanzados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    serviceType: String,
    isSearchActive: Boolean,
    searchQuery: String,
    currentFilter: ChatFilterState,
    sortMode: SortMode,
    isDeletionMode: Boolean,
    selectedChatsForDeletion: Set<String>,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (ChatFilterState) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onDeletionModeChange: (Boolean) -> Unit,
    onChatSelectionChange: (Set<String>) -> Unit,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    onShowNotificationDialog: () -> Unit,
    onShowVisibilityDialog: () -> Unit,
    onShowDateRangeDialog: () -> Unit,
    onShowLockDialog: () -> Unit
) {
    val conversations = ChatData.getConversationsByServiceType(serviceType)
    val colors = getPrestadorColors()

    
    // Aplicar filtros
    val filteredConversations = conversations.filter { conv ->
        // Filtro de búsqueda
        val matchesSearch = if (searchQuery.isBlank()) {
            true
        } else {
            conv.userName.contains(searchQuery, ignoreCase = true) ||
            conv.lastMessage.contains(searchQuery, ignoreCase = true)
        }

        //Filtro por estado

        val matchesFilter = when (currentFilter) {
            ChatFilterState.ALL -> true
            ChatFilterState.NOTIFICATIONS_ON -> conv.notificationsEnabled
            ChatFilterState.VISIBLE -> conv.isVisible
            ChatFilterState.DATE_RANGE -> true
            //Implementar lógica de rango de fechas
            ChatFilterState.LOCKED -> conv.isLocked
            ChatFilterState.UNREAD -> conv.unreadCount > 0
        }
        matchesSearch && matchesFilter
    }


    //Aplicar ordenamiento
    val sortedConversations = when (sortMode)
    {
        SortMode.ALPHABETICAL -> filteredConversations.sortedBy { it.userName }
        SortMode.RECENT -> filteredConversations.sortedByDescending { it.timestamp }
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            if (isDeletionMode) {
                //Top para modo de eliminacion
                TopAppBar(
                    title = {
                        Text("${selectedChatsForDeletion.size} seleccionados") },
                    navigationIcon = {
                        IconButton(onClick = {
                            onDeletionModeChange(false)
                            onChatSelectionChange(emptySet())
                        }) {
                            Icon(Icons.Default.Close, "Cancelar")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            //Todo: elimanr chats seleccionados
                            onDeletionModeChange(false)
                            onChatSelectionChange(emptySet())
                        }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF6B35),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            } else if (isSearchActive) {
                //Barra de busqueda
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text("Buscar conversaciones...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onSearchActiveChange(false)
                            onSearchQueryChange("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF6B35)
                    )
                )
            } else {
                // TopBar normal con gradiente
                TopAppBar(
                    title = {
                        Text(
                            "Conversaciones",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Botón de búsqueda
                        IconButton(onClick = { onSearchActiveChange(true) }) {
                            Icon(Icons.Default.Search, "Buscar", tint = Color.White)
                        }
                        
                        // Botón de configuración
                        IconButton(onClick = { /* TODO: Abrir configuración */ }) {
                            Icon(Icons.Default.Settings, "Configuración", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFF6B35)
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de filtros avanzados
            if (!isSearchActive && !isDeletionMode) {
                FilterBar(
                    currentFilter = currentFilter,
                    sortMode = sortMode,
                    onFilterChange = onFilterChange,
                    onSortModeChange = onSortModeChange,
                    onShowNotificationDialog = onShowNotificationDialog,
                    onShowVisibilityDialog = onShowVisibilityDialog,
                    onShowDateRangeDialog = onShowDateRangeDialog,
                    onShowLockDialog = onShowLockDialog
                )
            }
            
            // Lista de conversaciones
            if (sortedConversations.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No se encontraron conversaciones"
                            } else {
                                "No tienes conversaciones"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(sortedConversations, key = { it.userId }) { conversation ->
                        ChatListItem(
                            conversation = conversation,
                            inDeletionMode = isDeletionMode,
                            isSelected = selectedChatsForDeletion.contains(conversation.userId),
                            onClick = {
                                if (isDeletionMode) {
                                    val newSelection = if (selectedChatsForDeletion.contains(conversation.userId)) {
                                        selectedChatsForDeletion - conversation.userId
                                    } else {
                                        selectedChatsForDeletion + conversation.userId
                                    }
                                    onChatSelectionChange(newSelection)
                                } else {
                                    onChatClick(conversation.userId)
                                }
                            },
                            onLongClick = {
                                onDeletionModeChange(true)
                                onChatSelectionChange(setOf(conversation.userId))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==================== PARTE 4: FILTER BAR Y CHAT LIST ITEM ====================

// Barra de filtros avanzados
@Composable
fun FilterBar(
    currentFilter: ChatFilterState,
    sortMode: SortMode,
    onFilterChange: (ChatFilterState) -> Unit,
    onSortModeChange: (SortMode) -> Unit,
    onShowNotificationDialog: () -> Unit,
    onShowVisibilityDialog: () -> Unit,
    onShowDateRangeDialog: () -> Unit,
    onShowLockDialog: () -> Unit
) {
    val colors = getPrestadorColors()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceColor)
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón de notificaciones
        item {
            FilterChip(
                selected = currentFilter == ChatFilterState.NOTIFICATIONS_ON,
                onClick = {
                    if (currentFilter == ChatFilterState.NOTIFICATIONS_ON) {
                        onFilterChange(ChatFilterState.ALL)
                    } else {
                        onShowNotificationDialog()
                        onFilterChange(ChatFilterState.NOTIFICATIONS_ON)
                    }
                },
                label = { Text("Notificaciones") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Botón de visibilidad
        item {
            FilterChip(
                selected = currentFilter == ChatFilterState.VISIBLE,
                onClick = {
                    if (currentFilter == ChatFilterState.VISIBLE) {
                        onFilterChange(ChatFilterState.ALL)
                    } else {
                        onShowVisibilityDialog()
                        onFilterChange(ChatFilterState.VISIBLE)
                    }
                },
                label = { Text("Visibles") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Botón de rango de fechas
        item {
            FilterChip(
                selected = currentFilter == ChatFilterState.DATE_RANGE,
                onClick = {
                    if (currentFilter == ChatFilterState.DATE_RANGE) {
                        onFilterChange(ChatFilterState.ALL)
                    } else {
                        onShowDateRangeDialog()
                        onFilterChange(ChatFilterState.DATE_RANGE)
                    }
                },
                label = { Text("Fecha") },
                leadingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Botón de bloqueados
        item {
            FilterChip(
                selected = currentFilter == ChatFilterState.LOCKED,
                onClick = {
                    if (currentFilter == ChatFilterState.LOCKED) {
                        onFilterChange(ChatFilterState.ALL)
                    } else {
                        onShowLockDialog()
                        onFilterChange(ChatFilterState.LOCKED)
                    }
                },
                label = { Text("Bloqueados") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Botón de no leídos
        item {
            FilterChip(
                selected = currentFilter == ChatFilterState.UNREAD,
                onClick = {
                    if (currentFilter == ChatFilterState.UNREAD) {
                        onFilterChange(ChatFilterState.ALL)
                    } else {
                        onFilterChange(ChatFilterState.UNREAD)
                    }
                },
                label = { Text("No leídos") },
                leadingIcon = {
                    Icon(
                        Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Divisor
        item {
            Divider(
                modifier = Modifier
                    .width(2.dp)
                    .height(32.dp)
                    .padding(vertical = 4.dp),
                color = Color.LightGray
            )
        }

        // Ordenar alfabéticamente
        item {
            FilterChip(
                selected = sortMode == SortMode.ALPHABETICAL,
                onClick = {
                    onSortModeChange(
                        if (sortMode == SortMode.ALPHABETICAL) SortMode.RECENT else SortMode.ALPHABETICAL
                    )
                },
                label = { Text("A-Z") },
                leadingIcon = {
                    Icon(
                        Icons.Default.SortByAlpha,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Ordenar por fecha reciente
        item {
            FilterChip(
                selected = sortMode == SortMode.RECENT,
                onClick = {
                    onSortModeChange(
                        if (sortMode == SortMode.RECENT) SortMode.ALPHABETICAL else SortMode.RECENT
                    )
                },
                label = { Text("Reciente") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

// Elemento de lista de chat (tarjeta de conversación)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListItem(
    conversation: ChatData.Conversation,
    isSelected: Boolean,
    inDeletionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = getPrestadorColors()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) Color(0xFFE3F2FD) else colors.surfaceColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isSelected) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color(0xFFF97316)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = conversation.userName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del chat
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    // Timestamp
                    Text(
                        text = formatTimestamp(conversation.timestamp),
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Último mensaje
                    Text(
                        text = conversation.lastMessage,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge de mensajes no leídos
                    if (conversation.unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF97316),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Indicadores de estado
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de notificaciones silenciadas
                    if (!conversation.notificationsEnabled) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones desactivadas",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Icono de chat bloqueado
                    if (conversation.isLocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Chat bloqueado",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Icono de chat oculto
                    if (!conversation.isVisible) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Chat oculto",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Checkbox para modo eliminación
            if (inDeletionMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
            }
        }
    }
}

// Función auxiliar para formatear timestamp
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Ahora" // Menos de 1 minuto
        diff < 3600000 -> "${diff / 60000}m" // Menos de 1 hora
        diff < 86400000 -> "${diff / 3600000}h" // Menos de 1 día
        diff < 604800000 -> { // Menos de 1 semana
            val days = diff / 86400000
            "${days}d"
        }
        else -> {
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
