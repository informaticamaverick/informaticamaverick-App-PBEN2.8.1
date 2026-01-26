package com.example.myapplication.Client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Data.ChatData
import com.example.myapplication.Models.Message
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null
) {
    // Manejar botón atrás del sistema
    BackHandler {
        onBack()
    }

    // Colores adaptativos
    val appColors = getAppColors()

    // Estado para la conversación activa
    var activeChatUserId by remember { mutableStateOf(initialProviderId) }

    // Estado para los mensajes de la conversación activa
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

    // Estado para el texto del input
    var inputText by remember { mutableStateOf("") }

    // Cargar mensajes cuando se selecciona un chat
    LaunchedEffect(activeChatUserId) {
        if (activeChatUserId != null) {
            val chatMessages = ChatData.getMessagesForUser(activeChatUserId!!)
            messages = chatMessages.ifEmpty {
                ChatData.getMessagesForUser("user_$activeChatUserId")
            }.toMutableList()
        } else {
            messages = emptyList()
        }
    }

    // Decidir qué vista mostrar
    if (activeChatUserId == null) {
        ChatListView(
            onChatClick = { userId ->
                activeChatUserId = userId
            },
            onBack = onBack,
            appColors = appColors
        )
    } else {
        // VISTA: Conversación activa
        // Obtenemos los datos del prestador para el header de la conversación
        val provider = SampleDataFalso.getPrestadorById(activeChatUserId!!)
        if (provider != null) {
            ChatConversationView(
                provider = provider,
                messages = messages,
                inputText = inputText,
                onInputChange = { inputText = it },
                onSendMessage = { messageText ->
                    // Agregar nuevo mensaje
                    val newMessage = Message(
                        id = System.currentTimeMillis(),
                        text = messageText,
                        isFromMe = true,
                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                        timestamp = System.currentTimeMillis()
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
        } else {
            // Manejar caso donde el providerId no es válido o no se encuentra
            // Podríamos mostrar un mensaje de error o volver a la lista de chats.
            Text("Error: Prestador no encontrado para ID $activeChatUserId", color = appColors.textPrimaryColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListView(
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    // PASO 1: Obtenemos una lista de 10 prestadores de ejemplo para simular chats
    var providersList by remember { mutableStateOf(SampleDataFalso.prestadores.take(10)) }

    // Estado para el diálogo de eliminación
    var providerToDelete by remember { mutableStateOf<PrestadorProfileFalso?>(null) }

    // Obtenemos las categorías (profesiones) de forma dinámica de los prestadores en el chat.
    val categories = remember(providersList) { providersList.map { it.services.first() }.distinct() }

    var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Filtramos los prestadores basándonos en los filtros seleccionados.
    val filteredProviders = remember(selectedCategories, providersList) {
        if (selectedCategories.isEmpty()) {
            providersList
        } else {
            providersList.filter { it.services.first() in selectedCategories }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.backgroundColor)
    ) {
        // --- Header ---
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                Box(modifier = Modifier.size(48.dp))
            }
        }

        // --- Filtros dinámicos ---
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(
                text = "Filtrar por profesión:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategories.contains(category)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories = if (isSelected) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        },
                        label = { Text(category) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Seleccionado",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        // --- Lista de Chats (Tarjetas rediseñadas) ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProviders, key = { it.id }) { provider ->
                PrestadorCard(
                    provider = provider,
                    onClick = { 
                        onChatClick(provider.id)
                    },
                    // onLongClick eliminado
                    onChat = { onChatClick(provider.id) },
                    onDeleteRequest = { providerToDelete = provider },
                    actionContent = {
                        ActionContent(
                            inDeleteMode = false,
                            onMessageClick = { onChatClick(provider.id) },
                            onDeleteRequest = { providerToDelete = provider }
                        )
                    }
                )
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (providerToDelete != null) {
        AlertDialog(
            onDismissRequest = { providerToDelete = null },
            title = { Text("Eliminar Conversación") },
            text = { Text("¿Estás seguro de que deseas eliminar el chat con ${providerToDelete!!.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        providersList = providersList.filter { it.id != providerToDelete!!.id }
                        providerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { providerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Ayer"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun DateLabel(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Text(
                text = date,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}


@Composable
fun ChatConversationView(
    provider: PrestadorProfileFalso,
    messages: List<Message>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val groupedMessages = messages.groupBy { formatDate(it.timestamp) }

    // Auto-scroll al final cuando cambian los mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
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
            provider = provider,
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
            groupedMessages.forEach { (date, messagesOnDate) ->
                item {
                    DateLabel(date)
                }
                items(messagesOnDate) { message ->
                    MessageBubble(
                        message = message,
                        appColors = appColors
                    )
                }
            }
        }

        // Input
        MessageInputBar(
            inputText = inputText,
            onInputChange = onInputChange,
            onSendMessage = onSendMessage,
            appColors = appColors
        )
    }
}

@Composable
fun ChatHeader(
    provider: PrestadorProfileFalso,
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = appColors.textPrimaryColor
                )
            }

            // Avatar
            Box {
                // Contenido del avatar... (sin cambios)
            }

            // Nombre y estado
            Column {
                Text(
                    text = "${provider.name} ${provider.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimaryColor
                )
                if (provider.isOnline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "En línea",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981),
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
    onSendMessage: (String) -> Unit,
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
            //Boton de adjuntar
            IconButton(
                onClick = { /* TODO: Implement attachment logic */ }
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Adjuntar",
                    tint = appColors.textSecondaryColor
                )
            }
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

            // Botón enviar o audio
            val isTextEmpty = inputText.trim().isEmpty()
            IconButton(
                onClick = {
                    if (!isTextEmpty) {
                        onSendMessage(inputText)
                    } else {
                        // TODO: Implement audio recording logic
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (!isTextEmpty) appColors.accentBlue else appColors.dividerColor,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isTextEmpty) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (isTextEmpty) "Grabar audio" else "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MyApplicationTheme {
        ChatScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ChatConversationViewPreview() {
    MyApplicationTheme {
        ChatConversationView(
            provider = SampleDataFalso.prestadores[0],
            messages = ChatData.getMessagesForUser("user_1"),
            inputText = "Hola, necesito ayuda",
            onInputChange = {},
            onSendMessage = {},
            onBack = {},
            appColors = getAppColors()
        )
    }
}
