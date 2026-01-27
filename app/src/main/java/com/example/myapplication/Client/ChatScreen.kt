package com.example.myapplication.Client

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.Data.ChatData
import com.example.myapplication.Models.Message
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.app.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.icu.number.NumberFormatter
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {}
) {
    // Manejar botón atrás del sistema
    BackHandler {
        onBack()
    }

    // Colores adaptativos
    val appColors = getAppColors()

    // Estado para la conversación activa
    var activeChatUserId by remember { mutableStateOf(initialProviderId) }

    // Notificar cuando cambia el estado de conversación
    LaunchedEffect(activeChatUserId) {
        onInConversationChange(activeChatUserId != null)
    }

    // Estado para los mensajes de la conversación activa
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

    // Estado para el texto del input
    var inputText by remember { mutableStateOf("") }
    //Estado para mostrar menú de adjuntos
    var showAttachMenu by remember { mutableStateOf(false) }

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
            //En lugar de: activeChatUserid = userId
            //Navegar a conversacion
            onChatClick = { userId ->
                if (navController != null) {
                    navController.navigate("chat_conversation/$userId")
                } else {
                    activeChatUserId = userId
                }
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
    var showAttachMenu by remember { mutableStateOf(false) }
    //Estados para agendar cita
    var showScheduleDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var appointmentNotes by remember { mutableStateOf("") }
    val context = LocalContext.current
    var isGettingLocation by remember { mutableStateOf(false) }

    // Función para obtener ubicación y enviar
    fun getLocationAndSend() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    // Crea el enlace de Google Maps
                    val googleMapsUrl = "https://maps.google.com/?q=$lat,$lng"
                    // Crea un mensaje con la ubicación
                    onSendMessage("📍 Mi ubicación\n$googleMapsUrl")
                    isGettingLocation = false
                }
            }
        } catch (e: SecurityException) {
            isGettingLocation = false
        }
    }

    // Launcher para pedir permiso de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            // Permiso concedido, obtener ubicación
            getLocationAndSend()
        }
    }

    // Auto-scroll al final cuando cambian los mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                appColors = appColors,
                onAttachMenuToggle = { showAttachMenu = !showAttachMenu }
            )
        }
        
        // Menú flotante de adjuntos - FUERA del flujo normal
        AnimatedVisibility(
            visible = showAttachMenu,
            enter = scaleIn(
                animationSpec = tween(300),
                transformOrigin = TransformOrigin(0f, 1f)
            ) + fadeIn(tween(200)),
            exit = scaleOut(
                animationSpec = tween(200),
                transformOrigin = TransformOrigin(0f, 1f)
            ) + fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 16.dp, start = 5.dp)
        ) {
            AttachmentOptionsMenu(
                onDismiss = { showAttachMenu = false },
                onImageClick = {
                    showAttachMenu = false
                    // TODO: Abrir galería
                },
                onLocationClick = {
                    showAttachMenu = false
                    // Verificar si ya tiene permisos
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        // Ya tiene permiso, obtener ubicación
                        getLocationAndSend()
                    } else {
                        // Pedir permisos
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onScheduleClick = {
                    showAttachMenu = false
                    showScheduleDialog = true
                }
            )
        }

        //Diálogo para agendar cita
        if (showScheduleDialog) {
            ScheduleAppointmentDialog(
                onDismiss = { showScheduleDialog = false },
                onConfirm = { date, time, notes ->
                    selectedDate = date
                    selectedTime = time
                    appointmentNotes = notes
                    showScheduleDialog = false


                    //TODO: Guardar en Firebase y enviar mensaje
                    // Formatear fecha para mensaje
                    val formattedMessage = "📅 Solicitud de cita|$date|$time|$notes"
                    onSendMessage(formattedMessage)
                }
            )
        }
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
fun buildAnnotatedStringWithLinks(text: String, linkColor: Color): AnnotatedString {
    val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
    return buildAnnotatedString {
        var lastIndex = 0
        urlPattern.findAll(text).forEach { matchResult ->
            // Texto antes del link
            append(text.substring(lastIndex, matchResult.range.first))
            
            // El link
            pushStringAnnotation(
                tag = "URL",
                annotation = matchResult.value
            )
            withStyle(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(matchResult.value)
            }
            pop()
            
            lastIndex = matchResult.range.last + 1
        }
        // Texto después del último link
        append(text.substring(lastIndex))
    }
}

@Composable
fun MessageBubble(
    message: Message,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    // Detectar si es un mensaje de cita
    val isAppointment = message.text.startsWith("📅 Solicitud de cita|")
    
    if (isAppointment) {
        // Extraer información de la cita
        val parts = message.text.removePrefix("📅 Solicitud de cita|").split("|")
        val date = parts.getOrNull(0) ?: ""
        val time = parts.getOrNull(1) ?: ""
        val notes = parts.getOrNull(2) ?: ""
        
        TarjetaMensajeCita(
            date = date,
            time = time,
            notes = notes,
            isFromMe = message.isFromMe,
            appColors = appColors
        )
    } else {
        // Mensaje normal
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
                    val context = LocalContext.current
                    val annotatedText = buildAnnotatedStringWithLinks(
                        text = message.text,
                        linkColor = if (message.isFromMe) Color.White else Color(0xFF2563EB)
                    )
                    
                    ClickableText(
                        text = annotatedText,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = if (message.isFromMe) Color.White else appColors.textPrimaryColor,
                            lineHeight = 20.sp
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                // Abrir el link en el navegador/Google Maps
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                context.startActivity(intent)
                            }
                        }
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
}

@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    appColors: com.example.myapplication.ui.theme.AppColors,
    onAttachMenuToggle: () -> Unit
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
                onClick = onAttachMenuToggle
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

@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onLocationClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    // Menú de burbujas simple
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Imágenes
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_image,
            label = "Imágenes",
            color = Color(0xFF8B5CF6),
            onClick = onImageClick
        )
        
        // Agendar cita
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_calendar,
            label = "Agendar cita",
            color = Color(0xFF3B82F6),
            onClick = onScheduleClick
        )
        
        // Ubicación
        AttachmentBubble(
            icon = com.example.myapplication.R.drawable.ic_location,
            label = "Ubicación",
            color = Color(0xFF10B981),
            onClick = onLocationClick
        )
    }
}

