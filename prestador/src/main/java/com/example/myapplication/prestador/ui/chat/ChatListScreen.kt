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
import androidx.compose.ui.graphics.Brush
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
    conversations: List<ChatData.Conversation>,
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

    val totalConversaciones = conversations.size
    val noLeidos = conversations.sumOf { it.unreadCount }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            when {
                isDeletionMode -> TopAppBar(
                    title = { Text("${selectedChatsForDeletion.size} seleccionados") },
                    navigationIcon = {
                        IconButton(onClick = { onDeletionModeChange(false); onChatSelectionChange(emptySet()) }) {
                            Icon(Icons.Default.Close, "Cancelar", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onDeletionModeChange(false); onChatSelectionChange(emptySet()) }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.primaryOrange,
                        titleContentColor = Color.White
                    )
                )
                isSearchActive -> TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Buscar conversaciones...") },
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
                        IconButton(onClick = { onSearchActiveChange(false); onSearchQueryChange("") }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.primaryOrange)
                )
                else -> null
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.backgroundColor)
        ) {
            // ── HEADER estilo Inicio (solo en modo normal) ────────────────
            if (!isDeletionMode && !isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colors.primaryOrange,
                                    colors.primaryOrange.copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(start = 4.dp, end = 16.dp, bottom = 20.dp, top = 8.dp)
                ) {
                    Column {
                        // Fila título + acciones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                                }
                                Text("Conversaciones", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row {
                                IconButton(onClick = { onSearchActiveChange(true) }) {
                                    Icon(Icons.Default.Search, "Buscar", tint = Color.White)
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Settings, "Configuración", tint = Color.White)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Stats chips
                        Row(
                            modifier = Modifier.padding(start = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("$totalConversaciones chats", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            if (noLeidos > 0) {
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Default.MarkEmailUnread, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("$noLeidos sin leer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (isSelected) colors.primaryOrange.copy(alpha = 0.1f) else colors.surfaceColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Franja de color izquierda
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        if (conversation.unreadCount > 0) colors.primaryOrange else colors.primaryOrange.copy(alpha = 0.3f),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colors.primaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.userName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.userName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatTimestamp(conversation.timestamp),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        fontSize = 13.sp,
                        color = if (conversation.unreadCount > 0) colors.textPrimary else colors.textSecondary,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(colors.primaryOrange, CircleShape)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                // Indicadores de estado
                if (!conversation.notificationsEnabled || conversation.isLocked || !conversation.isVisible) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!conversation.notificationsEnabled)
                            Icon(Icons.Default.Notifications, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                        if (conversation.isLocked)
                            Icon(Icons.Default.Lock, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        if (!conversation.isVisible)
                            Icon(Icons.Default.Visibility, null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            if (inDeletionMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(checked = isSelected, onCheckedChange = null)
            }
        }
        } // cierre Row externo con franja
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
