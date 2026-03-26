package com.example.myapplication.presentation.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.ui.theme.getAppColors
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.data.local.AppDatabase

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    providerViewModel: ProviderViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val appColors = getAppColors()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. CARGAMOS DATOS GENERALES
    val allProviders by providerViewModel.providers.collectAsStateWithLifecycle()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    // 2. ESTADO DE NAVEGACIÓN LOCAL
    var activeChatId by remember { mutableStateOf(initialProviderId) }

    LaunchedEffect(activeChatId) {
        onInConversationChange(activeChatId != null)
    }

    BackHandler {
        if (activeChatId != null) activeChatId = null else onBack()
    }

    if (profileState.isLoading || profileState.uid.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = appColors.accentBlue)
        }
        //LaunchedEffect(Unit) { profileViewModel.loadUserProfile() }
    } else {
        val currentUserId = profileState.uid
        val chatRepository = remember { 
            val db = AppDatabase.getDatabase(context, scope)
            ChatRepository(db.chatDao(), db.budgetDao())
        }

        // 🔥 [CORRECCIÓN] OBTENER CONTEOS DE NO LEÍDOS
        val unreadCountsList by chatRepository.getUnreadCountsPerChat(currentUserId)
            .collectAsStateWithLifecycle(initialValue = emptyList())
        
        val unreadMap = remember(unreadCountsList) {
            unreadCountsList.associate { it.chatId to it.count }
        }

        // 4. LÓGICA DE VISTAS
        if (activeChatId == null) {
            // VISTA A: BANDEJA DE ENTRADA
            val activeChatIds by chatRepository.getActiveChatIds(currentUserId)
                .collectAsStateWithLifecycle(initialValue = emptyList())

            // 🔥 [CORRECCIÓN] ORDENAR POR RECIENCIA (Room ya los trae ordenados por MAX(timestamp))
            val myChats = remember(allProviders, activeChatIds) {
                activeChatIds.mapNotNull { id -> 
                    allProviders.find { it.uid == id }
                }
            }

            ChatListView(
                providersList = myChats,
                allCategories = emptyList(),
                unreadCounts = unreadMap, // 🔥 Pasamos el mapa de no leídos
                currentUserId = currentUserId,
                onChatClick = { selectedId -> activeChatId = selectedId },
                onBack = onBack,
                appColors = appColors,
                navController = navController
            )
        } else {
            // VISTA B: CONVERSACIÓN ESPECÍFICA
            val provider = allProviders.find { it.uid == activeChatId }

            if (provider != null) {
                val chatId = "chat_${currentUserId}_${provider.uid}"
                
                // 🔥 [NUEVO] MARCAR COMO LEÍDO AL ENTRAR
                LaunchedEffect(chatId) {
                    chatRepository.markChatAsRead(chatId, currentUserId)
                }

                val chatViewModel: ChatViewModel = viewModel(
                    key = chatId,
                    factory = ChatViewModelFactory(
                        repository = chatRepository,
                        chatId = chatId,
                        currentUserId = currentUserId,
                        receiverId = provider.uid,
                        context = context
                    )
                )

                ChatConversationView(
                    provider = provider,
                    viewModel = chatViewModel,
                    onBack = { activeChatId = null },
                    appColors = appColors
                )
            }
        }
    }
}