@Composable
fun AttachmentBubble(
    icon: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícono circular
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Etiqueta
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    val appColors = getAppColors()
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { mutableStateOf("")}
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    val selectedDateText = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = appColors.accentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Agendar Cita",
                    style = MaterialTheme.typography.titleLarge,
                    color = appColors.textPrimaryColor
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de fecha
                val dateInteractionSource = remember { MutableInteractionSource() }
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    placeholder = { Text("Seleccionar fecha") },
                    readOnly = true,
                    interactionSource = dateInteractionSource,
                    trailingIcon = { 
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.DateRange, 
                                contentDescription = null,
                                tint = appColors.accentBlue
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accentBlue,
                        focusedLabelColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledTextColor = appColors.textPrimaryColor
                    ),
                    singleLine = true
                )
                
                // Capturar el clic en el campo de fecha
                LaunchedEffect(dateInteractionSource) {
                    dateInteractionSource.interactions.collect { interaction ->
                        if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            showDatePicker = true
                        }
                    }
                }

                //Selector de hora
                val timeInteractionSource = remember { MutableInteractionSource() }
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = {},
                    readOnly = true,
                    interactionSource = timeInteractionSource,
                    label = { Text("Hora") },
                    placeholder = { Text("Seleccionar hora") },
                    trailingIcon = { 
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                Icons.Default.Schedule, 
                                contentDescription = null,
                                tint = appColors.accentBlue
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accentBlue,
                        focusedLabelColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f),
                        disabledTextColor = appColors.textPrimaryColor
                    ),
                    singleLine = true
                )
                
                // Capturar el clic en el campo de hora
                LaunchedEffect(timeInteractionSource) {
                    timeInteractionSource.interactions.collect { interaction ->
                        if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            showTimePicker = true
                        }
                    }
                }

                //Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    placeholder = { Text("Detalles del servicio...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = appColors.accentBlue,
                        focusedLabelColor = appColors.accentBlue,
                        unfocusedBorderColor = appColors.textSecondaryColor.copy(alpha = 0.5f)
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDateText.isNotEmpty() && selectedTime.isNotEmpty()) {
                        onConfirm(selectedDateText, selectedTime, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.accentBlue,
                    disabledContainerColor = appColors.accentBlue.copy(alpha = 0.5f)
                ),
                enabled = selectedDateText.isNotEmpty() && selectedTime.isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = appColors.textSecondaryColor
                )
            ) {
                Text("Cancelar")
            }
        },
        containerColor = appColors.surfaceColor,
        titleContentColor = appColors.textPrimaryColor,
        textContentColor = appColors.textPrimaryColor
    )
    
    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        
                        if (selectedDate != null && selectedDate >= today) {
                            selectedDateMillis = selectedDate
                            showDatePicker = false
                        }
                    }
                ) {
                    Text("Aceptar", color = appColors.accentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = appColors.textSecondaryColor)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = appColors.surfaceColor
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = appColors.surfaceColor,
                    titleContentColor = appColors.textPrimaryColor,
                    headlineContentColor = appColors.textPrimaryColor,
                    weekdayContentColor = appColors.textSecondaryColor,
                    subheadContentColor = appColors.textSecondaryColor,
                    dayContentColor = appColors.textPrimaryColor,
                    selectedDayContainerColor = appColors.accentBlue,
                    todayContentColor = appColors.accentBlue,
                    todayDateBorderColor = appColors.accentBlue
                )
            )
        }
    }
    
    // TimePicker personalizado
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.split(":").getOrNull(0)?.toIntOrNull() ?: 9,
            initialMinute = selectedTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = appColors.surfaceColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar Hora",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = appColors.surfaceColor,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = appColors.textPrimaryColor,
                            selectorColor = appColors.accentBlue,
                            containerColor = appColors.surfaceColor,
                            periodSelectorBorderColor = appColors.textSecondaryColor.copy(alpha = 0.3f),
                            periodSelectorSelectedContainerColor = appColors.accentBlue,
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = appColors.textSecondaryColor,
                            timeSelectorSelectedContainerColor = appColors.accentBlue.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = appColors.surfaceColor,
                            timeSelectorSelectedContentColor = appColors.accentBlue,
                            timeSelectorUnselectedContentColor = appColors.textPrimaryColor
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar", color = appColors.textSecondaryColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val hour = timePickerState.hour.toString().padStart(2, '0')
                                val minute = timePickerState.minute.toString().padStart(2, '0')
                                selectedTime = "$hour:$minute"
                                showTimePicker = false
                            }
                        ) {
                            Text("Aceptar", color = appColors.accentBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaMensajeCita(
    date: String,
    time: String,
    notes: String,
    isFromMe: Boolean,
    appColors: com.example.myapplication.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 220.dp, max = 280.dp)
                .background(
                    color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            // 1. CABECERA (Icono + Título)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isFromMe) Color.White.copy(alpha = 0.9f) else appColors.textPrimaryColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFromMe) "SOLICITUD ENVIADA" else "SOLICITUD DE CITA",
                    color = if (isFromMe) Color.White.copy(alpha = 0.9f) else appColors.textPrimaryColor.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // 2. FECHA Y HORA
            Text(
                text = "$date, $time",
                color = if (isFromMe) Color.White else appColors.textPrimaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 3. NOTAS (si hay)
            if (notes.isNotEmpty()) {
                Text(
                    text = notes,
                    color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 4. FOOTER - Estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isFromMe) Color.White.copy(alpha = 0.2f) else appColors.backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFromMe) "⏳ Esperando confirmación" else "Propuesta recibida",
                    color = if (isFromMe) Color.White else appColors.textPrimaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
