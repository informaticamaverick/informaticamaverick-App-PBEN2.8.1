package com.example.myapplication.Client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Data.ChatData
import com.example.myapplication.Models.ChatConversation
import com.example.myapplication.Models.Message
import com.example.myapplication.ui.theme.getAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    onBack: () -> Unit
) {
    // Manejar botón atrás del sistema
    BackHandler {
        onBack()
    }
    
    // Colores adaptativos
    val appColors = getAppColors()
    
    // Estado: null = lista de chats, non-null = conversación activa
    var activeChatUserId by remember { mutableStateOf<String?>(null) }
    
    // Estado: mensajes de la conversación activa
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    
    // Estado: texto del input
    var inputText by remember { mutableStateOf("") }
    
    // Cargar mensajes cuando se selecciona un chat
    LaunchedEffect(activeChatUserId) {
        if (activeChatUserId != null) {
            messages = ChatData.getMessagesForUser(activeChatUserId!!).toMutableList()
        }
    }
    
    // Decidir qué vista mostrar
    if (activeChatUserId == null) {
        // VISTA: Lista de conversaciones
        ChatListView(
            onChatClick = { userId ->
                activeChatUserId = userId
            },
            onBack = onBack,
            appColors = appColors
        )
    } else {
        // VISTA: Conversación activa
        val conversation = ChatData.getConversationById(activeChatUserId!!)
        
        if (conversation != null) {
            ChatConversationView(
                conversation = conversation,
                messages = messages,
                inputText = inputText,
                onInputChange = { inputText = it },
                onSendMessage = { messageText ->
                    // Agregar nuevo mensaje
                    val newMessage = Message(
                        id = System.currentTimeMillis(),
                        text = messageText,
                        isFromMe = true,
                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    )
                    messages = messages + newMessage
                    inputText = ""
                },
                onBack = {
                    activeChatUserId = null
                    inputText = ""
                },
                appColors = appColors
            )
        }
    }
}

@Composable
fun ChatListView(
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    // Estado para controlar categorías colapsadas
    var collapsedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Agrupar chats por profesión
    val groupedChats = ChatData.conversations.groupBy { it.job }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.backgroundColor)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.surfaceColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = appColors.textPrimaryColor
                    )
                }
                Text(
                    text = "Mensajes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimaryColor
                )
                // Espacio para balance visual
                Box(modifier = Modifier.size(48.dp))
            }
        }
        
        // Lista de conversaciones agrupadas por categoría
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedChats.forEach { (category, chats) ->
                // Header de categoría
                item(key = "header_$category") {
                    CategoryHeader(
                        category = category,
                        count = chats.size,
                        isCollapsed = collapsedCategories.contains(category),
                        onToggle = {
                            collapsedCategories = if (collapsedCategories.contains(category)) {
                                collapsedCategories - category
                            } else {
                                collapsedCategories + category
                            }
                        },
                        appColors = appColors
                    )
                }
                
                // Chats de la categoría (solo si no está colapsada)
                if (!collapsedCategories.contains(category)) {
                    items(
                        items = chats,
                        key = { chat -> chat.userId }
                    ) { conversation ->
                        ChatListItem(
                            conversation = conversation,
                            onClick = { onChatClick(conversation.userId) },
                            appColors = appColors
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar con indicador de estado online
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = conversation.avatarColor.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.name.first().toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = conversation.avatarColor
                        )
                    }
                }
                
                // Indicador verde de "en línea"
                if (conversation.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(appColors.accentGreen, CircleShape)
                            .border(2.dp, appColors.surfaceColor, CircleShape)
                    )
                }
            }
            
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
                        text = conversation.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimaryColor
                    )
                    Text(
                        text = conversation.lastMessageTime,
                        fontSize = 12.sp,
                        color = appColors.textSecondaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = appColors.textSecondaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChatConversationView(
    conversation: ChatConversation,
    messages: List<Message>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto-scroll al final cuando cambian los mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.backgroundColor)
    ) {
        // Header
        ChatHeader(
            conversation = conversation,
            onBack = onBack,
            appColors = appColors
        )
        
        // Mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    appColors = appColors
                )
            }
        }
        
        // Input
        MessageInputBar(
            inputText = inputText,
            onInputChange = onInputChange,
            onSend = {
                if (inputText.trim().isNotEmpty()) {
                    onSendMessage(inputText)
                }
            },
            appColors = appColors
        )
    }
}

@Composable
fun ChatHeader(
    conversation: ChatConversation,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = appColors.textPrimaryColor
                )
            }
            
            // Avatar
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = conversation.avatarColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = conversation.name.first().toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = conversation.avatarColor
                    )
                }
            }
            
            // Nombre y estado
            Column {
                Text(
                    text = conversation.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimaryColor
                )
                if (conversation.isOnline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(appColors.accentGreen, CircleShape)
                        )
                        Text(
                            text = "En línea",
                            fontSize = 12.sp,
                            color = appColors.accentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            ),
            color = if (message.isFromMe) appColors.accentBlue else appColors.surfaceColor,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (message.isFromMe) Color.White else appColors.textPrimaryColor,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.time,
                    fontSize = 11.sp,
                    color = if (message.isFromMe) Color.White.copy(alpha = 0.7f) else appColors.textSecondaryColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Input
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                color = appColors.backgroundColor
            ) {
                TextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Escribe un mensaje...",
                            color = appColors.textSecondaryColor
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimaryColor,
                        unfocusedTextColor = appColors.textPrimaryColor,
                        focusedContainerColor = appColors.backgroundColor,
                        unfocusedContainerColor = appColors.backgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = appColors.accentBlue
                    ),
                    maxLines = 4
                )
            }
            
            // Botón enviar
            IconButton(
                onClick = onSend,
                enabled = inputText.trim().isNotEmpty(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (inputText.trim().isNotEmpty()) appColors.accentBlue else appColors.dividerColor,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: String,
    count: Int,
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimaryColor
            )
            Text(
                text = "($count)",
                fontSize = 14.sp,
                color = appColors.textSecondaryColor
            )
        }
        
        Icon(
            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = if (isCollapsed) "Expandir" else "Colapsar",
            tint = appColors.textSecondaryColor
        )
    }
}
