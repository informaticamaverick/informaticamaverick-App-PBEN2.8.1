package com.example.myapplication.prestador.ui.chat

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.ChatData
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.viewmodel.ChatSimulationViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Estados de filtrado de chats
enum class ChatFilterState {
    ALL, NOTIFICATIONS_ON, VISIBLE,
    DATE_RANGE, LOCKED, UNREAD
}

// Modo de ordenamiento
enum class SortMode {
    ALPHABETICAL, RECENT
}

// Pantalla principal de chat para prestador con funcionalidades avanzadas
@Composable
fun PrestadorChatScreen(
    onBack: () -> Unit = {},
    onInConversationChange: (Boolean) -> Unit = {},
    onNavigateToPresupuesto: () -> Unit = {},
    chatSimulationViewModel: ChatSimulationViewModel,  // DEBE ser pasado - sin default
    initialChatUserId: String? = null
) {
    // Obtener el providerId del usuario actual
    val currentUser = FirebaseAuth.getInstance().currentUser
    val providerId = currentUser?.uid ?: ""
    
    println("📱 PrestadorChatScreen renderizado con ViewModel (${chatSimulationViewModel.hashCode()})")
    println("📱 Initial chat userId: $initialChatUserId")
    
    // Estado: null = lista de chats
    var activeChatUserId by remember {
        mutableStateOf<String?>(initialChatUserId)
    }

    // Estado: mensajes de la conversación activa
    var messages by remember {
        mutableStateOf<List<Message>>(emptyList())
    }

    // Estado: texto del input
    var inputText by remember {
        mutableStateOf("")
    }

    // Estado de ui avanzada
    var isSearchActive by remember {
        mutableStateOf(false)
    }
    var searchQuery by remember {
        mutableStateOf("")
    }
    var currentFilter by remember {
        mutableStateOf(ChatFilterState.ALL)
    }

    var sortMode by remember {
        mutableStateOf(SortMode.RECENT)
    }

    var isDeletionMode by remember {
        mutableStateOf(false)
    }
    var selectedChatsForDeletion by remember {
        mutableStateOf(setOf<String>())
    }

    // Estados de diálogos
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }
    
    // Notificar cuando cambia el estado de conversación
    LaunchedEffect(activeChatUserId) {
        onInConversationChange(activeChatUserId != null)
        if (activeChatUserId != null) {
            messages = ChatData.getMessagesForUser(activeChatUserId!!).toMutableList()
        }
    }

    // Manejar botón atrás del sistema
    BackHandler {
        when {
            activeChatUserId != null -> {
                activeChatUserId = null
                inputText = ""
            }
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
            isDeletionMode -> {
                isDeletionMode = false
                selectedChatsForDeletion = emptySet()
            }
            else -> onBack()
        }
    }

    // Animación con Crossfade
    Crossfade(
        targetState = activeChatUserId,
        animationSpec = tween(300)
    ) { chatUserId ->
        if (chatUserId == null) {
            // LISTA DE CHATS
            ChatListScreen(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                currentFilter = currentFilter,
                sortMode = sortMode,
                isDeletionMode = isDeletionMode,
                selectedChatsForDeletion = selectedChatsForDeletion,
                onSearchActiveChange = { isSearchActive = it },
                onSearchQueryChange = { searchQuery = it },
                onFilterChange = { currentFilter = it },
                onSortModeChange = { sortMode = it },
                onDeletionModeChange = { isDeletionMode = it },
                onChatSelectionChange = { selectedChatsForDeletion = it },
                onChatClick = { userId -> activeChatUserId = userId },
                onBack = onBack,
                onShowNotificationDialog = { showNotificationDialog = true },
                onShowVisibilityDialog = { showVisibilityDialog = true },
                onShowDateRangeDialog = { showDateRangeDialog = true },
                onShowLockDialog = { showLockDialog = true }
            )
        } else {
            // CONVERSACIÓN INDIVIDUAL
            val userName = ChatData.getConversationById(chatUserId)?.name ?: "Usuario"
            
            ChatConversationScreen(
                userId = chatUserId,
                userName = userName,
                providerId = providerId,  // Pasar providerId real
                onBack = {
                    activeChatUserId = null
                    inputText = ""
                },
                onNavigateToPresupuesto = onNavigateToPresupuesto
            )
        }
    }

    // Diálogos
    if (showNotificationDialog) {
        NotificationSettingsDialog(
            onDismiss = { showNotificationDialog = false },
            onConfirm = { enabled ->
                // TODO: Guardar configuración
                showNotificationDialog = false
            }
        )
    }
    
    if (showVisibilityDialog) {
        VisibilitySettingsDialog(
            onDismiss = { showVisibilityDialog = false },
            onConfirm = { enabled -> 
                showVisibilityDialog = false
            }
        )
    }
    
    if (showDateRangeDialog) {
        DateRangeDialog(
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { period -> 
                showDateRangeDialog = false
            }
        )
    }
    
    if (showLockDialog) {
        LockSettingsDialog(
            onDismiss = { showLockDialog = false },
            onConfirm = { locked -> 
                showLockDialog = false
            }
        )
    }
}
